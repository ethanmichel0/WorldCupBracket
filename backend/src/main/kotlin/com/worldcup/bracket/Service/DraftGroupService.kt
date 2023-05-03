package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.worldcup.bracket.GetFootballDataEndpoints

import com.worldcup.bracket.DTO.LeagueResponse
import com.worldcup.bracket.DTO.NewDraftGroup
import com.worldcup.bracket.DTO.DraftGroupUpdateAfterPlayerDrafted
import com.worldcup.bracket.Repository.DraftGroupRepository
import com.worldcup.bracket.Repository.UserRepository
import com.worldcup.bracket.Repository.LeagueRepository
import com.worldcup.bracket.Repository.PlayerDraftRepository
import com.worldcup.bracket.Repository.PlayerSeasonRepository
import com.worldcup.bracket.Repository.ScheduledTaskRepository
import com.worldcup.bracket.Entity.DraftGroup
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.Entity.PlayerDraft
import com.worldcup.bracket.Entity.ScheduleType
import com.worldcup.bracket.Entity.TaskType
import com.worldcup.bracket.Entity.User

import java.security.Principal
import java.time.Instant

import java.util.GregorianCalendar
import java.util.Calendar

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.google.gson.Gson; 

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class DraftGroupService(private val draftGroupRepository: DraftGroupRepository, 
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val playerDraftRepository: PlayerDraftRepository,
    private val playerSeasonRepository: PlayerSeasonRepository,
    private val scheduledTaskRepository: ScheduledTaskRepository,
    private val leagueRepository: LeagueRepository,
    private val footballAPIData: GetFootballDataEndpoints,
    private val leagueService: LeagueService,
    private val schedulerService: SchedulerService,
    private val simpMessagingTemplate: SimpMessagingTemplate
    ) {

    private val httpClient = HttpClient.newHttpClient()
    private val logger : Logger = LoggerFactory.getLogger(javaClass)
    
    companion object {
        public val MAX_MEMBERS_PER_DRAFT_GROUP = 10
        public val GROUP_DNE = "Group Does Not Exist"
        public val INVALID_PW = "Password Does Not Match"
        public val ALREADY_IN_GROUP = "Member Already In Group"
        public val GROUP_FULL = "Group Size Is Already At Maximum"
        public val GROUP_ALREADY_EXISTS = "Group Already Exists"
        public val NOT_PERMITTED = "You must be the owner of the group to do this action"
        public val TIME_NOT_VALID = "The draft time you set is not valid."
        public val TEN_MINUTES_IN_SECONDS = 10 * 60
        public val NOT_ENOUGH_MEMBERS = "You must have at least two members in group to create a draft"
        public val NO_USER_TO_REMOVE = "The user you are trying to remove does not exist"
        public val WRONG_NUMBER_LEAGUES = "You must have at least one league, and if you have multiple, all must follow the same schedule (start and end same time of year)"
        public val TOO_LATE_TO_DRAFT = "One or more of your draft leagues is now too late to draft players"
        public val NOT_YOUR_TURN = "It it not your time to draft, please be patient"
        public val PLAYER_NOT_AVAILABLE = "The player that you are trying to draft is not currently available"
        public val NAME_ALREADY_TAKEN = "This league name is already taken"
    }

    public fun getAllDraftGroupsForUser(principal: Principal) : List<DraftGroup> {
        val relevantUser = userRepository.findByName(principal.getName())[0]
        return draftGroupRepository.findByIdIn(relevantUser.draftGroups.map{dg -> dg.id.toString()})
    }


    public fun saveNewDraftGroup(body: NewDraftGroup, principal: Principal) {
        // TODO use username/email as user identifier
        val password = passwordEncoder.encode(body.password)
        val allDraftGroupsSameName = draftGroupRepository.findByName(body.name)
        val owner = userRepository.findByName(principal.getName())[0]

        if (body.leagueIds == null) {
            throw Exception(WRONG_NUMBER_LEAGUES)
        }
        val leaguesForDraft = leagueRepository.findByIdIn(body.leagueIds).toMutableList()

        if (draftGroupRepository.findByName(body.name).size>0) {
            throw Exception(NAME_ALREADY_TAKEN)
        }

        if (leaguesForDraft.map{it.scheduleType}.distinct().size != 1) {
            leaguesForDraft.forEach{x -> println(x.scheduleType)}
            throw Exception(WRONG_NUMBER_LEAGUES)
        }
        leaguesForDraft.forEach{league ->
            if (league.lastDateToDraft == null || league.lastDateToDraft <= Instant.now().getEpochSecond()) {
                throw Exception(TIME_NOT_VALID)
            }
        }
        if (allDraftGroupsSameName.size != 0) throw Exception(GROUP_ALREADY_EXISTS)

        // figure out what the current season is by sending request to API
        val requestLeague = BuildNewRequest(footballAPIData.getLeagueEndpoint(body.leagueIds!![0]),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)

        val responseLeague = httpClient.send(requestLeague, HttpResponse.BodyHandlers.ofString());
        val responseWrapperLeague : LeagueResponse = Gson().fromJson(responseLeague.body(), LeagueResponse::class.java)
        
        val latestSeason = leagueService.getLatestSeasonGivenLeagueResponseFromAPI(responseWrapperLeague.response[0].seasons)

        val newDraftGroup = DraftGroup(
            name=body.name,
            password=password,
            owner=owner,
            members=mutableListOf(owner),
            leagues=leaguesForDraft,
            season = latestSeason,
            amountOfTimeEachTurn = body.amountTimePerTurnInSeconds,
            numberOfPlayersEachTeam = body.numPlayers
        )

        draftGroupRepository.save(newDraftGroup)
        owner.draftGroups.add(newDraftGroup)
        userRepository.save(owner)
    }

    public fun joinDraftGroup(body: NewDraftGroup, principal: Principal) {
        val currentUser = userRepository.findByName(principal.getName())[0]
        val group = draftGroupRepository.findByName(body.name)

        if (group.size == 0) throw Exception(GROUP_DNE)
        if (! passwordEncoder.matches(body.password,group[0].password)) throw Exception(INVALID_PW)
        if (group[0].members.contains(currentUser)) throw Exception (ALREADY_IN_GROUP)
        if (group[0].members.size == MAX_MEMBERS_PER_DRAFT_GROUP) throw Exception(GROUP_FULL)
        

        group[0].members.add(currentUser)
        draftGroupRepository.save(group[0])

        currentUser.draftGroups.add(group[0])
        userRepository.save(currentUser)
    }

    public fun getSpecificDraftGroup(name: String) : DraftGroup {
        val group = draftGroupRepository.findByName(name)
        if (group.size==0) throw Exception(GROUP_DNE)
        return group[0]
    }

    public fun removeUserFromDraftGroup(draftGroupName: String, userId: String, principal: Principal) {

        val currentUser = userRepository.findByName(principal.getName())[0]
        val groups = draftGroupRepository.findByName(draftGroupName)
        val userToRemove = userRepository.findByIdOrNull(userId)

        if (groups.size==0) throw Exception(GROUP_DNE)
        if (userToRemove == null) throw Exception(NO_USER_TO_REMOVE)
        if (currentUser != groups[0].owner) throw Exception(NOT_PERMITTED)
        val group = groups[0]
        group.members.remove(userToRemove)
        userToRemove.draftGroups.remove(group)
        userRepository.save(userToRemove)
    }

    public fun removeDraftGroup(draftGroupName: String, principal: Principal) {
        val currentUser = userRepository.findByName(principal.getName())[0]
        val groups = draftGroupRepository.findByName(draftGroupName)

        if (groups.size==0) throw Exception(GROUP_DNE)
        if (currentUser != groups[0].owner) throw Exception(NOT_PERMITTED)
        val group = groups[0]
        val listOfMembersInGroup = mutableListOf<User>()
        for (member in group.members) {
            member.draftGroups.remove(group)
            listOfMembersInGroup.add(member)
        }
        draftGroupRepository.delete(groups[0])
        playerDraftRepository.deleteAll(playerDraftRepository.findAllPlayerDraftsByGroup(draftGroupName))
        userRepository.saveAll(listOfMembersInGroup)
    }

    public fun scheduleDraftGroup(draftGroupName: String, time: Long, principal: Principal) {
        val currentUser = userRepository.findByName(principal.getName())[0]
        val groups = draftGroupRepository.findByName(draftGroupName)

        if (groups.size == 0) throw Exception(GROUP_DNE)
        val group = groups[0]
        if (currentUser != group.owner) throw Exception(NOT_PERMITTED)
        if (group.members.size < 2) throw Exception(NOT_ENOUGH_MEMBERS)

        val instant = Instant.now()

        group.leagues.forEach{league ->
            if (league.lastDateToDraft == null || league.lastDateToDraft <= time || time <= instant.getEpochSecond()) {
                throw Exception(TIME_NOT_VALID)
            }
        }

        if (group.draftTime == -1L) { // indicates that this is first time calling this method/setting the draft time
            // randomly shuffle members, but only on first time, that way someone can't call this method multiple times if they are unhappy with ordering
            group.availablePlayers.addAll(playerSeasonRepository.findAllPlayerSeasonsByLeaguesAndSeason(group.leagues.map{it.id},group.season).toMutableList())
            val playerDrafts : MutableList<PlayerDraft> = mutableListOf<PlayerDraft>()
            
            group.members.shuffle()
            group.members.forEach{playerInDraftGroup ->
            playerDrafts.add(PlayerDraft(
                user = playerInDraftGroup,
                draftGroup = group
            ))
            }
            playerDraftRepository.saveAll(playerDrafts)
        } else {
            if (group.draftTime <= instant.getEpochSecond()) {
                throw Exception(TIME_NOT_VALID)
            }

            // if there was a preexisting draft time that is changed/updated, ensure that previous task to start draft is cancelled
            scheduledTaskRepository.delete(schedulerService.removeTaskFromScheduler(scheduledTaskRepository.findByRelatedEntity(group.id.toString())[0].id.toString()))
        }

        val whenFirstPlayerMustChooseDeadline = GregorianCalendar()
        whenFirstPlayerMustChooseDeadline.setTimeInMillis(time * 1000)
        whenFirstPlayerMustChooseDeadline.add(Calendar.SECOND,group.amountOfTimeEachTurn)

        scheduledTaskRepository.save(schedulerService.addNewTask(
            task = Runnable{
                draftRandomPlayerForCurrentUser(group.name)
            },
            startTime = whenFirstPlayerMustChooseDeadline.toInstant(),
            type = TaskType.ScheduleDraft,
            repeatEvery = null,
            relatedEntity = group.id.toString()
        ))

        group.draftTime = time
        draftGroupRepository.save(group)
    }

    public fun draftPlayerForUser(groupName: String, playerId: String, principal: Principal) : DraftGroupUpdateAfterPlayerDrafted {

        val group = draftGroupRepository.findByName(groupName)[0]
        val userCurrentTurn : User = group.members[group.indexOfCurrentUser]
        val userMakingRequest : User = userRepository.findByName(principal.getName())[0]

        if (userMakingRequest != userCurrentTurn) {
            throw Exception(NOT_YOUR_TURN)
        }

        if (!group.availablePlayers.map{it.player.id}.contains(playerId)) {
            throw Exception(PLAYER_NOT_AVAILABLE)
        }

        val player = playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(group.season,playerId)[0]
        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUser(groupName,userCurrentTurn.id.toString())[0]

        group.availablePlayers.remove(player)
        playerDraft.players.add(player)
        group.numberPlayersDrafted++

        draftGroupRepository.save(group)
        playerDraftRepository.save(playerDraft)

        // since user is making choice before their time expires, we reset the time for the next user whose turn it is
        scheduledTaskRepository.delete(schedulerService.removeTaskFromScheduler(scheduledTaskRepository.findByRelatedEntity(group.id.toString())[0].id.toString()))

        // check to make sure that all teams aren't yet full
        if (! group.draftComplete) {
            val timeNextPlayerRunsOutOfTime = GregorianCalendar()
            timeNextPlayerRunsOutOfTime.add(Calendar.SECOND,group.amountOfTimeEachTurn)

            scheduledTaskRepository.save(schedulerService.addNewTask(
                task = Runnable{
                    group.indexOfCurrentUser = if (group.indexOfCurrentUser<group.members.size-1) group.indexOfCurrentUser + 1 else 0
                    draftGroupRepository.save(group)
                    draftRandomPlayerForCurrentUser(groupName)
                },
                startTime = timeNextPlayerRunsOutOfTime.toInstant(),
                type = TaskType.ScheduleDraft,
                repeatEvery = null,
                relatedEntity = group.id.toString()
            ))
        }

        return DraftGroupUpdateAfterPlayerDrafted(player,group.availablePlayers)
    }

    // if user runs out of time to pick automatically select a player (randomly)
    private fun draftRandomPlayerForCurrentUser(groupName: String) {
        val group = draftGroupRepository.findByName(groupName)[0]
        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUser(groupName,group.members[group.indexOfCurrentUser].id.toString())[0]

        val player = group.availablePlayers.random()

        group.availablePlayers.remove(player)
        playerDraft.players.add(player)
        group.numberPlayersDrafted++

        draftGroupRepository.save(group)
        playerDraftRepository.save(playerDraft)

        val timeNextPlayerRunsOutOfTime = GregorianCalendar()
        timeNextPlayerRunsOutOfTime.add(Calendar.SECOND,group.amountOfTimeEachTurn)

        if (! group.draftComplete) {
            scheduledTaskRepository.save(schedulerService.addNewTask(
                task = Runnable{
                    group.indexOfCurrentUser = if (group.indexOfCurrentUser<group.members.size-1) group.indexOfCurrentUser + 1 else 0
                    draftGroupRepository.save(group)
                    draftRandomPlayerForCurrentUser(groupName)
                },
                startTime = timeNextPlayerRunsOutOfTime.toInstant(),
                type = TaskType.ScheduleDraft,
                repeatEvery = null,
                relatedEntity = group.id.toString()
            ))
        }

        simpMessagingTemplate.convertAndSend("/topic/draft/${groupName}", 
            DraftGroupUpdateAfterPlayerDrafted(player,group.availablePlayers));
    }
}
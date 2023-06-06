package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.worldcup.bracket.GetFootballDataEndpoints

import org.bson.types.ObjectId

import com.worldcup.bracket.DTO.LeagueResponse
import com.worldcup.bracket.DTO.NewDraftGroup
import com.worldcup.bracket.DTO.DraftGroupInfoDuringDraft
import com.worldcup.bracket.DTO.DraftGroupWithMemberInformation
import com.worldcup.bracket.Repository.DraftGroupRepository
import com.worldcup.bracket.Repository.UserRepository
import com.worldcup.bracket.Repository.LeagueRepository
import com.worldcup.bracket.Repository.PlayerDraftRepository
import com.worldcup.bracket.Repository.PlayerSeasonRepository
import com.worldcup.bracket.Repository.ScheduledTaskRepository
import com.worldcup.bracket.Entity.DraftGroup
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.Entity.PlayerSeason
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
        public val TIME_NOT_VALID = "The draft time you set is not valid. It must be at least 10 minutes in the future and before the end of the season."
        public val TEN_MINUTES_IN_SECONDS = 1 * 60
        // todo change back from 3 minutes to 10 minutes
        public val NOT_ENOUGH_MEMBERS = "You must have at least two members in group to create a draft"
        public val NO_USER_TO_REMOVE = "The user you are trying to remove does not exist"
        public val WRONG_NUMBER_LEAGUES = "You must have at least one league, and if you have multiple, all must follow the same schedule (start and end same time of year)"
        public val TOO_LATE_TO_DRAFT = "One or more of your draft leagues is now too late to draft players"
        public val NOT_YOUR_TURN = "It it not your time to draft, please be patient"
        public val PLAYER_NOT_AVAILABLE = "The player that you are trying to draft is not currently available"
        public val NAME_ALREADY_TAKEN = "This league name is already taken"
        public val THIS_DRAFT_NOT_ONGOING = "This draft is either not yet started or already completed"
        public val DRAFT_NOT_SCHEDULED = "You must first schedule a draft before viewing the available players and your players"
        public val MUST_BE_MEMBER = "You must be a member of the relevant draft group to view information about it"
    }

    public fun getAllDraftGroupsForUser(principal: Principal) : List<DraftGroup> {
        val relevantUser = userRepository.findByPrincipalId(principal.getName())[0]
        return draftGroupRepository.findByIdIn(relevantUser.draftGroups)
    }


    public fun saveNewDraftGroup(body: NewDraftGroup, principal: Principal) {
        val password = passwordEncoder.encode(body.password)
        val allDraftGroupsSameName = draftGroupRepository.findByName(body.name)
        val owner = userRepository.findByPrincipalId(principal.getName())[0]

        if (body.leagueIds == null) {
            throw Exception(WRONG_NUMBER_LEAGUES)
        }
        val leaguesForDraft = leagueRepository.findByIdIn(body.leagueIds).toMutableList()

        if (draftGroupRepository.findByName(body.name).size>0) {
            throw Exception(NAME_ALREADY_TAKEN)
        }

        if (leaguesForDraft.map{it.scheduleType}.distinct().size != 1) {
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
        
        val latestSeason = 2022

        val newDraftGroup = DraftGroup(
            name=body.name,
            password=password,
            owner=owner,
            members=mutableListOf(owner.email),
            leagues=leaguesForDraft,
            season = latestSeason,
            amountOfTimeEachTurn = body.amountTimePerTurnInSeconds,
            numberOfPlayersEachTeam = body.numPlayers
        )

        draftGroupRepository.save(newDraftGroup)

        owner.draftGroups.add(newDraftGroup.id)
        userRepository.save(owner)
    }

    public fun joinDraftGroup(body: NewDraftGroup, principal: Principal) {
        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]
        val groups = draftGroupRepository.findByName(body.name)

        if (groups.size == 0) throw Exception(GROUP_DNE)
        val group = groups[0]
        if (! passwordEncoder.matches(body.password,group.password)) throw Exception(INVALID_PW)
        if (group.members.contains(currentUser.email)) throw Exception (ALREADY_IN_GROUP)
        if (group.members.size == MAX_MEMBERS_PER_DRAFT_GROUP) throw Exception(GROUP_FULL)
        

        group.members.add(currentUser.email)
        draftGroupRepository.save(group)

        currentUser.draftGroups.add(group.id)
        userRepository.save(currentUser)
    }

    public fun getSpecificDraftGroup(name: String, principal: Principal) : DraftGroupWithMemberInformation {
        val groups = draftGroupRepository.findByName(name)
        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]
        if (groups.size==0) throw Exception(GROUP_DNE)
        val group = groups[0]
        if (! group.members.contains(currentUser.email)) {
            throw Exception(MUST_BE_MEMBER) 
         }
        val playerDrafts = if (group.availablePlayers.size > 0) playerDraftRepository.findAllPlayerDraftsByGroup(group.name) else null
        // availablePlayers > 0 indicates that playerDraft objects have been created already and a draft has been scheduled

        val members = userRepository.findByEmailIn(group.members)

        return DraftGroupWithMemberInformation(
            draftGroup=group,
            playerDrafts=playerDrafts,
            members=members
        )
    }

    public fun removeUserFromDraftGroup(draftGroupName: String, userId: String, principal: Principal) {

        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]
        val groups = draftGroupRepository.findByName(draftGroupName)
        val userToRemove = userRepository.findByIdOrNull(userId)

        if (groups.size==0) throw Exception(GROUP_DNE)
        if (userToRemove == null) throw Exception(NO_USER_TO_REMOVE)
        if (currentUser != groups[0].owner) throw Exception(NOT_PERMITTED)
        val group = groups[0]
        group.members.remove(userToRemove.email)

        userToRemove.draftGroups.remove(group.id)
    }

    public fun removeDraftGroup(draftGroupName: String, principal: Principal) {
        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]
        val groups = draftGroupRepository.findByName(draftGroupName)

        if (groups.size==0) throw Exception(GROUP_DNE)
        if (currentUser != groups[0].owner) throw Exception(NOT_PERMITTED)
        val group = groups[0]
        draftGroupRepository.delete(group)
        playerDraftRepository.deleteAll(playerDraftRepository.findAllPlayerDraftsByGroup(draftGroupName))

        val allMembersInGroup = userRepository.findByEmailIn(group.members)
        allMembersInGroup.forEach{it.draftGroups.remove(group.id)}
        userRepository.saveAll(allMembersInGroup)
    }

    public fun scheduleDraftGroup(draftGroupName: String, time: Long, principal: Principal) {
        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]
        val groups = draftGroupRepository.findByName(draftGroupName)

        if (groups.size == 0) throw Exception(GROUP_DNE)
        val group = groups[0]
        if (currentUser != group.owner) throw Exception(NOT_PERMITTED)
        if (group.members.size < 2) throw Exception(NOT_ENOUGH_MEMBERS)

        val instant = Instant.now()

        group.leagues.forEach{league ->
            if (league.lastDateToDraft == null || league.lastDateToDraft <= time || time <= instant.getEpochSecond() + TEN_MINUTES_IN_SECONDS) {
                throw Exception(TIME_NOT_VALID)
            }
        }

        if (group.draftTime == -1L) { // indicates that this is first time calling this method/setting the draft time
            // randomly shuffle members, but only on first time, that way someone can't call this method multiple times if they are unhappy with ordering
            group.availablePlayers.addAll(playerSeasonRepository.findAllPlayerSeasonsByLeaguesAndSeason(group.leagues.map{it.id},group.season).toMutableList())
            val playerDrafts : MutableList<PlayerDraft> = mutableListOf<PlayerDraft>()
            
            group.members.shuffle()
            group.members.forEach{playerInDraftGroupEmail ->
                playerDrafts.add(PlayerDraft(
                    userEmail = playerInDraftGroupEmail,
                    draftGroup = group
                ))
            }
            playerDraftRepository.saveAll(playerDrafts)
        } else {
            group.members.shuffle()
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

    public fun getDraftGroupInfoDuringDraft(groupName: String, principal: Principal) : DraftGroupInfoDuringDraft {
        val groups = draftGroupRepository.findByName(groupName)
        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]

        if (groups.size==0) throw Exception(GROUP_DNE)
        val group = groups[0]
        
        if (group.draftTime == -1L) {
            throw Exception(DRAFT_NOT_SCHEDULED)
        }

        println("all members: ${group.members}")
        println("current user email: ${currentUser.email}")

        if (! group.members.contains(currentUser.email)) {
           throw Exception(MUST_BE_MEMBER) 
        }


        return DraftGroupInfoDuringDraft(
            draftGroup = group,
            playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(group.name,currentUser.email)[0]
        )
    }

    public fun draftPlayerForUser(groupName: String, playerId: String, principal: Principal) : DraftGroupInfoDuringDraft {
        val group = draftGroupRepository.findByName(groupName)[0]
        val userCurrentTurnEmail : String = group.members[group.indexOfCurrentUser]
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email

        if (userMakingRequestEmail != userCurrentTurnEmail) {
            throw Exception(NOT_YOUR_TURN)
        }

        if (!group.draftOngoing) {
            throw Exception(THIS_DRAFT_NOT_ONGOING)
        }

        if (!group.availablePlayers.map{it.player.id}.contains(playerId)) {
            throw Exception(PLAYER_NOT_AVAILABLE)
        }

        val player = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(group.season,playerId)[0]
        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(groupName,userCurrentTurnEmail)[0]

        group.availablePlayers.remove(player)
        playerDraft.players.add(player)
        group.numberPlayersDrafted++

        draftGroupRepository.save(group)
        playerDraftRepository.save(playerDraft)

        group.indexOfCurrentUser = if (group.indexOfCurrentUser<group.members.size-1) group.indexOfCurrentUser + 1 else 0
        draftGroupRepository.save(group)

        // since user is making choice before their time expires, we reset the time for the next user whose turn it is
        
        val draftRandomPlayerAtEndOfNextPlayersTurn = scheduledTaskRepository.findByRelatedEntityAndCompleteFalse(group.id.toString())[0]

        schedulerService.removeTaskFromScheduler(draftRandomPlayerAtEndOfNextPlayersTurn.id.toString())
        
        scheduledTaskRepository.delete(draftRandomPlayerAtEndOfNextPlayersTurn)

        // check to make sure that all teams aren't yet full
        if (! group.draftComplete) {
            val timeNextPlayerRunsOutOfTime = GregorianCalendar()
            timeNextPlayerRunsOutOfTime.add(Calendar.SECOND,group.amountOfTimeEachTurn)

            scheduledTaskRepository.save(schedulerService.addNewTask(
                task = Runnable{
                    draftRandomPlayerForCurrentUser(groupName)
                },
                startTime = timeNextPlayerRunsOutOfTime.toInstant(),
                type = TaskType.ScheduleDraft,
                repeatEvery = null,
                relatedEntity = group.id.toString()
            ))
        }

        return DraftGroupInfoDuringDraft(player,group,playerDraft)
    }

    // if user runs out of time to pick automatically select a player (randomly)
    private fun draftRandomPlayerForCurrentUser(groupName: String) {
        val group = draftGroupRepository.findByName(groupName)[0]
        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(groupName,group.members[group.indexOfCurrentUser])[0]

        val player = group.availablePlayers.random()

        group.availablePlayers.remove(player)
        playerDraft.players.add(player)
        group.numberPlayersDrafted++

        draftGroupRepository.save(group)
        playerDraftRepository.save(playerDraft)

        group.indexOfCurrentUser = if (group.indexOfCurrentUser<group.members.size-1) group.indexOfCurrentUser + 1 else 0
        draftGroupRepository.save(group)

        val timeNextPlayerRunsOutOfTime = GregorianCalendar()
        timeNextPlayerRunsOutOfTime.add(Calendar.SECOND,group.amountOfTimeEachTurn)

        if (! group.draftComplete) {
            scheduledTaskRepository.save(schedulerService.addNewTask(
                task = Runnable{
                    draftRandomPlayerForCurrentUser(groupName)
                },
                startTime = timeNextPlayerRunsOutOfTime.toInstant(),
                type = TaskType.ScheduleDraft,
                repeatEvery = null,
                relatedEntity = group.id.toString()
            ))
        }

        simpMessagingTemplate.convertAndSend("/topic/draft/${groupName}", 
            DraftGroupInfoDuringDraft(player,group,playerDraft));
    }

    public fun addPlayerToWatchList(groupName: String, playerId: String, principal: Principal) : PlayerSeason {
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email
        val groups = draftGroupRepository.findByName(groupName)

        if (groups.size == 0) throw Exception(GROUP_DNE)
        val group = groups[0]

        if (!group.availablePlayers.map{it.player.id}.contains(playerId)) {
            throw Exception(PLAYER_NOT_AVAILABLE)
        }

        val relevantPlayerSeason = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(group.season,playerId)[0]
        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(groupName,userMakingRequestEmail)[0]
        playerDraft.watchList.add(relevantPlayerSeason)
        return relevantPlayerSeason
    }

    public fun removePlayerFromWatchList(groupName: String, playerId: String, principal: Principal) : PlayerSeason {
        return playerSeasonRepository.findByIdOr("FILLINLATER")
    }
}
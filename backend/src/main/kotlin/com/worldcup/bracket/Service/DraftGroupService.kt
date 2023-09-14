package com.worldcup.bracket.Service 

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException


import com.worldcup.bracket.GetFootballDataEndpoints

import org.bson.types.ObjectId

import com.worldcup.bracket.DTO.LeagueResponse
import com.worldcup.bracket.DTO.NewDraftGroup
import com.worldcup.bracket.DTO.NewGameWeek
import com.worldcup.bracket.DTO.GameWeekSelection
import com.worldcup.bracket.DTO.DraftGroupInfoDuringDraft
import com.worldcup.bracket.DTO.DraftGroupWithMemberInformation
import com.worldcup.bracket.DTO.UpdatedWatchList
import com.worldcup.bracket.DTO.TradeOffer
import com.worldcup.bracket.Repository.DraftGroupRepository
import com.worldcup.bracket.Repository.UserRepository
import com.worldcup.bracket.Repository.LeagueRepository
import com.worldcup.bracket.Repository.PlayerDraftRepository
import com.worldcup.bracket.Repository.PlayerSeasonRepository
import com.worldcup.bracket.Repository.GameWeekRepository
import com.worldcup.bracket.Repository.ScheduledTaskRepository
import com.worldcup.bracket.Repository.PlayerTradeRepository
import com.worldcup.bracket.Entity.DraftGroup
import com.worldcup.bracket.Entity.GameWeek
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.Entity.PlayerSeason
import com.worldcup.bracket.Entity.PlayerDraft
import com.worldcup.bracket.Entity.GameWeekSummary
import com.worldcup.bracket.Entity.PlayerTrade
import com.worldcup.bracket.Entity.Formation
import com.worldcup.bracket.Entity.Position
import com.worldcup.bracket.Entity.ScheduleType
import com.worldcup.bracket.Entity.TaskType
import com.worldcup.bracket.Entity.TradeState
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
    private val gameWeekRepository: GameWeekRepository,
    private val leagueRepository: LeagueRepository,
    private val playerTradeRepository: PlayerTradeRepository,
    private val footballAPIData: GetFootballDataEndpoints,
    private val leagueService: LeagueService,
    private val schedulerService: SchedulerService,
    private val simpMessagingTemplate: SimpMessagingTemplate
    ) {

    private val httpClient = HttpClient.newHttpClient()
    private val logger : Logger = LoggerFactory.getLogger(javaClass)
    
    companion object {
        public val MAX_MEMBERS_PER_DRAFT_GROUP = 10
        public val TEN_MINUTES_IN_SECONDS = 1 * 60
        // todo change back from 3 minutes to 10 minutes
        public val MAX_PLAYERS_AT_EACH_POSITION = mapOf<Position,Int>(
            Position.Goalkeeper to 2,
            Position.Defender to 5,
            Position.Midfielder to 5,
            Position.Attacker to 5
        )

        public val MIN_PLAYERS_AT_EACH_POSITION = mapOf<Position,Int>(
            Position.Goalkeeper to 1,
            Position.Defender to 4,
            Position.Midfielder to 4,
            Position.Attacker to 3
        )

        public val GROUP_DNE = Pair("Group Does Not Exist",HttpStatus.NOT_FOUND)
        public val INVALID_PW = Pair("Password Does Not Match",HttpStatus.FORBIDDEN)
        public val ALREADY_IN_GROUP = Pair("Member Already In Group",HttpStatus.BAD_REQUEST)
        public val GROUP_FULL = Pair("Group Size Is Already At Maximum",HttpStatus.BAD_REQUEST)
        public val GROUP_ALREADY_EXISTS = Pair("Group Already Exists",HttpStatus.BAD_REQUEST)
        public val NOT_PERMITTED = Pair("You must be the owner of the group to do this action",HttpStatus.UNAUTHORIZED)
        public val TIME_NOT_VALID = Pair("The draft time you set is not valid. It must be at least 10 minutes in the future and before the end of the season.",HttpStatus.BAD_REQUEST)
        public val TOO_LATE_TO_SET_DRAFT_TIME = Pair("The draft is already either ongoing or complete, so it is too late to change the draft time", HttpStatus.BAD_REQUEST)
        public val NOT_ENOUGH_MEMBERS = Pair("You must have at least two members in group to create a draft",HttpStatus.BAD_REQUEST)
        public val NO_USER_TO_REMOVE = Pair("The user you are trying to remove does not exist",HttpStatus.BAD_REQUEST)
        public val WRONG_NUMBER_LEAGUES = Pair("You must have at least one league, and if you have multiple, all must follow the same schedule (start and end same time of year)",HttpStatus.BAD_REQUEST)
        public val TOO_LATE_TO_DRAFT = Pair("One or more of your draft leagues is now too late to draft players",HttpStatus.BAD_REQUEST)
        public val NOT_YOUR_TURN = Pair("It it not your time to draft, please be patient",HttpStatus.BAD_REQUEST)
        public val PLAYER_NOT_AVAILABLE = Pair("The player that you are trying to draft is not currently available",HttpStatus.BAD_REQUEST)
        public val POSITION_NO_LONGER_AVAILABLE = Pair("You have already drafted the maximum number of players at this position. Please choose another", HttpStatus.BAD_REQUEST)
        public val NAME_ALREADY_TAKEN = Pair("This league name is already taken",HttpStatus.BAD_REQUEST)
        public val THIS_DRAFT_NOT_ONGOING = Pair("This draft is either not yet started or already completed",HttpStatus.BAD_REQUEST)
        public val DRAFT_NOT_SCHEDULED = Pair("You must first schedule a draft before viewing the available players and your players",HttpStatus.BAD_REQUEST)
        public val MUST_BE_MEMBER = Pair("You must be a member of the relevant draft group to perform this action",HttpStatus.FORBIDDEN)
        public val PLAYER_ALREADY_IN_WATCHLIST = Pair("The player you are trying to add is already in the watchlist",HttpStatus.BAD_REQUEST)
        public val PLAYER_NOT_IN_WATCHLIST = Pair("The player you are trying to remove is not in the watchlist",HttpStatus.BAD_REQUEST)
        public val NEW_WATCHLIST_ORDERING_MUST_HAVE_SAME_PLAYERS = Pair("The reordered watchlist must contain the same players as the original watchlist. Only the order can change, not the elements themselves.",HttpStatus.BAD_REQUEST)
        public val TRADE_MUST_BE_WITHIN_GROUP = Pair("The player that you are trying to trade with must be in the same group as you",HttpStatus.BAD_REQUEST)
        public val INVALID_PLAYERID_IN_TRADE = Pair("One of the plaeyrs you are trying to trade has an invalid player id",HttpStatus.BAD_REQUEST)
        public val CANNOT_TRADE_WITH_SELF = Pair("You cannot trade with yourself",HttpStatus.BAD_REQUEST)
        public val PLAYER_IN_TRADE_NOT_OWNED = Pair("Not all of the players you are trying to trade are owned by you or the person you are trading with", HttpStatus.FORBIDDEN)
        public val PLAYER_IN_TRADE_NOT_AVAILABLE = Pair("One or more of the players you are trying to obtain is already owned by another player. Please offer that player a trade", HttpStatus.FORBIDDEN)
        public val UNEQUAL_NUMBER_PLAYERS_POSITIONS_FOR_TRADE = Pair("Trades must have equal positional numbers (e.g. one midfielder and one defender for one midfielder and one defender)", HttpStatus.BAD_REQUEST)
        public val INVALID_TRADE_ID = Pair("The trade id that you are referencing is not valid", HttpStatus.BAD_REQUEST)
        public val MUST_BE_USER_RECEIVING_TRADE_OFFER = Pair("In order to accept/decline the trade, you must be the user receiving the offer", HttpStatus.FORBIDDEN)
        public val NEED_TO_ACCEPT_OR_DECLINE_TRADE = Pair("You need to either accept of decline the trade", HttpStatus.BAD_REQUEST)
        public val CONFLICTING_TRADE_OFFER_EXISTS = Pair("""There is another active trade offer that involves players involved in this trade. Please ensure that neither the 
                                                        requesting player nor the offering player is currently involved in another active trade for the same players and try again""",HttpStatus.BAD_REQUEST)
        public val MUST_BE_TRADE_INITIATOR_TO_DELETE = Pair("In order to delete a trade you must be the person who initiated it", HttpStatus.FORBIDDEN)
        public val MUST_BE_ADMIN = Pair("You must be an admin to perform this action", HttpStatus.FORBIDDEN)
        public val GAME_WEEK_DNE = Pair("Game Week does not exist",HttpStatus.NOT_FOUND)
        public val STARTING_PLAYER_YOU_DONT_OWN = Pair("You do not own the player that you are trying to start!",HttpStatus.BAD_REQUEST)
        public val WRONG_NUMBER_PLAYERS_LINEUP = Pair("You are starting an invalid number of players or have an invalid number of subs",HttpStatus.BAD_REQUEST)
        public val FORMATION_DNE = Pair("The formation you have selected is invalid",HttpStatus.BAD_REQUEST)
    }

    public fun getAllDraftGroupsForUser(principal: Principal) : List<DraftGroup> {
        val relevantUser = userRepository.findByPrincipalId(principal.getName())[0]
        return draftGroupRepository.findByIdIn(relevantUser.draftGroups)
    }


    public fun saveNewDraftGroup(body: NewDraftGroup, principal: Principal) {
        val password = passwordEncoder.encode(body.password)
        val allDraftGroupsSameName = draftGroupRepository.findByName(body.name)
        val owner = userRepository.findByPrincipalId(principal.getName())[0]

        val leaguesForDraft = leagueRepository.findByIdIn(body.leagueIds).toMutableList()

        if (draftGroupRepository.findByName(body.name).size>0) {
            throw ResponseStatusException(NAME_ALREADY_TAKEN.second,NAME_ALREADY_TAKEN.first)
        }

        if (leaguesForDraft.map{it.scheduleType}.distinct().size != 1) {
            throw ResponseStatusException(WRONG_NUMBER_LEAGUES.second,WRONG_NUMBER_LEAGUES.first)
        }
        if (allDraftGroupsSameName.size != 0) throw ResponseStatusException(GROUP_ALREADY_EXISTS.second,GROUP_ALREADY_EXISTS.first)

        // figure out what the current season is by sending request to API
        val requestLeague = BuildNewRequest(footballAPIData.getLeagueEndpoint(body.leagueIds[0]),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
        val responseLeague = httpClient.send(requestLeague, HttpResponse.BodyHandlers.ofString());
        val responseWrapperLeague : LeagueResponse = Gson().fromJson(responseLeague.body(), LeagueResponse::class.java)
        
        val latestSeason = leagueService.getLatestSeasonGivenLeagueResponseFromAPI(responseWrapperLeague.response[0].seasons)

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
        println("160")
        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]
        val groups = draftGroupRepository.findByName(body.name)

        println("163")
        if (groups.size == 0) throw ResponseStatusException(GROUP_DNE.second,GROUP_DNE.first)
        val group = groups[0]
        if (! passwordEncoder.matches(body.password,group.password)) throw ResponseStatusException(INVALID_PW.second,INVALID_PW.first)
        if (group.members.contains(currentUser.email)) throw ResponseStatusException (ALREADY_IN_GROUP.second,ALREADY_IN_GROUP.first)
        if (group.members.size == MAX_MEMBERS_PER_DRAFT_GROUP) throw ResponseStatusException(GROUP_FULL.second,GROUP_FULL.first)
        

        group.members.add(currentUser.email)
        draftGroupRepository.save(group)

        currentUser.draftGroups.add(group.id)
        userRepository.save(currentUser)
    }

    public fun getSpecificDraftGroup(name: String, principal: Principal) : DraftGroupWithMemberInformation {
        val groups = draftGroupRepository.findByName(name)
        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]
        if (groups.size==0) throw ResponseStatusException(GROUP_DNE.second,GROUP_DNE.first)
        val group = groups[0]
        if (! group.members.contains(currentUser.email)) {
            throw ResponseStatusException(MUST_BE_MEMBER.second,MUST_BE_MEMBER.first) 
         }
        val playerDrafts = if (group.availableGoalkeepers.size > 0) playerDraftRepository.findAllPlayerDraftsByGroup(group.name) else null
        // availableGoalkeepers > 0 indicates that playerDraft objects have been created already and a draft has been scheduled

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

        if (groups.size==0) throw ResponseStatusException(GROUP_DNE.second,GROUP_DNE.first)
        if (userToRemove == null) throw ResponseStatusException(NO_USER_TO_REMOVE.second,NO_USER_TO_REMOVE.first)
        if (currentUser != groups[0].owner) throw ResponseStatusException(NOT_PERMITTED.second,NOT_PERMITTED.first)
        val group = groups[0]
        group.members.remove(userToRemove.email)

        userToRemove.draftGroups.remove(group.id)
    }

    public fun removeDraftGroup(draftGroupName: String, principal: Principal) {
        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]
        val groups = draftGroupRepository.findByName(draftGroupName)

        if (groups.size==0) throw ResponseStatusException(GROUP_DNE.second,GROUP_DNE.first)
        if (currentUser != groups[0].owner) throw ResponseStatusException(NOT_PERMITTED.second,NOT_PERMITTED.first)
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

        if (groups.size == 0) throw ResponseStatusException(GROUP_DNE.second,GROUP_DNE.first)
        val group = groups[0]
        if (currentUser != group.owner) throw ResponseStatusException(NOT_PERMITTED.second,GROUP_DNE.first)
        if (group.members.size < 2) throw ResponseStatusException(NOT_ENOUGH_MEMBERS.second,NOT_ENOUGH_MEMBERS.first)

        val instant = Instant.now()
        // TODO implmenent functionality making sure it's not too late to draft for a particular season

        if (group.draftTime <= instant.getEpochSecond() && group.draftTime != -1L) {
            throw ResponseStatusException(TOO_LATE_TO_SET_DRAFT_TIME.second,TOO_LATE_TO_SET_DRAFT_TIME.first)
        }

        if (time * 1000 <= GregorianCalendar().getTimeInMillis() + (1000 * TEN_MINUTES_IN_SECONDS)) {
            throw ResponseStatusException(TIME_NOT_VALID.second,TIME_NOT_VALID.first)
        }
        
        if (group.draftTime == -1L) { // indicates that this is first time calling this method/setting the draft time
            // randomly shuffle members, but only on first time, that way someone can't call this method multiple times if they are unhappy with ordering
            val allAvailablePlayers = playerSeasonRepository.findAllPlayerSeasonsByLeaguesAndSeason(group.leagues.map{it.id},group.season)
            group.availableGoalkeepers = allAvailablePlayers.filter{it -> it.position == Position.Goalkeeper}.toMutableList()
            group.availableDefenders = allAvailablePlayers.filter{it -> it.position == Position.Defender}.toMutableList()
            group.availableMidfielders = allAvailablePlayers.filter{it -> it.position == Position.Midfielder}.toMutableList()
            group.availableForwards = allAvailablePlayers.filter{it -> it.position == Position.Attacker}.toMutableList()

            val playerDrafts : MutableList<PlayerDraft> = mutableListOf<PlayerDraft>()

            val emailOfUserInGroupToNameOfUser = userRepository.findByEmailIn(group.members).associateBy({ it.email }, { it.name })
            
            group.members.shuffle()
            group.members.forEach{playerInDraftGroupEmail ->
                playerDrafts.add(PlayerDraft(
                    userEmail = playerInDraftGroupEmail,
                    userName = emailOfUserInGroupToNameOfUser.get(playerInDraftGroupEmail)!!,
                    draftGroup = group
                ))
            }
            playerDraftRepository.saveAll(playerDrafts)
        } else {
            println("not in if statement")
            group.members.shuffle()

            // if there was a preexisting draft time that is changed/updated, ensure that previous task to start draft is cancelled
            scheduledTaskRepository.delete(schedulerService.removeTaskFromScheduler(scheduledTaskRepository.findByRelatedEntity(group.id.toString())[0].id.toString()))
        }

        val whenFirstPlayerMustChooseDeadline = GregorianCalendar()
        whenFirstPlayerMustChooseDeadline.setTimeInMillis(time * 1000)
        whenFirstPlayerMustChooseDeadline.add(Calendar.SECOND,group.amountOfTimeEachTurn)

        scheduledTaskRepository.save(schedulerService.addNewTask(
            task = Runnable{
                autoDraftPlayerForCurrentUser(group.name)
            },
            startTime = whenFirstPlayerMustChooseDeadline.toInstant(),
            type = TaskType.ScheduleDraft,
            repeatEvery = null,
            relatedEntity = group.id.toString()
        ))

        group.draftTime = time
        group.nextDraftDeadline = whenFirstPlayerMustChooseDeadline.toInstant().getEpochSecond()
        draftGroupRepository.save(group)
    }

    public fun getDraftGroupInfoDuringDraft(groupName: String, principal: Principal) : DraftGroupInfoDuringDraft {
        val groups = draftGroupRepository.findByName(groupName)
        val currentUser = userRepository.findByPrincipalId(principal.getName())[0]

        if (groups.size==0) throw ResponseStatusException(GROUP_DNE.second,GROUP_DNE.first)
        val group = groups[0]
        
        if (group.draftTime == -1L) {
            throw ResponseStatusException(DRAFT_NOT_SCHEDULED.second,DRAFT_NOT_SCHEDULED.first)
        }

        if (! group.members.contains(currentUser.email)) {
           throw ResponseStatusException(MUST_BE_MEMBER.second,MUST_BE_MEMBER.first) 
        }


        return DraftGroupInfoDuringDraft(
            draftGroup = group,
            playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(group.name,currentUser.email)[0]
        )
    }

    public fun draftSpecificPlayerForUser(groupName: String, playerId: String, principal: Principal) : DraftGroupInfoDuringDraft {
        println("in draftSpecificPlayerForUser method with playerid: $playerId")
        val group = draftGroupRepository.findByName(groupName)[0]
        val userCurrentTurnEmail : String = group.members[group.indexOfCurrentUser]
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email

        if (userMakingRequestEmail != userCurrentTurnEmail) {
            throw ResponseStatusException(NOT_YOUR_TURN.second,NOT_YOUR_TURN.first)
        }

        if (!group.draftOngoing) {
            throw ResponseStatusException(THIS_DRAFT_NOT_ONGOING.second,THIS_DRAFT_NOT_ONGOING.first)
        }

        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(groupName,userCurrentTurnEmail)[0]
        val allPlayerDraftsWithDraftedPlayerInWatchList = playerDraftRepository.findAllPlayerDraftsWithPlayerOnWatchList(playerId)
        val players = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(group.season,playerId)
        if (players.size == 0) throw ResponseStatusException(PLAYER_NOT_AVAILABLE.second,PLAYER_NOT_AVAILABLE.first)
        val player = players[0]

        val positionOfPlayerSelectedToAllAvailablePlayersOfSamePosition = mapPositionOfPlayerToRemainingPlayersAtSamePosition(group)
        val allAvailablePlayersSamePosition = positionOfPlayerSelectedToAllAvailablePlayersOfSamePosition.get(player.position)!!

        if (!allAvailablePlayersSamePosition.map{it.player.id}.contains(playerId)) {
            throw ResponseStatusException(PLAYER_NOT_AVAILABLE.second,PLAYER_NOT_AVAILABLE.first)
        }

        allAvailablePlayersSamePosition.remove(player)

        val positionToSpaceRemainingOnTeamAtPosition = mapPositionOfPlayerToIfThereIsStillSpaceOnTeamForPosition(playerDraft)

        if (!positionToSpaceRemainingOnTeamAtPosition.get(player.position)!!) 
            throw ResponseStatusException(POSITION_NO_LONGER_AVAILABLE.second,POSITION_NO_LONGER_AVAILABLE.first)

        group.numberPlayersDrafted++
        group.indexOfCurrentUser = if (group.indexOfCurrentUser<group.members.size-1) group.indexOfCurrentUser + 1 else 0

        mapPositionOfPlayerToDraftedPlayersAtSamePosition(playerDraft).get(player.position)!!.add(player)
        updateWatchListsBasedOnDraftedPlayer(player,allPlayerDraftsWithDraftedPlayerInWatchList,playerDraft)
    
        playerDraftRepository.saveAll(allPlayerDraftsWithDraftedPlayerInWatchList)
        playerDraftRepository.save(playerDraft)

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
                    autoDraftPlayerForCurrentUser(groupName)
                },
                startTime = timeNextPlayerRunsOutOfTime.toInstant(),
                type = TaskType.ScheduleDraft,
                repeatEvery = null,
                relatedEntity = group.id.toString()
            ))
            group.nextDraftDeadline = timeNextPlayerRunsOutOfTime.toInstant().getEpochSecond()
        } else {
            group.nextDraftDeadline = null
        }
        draftGroupRepository.save(group)

        return DraftGroupInfoDuringDraft(player,group,playerDraft)
    }

    // if user runs out of time to pick automatically select a player from the top of their watchlist
    // or randomly (must not exceed max for a certain position e.g. 5 for midfielders) if all of their watch list picks are not elgible
    private fun autoDraftPlayerForCurrentUser(groupName: String) {
        val group = draftGroupRepository.findByName(groupName)[0]
        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(groupName,group.members[group.indexOfCurrentUser])[0]
        val allPlayerDraftsInGroup = playerDraftRepository.findAllPlayerDraftsByGroup(groupName)

        val allPositionsStillElgibleToAddPlayers = mutableListOf<Position>()
        val positionOfPlayerSelectedToNumberOfPlayersAtSamePositionAlready = mapPositionOfPlayerToIfThereIsStillSpaceOnTeamForPosition(playerDraft)

        // if we have already drafted max players for a position (5 midfielders for example), don't allow drafting any more
        positionOfPlayerSelectedToNumberOfPlayersAtSamePositionAlready.forEach {entry -> 
            if (entry.value) allPositionsStillElgibleToAddPlayers.add(entry.key)
        }

        val randomElgiblePosition = allPositionsStillElgibleToAddPlayers.random()

        val positionToAllAvailablePlayersOfSamePosition = mapPositionOfPlayerToRemainingPlayersAtSamePosition(group)
        val randomPlayerWithElgiblePosition = positionToAllAvailablePlayersOfSamePosition.get(randomElgiblePosition)!!.random()

        val player = if (playerDraft.watchListUndrafted.size > 0) playerDraft.watchListUndrafted.first() else randomPlayerWithElgiblePosition
        // the watch list sorts elgible players first, so if there are any elgible players still on watch list, get those players first, otherwise choose a random player
    
        println("relevant player to be drafted is: $player")
        positionToAllAvailablePlayersOfSamePosition.get(player.position)!!.remove(player)
        group.numberPlayersDrafted++
        group.indexOfCurrentUser = if (group.indexOfCurrentUser<group.members.size-1) group.indexOfCurrentUser + 1 else 0


        mapPositionOfPlayerToDraftedPlayersAtSamePosition(playerDraft).get(player.position)!!.add(player)
        updateWatchListsBasedOnDraftedPlayer(player,allPlayerDraftsInGroup,playerDraft)
    
        playerDraftRepository.saveAll(allPlayerDraftsInGroup)
        playerDraftRepository.save(playerDraft)


        if (! group.draftComplete) {
            val timeNextPlayerRunsOutOfTime = GregorianCalendar()
            timeNextPlayerRunsOutOfTime.add(Calendar.SECOND,group.amountOfTimeEachTurn)
            scheduledTaskRepository.save(schedulerService.addNewTask(
                task = Runnable{
                    autoDraftPlayerForCurrentUser(groupName)
                },
                startTime = timeNextPlayerRunsOutOfTime.toInstant(),
                type = TaskType.ScheduleDraft,
                repeatEvery = null,
                relatedEntity = group.id.toString()
            ))
            group.nextDraftDeadline = timeNextPlayerRunsOutOfTime.toInstant().getEpochSecond()
        } else {
            group.nextDraftDeadline  = null
        }

        draftGroupRepository.save(group)


        simpMessagingTemplate.convertAndSend("/topic/draft/${groupName}", 
            DraftGroupInfoDuringDraft(player,group,playerDraft));
    }

    public fun addToWatchList(groupName: String, playerId: String, principal: Principal) : PlayerSeason {
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email
        val groups = draftGroupRepository.findByName(groupName)

        if (groups.size == 0) throw ResponseStatusException(GROUP_DNE.second,GROUP_DNE.first)
        val group = groups[0]
        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(groupName,userMakingRequestEmail)[0]

        val playerSeasons = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(group.season,playerId)
        if (playerSeasons.size == 0) throw ResponseStatusException(PLAYER_NOT_AVAILABLE.second,PLAYER_NOT_AVAILABLE.first)
        val relevantPlayerSeason = playerSeasons[0]

        val remainingPlayersAtSamePosition = mapPositionOfPlayerToRemainingPlayersAtSamePosition(group).get(relevantPlayerSeason.position)!!
        println("${remainingPlayersAtSamePosition.size} + is length of remaining player same pos")
        if (!remainingPlayersAtSamePosition.map{it.player.id}.contains(playerId)) {
            println("that's an exception bro")
            throw ResponseStatusException(PLAYER_NOT_AVAILABLE.second,PLAYER_NOT_AVAILABLE.first)
        }
        println("453")
        println("${playerDraft.watchListUndrafted} is the watch list")
        if(playerDraft.watchListUndrafted.contains(relevantPlayerSeason)) {
            println("relevantPlayerSeason: $relevantPlayerSeason")
            throw ResponseStatusException(PLAYER_ALREADY_IN_WATCHLIST.second,PLAYER_ALREADY_IN_WATCHLIST.first)
        }
        println("made it to 455!")
        playerDraft.watchListUndrafted.add(relevantPlayerSeason)
        playerDraftRepository.save(playerDraft)
        return relevantPlayerSeason
    }

    public fun removeFromWatchList(groupName: String, playerId: String, principal: Principal) : PlayerSeason {
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email
        val groups = draftGroupRepository.findByName(groupName)

        if (groups.size == 0) throw ResponseStatusException(GROUP_DNE.second,GROUP_DNE.first)
        val group = groups[0]
        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(groupName,userMakingRequestEmail)[0]

        val playerSeasons = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(group.season,playerId)
        if (playerSeasons.size == 0) throw ResponseStatusException(PLAYER_NOT_AVAILABLE.second,PLAYER_NOT_AVAILABLE.first)
        val relevantPlayerSeason = playerSeasons[0]

        val remainingPlayersAtSamePosition = mapPositionOfPlayerToRemainingPlayersAtSamePosition(group).get(relevantPlayerSeason.position)!!

        if (!remainingPlayersAtSamePosition.map{it.player.id}.contains(playerId)) {
            throw ResponseStatusException(PLAYER_NOT_AVAILABLE.second,PLAYER_NOT_AVAILABLE.first)
        }

        if (playerDraft.watchListUndrafted.contains(relevantPlayerSeason)) throw ResponseStatusException(PLAYER_NOT_IN_WATCHLIST.second,PLAYER_NOT_AVAILABLE.first)
        playerDraft.watchListUndrafted.remove(relevantPlayerSeason)
        playerDraftRepository.save(playerDraft)
        return relevantPlayerSeason
    }

    public fun reorderWatchList(groupName: String, updatedWatchListUserInput: UpdatedWatchList, principal: Principal) {
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email
        val groups = draftGroupRepository.findByName(groupName)

        if (groups.size == 0) throw ResponseStatusException(GROUP_DNE.second,GROUP_DNE.first)

        val playerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(groupName,userMakingRequestEmail)[0]
        val originalSorted = playerDraft.watchListUndrafted.sortedBy{it.player.name}
        val modifiedSorted = updatedWatchListUserInput.updatedWatchList.sortedBy{it.player.name}.toMutableList()
        if (originalSorted != modifiedSorted) {
            throw ResponseStatusException(NEW_WATCHLIST_ORDERING_MUST_HAVE_SAME_PLAYERS.second,NEW_WATCHLIST_ORDERING_MUST_HAVE_SAME_PLAYERS.first)
        }

        playerDraft.watchListUndrafted = updatedWatchListUserInput.updatedWatchList.toMutableList()
        playerDraftRepository.save(playerDraft)
    }


    public fun offerTrade(tradeOffer: TradeOffer, draftGroupName: String, principal: Principal) {
        // first check if trade is with another player, or to obtain a player that is not currently owned by anyone.
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email
        val playerOfferingTradeMatches = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(draftGroupName,userMakingRequestEmail)
        if (playerOfferingTradeMatches.size == 0) throw ResponseStatusException(MUST_BE_MEMBER.second,MUST_BE_MEMBER.first)
        
        val playerOfferingTrade = playerOfferingTradeMatches[0]

        val playersOffered = playerSeasonRepository.findByIdIn(tradeOffer.offeredPlayers)
        val playersRequested = playerSeasonRepository.findByIdIn(tradeOffer.requestedPlayers)
        if (playersOffered.size != tradeOffer.offeredPlayers.size || playersRequested.size != tradeOffer.requestedPlayers.size)
            throw ResponseStatusException(INVALID_PLAYERID_IN_TRADE.second,INVALID_PLAYERID_IN_TRADE.first)

        playersOffered.forEach{
            if (! mapPositionOfPlayerToDraftedPlayersAtSamePosition(playerOfferingTrade).get(it.position)!!.contains(it)) 
                throw ResponseStatusException(PLAYER_IN_TRADE_NOT_OWNED.second,PLAYER_IN_TRADE_NOT_OWNED.first)
        }

        if (tradeOffer.playerDraftReceivingOffer == playerOfferingTrade.id.toString())
            throw ResponseStatusException(CANNOT_TRADE_WITH_SELF.second,CANNOT_TRADE_WITH_SELF.first)
        
        if (! (playersOffered.filter{it.position==Position.Goalkeeper}.size==playersRequested.filter{it.position==Position.Goalkeeper}.size
                && playersOffered.filter{it.position==Position.Defender}.size==playersRequested.filter{it.position==Position.Defender}.size
                && playersOffered.filter{it.position==Position.Midfielder}.size==playersRequested.filter{it.position==Position.Midfielder}.size
                && playersOffered.filter{it.position==Position.Attacker}.size==playersRequested.filter{it.position==Position.Attacker}.size
            )) {
                throw ResponseStatusException(UNEQUAL_NUMBER_PLAYERS_POSITIONS_FOR_TRADE.second,UNEQUAL_NUMBER_PLAYERS_POSITIONS_FOR_TRADE.first)
        }       
        
        if (tradeOffer.playerDraftReceivingOffer != null) {
            val playerReceivingTrade = playerDraftRepository.findByIdOrNull(tradeOffer.playerDraftReceivingOffer)

            if (playerReceivingTrade == null || playerReceivingTrade.draftGroup.name != draftGroupName) throw ResponseStatusException(TRADE_MUST_BE_WITHIN_GROUP.second,TRADE_MUST_BE_WITHIN_GROUP.first)

            // TODO allow trades with unequal positions (e.g. forward for a defender) as long as both players have minimum number at each position
            // // ensure player offering trade will have enough players at each position if trade is completed, and player who is offered trade will have enough players at all positions
            // // if trade is completed

            // val numPlayersAtEachPositionForOfferingPlayerAfterTradeComplete = mutableMapOf<Position,Int>()
            // numPlayersAtEachPositionForOfferingPlayerAfterTradeComplete.put(Position.Goalkeeper,playerOfferingTrade.draftedGoalkeepers.size)
            // numPlayersAtEachPositionForOfferingPlayerAfterTradeComplete.put(Position.Defender,playerOfferingTrade.draftedDefenders.size)
            // numPlayersAtEachPositionForOfferingPlayerAfterTradeComplete.put(Position.Midfielder,playerOfferingTrade.draftedMidfielders.size)
            // numPlayersAtEachPositionForOfferingPlayerAfterTradeComplete.put(Position.Attacker,playerOfferingTrade.draftedForwards.size)

            // val numPlayersAtEachPositionForReceivingPlayerAfterTradeComplete = mutableMapOf<Position,Int>()
            // numPlayersAtEachPositionForReceivingPlayerAfterTradeComplete.put(Position.Goalkeeper,playerReceivingTrade.draftedGoalkeepers.size)
            // numPlayersAtEachPositionForReceivingPlayerAfterTradeComplete.put(Position.Defender,playerReceivingTrade.draftedDefenders.size)
            // numPlayersAtEachPositionForReceivingPlayerAfterTradeComplete.put(Position.Midfielder,playerReceivingTrade.draftedMidfielders.size)
            // numPlayersAtEachPositionForReceivingPlayerAfterTradeComplete.put(Position.Attacker,playerReceivingTrade.draftedForwards.size)
        
            // numPlayersAtEachPositionForOfferingPlayerAfterTradeComplete.forEach { entry -> 
            //     if (MIN_PLAYERS_AT_EACH_POSITION.get(entry.key) < }  

            playersRequested.forEach{
                if (! mapPositionOfPlayerToDraftedPlayersAtSamePosition(playerReceivingTrade).get(it.position)!!.contains(it)) 
                    throw ResponseStatusException(PLAYER_IN_TRADE_NOT_OWNED.second,PLAYER_IN_TRADE_NOT_OWNED.first)
            } 
            // ensure that none of players involved in trade are currently involved in another trade with the same players that would 
            // jeopardize the trade if the other one went through. For example: if player 1 wants to trade messi for ronaldo with player 2 but player 2 already has an active trade offer
            // with ronaldo for mbappe with player 3, player 1 should not be allowed to post the trade. 

            // https://stackoverflow.com/questions/48096204/in-kotlin-how-to-check-contains-one-or-another-value

            playerTradeRepository.findAllActiveTradesInvolvingUser(playerOfferingTrade.id.toString()).forEach{activeTradeOffer ->
                if (activeTradeOffer.playersOffering.map{it.id.toString()}.any{it in tradeOffer.offeredPlayers} || activeTradeOffer.playersRequesting.map{it.id.toString()}.any{it in tradeOffer.offeredPlayers})
                    throw ResponseStatusException(CONFLICTING_TRADE_OFFER_EXISTS.second,CONFLICTING_TRADE_OFFER_EXISTS.first)
            }
            
            playerTradeRepository.findAllActiveTradesInvolvingUser(playerReceivingTrade.id.toString()).forEach{activeTradeOffer ->
                if (activeTradeOffer.playersOffering.map{it.id.toString()}.any{it in tradeOffer.requestedPlayers} || activeTradeOffer.playersRequesting.map{it.id.toString()}.any{it in tradeOffer.requestedPlayers})
                    throw ResponseStatusException(CONFLICTING_TRADE_OFFER_EXISTS.second,CONFLICTING_TRADE_OFFER_EXISTS.first)
            }
            
            playerTradeRepository.save(
                PlayerTrade(
                    offeringPlayer = playerOfferingTrade.id.toString(),
                    receivingPlayer = playerReceivingTrade.id.toString(),
                    playersOffering = playersOffered,
                    playersRequesting = playersRequested
                )
            )
        } else {
            // make sure that all players requested are available (undrafted by any player)
            // TODO only allow trades for currently undrafted players durihng certain periods (e.g. not while games are currently happening)
            // implement deadline
            playersRequested.forEach {
                if (! mapPositionOfPlayerToRemainingPlayersAtSamePosition(playerOfferingTrade.draftGroup).get(it.position)!!.contains(it)) 
                    throw ResponseStatusException(PLAYER_IN_TRADE_NOT_AVAILABLE.second,PLAYER_IN_TRADE_NOT_AVAILABLE.first)
            }

            val positionToUndraftedPlayersMap = mapPositionOfPlayerToRemainingPlayersAtSamePosition(playerOfferingTrade.draftGroup)
            val positionToDraftedPlayersMap = mapPositionOfPlayerToDraftedPlayersAtSamePosition(playerOfferingTrade)
            
            playersOffered.forEach{
                positionToUndraftedPlayersMap.get(it.position)!!.add(it)
                positionToDraftedPlayersMap.get(it.position)!!.remove(it)
            }

            playersRequested.forEach {
                positionToUndraftedPlayersMap.get(it.position)!!.remove(it)
                positionToDraftedPlayersMap.get(it.position)!!.add(it)
            }

            playerDraftRepository.save(playerOfferingTrade)
            draftGroupRepository.save(playerOfferingTrade.draftGroup)

            playerTradeRepository.save(
                PlayerTrade(
                    offeringPlayer = playerOfferingTrade.id.toString(),
                    receivingPlayer = null,
                    playersOffering = playersOffered,
                    playersRequesting = playersRequested
                )
            )
        }
    }

    public fun respondToTradeOffer(response: Boolean, playerTradeId: String, principal: Principal) {
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email

        val playerTrade = playerTradeRepository.findByIdOrNull(playerTradeId)
        if (playerTrade == null || playerTrade.state != TradeState.Offered) throw ResponseStatusException(INVALID_TRADE_ID.second,INVALID_TRADE_ID.first)
        val playerDraftReceivingOffer = playerDraftRepository.findByIdOrNull(playerTrade.receivingPlayer)!!
        // this is a valid id as we already validated this in the offerTrade method
        if (playerDraftReceivingOffer.userEmail != userMakingRequestEmail)
            throw ResponseStatusException(MUST_BE_USER_RECEIVING_TRADE_OFFER.second,MUST_BE_USER_RECEIVING_TRADE_OFFER.first)

        playerTrade.state = if (response) TradeState.Accepted else TradeState.Declined
        if (playerTrade.state == TradeState.Declined) {
            playerTradeRepository.save(playerTrade)
            return
        } else {
            val playerDraftOfferingPlayer = playerDraftRepository.findByIdOrNull(playerTrade.offeringPlayer)!!
            val playerDraftReceivingPlayer = playerDraftRepository.findByIdOrNull(playerTrade.receivingPlayer)!!
            
            val samePositionMapOfferingPlayer = mapPositionOfPlayerToDraftedPlayersAtSamePosition(playerDraftOfferingPlayer)
            val samePositionMapReceivingPlayer = mapPositionOfPlayerToDraftedPlayersAtSamePosition(playerDraftReceivingPlayer)
            
            playerTrade.playersOffering.forEach{
                samePositionMapOfferingPlayer.get(it.position)!!.remove(it)
                samePositionMapReceivingPlayer.get(it.position)!!.add(it)
            }

            playerTrade.playersRequesting.forEach{
                samePositionMapOfferingPlayer.get(it.position)!!.add(it)
                samePositionMapReceivingPlayer.get(it.position)!!.remove(it)
            }
            playerTradeRepository.save(playerTrade)
            playerDraftRepository.saveAll(listOf<PlayerDraft>(playerDraftOfferingPlayer,playerDraftReceivingPlayer))
        }
    }

    public fun deleteTradeOffer(tradeOfferId: String, draftGroupName: String, principal: Principal) {
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email

        val playerTrade = playerTradeRepository.findByIdOrNull(tradeOfferId)
        if (playerTrade == null || playerTrade.state != TradeState.Offered) throw ResponseStatusException(INVALID_TRADE_ID.second,INVALID_TRADE_ID.first)
        val playerDraftMakingOffer = playerDraftRepository.findByIdOrNull(playerTrade.offeringPlayer)!!
        // this is a valid id as we already validated this in the offerTrade method

        if (playerDraftMakingOffer.userEmail != userMakingRequestEmail)
            throw ResponseStatusException(MUST_BE_TRADE_INITIATOR_TO_DELETE.second,MUST_BE_TRADE_INITIATOR_TO_DELETE.first)
        
        playerTradeRepository.delete(playerTrade)
    }

    public fun getAllTradesInvolvingUser(draftGroupName: String, principal: Principal) : List<PlayerTrade> {
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email
        val playerOfferingTradeMatches = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(draftGroupName,userMakingRequestEmail)
        if (playerOfferingTradeMatches.size == 0) throw ResponseStatusException(MUST_BE_MEMBER.second,MUST_BE_MEMBER.first)
        return playerTradeRepository.findAllActiveTradesInvolvingUser(playerOfferingTradeMatches[0].id.toString())
    }

    private fun updateWatchListsBasedOnDraftedPlayer(player: PlayerSeason, allPlayerDraftsWithPlayerOnWatchlist: List<PlayerDraft>, myDraft: PlayerDraft) {
        allPlayerDraftsWithPlayerOnWatchlist.forEach{
            it.watchListUndrafted.remove(player)
        }
        myDraft.watchListUndrafted.remove(player)

        if (!mapPositionOfPlayerToIfThereIsStillSpaceOnTeamForPosition(myDraft).get(player.position)!!) {
            val updatedWatchListWithPlayersAtFullPositionRemoved = mutableListOf<PlayerSeason>()
            myDraft.watchListUndrafted.forEach{if(it.position!=player.position) updatedWatchListWithPlayersAtFullPositionRemoved.add(it)}
            myDraft.watchListUndrafted = updatedWatchListWithPlayersAtFullPositionRemoved
        }
    }

    public fun createGameWeek(gameWeek: NewGameWeek, principal: Principal): GameWeek {
        // Authentication check
        //TODO make sure user is admin!! This is important
        if (principal.name == null) {
            throw ResponseStatusException(MUST_BE_ADMIN.second,MUST_BE_ADMIN.first)
        }

        return gameWeekRepository.save(GameWeek(
            start=gameWeek.start,
            end=gameWeek.end,
            deadline=gameWeek.deadline,
            gameWeekName=gameWeek.gameWeekName,
            league=leagueRepository.findByIdOrNull(gameWeek.league)!!
        ))
    }

    public fun editGameWeek(gameWeek: GameWeek, principal: Principal): GameWeek {
        // Authentication check
        if (principal.name == null) {
            throw ResponseStatusException(MUST_BE_ADMIN.second,MUST_BE_ADMIN.first)
        }

        val existingGameWeek = gameWeekRepository.findTopByGameWeekName(gameWeek.gameWeekName)
            ?:  throw ResponseStatusException(GAME_WEEK_DNE.second,GAME_WEEK_DNE.first)
        
        existingGameWeek.start=gameWeek.start
        existingGameWeek.end=gameWeek.end
        existingGameWeek.deadline=gameWeek.deadline

        return gameWeekRepository.save(existingGameWeek)
    }

    public fun deleteGameWeek(gameWeekName: String, principal: Principal) {
        // Authentication check
        if (principal.name == null) {
            throw ResponseStatusException(MUST_BE_ADMIN.second,MUST_BE_ADMIN.first)
        }

        val existingGameWeek = gameWeekRepository.findTopByGameWeekName(gameWeekName)
            ?: throw ResponseStatusException(GAME_WEEK_DNE.second,GAME_WEEK_DNE.first)

        gameWeekRepository.delete(existingGameWeek)
    }

    public fun setGameWeekLineup(draftGroupName: String, gameWeekSelection: GameWeekSelection, principal: Principal) {
        val userMakingRequestEmail : String = userRepository.findByPrincipalId(principal.getName())[0].email
        val playerOfferingTradeMatches = playerDraftRepository.findPlayerDraftByGroupAndUserEmail(draftGroupName,userMakingRequestEmail)
        if (playerOfferingTradeMatches.size == 0) throw ResponseStatusException(MUST_BE_MEMBER.second,MUST_BE_MEMBER.first)
        val playerOfferingTrade = playerOfferingTradeMatches[0]

        val startingPlayers = playerSeasonRepository.findByIdIn(gameWeekSelection.startingPlayers)
        val subs = playerSeasonRepository.findByIdIn(gameWeekSelection.subPriority)
        if (startingPlayers.size != gameWeekSelection.startingPlayers.size || subs.size != gameWeekSelection.subPriority.size) 
            throw ResponseStatusException(INVALID_PLAYERID_IN_TRADE.second,INVALID_PLAYERID_IN_TRADE.first)

        if (! Formation.values().map { it.name }.contains(gameWeekSelection.formation)) throw ResponseStatusException(FORMATION_DNE.second,FORMATION_DNE.first)
        val allOwnedPlayers = mapPositionOfPlayerToDraftedPlayersAtSamePosition(playerOfferingTrade)
        startingPlayers.forEach{
            if (! allOwnedPlayers.get(it.position)!!.contains(it)) throw ResponseStatusException(STARTING_PLAYER_YOU_DONT_OWN.second,STARTING_PLAYER_YOU_DONT_OWN.first)
        }

        subs.forEach{
            if (! allOwnedPlayers.get(it.position)!!.contains(it)) throw ResponseStatusException(STARTING_PLAYER_YOU_DONT_OWN.second,STARTING_PLAYER_YOU_DONT_OWN.first)
        }

        if (startingPlayers.size != 11) throw ResponseStatusException(WRONG_NUMBER_PLAYERS_LINEUP.second,WRONG_NUMBER_PLAYERS_LINEUP.first)
        if (startingPlayers.size + subs.size != playerOfferingTrade.draftGroup.numberOfPlayersEachTeam) throw ResponseStatusException(WRONG_NUMBER_PLAYERS_LINEUP.second,WRONG_NUMBER_PLAYERS_LINEUP.first)        
        
        val currentGameWeek = gameWeekRepository.findTopByEndGreaterThanEqualOrderByEnd(Instant.now().getEpochSecond())

        val startingLineup = startingPlayers.associate{it.player.name to 0}
        
        val subsLineup = subs.mapIndexed{subPriority: Int, sub: PlayerSeason -> Pair(subPriority,sub.player.name)}
            .associate{it.second to Pair(it.first,0)}
        // each sub will have their sub priority (how soon they should sub in if a starting player doesn't play) and their number of points

        
        playerOfferingTrade.pointsByCurrentStarters = startingLineup
        playerOfferingTrade.pointsByCurrentSubs = subsLineup
        playerOfferingTrade.gameWeekSummary.put(currentGameWeek.gameWeekName,GameWeekSummary(formation=gameWeekSelection.formation))
        playerDraftRepository.save(playerOfferingTrade)
    }

    // for example, if a midfielder is selected this will map to all remaining midfielders in the league
    private fun mapPositionOfPlayerToRemainingPlayersAtSamePosition(group: DraftGroup) : Map<Position,MutableList<PlayerSeason>> {
        return mapOf<Position,MutableList<PlayerSeason>>(
            Position.Goalkeeper to group.availableGoalkeepers,
            Position.Defender to group.availableDefenders,
            Position.Midfielder to group.availableMidfielders,
            Position.Attacker to group.availableForwards
        )
    }

    // for example, if a midfielder is selcted, and you already have 5 midfielders, which is the max number on your team (see MAX_PLAYERS_AT_EACH_POSITION)
    // you can no longer select any more midfielders
    private fun mapPositionOfPlayerToIfThereIsStillSpaceOnTeamForPosition(playerDraft: PlayerDraft) : Map<Position,Boolean> {
        return mapOf<Position,Boolean>(
            Position.Goalkeeper to (playerDraft.draftedGoalkeepers.size < MAX_PLAYERS_AT_EACH_POSITION.get(Position.Goalkeeper)!!),
            Position.Defender to (playerDraft.draftedDefenders.size < MAX_PLAYERS_AT_EACH_POSITION.get(Position.Defender)!!),
            Position.Midfielder to (playerDraft.draftedMidfielders.size < MAX_PLAYERS_AT_EACH_POSITION.get(Position.Midfielder)!!),
            Position.Attacker to (playerDraft.draftedForwards.size < MAX_PLAYERS_AT_EACH_POSITION.get(Position.Attacker)!!)
        )
    }

    private fun mapPositionOfPlayerToDraftedPlayersAtSamePosition(playerDraft: PlayerDraft) : Map<Position,MutableList<PlayerSeason>>{
        return mapOf<Position,MutableList<PlayerSeason>>(
            Position.Goalkeeper to playerDraft.draftedGoalkeepers,
            Position.Defender to playerDraft.draftedDefenders,
            Position.Midfielder to playerDraft.draftedMidfielders,
            Position.Attacker to playerDraft.draftedForwards
        )
    }
}
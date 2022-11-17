package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service

import com.worldcup.bracket.DTO.NewDraftGroup
import com.worldcup.bracket.DTO.SetDraftTime
import com.worldcup.bracket.Repository.DraftGroupRepository
import com.worldcup.bracket.Repository.UserRepository
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Repository.PlayerDraftRepository
import com.worldcup.bracket.Entity.DraftGroup
import com.worldcup.bracket.Entity.PlayerDraft
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull


import java.security.Principal
import java.time.Instant

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service
class DraftGroupService(private val draftGroupRepository: DraftGroupRepository, 
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val playerRepository: PlayerRepository,
    private val playerDraftRepository: PlayerDraftRepository) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)
    
    companion object {
        private val MAX_MEMBERS_PER_DRAFT_GROUP = 10
        public val GROUP_DNE = "Group Does Not Exist"
        public val INVALID_PW = "Password Does Not Match"
        public val ALREADY_IN_GROUP = "Member Already In Group"
        public val GROUP_FULL = "Group Size Is Already At Maximum"
        public val GROUP_ALREADY_EXISTS = "Group Already Exists"
        public val NOT_PERMITTED = "You must be the owner of the group to do this action"
        public val WORLD_CUP_START_KNOCKOUT = 1670079600
        public val TIME_NOT_VALID = "The draft time you set is not valid. It must be 10 minutes in the future and before the start of the knockout round"
        public val TEN_MINUTES_IN_SECONDS = 10 * 60
        public val NOT_ENOUGH_MEMBERS = "You must have at least two members in group to create a draft"
        public val NO_USER_TO_REMOVE = "The user you are trying to remove does not exist"
    }

    public fun saveNewDraftGroup(body: NewDraftGroup, principal: Principal) {
        val password = passwordEncoder.encode(body.password)
        val allDraftGroupsSameName = draftGroupRepository.findByName(body.name)
        val owner = userRepository.findByName(principal.getName())[0]
        if (allDraftGroupsSameName.size != 0) throw Exception(GROUP_ALREADY_EXISTS)
        draftGroupRepository.save(
            DraftGroup(
                name=body.name,
                password=password,
                owner=owner,
                members=mutableListOf(owner)
            )
        )
    }

    public fun joinDraftGroup(body: NewDraftGroup, principal: Principal) {
        val currentUser = userRepository.findByName(principal.getName())[0]
        val group = draftGroupRepository.findByName(body.name)

        if (group.size == 0) throw Exception(GROUP_DNE)
        if (! passwordEncoder.matches(body.password,group[0].password)) throw Exception(INVALID_PW)
        if (group[0].members.filter{user -> user == currentUser}.size > 0) throw Exception (ALREADY_IN_GROUP)
        if (group[0].members.size == MAX_MEMBERS_PER_DRAFT_GROUP) throw Exception(GROUP_FULL)
        

        group[0].members.add(currentUser)
        draftGroupRepository.save(group[0])
    }

    public fun getSpecificDraftGroup(id: String) : DraftGroup? {
        return draftGroupRepository.findByIdOrNull(id)
    }

    public fun removeUserFromDraftGroup(draftGroupId: String, userId: String, principal: Principal) {

        val currentUser = userRepository.findByName(principal.getName())[0]
        val group = draftGroupRepository.findByIdOrNull(draftGroupId)
        val userToRemove = userRepository.findByIdOrNull(userId)

        if (group == null) throw Exception(GROUP_DNE)
        if (userToRemove == null) throw Exception(NO_USER_TO_REMOVE)
        if (currentUser != group.owner) throw Exception(NOT_PERMITTED)
        group.members = group.members.filter{
            user -> user != userToRemove
        }.toMutableList()
        draftGroupRepository.save(group)
    }

    public fun removeDraftGroup(draftGroupId: String, principal: Principal) {
        val currentUser = userRepository.findByName(principal.getName())[0]
        val group = draftGroupRepository.findByIdOrNull(draftGroupId)

        if (group == null) throw Exception(GROUP_DNE)
        if (currentUser != group.owner) throw Exception(NOT_PERMITTED)
        draftGroupRepository.delete(group)
        playerDraftRepository.deleteAll(playerDraftRepository.findAllPlayerDraftsByGroup(draftGroupId))
    }

    public fun scheduleDraftGroup(draftGroupId: String, body: SetDraftTime) {
        val group = draftGroupRepository.findByIdOrNull(draftGroupId)
        if (group == null) throw Exception (GROUP_DNE)
        val instant = Instant.now()
        instant.plusSeconds(10000)
        if (body.time > WORLD_CUP_START_KNOCKOUT || body.time < instant.getEpochSecond() + TEN_MINUTES_IN_SECONDS) throw Exception(TIME_NOT_VALID)
        if (group.members.size < 2) throw Exception(NOT_ENOUGH_MEMBERS)

        group.draftTime = body.time
        group.availablePlayers.addAll(playerRepository.findAll().toMutableList())
        val playerDrafts : MutableList<PlayerDraft> = mutableListOf<PlayerDraft>()
        group.members.forEach{playerInDraftGroup ->
            playerDrafts.add(PlayerDraft(
                user = playerInDraftGroup,
                draftGroup = group
            ))
        }
        playerDraftRepository.saveAll(playerDrafts)
        draftGroupRepository.save(group)
    }

    public fun draftPlayerForUser(playerDraftId: String, playerId: String) {
        val playerDraft = playerDraftRepository.findByIdOrNull(playerDraftId)!!
        val groupId = playerDraft.draftGroup.id.toString()
        val group = draftGroupRepository.findByIdOrNull(groupId)!!
        val player = playerRepository.findByIdOrNull(playerId)!!
        group.availablePlayers.filter{p -> p.id != playerId}
        playerDraft.players.add(player)
        draftGroupRepository.save(group)
        playerDraftRepository.save(playerDraft)
    }
}
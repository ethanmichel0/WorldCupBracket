package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody

import com.worldcup.bracket.repository.GroupRepository
import com.worldcup.bracket.Entity.Group
import com.worldcup.bracket.Service.GroupService
import com.worldcup.bracket.DTO.OverrideGroupSettings

@RestController
class GroupController(private val groupRepository: GroupRepository, private val groupService : GroupService) {
    @GetMapping("/api/groups")
    fun getAllGroups() : List<Group> {
        return groupRepository.findAll()
    }
    @PostMapping("/api/groups/tieBreaker")
    fun overrideTieBreak(@RequestBody overrideGroupSettings : OverrideGroupSettings) : Group {
        return groupService.overrideGroupOrdering(overrideGroupSettings)
    }
}
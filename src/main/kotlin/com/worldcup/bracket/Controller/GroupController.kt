package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

import com.worldcup.bracket.repository.GroupRepository
import com.worldcup.bracket.Entity.Group


@RestController
class GroupController(private val groupRepository: GroupRepository) {
    @GetMapping("/api/groups")
    fun getAllGroups() : List<Group> {
        return groupRepository.findAll()
    }
}
package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

import java.security.Principal

import com.worldcup.bracket.DTO.NewDraftGroup
import com.worldcup.bracket.Service.DraftGroupService


@RestController
class DraftGroupController(private val draftGroupService : DraftGroupService) {
    @GetMapping("/api/draftgroups/{draftGroupId}")
    fun getSpecificDraftGroup(@PathVariable draftGroupId : String) : String {
        return "Testing if redirection woroking"
    }

    @PostMapping("/api/draftgroups")
    fun createNewDraftGroup(@RequestBody body : NewDraftGroup, principal: Principal) {
        draftGroupService.saveNewDraftGroup(body,principal)
    }

    @PutMapping("/api/draftgroups/{draftGroupId}/draftTime")
    fun setDraftTime(@PathVariable draftGroupId : String) {}

    @PutMapping("/api/draftgroups/{draftGroupId}/join")
    fun joinDraftGroup(@PathVariable draftGroupId : String) {}

    @DeleteMapping("/api/draftgroups/{draftGroupId}")  
    fun deleteDraftGroup(@PathVariable draftGroupId : String) {}

    @DeleteMapping("/api/draftgroups/{draftGroupId}/{userId}")
    fun removeUserFromDraftGroup(@PathVariable draftGroupId : String, @PathVariable userId : String){}

}
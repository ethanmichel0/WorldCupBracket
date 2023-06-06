package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import java.security.Principal

import com.worldcup.bracket.DTO.DraftGroupWithMemberInformation
import com.worldcup.bracket.DTO.NewDraftGroup
import com.worldcup.bracket.DTO.SetDraftTime
import com.worldcup.bracket.DTO.DraftGroupInfoDuringDraft
import com.worldcup.bracket.Service.DraftGroupService
import com.worldcup.bracket.Entity.DraftGroup
import com.worldcup.bracket.Entity.PlayerSeason

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Payload
import  org.springframework.messaging.handler.annotation.DestinationVariable

import org.slf4j.Logger
import org.slf4j.LoggerFactory



@RestController
class DraftGroupController(private val draftGroupService : DraftGroupService) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/api/draftgroups")
    fun getAllDraftGroupsForUser(principal: Principal) : ResponseEntity<List<DraftGroup>> {
        println("trying get all draft groups for user!")
        try {
            val result = draftGroupService.getAllDraftGroupsForUser(principal)
            println("resulut is $result")
            return ResponseEntity.status(HttpStatus.OK).body(result)
        } catch (e: Exception) {
            println("${e}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping("/api/draftgroups/{groupName}/remainingplayers")
    fun getDraftGroupInfoDuringDraft(@PathVariable groupName: String, principal: Principal) : ResponseEntity<DraftGroupInfoDuringDraft> {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(draftGroupService.getDraftGroupInfoDuringDraft(groupName,principal))
        } catch (e: Exception) {
            if (e.message == DraftGroupService.DRAFT_NOT_SCHEDULED)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
            if (e.message == DraftGroupService.GROUP_DNE)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            if (e.message == DraftGroupService.MUST_BE_MEMBER)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
            println(e.message + "IS ERROR MESSAGE")
            throw(e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping("/api/draftgroups/{draftGroupName}")
    fun getSpecificDraftGroup(@PathVariable draftGroupName : String, principal: Principal) : ResponseEntity<DraftGroupWithMemberInformation> {
        println("IN GET SPECIFIC DRAFAT GROUP!!")
        try {
            val group = draftGroupService.getSpecificDraftGroup(draftGroupName,principal)
            return ResponseEntity.status(HttpStatus.OK).body(group)
        } catch (e: Exception) {
            if (e.message == DraftGroupService.GROUP_DNE) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            println("error is: ${e}")
            println("stacktrace is: ${e.getStackTrace()}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PostMapping("/api/draftgroups")
    fun createNewDraftGroup(@RequestBody body : NewDraftGroup, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.saveNewDraftGroup(body,principal)
            return ResponseEntity.status(HttpStatus.OK).body("")
        } catch (e: Exception) {
            if (e.message == DraftGroupService.GROUP_ALREADY_EXISTS) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
            }
            println("${e.stackTraceToString()} + IS MESSAGE")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @PutMapping("/api/draftgroups/{draftGroupName}/draftTime")
    fun setDraftTime(@PathVariable draftGroupName : String, principal: Principal, @RequestParam time: Long) : ResponseEntity<String> {
        try {
            draftGroupService.scheduleDraftGroup(draftGroupName,time,principal)
            return ResponseEntity.status(HttpStatus.OK).body("")
        } catch (e: Exception) {
            if (e.message == DraftGroupService.GROUP_DNE)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
            if (e.message == DraftGroupService.NOT_PERMITTED)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
            if (e.message == DraftGroupService.NOT_ENOUGH_MEMBERS)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
            if (e.message == DraftGroupService.TIME_NOT_VALID)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @PostMapping("/api/draftgroups/join")
    fun joinDraftGroup(@RequestBody requestBody : NewDraftGroup, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.joinDraftGroup(requestBody,principal)
            return ResponseEntity.status(HttpStatus.OK).body("")
        } catch (e: Exception) {
            if (e.message == DraftGroupService.GROUP_DNE)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
            if (e.message == DraftGroupService.INVALID_PW)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
            if (e.message == DraftGroupService.ALREADY_IN_GROUP)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
            if (e.message == DraftGroupService.GROUP_FULL)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
            
            // TODO Fix these status codes for last two exceptions

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @DeleteMapping("/api/draftgroups/{draftGroupName}/remove/{userId}")
    fun removeUserFromDraftGroup(@PathVariable draftGroupName: String, @PathVariable userId: String, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.removeUserFromDraftGroup(draftGroupName,userId,principal)
            return ResponseEntity.status(HttpStatus.OK).body("")
        } catch (e: Exception) {
            if (e.message == DraftGroupService.NOT_PERMITTED) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @DeleteMapping("/api/draftgroups/{draftGroupName}")  
    fun deleteDraftGroup(@PathVariable draftGroupName : String, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.removeDraftGroup(draftGroupName, principal)
            return ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            if (e.message == DraftGroupService.NOT_PERMITTED) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }
/* 
    @PutMapping("/api/draftgroups/{draftGroupName}/addToWatchList/{playerId}")
    fun addPlayerToWatchList(@PathVariable draftGroupName: String, @PathVariable playerId: String, principal: Principal) : ResponseEntity<PlayerSeason> {
        try {
            draftGroupService.addPlayerToWatchList(draftGroupName, playerId, principal)
        } catch (e : Exception) {
            // TODO
        }
    }

    @DeleteMapping("/api/draftgroups/{draftGroupName}/addToWatchList/{playerId}")
    fun removePlayerFromWatchList(@PathVariable draftGroupName : String, @PathVariable playerId: String, principal: Principal) : ResponseEntity<PlayerSeason> {
        try {
            draftGroupService.removePlayerFromWatchList(draftGroupName, playerId, principal)
        } catch (e: Exception) {
            // TODO
        }
    } */

    @MessageMapping("/api/draftgroups/{draftGroupName}/draftplayer/{playerId}")
    @SendTo("/topic/draft/{draftGroupName}")
    fun draftPlayerForUser(@DestinationVariable draftGroupName: String, @DestinationVariable playerId: String, principal: Principal) : DraftGroupInfoDuringDraft {
        println("in draftplayerfor user method")
        try {
            return draftGroupService.draftPlayerForUser(draftGroupName,playerId,principal)
        } catch (e: Exception) {
            throw e;
        }
    } 
}
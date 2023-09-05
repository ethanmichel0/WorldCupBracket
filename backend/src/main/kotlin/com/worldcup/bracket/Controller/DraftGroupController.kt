package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.web.server.ResponseStatusException
import org.springframework.security.access.prepost.PreAuthorize


import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import java.security.Principal

import com.worldcup.bracket.DTO.DraftGroupWithMemberInformation
import com.worldcup.bracket.DTO.NewDraftGroup
import com.worldcup.bracket.DTO.NewGameWeek
import com.worldcup.bracket.DTO.SetDraftTime
import com.worldcup.bracket.DTO.DraftGroupInfoDuringDraft
import com.worldcup.bracket.DTO.TradeOffer
import com.worldcup.bracket.DTO.UpdatedWatchList
import com.worldcup.bracket.Service.DraftGroupService
import com.worldcup.bracket.Entity.DraftGroup
import com.worldcup.bracket.Entity.GameWeek
import com.worldcup.bracket.Entity.PlayerSeason
import com.worldcup.bracket.Entity.PlayerTrade

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
        try {
            val result = draftGroupService.getAllDraftGroupsForUser(principal)
            return ResponseEntity.status(HttpStatus.OK).body(result)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping("/api/draftgroups/{groupName}/remainingplayers")
    fun getDraftGroupInfoDuringDraft(@PathVariable groupName: String, principal: Principal) : ResponseEntity<DraftGroupInfoDuringDraft> {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(draftGroupService.getDraftGroupInfoDuringDraft(groupName,principal))
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(DraftGroupInfoDuringDraft(error=e.getReason()))
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping("/api/draftgroups/{draftGroupName}")
    fun getSpecificDraftGroup(@PathVariable draftGroupName : String, principal: Principal) : ResponseEntity<DraftGroupWithMemberInformation> {
        try {
            val group = draftGroupService.getSpecificDraftGroup(draftGroupName,principal)
            return ResponseEntity.status(HttpStatus.OK).body(group)
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(DraftGroupWithMemberInformation(error=e.getReason()))
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PostMapping("/api/draftgroups")
    fun createNewDraftGroup(@RequestBody body : NewDraftGroup, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.saveNewDraftGroup(body,principal)
            return ResponseEntity.status(HttpStatus.OK).body("")
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PutMapping("/api/draftgroups/{draftGroupName}/draftTime")
    fun setDraftTime(@PathVariable draftGroupName : String, principal: Principal, @RequestParam time: Long) : ResponseEntity<String> {
        try {
            draftGroupService.scheduleDraftGroup(draftGroupName,time,principal)
            return ResponseEntity.status(HttpStatus.OK).body("")
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PostMapping("/api/draftgroups/join")
    fun joinDraftGroup(@RequestBody requestBody : NewDraftGroup, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.joinDraftGroup(requestBody,principal)
            return ResponseEntity.status(HttpStatus.OK).body("")
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @DeleteMapping("/api/draftgroups/{draftGroupName}/remove/{userId}")
    fun removeUserFromDraftGroup(@PathVariable draftGroupName: String, @PathVariable userId: String, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.removeUserFromDraftGroup(draftGroupName,userId,principal)
            return ResponseEntity.status(HttpStatus.OK).body("")
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @DeleteMapping("/api/draftgroups/{draftGroupName}")  
    fun deleteDraftGroup(@PathVariable draftGroupName : String, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.removeDraftGroup(draftGroupName, principal)
            return ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
    
    @PostMapping("/api/draftgroups/{draftGroupName}/addToWatchList/{playerId}")
    fun addToWatchList(@PathVariable draftGroupName: String, @PathVariable playerId: String, principal: Principal) : ResponseEntity<String> {
        try {
            println("in addToWatchList in controller")
            draftGroupService.addToWatchList(draftGroupName, playerId, principal)
            return ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @DeleteMapping("/api/draftgroups/{draftGroupName}/removeFromWatchList/{playerId}")
    fun removeFromWatchList(@PathVariable draftGroupName: String, @PathVariable playerId: String, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.removeFromWatchList(draftGroupName, playerId, principal)
            return ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PutMapping("/api/draftgroups/{draftGroupName}/reorderWatchList")
    fun reorderWatchList(@PathVariable draftGroupName: String, @RequestBody updatedWatchList: UpdatedWatchList, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.reorderWatchList(draftGroupName, updatedWatchList, principal)
            return ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @MessageMapping("/api/draftgroups/{draftGroupName}/draftplayer/{playerId}")
    @SendTo("/topic/draft/{draftGroupName}")
    fun draftSpecificPlayerForUser(@DestinationVariable draftGroupName: String, @DestinationVariable playerId: String, principal: Principal) : DraftGroupInfoDuringDraft {
        println("in draftplayerfor user method and playerId is: ${playerId}")
        try {
            return draftGroupService.draftSpecificPlayerForUser(draftGroupName,playerId,principal)
        } catch (e: Exception) {
            throw e;
        }
    }

    @MessageExceptionHandler
    @SendTo("/topic/draft/{draftGroupName}/errors")
    fun handleException(e: Exception) : String? {
        println("in handle exception with e ${e}")
        if (e is ResponseStatusException) return e.getReason()
        throw e
    }

    @PostMapping("/api/draftgroups/{draftGroupName}/offerTrade")
    fun offerTrade(@RequestBody tradeOffer: TradeOffer, @PathVariable draftGroupName: String, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.offerTrade(tradeOffer, draftGroupName, principal)
            return ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PostMapping("/api/draftgroups/{draftGroupName}/respondToTradeOffer/{tradeOfferId}/{response}")
    fun respondToTradeOffer(@PathVariable response: Boolean, @PathVariable draftGroupName: String, @PathVariable tradeOfferId: String, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.respondToTradeOffer(response, tradeOfferId, principal)
            return ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @DeleteMapping("/api/draftgroups/{draftGroupName}/{tradeOfferId}")
    fun deleteTradeOffer(@PathVariable tradeOfferId: String, @PathVariable draftGroupName: String, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.deleteTradeOffer(tradeOfferId, draftGroupName, principal)
            return ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN)")
    @PostMapping("/api/gameweek")
    fun createGameWeek(@RequestBody gameWeek: NewGameWeek, principal: Principal) : ResponseEntity<GameWeek> {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(draftGroupService.createGameWeek(gameWeek, principal))
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(null)
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN)")
    @PutMapping("/api/gameweek")
    fun editGameWeek(@RequestParam gameWeek: GameWeek, principal: Principal) : ResponseEntity<GameWeek> {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(draftGroupService.editGameWeek(gameWeek, principal))
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(null)
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @DeleteMapping("/api/gameweek/{gameWeekName}")
    fun deleteGameWeek(@RequestParam gameWeekName: String, principal: Principal) : ResponseEntity<String> {
        try {
            draftGroupService.deleteGameWeek(gameWeekName, principal)
            return ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(e.getReason())
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
    

    @GetMapping("/api/draftgroups/{draftGroupName}/trades")
    fun getAllTradesInvolvingUser(@PathVariable draftGroupName: String, principal: Principal) : ResponseEntity<List<PlayerTrade>> {
        println("246!!!")
        try {
            val allTrades = draftGroupService.getAllTradesInvolvingUser(draftGroupName,principal)
            println("249")
            return ResponseEntity.status(HttpStatus.OK).body(allTrades)
        } catch (e: Exception) {
            println("in exception: ${e}")
            if (e is ResponseStatusException) return ResponseEntity.status(e.getStatus()).body(null)
            // since exception is unaccounted for log the message:
            logger.error("$e")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}
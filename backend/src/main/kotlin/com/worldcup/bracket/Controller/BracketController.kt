package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class BracketController() {
    @GetMapping("/api/brackets")
    fun getAllGamesSortByDateAsc() : String {
        return "test restricted"
    }
}
package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class BracketController() {
    @GetMapping("/api/brackets")
    fun getAllGamesSortByDateAsc() : String {
        print("df")
        return "test restrictedz"
    }
    @GetMapping("/api/testAuth")
    fun test() : String {
        println("SDf")
        return "Testing if redirection woroking"
    }
}
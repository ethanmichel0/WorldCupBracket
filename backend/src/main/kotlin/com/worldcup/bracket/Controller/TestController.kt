package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize

import com.worldcup.bracket.DTO.UserInfo

@RestController
class TestController() {

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/api/user")
    fun getUser() : String {
        return "hello user"
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin")
    fun getAdmin() : String {
        return "hello admin"
    }
}
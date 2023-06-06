package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import com.worldcup.bracket.DTO.UserInfo

@RestController
class UserController() {

    @GetMapping("/api/userinfo")
    fun getInfoForUser() : ResponseEntity<UserInfo> {
        val auth = SecurityContextHolder.getContext().getAuthentication()
        if (auth is AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
        }
        val principal : DefaultOidcUser = auth.getPrincipal() as DefaultOidcUser
        return ResponseEntity.status(HttpStatus.OK).body(UserInfo(
            email=principal.getEmail(),
            name=principal.getFullName()
        ))
    }
}
package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.DocumentReference
import org.springframework.data.annotation.ReadOnlyProperty

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.GrantedAuthority


import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId

@Document(collection="users")
class User (
    val principalId: String,
    val name: String,
    val email: String, 
    val service: AuthService, 
    @JsonIgnore
    @get:JvmName("getPasswordJVM")
    var password: String? = null,
    val createdDate: Long = System.currentTimeMillis() / 1000,
    val draftGroups: MutableList<ObjectId> = mutableListOf<ObjectId>(),
    @Id 
    val id : ObjectId = ObjectId.get(),
    var accountNonExpired: Boolean = true,
    var accountNonLocked: Boolean = true,
    var credentialsNonExpired: Boolean = true,
    var enabled: Boolean = true,
    var roles: String = "ROLE_USER"
    ) : UserDetails {
        override fun equals(other: Any?): Boolean =
            other is User && other.name == name && other.email == email
        var admin : Boolean = false
        override fun getAuthorities() : List<GrantedAuthority> {
            val authorities = mutableListOf<GrantedAuthority>()
            for (role in roles.split(",")) {
                authorities.add(SimpleGrantedAuthority(role))
            }
            return authorities
        }
        override fun getUsername() : String {
            return this.name
        }
        override fun getPassword() : String {
            return this.password ?: "no password due to oauth"
        }
        override fun isAccountNonExpired() : Boolean {
            return this.accountNonExpired
        }
        override fun isAccountNonLocked() : Boolean {
            return this.accountNonLocked
        }
        override fun isCredentialsNonExpired() : Boolean {
            return this.credentialsNonExpired
        }
        override fun isEnabled() : Boolean  {
            return this.enabled
        }
    }

enum class AuthService {
    GOOGLE
}
package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service

import com.worldcup.bracket.DTO.NewDraftGroup
import com.worldcup.bracket.Repository.DraftGroupRepository
import com.worldcup.bracket.Repository.UserRepository
import com.worldcup.bracket.Entity.DraftGroup
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

import java.security.Principal


@Service
class DraftGroupService(private val draftGroupRepository: DraftGroupRepository, 
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository) {
    public fun saveNewDraftGroup(body: NewDraftGroup, principal: Principal) {
        val password =passwordEncoder.encode(body.password)
        draftGroupRepository.save(
            DraftGroup(
                name=body.name,
                password=password,
                owner=userRepository.findByName(principal.getName())[0]
            )
        )
    }
}
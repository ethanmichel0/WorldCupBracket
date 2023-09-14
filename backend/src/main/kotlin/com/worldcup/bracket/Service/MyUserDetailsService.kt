package com.worldcup.bracket.Service

import com.worldcup.bracket.Entity.User
import com.worldcup.bracket.Repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService (private val userRepository: UserRepository) : UserDetailsService {

    public override fun loadUserByUsername(s: String) : UserDetails {
        val users : List<User> = userRepository.findByName(s);

        if (users.size == 0)
            throw UsernameNotFoundException(String.format("Username[%s] not found"));
        
        return users[0]
    }
}
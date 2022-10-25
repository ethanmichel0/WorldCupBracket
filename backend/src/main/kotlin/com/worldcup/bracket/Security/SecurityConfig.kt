package com.worldcup.bracket.Security

import com.worldcup.bracket.Service.AuthenticationSuccessHandler
import kotlin.Throws
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import java.lang.Exception
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ComponentScan

import org.springframework.security.config.Customizer.withDefaults


@EnableWebSecurity
@Configuration
@ComponentScan("com.worldcup.bracket.*")
public class SecurityConfig(private val authenticationSuccessHandler : AuthenticationSuccessHandler) {
    @Throws(Exception::class)
    @Bean
    public fun override(http: HttpSecurity): SecurityFilterChain {
        return http
                .csrf{csrf -> csrf.disable()}
                .authorizeRequests{auth -> 
                    auth.antMatchers("/api/brackets").authenticated()
                    auth.antMatchers("/**").permitAll()
                }
                .oauth2Login()
                .successHandler(authenticationSuccessHandler)
                .and()
                .build()
    }
}
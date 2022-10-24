package com.worldcup.bracket.Security

import kotlin.Throws
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import java.lang.Exception
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.context.annotation.Configuration

import org.springframework.security.config.Customizer.withDefaults


@EnableWebSecurity
@Configuration
public class SecurityConfig() {
    @Throws(Exception::class)
    @Bean
    public fun override(http: HttpSecurity): SecurityFilterChain {
        return http
                .csrf{csrf -> csrf.disable()}
                .authorizeRequests{auth -> 
                    auth.antMatchers("/api/bracket").authenticated()
                    auth.antMatchers("/**").permitAll()
                }
                .httpBasic()
                .and()
                .build()
    }
}
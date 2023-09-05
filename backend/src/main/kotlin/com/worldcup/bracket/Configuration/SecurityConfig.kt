package com.worldcup.bracket.Configuration

import kotlin.Throws
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import java.lang.Exception
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

import com.worldcup.bracket.Service.UserDetailsService

import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.web.context.SecurityContextHolderFilter

@EnableWebSecurity
@Configuration
public class SecurityConfig(
    private val authenticationSuccessHandler: AuthenticationSuccessHandler,
    private val userDetailsService: UserDetailsService) {
    @Throws(Exception::class)
    @Bean
    public fun override(http: HttpSecurity): SecurityFilterChain {
        return http
                .csrf{csrf -> csrf.disable()}
                .authorizeRequests{auth -> 
                    println("IN AUTHORIZING REQUEST!!")
                    auth.antMatchers("/api/games/**").authenticated()
                    auth.antMatchers("/app/**").authenticated()
                    // auth.antMatchers("/ws").authenticated()
                    auth.antMatchers("/ws/**").authenticated()
                    .anyRequest().authenticated()
                }
                .userDetailsService(userDetailsService)
                .oauth2Login()
                .successHandler(authenticationSuccessHandler)
                //\.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // csrf token is stored in browser as cookie XSRF-TOKEN, it is send along with POST requests in request header X-XSRF-TOKEN
                // https://itnext.io/how-to-prevent-cross-site-request-forgery-of-legitime-cross-site-request-5b59a6a56808
                .and()
                .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }
}
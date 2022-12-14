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

import org.springframework.security.config.Customizer.withDefaults


@EnableWebSecurity
@Configuration
public class SecurityConfig(private val authenticationSuccessHandler : AuthenticationSuccessHandler) {
    @Throws(Exception::class)
    @Bean
    public fun override(http: HttpSecurity): SecurityFilterChain {
        return http
                .csrf{csrf -> csrf.disable()}
                .authorizeRequests{auth -> 
                    auth.antMatchers("/api/draftgroups").authenticated()
                    auth.antMatchers("/**").permitAll()
                }
                .oauth2Login()
                .successHandler(authenticationSuccessHandler)
                //.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
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
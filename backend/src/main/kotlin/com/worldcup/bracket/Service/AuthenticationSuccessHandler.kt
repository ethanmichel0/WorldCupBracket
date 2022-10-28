package com.worldcup.bracket.Service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.core.authority.SimpleGrantedAuthority

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection
import kotlin.Throws

import com.worldcup.bracket.Entity.User
import com.worldcup.bracket.Entity.AuthService
import com.worldcup.bracket.Repository.UserRepository


@Component
public class AuthenticationSuccessHandler : SavedRequestAwareAuthenticationSuccessHandler() {

    private val redirectStrategy : RedirectStrategy = DefaultRedirectStrategy();
    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var userRepository : UserRepository

    @Throws(ServletException::class,IOException::class)
    override public fun onAuthenticationSuccess(request : HttpServletRequest, response : HttpServletResponse, authentication : Authentication)  {
        //if redirected from some specific url, need to remove the cachedRequest to force use defaultTargetUrl
        val requestCache : RequestCache = HttpSessionRequestCache();
        val savedRequest : SavedRequest = requestCache.getRequest(request, response);
        val userDetails : DefaultOidcUser = authentication.getPrincipal() as DefaultOidcUser
        logger.info(userDetails.getIdToken().getTokenValue())
        logger.info("TESTING")
        response.setHeader("Authorization","Bearer " + userDetails.getIdToken().getTokenValue())
        userRepository.save(User(userDetails.getName(), userDetails.getEmail(), AuthService.GOOGLE))
        redirectStrategy.sendRedirect(request, response, "/api/testAuth");
    }
}
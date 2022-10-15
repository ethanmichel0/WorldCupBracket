package com.worldcup.bracket 

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class WireMockUtility: ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {

        val wmServer = WireMockServer(WireMockConfiguration().dynamicPort())
        wmServer.start()

        applicationContext.beanFactory.registerSingleton("wireMock", wmServer)

        applicationContext.addApplicationListener {
            if (it is ContextClosedEvent) {
                wmServer.stop()
            }
        }
        
        TestPropertyValues
            .of("footballAPI.baseAPI=http://localhost:${wmServer.port()}/footballAPI/")
            .applyTo(applicationContext) 
    }
    companion object {
        fun stubResponse(url: String, responseBody: String, wireMockServer: WireMockServer) {
            wireMockServer.stubFor(get(urlEqualTo(url))
                .willReturn(
                    aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(responseBody))
            )
        }   
    } 
}
package com.worldcup.bracket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.context.annotation.Bean;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

// import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.context.annotation.Configuration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

@EnableMongoRepositories
@SpringBootApplication
@Configuration
@ConfigurationPropertiesScan
class BracketApplication 

fun main(args: Array<String>) {
	runApplication<BracketApplication>(*args)
}
// class WebConfig : WebMvcConfigurer {

// 	override fun addCorsMappings(registry: CorsRegistry) {

// 		registry.addMapping("/**")
// 				.allowedOrigins("*")
// 				.allowedMethods("PUT", "DELETE", "GET", "POST")
// 				.allowedHeaders("*")
// 	}
// }
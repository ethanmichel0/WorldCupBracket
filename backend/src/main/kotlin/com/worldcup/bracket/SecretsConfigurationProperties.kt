package com.worldcup.bracket

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "secrets")
data class SecretsConfigurationProperties(val footballApiKey : String, val overridePw: String)
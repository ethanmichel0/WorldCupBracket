package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.worldcup.bracket.Entity.Team

@Document(collection="players")
data class Player(
    @Id 
    var id: String,
    val name: String
)
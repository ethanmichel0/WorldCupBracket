package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id

@Document(collection="teams")
data class Team (
    val name: String, 
    val group: String? = null,
    @Id
    val id: String,  // will be same as Id used in football api for simplicity and hence passed in
    val logo: String? = null) {

    override fun equals(other: Any?): Boolean =
        other is Team && other.name == name
    
    var teamIdWhoScored: String? = null // who scored website will allow us to retrieve more information about games. We will manually set the who scored team
    // ids for each team in league so that we can get information for games. 
}
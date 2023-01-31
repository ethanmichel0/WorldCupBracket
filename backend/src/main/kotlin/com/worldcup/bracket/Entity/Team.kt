package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id

@Document(collection="teams")
class Team (
    val name: String, 
    val group: String,
    @Id
    val id: String,  // will be same as Id used in football api for simplicity and hence passed in
    val logo: String) {

    override fun equals(other: Any?): Boolean =
        other is Team && other.name == name
    
    override fun toString(): String = "Team Name:" + name
}
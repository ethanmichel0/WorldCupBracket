package com.worldcup.bracket.Seeder

import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.boot.ApplicationArguments
import java.nio.file.Paths
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken

import com.worldcup.bracket.repository.GroupRepository
import com.worldcup.bracket.Entity.Group
import com.worldcup.bracket.Entity.Game

import java.io.File

@Component()
class DataInitializer(private val groupRepository: GroupRepository) : ApplicationRunner {
    override fun run (args: ApplicationArguments) {
        if (this.groupRepository.findAll().size == 0) { // only initialize groups one time
            val path: String = Paths.get("").toAbsolutePath().toString()
            val jsonString: String = File(path + "/src/constants.json").readText(Charsets.UTF_8)
            val groupsWrapper : GroupsWrapper = Gson().fromJson(jsonString, GroupsWrapper::class.java)
            for (group in groupsWrapper.groups) {
                val gamesInGroup = mutableListOf<Game>()
                for (i in 0 until group.teams.size) {
                    for (j in 1 until group.teams.size) 
                        if(groups.teams[i] != groups.teams[j])
                            gamesInGroup.add(Game(group.teams[i],group.teams[j],false))
                }
                group.games = gamesInGroup
            }
            groupRepository.saveAll(groupsWrapper.groups)
        }
    }
}

data class GroupsWrapper(
    val groups: List<Group>
)
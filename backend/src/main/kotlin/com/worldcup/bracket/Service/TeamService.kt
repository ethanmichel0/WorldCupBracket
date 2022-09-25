package com.worldcup.bracket.Service

import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.repository.TeamRepository
import com.worldcup.bracket.repository.GameRepository
import com.worldcup.bracket.DTO.OverrideGroupSettings

import org.springframework.stereotype.Service


@Service
class TeamService(private val teamRepository : TeamRepository, private val gameRepository: GameRepository){
    fun sortTeamsInGroup(groupLetter : String) {
        val group : List<Team> = teamRepository.findByGroup(groupLetter)
        val pointsComparison = compareByDescending<Team>{it.pointsGroup}
            .thenByDescending{it.goalsForGroup - it.goalsAgainstGroup}
            .thenByDescending{it.goalsForGroup}
            .thenComparator({a,b -> 
                orderTwoTeamsByRelatedGame(a,b)
            })
        group.sortedWith(pointsComparison)
        group[0].positionGroup = 1
        group[1].positionGroup = 2

        teamRepository.saveAll(listOf(group[0],group[1]))
    }

    fun orderTwoTeamsByRelatedGame(team1 : Team, team2: Team) : Int {
        val relatedGame = gameRepository.getAllGamesBetweenTwoTeams(team1.name,team2.name)[0]
        return if (relatedGame.home == team1) relatedGame.awayScore - relatedGame.homeScore else relatedGame.homeScore - relatedGame.awayScore
    }

    // In case teams are still tied after trying to figure out which teams should advance
    fun overrideGroupOrdering(overrideGroupSettings : OverrideGroupSettings) : List<Team> {
        if (overrideGroupSettings.overridePass != System.getenv("OVERRIDE_PW")) throw IllegalArgumentException("You need special permission to set tiebreakers")
        if (! (teamRepository.existsByName(overrideGroupSettings.first) && teamRepository.existsByName(overrideGroupSettings.second))) {
            throw IllegalArgumentException("Teams Provided Do Not Match")
        }

        val team1 : Team = teamRepository.findByName(overrideGroupSettings.first)[0]
        val team2 : Team = teamRepository.findByName(overrideGroupSettings.second)[0]

        val relevantGroup : List<Team> = teamRepository.findByGroup(overrideGroupSettings.groupLetter)
        if (! (relevantGroup.contains(team1)
            && relevantGroup.contains(team2))) {
                throw IllegalArgumentException("Teams Provided Do Not Match")
        }

        team1.positionGroup = 1
        team2.positionGroup = 2

        teamRepository.saveAll(listOf(team1,team2))

        return listOf(team1,team2)
    }
}
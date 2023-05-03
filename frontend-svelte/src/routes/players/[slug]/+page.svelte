<script>
    import {Table} from 'sveltestrap'
    /** @type {import('$types').PageData} */
    export let data;
    console.log("dest")
    console.log(data)
</script>

<h2>Players</h2>
<Table bordered>
    <thead>
      <tr>
        <th>Opponent</th>
        <th>Started</th>
        <th>Minutes</th>
        <th>Goals</th>
        <th>Goals Outside Box</th>
        <th>Errors Leading To Goal</th>
        <th>Assists</th>
        <th>Key Passes</th>
        <th>Total Passes</th>
        <th>Accurate Passes</th>
        <th>Yellow Cards</th>
        <th>Red Cards</th>
        <th>Missed Penalties</th>
        <th>Saved Penalties</th>
        <th>Scored Penalties</th>
        <th>Penalties Drawn</th>
        <th>Penalties Committed</th>
        <th>Clean Sheet</th>
        <th>Attempted Dribbles</th>
        <th>Successful dribbles</th>
        <th>Dribbled Past</th>
        <th>Interceptions</th>
        <th>Total Duels</th>
        <th>Duels Won</th>
        <th>Tackles</th>
        <th>Fouls Drawn</th>
        <th>Fouls Committed</th>
    </tr>
    </thead>
    <tbody>
        {#each Object.values(data) as performance}
        <tr>
            <td><a href={performance.game.id}>{performance.playerSeason.teamSeason.team.name == performance.game.home.team.name ? performance.game.away.team.name : performance.game.home.team.name}</a></td>
            <td>{performance.started}</td>
            <td>{performance.minutes}</td>
            <td>{performance.goals}</td>
            <td>{performance.goalsFromOutsideOfBox}</td>
            <td>{performance.errorsLeadingToGoal}</td>
            <td>{performance.assists}</td>
            <td>{performance.keyPasses}</td>
            <td>{performance.totalPasses}</td>
            <td>{performance.accuratePasses}</td>
            <td>{performance.yellowCards}</td>
            <td>{performance.redCards}</td>
            <td>{performance.penaltiesMissed}</td>
            <td>{performance.penaltiesSaved}</td>
            <td>{performance.penaltiesScored}</td>
            <td>{performance.penaltiesDrawn}</td>
            <td>{performance.penaltiesCommitted}</td>
            <td>{performance.cleanSheet}</td>
            <td>{performance.dribblesAttempted}</td>
            <td>{performance.dribblesSuccesful}</td>
            <td>{performance.dribblesPast}</td>
            <td>{performance.interceptions}</td>
            <td>{performance.totalDuels}</td>
            <td>{performance.duelsWon}</td>
            <td>{performance.tackles}</td>
            <td>{performance.foulsDrawn}</td>
            <td>{performance.foulsCommitted}</td>
        </tr>
        {/each}
        <tr>
            <td>All Opponents</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => currentValue.started ? accumulator +1 : accumulator,0)}
            </td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.minutes,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.goals,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.goalsFromOutsideOfBox,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.errorsLeadingToGoal,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.assists,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.keyPasses,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.totalPasses,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.accuratePasses,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.yellowCards,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.redCards,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.penaltiesMissed,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.penaltiesSaved,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.penaltiesScored,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.penaltiesDrawn,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.penaltiesCommitted,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => currentValue.cleanSheet ? accumulator +1 : accumulator,0)}
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.dribblesAttempted,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.dribblesSuccesful,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.dribblesPast,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.interceptions,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.totalDuels,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.duelsWon,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.tackles,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.foulsDrawn,0)}</td>
            <td>{Object.values(data).reduce((accumulator, currentValue) => accumulator + currentValue.foulsCommitted,0)}</td>
        </tr>
    </tbody>
  </Table>
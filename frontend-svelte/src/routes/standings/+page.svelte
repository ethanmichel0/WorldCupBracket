<script>
    import {Table} from 'sveltestrap'
    /** @type {import('../$types').PageData} */
    export let data;
    console.log(data)
</script>

{#each Object.values(data) as group}
    <Table bordered>
      <thead>
        <tr>
          <th>Position</th>
          <th>Team</th>
          <th>Logo</th>
          <th>Total Games</th>
          <th>Wins</th>
          <th>Losses</th>
          <th>Ties</th>
          <th>Goals For</th>
          <th>Goals Against</th>
        </tr>
      </thead>
      <tbody>
        {#each group as team, pos}
          <tr>
            <td>{pos + 1}</td>
            <td><a href={`/team/${team.id}`}>{team.name}</a></td>
            <td><img src={team.logo} height="100px"/></td>
            <td>{team.totalGamesGroup}</td>
            <td>{team.winsGroup}</td>
            <td>{team.lossesGroup}</td>
            <td>{team.ties}</td> <!-- Only can have ties in group stage, thus API doesn't have a "tiesGroup property"-->
            <td>{team.goalsForGroup}</td>
            <td>{team.goalsAgainstGroup}</td>
          </tr>
        {/each}
      </tbody>
    </Table>
{/each}
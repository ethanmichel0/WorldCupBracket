<script>
    import {Table, Image} from 'sveltestrap'
    /** @type {import('../$types').PageData} */
    export let data;
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
        {#each group.teams as team, pos}
          <tr>
            <td scope="row">{pos+1}</td>
            <td>{team.name}</td>
            <td><img src={team.logo} height="100px"/></td>
            <td>{team.totalGames}</td>
            <td>{team.wins}</td>
            <td>{team.losses}</td>
            <td>{team.ties}</td>
            <td>{team.goalsFor}</td>
            <td>{team.goalsAgainst}</td>
          </tr>
        {/each}
      </tbody>
    </Table>
{/each}
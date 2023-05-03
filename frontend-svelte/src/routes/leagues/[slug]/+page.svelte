<script>
    import {Table} from 'sveltestrap'
	import { getBaseUrlFromServer } from '../../../lib/utils';
    /** @type {import('$types').PageData} */
    export let data;
    console.log("asdf")
</script>

<Table bordered>
    <thead>
      <tr>
        <th>Name</th>
        <th>Logo</th>
        <th>Games Played</th>
        <th>Wins</th>
        <th>Losses</th>
        <th>Ties</th>
        <th>Goals For</th>
        <th>Goals Against</th>
        <th>Position</th>
      </tr>
    </thead>
    <tbody>
        {#each Object.values(data.league) as team}
        <tr>
          <td><a href={`/teams/${team.team.id}`}>{team.team.name}</a></td>
          <td><img src={team.team.logo} height="100px"/></td>
          <td>{team.wins + team.losses + team.ties}</td>
          <td>{team.wins}</td>
          <td>{team.losses}</td>
          <td>{team.ties}</td>
          <td>{team.goalsFor}</td>
          <td>{team.goalsAgainst}</td>
          <td>{team.position}</td>
        </tr>
        {/each}
    </tbody>
  </Table>
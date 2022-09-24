<script>
    import {Table} from 'sveltestrap'
    /** @type {import('$types').PageData} */
    export let data;
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
      </tr>
    </thead>
    <tbody>
        <tr>
          <td>{team.name}</td>
          <td><img src={team.logo} height="100px"/></td>
          <td>{team.totalGames}</td>
          <td>{team.wins}</td>
          <td>{team.losses}</td>
          <td>{team.ties}</td>
          <td>{team.goalsFor}</td>
          <td>{team.goalsAgainst}</td>
        </tr>
ion    </tbody>
  </Table>
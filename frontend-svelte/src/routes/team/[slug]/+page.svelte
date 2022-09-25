<script>
    import {Table} from 'sveltestrap'
    /** @type {import('$types').PageData} */
    export let data;
    console.log("dest")
    console.log(data)
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
          <td>{data.name}</td>
          <td><img src={data.logo} height="100px"/></td>
          <td>{data.totalGames}</td>
          <td>{data.wins}</td>
          <td>{data.losses}</td>
          <td>{data.ties}</td>
          <td>{data.goalsFor}</td>
          <td>{data.goalsAgainst}</td>
        </tr>
    </tbody>
  </Table>
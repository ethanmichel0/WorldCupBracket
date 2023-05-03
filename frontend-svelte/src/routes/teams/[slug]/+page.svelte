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
        <th>Name</th>
        <th>Position</th>
        <th>Number</th>
        <th>Goals</th>
        <th>Assists</th>
      </tr>
    </thead>
    {#each Object.values(data.players) as player}
    <tbody>
        <tr>
          <td><a href={`/players/${player.player.id}`}>{player.player.name}</a></td>
          <td>{player.position}</td>
          <td>{player.number}</td>
          <td>{player.goals}</td>
          <td>{player.assists}</td>
        </tr>
    </tbody>
    {/each}
  </Table>
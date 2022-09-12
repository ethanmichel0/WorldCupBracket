<script>
    import {Table, Image} from 'sveltestrap'
    /** @type {import('../$types').PageData} */
    export let data;
    export let displayType;
</script>
    <Table bordered>
      <thead>
        <tr>
          <th>Team1</th>
          <th>Team2</th>
          <th>Group</th>
          <th>Home Score</th>
          <th>Away Score</th>
          <th>Date</th>
          {#if displayType=="upcoming"}
            <th>Minute</th>
          {/if}
        </tr>
      </thead>
      {#each Object.values(data) as game}
      <tbody>
          <tr>
            <td>{game.team1.name}</td>
            <td>{game.team2.name}</td>
            <td>{game.group}</td>
            <td>{game.homeScore}</td>
            <td>{game.awayScore}</td>
            <td>{new Date(game.date * 1000).toLocaleString()}</td>
            {#if displayType=="upcoming"}
            <td>{game.currentMinute}</td>
            {/if}
          </tr>
      </tbody>
      {/each}
    </Table>
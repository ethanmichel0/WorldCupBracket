<script>
    import {Table, Image} from 'sveltestrap'
    /** @type {import('../$types').PageData} */
    export let data;
    export let displayType;
</script>
    <Table bordered>
      <thead>
        <tr>
          <th>home</th>
          <th>away</th>
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
            <td>{game.home.name}</td>
            <td>{game.away.name}</td>
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
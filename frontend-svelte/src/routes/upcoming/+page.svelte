<script>
    import {Table, Image} from 'sveltestrap'
    /** @type {import('../$types').PageData} */
    export let data;
    console.log(data);
</script>

{#each Object.values(data) as game}
    <Table bordered>
      <thead>
        <tr>
          <th>Team1</th>
          <th>Team2</th>
        </tr>
      </thead>
      <tbody>
          <tr>
            <td>{game.team1.name}</td>
            <td>{game.team2.name}</td>
          </tr>
      </tbody>
    </Table>
{/each}
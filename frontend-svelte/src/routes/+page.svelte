<script>
    import { TabContent, TabPane, Table, Image} from 'sveltestrap';
    /** @type {import('./$types').PageData} */
    export let data;
</script>
<TabContent>
    {data}
    <TabPane tabId="alpha" tab="Standings" active>
    {#each Object.values(data) as group}
    <Table bordered>
      <thead>
        <tr>
          <th>Position</th>
          <th>Team</th>
          <th>Logo</th>
          <th>Wins</th>
          <th>Losses</th>
          <th>Goals For</th>
          <th>Goals Against</th>
        </tr>
      </thead>
      <tbody>
        {#each group.teams as team, pos}
          <tr>
            <th scope="row">{pos+1}</th>
            <td>{team.name}</td>
            <td><Image src={team.logo} height="100px" width="100px"}></Image></td>
            <td>safda</td>
          </tr>
        {/each}
        <tr>
      </tbody>
    </Table>
    {/each}
  </TabPane>
  </TabContent>




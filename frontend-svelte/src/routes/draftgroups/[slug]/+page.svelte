<script>
    import {Table} from 'sveltestrap'
    /** @type {import('$types').PageData} */
    export let data;
</script>

<h1>{data.name}</h1>
<h1>Members:</h1>
<h1>Owner: {data.owner.name}</h1>
<h1>Draft Time: {data.draftTime}</h1>
<h1>Leagues: {data.leagues}</h1>
<Table bordered>
    <thead>
      <tr>
        <th></th>
        <th>Name</th>
        <th>Email</th>
      </tr>
    </thead>
    <tbody>
        {#each Object.values(data.members) as member}
        <tr>
          <td>{member.name}</td>
          <td>{member.email}</td>
        </tr>
        {/each}
    </tbody>
  </Table>
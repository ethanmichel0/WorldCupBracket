<script>
    import {Form, Table} from 'sveltestrap'
    import {page} from '$app/stores';   
    /** @type {import('$types').PageData} */
    export let data;
    function getGroupStatus(group) {
        console.log("draft time is :" + group.draftTime)
        if (group.draftComplete) {
            return 'complete'
        }
        if (group.draftTime == -1) {
            return 'not scheduled'
        }
        return `upcoming at time ${group.draftTime}`
    }
</script>

<h1>Add a draft group</h1>
<Form method="POST" action="?/createGroup">
    <label for="groupName">Group Name</label>
    <input type="text" id="groupName" name="name"><br><br>
    <label for="password">Password</label>
    <input type="text" id="password" name="password"><br>
    <label for="numPlayers">Number of Players per Team</label>
    <input type="text" id="numPlayers" name="numPlayers"><br>
    <label for="amountTime">Amount of time per turn in seconds</label>
    <input type="text" id="amountTime" name="amountTimePerTurnInSeconds"><br>
    <label for="leagueIds">Choose the leagues for the draft</label>
    <select name="leagueIds" id="leagueIds" multiple>
        <option value="39">Premier League</option>
        <option value="322">Fake LEague</option>
    </select>
    <input type="submit" value="Submit">
</Form>


<h1>Join an Existing Draft Group</h1>
<Form method="POST" action="?/joinGroup">
    <label for="groupName">Group Name</label>
    <input type="text" id="groupName" name="name"><br><br>
    <label for="password">Password</label>
    <input type="text" id="password" name="password"><br>
    <input type="submit" value="Submit">
</Form>

{#if data.ongoing.length == 1}
<h1>Current Draft</h1>
<a href={`/draftGroups/${encodeURIComponent(data.ongoing[0].name)}/draftroom`}>{data.ongoing[0].name}</a>
{/if}

<h1>All of my groups</h1>
<Table>
    <thead>
        <tr>
          <th>Group Name</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
          {#each Object.values(data.otherGroups) as group}
          <tr>
            <td><a href={`/draftgroups/${encodeURIComponent(group.name)}`}>{group.name} 
                {#if data.userAuth.email == group.owner.email} (owner) {/if}
            </a></td>
            <td>
                {getGroupStatus(group)}
            </td>
          </tr>
          {/each}
      </tbody>
</Table>
<p></p>
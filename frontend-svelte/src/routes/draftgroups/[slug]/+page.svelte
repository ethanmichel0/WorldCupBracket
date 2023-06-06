<script>
    import {Table, Input, Form} from 'sveltestrap'
    import { enhance } from '$app/forms';
    /** @type {import('$types').PageData} */
    export let data;
    console.log(data)
    /**
	 * @type {number}
	 */
    let timeStampMillis;
    $: validTime = true
    // the chosen draft time must be at least ten minutes in the future

    console.log("valudTime is:" + validTime)

</script>

<h1>{data.draftGroup.name}</h1>
<h4>Owner: {data.draftGroup.owner.name} ({data.draftGroup.owner.email})</h4>
<h4>Draft Time: {data.draftGroup.draftTime == -1 ? "Unset" : new Date(data.draftGroup.draftTime * 1000)}</h4>
<h4>Leagues: {data.draftGroup.leagues.map(l => l.name)}</h4>
<h1>Members:</h1>

<Table bordered>
    <thead>
      <tr>
        <th>Name</th>
        <th>Email</th>
      </tr>
    </thead>
    <tbody>
        {#each (data.members) as member}
        <tr>
          <td>{member.name}</td>
          <td>{member.email}</td>
        </tr>
        {/each}
    </tbody>
</Table>
{#if data.userAuth.email == data.draftGroup.owner.email} 
  <form 
    method="POST"
    use:enhance={({ data }) => {
      data.set("unixTimeStamp",Date.parse(timeStampMillis)/1000)
    }}>
    Set Draft Time! (in your local time {Intl.DateTimeFormat().resolvedOptions().timeZone})
    <Input
      type="datetime-local"
      name="datetime"
      id="exampleDatetime"
      placeholder="datetime placeholder"
      bind:value={timeStampMillis}
      invalid={!validTime}
      feedback={validTime ? "" : "the chosen draft time must be at least ten minutes in the future"}
      
    />
    <button type="submit" disabled={!timeStampMillis || !validTime}>Set Time!</button>
</form>
{/if}
{data.draftGroup.availablePlayers.length}
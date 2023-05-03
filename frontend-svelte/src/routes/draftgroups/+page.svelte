<script>
    import {Form} from 'sveltestrap'
    import { getBaseUrlFromClient } from '$lib/utils.js';
    /** @type {import('$types').PageData} */
    export let data;
</script>

<h1>Add a draft group</h1>
<Form method="POST"}>
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
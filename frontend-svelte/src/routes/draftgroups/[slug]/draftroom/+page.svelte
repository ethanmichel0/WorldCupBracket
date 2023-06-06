<script>
	import { onMount } from 'svelte';
    import {Table, Button} from 'sveltestrap'
    import {over} from "stompjs"
    import SockJS from "sockjs-client/dist/sockjs"
    import { enhance } from '$app/forms';


    /** @type {import('$types').PageData} */
    export let data;
    let playerSelected

    let client
    let days, hours, minutes, seconds
    const myIndex = data.draftRoomInfo.draftGroup.members.indexOf(data.userAuth.email)
    let userCurrentTurnIndex = data.draftRoomInfo.draftGroup.indexOfCurrentUser

    $: turnsUntilMine = (myIndex >= userCurrentTurnIndex) ? myIndex - userCurrentTurnIndex: data.draftRoomInfo.draftGroup.members.length - userCurrentTurnIndex + myIndex
    $: draftLive = days <= 0 && hours <= 0 && minutes <=0

    onMount(async() => {
        console.log("IN LOAD")
        let sockJS = new SockJS('http://localhost:6868/ws')
        client = over(sockJS)
        client.connect({
            Cookie : document.cookie},
            onConnectedCallback,
            (error) => console.log(`There was an error connecting to websocket: ${error}`))
        function onConnectedCallback() {
            client.subscribe(`/topic/draft/${data.groupName}`, function(message) {
                console.log("received mess")
                let bodyAsObj = JSON.parse(message.body)
                console.log("message is:" + JSON.stringify(bodyAsObj))
                console.log(bodyAsObj.draftGroup.indexOfCurrentUser)
                console.log(`player selected is: ${bodyAsObj.playerSelected.player.name}`)
                playerSelected = bodyAsObj.playerSelected.player.name
                userCurrentTurnIndex = bodyAsObj.draftGroup.indexOfCurrentUser
                data.draftRoomInfo.draftGroup.availablePlayers.filter(playerSeason => playerSeason.player.id != bodyAsObj.playerSelected.player.id)
                console.log(bodyAsObj.draftGroup.indexOfCurrentUser + "is curr ind")
            })
        }
    })

    function draftPlayer(playerId) {
        client.send(`/app/api/draftgroups/${data.groupName}/draftplayer/${playerId}`,{},"")
    }

    // Set the date we're counting down to
    var countDownDate = new Date(data.draftRoomInfo.draftGroup.draftTime * 1000).getTime();

    // Update the count down every 1 second
    // example from https://www.w3schools.com/howto/howto_js_countdown.asp
    var x = setInterval(function() {

        // Get today's date and time
        var now = new Date().getTime();

        // Find the distance between now and the count down date
        var distance = countDownDate - now;

        // Time calculations for days, hours, minutes and seconds
        days = Math.floor(distance / (1000 * 60 * 60 * 24));
        hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        seconds = Math.floor((distance % (1000 * 60)) / 1000);

        // If the count down is finished, write some text
        if (distance < 0) {
            clearInterval(x);
        }
    }, 1000);
</script>

{#if days >= 0 && hours >=0 && minutes >= 0 && seconds >=0}
<p>
    {days} days, {hours} hours,
    {minutes} minutes {seconds} seconds until start of draft
</p>
{/if}
<p>
    {turnsUntilMine} until my turn!
</p>
{#if playerSelected}
    <p>Last selected player is: {playerSelected}</p>
{/if}

<h1>All Available Players</h1>
    <Table>
        <thead>
            <tr>
            <th>Player Name</th>
            <th>Player Team</th>
            <th>Draft! 
                {#if turnsUntilMine > 0}
                    (not yet my turn)
                {/if}

            </th>
            </tr>
        </thead>
        <tbody>
            {#each Object.values(data.draftRoomInfo.draftGroup.availablePlayers) as player}
            <tr>
                <td>{player.player.name}</td>
                <td>{player.teamSeason.team.name}</td>
                <td><Button value={player.player.id} disabled={turnsUntilMine>0||!draftLive} on:click={() => draftPlayer(player.player.id)}>draft</Button></td>
            </tr>
            {/each}
        </tbody>
    </Table>
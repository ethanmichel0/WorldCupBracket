<script>
	import { onMount } from 'svelte';
    import {Table, Button, TabContent, TabPane, Input, Form} from 'sveltestrap'
    import {over} from "stompjs"
    import SockJS from "sockjs-client/dist/sockjs"

    /** @type {import('$types').PageData} */
    export let data;
    let playerSelected

    let client
    let days, hours, minutes, seconds
    const myIndex = data.draftRoomInfo.draftGroup.members.indexOf(data.userAuth.email)
    let userCurrentTurnIndex = data.draftRoomInfo.draftGroup.indexOfCurrentUser

    $: turnsUntilMine = (myIndex >= userCurrentTurnIndex) ? myIndex - userCurrentTurnIndex: data.draftRoomInfo.draftGroup.members.length - userCurrentTurnIndex + myIndex
    $: draftLive = days <= 0 && hours <= 0 && minutes <=0
    let allPlayersMatchingCriteria = []
    $: allPlayersEveryPosition = data.draftRoomInfo.draftGroup.availableForwards.concat(data.draftRoomInfo.draftGroup.availableMidfielders,data.draftRoomInfo.draftGroup.availableDefenders,data.draftRoomInfo.draftGroup.availableGoalkeepers)
    $: allTeamNames = [...new Set(allPlayersEveryPosition.map(playerSeason => playerSeason.teamSeason.team.name))]
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

    function draftPlayer(playerSeason) {
        client.send(`/app/api/draftgroups/${data.groupName}/draftplayer/${playerSeason.player.id}`,{},"")
        
        switch (playerSeason.position) {
            case 'Attacker':
                data.draftRoomInfo.availableForwards = data.draftRoomInfo.availableForwards.filter(forward => forward.player.id != playerSeason.player.id)
                break;
            case 'Midfielder':
                data.draftRoomInfo.availableMidfielders = data.draftRoomInfo.availableMidfielders.filter(midfielder => midfielder.player.id != playerSeason.player.id)
                break;
            case 'Defender':
                data.draftRoomInfo.availableDefenders = data.draftRoomInfo.availableDefenders.filter(defender => defender.player.id != playerSeason.player.id)
                break;
            default:
                data.draftRoomInfo.availableGoalkeepers = data.draftRoomInfo.availableGoalkeepers.filter(goalkeeper => goalkeeper.player.id != playerSeason.player.id)
        }
    }

    function searchByTeam(event) {
        let teamName = event.target.value
        allPlayersMatchingCriteria = allPlayersEveryPosition.filter(playerSeason => playerSeason.teamSeason.team.name == teamName)
    }

    function searchByPosition(event) {
        let position = event.target.value
        allPlayersMatchingCriteria = allPlayersEveryPosition.filter(playerSeason => playerSeason.position == position)
    }

    function searchByName(event) {
        let nameLowerCased = event.target.value.toLowerCase()
        allPlayersMatchingCriteria = allPlayersEveryPosition.filter(playerSeason => playerSeason.player.name.toLowerCase().includes(nameLowerCased))
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

<TabContent>
    <TabPane tabId="team" tab="Team" active>
      <h2>Players By Team</h2>
      <Input type="select" on:change={searchByTeam}>
        {#each allTeamNames as teamName}
            <option>{teamName}</option>
        {/each}
      </Input>
    </TabPane>
    <TabPane tabId="position" tab="Position">
      <h2>Players By Position</h2>
      <Input type="select" on:change={searchByPosition}>
        <option selected>Forward</option>
        <option>Midfielder</option>
        <option>Defender</option>
        <option>Goalkeeper</option>
      </Input>
    </TabPane>
    <TabPane tabId="search" tab="Search For a Player">
      <h2>Search For A Player</h2>
        <Input
          type="search"
          name="search"
          id="exampleSearch"
          placeholder="enter a player's name to search" 
          on:input={searchByName}/>
    </TabPane>
  </TabContent>

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
            <th>Add To Watchlist
            </th>
            </tr>
        </thead>
        <tbody>
            {#each allPlayersMatchingCriteria as playerSeason}
            <tr>
                <td>{playerSeason.player.name}</td>
                <td>{playerSeason.teamSeason.team.name}</td>
                <td><Button value={playerSeason.player.id} disabled={turnsUntilMine>0||!draftLive} on:click={() => draftPlayer(playerSeason)}>draft</Button></td>
                <td><form method="POST" action="?/addToWatchlist">
                    <input name="playerId" value={playerSeason.player.id} type="hidden"/>
                    <input value="submit" type="submit"/>
                </form></td>
            </tr>
            {/each}
        </tbody>
    </Table>
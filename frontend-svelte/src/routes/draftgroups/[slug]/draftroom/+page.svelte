<script>
	import { onMount } from 'svelte';
    import {Table, Button, TabContent, TabPane, Input, Form} from 'sveltestrap'
    import {over} from "stompjs"
    import SockJS, { stringify } from "sockjs-client/dist/sockjs"
    import {dndzone} from "svelte-dnd-action";
	import { enhance } from '$app/forms';

    /** @type {import('$types').PageData} */
    export let data;
    let lastSelectedPlayer
    let lastUserToSelect
    let onWatchListTab = false
    let onMySquadTab = false

    let client 
    let days, hours, minutes, seconds
    const myIndex = data.draftRoomInfo.draftGroup.members.indexOf(data.userAuth.email)
    let userCurrentTurnIndex = data.draftRoomInfo.draftGroup.indexOfCurrentUser

    $: turnsUntilMine = (myIndex >= userCurrentTurnIndex) ? myIndex - userCurrentTurnIndex: data.draftRoomInfo.draftGroup.members.length - userCurrentTurnIndex + myIndex
    $: draftLive = days <= 0 && hours <= 0 && minutes <=0
    let allPlayersMatchingCriteria = []
    $: myPlayers = data.draftRoomInfo.playerDraft.draftedPlayersAllPositions
    console.log(myPlayers)
    $: allPlayersEveryPosition = data.draftRoomInfo.draftGroup.availableForwards.concat(
        data.draftRoomInfo.draftGroup.availableMidfielders,
        data.draftRoomInfo.draftGroup.availableDefenders,
        data.draftRoomInfo.draftGroup.availableGoalkeepers).map(
            playerSeason => {
                playerSeason.id = playerSeason.player.id // for the drag and drop component, the unique key cannot be nested (playerSeason.player.id),
                // so we must copy this property so it is nested directly under playerSeason.
                // https://github.com/isaacHagoel/svelte-dnd-action/issues/464
                return playerSeason
            })
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
                let bodyAsObj = JSON.parse(message.body)
                console.log(bodyAsObj.draftGroup.indexOfCurrentUser)
                lastSelectedPlayer = bodyAsObj.playerSelected
                lastUserToSelect = bodyAsObj.playerDraft.userName
                userCurrentTurnIndex = bodyAsObj.draftGroup.indexOfCurrentUser
                handlePlayerDrafted(lastSelectedPlayer)
            })
        }
    })

    function handleDndConsider(e) {
        allPlayersMatchingCriteria = e.detail.items;
    }
    function handleDndFinalize(e) {
        allPlayersMatchingCriteria = e.detail.items;
    }

    function handlePlayerDrafted(playerSeason) {
        console.log("in handler! value is" + playerSeason)
        data.draftRoomInfo.playerDraft.watchListUndrafted = data.draftRoomInfo.playerDraft.watchListUndrafted.filter(ps => ps != playerSeason)
        console.log("46: size is " + data.draftRoomInfo.draftGroup.availableMidfielders.length)
        switch (playerSeason.position) {
            case 'Attacker':
                data.draftRoomInfo.draftGroup.availableForwards = data.draftRoomInfo.draftGroup.availableForwards.filter(forward => forward.player.id != playerSeason.player.id)
                break;
            case 'Midfielder':
                data.draftRoomInfo.draftGroup.availableMidfielders = data.draftRoomInfo.draftGroup.availableMidfielders.filter(midfielder => midfielder.player.id != playerSeason.player.id)
                break;
            case 'Defender':
                data.draftRoomInfo.draftGroup.availableDefenders = data.draftRoomInfo.draftGroup.availableDefenders.filter(defender => defender.player.id != playerSeason.player.id)
                break;
            default:
                data.draftRoomInfo.draftGroup.availableGoalkeepers = data.draftRoomInfo.draftGroup.availableGoalkeepers.filter(goalkeeper => goalkeeper.player.id != playerSeason.player.id)
        }
    }

    function handleTabChanges(event) {
        if (event.detail == "watchlist") {
            onWatchListTab = true
            onMySquadTab = false
            showWatchlist()
        } else if (event.detail == "myplayers") {
            onWatchListTab = false
            onMySquadTab = true
        } else {
            onWatchListTab = false
            onMySquadTab = false
            allPlayersMatchingCriteria = []
        }
    }

    function draftPlayer(playerSeason) {
        console.log("player id is " + playerSeason.player.id)
        client.send(`/app/api/draftgroups/${data.groupName}/draftplayer/${playerSeason.player.id}`,{},"")
        // TODO make sure that player was drafted properly
        myPlayers.push(playerSeason)
    }

    function showWatchlist() {
        console.log("showing watch list")
        allPlayersMatchingCriteria = data.draftRoomInfo.playerDraft.watchListUndrafted
        onWatchListTab = true
    }

    // async function reorderWatchList() {
    //     console.log(allPlayersMatchingCriteria.map(x => x.id))
    //     console.log("in reorder watch list")
    //     console.log("cookie is: " + data.authCookie)
    //     let response = await fetch(`${getBaseUrlFromClient()}/api/draftgroups/${data.groupName}/reorderWatchList`,
    //     {
    //     credentials:'include',
    //     headers:{
    //         Cookie : `JSESSIONID=${data.userAuth}`,
    //         "Content-Type": "application/json",
    //         'Accept': 'text/html'
    //     },
    //     method: "PUT",
    //     body: JSON.stringify({updatedWatchList:allPlayersMatchingCriteria})})
    //     console.log(response.status + "is response status")
    //     console.log(await response.text() + "is text")
    // }

    function searchByTeam(event) {
        let teamName = event.target.value
        allPlayersMatchingCriteria = allPlayersEveryPosition.filter(playerSeason => playerSeason.teamSeason.team.name == teamName)
    }

    function searchByPosition(event) {
        let position = event.target.value
        console.log("position is: " + position)
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
{#if lastSelectedPlayer}
    <p>Last selected player was: {lastSelectedPlayer.player.name} by {lastUserToSelect} </p>
{/if}

<TabContent on:tab={handleTabChanges}>
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
        <option selected value="Attacker">Forward</option>
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
    <TabPane tabId="watchlist" tab="My Watchlist">
        <h2>Please note that the order matters!</h2>
        <p>The top player on your watchlist still meeting positional requirements and still undrafted will be drafted first on your next turn if you fail to make your selection in time.</p>
    </TabPane>
    <TabPane tabId="myplayers" tab="My Squad">
        <h2>You can offer trades once the draft is finished</h2>
        <Table>
            <thead>
                <tr>
                <th>Player Name</th>
                <th>Player Team</th>
                <th>Player Position</th>
            </thead>
                <tbody>
                    {#each myPlayers as playerSeason(playerSeason.player.id)}
                            <tr>
                                <td>{playerSeason.player.name}</td>
                                <td>{playerSeason.teamSeason.team.name}</td>
                                <td>{playerSeason.position}</td>
                            </tr>
                    {/each}
                </tbody>
        </Table>
    </TabPane>
  </TabContent>

    {#if ! onMySquadTab}
        <Table>
            <thead>
                <th>Player Name</th>
                <th>Player Team</th>
                <th>Player Position</th>
                <th>Draft! 
                    {#if turnsUntilMine > 0}
                        (not yet my turn)
                    {/if}
                </th>
                {#if !onWatchListTab}
                    <th>Add To Watchlist</th>
                {/if}
            </thead>
                <tbody use:dndzone={{items: allPlayersMatchingCriteria}} on:consider={handleDndConsider} on:finalize={handleDndFinalize}>
                    {#each allPlayersMatchingCriteria as playerSeason(playerSeason.id)}
                            <tr>
                                <td>{playerSeason.player.name}</td>
                                <td>{playerSeason.teamSeason.team.name}</td>
                                <td>{playerSeason.position}</td>
                                <td><Button value={playerSeason.player.id} disabled={turnsUntilMine>0||!draftLive} on:click={() => draftPlayer(playerSeason)}>draft</Button></td>
                                {#if !onWatchListTab}
                                    <td><form method="POST" action="?/addToWatchlist">
                                        <input name="playerId" value={playerSeason.player.id} type="hidden"/>
                                        <input type="submit" value="asdf"/>
                                    </form></td>
                                {/if}
                            </tr>
                    {/each}
                </tbody>
                    {#if onWatchListTab}
                        <button type="submit" form=reorderform>Reorder</button>
                    {/if}
        </Table>
    {/if}


    <form
        method="POST"
        action="?/reorderWatchList"
        use:enhance={({ data }) => {
            data.set("updatedWatchList",JSON.stringify({updatedWatchList:allPlayersMatchingCriteria}))
            // this trick means that we don't have to worry about configuring CORS server side
        }}
        id="reorderform">
    </form>
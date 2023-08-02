<script>
    import {Table,
    Input, 
    TabContent, 
    TabPane,
    Modal,
    ModalBody,
    ModalHeader,
    ModalFooter,
    Button,
    Container,
    Row,
    Col} from 'sveltestrap'
    import { enhance } from '$app/forms';
    import {dndzone} from "svelte-dnd-action";
	import { getBaseUrlFromClient } from '$lib/utils';

    /** @type {import('$types').PageData} */
    export let data;

   let allPlayersDraftedAndUndraftedWithOwner = data.playerDrafts ? data.playerDrafts.flatMap(playerDraft => {
        let allPositions = playerDraft.draftedPlayersAllPositions
        allPositions.forEach(playerSeason => {
          playerSeason.ownerName=playerDraft.userName
          playerSeason.ownerEmail=playerDraft.userEmail
          playerSeason.ownerId=playerDraft.id
          // for dnd zone, unique id must not be nested so we must copy property
        })
      return allPositions}).concat(data.draftGroup.availablePlayersAllPositions) : []
  console.log(allPlayersDraftedAndUndraftedWithOwner.length + "length on line 29")
  console.log(data.draftGroup.availablePlayersAllPositions.filter(x => x.ownerEmail == null))
  console.log(allPlayersDraftedAndUndraftedWithOwner.filter(x => x.ownerEmail == null))
  let myPlayers = allPlayersDraftedAndUndraftedWithOwner.filter(playerSeason => playerSeason.ownerEmail == data.userAuth.email)
  let allPlayersOwnedByTradeRecipient = []
  let playerTradingWith = {name:null,id:null}
  let allMatchingPlayers = []
  let timeStampMillis
  let inTradeModal=false
  const toggle = () => (inTradeModal = !inTradeModal);
  let tradeForUnclaimedPlayers = false;
  // differentiates whether the user is trading for players that haven't been drafted yet (in which case trade can happen instantaneously) or if with another player
  // and must first be approved by that player

  let offeredPlayers = []
  let requestedPlayers = []
    $: validTime = true
    // the chosen draft time must be at least ten minutes in the future

    function handleDndSortMyPlayers(e) {
      myPlayers = e.detail.items;
    }

    function handleDndSortOtherPersonsPlayers(e) {
      allPlayersOwnedByTradeRecipient = e.detail.items
    }

    function handleDndSortOfferedPlayers(e) {
      offeredPlayers = e.detail.items
      console.log(offeredPlayers + "is offered Players")
    }

    function handleDndSortRequestedPlayers(e) {
      requestedPlayers = e.detail.items
      console.log(requestedPlayers + "is requested Players")
    }

    function reset() {
      requestedPlayers = []
      offeredPlayers = []
      myPlayers = allPlayersDraftedAndUndraftedWithOwner.filter(playerSeason => playerSeason.ownerEmail == data.userAuth.email)
      allPlayersOwnedByTradeRecipient = []
    }


    function searchByName(event) {
      let nameLowerCased = event.target.value.toLowerCase()
      if (nameLowerCased.length == 0) allMatchingPlayers = []
      allMatchingPlayers = allPlayersDraftedAndUndraftedWithOwner.filter(playerSeason => playerSeason.player.name.toLowerCase().includes(nameLowerCased))
    }

    function openTradeModal(playerSeason) {
      console.log(playerSeason.ownerEmail + "is email")
      playerTradingWith.name = playerSeason.ownerName
      playerTradingWith.id = playerSeason.ownerId
      requestedPlayers.push(playerSeason)
      allPlayersOwnedByTradeRecipient = allPlayersDraftedAndUndraftedWithOwner.filter(ps => ps.ownerEmail==playerSeason.ownerEmail && ps.id != playerSeason.id)
      console.log(allPlayersOwnedByTradeRecipient.map(s => s.player.name) + "is pool to trade from")
      toggle()
    }

    

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
    action="?/setTime" 
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
{#if data.playerDrafts}
<h3>Players In Squad</h3>
<TabContent>
  {#each data.playerDrafts as playerDraft}
    <TabPane tabId={playerDraft.userEmail} tab={playerDraft.userName}>
      <Table>
          <thead>
              <tr>
              <th>Player Name</th>
              <th>Player Team</th>
              <th>Player Position</th>
          </thead>
              <tbody>
                  {#each playerDraft.draftedPlayersAllPositions as playerSeason(playerSeason.player.id)}
                          <tr>
                              <td>{playerSeason.player.name}</td>
                              <td>{playerSeason.teamSeason.team.name}</td>
                              <td>{playerSeason.position}</td>
                          </tr>
                  {/each}
              </tbody>
      </Table>



    </TabPane>



  {/each}
</TabContent>
<h2>Find a Player</h2>
<Input
  type="search"
  name="search"
  id="exampleSearch"
  placeholder="enter a player's name to search" 
  on:input={searchByName}/>
<Table>
  <thead>
    <th>Name</th>
    <th>Team</th>
    <th>Position</th>
    <th>Owner</th>
    <th>Make a Trade</th>
  </thead>
  <tbody>
  {#each allMatchingPlayers as playerSeason(playerSeason.player.id)}
    <tr>
      <td>{playerSeason.player.name}</td>
      <td>{playerSeason.teamSeason.team.name}</td>
      <td>{playerSeason.position}</td>
      <td>{data.userAuth.email == playerSeason.ownerEmail ? "Me" : (playerSeason.ownerName  ?? "Unowned")}</td>
      <td><Button color="danger" on:click={() => openTradeModal(playerSeason,)}>Make a trade</Button></td>
    </tr>
    {/each}
  </tbody>
</Table> 
{/if}

<Modal isOpen={inTradeModal} on:close={reset}>
  <ModalHeader {toggle}>Proposed Trade</ModalHeader>
  <ModalBody>
    <Container>
      <Row>
        <Col>
          <div>
            <div>My Players</div>
            <div use:dndzone={{items:myPlayers, type:"Offering"}}
                 on:consider={handleDndSortMyPlayers} on:finalize={handleDndSortMyPlayers}>
                {#each myPlayers as player (player.id)}
                    <div class="card">
                        {player.player.name}
                    </div>
                {/each}
            </div>
        </Col>
        <Col>
          <div>
            <div>{playerTradingWith.name}'s players</div>
            <div use:dndzone={{items:allPlayersOwnedByTradeRecipient, type:"Requesting"}}
                 on:consider={handleDndSortOtherPersonsPlayers} on:finalize={handleDndSortOtherPersonsPlayers}>
                {#each allPlayersOwnedByTradeRecipient as player (player.id)}
                    <div class="card">
                        {player.player.name}
                    </div>
                {/each}
            </div>
        </Col>
      </Row>
      <Row>
        <Col>
          <div>
            <div>Offered Players</div>
            <div class=giveminheight use:dndzone={{items:offeredPlayers, type:"Offering"}}
                 on:consider={handleDndSortOfferedPlayers} on:finalize={handleDndSortOfferedPlayers}>
                {#each offeredPlayers as player (player.id)}
                    <div class="card">
                        {player.player.name}
                    </div>
                {/each}
            </div>
        </Col>
        <Col>
          <div>
            <div>Requested Players</div>
            <div use:dndzone={{items:requestedPlayers, type:"Requesting"}}
                on:consider={handleDndSortRequestedPlayers} on:finalize={handleDndSortRequestedPlayers}>
                {#each requestedPlayers as player (player.id)}
                    <div class="card">
                        {player.player.name}
                    </div>
                {/each}
          </div>`
        </Col>
      </Row>
    </Container>
  </ModalBody>
  <ModalFooter>
    <Button color="primary" type="submit" form="makeTrade">{tradeForUnclaimedPlayers ? "Transfer Players" : "Offer Trade"}</Button>
    <Button color="secondary" on:click={toggle}>Cancel</Button>
  </ModalFooter>
</Modal>

<form
  method="POST"
  action="?/makeTrade"
  use:enhance={({ data }) => {
    console.log("in enhance correcyly")
    data.set("tradeOffer",JSON.stringify({offeredPlayers:offeredPlayers.map(playerSeason => playerSeason.id),requestedPlayers:requestedPlayers.map(playerSeason => playerSeason.id),playerDraftReceivingOffer:playerTradingWith.id}))
     // this trick means that we don't have to worry about configuring CORS server side
  }}
  id="makeTrade">
  </form>

<style>
  .board {
      height: 90vh;
  }
  .column {
        height: 50%;
        width: 100px;
        padding: 0.5em;
        margin: 1em;
        float: left;
        border: 1px solid #333333;
        /*Notice we make sure this container doesn't scroll so that the title stays on top and the dndzone inside is scrollable*/
        overflow-y: hidden;
    }
    .column-content {
        height: 100%;
        /* Notice that the scroll container needs to be the dndzone if you want dragging near the edge to trigger scrolling */
        overflow-y: scroll;
    }
    .column-title {
        margin-bottom: 1em;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .card {
        height: 15%;
        width: 100%;
        margin: 0.4em 0;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #dddddd;
        border: 1px solid #333333 ;
    }
    .giveminheight {
      min-height: 2em;
    }
</style>
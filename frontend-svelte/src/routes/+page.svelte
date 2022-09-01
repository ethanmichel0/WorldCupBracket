<script>
    import { TabContent, TabPane } from 'sveltestrap';
    /** @type {import('./$types').PageData} */
    export let data;
    console.log("test")
</script>
<h1>TESTTTT</h1>
<TabContent>
    <TabPane tabId="alpha" tab="Standings" active>
      <h2>Alpha</h2>
      <img
        alt="Alpha Flight"
        src="https://upload.wikimedia.org/wikipedia/en/4/49/Alpha_Flight_cast_picture_%28John_Byrne_era%29.gif"
      />
      
    </TabPane>
  </TabContent>




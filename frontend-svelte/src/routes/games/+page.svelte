<script>
    /** @type {import('$types').PageData} */
    export let data;
    import { onMount } from 'svelte';
    onMount(async () => {
        console.log("on mount entered!")
		const res = await data.client.send(`/app/api/games/testwebsocket`,{},"asdf");
	});
</script>


<h1>TEST PAGE!!!</h1>
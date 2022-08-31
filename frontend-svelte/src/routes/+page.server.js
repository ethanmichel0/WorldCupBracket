import { getBaseUrl } from '$lib/utils.js';
 
/** @type {import('../../.svelte-kit/types/src/routes/$types').PageServerLoad} */
export async function load({ params }) {
    console.log("test")
    const response = await fetch(`${getBaseUrl()}/api/groups`);

    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    return response.json()
}
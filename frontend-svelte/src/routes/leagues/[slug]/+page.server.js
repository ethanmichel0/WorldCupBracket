import { getBaseUrlFromServer } from '$lib/utils.js';
 
/** @type {import('./$types').PageServerLoad} */
export async function load({ params }) {
    console.log("TEST")
    const response = await fetch(`${getBaseUrlFromServer()}/api/leagues/${params.slug}/2022`);

    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = await response.json()

    return {league:responseJSON}
}
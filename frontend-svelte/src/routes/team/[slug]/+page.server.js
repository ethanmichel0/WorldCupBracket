import { getBaseUrl } from '$lib/utils.js';
 
/** @type {import('./$types').PageServerLoad} */
export async function load({ params }) {
    const response = await fetch(`${getBaseUrl()}/api/teams/${params.slug}`);

    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = await response.json()
    console.log(responseJSON.team)
    console.log("TESDFSD")

    return responseJSON.team
}
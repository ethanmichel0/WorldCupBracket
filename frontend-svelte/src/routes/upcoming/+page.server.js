import { getBaseUrl } from '$lib/utils.js';
 
/** @type {import('./$types').PageServerLoad} */
export async function load({ params }) {
    const response = await fetch(`${getBaseUrl()}/api/games`);

    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = response.json()

    return responseJSON
}
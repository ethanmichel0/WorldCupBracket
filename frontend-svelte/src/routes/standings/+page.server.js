import { getBaseUrl } from '$lib/utils.js';
 
/** @type {import('./$types').PageServerLoad} */
export async function load({ params }) {
    console.log('loading correct')
    console.log(getBaseUrl())
    const response = await fetch(`${getBaseUrl()}/api/groups`);

    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = response.json()

    return responseJSON
}
import { getBaseUrlFromServer } from '$lib/utils.js';
 
/** @type {import('./$types').PageServerLoad} */
export async function load({ params }) {
    const response = await fetch(`${getBaseUrlFromServer()}/api/teams/${params.slug}`);
    console.log("RESPONSE IS")
    console.log(response)

    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = await response.json()

    return responseJSON
}
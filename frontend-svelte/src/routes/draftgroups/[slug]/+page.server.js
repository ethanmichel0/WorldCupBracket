import { getBaseUrlFromServer } from '$lib/utils.js';
import { redirect } from '@sveltejs/kit';
 
/** @type {import('./$types').PageServerLoad} */
export async function load({ params }) {
    const response = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/${params.slug}`);

    if(response.status==401) {
        throw redirect('302',`${getBaseUrlFromServer()}/api/draftGroups`)
    }
    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = await response.json()

    return responseJSON
}
import { getBaseUrl } from '$lib/utils.js';
 
/** @type {import('./$types').PageServerLoad} */
export async function load({ params }) {
    const response = await fetch(`${getBaseUrl()}/api/standings`);
    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = await response.json();
    let groups = new Array(new Array())
    let currentGroup = 'A'
    for (let i of responseJSON) {
        if(i.group == currentGroup) groups.slice(-1)[0].push(i)
        else {
            groups.push(new Array(i))
            currentGroup = i.group
        }
    }

    console.log(groups)
    console.log("IS GROUPS")

    return groups
}
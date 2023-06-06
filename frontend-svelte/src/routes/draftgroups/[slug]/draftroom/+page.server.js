import {getBaseUrlFromServer} from '$lib/utils';

/** @type {import('./$types').PageServerLoad} */
export async function load({cookies,params,locals}) {
    console.log("TESTDRAFTROOM")
    const response = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/${params.slug}/remainingplayers`,{headers:{
        Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
    }})

    console.log("response status is:" + response.status)

    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = await response.json()

    return {
        draftRoomInfo: responseJSON,
        groupName: params.slug,
        userAuth: locals.user
    }
}
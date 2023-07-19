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

export const actions = {
	addToWatchlist: async ({request,cookies,params}) => {
        console.log("in action!")
        const formData = await request.formData();
        const playerId = formData.get("playerId")
        console.log("playerId is:" + playerId)
        console.log("group name is: " + params.slug)
        let x = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/${params.slug}/addToWatchList/${playerId}`,{method:'POST'
        ,headers:{
            Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
            "Content-Type": "application/json",
            'Accept': 'application/json'
        }})
        console.log("just added to watch list in action!")
        console.log(await x.text() + " bruh!!")
    },
    removeFromWatchlist: async ({request,cookies}) => {
        const formData = await request.formData();
        const playerId = formData.get("playerId")
        let x = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/${params.slug}/removeFromWatchList/${playerId}"`,{method:'DELETE'
        ,headers:{
            Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
            "Content-Type": "application/json",
            'Accept': 'application/json'
        }})
        console.log(x.status)
    },
    reorderWatchList: async ({request,cookies}) => {
        console.log("IN JOIN GROUP!!!")
        const formData = await request.formData();
        console.log("data to send is:" + formDataToJson(formData,[]))

        let x = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/join`,{method:'POST',body:formDataToJson(formData,[])
        ,headers:{
            Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
            "Content-Type": "application/json",
            'Accept': 'application/json'
        }})

        console.log(x.headers.get("content-type"))
        console.log(x.status)
    }
}
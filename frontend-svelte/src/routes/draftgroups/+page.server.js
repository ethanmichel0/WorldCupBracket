import { formDataToJson, getBaseUrlFromServer } from '$lib/utils';
 
export const actions = {
	createGroup: async ({request,cookies}) => {
        const formData = await request.formData();
        let allMultiSelectFields = ["leagueIds"];

        let x = await fetch(`${getBaseUrlFromServer()}/api/draftgroups`,{method:'POST',body:formDataToJson(formData,allMultiSelectFields)
        ,headers:{
            Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
            "Content-Type": "application/json",
            'Accept': 'application/json'
        }})

        console.log(x.headers.get("content-type"))
        console.log(x.status)
    },
    joinGroup: async ({request,cookies}) => {
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

/** @type {import('./$types').PageServerLoad} */
export async function load({cookies,locals}) {
    console.log("TEST INSIDE fetch")
    const response = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/`,{headers:{
        Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
        "Content-Type": "application/json",
        'Accept': 'application/json'
    }})

    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = await response.json()

    // TODO since this is a getter it may not be persisted to db or returned by api call so may have to change
    // draftComplete field

    let draftGroupsOngoing = responseJSON.filter(x => x.draftTime != - 1 && x.draftTime <= Date.now()/1000 && ! x.draftComplete)
    // ongoing draft groups will have seperate link to draftroom

    let allOtherDraftGroups = responseJSON.filter(x => ! (draftGroupsOngoing.includes(x)))

    return {
        ongoing:draftGroupsOngoing,
        otherGroups:allOtherDraftGroups,
        userAuth:locals.user
    }
}
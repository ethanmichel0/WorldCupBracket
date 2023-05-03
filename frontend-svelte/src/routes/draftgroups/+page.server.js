import { formDataToJson, getBaseUrlFromServer } from '$lib/utils';
 
export const actions = {
	default: async ({request,cookies}) => {
    const formData = await request.formData();
    formData.forEach((f,v) => console.log("field is: " + f + " and value is: " + v))
    console.log(formData.get('leagueIDs') + "is league ids!!")
    let allMultiSelectFields = ["leagueIds"];
    console.log("all multiselect = " + allMultiSelectFields)

    let x = await fetch(`${getBaseUrlFromServer()}/api/draftgroups`,{method:'POST',body:formDataToJson(formData,allMultiSelectFields)
    ,headers:{
        Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
        "Content-Type": "application/json",
        'Accept': 'application/json'
    }})

    console.log(x.headers.get("content-type"))
    console.log(x.status)
}}

/** @type {import('./$types').PageServerLoad} */
export async function load({cookies}) {
    console.log("TEST")
    const response = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/`,{headers:{
        Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
    }})

    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = await response.json()
    console.log(responseJSON)

    return {draftGroups:responseJSON}
}
import { getBaseUrlFromServer } from '$lib/utils.js';
import { redirect } from '@sveltejs/kit';
 
/** @type {import('./$types').PageServerLoad} */
export async function load({ params, locals, cookies }) {
    const response = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/${params.slug}`,
        {headers:{
            Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
            "Content-Type": "application/json",
            'Accept': 'application/json'
        }})
    console.log(response.status + "is status")
    if(response.status==401) {
        throw redirect('302',`${getBaseUrlFromServer()}/api/draftgroups`)
    }
    if (!response.ok) {
        throw new Error(`Error! status: ${response.status}`);
    }

    let responseJSON = await response.json()

    return {...responseJSON,userAuth:locals.user,groupName:params.slug}
}

export const actions = {
    setTime: async ({cookies,request,params}) => {
        console.log("in default action!")
        const data = await request.formData();
        const dateAsString = data.get("unixTimeStamp")
        console.log("unixTimeStamp is:" + dateAsString)

        const response = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/${params.slug}/draftTime?time=${dateAsString}`,{
            method: "PUT",
            headers:{
                Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`
            }
        })
        console.log(response.status + "IS RSP STATUS")
        const text = await response.text()
        console.log(text + "IS RESPONSE TXT")
    },
    makeTrade: async ({cookies,request,params}) => {
        const data = await request.formData();
        const tradeOffer = data.get("tradeOffer")

        let response = await fetch(`${getBaseUrlFromServer()}/api/draftgroups/${params.slug}/offerTrade`,
        {
        headers:{
            Cookie : `JSESSIONID=${cookies.get('JSESSIONID')}`,
            "Content-Type": "application/json",
            'Accept': 'text/html'
        },
        method: "POST",
        body: tradeOffer})
        console.log(response.status + "is response status")
        console.log(await response.text() + "is text")
    }
};
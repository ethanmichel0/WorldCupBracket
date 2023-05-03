import {over} from "stompjs"
import SockJS from "sockjs-client/dist/sockjs"
export const ssr = false;

/** @type {import('./$types').PageLoad} */
export async function load({ params}) {
    
    console.log("IN LOAD")
    console.log(document.cookie)
    console.log(document.cookie.length)
    let sockJS = new SockJS('http://localhost:6868/ws')
    const client = over(sockJS)
    client.connect({
        Cookie : document.cookie},
        onConnectedCallback,
        (error) => console.log(`There was an error connecting to websocket: ${error}`))
    function onConnectedCallback() {
        client.subscribe("/topic/greetings", function(message) {
            console.log(`message is: ${message.body}`)
        })
    }
    await delay(5000)
    return {client:client}
}

function delay(time) {
    return new Promise(resolve => setTimeout(resolve, time));
}

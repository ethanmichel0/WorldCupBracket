import { isUserLoggedIn } from "$lib/utils"
import { redirect} from "@sveltejs/kit"

export const handle = async ({ event, resolve }) => {
    // https://www.youtube.com/watch?v=K1Tya6ovVOI
	if (event.url.pathname.startsWith("/draftgroups") && ! await isUserLoggedIn(event)) {
        console.log("throwing redirect")
        throw redirect(303,'/login')
    }

    if (event.locals.user) {
        console.log("checking if email is set in event.locals:" + event.locals.user.email)
    }
    const response = await resolve(event)
	return response
}
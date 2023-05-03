import { isUserLoggedIn } from "$lib/utils"
import { redirect} from "@sveltejs/kit"

export const handle = async ({ event, resolve }) => {
    // https://www.youtube.com/watch?v=K1Tya6ovVOI
	if (event.url.pathname.startsWith("/draftgroups") && ! await isUserLoggedIn(event.cookies)) {
        throw redirect(303,'/login')
    }

    const response = await resolve(event)
	return response
}
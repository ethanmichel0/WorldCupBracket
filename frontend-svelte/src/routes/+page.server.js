import { redirect } from '@sveltejs/kit';

/** @type {import('./$types').LayoutServerLoad} */
export function load({ _ }) {
		throw redirect(307, '/standings');
}
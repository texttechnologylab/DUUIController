import type { Cookies } from '@sveltejs/kit'
import { equals } from './duui/utils/text'

/**
 * Displays a notification message set by the message parameter.
 *
 * @param url The url to redirect to after logging in.
 * @param message The message do display as the notification.
 * @returns a url to the login page wiht the origin as a search parameter.
 */
export const handleLoginRedirect = (
	url: URL,
	message: string = 'You must be logged in to access this ressource.'
) => {
	return `/account/login?redirectTo=${url.pathname + url.search}&message=${message}`
}

/**
 * A utility function that checks if a given generic like the word 'Any' is present in an array
 * of filters. If the generic is present, returns an array that only contains the generic.
 *
 * @param filters An array of strings that represent valid filters (return true when applied).
 * @param generic The string that represents always returns true when filtered.
 * @returns the array if the generic value is not present or an array containing only the generic.
 */
export const getFilterOrGeneric = (filters: string[], generic: string = 'Any') => {
	const last: string | undefined = filters.at(-1)

	if (equals(last || '', generic) || filters.length === 0) {
		return [generic]
	} else {
		return filters.filter((item) => !equals(item, generic))
	}
}

export const createSession = (cookies: Cookies, sessionId: string) => {
	cookies.set('session', sessionId, {
		path: '/',
		sameSite: 'lax',
		httpOnly: true,
		secure: process.env.NODE_ENV === 'production',
		maxAge: 60 * 60 * 24 * 30
	})
}

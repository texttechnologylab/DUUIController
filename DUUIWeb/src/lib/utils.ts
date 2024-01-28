/**
 * Displays a notification
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

const urlRegex =
	'https?://(www.)?[-a-zA-Z0-9@:%._+~#=]{1,256}.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)'

const regex = new RegExp(urlRegex)

export const validateURL = (url: string) => {
	if (url.match(regex)) return true
	return false
}

export const handleLoginRedirect = (
	url: URL,
	message: string = 'You must be logged in to access this ressource.'
) => {
	return `/user/login?redirectTo=${url.pathname + url.search}&message=${message}`
}

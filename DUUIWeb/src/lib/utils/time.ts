export const formatSeconds = (durationInSeconds: number) => {
	const hours = Math.floor(durationInSeconds / 3600)
	const minutes = Math.floor((durationInSeconds % 3600) / 60)
	const seconds = Math.floor(durationInSeconds % 60)

	const hoursText = hours > 0 ? `${hours}h` : ''
	const minutesText = minutes > 0 ? `${minutes}m` : ''
	const secondsText = seconds > 0 && hours <= 0 ? `${seconds}s` : ''

	const result = [hoursText, minutesText, secondsText].filter(Boolean).join(' ')

	return result || '0s'
}

export const getDuration = (start: number | undefined, end: number | undefined) => {
	if (!start) return 0

	if (!end) return formatSeconds(Math.ceil((Date.now() - start) / 1000))

	return formatSeconds(Math.ceil((end - start) / 1000))
}

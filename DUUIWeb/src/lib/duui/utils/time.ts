export const formatMilliseconds = (durationInMilliseconds: number) => {
	const hours = Math.floor(durationInMilliseconds / 3600000)
	const minutes = Math.floor((durationInMilliseconds % 3600000) / 60000)
	const seconds = Math.floor((durationInMilliseconds % 60000) / 1000)
	const milliseconds = durationInMilliseconds % 1000

	const hoursText = hours > 0 ? `${hours}h` : ''
	const minutesText = minutes > 0 ? `${minutes}m` : ''
	const secondsText = seconds > 0 && hours <= 0 ? `${seconds}s` : ''

	let result = [hoursText, minutesText, secondsText].filter(Boolean).join(' ')

	if (durationInMilliseconds < 1000) {
		result = `${result} ${milliseconds}ms`
	}

	return result || '0s'
}

export const getDuration = (start: number | undefined, end: number | undefined) => {
	if (!start) return formatMilliseconds(0)

	if (!end) return formatMilliseconds(Math.ceil(Date.now() - start))

	return formatMilliseconds(Math.ceil(end - start))
}

import type { DrawerSettings } from '@skeletonlabs/skeleton'

// Central position to change drawer settings for the componentdrawer.
export const componentDrawerSettings: DrawerSettings = {
	width: 'w-full lg:w-1/2 2xl:w-[40%]',
	position: 'right',
	rounded: 'rounded-none',
	border: 'border-l border-color'
}

export const COLORS = { blue: '#006c98', red: '#f87060', purple: '#731dd8', green: '#44d268' }

/**
 * Get the current theme's primary color or a complementary color with less opacity.
 * @param alternate a flag to retrieve a complementary color instead.
 * @returns a color represented by a hex string.
 */
export const getColor = (alternate: boolean = false) => {
	let color = COLORS.blue

	try {
		let THEME = document.body.dataset.theme || 'blue'
		color = COLORS[THEME.replace('theme-', '')]
		if (alternate) {
			return getComplementaryColor(color)
		}
	} catch (err) {}

	return color
}

/**
 * Get the complementary color for a given input color in the hex format.	
 *
 * @param color the color to convert in hex format
 * @returns the complementary color to the input color.
 */
export const getComplementaryColor = (color: string) => {
	var c = color.slice(1),
		i = parseInt(c, 16),
		v = ((1 << (4 * c.length)) - 1 - i).toString(16)

	while (v.length < c.length) {
		v = '0' + v
	}
	return '#' + v
}

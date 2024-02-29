import type { DrawerSettings } from '@skeletonlabs/skeleton'

// Central position to change drawer settings.
export const componentDrawerSettings: DrawerSettings = {
	width: 'w-full lg:w-1/2 2xl:w-[40%]',
	position: 'right',
	rounded: 'rounded-none',
	border: 'border-l border-color'
}

export const COLORS = { blue: '#006c98', red: '#f87060', purple: '#731dd8', green: '#44d268' }

export const getColor = (alternate: boolean = false) => {
	let color = COLORS.blue

	try {
		let THEME = document.body.dataset.theme || 'blue'
		color = COLORS[THEME.replace('theme-', '')]
		if (alternate) {
			return '#696969'
		}
	} catch (err) {}

	return color
}

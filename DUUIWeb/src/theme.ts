import type { CustomThemeConfig } from '@skeletonlabs/tw-plugin'

export const Theme: CustomThemeConfig = {
	name: 'theme',
	properties: {
		// =~= Theme Properties =~=
		'--theme-font-family-base': `system-ui`,
		'--theme-font-family-heading': `system-ui`,
		'--theme-font-color-base': '0 0 0',
		'--theme-font-color-dark': '255 255 255',
		'--theme-rounded-base': '6px',
		'--theme-rounded-container': '6px',
		'--theme-border-base': '1px',
		// =~= Theme On-X Colors =~=
		'--on-primary': '255 255 255',
		'--on-secondary': '0 0 0',
		'--on-tertiary': '0 0 0',
		'--on-success': '0 0 0',
		'--on-warning': '0 0 0',
		'--on-error': '255 255 255',
		'--on-surface': '255 255 255',
		// =~= Theme Colors  =~=
		// primary | #006c98
		'--color-primary-50': '217 233 240', // #d9e9f0
		'--color-primary-100': '204 226 234', // #cce2ea
		'--color-primary-200': '191 218 229', // #bfdae5
		'--color-primary-300': '153 196 214', // #99c4d6
		'--color-primary-400': '77 152 183', // #4d98b7
		'--color-primary-500': '0 108 152', // #006c98
		'--color-primary-600': '0 97 137', // #006189
		'--color-primary-700': '0 81 114', // #005172
		'--color-primary-800': '0 65 91', // #00415b
		'--color-primary-900': '0 53 74', // #00354a
		// secondary | #00bcff
		'--color-secondary-50': '217 245 255', // #d9f5ff
		'--color-secondary-100': '204 242 255', // #ccf2ff
		'--color-secondary-200': '191 238 255', // #bfeeff
		'--color-secondary-300': '153 228 255', // #99e4ff
		'--color-secondary-400': '77 208 255', // #4dd0ff
		'--color-secondary-500': '0 188 255', // #00bcff
		'--color-secondary-600': '0 169 230', // #00a9e6
		'--color-secondary-700': '0 141 191', // #008dbf
		'--color-secondary-800': '0 113 153', // #007199
		'--color-secondary-900': '0 92 125', // #005c7d
		// tertiary | #e99b26
		'--color-tertiary-50': '252 240 222', // #fcf0de
		'--color-tertiary-100': '251 235 212', // #fbebd4
		'--color-tertiary-200': '250 230 201', // #fae6c9
		'--color-tertiary-300': '246 215 168', // #f6d7a8
		'--color-tertiary-400': '240 185 103', // #f0b967
		'--color-tertiary-500': '233 155 38', // #e99b26
		'--color-tertiary-600': '210 140 34', // #d28c22
		'--color-tertiary-700': '175 116 29', // #af741d
		'--color-tertiary-800': '140 93 23', // #8c5d17
		'--color-tertiary-900': '114 76 19', // #724c13
		// success | #5cb85c
		'--color-success-50': '231 244 231', // #e7f4e7
		'--color-success-100': '222 241 222', // #def1de
		'--color-success-200': '214 237 214', // #d6edd6
		'--color-success-300': '190 227 190', // #bee3be
		'--color-success-400': '141 205 141', // #8dcd8d
		'--color-success-500': '92 184 92', // #5cb85c
		'--color-success-600': '83 166 83', // #53a653
		'--color-success-700': '69 138 69', // #458a45
		'--color-success-800': '55 110 55', // #376e37
		'--color-success-900': '45 90 45', // #2d5a2d
		// warning | #f0ad4e
		'--color-warning-50': '253 243 228', // #fdf3e4
		'--color-warning-100': '252 239 220', // #fcefdc
		'--color-warning-200': '251 235 211', // #fbebd3
		'--color-warning-300': '249 222 184', // #f9deb8
		'--color-warning-400': '245 198 131', // #f5c683
		'--color-warning-500': '240 173 78', // #f0ad4e
		'--color-warning-600': '216 156 70', // #d89c46
		'--color-warning-700': '180 130 59', // #b4823b
		'--color-warning-800': '144 104 47', // #90682f
		'--color-warning-900': '118 85 38', // #765526
		// error | #d9534f
		'--color-error-50': '249 229 229', // #f9e5e5
		'--color-error-100': '247 221 220', // #f7dddc
		'--color-error-200': '246 212 211', // #f6d4d3
		'--color-error-300': '240 186 185', // #f0bab9
		'--color-error-400': '228 135 132', // #e48784
		'--color-error-500': '217 83 79', // #d9534f
		'--color-error-600': '195 75 71', // #c34b47
		'--color-error-700': '163 62 59', // #a33e3b
		'--color-error-800': '130 50 47', // #82322f
		'--color-error-900': '106 41 39', // #6a2927
		// surface | #292929
		'--color-surface-50': '255 255 255', // #ffffff
		'--color-surface-100': '244 244 244', // #f2f2f2
		'--color-surface-200': '202 202 202', // #cacaca
		'--color-surface-300': '169 169 169', // #a9a9a9
		'--color-surface-400': '105 105 105', // #696969
		'--color-surface-500': '41 41 41', // #292929
		'--color-surface-600': '37 37 37', // #252525
		'--color-surface-700': '31 31 31', // #1f1f1f
		'--color-surface-800': '25 25 25', // #191919
		'--color-surface-900': '20 20 20' // #141414
	}
}

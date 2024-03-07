import type { CustomThemeConfig } from '@skeletonlabs/tw-plugin'

// Visit https://www.skeleton.dev/docs/themes to create a custom theme

export const ThemePurple: CustomThemeConfig = {
	name: 'theme-purple',
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
		'--on-success': '255 255 255',
		'--on-warning': '0 0 0',
		'--on-error': '255 255 255',
		'--on-surface': '255 255 255',
		// =~= Theme Colors  =~=
		// primary | #731dd8
		'--color-primary-50': '234 221 249', // #eaddf9
		'--color-primary-100': '227 210 247', // #e3d2f7
		'--color-primary-200': '220 199 245', // #dcc7f5
		'--color-primary-300': '199 165 239', // #c7a5ef
		'--color-primary-400': '157 97 228', // #9d61e4
		'--color-primary-500': '115 29 216', // #731dd8
		'--color-primary-600': '104 26 194', // #681ac2
		'--color-primary-700': '86 22 162', // #5616a2
		'--color-primary-800': '69 17 130', // #451182
		'--color-primary-900': '56 14 106', // #380e6a
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
		// error | #f95959
		'--color-error-50': '254 230 230', // #fee6e6
		'--color-error-100': '254 222 222', // #fedede
		'--color-error-200': '254 214 214', // #fed6d6
		'--color-error-300': '253 189 189', // #fdbdbd
		'--color-error-400': '251 139 139', // #fb8b8b
		'--color-error-500': '249 89 89', // #f95959
		'--color-error-600': '224 80 80', // #e05050
		'--color-error-700': '187 67 67', // #bb4343
		'--color-error-800': '149 53 53', // #953535
		'--color-error-900': '122 44 44', // #7a2c2c
		// surface | #242735
		'--color-surface-50': '242 242 242', // #f2f2f2
		'--color-surface-100': '231 231 231', // #e7e7e7
		'--color-surface-200': '200 201 205', // #c8c9cd
		'--color-surface-300': '167 169 174', // #a7a9ae
		'--color-surface-400': '102 104 114', // #666872
		'--color-surface-500': '36 39 53', // #242735
		'--color-surface-600': '32 35 48', // #202330
		'--color-surface-700': '27 29 40', // #1b1d28
		'--color-surface-800': '22 23 32', // #161720
		'--color-surface-900': '18 19 26' // #12131a
	}
}

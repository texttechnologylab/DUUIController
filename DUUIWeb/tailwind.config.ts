import { skeleton } from '@skeletonlabs/tw-plugin'
import forms from '@tailwindcss/forms'
import { join } from 'path'
import type { Config } from 'tailwindcss'
import { ThemeBlue } from './static/themes/theme-blue'
import { ThemeGreen } from './static/themes/theme-green'
import { ThemePurple } from './static/themes/theme-purple'
import { ThemeRed } from './static/themes/theme-red'

export default {
	darkMode: 'class',
	content: [
		'./src/**/*.{html,js,svelte,ts}',
		join(require.resolve('@skeletonlabs/skeleton'), '../**/*.{html,js,svelte,ts}')
	],
	theme: {
		extend: {
			animation: {
				'spin-slow': 'spin 2s linear infinite',
				hourglass: 'turn 4s linear infinite',
				'ping-once': 'ping 1s ease-in-out forwards'
			},
			keyframes: {
				turn: {
					'0%': { transform: 'rotate( 0.0deg)' },
					'10%': { transform: 'rotate(90.0deg)' },
					'20%': { transform: 'rotate(180.0deg)' },
					'30%': { transform: 'rotate(180.0deg)' },
					'40%': { transform: 'rotate(180.0deg)' },
					'50%': { transform: 'rotate(180.0deg)' },
					'60%': { transform: 'rotate(270.0deg)' },
					'70%': { transform: 'rotate(360.0deg)' },
					'80%': { transform: 'rotate(360.0deg)' },
					'90%': { transform: 'rotate(360.0deg)' },
					'100%': { transform: 'rotate(360.0deg)' }
				}
			},
			gridTemplateColumns: {
				'auto-fit-420': 'repeat(auto-fit, minmax(min(100%, 420px), 1fr))'
			},
			transitionProperty: {
				width: 'width'
			}
		}
	},
	plugins: [
		forms,
		skeleton({
			themes: {
				custom: [ThemeBlue, ThemeRed, ThemePurple, ThemeGreen],
				preset: [
					{
						name: 'rocket',
						enhancements: true
					},
					'modern',
					'crimson',
					'gold-nouveau',
					'hamlindigo'
				]
			}
		})
	]
} satisfies Config

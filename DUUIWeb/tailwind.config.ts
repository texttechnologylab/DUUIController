import { join } from 'path'
import type { Config } from 'tailwindcss'
import { skeleton } from '@skeletonlabs/tw-plugin'
import forms from '@tailwindcss/forms'
import { Theme } from './custom-theme'

export default {
	darkMode: 'class',
	content: [
		'./src/**/*.{html,js,svelte,ts}',
		join(require.resolve('@skeletonlabs/skeleton'), '../**/*.{html,js,svelte,ts}')
	],
	theme: {
		extend: {
			animation: {
				'spin-slow': 'spin 2s linear infinite'
			}
		}
	},
	plugins: [
		forms,
		skeleton({
			themes: {
				custom: [
					Theme
				],
				preset: [
					{
						name: 'rocket',
						enhancements: true
					}
				]
			}
		})
	]
} satisfies Config

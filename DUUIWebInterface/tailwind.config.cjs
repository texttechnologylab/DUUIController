/** @type {import('tailwindcss').Config}*/
const config = {
	content: ['./src/**/*.{html,js,svelte,ts}'],

	theme: {
		fontFamily: {
			'display': ['Ubuntu'],
			'body': ['"Ubuntu"']
		},
		extend: {}
	},

	plugins: []
};

module.exports = config;

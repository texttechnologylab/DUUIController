/** @type {import('tailwindcss').Config}*/
const config = {
	content: ['./src/**/*.{html,js,svelte,ts}'],

	theme: {
		fontFamily: {
			'display': ['Ubuntu'],
			'body': ['"Ubuntu"']
		},
		extend: {
			gridTemplateColumns: {
				'pipelineItem': 'auto repeat(2, 1fr)'
			}
		}
	},

	plugins: []
};

module.exports = config;

import { getColor } from '$lib/config'
import { toTitleCase } from '$lib/duui/utils/text'

export const getPlotOptions = (feedback: FeedbackResult[], title: string, darkmode: boolean) => {
	if (feedback.length === 0) return {}

	const gridSettings = {
		borderColor: darkmode ? '#e7e7e720' : '#29292920',
		row: {
			colors: [darkmode ? '#292929' : '#f3f3f3', 'transparent'], // takes an array which will be repeated on columns
			opacity: 0.5
		}
	}
	let keys = [
		'programming',
		'nlp',
		'duuiRating',
		'java',
		'python',
		'requirements',
		'frustration',
		'ease',
		'correction'
	]

	const averages: number[] = []
	const count = feedback.length

	for (let key of keys) {
		let value = 0
		for (let item of feedback) {
			value += item[key]
		}

		averages.push(+(value / count).toFixed(2))
	}

	const labelColors = Array(keys.length).fill(darkmode ? 'white' : 'black')

	return {
		series: [
			{
				name: 'Score',
				data: averages,
				color: getColor()
			}
		],
		chart: {
			height: 450,
			type: 'radar',
		},
		plotOptions: {
			bar: {
				borderRadius: 8
			},
			radar: {
				polygons: {
					strokeColors: darkmode ? '#e7e7e720' : '#29292920', // Set the color of the polygon grid lines
					connectorColors: darkmode ? '#e7e7e720' : '#29292920', // Set the color of the connector grid lines
					fill: {
						colors: undefined // [darkmode ? '#29292920' : '#f2f2f220', 'transparent'] // Set the colors of the polygon
					}
				}
			}
		},
		title: {
			text: title,
			align: 'center',
			style: {
				fontSize: '20px',
				color: darkmode ? 'white' : 'black'
			}
		},
		xaxis: {
			categories: keys.map((key) => toTitleCase(key)),
			position: 'bottom',
			labels: {
				style: {
					colors: labelColors
				}
			},
			axisBorder: {
				show: false
			},
			axisTicks: {
				show: false
			},
			crosshairs: {
				fill: {
					type: 'gradient',
					gradient: {
						colorFrom: '#D8E3F0',
						colorTo: '#BED1E6',
						stops: [0, 100],
						opacityFrom: 0.4,
						opacityTo: 0.5
					}
				}
			}
		},
		yaxis: {
			axisBorder: {
				show: false
			},
			axisTicks: {
				show: false
			},
			labels: {
				show: true,
				style: {
					colors: darkmode ? 'white' : 'black'
				}
			}
		}
	}
}

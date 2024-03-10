import { getColor } from '$lib/config'
import { toTitleCase } from '$lib/duui/utils/text'

export const getPlotOptions = (
	keys: string[],
	values: number[],
	title: string,
	darkmode: boolean
) => {
	if (keys.length === 0) return {}

	const labelColors = Array(keys.length).fill(darkmode ? 'white' : 'black')

	return {
		series: [
			{
				name: 'Score',
				data: values,
				color: getColor()
			}
		],
		chart: {
			height: 450,
			type: 'radar',
			toolbar: {
				show: false
			}
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
				formatter: function (value: number) {
					return value.toFixed(0)
				},
				show: true,
				style: {
					colors: darkmode ? 'white' : 'black'
				}
			}
		}
	}
}

export const getUsersPlotOptions = (
	counters: {
		duui: { experienced: number; inexperienced: number }
		programming: { experienced: number; inexperienced: number }
	},
	darkmode: boolean
) => {
	const gridSettings = {
		borderColor: darkmode ? '#e7e7e720' : '#29292920',
		row: {
			colors: [darkmode ? '#292929' : '#f3f3f3', 'transparent'], // takes an array which will be repeated on columns
			opacity: 0.5
		}
	}

	return {
		series: [
			{
				name: 'Experienced',
				data: [counters.duui.experienced, counters.programming.experienced],
				color: getColor()
			},
			{
				name: 'Inexperienced',
				data: [counters.duui.inexperienced, counters.programming.inexperienced],
				color: getColor(true)
			}
		],
		chart: {
			height: 450,
			type: 'bar',
			toolbar: {
				show: false
			}
		},
		plotOptions: {
			bar: {
				borderRadius: 8
			}
		},
		legend: {
			labels: {
				colors: darkmode ? 'white' : 'black'
			}
		},
		grid: gridSettings,
		xaxis: {
			categories: ['DUUI', 'Programming'],
			position: 'top',
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
			},
			labels: {
				show: true,
				style: {
					colors: darkmode ? 'white' : 'black'
				}
			}
		},
		yaxis: {
			axisBorder: {
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

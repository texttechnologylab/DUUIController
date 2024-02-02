import type { DUUIPipeline } from '$lib/duui/pipeline'
import { equals } from '$lib/duui/utils/text'

const gridSettings = {
	borderColor: '#e7e7e7',
	row: {
		colors: ['#f3f3f3', 'transparent'], // takes an array which will be repeated on columns
		opacity: 0.5
	}
}

const theme = {
	palette: 'palette2', // upto palette10
	monochrome: {
		enabled: false
	}
}

export const getStatusPlotOptions = (pipeline: DUUIPipeline) => {
	if (!pipeline.statistics) return {}

	return {
		series: [
			{
				name: 'Status',
				data: pipeline.statistics.status.map((s) => s.count)
			}
		],
		chart: {
			height: 350,
			type: 'bar'
		},
		plotOptions: {
			bar: {
				borderRadius: 8
			}
		},

		xaxis: {
			categories: pipeline.statistics.status.map((s) => s._id),
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
			}
		},
		grid: gridSettings,
		yaxis: {
			axisBorder: {
				show: false
			},
			axisTicks: {
				show: false
			},
			labels: {
				show: true
			}
		},
		title: {
			text: 'Status of executed processes',
			align: 'center',
			style: {
				color: 'black',
				fontSize: '20px'
			}
		},
		theme: theme
	}
}

export const getErrorsPlotOptions = (pipeline: DUUIPipeline) => {
	if (!pipeline.statistics) return {}

	const x = pipeline.statistics ? pipeline.statistics.errors.map((s) => s._id) : []
	const y = pipeline.statistics ? pipeline.statistics.errors.map((s) => s.count) : []

	return {
		series: [
			{
				name: 'Error',
				data: y,
				color: '#f95959'
			}
		],
		chart: {
			height: 350,
			type: 'bar'
		},
		plotOptions: {
			bar: {
				borderRadius: 8
			}
		},
		grid: gridSettings,
		xaxis: {
			categories: x,
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
				show: true
			}
		},
		title: {
			text: 'Errors of executed processes',
			align: 'center',
			style: {
				color: 'black',
				fontSize: '20px'
			}
		},
		theme: theme
	}
}

export const getIOPlotOptions = (pipeline: DUUIPipeline) => {
	if (!pipeline.statistics) return {}

	const labels: string[] = ['File', 'Minio', 'None', 'Text', 'Dropbox']
	const input: number[] = [0, 0, 0, 0, 0]
	const output: number[] = [0, 0, 0, 0, 0]

	for (let index = 0; index < labels.length; index++) {
		const label = labels[index]
		for (let item of pipeline.statistics.input) {
			if (equals(item._id, label)) {
				input[index] = item.count
			}
		}

		for (let item of pipeline.statistics.output) {
			if (equals(item._id, label)) {
				output[index] = item.count
			}
		}
	}

	return {
		series: [
			{
				name: 'Input',
				data: input
			},
			{
				name: 'Output',
				data: output
			}
		],
		chart: {
			height: 350,
			type: 'bar'
		},
		plotOptions: {
			bar: {
				borderRadius: 8
			}
		},
		grid: gridSettings,
		xaxis: {
			categories: labels,
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
				show: true
			}
		},
		title: {
			text: 'Input and Output',
			align: 'center',
			style: {
				color: 'black',
				fontSize: '20px'
			}
		},
		theme: theme
	}
}

export const getUsagePlotOptions = (pipeline: DUUIPipeline) => {
	if (!pipeline.statistics) return {}
	const year = new Date().getFullYear()

	const xLabels: string[] = [
		`Jan ${year}`,
		`Feb ${year}`,
		`Mar ${year}`,
		`Apr ${year}`,
		`May ${year}`,
		`Jun ${year}`,
		`Jul ${year}`,
		`Aug ${year}`,
		`Sep ${year}`,
		`Oct ${year}`,
		`Nov ${year}`,
		`Dec ${year}`
	]

	let yValues = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
	for (let index = 0; index < xLabels.length; index++) {
		for (let item of pipeline.statistics.usage) {
			if (item._id.year === year && item._id.month === index) {
				yValues[index] = item.count
			}
		}
	}

	return {
		series: [
			{
				name: '# Processes',
				data: yValues
			}
		],
		plotOptions: {
			bar: {
				borderRadius: 8
			}
		},
		chart: {
			height: 350,
			type: 'bar',

			dropShadow: {
				enabled: true,
				color: '#000',
				top: 18,
				left: 7,
				blur: 10,
				opacity: 0.2
			},
			toolbar: {
				show: true,
				tools: {
					selection: true,
					zoom: false,
					zoomin: false,
					zoomout: false,
					pan: false,
					reset: false
				}
			}
		},
		dataLabels: {
			enabled: true
		},
		title: {
			text: 'Usage per month',
			align: 'center',
			style: {
				color: 'black',
				fontSize: '20px'
			}
		},
		theme: theme,
		grid: gridSettings,
		xaxis: {
			categories: xLabels
		},
		yaxis: {
			title: {
				text: '# Processes'
			}
		},
		tooltip: {
			show: false
		}
	}
}

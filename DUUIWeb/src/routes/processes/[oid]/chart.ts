import type { DUUIDocument } from '$lib/duui/io'
import type { DUUIEvent } from '$lib/duui/monitor'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import { getDuration } from '$lib/duui/utils/time'

const theme = {
	palette: 'palette1', // upto palette10
	monochrome: {
		enabled: false
	}
}

export const getAnnotationsPlotOptions = (annotations: Map<string, number>, darkmode: boolean) => {
	if (!annotations) return {}

	return {
		series: [
			{
				data: Array.from(annotations.entries()).map((entry) => {
					return {
						x: entry[0].split('.').slice(-1),
						y: entry[1]
					}
				})
			}
		],
		legend: {
			show: false
		},
		chart: {
			height: 350,
			type: 'treemap'
		},
		plotOptions: {
			treemap: {
				useFillColorAsStroke: true
			}
		},
		theme: theme
	}
}

export const getTimelinePlotOptions = (
	process: DUUIProcess,
	pipeline: DUUIPipeline,
	document: DUUIDocument,
	darkmode: boolean
) => {
	let steps: { x: string; y: number[] }[] = [
		{
			x: 'Setup',
			y: [process.started_at, process.started_at + document.duration_wait]
		}
	]

	let start = document.events.find((event) =>
		event.event.message.includes('Starting to process')
	)?.timestamp

	if (start) {
		steps.push({
			x: 'Waiting',
			y: [process.started_at + document.duration_wait, start]
		})
	}

	const gridSettings = {
		borderColor: darkmode ? '#e7e7e720' : '#29292920',
		row: {
			colors: [darkmode ? '#292929' : '#f3f3f3', 'transparent'], // takes an array which will be repeated on columns
			opacity: 0.5
		}
	}

	const components = pipeline.components.map((c) => c.name)
	let index: number = 0
	for (let component of components) {
		let start: DUUIEvent | undefined = document.events.find((event) =>
			event.event.message.includes(`is being processed by component ${component}`)
		)

		let end: DUUIEvent | undefined = document.events.find((event) =>
			event.event.message.includes(`has been processed by component ${component}`)
		)

		if (start) {
			index += 1
			if (end) {
				steps.push({ x: component + ' (' + index + ')', y: [start.timestamp, end.timestamp] })
			} else {
				steps.push({
					x: component + ' (' + index + ')',
					y: [start.timestamp, process.finished_at || new Date().getTime()]
				})
			}
		}
	}

	return {
		series: [
			{
				data: steps,
			}
		],
		chart: {
			height: 300 + pipeline.components.length * 50,
			type: 'rangeBar',
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

		plotOptions: {
			bar: {
				horizontal: true
			}
		},
		grid: gridSettings,
		xaxis: {
			labels: {
				formatter: (timestamp) => '+' + getDuration(process.started_at, timestamp),
				show: true,
				style: {
					colors: darkmode ? 'white' : 'black'
				}
			},
			axisBorder: {
				color: darkmode ? '#e7e7e720' : '#29292920'
			},
			axisTicks: {
				show: false
			}
		},
		yaxis: {
			labels: {
				show: true,
				style: {
					colors: darkmode ? 'white' : 'black',
				},
			}
		},
		tooltip: {
			custom: function ({ series, seriesIndex, dataPointIndex, w }) {
				var data = w.globals.initialSeries[seriesIndex].data[dataPointIndex]
				// You can customize the tooltip content here
				return (
					'<div class="p-2 bg-surface-50 bordered-soft dimmed flex flex-col gap-1">' +
					'<p class="font-bold text-surface-900">' +
					data.x +
					' </p>' +
					'<p class="text-surface-600">' +
					getDuration(data.y[0], data.y[1]) +
					' </p>' +
					'</div>'
				)
			}
		},
		theme: theme
	}
}

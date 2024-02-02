import type { DUUIDocument } from '$lib/duui/io'
import type { DUUIEvent } from '$lib/duui/monitor'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import { getDuration } from '$lib/duui/utils/time'

const theme = {
	palette: 'palette2', // upto palette10
	monochrome: {
		enabled: false
	}
}

export const getAnnotationsPlotOptions = (annotations: Map<string, number>) => {
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
		title: {
			text: 'Annotations',
			style: {
				fontSize: '20px',
				color: 'black'
			}
		},
		theme: theme
	}
}

export const getTimelinePlotOptions = (
	process: DUUIProcess,
	pipeline: DUUIPipeline,
	document: DUUIDocument
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

	const components = pipeline.components.map((c) => c.name)
	for (let component of components) {
		let start: DUUIEvent | undefined = document.events.find((event) =>
			event.event.message.includes(`is being processed by component ${component}`)
		)

		let end: DUUIEvent | undefined = document.events.find((event) =>
			event.event.message.includes(`has been processed by component ${component}`)
		)

		if (start) {
			if (end) {
				steps.push({ x: component, y: [start.timestamp, end.timestamp] })
			} else {
				steps.push({
					x: component,
					y: [start.timestamp, process.finished_at || new Date().getTime()]
				})
			}
		}
	}

	return {
		series: [
			{
				data: steps
			}
		],
		chart: {
			height: 350,
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
		title: {
			text: 'Timeline',
			style: {
				fontSize: '20px',
				color: 'black dark:white'
			}
		},
		xaxis: {
			labels: {
				formatter: (timestamp) => '+' + getDuration(process.started_at, timestamp)
			}
		},
		tooltip: {
			custom: function ({ series, seriesIndex, dataPointIndex, w }) {
				var data = w.globals.initialSeries[seriesIndex].data[dataPointIndex]
				// You can customize the tooltip content here
				return (
					'<div class="p-2 bg-white bordered-soft dimmed flex flex-col gap-1">' +
					'<p class="font-bold">' +
					data.x +
					' </p>' +
					'<p>' +
					getDuration(data.y[0], data.y[1]) +
					' </p>' +
					'</div>'
				)
			}
		},
		theme: theme
	}
}

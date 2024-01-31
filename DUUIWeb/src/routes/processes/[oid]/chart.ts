import type { DUUIDocument } from '$lib/duui/io'

const gridSettings = {
	borderColor: '#e7e7e7',
	row: {
		colors: ['#f3f3f3', 'transparent'], // takes an array which will be repeated on columns
		opacity: 0.5
	}
}

export const getAnnotationsPlotOptions = (document: DUUIDocument) => {
	if (!document.annotations) return {}
	
	const annotations: Map<string, number> = new Map(Object.entries(document.annotations))


	return {
		series: [
			{
				name: 'Count',
				data: Array.from(annotations.values()),
				color: '#006c98'
			}
		],
		chart: {
			height: 600,
			type: 'line'
		},
		plotOptions: {
			bar: {
				borderRadius: 8
			},
			dataLabels: {
				position: 'top' // top, center, bottom
			}
		},
		dataLabels: {
			enabled: true,
			style: {
				colors: ['#304758']
			}
		},
		xaxis: {
			categories: Array.from(annotations.keys()).map((key) => key.split('.').at(-1)),
			position: 'top',
			axisBorder: {
				show: false
			},
			axisTicks: {
				show: false
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
			text: 'Annotations in Document',
			align: 'center'
		}
	}
}

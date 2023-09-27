import { faCopy, faUsers, faCircleNodes } from '@fortawesome/free-solid-svg-icons'
import type { PageLoad } from './$types'

export const load: PageLoad = () => {
	return {
		sections: [
			{
				title: 'Reproducible',
				content:
					'Each pipeline component is fully serializable and annotates each processed document. All performed annotations, including analysis engines, models and settings can be fully reconstructed.',
				icon: faCopy
			},
			{
				title: 'Accessible',
				content:
					'DUUI is a lightweight framework for running NLP routines. No extensive knowledge about computer science and programming is required.',
				icon: faUsers
			},
			{
				title: 'Scalable',
				content:
					'DUUI guarantees horizontal and vertical via a native Docker Swarm implementation. Docker enables machine-specific resource management. Built to work with Docker images distributed within the Docker Swarm network.',
				icon: faCircleNodes
			}
		]
	}
}

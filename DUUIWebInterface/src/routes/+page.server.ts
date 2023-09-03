import { dev } from '$app/environment';
import type { PageServerLoad } from './$types';

// we don't need any JS on this page, though we'll load
// it in dev so that we get hot module replacement
export const csr = dev;

// since there's no dynamic data here, we can prerender
// it so that it gets served as a static asset in production
export const prerender = true;

export const load: PageServerLoad = () => {
	return {
		sections: [
			{
				title: 'Scalable',
				content:
					'DUUI guarantees horizontal and vertical via a native Docker Swarm implementation. Docker enables machine-specific resource management. Built to work with Docker images distributed within the Docker Swarm network.',
				image: 'share-nodes-solid'
			},
			{
				title: 'Accessible',
				content:
					'DUUI takes care of the complications NLP-Tools bring. Build your pipeline and we take care of the rest.',
				image: 'user-group-solid'
			},
			{
				title: 'Reproducible',
				content:
					'NLP-Pipelines and their annotations created with DUUI are fully reproducible.',
				image: 'file-copy-solid'
			}
		]
	};
};

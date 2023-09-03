import { dev } from '$app/environment';
import { server_address } from '$lib/api/config';
import { getPipelines } from '$lib/api/pipeline';
import type { PageLoad } from './$types';

// we don't need any JS on this page, though we'll load
// it in dev so that we get hot module replacement
export const csr = dev;

// since there's no dynamic data here, we can prerender
// it so that it gets served as a static asset in production
export const prerender = true;

export const load: PageLoad = async () => {
	const loadPipelines = async () => {
		const result = await fetch(server_address + '/pipelines', {
			method: 'GET',
			mode: 'cors'
		});
		return await result.json();
	} 

	return {
		pipelines: loadPipelines()
	};
};

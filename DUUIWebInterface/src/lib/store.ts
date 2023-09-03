import { writable, type Writable } from 'svelte/store';
import { blankPipeline, dummyPipeline, type DUUIPipeline } from './data';

export const pipelineStore: Writable<DUUIPipeline[]> = writable([dummyPipeline]);
export const currentPipelineStore: Writable<DUUIPipeline> = writable(blankPipeline());
export const pipelineFilterStore: Writable<string[]> = writable([]);

// export let pipelines: Writable<DUUIPipeline[]> = writable([]);
// export let pipelineResults: Writable<Map<String, String>> = writable(new Map());
// export let activePipelineStore: Writable<DUUIPipelineComponent[]> = writable([]);
// export let editedPipeline: Writable<DUUIPipeline> = writable();

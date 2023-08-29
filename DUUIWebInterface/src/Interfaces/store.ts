import { writable, type Writable } from 'svelte/store';
import type { DUUIPipeline, DUUIPipelineComponent } from './interfaces';

export let pipelines: Writable<DUUIPipeline[]> = writable([]);
export let pipelineResults: Writable<Map<String, String>> = writable(new Map());
export let activePipelineStore: Writable<DUUIPipelineComponent[]> = writable([]);
export let editedPipeline: Writable<DUUIPipeline> = writable();

import { writable, type Writable} from 'svelte/store';
import type { DUUIPipeline, DUUIPipelineComponent } from './interfaces';

export let activePipelineStore: Writable<DUUIPipelineComponent[]> = writable([]);
export let editedPipeline: Writable<DUUIPipeline> = writable();

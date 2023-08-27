import { writable, type Writable} from 'svelte/store';
import type { DUUIPipelineComponent } from './interfaces';

export let activePipelineStore: Writable<DUUIPipelineComponent[]> = writable([]);

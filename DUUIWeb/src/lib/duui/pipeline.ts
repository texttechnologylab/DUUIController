import { v4 as uuidv4 } from 'uuid'
import type { DUUIComponent } from './component'
import type { _Object } from '$lib/config'

export interface DUUIPipeline {
	id: string
	name: string
	description: string
	createdAt: number
	serviceStartTime: number
	timesUsed: number
	settings: _Object
	userId: string | null // if null -> Template
	components: DUUIComponent[]
}

export const blankPipeline = () =>
	<DUUIPipeline>{
		id: uuidv4(),
		name: 'New Pipeline',
		description: '',
		createdAt: Date.now(),
		serviceStartTime: 0,
		timesUsed: 0,
		settings: {},
		userId: null,
		components: []
	}

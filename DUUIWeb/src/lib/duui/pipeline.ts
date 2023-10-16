import type { DUUIComponent } from './component'
import { v4 as uuidv4 } from 'uuid'

export interface DUUIPipeline {
	id: string
	name: string
	description: string
	createdAt: number
	isService: boolean
	components: DUUIComponent[]
}

export const blankPipeline = () =>
	<DUUIPipeline>{
		id: uuidv4(),
		name: 'New Pipeline',
		description: '',
		createdAt: Date.now(),
		isService: true,
		components: []
	}

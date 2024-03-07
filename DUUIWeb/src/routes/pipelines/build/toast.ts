import {
	DUUIDockerDriver,
	DUUIRemoteDriver,
	DUUISwarmDriver,
	DUUIUIMADriver
} from '$lib/duui/component'
import type { ToastStore } from '@skeletonlabs/skeleton'

export const driverTargetMap: Map<string, string> = new Map([
	[DUUIRemoteDriver, 'Address'],
	[DUUIUIMADriver, 'Class Path'],
	[DUUIDockerDriver, 'Image Name'],
	[DUUISwarmDriver, 'Image Name']
])

const errorToast = (message: string, timeout: number = 4000) => {
	return {
		message: message,
		timeout: timeout,
		background: 'variant-filled-error'
	}
}

const successToast = (message: string, timeout: number = 4000) => {
	return {
		message: message,
		timeout: timeout,
		background: 'variant-filled-success'
	}
}

export const invalidTargetToast = (driver: string, toatsStore: ToastStore) => {
	toatsStore.trigger(errorToast(driverTargetMap.get(driver) + ' cannot be empty!'))
}

export const invalidNameToast = (toatsStore: ToastStore) => {
	toatsStore.trigger(errorToast('Name cannot be empty!'))
}

export const pipelineCreatedToast = (toatsStore: ToastStore) => {
	toatsStore.trigger(successToast('Pipeline created successfully.'))
}

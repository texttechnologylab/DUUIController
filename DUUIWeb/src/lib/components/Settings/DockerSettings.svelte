<script lang="ts">
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import { componentStore } from '../../../routes/pipelines/new/store'

	let useGPU: boolean = false
	let dockerImageFetching: boolean = false

	const tooltip: PopupSettings = {
		event: 'hover',
		target: 'tooltip',
		placement: 'top'
	}

	$: {
		$componentStore.settings.options['gpu'] = useGPU
		$componentStore.settings.options['dockerImageFetching'] = dockerImageFetching
	}
</script>


<script lang="ts">
	import { onMount } from "svelte"

	export let pipelineId: string

    type Progress = {
        name: string,
        items: {
            id: string,
            status: string,
            progress: number,
            finished: boolean
        }[]
    }

    let progress: Progress
    const UPDATE_INTERVAL = 2000

	const update = async () => {
		const response = await fetch(`/api/components/download?id=${pipelineId}`, {
			method: 'GET'
		})

        if (response.ok) {
            progress = progress
        }
	}

    onMount(() => {

        let interval: NodeJS.Timeout = setInterval(update, UPDATE_INTERVAL)
		update()
		return () => clearInterval(interval)
    })

</script>

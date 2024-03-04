<script lang="ts">
	// import { faCheck, faClipboard, faClipboardCheck } from '@fortawesome/free-solid-svg-icons'
	// import { clipboard } from '@skeletonlabs/skeleton'
	// import { onMount } from 'svelte'
	// import Fa from 'svelte-fa'

	export let key: string
	export let value: any

	// const content = JSON.stringify(
	// 	{
	// 		key: key,
	// 		value: value
	// 	},
	// 	null,
	// 	2
	// )

	// let copied: boolean = false
	// onMount(() => {
	// 	copied = false
	// })
</script>

<div class="card p-4 flex text-center items-center justify-center">
	<div>
		<p class="font-bold  text-lg">{value}</p>
		<p>{key}</p>
	</div>
	<!-- {#if copied}
		<Fa class="text-success-500" icon={faCheck} />
	{:else}
		<button
			class="inline-flex items-center gap-2
		"
			use:clipboard={content}
			on:click={() => (copied = true)}
		>
			<Fa class="transition-300 hover:text-primary-500" icon={faClipboard} />
		</button>
	{/if} -->
</div>

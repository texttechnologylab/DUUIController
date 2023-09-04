<script lang="ts">
	import { goto } from '$app/navigation'
	import { Step, Stepper } from '@skeletonlabs/skeleton'
	let name: String = ''
</script>

<Stepper on:complete={() => alert(name)}>
	<Step locked={name === ''}>
		<svelte:fragment slot="header">New Pipeline</svelte:fragment>
		<label class="label">
			<span>Name</span>
			<input bind:value={name} class="input focus-within:outline-primary-400" type="text" />
		</label>
		<svelte:fragment slot="navigation">
			<button class="btn variant-filled-error" on:abort={() => goto("/pipelines")} >Cancel</button>
		</svelte:fragment>
	</Step>
	<Step>
		<svelte:fragment slot="header">(header)</svelte:fragment>
	</Step>
	<!-- ... -->
</Stepper>

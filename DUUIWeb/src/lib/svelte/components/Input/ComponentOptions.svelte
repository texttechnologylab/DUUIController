<!--	
	@component
	A component used to edit the options for a DUUIComponent.
-->
<script lang="ts">
	import { DUUIDockerDriver, DUUIRemoteDriver, type DUUIComponent } from '$lib/duui/component'
	import { equals } from '$lib/duui/utils/text'
	import { SlideToggle } from '@skeletonlabs/skeleton'
	import Number from './Number.svelte'
	export let component: DUUIComponent
</script>

<div class="gap-4 flex flex-col items-start justify-center">
	{#if equals(component.driver, DUUIDockerDriver)}
		<div class="space-y-1">
			<label for="slider" class="flex items-center">
				<SlideToggle
					background="bg-surface-100-800-token"
					active="bg-primary-500"
					rounded="rounded-full"
					border="bordered-soft"
					name="slider"
					bind:checked={component.options.use_GPU}
				>
					<span class="form-label">Use GPU</span>
				</SlideToggle>
			</label>
			<p>When checked, allows Docker to utilize the GPU.</p>
		</div>

		<div class="space-y-1">
			<label for="slider" class="flex items-center">
				<SlideToggle
					background="bg-surface-100-800-token"
					active="bg-primary-500"
					rounded="rounded-full"
					border="bordered-soft"
					name="slider"
					bind:checked={component.options.docker_image_fetching}
				>
					<span class="form-label">Docker Image Fetching</span>
				</SlideToggle>
			</label>
			<p>When checked, attempts to download the image if it is doesn't exist locally.</p>
		</div>
	{/if}

	{#if equals(component.driver, DUUIRemoteDriver)}
		<div class="space-y-1">
			<label for="slider" class="flex items-center">
				<SlideToggle
					background="bg-surface-200-700-token"
					active="bg-primary-500"
					rounded="rounded-full"
					name="slider"
					bind:checked={component.options.ignore_200_error}
				>
					<span class="form-label">Ignore 200 errors</span>
				</SlideToggle>
			</label>
			<p>When checked, ignores all errors as long as a status code of 200 is returned.</p>
		</div>
	{/if}
	<div class="space-y-2">
		<Number min={1} max={20} name="scale" label="Scale" bind:value={component.options.scale} />
		<p>Components can create multiple replicas of themselves for distributed processing.</p>
	</div>
	<!-- <TextInput name="host" label="Host" bind:value={component.options.host} /> -->
	<!-- <div class="space-y-4">
		<TextInput name="host" label="Username" bind:value={component.options.registry_auth.username} />
		<TextInput name="host" label="Password" bind:value={component.options.registry_auth.password} />
	</div> -->
</div>

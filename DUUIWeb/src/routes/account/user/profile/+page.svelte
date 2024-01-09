<script lang="ts">
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import Checkbox from '$lib/svelte/widgets/input/Checkbox.svelte'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'

	import { isEqual, cloneDeep } from 'lodash'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import { faFileCircleCheck } from '@fortawesome/free-solid-svg-icons'
	import { fly } from 'svelte/transition'

	export let data
	const { user } = data
	const copy = cloneDeep(user)

	let editing: boolean = false
	let name: string = 'Test'
	let changed: boolean = false

	$: {
		changed = !isEqual(user, copy)
	}
</script>

{#if changed}
	<div in:fly={{ y: -50 }}>
		<ActionButton
			text="Save changes"
			variant="variant-filled-success dark:variant-soft-success"
			icon={faFileCircleCheck}
		/>
	</div>
{/if}
<section class="grid md:grid-cols-2 gap-4">
	<div class="space-y-4">
		<Text label="Name" name="name" readonly={!editing} bind:value={name} />
		<Text label="E-Mail" name="email" readonly={!editing} bind:value={user.email} />
		<!-- <Dropdown
			label="Language"
			name="language"
			bind:value={user.preferences.language}
			options={['English', 'German']}
		/> -->
	</div>
	<div class="space-y-4 pt-5">
		<Checkbox
			label="Show hints when using the editor."
			name="hints"
			bind:checked={user.preferences.tutorial}
		/>
		<Checkbox
			label="Enable notifications to get informed when a pipeline is finished."
			name="notifications"
			bind:checked={user.preferences.notifications}
		/>
	</div>
</section>

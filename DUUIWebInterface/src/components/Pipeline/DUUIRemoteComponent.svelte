<script lang="ts">
	import TextInput from '../Forms/TextInput.svelte';

	let address: string;
</script>

<TextInput label={'Address'} bind:value={address} />

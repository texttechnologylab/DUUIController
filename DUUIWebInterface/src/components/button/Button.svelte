<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';
	import { createEventDispatcher } from 'svelte';

    const dispatch = createEventDispatcher();
	export let text: string;
	export let error: boolean = false;
	export let iconName: string;

    function clickEvent() {
        dispatch("click", "generate")
    }
</script>

<a class="button {error ? 'error' : ''}" href="#" on:click>
	<Icon class="text-white w-5 h-5" name={iconName} />
	<span>{text}</span>
</a>

<style lang="scss">
	.button {
		font-weight: bold;
		border: none;
		cursor: pointer;
		text-decoration: none;
		color: white;
		background-color: #448cc1;
		border-radius: 0.25rem;
		display: flex;
		align-items: center;
		justify-content: flex-start;

		padding: 0.75rem 1rem;
		transition: 0.3s;

		& span {
			padding: 0rem 1.5rem;
			color: white;
		}

		&:hover {
			background-color: #2970a5;
		}
	}

	.error {
		background-color: #c14444;
		&:hover {
			background-color: #cf1919;
		}
	}
</style>

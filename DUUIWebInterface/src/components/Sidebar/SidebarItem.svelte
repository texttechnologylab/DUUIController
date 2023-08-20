<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';
	import { page } from '$app/stores';

	export let text: string;
	export let link: string;
	export let iconName: string;
</script>

<li class="sidebar__item" aria-current={$page.url.pathname === link ? 'page' : undefined}>
	<a href={link} class="sidebar__item-link">
		<Icon class="text-white w-5 h-5" name={iconName} />
		<span>{text}</span>
	</a>
</li>

<style lang="scss">
	.sidebar__item {
		&:hover {
			background-color: #2970a5;
		}

		&[aria-current='page'] {
			background-color: #2970a5;
		}
	}

	.sidebar__item:nth-child(5) {
		margin-top: auto;
	}

	.sidebar__item-link {
		display: flex;
		align-items: center;
		justify-content: flex-start;
		gap: 1rem;
		color: black;
		text-decoration: none;
		padding: 1rem 1rem;

		span {
			padding: 0rem 1rem 0rem 0rem;
			color: white;
		}
	}
</style>

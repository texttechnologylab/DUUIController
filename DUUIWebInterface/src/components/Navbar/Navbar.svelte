<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';
	import NavbarItem from './NavbarItem.svelte';
	// [&>*:nth-child(5)]:

	let navVisible: boolean = false;
</script>

<header class="sticky top-0 flex justify-between items-center bg-blue-500 z-10 shadow-md">
	<nav class="mx-auto flex justify-end items-center w-full">
		<ul
			role="list"
			class="
			md:grid grid-cols-4 items-center justify-center gap-4 xl:gap-12 px-4 py-2 hidden"
		>
			<NavbarItem text="Home" href="/" iconName="home-solid" />
			<NavbarItem text="Create" href="/pipelines/create" iconName="rocket-solid" />
			<NavbarItem text="Pipelines" href="/pipelines" iconName="bookmark-solid" />
			<NavbarItem text="Login" href="/login" iconName="user-solid" />
		</ul>
		<button class="md:hidden text-white p-4 self-end">
			<Icon name="bars-solid" class="min-w-[1.25rem] aspect-square" />
		</button>
	</nav>
</header>

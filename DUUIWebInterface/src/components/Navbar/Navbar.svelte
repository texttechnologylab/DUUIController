<script>
	import NavbarItem from './NavbarItem.svelte';
	// [&>*:nth-child(5)]:
</script>

<aside class="navbar | sticky bg-slate-800 top-0">
	<ul
		role="list"
		class="navbar__list | flex justify-center flex-row min-w-screen ml-auto" 
		
	>
		<NavbarItem text="Home" href="/" iconName="home-solid" />
		<NavbarItem text="Analysis" href="/analysis" iconName="rocket-solid" />
		<NavbarItem text="Status" href="/status" iconName="shield-check-solid" />
		<NavbarItem text="Templates" href="/templates" iconName="bookmark-solid" />
		<NavbarItem text="About" href="/about" iconName="info-circle-solid" />
		<NavbarItem text="Login" href="/login" iconName="user-plus-solid" />
	</ul>
</aside>

<style>
</style>

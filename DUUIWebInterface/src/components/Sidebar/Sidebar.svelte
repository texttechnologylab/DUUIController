<script>
	import SidebarItem from './SidebarItem.svelte';
</script>

<aside class="sidebar | fixed bg-blue-400 top-0 bottom-0">
	<ul role="list" class="sidebar__list | flex justify-start flex-col min-h-screen [&>*:nth-child(5)]:mt-auto">
		<SidebarItem text="Home" link="/" iconName="home-solid" />
		<SidebarItem text="Analysis" link="/analysis" iconName="rocket-solid" />
		<SidebarItem text="Status" link="/status" iconName="shield-check-solid" />
		<SidebarItem text="Templates" link="/templates" iconName="bookmark-solid" />
		<SidebarItem text="About" link="/about" iconName="info-circle-solid" />
		<SidebarItem text="Login" link="/login" iconName="user-plus-solid" />
	</ul>
</aside>

<style>

</style>

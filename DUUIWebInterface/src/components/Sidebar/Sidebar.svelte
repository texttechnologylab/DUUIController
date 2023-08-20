<script>
	import SidebarItem from './SidebarItem.svelte';
</script>

<aside class="sidebar">
	<ul role="list" class="sidebar__list">
		<SidebarItem text="Home" link="/" iconName="home-solid" />
		<SidebarItem text="Analysis" link="/analysis" iconName="rocket-solid" />
		<SidebarItem text="Status" link="/status" iconName="shield-check-solid" />
		<SidebarItem text="Templates" link="/templates" iconName="bookmark-solid" />
		<SidebarItem text="About" link="/about" iconName="info-circle-solid" />
		<SidebarItem text="Login" link="/login" iconName="user-plus-solid" />
	</ul>
</aside>

<style>
	.sidebar {
		position: fixed;
		background-color: #448cc1;
		top: 0;
		bottom: 0;
	}

	.sidebar__list {
		display: flex;
		justify-content: flex-start;
		flex-direction: column;
		min-height: 100vh;
	}
</style>

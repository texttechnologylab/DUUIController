<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';

	const links: string[] = ['Github', 'Documentation', 'TTLab'];
</script>

<footer class=" bg-[#333] text-white mt-auto">
	<div class="max-w-7xl mx-auto flex justify-between p-8 gap-4 items-center">
		<img src="LogoTTLabWhite.png" alt="" class="max-h-14" />
		<p class="text-center font-heading font-bold text-lg p-4">DUUI</p>

		<nav>
			<ul class="flex flex-col gap-2">
				{#each links as link}
					<li class="flex gap-4 items-center">
						<Icon name="github-solid" />
						<a href="#">{link}</a>
					</li>
				{/each}
			</ul>
		</nav>
	</div>
</footer>

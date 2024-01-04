<script lang="ts">
	export let title: string
	export let info: string = ''
	export let href: string
</script>

<a
	{href}
	class="group space-y-1 transition-colors text-left text-sm rounded-none hover:variant-filled-primary dark:hover:variant-soft-primary p-4"
	><span class="font-bold"> {title} </span>
	{#if info}
		<p class="max-w-[40ch]">{info}</p>
	{/if}
</a>

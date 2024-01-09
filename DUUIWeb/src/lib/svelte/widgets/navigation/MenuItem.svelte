<script lang="ts">
	export let title: string
	export let info: string = ''
	export let href: string
</script>

<a
	{href}
	class="rounded-none space-y-1 transition-colors text-left hover:variant-filled-primary dark:hover:variant-soft-primary p-3"
	><span> {title} </span>
	{#if info}
		<p class="max-w-[40ch] text-sm">{info}</p>
	{/if}
</a>

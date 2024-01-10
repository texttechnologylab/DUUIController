<script lang="ts">
	export let href: string
	export let dimmed: boolean = false
	export let underline: boolean = false
</script>

<a
	class="hover:text-primary-500 transition-colors
    {dimmed ? 'text-surface-400 dark:text-surface-200' : ''}
	{underline ? 'anchor' : ''}"
	{href}
>
	<slot />
</a>

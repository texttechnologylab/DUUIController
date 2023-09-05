<script lang="ts">
	export let title: string
	export let content: string
</script>

<section
	class="card variant-glass-surface p-4 shadow-lg md:p-8 space-y-4 self-stretch md:flex gap-8"
>
	<div class="flex flex-col md:items-start gap-4 md:text-left">
		<h3 class="h3 card-header">{title}</h3>
		<p class="max-w-[40ch]">{content}</p>
	</div>
	<slot name="illustration" />
</section>

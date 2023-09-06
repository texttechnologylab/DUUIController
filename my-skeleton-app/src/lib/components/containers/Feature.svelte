<script lang="ts">
	export let title: string
	export let content: string
	export let reverse: boolean = false
</script>

<section class="card variant-glass-secondary p-4 shadow-lg md:p-8 space-y-4">
	<h3 class="h3 card-header">{title}</h3>
	<div class="space-y-4">
		<p class="max-w-[40ch]">{content}</p>
		<slot name="illustration" />
	</div>
</section>

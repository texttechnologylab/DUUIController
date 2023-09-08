<script lang="ts">
	export let title: string
	export let content: string
	export let reverse: boolean = false
</script>

<section class="p-4 text-center md:p-8 space-y-4 card variant-glass-surface shadow-lg">
	<h3 class="h3">{title}</h3>
	<hr class="max-w-3xl mx-auto" />
	<div class="md:flex md:text-left md:items-center">
		<p class="max-w-[40ch] md:mr-auto grow">{content}</p>
		<slot name="illustration" />
	</div>
</section>

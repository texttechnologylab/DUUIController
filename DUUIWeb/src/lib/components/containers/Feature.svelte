<script lang="ts">
	export let title: string
	export let content: string
</script>

<section class="variant-soft-surface shadow-lg p-4 md:p-8 space-y-4 text-left">
	<h3 class="h3 font-bold">{title}</h3>
	<div class="md:flex md:text-left md:items-center">
		<p class="max-w-[40ch] md:mr-auto grow">{content}</p>
		<slot name="illustration" />
	</div>
</section>

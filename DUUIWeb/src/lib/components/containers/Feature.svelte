<script lang="ts">
	export let title: string
	export let content: string
</script>

<section class="card rounded-md shadow-lg p-4 text-center md:p-8 space-y-4">
	<h3 class="h3">{title}</h3>
	<hr class="max-w-3xl mx-auto" />
	<div class="md:flex md:text-left md:items-center">
		<p class="max-w-[40ch] md:mr-auto grow">{content}</p>
		<slot name="illustration" />
	</div>
</section>

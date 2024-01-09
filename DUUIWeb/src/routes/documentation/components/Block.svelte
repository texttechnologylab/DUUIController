<script lang="ts">
	export let id: string
	export let title: string = id
</script>

<section {id} class="space-y-4 scroll-m-8">
	<h2 class="h3 font-bold capitalize">{title}</h2>
	<slot />
</section>

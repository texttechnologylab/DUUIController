<!-- @component A component that represents a key value pair. -->
<script lang="ts">
	export let key: string
	export let value: any
</script>

<div class="card p-4 flex text-center items-center justify-center">
	<div>
		<p class="font-bold text-lg">{value}</p>
		<p>{key}</p>
	</div>
</div>

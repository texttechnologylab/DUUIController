<script lang="ts">
	import Rating from '$lib/svelte/components/Rating.svelte'
	import TextArea from '$lib/svelte/components/TextArea.svelte'

	let requirements = 4
	let frustration = 4
	let ease = 4
	let correction = 4
	
	let message: string = ''
</script>

<div class="p-8 space-y-8">
	<h1 class="h1 text-center font-bold">Feedback</h1>
	<form
		action="?/send"
		method="POST"
		class="grid md: grid-cols-2 gap-8 items-center justify-center mx-auto"
	>
		<Rating
			index={0}
			bind:value={requirements}
			name="requirements"
			question="The website's capabilities met my requirements."
		/>
		<Rating
			index={1}
			bind:value={frustration}
			name="frustrating"
			question="Using the website is a frustrating experience."
		/>
		<Rating index={2} bind:value={ease} name="ease" question="The website is easy to use." />
		<Rating
			index={3}
			bind:value={correction}
			name="correction"
			question="I have to spend too much time correcting things when using the website."
		/>
		<div class="col-span-2 flex flex-col items-center justify-center gap-4">
			<TextArea label="Message" name="message" style="md:min-w-[600px]" bind:value={message} />
			<button type="submit" class="button-primary">Submit</button>
		</div>
	</form>
</div>

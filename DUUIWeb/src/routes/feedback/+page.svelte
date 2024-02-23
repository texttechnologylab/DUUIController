<script lang="ts">
	import { page } from '$app/stores'
	import Rating from '$lib/svelte/components/Rating.svelte'
	import TextArea from '$lib/svelte/components/TextArea.svelte'
	import TextInput from '$lib/svelte/components/TextInput.svelte'

	let requirements = 4
	let frustration = 4
	let ease = 4
	let correction = 4

	let message: string = ''
	let name: string = ''

	let success: boolean = ($page.url.searchParams.get('success') || 'false') === 'true'
</script>

<div class="bg-surface-100-800-token pattern md:py-16">
	{#if success}
		<div
			class="section-wrapper p-8 flex items-center justify-center absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2"
		>
			<p class="h2">Thank you for your feedback!</p>
		</div>
	{:else}
		<div class="p-4 space-y-8">
			<h1 class="h1 text-center font-bold">Feedback</h1>
			<p class="mx-auto text-lg variant-filled-primary p-4 rounded-md text-center">
				Thank you for testing the web interface. This feedback form includes four short questions
				regarding usability. When selecting the minimum or maximum rating for a question, consider
				also adding a short explanation (this is optional).
			</p>
			<form action="?/send" method="POST" class="grid gap-8 items-center justify-center">
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
				<div class="flex flex-col justify-center gap-8 section-wrapper p-4 md:p-16">
					<TextInput label="Name" bind:value={name} required={true} />
					<TextArea label="Message" name="message" style="md:min-w-[600px]" bind:value={message} />
					<button type="submit" class="cta box-shadow button-primary button-modal">Submit</button>
				</div>
			</form>
		</div>
	{/if}
</div>

<style>
	.pattern {
		background-image: repeating-linear-gradient(
			45deg,
			#006c9811 0,
			#006c9811 0.5px,
			transparent 0,
			transparent 50%
		);
		background-size: 16px 16px;
		background-color: rgba(71, 212, 255, 0);
	}
</style>

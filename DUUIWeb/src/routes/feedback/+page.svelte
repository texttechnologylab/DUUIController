<script lang="ts">
	import Rating from '$lib/svelte/widgets/input/Rating.svelte'
	import { Step, Stepper } from '@skeletonlabs/skeleton'

	let ease = 3
	let fast = 3
	let ui = 3
	let revisit = 3
	let prefer = 3

	let performance = 3
	let informative = 3
	let cloud = 3
	let problems = 3
	let help = 3

	let form: HTMLFormElement

	const submitForm = async () => {
		if (form) {
			form.submit()
		}
	}
</script>

<div class="p-8 space-y-8">
	<div class="p-4 section-wrapper grid grid-cols-2 gap-8">
		<div class="space-y-2">
			<p class="h2">Future Work</p>
			<ul class="space-y-4 list-decimal px-8">
				<li>Kubernetes Driver</li>
				<li>Ausweitung des Event / Status-Systems</li>
				<li>OneDrive DataReader</li>
				<li>E-Mail Benachrichtigungen</li>
				<li>API Limitierung der Anfragen pro Zeitintervall</li>
				<li>Vervollständigen der Dokumentation</li>
				<li>Python Bindings für die API</li>
			</ul>
		</div>
		<div class="space-y-2">
			<p class="h2">TODO</p>
			<ul class="space-y-4 list-decimal px-8">
				<li>Evaluation</li>
				<li>Dokumentation</li>
				<li>Bachelor Arbeit schreiben</li>
			</ul>
		</div>
	</div>

	<!-- TITEL = A web-based interface for DUUI -->
	<div class="p-4 section-wrapper">
		<h1 class="h1 text-center font-bold">Feedback</h1>
		<Stepper on:complete={submitForm}>
			<form bind:this={form} action="?/send" method="POST" class="grid gap-16">
				<Step>
					<div class="grid md:grid-cols-2 md:gap-4">
						<Rating
							bind:value={ease}
							name="ease"
							question="The website made it easier to create Pipelines."
						/>
						<Rating
							bind:value={fast}
							name="efficient"
							question="The website allowed me to work with DUUI faster."
						/>
						<Rating bind:value={revisit} name="revisit" question="I would use the website again." />
						<Rating
							bind:value={prefer}
							name="prefer"
							question="Working with DUUI is more enjoyable in the browser."
						/>
						<Rating bind:value={ui} name="ui" question="The website is visually pleasing." />
						<Rating
							bind:value={performance}
							name="performance"
							question="Processes did not take too long to complete."
						/>
					</div>
				</Step>
				<Step>
					<div class="grid md:grid-cols-2 md:gap-4">
						<Rating
							bind:value={informative}
							name="informative"
							question="The website presented useful information."
						/>
						<Rating
							bind:value={cloud}
							name="cloud"
							question="Using Dropbox / Minio worked without problems."
						/>
						<Rating
							bind:value={problems}
							name="problems"
							question="The task could be completed without problems."
						/>
						<Rating
							bind:value={help}
							name="help"
							question="The documentation was helpful and easy to use."
						/>
					</div>
				</Step>
			</form>
		</Stepper>
	</div>
</div>

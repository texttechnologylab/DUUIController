<script lang="ts">
	import { faClose, faMagnifyingGlass, faMapSigns } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const modalStore = getModalStore()

	const startTour = async () => {
		modalStore.close()
		modalStore.trigger({
			type: 'component',
			component: 'helpModal'
		})
	}
</script>

<div class="z-50 bg-surface-50-900-token w-modal rounded-md overflow-hidden border-2 border-color">
	<div class="modal-header bg-surface-100-800-token">
		<h3 class="h3">Welcome to DUUI Web!</h3>
		<button on:click={modalStore.close}>
			<Fa icon={faClose} size="lg" />
		</button>
	</div>
	<div class="modal-body">
		<div class="p-8 space-y-12 text-lg grid">
			<p>
				This web interface complements the Java framework <a
					href="https://github.com/texttechnologylab/DockerUnifiedUIMAInterface"
					class="anchor">Docker Unified UIMA Interface</a
				> and is designed to help you automate the analysis of large quantities of natural language with
				the need for programming knowledge.
			</p>
			<hr class="hr" />
			<p>Do you want to start a small tour through the web interface to get started?</p>
			<div class="grid md:grid-cols-2 items-center gap-4">
				<button class="button-primary button-modal" on:click={startTour}>
					<Fa icon={faMapSigns} />
					<span>Help</span>
				</button>
				<button class="button-neutral button-modal" on:click={modalStore.close}>
					<span>Explore on your own</span>
					<Fa icon={faMagnifyingGlass} />
				</button>
			</div>
		</div>
	</div>
</div>

<script lang="ts">
	import Endpoint from '$lib/svelte/components/Endpoint.svelte'
	import { faArrowUp } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import { CodeBlock } from '@skeletonlabs/skeleton'

	export let data
	const { endpoints } = data
</script>

<div class=" max-w-7xl mx-auto space-y-8 p-4">
	<h1 class="h1 scroll-mt-8">API Reference</h1>
	<hr class="hr !w-full" />

	<div class="space-y-8 leading-normal">
		<!-- REST -->
		<h2 id="rest" class="h2">REST</h2>
		<p>
			This page lists all available endpoints. Each endpoint can be expanded for a more detailed
			view.
		</p>

		<hr class="hr !w-full" />

		<div class="space-y-4">
			<h3 class="h3" id="pipeline">Pipelines</h3>
			{#each endpoints.pipelines as endpoint}
				<Endpoint {endpoint} />
			{/each}
		</div>
		<hr class="hr !w-full" />
		<div class="space-y-4">
			<h3 class="h3" id="component">Components</h3>
			{#each endpoints.components as endpoint}
				<Endpoint {endpoint} />
			{/each}
		</div>
		<hr class="hr !w-full" />
		<div class="space-y-4">
			<h3 class="h3" id="process">Processes</h3>
			{#each endpoints.processes as endpoint}
				<Endpoint {endpoint} />
			{/each}
		</div>

		<hr class="hr !w-full" />

		<!-- Java -->
		<div class="space-y-4">
			<h2 class="h2" id="java">Java</h2>
			<div class="space-y-4">
				<p>
					Documentation for DUUI using Java can be found on <a
						class="anchor"
						href="https://github.com/texttechnologylab/DockerUnifiedUIMAInterface">GitHub</a
					>.
				</p>
			</div>
		</div>

		<hr class="hr !w-full" />

		<!-- Python -->
		<div class="space-y-4">
			<h2 class="h2" id="python">Python</h2>
			<div class="space-y-8">
				<p class="max-w-none">
					Using python with DUUI can be done by sending requests to the API. An example for creating
					a pipeline and running it afterwards can be seen below.
				</p>
				<hr class="hr !w-full" />
				<div class="space-y-4">
					<h3 class="h3">Create a pipeline</h3>
					<CodeBlock
						language="py"
						code={`import requests

API_KEY = "YOUR API KEY"


def main() -> None:
    response = requests.post(
        "api.duui.texttechnologylab.org/pipelines",
        headers={"Authorization": API_KEY},
        json={
            "name": "Pipeline made with Python",
            "description": "This pipeline has been created using the API with Python.",
            "tags": ["Python", "Token"],
            "settings": {
                "timeout": 10000,
            },
            "components": [
                {
                    "name": "Tokenizer",
                    "tags": ["UIMA", "Token", "Sentence"],
                    "options": {
                        "scale": 5,
                    },
                    "driver": "DUUIUIMADriver",
                    "target": "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter",
                    "parameters": {"timeout": 50000},
                }
            ],
        },
    )


if __name__ == "__main__":
    main()
				`}
					/>
				</div>
				<div class="space-y-4">
					<h3 class="h3">Start a process</h3>
					<CodeBlock
						language="py"
						code={`response = requests.post(
	"api.duui.texttechnologylab.org/processes",
	headers={"Authorization": API_KEY},
	json={
		"pipeline_id": "65bbb2ab10629974b51d19ca",
		"input": {
			"provider": "Dropbox",
			"path": "/input",
			"file_extension": ".txt",
		},
		"output": {
			"provider": "Dropbox",
			"file_extension": ".txt",
			"path": "/output/python",
		},
		"settings": {
			"recursive": True,
			"minimum_size": 0,
			"worker_count": 10,
			"check_target": True,
		},
	},
)
				`}
					/>
				</div>
			</div>
		</div>
	</div>
</div>

<a href="/documentation/api#rest" class="button-neutral rounded-full fixed bottom-8 right-8 z-50">
	<Fa icon={faArrowUp} size="lg" />
</a>

<style>
	h2,
	h3 {
		scroll-margin-top: 32px;
	}
</style>

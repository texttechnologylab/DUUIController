<script lang="ts">
	import Endpoint from '$lib/svelte/components/Endpoint.svelte'
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
				<div class="space-y-4">
					<h3 class="h3">Create a pipeline</h3>
					<CodeBlock
						language="py"
						code={`import requests

API_KEY = "YOUR API KEY"


from duui.client import DUUIClient
from duui.config import API_KEY

CLIENT = DUUIClient(API_KEY)


my_pipeline = CLIENT.pipelines.create(
	name="My Pipeline",
	components=[
		{
			"name": "Tokenizer",
			"tags": ["Token", "Sentence"],
			"description": """Split the document into Tokens and Sentences
				using the DKPro BreakIteratorSegmenter AnalysisEngine.""",
			"driver": "DUUIUIMADriver",
			"target": "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter",
		},
		{
			"name": "GerVADER",
			"description": """GerVADER is a German adaptation of the sentiment
				classification tool VADER. Classify sentences into positive,
				negative or neutral statements.""",
			"tags": ["Sentiment", "German"],
			"driver": "DUUIDockerDriver",
			"target": "docker.texttechnologylab.org/gervader_duui:latest",
			"options": {"scale": 2, "use_GPU": True},
		},
	],
	description="""This pipeline has been created using the API with Python.
	It splits the document text into Tokens and Sentences
	and then analyzes the Sentiment of these Sentences.""",
	tags=["Python", "Sentence", "Sentiment"],
)
				`}
					/>
				</div>
				<div class="space-y-4">
					<h3 class="h3">Start a process</h3>
					<p class="max-w-none">
						Before starting a process, the pipeline is instantiated for better reusability. After a
						process has completed, the pipeline won't shutdown but remain active for further
						requests.
					</p>
					<CodeBlock
						language="py"
						code={`
pipeline_id = my_pipeline.get("oid")
# Instantiate the pipeline so it can be used multiple times
# without the need to restart Docker components
CLIENT.pipelines.instantiate(pipeline_id)

# Start a process that finds .txt files with a minimum size of 500 bytes
# recursively start from the /input directory in Dropbox.
my_process = CLIENT.processes.start(
	pipeline_id,
	input={
		"provider": "Dropbox",
		"path": "/input",
		"file_extension": ".txt"
	},
	output={
		"provider": "Dropbox",
		"path": "/output/python",
		"file_extension": ".txt",
	},
	recursive=True,
	sort_by_size=True,
	minimum_size=500,
	worker_count=3
)
`}
					/>
				</div>
				<div class="space-y-4">
					<h3 class="h3">Monitor the process</h3>
					<CodeBlock
						language="py"
						code={`
...

import schedule
import sys
import time

process_id = my_process.get("oid")

def update() -> None:
	process = CLIENT.processes.findOne(process_id)
	documents = CLIENT.processes.documents(
		process_id, status_filter=["Failed"], include_count=True
	)
	total = len(process["document_names"])

	print(
		f"""Progress: {round(process['progress'] / total * 100)}%
		\t{documents['count']} Documents have failed.\r""",
		end="",
	)

	if process["status"] in ["Completed", "Failed", "Cancelled"]:
		print(
			f"""Progress: {round(process['progress'] / total * 100)}
			%\t{documents['count']} Documents have failed.""",
		)

		print(f"Process finished with status {process['status']}.")
		sys.exit(0)

schedule.every(5).seconds.do(update)

while True:
	schedule.run_pending()
	time.sleep(1)
				`}
					/>
				</div>
			</div>
		</div>
	</div>
</div>

<style>
	h2,
	h3 {
		scroll-margin-top: 32px;
	}
</style>

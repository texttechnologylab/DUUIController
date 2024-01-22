<script>
	import JavaClass from '$lib/svelte/widgets/duui/JavaClass.svelte'
	import { faArrowUp } from '@fortawesome/free-solid-svg-icons'
	import { CodeBlock } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const code = `
	int iWorkers = 2; // define the number of workers

JCas jc = JCasFactory.createJCas(); // A empty CAS document is defined.

// load content into jc ...

// Defining LUA-Context for communication
DUUILuaContext ctx = LuaConsts.getJSON();

// Defining a storage backend based on SQlite.
DUUISqliteStorageBackend sqlite = new DUUISqliteStorageBackend("loggingSQlite.db")
            .withConnectionPoolSize(iWorkers);

// The composer is defined and initialized with a standard Lua context as well with a storage backend.
DUUIComposer composer = new DUUIComposer().withLuaContext(ctx)
                        .withScale(iWorkers).withStorageBackend(sqlite);
                
// Instantiate drivers with options (example)
DUUIDockerDriver docker_driver = new DUUIDockerDriver()
        .withTimeout(10000);
DUUIRemoteDriver remote_driver = new DUUIRemoteDriver(10000);
DUUIUIMADriver uima_driver = new DUUIUIMADriver().withDebug(true);
DUUISwarmDriver swarm_driver = new DUUISwarmDriver();

// A driver must be added before components can be added for it in the composer. After that the composer is able to use the individual drivers.
composer.addDriver(docker_driver, remote_driver, uima_driver, swarm_driver);

// A new component for the composer is added
composer.add(new DUUIDockerDriver.
    Component("docker.texttechnologylab.org/gnfinder:latest")
    .withScale(iWorkers)
    // The image is reloaded and fetched, regardless of whether it already exists locally (optional)
    .withImageFetching());
    
// Adding a UIMA annotator for writing the result of the pipeline as XMI files.
composer.add(new DUUIUIMADriver.Component(
                createEngineDescription(XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, sOutputPath,
                )).withScale(iWorkers));

// The document is processed through the pipeline. In addition, files of entire repositories can be processed.
composer.run(jc);
	`

	const jitpack = `<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>`

	const maven = `<dependency>
  <groupId>com.github.texttechnologylab</groupId>
  <artifactId>DockerUnifiedUIMAInterface</artifactId>
  <version>1.0</version>
</dependency>`
</script>

<svelte:head>
	<title>Documentation</title>
</svelte:head>

<div class="bg-repeat [&_p]:max-w-[70ch] md:text-left py-8 gradient">
	<div class=" max-w-7xl mx-auto space-y-8 p-4">
		<h1 class="h1 font-bold scroll-mt-4 !mb-16" id="introduction">Documentation</h1>
		<div class="grid md:grid-cols-2 gap-8">
			<div class="space-y-4 text-justify">
				<div class="space-y-4">
					<h2 class="h2 font-bold pb-4">Introduction</h2>
					<p>
						Automatic analysis of large text corpora is a complex task. This complexity particularly
						concerns the question of time efficiency. Furthermore, efficient, flexible, and
						extensible textanalysis requires the continuous integration of every new text analysis
						tools.
					</p>
					<p>
						Since there are currently, in the area of NLP and especially in the application context
						of UIMA, only very few to no adequate frameworks for these purposes, which are not
						simultaneously outdated or can no longer be used for security reasons, this work will
						present a new approach to fill this gap.
					</p>
				</div>

				<div class="space-y-4">
					<h2 class="h2 font-bold">Docker Unified UIMA Interface</h2>
					<p>
						To this end, we present Docker Unified UIMA Interface (DUUI), a scalable, flexible,
						lightweight, and featurerich framework for automated and distributed analysis of text
						corpora that leverages experience in Big Data analytics and virtualization with Docker.
					</p>
					<h3 class="h3 font-bold">Functionality</h3>
					<p>
						Lorem ipsum dolor sit amet consectetur adipisicing elit. Maxime dolorem culpa beatae in
						quisquam exercitationem nemo possimus unde et nihil! Officiis alias numquam obcaecati
						suscipit, tempora voluptas asperiores accusantium nisi!
					</p>
				</div>
			</div>

			<JavaClass
				name="IDUUIDocumentHandler"
				members={[]}
				methods={[
					{
						name: 'writeDocument',
						throws: ['IOExcetion'],
						args: [
							{
								type: 'DUUIDocument',
								name: 'document'
							},
							{
								type: 'String',
								name: 'path'
							}
						],
						description:
							'Writes one document to the target location. Should be the full path in the storage.'
					},
					{
						name: 'writeDocuments',
						throws: ['IOExcetion'],
						args: [
							{
								type: 'List<DUUIDocument>',
								name: 'documents'
							},
							{
								type: 'String',
								name: 'path'
							}
						],
						description:
							'Writes multiple documents to the target location. Should be the full path to a folder or bucket.'
					},
					{
						name: 'readDocument',
						returns: 'DUUIDocument',
						throws: ['IOExcetion'],
						args: [
							{
								type: 'String',
								name: 'path'
							}
						],
						description:
							'Reads one document from the source location. Should be the full path.'
					},
					{
						name: 'readDocuments',
						returns: 'List<DUUIDocument>',
						throws: ['IOExcetion'],
						args: [
							{
								type: 'List<String>',
								name: 'paths'
							}
						],
						description:
							'Reads multiple documents from the source location. Should be the full to a folder or bucket.'
					},
					{
						name: 'listDocuments',
						returns: 'List<String>',
						throws: ['IOExcetion'],
						args: [
							{
								type: 'String',
								name: 'path'
							},
							{
								type: 'String',
								name: 'fileExtension'
							},
							{
								type: 'boolean',
								name: 'recursive'
							}
						],
						description:
							'Reads multiple documents from the source location. Should be the full to a folder or bucket.'
					}
				]}
				packageName="org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader"
			>
				<svelte:fragment slot="description">
					Handles the IO operations for reading and writing from and to different cloud or local
					services. This interface enforces the implementation of 6 methods.
				</svelte:fragment>
			</JavaClass>
		</div>

		<hr class="hr !h-[2px] !my-16" />

		<div id="composer" class="scroll-mt-4 space-y-4">
			<h2 class="h2 font-bold pb-4">Composer</h2>
			<div class=" space-y-8">
				<p>
					The Composer controls the entire Workflow by managing <a
						class="anchor"
						href="/documentation#driver">Drivers</a
					> and the IO.
				</p>
			</div>
		</div>

		<hr class="hr !h-[2px] !my-16" />

		<div id="driver" class="scroll-mt-4 space-y-2 text-justify">
			<h2 class="h2 font-bold pb-4">Driver</h2>
			<h3 class="h3 font-bold">UIMADriver</h3>
			<div class="grid md:grid-cols-2 py-2 items-start gap-8">
				<p>
					The UIMADriver runs a UIMA Analysis Engine (AE) on the local machine (using local memory
					and processor) in the same process within the JRE and allows scaling on that machine by
					replicating the underlying Analysis Engine. This enables the use of all previous analysis
					methods based on UIMA AE without further adjustments.
				</p>
				<CodeBlock
					code="DUUIUIMADriver uimaDriver = new DUUIUIMADriver().withDebug(true);"
					language="java"
				/>
			</div>
			<h3 class="h3 font-bold scroll-mt-4" id="dockerdriver">DockerDriver</h3>
			<div class="grid md:grid-cols-2 py-2 items-start gap-8">
				<p>
					The DUUI core driver runs Components on the local Docker daemon and enables
					machine-specific resource management. This requires that the AEs are available as Docker
					images according to DUUI to run as Docker containers. It is not relevant whether the
					Docker image is stored locally or in a remote registry, since the Docker container is
					built on startup. This makes it very easy to test new AEs (as local containers) before
					being released. The distinction between local and remote Docker images is achieved by the
					URI of the Docker image used.
				</p>
				<CodeBlock
					code="DUUIDockerDriver dockerDriver = new DUUIDockerDriver().withTimeout(10000);"
					language="java"
				/>
			</div>
			<h3 class="h3 font-bold">RemoteDriver</h3>
			<div class="grid md:grid-cols-2 py-2 items-start gap-8">
				<p>
					AEs that are not available as containers and whose models can or should not be shared can
					still be used if they are available via REST. Since DUUI communicates via RESTful, remote
					endpoints can be used for pre-processing. In general, AEs implemented based on DUUI can be
					accessed and used via REST, but the scaling is limited regarding request and processing
					capabilities of the hosting system.<br /><br /> In addition, Components addressed via the RemoteDriver
					can be used as services. This has advantages for AEs that need to hold large models in memory
					and thus require a long startup time. To avoid continuous reloading, it may be necessary to
					start a service once or twice in a dedicated mode and then use a RemoteDriver to access it.
					To use services, their URL must be specified to enable horizontal scaling.
				</p>
				<CodeBlock code="DUUIRemote remoteDriver = new DUUIRemoteDriver(10000);" language="java" />
			</div>
			<h3 class="h3 font-bold scroll-mt-4" id="swarmdriver">SwarmDriver</h3>
			<div class="grid md:grid-cols-2 py-2 items-start gap-8">
				<p>
					The SwarmDriver complements the <a class="anchor" href="/documentation#dockerdriver"
						>DockerDriver</a
					>; it uses the same functionalities, but its AEs are used as Docker images distributed
					within the Docker Swarm network. A swarm consists of n nodes and is controlled by a leader
					node within the Docker framework. However, if an application using DUUI is executed on a
					Docker leader node, the individual AEs can be executed on multiple swarm nodes.
				</p>
				<CodeBlock code="DUUISwarmDriver swarmDriver = new DUUISwarmDriver();" language="java" />
			</div>
			<h3 class="h3 font-bold">KubernetesDriver</h3>
			<div class="grid md:grid-cols-2 py-2 items-start gap-8">
				<p>
					The KubernetesDriver works similarly to the <a
						class="anchor"
						href="/documentation#swarmdriver">SwarmDriver</a
					>, but Kubernetes is used as the runtime environment instead of Docker Swarm.
				</p>
				<CodeBlock
					code="DUUIKubernetesDriver kubernetesDriver = new DUUIKubernetesDriver();"
					language="java"
				/>
			</div>
		</div>

		<hr class="hr !h-[2px] !my-16" />

		<div class="space-y-4 text-justify">
			<h2 class="h2 font-bold pb-4 scroll-mt-4" id="component">Component</h2>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Ducimus quidem perspiciatis
				debitis, animi amet minima alias architecto molestiae saepe sit veritatis maiores dolorum?
				Repudiandae, architecto ea! Fugit natus praesentium suscipit.
			</p>
		</div>
		<hr class="hr !h-[2px] !my-16" />

		<div class="space-y-4 text-justify">
			<h2 class="h2 font-bold pb-4 scroll-mt-4" id="document">Document</h2>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Ducimus quidem perspiciatis
				debitis, animi amet minima alias architecto molestiae saepe sit veritatis maiores dolorum?
				Repudiandae, architecto ea! Fugit natus praesentium suscipit.
			</p>
		</div>
		<hr class="hr !h-[2px] !my-16" />

		<div class="space-y-4 text-justify">
			<h2 class="h2 font-bold pb-4 scroll-mt-4" id="process">Process</h2>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Ducimus quidem perspiciatis
				debitis, animi amet minima alias architecto molestiae saepe sit veritatis maiores dolorum?
				Repudiandae, architecto ea! Fugit natus praesentium suscipit.
			</p>
		</div>
	</div>
</div>

<a
	href="/documentation#introduction"
	class="btn-icon variant-filled-primary dark:variant-soft-primary rounded-full fixed bottom-8 right-8 z-50"
>
	<Fa icon={faArrowUp} size="lg" />
</a>

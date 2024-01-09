<script>
	import HLine from '$lib/svelte/widgets/navigation/HLine.svelte'
	import { faArrowUp } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import Block from './components/Block.svelte'
</script>

<svelte:head>
	<title>Documentation</title>
</svelte:head>

<div class="space-y-16 scroll-smooth">
	<Block id="introduction">
		<svelte:fragment>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Animi veritatis quod asperiores in
				magni iure, culpa nemo voluptate consectetur voluptatem numquam, nam, quaerat impedit?
				Recusandae facere iure delectus quam neque?
			</p>
		</svelte:fragment>
	</Block>

	<HLine width="w-1/4  mx-auto" thickness={4} />

	<Block id="composer">
		<svelte:fragment>
			<p>
				The Composer controls the entire Workflow by managing <a
					class="anchor"
					href="/documentation#driver">Drivers</a
				> and the IO.
			</p>
		</svelte:fragment>
	</Block>

	<HLine width="w-1/4  mx-auto" thickness={4} />

	<Block id="driver">
		<svelte:fragment>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Animi veritatis quod asperiores in
				magni iure, culpa nemo voluptate consectetur voluptatem numquam, nam, quaerat impedit?
				Recusandae facere iure delectus quam neque?
			</p>
		</svelte:fragment>
	</Block>

	<HLine width="w-1/4  mx-auto" thickness={4} />

	<Block id="component">
		<svelte:fragment>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Animi veritatis quod asperiores in
				magni iure, culpa nemo voluptate consectetur voluptatem numquam, nam, quaerat impedit?
				Recusandae facere iure delectus quam neque?
			</p>
		</svelte:fragment>
	</Block>

	<HLine width="w-1/4  mx-auto" thickness={4} />

	<Block id="document">
		<svelte:fragment>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Animi veritatis quod asperiores in
				magni iure, culpa nemo voluptate consectetur voluptatem numquam, nam, quaerat impedit?
				Recusandae facere iure delectus quam neque?
			</p>
		</svelte:fragment>
	</Block>

	<HLine width="w-1/4  mx-auto" thickness={4} />

	<Block id="process">
		<svelte:fragment>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Animi veritatis quod asperiores in
				magni iure, culpa nemo voluptate consectetur voluptatem numquam, nam, quaerat impedit?
				Recusandae facere iure delectus quam neque?
			</p>
		</svelte:fragment>
	</Block>

	<HLine width="w-1/4  mx-auto" thickness={4} />

	<div class="">
		<h2>IDEAS</h2>
		<p>Timeout: Define a timeout period, after which the task is marked as failure.</p>
		<p>Recover from failures: Recover only the failed tasks</p>
		<p>Create new Pipeline from copy of existing one</p>
	</div>
</div>

<a
	href="/documentation#introduction"
	class="btn-icon variant-filled-primary dark:variant-soft-primary rounded-full fixed bottom-8 right-8 z-50"
>
	<Fa icon={faArrowUp} size="lg" />
</a>

<script lang="ts">
	import { faArrowUp } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import Block from '../components/Block.svelte'
	import HLine from '$lib/svelte/widgets/navigation/HLine.svelte'
</script>

<div class="space-y-16 scroll-smooth">
	<Block id="pipeline">
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

	<Block id="event">
		<svelte:fragment>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Animi veritatis quod asperiores in
				magni iure, culpa nemo voluptate consectetur voluptatem numquam, nam, quaerat impedit?
				Recusandae facere iure delectus quam neque?
			</p>
		</svelte:fragment>
	</Block>
</div>

<a
	href="/documentation#introduction"
	class="btn-icon variant-filled-primary dark:variant-soft-primary rounded-full fixed bottom-8 right-8 z-50"
>
	<Fa icon={faArrowUp} size="lg" />
</a>

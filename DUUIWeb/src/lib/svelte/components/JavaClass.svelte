<script lang="ts">
	export let name: string
	export let packageName: string

	export let members: Variable[]
	export let methods: Method[]
	export let isInterface: boolean = true
</script>

<div class="section-wrapper mx-auto text-sm ">
	<div class="dark:bg-surface-900 bg-surface-200/20 variant-glass">
		<div class="p-4 flex items-start gap-4 justify-between">
			<div>
				<p class="font-bold text-xl">{name}</p>
				<p class="italic hidden md:block">{packageName}</p>
			</div>
			{#if isInterface}
				<p class="badge variant-soft-success">Interface</p>
			{:else}
				<p class="badge variant-soft-primary">Class</p>
			{/if}
		</div>
		<div class="border-b p-4 border-surface-200 dark:border-surface-500">
			<slot name="description" />
		</div>
	</div>
	{#if members}
		<div class="grid grid-cols-3">
			{#each members as member}
				<div>
					<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
						<p class="font-bold">Name</p>
						<p>{member.name}</p>
					</div>
					<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
						<p class="font-bold">Type</p>
						<p>{member.type}</p>
					</div>
				</div>
			{/each}
		</div>
	{/if}

	{#if methods}
		<div class="[&>*:not(:last-child)]:border-b dark:bg-surface-500/20">
			{#each methods as method}
				<div class="py-2 px-4 border-surface-200 dark:border-surface-500">
					<!-- <pre>{JSON.stringify(method, null, 2)}</pre> -->
					<div class="flex items-start gap-4 justify-between py-2">
						<p class="font-bold text-lg">
							{method.name}
							<span>({method.args.map((arg) => arg.type + ' ' + arg.name).join(', ')})</span>
						</p>
						<p class="badge variant-soft-tertiary">{method.returns || 'void'}</p>
					</div>
					<div class="md:flex space-y-1 md:space-y-0 items-start gap-16 py-1">
						<p class="flex-1 text-sm border-surface-200/50">
							{method.description}
						</p>
						<p>Throws <span class="italic">{method.throws.join(', ')}</span></p>
					</div>
				</div>
			{/each}
		</div>
	{/if}
</div>

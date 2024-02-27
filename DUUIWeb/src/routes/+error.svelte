<script lang="ts">
	import { page } from '$app/stores'
</script>

<div class="h-full flex items-center justify-center">
	<div
		class="rounded-md hidden sm:block container max-w-screen-sm
	 		   mx-auto border border-surface-200 dark:border-surface-500 shadow-md isolate p-4 pr-12"
	>
		<div class="flex gap-4 items-center">
			<p class="text-4xl font-bold">{$page.status}</p>
			<p>{$page.error?.message}</p>
		</div>
		<p class="font-bold">
			It seems there has been an error on the Server when trying to reach <span
				>{$page.url.pathname}</span
			>.
		</p>
	</div>
</div>

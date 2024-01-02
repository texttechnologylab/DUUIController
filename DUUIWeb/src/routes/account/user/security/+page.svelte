<script lang="ts">
	import { goto, invalidateAll } from '$app/navigation'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import TimelineV2 from '$lib/svelte/widgets/timeline/TimelineV2.svelte'
	import { makeApiCall, Api } from '$lib/utils/api'
	import { faUserMinus } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, type ModalSettings } from '@skeletonlabs/skeleton'

	const modalStore = getModalStore()

	const deleteAccount = async (event) => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: 'Delete Account',
					body: `Are you sure you want to delete your account?`
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return

			await fetch(event.target.action, {
				method: 'POST',
				body: new FormData()
			})

			await makeApiCall(Api.Logout, 'PUT', {})
			goto('/', { invalidateAll: true })
		})
	}
</script>

<section class="space-y-8">
	<p>
		Lorem ipsum dolor sit, amet consectetur adipisicing elit. Molestiae ipsum tenetur quae
		voluptatum, animi cumque porro, error obcaecati eveniet est officia, fugit facilis ullam
		nesciunt modi magni iste quos. Voluptate.
	</p>
</section>

<form action="?/deleteAccount" on:submit|preventDefault={deleteAccount}>
	<ActionButton
		text="Delete Account"
		icon={faUserMinus}
		variant="variant-filled-error dark:variant-soft-error"
	/>
</form>

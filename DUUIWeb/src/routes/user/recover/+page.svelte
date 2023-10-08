<script lang="ts">
	let email: string = "";
</script>

<svelte:head>
    <title>Recover</title> 
</svelte:head>

<div class="card rounded-md shadow-lg p-4 space-y-8">
	<h2 class="h2">Forgot Password</h2>
	<form method="POST" class="space-y-8">
		<label class="label">
			<span>E-Mail</span>
			<input class="input border-2 focus-within:outline-primary-400" type="text" name="email" bind:value={email} />
		</label>
		<button type="submit" class="btn variant-filled-primary" disabled='{email === ""}'>Request new password</button>
	</form>
</div>

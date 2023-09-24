<script lang="ts">
</script>

<svelte:head>
    <title>Recover</title> 
</svelte:head>

<div class="shadow-lg p-16 rounded-md card variant-form-material space-y-8">
	<h2 class="h3">Forgot Password</h2>
	<form method="POST" class="space-y-8">
		<label class="label">
			<span>E-Mail</span>
			<input class="input border-2 focus-within:outline-primary-400" type="text" name="email" />
		</label>
		<button type="submit" class="btn variant-filled-primary">Request new password</button>
	</form>
</div>

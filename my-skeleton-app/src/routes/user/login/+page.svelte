<script lang="ts">
</script>

<h2 class="h2">Login</h2>
<form method="POST" class="space-y-4">
	<label class="label">
		<span>E-Mail</span>
		<input class="input focus-within:outline-primary-400" type="text" />
	</label>
	<label class="label">
		<span>Password</span>
		<input class="input focus-within:outline-primary-400" type="password" />
	</label>

	<div class="space-y-4 pt-8">
		<div class="flex justify-between">
			<button type="submit" class="btn variant-filled-primary"> Login</button>
			<a href="/user/forgot-password" class="btn p-0 text-secondary-500"
				>Forgot Password?</a
			>
		</div>
		<p class="btn flex items-start">
			Don't have an account?<br /><a href="/user/register" class="text-secondary-500">
				Register</a
			>
		</p>
	</div>
</form>

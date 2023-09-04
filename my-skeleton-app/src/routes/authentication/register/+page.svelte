<script lang="ts">
</script>

<h2 class="h3">Register</h2>

<form method="POST" class="space-y-4 px-8 py-4">
	<label class="label">
		<span>E-Mail</span>
		<input class="input focus-within:outline-primary-400" type="text" />
	</label>
	<label class="label">
		<span>Password</span>
		<input class="input focus-within:outline-primary-400" type="password" />
	</label>
	<label class="label">
		<span>Confirm Password</span>
		<input class="input focus-within:outline-primary-400" type="password" />
	</label>
	<div class="space-y-4 pt-8">
		<button type="submit" class="btn variant-filled-primary"> Register</button>
		<p class="btn flex items-start">
			Already have an account?<br /><a href="/authentication/login" class="text-secondary-500">
				Login</a
			>
		</p>
	</div>
</form>

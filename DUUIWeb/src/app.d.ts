// See https://kit.svelte.dev/docs/types#app
// for information about these interfaces
// and what to do when importing types
declare namespace App {
	interface Locals {
		user: {
			preferences: any
			connections: any
			oid: string
			email: string
			role: string
			key: string
			minio: {
				access_key: string | null
				secret_key: string | null
				endpoint: string | null
			}
		}
	}
	// interface PageData {}
	// interface Error {}
	// interface Platform {}
}

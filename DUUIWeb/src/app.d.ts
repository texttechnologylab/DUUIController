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
			authorization: string
		}
	}
	// interface PageData {}
	// interface Error {}
	// interface Platform {}
}

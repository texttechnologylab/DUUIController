// See https://kit.svelte.dev/docs/types#app
// for information about these interfaces
// and what to do when importing types
declare namespace App {
	interface Locals {
		user: User
	}
}

type User = UserProperties | null | undefined

interface UserProperties {
	oid: string
	email?: string
	password?: string
	role: 'None' | 'User' | 'Admin' | 'Trial' | 'System'
	session: string
	expires?: string
	connections: {
		key: string
		dropbox: {
			access_token: string | null
			refresh_token: string | null
		}
		minio: {
			endpoint: string | null
			access_key: string | null
			secret_key: string | null
		}
		mongoDB: {
			uri: string | null
			host: string | null
			port: string | null
			username: string | null
			password: string | null
		}
	}
}

interface UserConnections {
	dropbox: boolean
	minio: boolean
}

type PaginationSettings = {
	page: number = 0
	limit: number = 10
	total: number
	sizes: number[] = [5, 10, 20, 50]
}

type Sort = {
	by: number
	order: 1 | -1
}

type ServiceStatus = 'starting' | 'active' | 'stopping' | 'inactive'

type Locale = Language | undefined

interface Language {
	name: string
	code: string
	data: {}
}

type Variable = {
	visibility?: string
	name: string
	type: string
}

type Method = {
	visibility?: string
	name: string
	description: string
	args: Variable[]
	throws: string[]
	returns?: string
}

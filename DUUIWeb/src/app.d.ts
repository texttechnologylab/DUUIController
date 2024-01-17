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
	role: 'user' | 'admin' | 'trial' | 'none'
	session: string
	expires?: string
	key?: string
	dropbox?: {
		access_token?: string
		refresh_token?: string
	}
	minio?: {
		endpoint: string
		access_key: string
		secret_key: string
	}
	mongoDBConnectionURI: string
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

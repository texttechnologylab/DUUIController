// See https://kit.svelte.dev/docs/types#app
// for information about these interfaces
// and what to do when importing types
declare namespace App {
	interface Locals {
		user: User
	}
}

// Types related to a User
type User = UserProperties | null | undefined

interface UserProperties {
	oid: string
	name?: string
	email?: string
	password?: string
	role: 'None' | 'User' | 'Admin' | 'Trial' | 'System'
	session: string
	expires?: string
	preferences: {
		tutorial: boolean
		step: number
	}
	connections: {
		key: string | null
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
		nextcloud?: {
			uri: string | null
			username: string | null
			password: string | null
		}
		google?: {
			access_token: string | null
			refresh_token: string | null
		}
	}
}

// Types related to the pagination of tables
type PaginationSettings = {
	page: number = 0
	limit: number = 20
	total: number
	sizes: number[] = [5, 10, 20, 50]
}

enum Order {
	Ascending = 1,
	Descending = -1
}

type Sort = {
	index: number
	order: Order
}

// Types related to the documentation of the api at /documentation/api
type AggregationStep = { _id: string; count: number[] }
type AggreationResult = AggreationStep[]

type APIMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'
type ParameterType = 'Query' | 'Body'
type APIParameter = { name: string; type: ParameterType; description: string }

type APIEndpoint = {
	method: APIMethod
	route: string
	returns: { code: number; message: string }[]
	parameters: APIParameter[]
	description: string
	exampleRequest: string = ''
}

// Types related to Feedback

type Score = 1 | 2 | 3 | 4 | 5 | 6 | 7

type Feedback = {
	name: string
	message: string
	step: number
	language: 'german' | 'english'
	programming: number
	nlp: number
	nlpNeeded: boolean
	duui: boolean
	duuiRating: number
	java: number
	python: number
	requirements: number
	frustration: number
	ease: number
	correction: number
}

type FeedbackResult = {
	programming: number
	nlp: number
	nlpNeeded: boolean
	duui: boolean
	duuiRating: number
	java: number
	python: number
	requirements: number
	frustration: number
	ease: number
	correction: number
	timestamp: number
}

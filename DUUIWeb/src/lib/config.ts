const ubuntu = false

export const API_URL = ubuntu ? 'http://192.168.2.165:2605' : 'http://192.168.2.122:2605' // : 'http://169.254.222.47:2605'

export interface _Object {
	[key: string]: any
}

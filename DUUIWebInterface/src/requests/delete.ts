import { server_address } from '../lib/api/config';

export async function deletePipeline(id: string) {
    
	fetch(server_address + '/pipeline/' + id, {
		method: 'DELETE',
		mode: 'cors'
	})
		.then((response) => {
			return response.text();
		})
		.catch((error) => {
			console.log(error);
			return '[Error]: ' + error;
		});
}

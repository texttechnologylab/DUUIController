import { redirect } from '@sveltejs/kit'
import { API_URL, SECRET_CLIENT_ID, SECRET_CLIENT_SECRET } from '$env/static/private'
import type { PageServerLoad } from './$types'
import { OAuth2Client } from 'google-auth-library'


async function getUserData(access_token) {

	const response = await fetch(`https://www.googleapis.com/oauth2/v3/userinfo?access_token=${access_token}`);
	console.log('response',response);
	const data = await response.json();
	console.log('data',data);
}


export const GET = async ({ url }) => {
	const redirectURL = 'http://localhost:5173/auth/googledrive/';
	console.log("asdfsdfasdf")
	const code = await url.searchParams.get('code')

	try {
		const oAuth2Client = new  OAuth2Client(
			SECRET_CLIENT_ID,
			SECRET_CLIENT_SECRET,
			redirectURL);

		const r = await oAuth2Client.getToken(code ? code : "");
		oAuth2Client.setCredentials(r.tokens);
	} catch (err) {
		console.error("Error logging: ", err);
	}

	throw redirect(303, "/account?tab=1");
}

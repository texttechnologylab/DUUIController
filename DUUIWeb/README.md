# Structure

A SvelteKit App is split into the two parts lib and routes.

## Lib

The lib folder contains functions, types and interface definitions used throughout the interface. Additionally Svelte components (reusable blocks of html and javascript) are stored in `/lib/svelte/components`.

## Routes

The folder structure in routes represents the web available web pages. The api folder acts a sort of proxy api between the client and Java Backend. Requests are sent from the `+page.svelte` files to corresponding `+server.ts` files in the api folder. These server files communicate with the Backend and return the data to the page.

Most `+page.svelte` files are complemented by a `+page.server.ts` file that initially loads data for the page to display.
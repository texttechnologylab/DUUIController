# Structure

A SvelteKit App is split into the two parts lib and routes. The website can be found at [https://duui.texttechnologylab.org](https://duui.texttechnologylab.org).

## Lib

The lib folder contains functions, types and interface definitions used throughout the interface. Additionally Svelte components (reusable blocks of html and javascript) are stored in `/lib/svelte/components`.

## Routes

The folder structure in routes represents the web available web pages. The api folder acts a sort of proxy api between the client and Java Backend. Requests are sent from the `+page.svelte` files to corresponding `+server.ts` files in the api folder. These server files communicate with the Backend and return the data to the page.

Most `+page.svelte` files are complemented by a `+page.server.ts` file that initially loads data for the page to display.

## Style and Theme

The basis for styles and themes used in the web interface is [Skeleton](https://www.skeleton.dev) and [tailwindcss](https://tailwindcss.com/). Skeleton is used to define themes and apply basic styling to html elements. tailwindcss is a layer on top of the base styles to further customize the visual representation of elements.

## Running

Before the app can be run make sure to follow these steps:

- Open the console with the current working directory set to .../DUUIWeb
- Run `npm install`
- Run `npm run dev -- --open --host`
- Follow the instructions by Vite in the console

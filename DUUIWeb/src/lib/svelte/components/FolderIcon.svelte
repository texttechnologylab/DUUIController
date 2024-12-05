<script>
    import Fa from 'svelte-fa'
    import { faFolder} from '@fortawesome/free-solid-svg-icons'

    export let icon = faFolder; // Prop to receive the icon name (e.g., "folder")
    export let size = '1em'; // Optional prop for icon size (default: 1em)

    // Import specific icons as needed (optional, for better performance)
    // import faStar from '@fortawesome/free-solid-svg-icons/faStar';
    // import faCog from '@fortawesome/free-solid-svg-icons/faCog';
</script>

<Fa icon="{icon}" size="{size}" />
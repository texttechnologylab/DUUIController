<script lang="ts">
	import type { DUUIPipelineComponent } from "$lib/data"

    export let component: DUUIPipelineComponent;


</script>


<div class="container">
    <p>{component.name}</p>
    <p>{component.driver}</p>
    <p>{component.target}</p>
</div>
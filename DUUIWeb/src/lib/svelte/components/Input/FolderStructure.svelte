<!--
	@component
	A FolderStructure component where one option can be selected.
-->

<script lang="ts">
    import { equals } from '$lib/duui/utils/text'
    import type { Placement } from '@floating-ui/dom'
    import {faCheck, faFolder, faChevronDown, type IconDefinition} from '@fortawesome/free-solid-svg-icons'
    import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
    import { RecursiveTreeView, type TreeViewNode } from '@skeletonlabs/skeleton'
    import FolderIcon from "$lib/svelte/components/FolderIcon.svelte";

    import Fa from 'svelte-fa'
    import type {ComponentType} from "svelte";

    export let label: string = ''
    export let name: string = label
    export let isMultiple = false

    export let value: string | number = ""

    export let placement: Placement = 'bottom-start'

    export let offset: number = 4

    export let tree: TreeViewNode

    // export let style: string = 'input-wrapper'
    // export let rounded: string = 'rounded-md'
    // export let border: string = 'border'
    // export let textAlign: string = 'text-start'

    let icon: IconDefinition = faFolder

    const dropdown: PopupSettings = {
        event: 'click',
        target: name,
        placement: placement,
        closeQuery: '.listbox-item',
        middleware: {
            offset: offset
        }
    }

    let checkedNodes : string[] = []
    let prevNode: string = ""
    let indeterminateNodes: string[] = []

    let myTreeViewNodes: TreeViewNode[] = [tree]
    let addFolderIcon = (node: TreeViewNode, height: number) => {
        node.lead = FolderIcon
        if (node.children) {
            for (let child of node.children) {
                addFolderIcon(child, height + 1)
            }
        }
    }

    for (let node of myTreeViewNodes) {
        addFolderIcon(node, 1)

    }

    let getNode = (nodes: TreeViewNode[], id: string) => {
        let getNode1 = (node: TreeViewNode) => {
            if (node.id === id) return node;

            if (node.children) {
                for (let child of node.children) {
                    let result = getNode1(child)
                    if (result) return result
                }
            }
            return null;
        }

        for (let node of nodes) {
            let result = getNode1(node)
            if (result) return result
        }
        return null;
    }

    let displayCheckedNodes = (nodes: string[]) => {
        if (isMultiple) {
            for (let i = 0; i < indeterminateNodes.length; i++) {
                let n = indeterminateNodes[i]
                let k = checkedNodes.indexOf(n)
                if (k > -1) {
                    checkedNodes.splice(k, 1)
                }
            }

            value = checkedNodes.map((x) => getNode(myTreeViewNodes, x).content).join(", ")
        } else {
            if (checkedNodes.length > 0) {
                if (checkedNodes.length > 1 && equals(prevNode, checkedNodes[0])) checkedNodes.shift()
                if (checkedNodes.length > 1 && equals(prevNode, checkedNodes[1])) checkedNodes.pop()
                value = getNode(myTreeViewNodes, checkedNodes[0]).content
                prevNode = checkedNodes[0]
            }
        }

        return value;
    }

</script>

<div class="label flex flex-col md:min-w-[220px]">
    {#if label}
        <span class="form-label text-start">{label} </span>
    {/if}
    <button
            class="flex items-center !justify-between gap-2 px-3 py-2 leading-6 border rounded-md input-wrapper"
            use:popup={dropdown}
    >
        <span>{displayCheckedNodes(checkedNodes)}</span>

        <Fa {icon} />
    </button>
</div>
<div data-popup={name}>
    <div class="popup-solid p-1 md:min-w-[768px]">
        <RecursiveTreeView
            selection
            multiple={isMultiple}
            relational={false}
            bind:checkedNodes={checkedNodes}
            bind:indeterminateNodes={indeterminateNodes}

            nodes={myTreeViewNodes}

        />
    </div>

</div>

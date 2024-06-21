<!--
	@component
	A FolderStructure component where one option can be selected.
-->

<script lang="ts">
    import { equals, toTitleCase } from '$lib/duui/utils/text'
    import type { Placement } from '@floating-ui/dom'
    import {faCheck, faFolder, faChevronDown, type IconDefinition} from '@fortawesome/free-solid-svg-icons'
    import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'
    import { TreeView, TreeViewItem, RecursiveTreeView, type TreeViewNode } from '@skeletonlabs/skeleton'
    import FolderIcon from "$lib/svelte/components/FolderIcon.svelte";

    import Fa from 'svelte-fa'
    import type {ComponentType} from "svelte";

    export let label: string = ''
    export let name: string = label
    export let isMultiple = false

    export let options: string[] | number[] = []
    export let value: string | number = ""

    export let placement: Placement = 'bottom-start'

    export let offset: number = 4

    export let style: string = 'input-wrapper'
    export let rounded: string = 'rounded-md'
    export let border: string = 'border'
    export let textAlign: string = 'text-start'

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
    let indeterminateNodes: string[] = []

    let myTreeViewNodes: TreeViewNode[] = [
        {
            id: '1',
            content: 'Folder 1',
            children: [
                {
                    id: '2',
                    content: 'Folder 2'
                },
                {
                    id: '3',
                    content: 'Folder 3'
                }
            ]
        },
        {
            id: '4',
            content: 'Folder 5'
        }
    ]
    // myTreeViewNodes[0]["unique-id"].children[0].lead = FolderIcon

    let addFolderIcon = (node: TreeViewNode) => {
        node.lead = FolderIcon
        if (node.children) {
            for (let child of node.children) {
                addFolderIcon(child)
            }
        }
    }

    for (let node of myTreeViewNodes) {
        addFolderIcon(node)
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
        let filtered = checkedNodes.filter(x => !getNode(myTreeViewNodes, x)?.children || !isMultiple)
        return filtered.map((x) => " " + getNode(myTreeViewNodes, x).content)
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
            multiple={isMultiple}
            relational={true}
            bind:checkedNodes={checkedNodes}
            bind:indeterminateNodes={indeterminateNodes}
            nodes={myTreeViewNodes}

        />
    </div>

</div>

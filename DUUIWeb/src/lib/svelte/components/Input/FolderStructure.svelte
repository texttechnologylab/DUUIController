<!--
	@component
	A Dropdown component where one option can be selected.
-->

<script lang="ts">
    import { equals, toTitleCase } from '$lib/duui/utils/text'
    import type { Placement } from '@floating-ui/dom'
    import { faCheck, faChevronDown, type IconDefinition } from '@fortawesome/free-solid-svg-icons'
    import { TreeView, TreeViewItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'
    import Fa from 'svelte-fa'

    export let label: string = ''
    export let name: string = label

    // export let options: string[] | number[]
    export let selectedItem: string = ""

    function toggleSelectable(e, item) {
        selectedItem = item;
    }
</script>

<TreeView>
    <TreeViewItem
        selectable
        selected={selectedItem === 'images'}
        label="images"
        iconF7="folder_fill"
        onClick={(e) => toggleSelectable(e, 'images')}
    >
        <TreeViewItem
            selectable
            selected={selectedItem === 'avatar.png'}
            label="avatar.png"
            iconF7="photo_fill"
            onClick={(e) => toggleSelectable(e, 'avatar.png')}
        />
        <TreeViewItem
            selectable
            selected={selectedItem === 'background.jpg'}
            label="background.jpg"
            iconF7="photo_fill"
            onClick={(e) => toggleSelectable(e, 'background.jpg')}
        />
    </TreeViewItem>
    <TreeViewItem
        selectable
        selected={selectedItem === '.gitignore'}
        label=".gitignore"
        iconF7="logo_github"
        onClick={(e) => toggleSelectable(e, '.gitignore')}
    />
    <TreeViewItem
        selectable
        selected={selectedItem === 'index.html'}
        label="index.html"
        iconF7="doc_text_fill"
        onClick={(e) => toggleSelectable(e, 'index.html')}
    />
</TreeView>

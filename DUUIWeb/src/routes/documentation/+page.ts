// ;<JavaClass
// 	name="IDUUIDocumentHandler"
// 	members={[]}
// 	methods={[
// 		{
// 			name: 'writeDocument',
// 			throws: ['IOExcetion'],
// 			args: [
// 				{
// 					type: 'DUUIDocument',
// 					name: 'document'
// 				},
// 				{
// 					type: 'String',
// 					name: 'path'
// 				}
// 			],
// 			description:
// 				'Writes one document to the target location. Should be the full path in the storage.'
// 		},
// 		{
// 			name: 'writeDocuments',
// 			throws: ['IOExcetion'],
// 			args: [
// 				{
// 					type: 'List<DUUIDocument>',
// 					name: 'documents'
// 				},
// 				{
// 					type: 'String',
// 					name: 'path'
// 				}
// 			],
// 			description:
// 				'Writes multiple documents to the target location. Should be the full path to a folder or bucket.'
// 		},
// 		{
// 			name: 'readDocument',
// 			returns: 'DUUIDocument',
// 			throws: ['IOExcetion'],
// 			args: [
// 				{
// 					type: 'String',
// 					name: 'path'
// 				}
// 			],
// 			description: 'Reads one document from the source location. Should be the full path.'
// 		},
// 		{
// 			name: 'readDocuments',
// 			returns: 'List<DUUIDocument>',
// 			throws: ['IOExcetion'],
// 			args: [
// 				{
// 					type: 'List<String>',
// 					name: 'paths'
// 				}
// 			],
// 			description:
// 				'Reads multiple documents from the source location. Should be the full to a folder or bucket.'
// 		},
// 		{
// 			name: 'listDocuments',
// 			returns: 'List<String>',
// 			throws: ['IOExcetion'],
// 			args: [
// 				{
// 					type: 'String',
// 					name: 'path'
// 				},
// 				{
// 					type: 'String',
// 					name: 'fileExtension'
// 				},
// 				{
// 					type: 'boolean',
// 					name: 'recursive'
// 				}
// 			],
// 			description:
// 				'Reads multiple documents from the source location. Should be the full to a folder or bucket.'
// 		}
// 	]}
// 	packageName="org.texttechnologylab.DockerUnifiedUIMAInterface.data_reader"
// >
// 	<svelte:fragment slot="description">
// 		Handles the IO operations for reading and writing from and to different cloud or local services.
// 		This interface enforces the implementation of 6 methods.
// 	</svelte:fragment>
// </JavaClass>

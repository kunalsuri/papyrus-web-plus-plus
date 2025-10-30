INSERT INTO project (
	id,
	name,
	created_on,
	last_modified_on)
	 VALUES (
		'c87b6af4-7f3b-4802-9497-94bc5219d5f6',
		'UML',
		'2024-10-03 15:49:21.165084+02',
		'2024-10-03 15:49:21.165084+02');

INSERT INTO nature (
	project_id,
	name) VALUES (
		'c87b6af4-7f3b-4802-9497-94bc5219d5f6',
		'papyrusweb://nature?kind=uml'
		);

INSERT INTO semantic_data (id,created_on,last_modified_on) VALUES
	 ('cbf34577-d7fb-4d12-a096-27d0d1f9164e','2024-10-03 15:49:21.789378+02','2024-10-03 15:50:47.926621+02');

INSERT INTO project_semantic_data (id,project_id,semantic_data_id,name,created_on,last_modified_on) VALUES
	 ('e74318a6-a0be-4366-95da-6d2a41850a7f','c87b6af4-7f3b-4802-9497-94bc5219d5f6','cbf34577-d7fb-4d12-a096-27d0d1f9164e','main','2024-10-03 15:49:21.789378+02','2024-10-03 15:50:47.926621+02');


INSERT INTO semantic_data_domain (semantic_data_id,uri) VALUES
	 ('cbf34577-d7fb-4d12-a096-27d0d1f9164e','http://www.eclipse.org/uml2/5.0.0/UML/Profile/Standard'),
	 ('cbf34577-d7fb-4d12-a096-27d0d1f9164e','http://www.eclipse.org/uml2/5.0.0/UML');


INSERT INTO document (id,semantic_data_id,name,"content",is_read_only,created_on,last_modified_on) VALUES
	 ('b6270893-90c6-4184-9d2c-235c0d5b53b4','cbf34577-d7fb-4d12-a096-27d0d1f9164e','Model.uml','{
  "json": { "version": "1.0", "encoding": "utf-8" },
  "ns": {
    "standard": "http://www.eclipse.org/uml2/5.0.0/UML/Profile/Standard",
    "uml": "http://www.eclipse.org/uml2/5.0.0/UML"
  },
  "content": [
    {
      "id": "8f568fdc-f688-4ecf-8e5f-9a1ed1a5fc6d",
      "eClass": "uml:Model",
      "data": {
        "name": "Model",
        "packageImport": [
          {
            "id": "159f422d-051e-4c98-a161-7dcbc5834f60",
            "eClass": "uml:PackageImport",
            "data": {
              "importedPackage": "uml:Model pathmap://UML_LIBRARIES/UMLPrimitiveTypes.library.uml#_0"
            }
          }
        ],
        "packagedElement": [
          {
            "id": "ff236757-fcd6-418b-8bf4-bd087adbf988",
            "eClass": "uml:Class",
            "data": {
              "name": "Class1",
              "isAbstract": "true",
              "ownedAttribute": [
                {
                  "id": "7a2ec924-da0b-4b7f-b42c-81b524f393a0",
                  "eClass": "uml:Property",
                  "data": { "name": "prop1", "isStatic": "true" }
                }
              ]
            }
          }
        ],
        "profileApplication": [
          {
            "id": "a5201902-d96a-4304-a16d-d8fbf5854c58",
            "eClass": "uml:ProfileApplication",
            "data": {
              "eAnnotations": [
                {
                  "source": "http://www.eclipse.org/uml2/2.0.0/UML",
                  "references": [
                    "http://www.eclipse.org/uml2/5.0.0/UML/Profile/Standard#/"
                  ]
                }
              ],
              "appliedProfile": "uml:Profile pathmap://UML_PROFILES/Standard.profile.uml#_0"
            }
          }
        ]
      }
    },
    {
      "id": "5f941300-1841-4f76-bdc9-e17202e218f8",
      "eClass": "standard:Type",
      "data": { "base_Class": "/0/Class1" }
    }
  ]
}
', false, '2024-10-03 15:50:47.926621+02','2024-10-03 15:50:47.926621+02');


INSERT INTO representation_metadata (
    id,
    semantic_data_id,
    target_object_id,
    description_id,
    "label",
    documentation,
    kind,
    created_on,
    last_modified_on)
VALUES (
    'dc0083d6-233a-4523-b06a-6c62c9baf3a4',
    'cbf34577-d7fb-4d12-a096-27d0d1f9164e',
    '8f568fdc-f688-4ecf-8e5f-9a1ed1a5fc6d',
    '',
    'siriusComponents://representationDescription?kind=diagramDescription&sourceKind=view&sourceId=fe67a192-1679-3290-92ff-cf77016f6aa2&sourceElementId=a8c63f96-b6a9-387d-942f-4b0a54f8bfed',
    'Root Package Diagram',
    'siriusComponents://representation?type=Diagram',
    '2024-10-03 15:49:25.617986+02',
    '2024-10-03 15:49:29.482887+02'
 );

INSERT INTO representation_content (
    id,
    content,
    last_migration_performed,
    migration_version,
    created_on,
    last_modified_on)
VALUES (
    'dc0083d6-233a-4523-b06a-6c62c9baf3a4',
    '{"id":"dc0083d6-233a-4523-b06a-6c62c9baf3a4","kind":"siriusComponents://representation?type=Diagram","targetObjectId":"8f568fdc-f688-4ecf-8e5f-9a1ed1a5fc6d","descriptionId":"siriusComponents://representationDescription?kind=diagramDescription&sourceKind=view&sourceId=fe67a192-1679-3290-92ff-cf77016f6aa2&sourceElementId=a8c63f96-b6a9-387d-942f-4b0a54f8bfed","label":"Root Package Diagram","nodes":[{"id":"aa34a779-bea0-3a4c-9c9b-502d9c3cc1b0","type":"customnode:package","targetObjectId":"8f568fdc-f688-4ecf-8e5f-9a1ed1a5fc6d","targetObjectKind":"siriusComponents://semantic?domain=uml&entity=Model","targetObjectLabel":"Model","descriptionId":"siriusComponents://nodeDescription?sourceKind=view&sourceId=fe67a192-1679-3290-92ff-cf77016f6aa2&sourceElementId=ba3a7b65-91f3-3496-9504-4d112e95a997","borderNode":false,"modifiers":[],"state":"Normal","collapsingState":"EXPANDED","insideLabel":{"id":"7bf15eb9-bb55-3de6-b86b-3ae3a9ebc315","text":"Model","insideLabelLocation":"TOP_CENTER","style":{"color":"#0b006b","fontSize":14,"bold":false,"italic":false,"underline":false,"strikeThrough":false,"iconURL":["/icons-override/full/obj16/Model.svg"],"background":"transparent","borderColor":"black","borderSize":0,"borderRadius":3,"borderStyle":"Solid","maxWidth":null},"isHeader":false,"headerSeparatorDisplayMode":"NEVER","overflowStrategy":"NONE","textAlign":"CENTER"},"outsideLabels":[],"style":{"background":"#f1f8fe","borderColor":"#0b006b","borderSize":1,"borderStyle":"Solid"},"childrenLayoutStrategy":{"kind":"FreeForm"},"borderNodes":[],"childNodes":[],"defaultWidth":300,"defaultHeight":150,"labelEditable":true,"pinned":false},{"id":"2b92b5a3-7286-3edb-83c4-268ea2e90946","type":"customnode:package","targetObjectId":"UMLPrimitiveTypes.library.uml#_0","targetObjectKind":"siriusComponents://semantic?domain=uml&entity=Model","targetObjectLabel":"PrimitiveTypes","descriptionId":"siriusComponents://nodeDescription?sourceKind=view&sourceId=fe67a192-1679-3290-92ff-cf77016f6aa2&sourceElementId=ba3a7b65-91f3-3496-9504-4d112e95a997","borderNode":false,"modifiers":[],"state":"Normal","collapsingState":"EXPANDED","insideLabel":{"id":"f36747c2-754c-3da6-aca6-c725566606e5","text":"«EPackage, ModelLibrary»\r\nPrimitiveTypes","insideLabelLocation":"TOP_CENTER","style":{"color":"#0b006b","fontSize":14,"bold":false,"italic":false,"underline":false,"strikeThrough":false,"iconURL":["/icons-override/full/obj16/Model.svg"],"background":"transparent","borderColor":"black","borderSize":0,"borderRadius":3,"borderStyle":"Solid","maxWidth":null},"isHeader":false,"headerSeparatorDisplayMode":"NEVER","overflowStrategy":"NONE","textAlign":"CENTER"},"outsideLabels":[],"style":{"background":"#f1f8fe","borderColor":"#0b006b","borderSize":1,"borderStyle":"Solid"},"childrenLayoutStrategy":{"kind":"FreeForm"},"borderNodes":[],"childNodes":[],"defaultWidth":300,"defaultHeight":150,"labelEditable":true,"pinned":false}],"edges":[{"id":"fa949109-3010-31f3-8351-92ab99553a86","type":"edge:straight","targetObjectId":"159f422d-051e-4c98-a161-7dcbc5834f60","targetObjectKind":"siriusComponents://semantic?domain=uml&entity=PackageImport","targetObjectLabel":"","descriptionId":"siriusComponents://edgeDescription?sourceKind=view&sourceId=fe67a192-1679-3290-92ff-cf77016f6aa2&sourceElementId=eb2f031e-2fcd-381d-a07a-bb2082c4e43d","beginLabel":null,"centerLabel":{"id":"ae176999-2c6d-3ea2-ae1d-caa7caffcd99","type":"label:edge-center","text":"«import»","style":{"color":"#0b006b","fontSize":14,"bold":false,"italic":false,"underline":false,"strikeThrough":false,"iconURL":[],"background":"transparent","borderColor":"black","borderSize":0,"borderRadius":3,"borderStyle":"Solid","maxWidth":null}},"endLabel":null,"sourceId":"aa34a779-bea0-3a4c-9c9b-502d9c3cc1b0","targetId":"2b92b5a3-7286-3edb-83c4-268ea2e90946","modifiers":[],"state":"Normal","style":{"size":1,"lineStyle":"Dash","sourceArrow":"None","targetArrow":"InputArrow","color":"#0b006b"},"centerLabelEditable":true}],"layoutData":{"nodeLayoutData":{"aa34a779-bea0-3a4c-9c9b-502d9c3cc1b0":{"id":"aa34a779-bea0-3a4c-9c9b-502d9c3cc1b0","position":{"x":0.0,"y":0.0},"size":{"width":300.0,"height":150.0},"resizedByUser":false},"2b92b5a3-7286-3edb-83c4-268ea2e90946":{"id":"2b92b5a3-7286-3edb-83c4-268ea2e90946","position":{"x":320.0,"y":0.0},"size":{"width":300.0,"height":150.0},"resizedByUser":false}},"edgeLayoutData":{},"labelLayoutData":{}}}',
    'none',
    '2024.5.4-202407040900',
    '2024-10-03 15:49:25.617986+02',
    '2024-10-03 15:49:29.482887+02'
);

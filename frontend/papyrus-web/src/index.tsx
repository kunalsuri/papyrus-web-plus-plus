/*******************************************************************************
 * Copyright (c) 2019, 2025 CEA LIST, Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *     Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 210
 *******************************************************************************/
import { loadDevMessages, loadErrorMessages } from '@apollo/client/dev';
import { ExtensionRegistry } from '@eclipse-sirius/sirius-components-core';
import {
  DiagramPaletteToolContributionProps,
  diagramPaletteToolExtensionPoint,
  EdgeData,
  NodeData,
  NodeTypeContribution,
} from '@eclipse-sirius/sirius-components-diagrams';
import {
  ApolloClientOptionsConfigurer,
  apolloClientOptionsConfigurersExtensionPoint,
  DefaultExtensionRegistryMergeStrategy,
  DiagramRepresentationConfiguration,
  footerExtensionPoint,
  navigationBarIconExtensionPoint,
  navigationBarMenuHelpURLExtensionPoint,
  NodeTypeRegistry,
  SiriusWebApplication,
} from '@eclipse-sirius/sirius-web-application';
import { forkRegistry } from '@eclipse-sirius/sirius-web-view-fork';
import FormatListBulletedIcon from '@mui/icons-material/FormatListBulleted';
import { httpOrigin, wsOrigin } from './core/URL';
import { CuboidNode } from './nodes/cuboid/CuboidNode';
import { CuboidNodeConverter } from './nodes/cuboid/CuboidNodeConverter';
import { CuboidNodeLayoutHandler } from './nodes/cuboid/CuboidNodeLayoutHandler';
import { CuboidNodeListConverter } from './nodes/cuboid/CuboidNodeListConverter';
import { CuboidNodeListLayoutHandler } from './nodes/cuboid/CuboidNodeListLayoutHandler';
import { CustomImageNode } from './nodes/customImage/CustomImageNode';
import { CustomImageNodeConverter } from './nodes/customImage/CustomImageNodeConverter';
import { CustomImageNodeLayoutHandler } from './nodes/customImage/CustomImageNodeLayoutHandler';
import { EllipseNode } from './nodes/ellipse/EllipseNode';
import { EllipseNodeConverter } from './nodes/ellipse/EllipseNodeConverter';
import { EllipseNodeLayoutHandler } from './nodes/ellipse/EllipseNodeLayoutHandler';
import { InnerFlagNode } from './nodes/innerFlag/InnerFlagNode';
import { InnerFlagNodeConverter } from './nodes/innerFlag/InnerFlagNodeConverter';
import { InnerFlagNodeLayoutHandler } from './nodes/innerFlag/InnerFlagNodeLayoutHandler';
import { NoteNode } from './nodes/note/NoteNode';
import { NoteNodeConverter } from './nodes/note/NoteNodeConverter';
import { NoteNodeLayoutHandler } from './nodes/note/NoteNodeLayoutHandler';
import { OuterFlagNode } from './nodes/outerFlag/OuterFlagNode';
import { OuterFlagNodeConverter } from './nodes/outerFlag/OuterFlagNodeConverter';
import { OuterFlagNodeLayoutHandler } from './nodes/outerFlag/OuterFlagNodeLayoutHandler';
import { PackageNode } from './nodes/package/PackageNode';
import { PackageNodeConverter } from './nodes/package/PackageNodeConverter';
import { PackageNodeLayoutHandler } from './nodes/package/PackageNodeLayoutHandler';
import { PackageNodeListConverter } from './nodes/package/PackageNodeListConverter';
import { PackageNodeListLayoutHandler } from './nodes/package/PackageNodeListLayoutHandler';
import { RectangleWithExternalLabelNode } from './nodes/rectangleWithExternalLabel/RectangleWithExternalLabelNode';
import { RectangleWithExternalLabelNodeConverter } from './nodes/rectangleWithExternalLabel/RectangleWithExternalLabelNodeConverter';
import { RectangleWithExternalLabelNodeLayoutHandler } from './nodes/rectangleWithExternalLabel/RectangleWithExternalLabelNodeLayoutHandler';

import {
  GQLWidget,
  PropertySectionComponent,
  widgetContributionExtensionPoint,
} from '@eclipse-sirius/sirius-components-forms';
import { treeItemContextMenuEntryExtensionPoint } from '@eclipse-sirius/sirius-components-trees';
import {
  ReferenceIcon,
  ReferencePreview,
  ReferencePropertySection,
} from '@eclipse-sirius/sirius-components-widget-reference';
import './ReactFlow.css';
import { PapyrusPopupToolContribution } from './diagram-tools/PapyrusPopupToolContribution';
import './fonts.css';
import { Footer } from './footer/Footer';
import { nodesStyleDocumentTransform } from './nodes/NodesDocumentTransform';
import './portals.css';
import { UMLModelTreeItemContextMenuContribution } from './profile/apply-profile/UMLModelTreeItemContextMenuContribution';
import { UMLElementTreeItemContextMenuContribution } from './profile/apply-stereotype/UMLElementTreeItemContextMenuContribution';
import './reset.css';
import './variables.css';
import { customWidgetsDocumentTransform } from './widgets/CustomWidgetsDocumentTransform';

import { Edge, Node } from '@xyflow/react';
import { ExtensionRegistryMergeStrategy } from './extensions/ExtensionRegistryMergeStrategy';
import { createRoot } from 'react-dom/client';
import { PapyrusNavigationBarIcon } from './core/PapyrusNavigationBarIcon';
import { PublishProfileTreeItemContextMenuContribution } from './profile/publish-profile/PublishProfileTreeItemContextMenuContribution';
import { ContainmentReferenceIcon } from './widgets/containmentReference/ContainmentReferenceIcon';
import { ContainmentReferencePreview } from './widgets/containmentReference/ContainmentReferencePreview';
import ContainmentReferenceSection from './widgets/containmentReference/ContainmentReferenceSection';
import { CustomImageIcon } from './widgets/customImage/CustomImageIcon';
import { CustomImagePreview } from './widgets/customImage/CustomImagePreview';
import { CustomImageSection } from './widgets/customImage/CustomImageSection';
import { LanguageExpressionIcon } from './widgets/languageExpression/LanguageExpressionIcon';
import { LanguageExpressionPreview } from './widgets/languageExpression/LanguageExpressionPreview';
import { LanguageExpressionSection } from './widgets/languageExpression/LanguageExpressionSection';
import { PrimitiveListWidgetPreview } from './widgets/primitiveList/PrimitiveListWidgetPreview';
import { PrimitiveListSection } from './widgets/primitiveList/PrimitiveListWidgetPropertySection';
import { PrimitiveRadioIcon } from './widgets/primitiveRadio/PrimitiveRadioIcon';
import { PrimitiveRadioPreview } from './widgets/primitiveRadio/PrimitiveRadioPreview';
import { PrimitiveRadioSection } from './widgets/primitiveRadio/PrimitiveRadioSection';

if (process.env.NODE_ENV !== 'production') {
  loadDevMessages();
  loadErrorMessages();
}

const extensionRegistry: ExtensionRegistry = new ExtensionRegistry();

/*
 * Custom node contribution
 */
const nodeTypeRegistryValue: NodeTypeRegistry = {
  nodeLayoutHandlers: [
    new EllipseNodeLayoutHandler(),
    new PackageNodeLayoutHandler(),
    new PackageNodeListLayoutHandler(),
    new RectangleWithExternalLabelNodeLayoutHandler(),
    new NoteNodeLayoutHandler(),
    new InnerFlagNodeLayoutHandler(),
    new OuterFlagNodeLayoutHandler(),
    new CuboidNodeLayoutHandler(),
    new CuboidNodeListLayoutHandler(),
    new CustomImageNodeLayoutHandler(),
  ],
  nodeConverters: [
    new EllipseNodeConverter(),
    new PackageNodeConverter(),
    new PackageNodeListConverter(),
    new RectangleWithExternalLabelNodeConverter(),
    new NoteNodeConverter(),
    new InnerFlagNodeConverter(),
    new OuterFlagNodeConverter(),
    new CuboidNodeConverter(),
    new CuboidNodeListConverter(),
    new CustomImageNodeConverter(),
  ],
  nodeTypeContributions: [
    <NodeTypeContribution component={EllipseNode} type={'ellipseNode'} />,
    <NodeTypeContribution component={PackageNode} type={'packageNode'} />,
    <NodeTypeContribution component={PackageNode} type={'packageNodeList'} />,
    <NodeTypeContribution component={RectangleWithExternalLabelNode} type={'rectangleWithExternalLabelNode'} />,
    <NodeTypeContribution component={NoteNode} type={'noteNode'} />,
    <NodeTypeContribution component={InnerFlagNode} type={'innerFlagNode'} />,
    <NodeTypeContribution component={OuterFlagNode} type={'outerFlagNode'} />,
    <NodeTypeContribution component={CuboidNode} type={'cuboidNode'} />,
    <NodeTypeContribution component={CuboidNode} type={'cuboidNodeList'} />,
    <NodeTypeContribution component={CustomImageNode} type={'customImageNode'} />,
  ],
};

// Contribution to modify GraphQl requests to handle custom node
const nodeApolloClientOptionsConfigurer: ApolloClientOptionsConfigurer = (currentOptions) => {
  const { documentTransform } = currentOptions;

  const newDocumentTransform = documentTransform
    ? documentTransform.concat(nodesStyleDocumentTransform)
    : nodesStyleDocumentTransform;
  return {
    ...currentOptions,
    documentTransform: newDocumentTransform,
  };
};

/*
 * Custom widgets contribution
 */

// Contribution to modify GraphQl requests to handle custom widgets
const widgetsApolloClientOptionsConfigurer: ApolloClientOptionsConfigurer = (currentOptions) => {
  const { documentTransform } = currentOptions;

  const newDocumentTransform = documentTransform
    ? documentTransform.concat(customWidgetsDocumentTransform)
    : customWidgetsDocumentTransform;
  return {
    ...currentOptions,
    documentTransform: newDocumentTransform,
  };
};

extensionRegistry.putData(widgetContributionExtensionPoint, {
  identifier: 'papyrus-custom-widget-primitive-list',
  data: [
    {
      name: 'PrimitiveListWidget',
      icon: <FormatListBulletedIcon />,
      previewComponent: PrimitiveListWidgetPreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        if (widget.__typename === 'PrimitiveListWidget') {
          return PrimitiveListSection;
        }
        return null;
      },
    },
    {
      name: 'LanguageExpression',
      icon: <LanguageExpressionIcon />,
      previewComponent: LanguageExpressionPreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        if (widget.__typename === 'LanguageExpression') {
          return LanguageExpressionSection;
        }
        return null;
      },
    },
    {
      name: 'ContainmentReferenceWidget',
      icon: <ContainmentReferenceIcon />,
      previewComponent: ContainmentReferencePreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        if (widget.__typename === 'ContainmentReferenceWidget') {
          return ContainmentReferenceSection;
        }
        return null;
      },
    },
    {
      name: 'PrimitiveRadio',
      icon: <PrimitiveRadioIcon />,
      previewComponent: PrimitiveRadioPreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        if (widget.__typename === 'PrimitiveRadio') {
          return PrimitiveRadioSection;
        }
        return null;
      },
    },
    {
      name: 'ReferenceWidget',
      icon: <ReferenceIcon />,
      previewComponent: ReferencePreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        let propertySectionComponent: PropertySectionComponent<GQLWidget> | null = null;

        if (widget.__typename === 'ReferenceWidget') {
          propertySectionComponent = ReferencePropertySection;
        }
        return propertySectionComponent;
      },
    },
    {
      name: 'CustomImageWidget',
      icon: <CustomImageIcon />,
      previewComponent: CustomImagePreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        if (widget.__typename === 'CustomImageWidget') {
          return CustomImageSection;
        }
        return null;
      },
    },
  ],
});

// Plug both (widgets and node) graphQl document transformers
extensionRegistry.putData(apolloClientOptionsConfigurersExtensionPoint, {
  identifier: `papyrusweb_${apolloClientOptionsConfigurersExtensionPoint.identifier}`,
  data: [nodeApolloClientOptionsConfigurer, widgetsApolloClientOptionsConfigurer],
});

// Palette tools contribution
const diagramPaletteToolContributions: DiagramPaletteToolContributionProps[] = [
  {
    canHandle: (_: Node<NodeData> | Edge<EdgeData>) => true,
    component: PapyrusPopupToolContribution,
  },
];
extensionRegistry.putData<DiagramPaletteToolContributionProps[]>(diagramPaletteToolExtensionPoint, {
  identifier: 'papyrus-diagram-tools',
  data: diagramPaletteToolContributions,
});

// Tree Item context menu contributions
extensionRegistry.addComponent(treeItemContextMenuEntryExtensionPoint, {
  identifier: 'papyrus-custom-tree-menu-profile',
  Component: UMLModelTreeItemContextMenuContribution,
});
extensionRegistry.addComponent(treeItemContextMenuEntryExtensionPoint, {
  identifier: 'papyrus-custom-tree-menu-stereotype',
  Component: UMLElementTreeItemContextMenuContribution,
});
extensionRegistry.addComponent(treeItemContextMenuEntryExtensionPoint, {
  identifier: 'papyrus-custom-tree-menu-publish-profile',
  Component: PublishProfileTreeItemContextMenuContribution,
});

// Footer contribution
extensionRegistry.addComponent(footerExtensionPoint, {
  identifier: 'papyrus-footer',
  Component: Footer,
});

// Main icon contribution
extensionRegistry.addComponent(navigationBarIconExtensionPoint, {
  identifier: 'papyrusweb_navigationbar#icon',
  Component: PapyrusNavigationBarIcon,
});

// Customize help url
extensionRegistry.putData(navigationBarMenuHelpURLExtensionPoint, {
  identifier: `papyrus_web_doc_${navigationBarMenuHelpURLExtensionPoint.identifier}`,
  data: `${httpOrigin}/doc/index.html`,
});

// Table contribution
extensionRegistry.addAll(forkRegistry, new DefaultExtensionRegistryMergeStrategy());

const container = document.getElementById('root');
const root = createRoot(container!);
root.render(
  <SiriusWebApplication
    httpOrigin={httpOrigin}
    wsOrigin={wsOrigin}
    extensionRegistry={extensionRegistry}
    extensionRegistryMergeStrategy={new ExtensionRegistryMergeStrategy()}>
    <DiagramRepresentationConfiguration nodeTypeRegistry={nodeTypeRegistryValue} />
  </SiriusWebApplication>
);

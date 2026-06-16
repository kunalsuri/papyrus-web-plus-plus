/*******************************************************************************
 * Copyright (c) 2026 CEA LIST, Obeo.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *  Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Issue 283
 *******************************************************************************/

import { ExtensionRegistry } from '@eclipse-sirius/sirius-components-core';
import { forkRegistry } from '@eclipse-sirius/sirius-web-view-fork';
import {
  GQLWidget,
  PropertySectionComponent,
  widgetContributionExtensionPoint,
} from '@eclipse-sirius/sirius-components-forms';
import {
  ReferenceIcon,
  ReferencePreview,
  ReferencePropertySection,
} from '@eclipse-sirius/sirius-components-widget-reference';
import {
  ApolloClientOptionsConfigurer,
  apolloClientOptionsConfigurersExtensionPoint,
  DefaultExtensionRegistryMergeStrategy,
} from '@eclipse-sirius/sirius-web-application';
import FormatListBulletedIcon from '@mui/icons-material/FormatListBulleted';
import { ContainmentReferenceIcon } from '../widgets/containmentReference/ContainmentReferenceIcon';
import { ContainmentReferencePreview } from '../widgets/containmentReference/ContainmentReferencePreview';
import {
  ContainmentReferenceSection,
  isContainmentReferenceSection,
} from '../widgets/containmentReference/ContainmentReferenceSection';
import { CustomImageIcon } from '../widgets/customImage/CustomImageIcon';
import { CustomImagePreview } from '../widgets/customImage/CustomImagePreview';
import { CustomImageSection, isCustomImage } from '../widgets/customImage/CustomImageSection';
import { LanguageExpressionIcon } from '../widgets/languageExpression/LanguageExpressionIcon';
import { LanguageExpressionPreview } from '../widgets/languageExpression/LanguageExpressionPreview';
import {
  isLanguageExpressionWidget,
  LanguageExpressionSection,
} from '../widgets/languageExpression/LanguageExpressionSection';
import { PrimitiveListWidgetPreview } from '../widgets/primitiveList/PrimitiveListWidgetPreview';
import {
  isPrimitiveListWidget,
  PrimitiveListSection,
} from '../widgets/primitiveList/PrimitiveListWidgetPropertySection';
import { PrimitiveRadioIcon } from '../widgets/primitiveRadio/PrimitiveRadioIcon';
import { PrimitiveRadioPreview } from '../widgets/primitiveRadio/PrimitiveRadioPreview';
import { isPrimitiveRadioWidget, PrimitiveRadioSection } from '../widgets/primitiveRadio/PrimitiveRadioSection';
import { nodesStyleDocumentTransform } from '../nodes/NodesDocumentTransform';
import { customWidgetsDocumentTransform } from '../widgets/CustomWidgetsDocumentTransform';
import { treeItemContextMenuEntryExtensionPoint } from '@eclipse-sirius/sirius-components-trees';
import { UMLModelTreeItemContextMenuContribution } from '../profile/apply-profile/UMLModelTreeItemContextMenuContribution';
import { UMLElementTreeItemContextMenuContribution } from '../profile/apply-stereotype/UMLElementTreeItemContextMenuContribution';
import { PublishProfileTreeItemContextMenuContribution } from '../profile/publish-profile/PublishProfileTreeItemContextMenuContribution';
import {
  DiagramPaletteToolContributionProps,
  diagramPaletteToolExtensionPoint,
  NodeData,
  EdgeData,
} from '@eclipse-sirius/sirius-components-diagrams';
import { PapyrusPopupToolContribution } from '../diagram-tools/PapyrusPopupToolContribution';
import { Edge, Node } from '@xyflow/react';
import {
  OmniboxCommandOverrideContribution,
  omniboxCommandOverrideContributionExtensionPoint,
} from '@eclipse-sirius/sirius-components-omnibox';

import { PublishUMLLibraryCommand } from '../libraries/PublishUMLLibraryCommand';
const papyrusWebExtensionRegistry: ExtensionRegistry = new ExtensionRegistry();

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

papyrusWebExtensionRegistry.putData(widgetContributionExtensionPoint, {
  identifier: 'papyrus-custom-widget-primitive-list',
  data: [
    {
      name: 'PrimitiveListWidget',
      icon: <FormatListBulletedIcon />,
      previewComponent: PrimitiveListWidgetPreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        let propertySectionComponent: PropertySectionComponent<GQLWidget> | null = null;

        if (isPrimitiveListWidget(widget)) {
          propertySectionComponent = PrimitiveListSection;
        }
        return propertySectionComponent;
      },
    },
    {
      name: 'LanguageExpression',
      icon: <LanguageExpressionIcon />,
      previewComponent: LanguageExpressionPreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        let propertySectionComponent: PropertySectionComponent<GQLWidget> | null = null;

        if (isLanguageExpressionWidget(widget)) {
          propertySectionComponent = LanguageExpressionSection;
        }
        return propertySectionComponent;
      },
    },
    {
      name: 'ContainmentReferenceWidget',
      icon: <ContainmentReferenceIcon />,
      previewComponent: ContainmentReferencePreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        let propertySectionComponent: PropertySectionComponent<GQLWidget> | null = null;

        if (isContainmentReferenceSection(widget)) {
          propertySectionComponent = ContainmentReferenceSection;
        }
        return propertySectionComponent;
      },
    },
    {
      name: 'PrimitiveRadio',
      icon: <PrimitiveRadioIcon />,
      previewComponent: PrimitiveRadioPreview,
      component: (widget: GQLWidget): PropertySectionComponent<GQLWidget> | null => {
        let propertySectionComponent: PropertySectionComponent<GQLWidget> | null = null;

        if (isPrimitiveRadioWidget(widget)) {
          propertySectionComponent = PrimitiveRadioSection;
        }
        return propertySectionComponent;
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
        let propertySectionComponent: PropertySectionComponent<GQLWidget> | null = null;

        if (isCustomImage(widget)) {
          propertySectionComponent = CustomImageSection;
        }
        return propertySectionComponent;
      },
    },
  ],
});

// Palette tools contribution
const diagramPaletteToolContributions: DiagramPaletteToolContributionProps[] = [
  {
    canHandle: (_: Node<NodeData> | Edge<EdgeData> | null) => true,
    component: PapyrusPopupToolContribution,
  },
];
papyrusWebExtensionRegistry.putData<DiagramPaletteToolContributionProps[]>(diagramPaletteToolExtensionPoint, {
  identifier: 'papyrus-diagram-tools',
  data: diagramPaletteToolContributions,
});

// Tree Item context menu contributions
papyrusWebExtensionRegistry.addComponent(treeItemContextMenuEntryExtensionPoint, {
  identifier: 'papyrus-custom-tree-menu-profile',
  Component: UMLModelTreeItemContextMenuContribution,
});
papyrusWebExtensionRegistry.addComponent(treeItemContextMenuEntryExtensionPoint, {
  identifier: 'papyrus-custom-tree-menu-stereotype',
  Component: UMLElementTreeItemContextMenuContribution,
});
papyrusWebExtensionRegistry.addComponent(treeItemContextMenuEntryExtensionPoint, {
  identifier: 'papyrus-custom-tree-menu-publish-profile',
  Component: PublishProfileTreeItemContextMenuContribution,
});

// Plug both (widgets and node) graphQl document transformers
papyrusWebExtensionRegistry.putData(apolloClientOptionsConfigurersExtensionPoint, {
  identifier: `papyrusweb_${apolloClientOptionsConfigurersExtensionPoint.identifier}`,
  data: [nodeApolloClientOptionsConfigurer, widgetsApolloClientOptionsConfigurer],
});

//publish library extension registry
const omniboxCommandOverrides: OmniboxCommandOverrideContribution[] = [
  {
    canHandle: (action) => {
      return action.id === 'publishUMLModel';
    },
    component: PublishUMLLibraryCommand,
  },
];

papyrusWebExtensionRegistry.putData<OmniboxCommandOverrideContribution[]>(
  omniboxCommandOverrideContributionExtensionPoint,
  {
    identifier: `papyrusweb_${omniboxCommandOverrideContributionExtensionPoint.identifier}`,
    data: omniboxCommandOverrides,
  }
);

// Table contribution
papyrusWebExtensionRegistry.addAll(forkRegistry, new DefaultExtensionRegistryMergeStrategy());

export { papyrusWebExtensionRegistry };

/*******************************************************************************
 * Copyright (c) 2024 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/

import { DocumentTransform } from '@apollo/client';
import { DocumentNode, FieldNode, InlineFragmentNode, Kind, SelectionNode, visit } from 'graphql';

const shouldTransform = (document: DocumentNode) => {
  return (
    document.definitions[0] &&
    document.definitions[0].kind === Kind.OPERATION_DEFINITION &&
    document.definitions[0].name?.value === 'diagramEvent'
  );
};

const isNodeStyleFragment = (field: FieldNode) => {
  if (field.name.value === 'style') {
    const inLinesFragment = field.selectionSet.selections
      .filter((selection) => selection.kind === Kind.INLINE_FRAGMENT)
      .map((inlineFragment: InlineFragmentNode) => inlineFragment.typeCondition.name.value);
    if (inLinesFragment.includes('RectangularNodeStyle') && inLinesFragment.includes('ImageNodeStyle')) {
      return true;
    }
  }
  return false;
};

const borderColorField: SelectionNode = {
  kind: Kind.FIELD,
  name: {
    kind: Kind.NAME,
    value: 'borderColor',
  },
};

const borderSizeField: SelectionNode = {
  kind: Kind.FIELD,
  name: {
    kind: Kind.NAME,
    value: 'borderSize',
  },
};

const borderStyleField: SelectionNode = {
  kind: Kind.FIELD,
  name: {
    kind: Kind.NAME,
    value: 'borderStyle',
  },
};

const backgroundField: SelectionNode = {
  kind: Kind.FIELD,
  name: {
    kind: Kind.NAME,
    value: 'background',
  },
};

const shapeField: SelectionNode = {
  kind: Kind.FIELD,
  name: {
    kind: Kind.NAME,
    value: 'shape',
  },
};

export const nodesStyleDocumentTransform = new DocumentTransform((document) => {
  if (shouldTransform(document)) {
    return visit(document, {
      Field(field) {
        if (!isNodeStyleFragment(field)) {
          return undefined;
        }

        const selections = field.selectionSet?.selections ?? [];

        const cuboidNodeStyleInlineFragment: InlineFragmentNode = {
          kind: Kind.INLINE_FRAGMENT,
          selectionSet: {
            kind: Kind.SELECTION_SET,
            selections: [borderColorField, borderSizeField, borderStyleField, backgroundField],
          },
          typeCondition: {
            kind: Kind.NAMED_TYPE,
            name: {
              kind: Kind.NAME,
              value: 'CuboidNodeStyle',
            },
          },
        };

        const innerFlagNodeStyleInlineFragment: InlineFragmentNode = {
          kind: Kind.INLINE_FRAGMENT,
          selectionSet: {
            kind: Kind.SELECTION_SET,
            selections: [borderColorField, borderSizeField, borderStyleField, backgroundField],
          },
          typeCondition: {
            kind: Kind.NAMED_TYPE,
            name: {
              kind: Kind.NAME,
              value: 'InnerFlagNodeStyle',
            },
          },
        };

        const noteNodeStyleInlineFragment: InlineFragmentNode = {
          kind: Kind.INLINE_FRAGMENT,
          selectionSet: {
            kind: Kind.SELECTION_SET,
            selections: [borderColorField, borderSizeField, borderStyleField, backgroundField],
          },
          typeCondition: {
            kind: Kind.NAMED_TYPE,
            name: {
              kind: Kind.NAME,
              value: 'NoteNodeStyle',
            },
          },
        };

        const outerFlagNodeStyleInlineFragment: InlineFragmentNode = {
          kind: Kind.INLINE_FRAGMENT,
          selectionSet: {
            kind: Kind.SELECTION_SET,
            selections: [borderColorField, borderSizeField, borderStyleField, backgroundField],
          },
          typeCondition: {
            kind: Kind.NAMED_TYPE,
            name: {
              kind: Kind.NAME,
              value: 'OuterFlagNodeStyle',
            },
          },
        };

        const packageNodeStyleInlineFragment: InlineFragmentNode = {
          kind: Kind.INLINE_FRAGMENT,
          selectionSet: {
            kind: Kind.SELECTION_SET,
            selections: [borderColorField, borderSizeField, borderStyleField, backgroundField],
          },
          typeCondition: {
            kind: Kind.NAMED_TYPE,
            name: {
              kind: Kind.NAME,
              value: 'PackageNodeStyle',
            },
          },
        };

        const rectangleWithExternalLabelNodeStyleInlineFragment: InlineFragmentNode = {
          kind: Kind.INLINE_FRAGMENT,
          selectionSet: {
            kind: Kind.SELECTION_SET,
            selections: [borderColorField, borderSizeField, borderStyleField, backgroundField],
          },
          typeCondition: {
            kind: Kind.NAMED_TYPE,
            name: {
              kind: Kind.NAME,
              value: 'RectangleWithExternalLabelNodeStyle',
            },
          },
        };

        const customImageNodeStyleInlineFragment: InlineFragmentNode = {
          kind: Kind.INLINE_FRAGMENT,
          selectionSet: {
            kind: Kind.SELECTION_SET,
            selections: [shapeField, borderColorField, borderSizeField, borderStyleField, backgroundField],
          },
          typeCondition: {
            kind: Kind.NAMED_TYPE,
            name: {
              kind: Kind.NAME,
              value: 'CustomImageNodeStyle',
            },
          },
        };

        return {
          ...field,
          selectionSet: {
            ...field.selectionSet,
            selections: [
              ...selections,
              cuboidNodeStyleInlineFragment,
              innerFlagNodeStyleInlineFragment,
              noteNodeStyleInlineFragment,
              outerFlagNodeStyleInlineFragment,
              packageNodeStyleInlineFragment,
              rectangleWithExternalLabelNodeStyleInlineFragment,
              customImageNodeStyleInlineFragment,
            ],
          },
        };
      },
    });
  }
  return document;
});

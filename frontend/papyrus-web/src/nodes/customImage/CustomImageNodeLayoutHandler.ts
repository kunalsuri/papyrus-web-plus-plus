/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 218
 *****************************************************************************/

import {
  Diagram,
  DiagramNodeType,
  ForcedDimensions,
  ILayoutEngine,
  INodeLayoutHandler,
  NodeData,
  applyRatioOnNewNodeSizeValue,
  computePreviousSize,
  findNodeIndex,
  getDefaultOrMinHeight,
  getDefaultOrMinWidth,
  getHeaderHeightFootprint,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Node } from '@xyflow/react';
import { CustomImageNodeData } from './CustomImageNode.types';

export class CustomImageNodeLayoutHandler implements INodeLayoutHandler<NodeData> {
  canHandle(node: Node<NodeData, DiagramNodeType>) {
    return node.type === 'customImageNode';
  }

  handle(
    _layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<CustomImageNodeData, 'customImageNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    _directChildren: Node<NodeData, DiagramNodeType>[],
    _newlyAddedNode: Node<NodeData, DiagramNodeType> | undefined,
    forceWidth?: ForcedDimensions
  ) {
    const nodeIndex = findNodeIndex(visibleNodes, node.id);
    const nodeElement = document.getElementById(`${node.id}-noteNode-${nodeIndex}`)?.children[0];
    const borderWidth = nodeElement ? parseFloat(window.getComputedStyle(nodeElement).borderWidth) : 1;

    const labelElement = document.getElementById(`${node.id}-label-${nodeIndex}`);

    const labelWidth = (labelElement?.getBoundingClientRect().width ?? 0) + borderWidth * 2 + 8 + 20;
    const labelHeight = getHeaderHeightFootprint(labelElement, node.data.insideLabel, 'TOP');

    const nodeMinComputeWidth = labelWidth;
    const nodeMinComputeHeight = labelHeight + borderWidth * 2;

    const nodeWith = forceWidth?.width ?? getDefaultOrMinWidth(nodeMinComputeWidth, node);
    const nodeHeight = getDefaultOrMinHeight(nodeMinComputeHeight, node);

    const previousNode = (previousDiagram?.nodes ?? []).find((previouseNode) => previouseNode.id === node.id);
    const previousDimensions = computePreviousSize(previousNode, node);
    if (node.data.resizedByUser) {
      if (nodeMinComputeWidth > previousDimensions.width) {
        node.width = nodeMinComputeWidth;
      } else {
        node.width = previousDimensions.width;
      }
      if (nodeMinComputeHeight > previousDimensions.height) {
        node.height = nodeMinComputeHeight;
      } else {
        node.height = previousDimensions.height;
      }
    } else {
      node.width = nodeWith;
      node.height = nodeHeight;
    }

    if (node.data.nodeDescription?.keepAspectRatio) {
      applyRatioOnNewNodeSizeValue(node);
    }
  }
}

/*****************************************************************************
 * Copyright (c) 2024, 2026 CEA LIST, Obeo.
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
 *****************************************************************************/
import {
  Diagram,
  DiagramNodeType,
  ILayoutEngine,
  INodeLayoutHandler,
  NodeData,
  applyRatioOnNewNodeSizeValue,
  computePreviousSize,
  findNodeIndex,
  getBorderNodeExtent,
  getChildNodePosition,
  getDefaultOrMinHeight,
  getDefaultOrMinWidth,
  setBorderNodesPosition,
  ForcedDimensions,
  getHeaderHeightFootprint,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Node } from '@xyflow/react';
import { OuterFlagNodeData } from './OuterFlagNode.types';

export class OuterFlagNodeLayoutHandler implements INodeLayoutHandler<NodeData> {
  canHandle(node: Node<NodeData, DiagramNodeType>) {
    return node.type === 'outerFlagNode';
  }

  handle(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<OuterFlagNodeData, 'outerFlagNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[],
    newlyAddedNodes: Node<NodeData, DiagramNodeType>[],
    forceWidth?: ForcedDimensions
  ) {
    layoutEngine.layoutNodes(previousDiagram, visibleNodes, directChildren, newlyAddedNodes);

    const nodeIndex = findNodeIndex(visibleNodes, node.id);

    const nodeElement = document.getElementById(`${node.id}-outerFlagNode-${nodeIndex}`)?.children[0];
    const borderWidth = nodeElement ? parseFloat(window.getComputedStyle(nodeElement).borderWidth) : 0;

    const labelWidth = (node.data.insideLabel?.width ?? 0) + borderWidth * 2 + 8 + 20;
    const labelHeight = getHeaderHeightFootprint(node.data.insideLabel, 'TOP');

    const borderNodes = directChildren.filter((node) => node.data.isBorderNode);
    const directNodesChildren = directChildren.filter((child) => !child.data.isBorderNode);

    // Update children position to be under the label and at the right padding.
    directNodesChildren.forEach((child, index) => {
      const previousNode = (previousDiagram?.nodes ?? []).find((previouseNode) => previouseNode.id === child.id);

      const createdNode = newlyAddedNodes.find((node) => node?.id === child.id);

      if (!!createdNode) {
        child.position = createdNode.position;
      } else if (previousNode) {
        child.position = previousNode.position;
      } else {
        child.position = child.position = getChildNodePosition(visibleNodes, child, labelHeight, borderWidth);
        const previousSibling = directNodesChildren[index - 1];
        if (previousSibling) {
          child.position = getChildNodePosition(visibleNodes, child, labelHeight, borderWidth, previousSibling);
        }
      }
    });

    const nodeMinComputeWidth = labelWidth;
    const nodeMinComputeHeight = labelHeight + borderWidth * 2;

    const nodeWith = forceWidth?.width ?? getDefaultOrMinWidth(nodeMinComputeWidth, node); // WARN: not sure yet for the
    // forceWidth to be here.
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

    // Update border nodes positions
    borderNodes.forEach((borderNode) => {
      borderNode.extent = getBorderNodeExtent(node, borderNode);
    });
    setBorderNodesPosition(borderNodes, node, previousDiagram);
  }
}

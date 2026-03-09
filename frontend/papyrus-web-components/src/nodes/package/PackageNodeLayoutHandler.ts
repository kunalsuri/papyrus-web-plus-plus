/*****************************************************************************
 * Copyright (c) 2023, 2026 CEA LIST, Obeo.
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
  computeNodesBox,
  Diagram,
  DiagramNodeType,
  findNodeIndex,
  getBorderNodeExtent,
  getChildNodePosition,
  getEastBorderNodeFootprintHeight,
  getNorthBorderNodeFootprintWidth,
  getSouthBorderNodeFootprintWidth,
  getWestBorderNodeFootprintHeight,
  ILayoutEngine,
  INodeLayoutHandler,
  NodeData,
  setBorderNodesPosition,
  computePreviousSize,
  computePreviousPosition,
  ForcedDimensions,
  getHeaderHeightFootprint,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Node } from '@xyflow/react';
import { PackageNodeData } from './PackageNode.types';

const rectangularNodePadding: number = 8;

export class PackageNodeLayoutHandler implements INodeLayoutHandler<PackageNodeData> {
  public canHandle(node: Node<NodeData, DiagramNodeType>) {
    return node.type === 'packageNode';
  }

  public handle(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<PackageNodeData, 'packageNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[],
    newlyAddedNodes: Node<NodeData, DiagramNodeType>[],
    forceWidth?: ForcedDimensions
  ) {
    const nodeIndex = findNodeIndex(visibleNodes, node.id);
    const nodeElement = document.getElementById(`${node.id}-packageNode-${nodeIndex}`)?.children[0];
    const borderWidth = nodeElement ? parseFloat(window.getComputedStyle(nodeElement).borderWidth) : 0;
    if (directChildren.length > 0) {
      this.handleParentNode(
        layoutEngine,
        previousDiagram,
        node,
        visibleNodes,
        directChildren,
        newlyAddedNodes,
        borderWidth,
        forceWidth
      );
    } else {
      this.handleLeafNode(previousDiagram, node, borderWidth, forceWidth);
    }
  }

  private handleParentNode(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<PackageNodeData, 'packageNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[],
    newlyAddedNodes: Node<NodeData, DiagramNodeType>[],
    borderWidth: number,
    _forceWidth?: ForcedDimensions
  ) {
    layoutEngine.layoutNodes(previousDiagram, visibleNodes, directChildren, newlyAddedNodes);

    const headerHeightFootprint = getHeaderHeightFootprint(node.data.insideLabel, 'TOP', borderWidth);

    const borderNodes = directChildren.filter((node) => node.data.isBorderNode);
    const directNodesChildren = directChildren.filter((child) => !child.data.isBorderNode);

    // Update children position to be under the label and at the right padding.
    directNodesChildren.forEach((child, index) => {
      const previousNode = (previousDiagram?.nodes ?? []).find((previouseNode) => previouseNode.id === child.id);
      const previousPosition = computePreviousPosition(previousNode, child);
      const createdNode = newlyAddedNodes.find((node) => node?.id === child.id);

      if (!!createdNode) {
        child.position = createdNode.position;
        if (child.position.y < borderWidth + headerHeightFootprint + rectangularNodePadding) {
          child.position = { ...child.position, y: borderWidth + headerHeightFootprint + rectangularNodePadding };
        }
        if (child.position.x < 0) {
          child.position = { ...child.position, x: rectangularNodePadding };
        }
      } else if (previousPosition) {
        child.position = previousPosition;
        if (child.position.y < headerHeightFootprint + rectangularNodePadding) {
          child.position = { ...child.position, y: headerHeightFootprint + rectangularNodePadding };
        }
        // Force the position.x to rectangularNodePadding if the child is moved outside the west border.
        if (child.position.x < 0) {
          child.position = { ...child.position, x: rectangularNodePadding };
        }
      } else {
        child.position = getChildNodePosition(visibleNodes, child, headerHeightFootprint, borderWidth);
        const previousSibling = directNodesChildren[index - 1];
        if (previousSibling) {
          child.position = getChildNodePosition(
            visibleNodes,
            child,
            headerHeightFootprint,
            borderWidth,
            previousSibling
          );
        }
        if (child.position.y < headerHeightFootprint + rectangularNodePadding) {
          child.position = { ...child.position, y: child.position.y + headerHeightFootprint };
        }
        // Force the position.x to rectangularNodePadding if the child is moved outside the west border.
        if (child.position.x < 0) {
          child.position = { ...child.position, x: rectangularNodePadding };
        }
      }
    });

    // Update node to layout size
    // WARN: We suppose label are always on top of children (that wrong)
    const childrenContentBox = computeNodesBox(visibleNodes, directNodesChildren); // WARN: The current content box algorithm does not take the margin of direct children (it should)
    const directChildrenAwareNodeWidth = childrenContentBox.x + childrenContentBox.width + rectangularNodePadding;
    const northBorderNodeFootprintWidth = getNorthBorderNodeFootprintWidth(visibleNodes, borderNodes, previousDiagram);
    const southBorderNodeFootprintWidth = getSouthBorderNodeFootprintWidth(visibleNodes, borderNodes, previousDiagram);

    const nodeWidth =
      Math.max(
        directChildrenAwareNodeWidth,
        northBorderNodeFootprintWidth,
        southBorderNodeFootprintWidth,
        node.data.defaultWidth ? node.data.defaultWidth : 0
      ) +
      borderWidth * 2;

    // WARN: the label is not used for the height because children are already position under the label
    const directChildrenAwareNodeHeight = childrenContentBox.y + childrenContentBox.height + rectangularNodePadding;
    const eastBorderNodeFootprintHeight = getEastBorderNodeFootprintHeight(visibleNodes, borderNodes, previousDiagram);
    const westBorderNodeFootprintHeight = getWestBorderNodeFootprintHeight(visibleNodes, borderNodes, previousDiagram);

    const nodeHeight =
      Math.max(
        directChildrenAwareNodeHeight,
        eastBorderNodeFootprintHeight,
        westBorderNodeFootprintHeight,
        node.data.defaultHeight ? node.data.defaultHeight : 0
      ) +
      borderWidth * 2;

    const minNodeWith = nodeWidth;
    const minNodeheight = nodeHeight;

    const previousNode = (previousDiagram?.nodes ?? []).find((previouseNode) => previouseNode.id === node.id);
    const previousDimensions = computePreviousSize(previousNode, node);
    if (node.data.nodeDescription?.userResizable) {
      if (minNodeWith > previousDimensions.width) {
        node.width = minNodeWith;
      } else {
        node.width = previousDimensions.width;
      }
      if (minNodeheight > previousDimensions.height) {
        node.height = minNodeheight;
      } else {
        node.height = previousDimensions.height;
      }
    } else {
      node.width = minNodeWith;
      node.height = minNodeheight;
    }

    // Update border nodes positions
    borderNodes.forEach((borderNode) => {
      borderNode.extent = getBorderNodeExtent(node, borderNode);
    });
    setBorderNodesPosition(borderNodes, node, previousDiagram);
  }

  private handleLeafNode(
    previousDiagram: Diagram | null,
    node: Node<PackageNodeData, 'packageNode'>,
    _borderWidth: number,
    _forceWidth?: ForcedDimensions
  ) {
    const labelHeight = rectangularNodePadding + (node.data.insideLabel?.height ?? 0) + rectangularNodePadding;

    const minNodeWith = Math.max(node.data.defaultWidth ? node.data.defaultWidth : 0);
    const minNodeheight = Math.max(labelHeight, node.data.defaultHeight ? node.data.defaultHeight : 0);

    const previousNode = (previousDiagram?.nodes ?? []).find((previouseNode) => previouseNode.id === node.id);
    const previousDimensions = computePreviousSize(previousNode, node);

    if (node.data.resizedByUser) {
      if (minNodeWith > previousDimensions.width) {
        node.width = minNodeWith;
      } else {
        node.width = previousDimensions.width;
      }
      if (minNodeheight > previousDimensions.height) {
        node.height = minNodeheight;
      } else {
        node.height = previousDimensions.height;
      }
    } else {
      node.width = minNodeWith;
      node.height = minNodeheight;
    }
  }
}

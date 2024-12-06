/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo.
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
  computeNodesBox,
  computePreviousPosition,
  computePreviousSize,
  findNodeIndex,
  getBorderNodeExtent,
  getChildNodePosition,
  getEastBorderNodeFootprintHeight,
  getNorthBorderNodeFootprintWidth,
  getSouthBorderNodeFootprintWidth,
  getWestBorderNodeFootprintHeight,
  setBorderNodesPosition,
  ForcedDimensions,
  getHeaderHeightFootprint,
  getInsideLabelWidthConstraint,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Node } from '@xyflow/react';
import { CuboidNodeData } from './CuboidNode.types';
const rectangularNodePadding: number = 8;

// The number of px reserved on the right & top of the cuboid node to draw perspective lines.
const cuboidBorder: number = 20;
// The padding around the label (which is not computed by headerHeightFootprint)
const labelPadding: number = 18;

export class CuboidNodeLayoutHandler implements INodeLayoutHandler<CuboidNodeData> {
  public canHandle(node: Node<NodeData, DiagramNodeType>) {
    return node.type === 'cuboidNode';
  }

  public handle(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<CuboidNodeData, 'cuboidNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[],
    newlyAddedNode: Node<NodeData, DiagramNodeType> | undefined,
    forceWidth?: ForcedDimensions
  ) {
    const nodeIndex = findNodeIndex(visibleNodes, node.id);
    const nodeElement = document.getElementById(`${node.id}-cuboidNode-${nodeIndex}`)?.children[0];
    const borderWidth = nodeElement ? parseFloat(window.getComputedStyle(nodeElement).borderWidth) : 0;
    if (directChildren.length > 0) {
      this.handleParentNode(
        layoutEngine,
        previousDiagram,
        node,
        visibleNodes,
        directChildren,
        newlyAddedNode,
        borderWidth,
        forceWidth
      );
    } else {
      this.handleLeafNode(previousDiagram, node, visibleNodes, borderWidth, forceWidth);
    }
  }

  private handleParentNode(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<CuboidNodeData, 'cuboidNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[],
    newlyAddedNode: Node<NodeData, DiagramNodeType> | undefined,
    borderWidth: number,
    _forceWidth?: ForcedDimensions
  ) {
    layoutEngine.layoutNodes(previousDiagram, visibleNodes, directChildren, newlyAddedNode);

    const nodeIndex = findNodeIndex(visibleNodes, node.id);
    const labelElement = document.getElementById(`${node.id}-label-${nodeIndex}`);
    const labelWidth =
      getInsideLabelWidthConstraint(node.data.insideLabel, labelElement) + 2 * borderWidth + labelPadding;
    const headerHeightFootprint = getHeaderHeightFootprint(labelElement, node.data.insideLabel, 'TOP');
    const borderNodes = directChildren.filter((node) => node.data.isBorderNode);
    const directNodesChildren = directChildren.filter((child) => !child.data.isBorderNode);

    // Update children position to be under the label and at the right padding.
    directNodesChildren.forEach((child, index) => {
      const previousNode = (previousDiagram?.nodes ?? []).find((previouseNode) => previouseNode.id === child.id);
      const previousPosition = computePreviousPosition(previousNode, child);
      const createdNode = newlyAddedNode?.id === child.id ? newlyAddedNode : undefined;

      if (!!createdNode) {
        child.position = createdNode.position;
        if (
          child.position.y <
          borderWidth + headerHeightFootprint + rectangularNodePadding + cuboidBorder + labelPadding
        ) {
          child.position = {
            ...child.position,
            y: borderWidth + headerHeightFootprint + rectangularNodePadding + cuboidBorder + labelPadding,
          };
        }
        if (child.position.x + child.width > node.width - cuboidBorder - rectangularNodePadding - borderWidth) {
          child.position = {
            ...child.position,
            x: node.width - child.width - cuboidBorder - borderWidth - rectangularNodePadding,
          };
        }
        if (child.position.x < 0) {
          child.position = { ...child.position, x: rectangularNodePadding };
        }
      } else if (previousPosition) {
        child.position = previousPosition;
        if (child.position.y < headerHeightFootprint + rectangularNodePadding + cuboidBorder + labelPadding) {
          child.position = {
            ...child.position,
            y: headerHeightFootprint + rectangularNodePadding + cuboidBorder + labelPadding,
          };
        }

        if (child.position.x + child.width > node.width - cuboidBorder - rectangularNodePadding - borderWidth) {
          child.position = {
            ...child.position,
            x: node.width - child.width - cuboidBorder - borderWidth - rectangularNodePadding,
          };
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
        if (child.position.y < headerHeightFootprint + rectangularNodePadding + cuboidBorder + labelPadding) {
          child.position = {
            ...child.position,
            y: child.position.y + headerHeightFootprint + cuboidBorder + labelPadding,
          };
        }
        if (child.position.x + child.width > node.width - cuboidBorder - rectangularNodePadding - borderWidth) {
          child.position = {
            ...child.position,
            x: node.width - child.width - cuboidBorder - borderWidth - rectangularNodePadding,
          };
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
        labelWidth + cuboidBorder,
        node.data.defaultWidth ? node.data.defaultWidth : 0
      ) +
      borderWidth * 2 +
      cuboidBorder +
      rectangularNodePadding * 2;

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
    node: Node<CuboidNodeData, 'cuboidNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    borderWidth: number,
    _forceWidth?: ForcedDimensions
  ) {
    const nodeIndex = findNodeIndex(visibleNodes, node.id);
    const labelElement = document.getElementById(`${node.id}-label-${nodeIndex}`);
    const labelWidth =
      getInsideLabelWidthConstraint(node.data.insideLabel, labelElement) + 2 * borderWidth + labelPadding;

    const labelHeight =
      rectangularNodePadding + (labelElement?.getBoundingClientRect().height ?? 0) + rectangularNodePadding;

    const minNodeWith = Math.max(labelWidth + cuboidBorder, node.data.defaultWidth ? node.data.defaultWidth : 0);
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

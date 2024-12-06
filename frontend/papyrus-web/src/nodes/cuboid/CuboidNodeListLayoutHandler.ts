/*****************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo.
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
  applyRatioOnNewNodeSizeValue,
  computeNodesBox,
  computePreviousSize,
  Diagram,
  DiagramNodeType,
  findNodeIndex,
  ForcedDimensions,
  getBorderNodeExtent,
  getDefaultOrMinHeight,
  getDefaultOrMinWidth,
  getEastBorderNodeFootprintHeight,
  getHeaderHeightFootprint,
  getNorthBorderNodeFootprintWidth,
  getSouthBorderNodeFootprintWidth,
  getWestBorderNodeFootprintHeight,
  ILayoutEngine,
  INodeLayoutHandler,
  NodeData,
  setBorderNodesPosition,
  getInsideLabelWidthConstraint,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Node } from '@xyflow/react';
import { CuboidNodeListData } from './CuboidNode.types';

// The number of px reserved on the right & top of the cuboid node to draw perspective lines.
const cuboidBorder: number = 20;
// The padding around the label (which is not computed by headerHeightFootprint)
const labelPadding: number = 18;

const rectangularNodePadding: number = 8;

export class CuboidNodeListLayoutHandler implements INodeLayoutHandler<CuboidNodeListData> {
  public canHandle(node: Node<NodeData, DiagramNodeType>) {
    return node.type === 'cuboidNodeList';
  }

  public handle(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<CuboidNodeListData, 'cuboidNodeList'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[],
    newlyAddedNode: Node<NodeData, DiagramNodeType> | undefined,
    forceDimensions?: ForcedDimensions
  ) {
    const nodeIndex = findNodeIndex(visibleNodes, node.id);
    const nodeElement = document.getElementById(`${node.id}-cuboidNodeList-${nodeIndex}`)?.children[0];
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
        forceDimensions
      );
    } else {
      this.handleLeafNode(previousDiagram, node, visibleNodes, borderWidth, forceDimensions);
    }
  }

  private handleParentNode(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<CuboidNodeListData, 'cuboidNodeList'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[],
    newlyAddedNode: Node<NodeData, DiagramNodeType> | undefined,
    borderWidth: number,
    forceDimensions?: ForcedDimensions
  ) {
    layoutEngine.layoutNodes(previousDiagram, visibleNodes, directChildren, newlyAddedNode, forceDimensions);

    const nodeIndex = findNodeIndex(visibleNodes, node.id);
    const labelElement = document.getElementById(`${node.id}-label-${nodeIndex}`);
    const labelWidth =
      getInsideLabelWidthConstraint(node.data.insideLabel, labelElement) + 2 * borderWidth + labelPadding;
    const labelHeight =
      rectangularNodePadding + (labelElement?.getBoundingClientRect().height ?? 0) + rectangularNodePadding;
    let headerHeightFootprint = getHeaderHeightFootprint(labelElement, node.data.insideLabel, 'TOP');
    const borderNodes = directChildren.filter((node) => node.data.isBorderNode);
    const directNodesChildren = directChildren.filter((child) => !child.data.isBorderNode);
    const minNodeWith = Math.max(labelWidth + cuboidBorder, node.data.defaultWidth ? node.data.defaultWidth : 0);
    const minNodeheight = Math.max(labelHeight, node.data.defaultHeight ? node.data.defaultHeight : 0);

    const withHeader: boolean = node.data.insideLabel?.isHeader ?? false;
    const northBorderNodeFootprintWidth = getNorthBorderNodeFootprintWidth(visibleNodes, borderNodes, previousDiagram);
    const southBorderNodeFootprintWidth = getSouthBorderNodeFootprintWidth(visibleNodes, borderNodes, previousDiagram);

    const previousNode: Node<NodeData, string> | undefined = (previousDiagram?.nodes ?? []).find(
      (previouseNode) => previouseNode.id === node.id
    );

    if (!node.width || node.width < minNodeWith) {
      node.width = minNodeWith;
    }
    if (!node.height || node.height < minNodeheight) {
      node.height = minNodeheight;
    }

    if (!forceDimensions) {
      let previousChildrenContentBoxWidthToConsider: number = getDefaultOrMinWidth(0, node) - borderWidth * 2;
      let previousChildrenContentBoxHeightToConsider: number = 0;
      if (node.data.resizedByUser) {
        previousChildrenContentBoxWidthToConsider = (previousNode?.width ?? node.width ?? 0) - borderWidth * 2;
        previousChildrenContentBoxHeightToConsider =
          (previousNode?.height ?? node.height ?? 0) -
          borderWidth * 2 -
          (withHeader ? labelElement?.getBoundingClientRect().height ?? 0 : 0);
      }
      let fixedWidth: number = Math.max(
        directNodesChildren.reduce<number>(
          (widerWidth, child) => Math.max(child.width ?? 0, widerWidth),
          labelElement?.getBoundingClientRect().width ?? 0
        ),
        northBorderNodeFootprintWidth,
        southBorderNodeFootprintWidth,
        previousChildrenContentBoxWidthToConsider,
        minNodeWith
      );
      fixedWidth = fixedWidth - cuboidBorder - borderWidth;
      const nonGrowableChilds = directNodesChildren.filter(
        (child) => !node.data.growableNodeIds.includes(child.data.descriptionId) || child.data.resizedByUser
      );
      nonGrowableChilds.forEach((nonGrowableChild) => {
        layoutEngine.layoutNodes(previousDiagram, visibleNodes, [nonGrowableChild], newlyAddedNode, {
          width: fixedWidth,
          height: null,
        });
      });
      previousChildrenContentBoxHeightToConsider -= nonGrowableChilds.reduce<number>(
        (height, node) => height + (node.height ?? 0),
        0
      );

      const growableChilds = directNodesChildren.filter(
        (child) => node.data.growableNodeIds.includes(child.data.descriptionId) && !child.data.resizedByUser
      );
      const childHeight: number = previousChildrenContentBoxHeightToConsider / growableChilds.length;
      growableChilds.forEach((growableChild) => {
        layoutEngine.layoutNodes(previousDiagram, visibleNodes, [growableChild], newlyAddedNode, {
          width: fixedWidth,
          height: Math.max(growableChild.height ?? 0, childHeight) - labelPadding,
        });
      });
    }
    directNodesChildren.forEach((child, index) => {
      child.position = {
        x: borderWidth,
        y: headerHeightFootprint + labelPadding,
      };
      const previousSibling = directNodesChildren[index - 1];
      if (previousSibling) {
        child.position = { ...child.position, y: previousSibling.position.y + (previousSibling.height ?? 0) };
      }
    });

    const childrenContentBox = computeNodesBox(visibleNodes, directNodesChildren);

    const labelOnlyWidth = getInsideLabelWidthConstraint(node.data.insideLabel, labelElement);
    const nodeMinComputeWidth = Math.max(childrenContentBox.width, labelOnlyWidth) + borderWidth * 2;

    const directChildrenAwareNodeHeight =
      childrenContentBox.y + childrenContentBox.height + borderWidth + node.data.bottomGap;

    const eastBorderNodeFootprintHeight = getEastBorderNodeFootprintHeight(visibleNodes, borderNodes, previousDiagram);
    const westBorderNodeFootprintHeight = getWestBorderNodeFootprintHeight(visibleNodes, borderNodes, previousDiagram);

    const nodeMinComputeHeight = Math.max(
      directChildrenAwareNodeHeight,
      eastBorderNodeFootprintHeight,
      westBorderNodeFootprintHeight
    );

    const nodeWidth = forceDimensions?.width ?? getDefaultOrMinWidth(nodeMinComputeWidth, node) + cuboidBorder;
    const nodeHeight = forceDimensions?.height ?? getDefaultOrMinHeight(nodeMinComputeHeight, node);

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
      node.width = nodeWidth;
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

  private handleLeafNode(
    previousDiagram: Diagram | null,
    node: Node<CuboidNodeListData, 'cuboidNodeList'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    borderWidth: number,
    forceDimensions?: ForcedDimensions
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

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
 *  Aurelien DIDIER (Artal Technologies) - Issue 229
 *****************************************************************************/

//inspired by: packages/diagrams/frontend/sirius-components-diagrams/src/renderer/layout/ListNodeLayoutHandler.ts
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
import { PackageNodeListData } from './PackageNode.types';

export class PackageNodeListLayoutHandler implements INodeLayoutHandler<PackageNodeListData> {
  public canHandle(node: Node<NodeData, DiagramNodeType>) {
    return node.type === 'packageNodeList';
  }

  public handle(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<PackageNodeListData, 'packageNodeList'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[],
    newlyAddedNodes: Node<NodeData, DiagramNodeType>[],
    forceDimensions?: ForcedDimensions
  ) {
    const nodeIndex = findNodeIndex(visibleNodes, node.id);
    const nodeElement = document.getElementById(`${node.id}-packageNodeList-${nodeIndex}`);
    const nodeElementChild =
      nodeElement?.children &&
      Array.from(nodeElement.children).filter((child) => !child.classList.contains('react-flow__resize-control'))[0];
    const borderWidth = nodeElementChild ? parseFloat(window.getComputedStyle(nodeElementChild).borderLeftWidth) : 0;

    if (directChildren.length > 0) {
      this.handleParentNode(
        layoutEngine,
        previousDiagram,
        node,
        visibleNodes,
        directChildren,
        newlyAddedNodes,
        borderWidth,
        forceDimensions
      );
    } else {
      this.handleLeafNode(previousDiagram, node, borderWidth, forceDimensions);
    }
  }

  handleLeafNode(
    previousDiagram: Diagram | null,
    node: Node<PackageNodeListData, 'packageNodeList'>,
    borderWidth: number,
    forceDimensions?: ForcedDimensions
  ) {
    const nodeMinComputeWidth = getInsideLabelWidthConstraint(node.data.insideLabel) + borderWidth * 2;
    const nodeMinComputeHeight = (node.data.insideLabel?.height ?? 0) + borderWidth * 2;
    const nodeWith = forceDimensions?.width ?? getDefaultOrMinWidth(nodeMinComputeWidth, node);
    const nodeHeight = forceDimensions?.height ?? getDefaultOrMinHeight(nodeMinComputeHeight, node);

    const previousNode = (previousDiagram?.nodes ?? []).find((previouseNode) => previouseNode.id === node.id);
    const previousDimensions = computePreviousSize(previousNode, node);

    const heightLostSincePrevDiagram: number =
      previousDiagram?.nodes
        .filter((prevNode) => prevNode.parentId === node.id && !prevNode.hidden)
        .reduce<number>((height, node) => height + (node.height ?? 0), 0) ?? 0;

    if (node.data.resizedByUser) {
      if (nodeMinComputeWidth > previousDimensions.width) {
        node.width = nodeMinComputeWidth;
      } else {
        node.width = previousDimensions.width;
      }
      if (nodeMinComputeHeight > previousDimensions.height) {
        node.height = nodeMinComputeHeight;
      } else {
        node.height = getDefaultOrMinHeight(previousDimensions.height - heightLostSincePrevDiagram, node);
      }
    } else {
      node.width = nodeWith;
      node.height = nodeHeight;
    }
  }

  private handleParentNode(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<PackageNodeListData, 'packageNodeList'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[],
    newlyAddedNodes: Node<NodeData, DiagramNodeType>[],
    borderWidth: number,
    forceDimensions?: ForcedDimensions
  ) {
    layoutEngine.layoutNodes(previousDiagram, visibleNodes, directChildren, newlyAddedNodes, forceDimensions);

    const withHeader: boolean = node.data.insideLabel?.isHeader ?? false;

    const borderNodes = directChildren.filter((node) => node.data.isBorderNode);
    const directNodesChildren = directChildren.filter((child) => !child.data.isBorderNode);
    const northBorderNodeFootprintWidth = getNorthBorderNodeFootprintWidth(visibleNodes, borderNodes, previousDiagram);
    const southBorderNodeFootprintWidth = getSouthBorderNodeFootprintWidth(visibleNodes, borderNodes, previousDiagram);

    const previousNode: Node<NodeData, string> | undefined = (previousDiagram?.nodes ?? []).find(
      (previouseNode) => previouseNode.id === node.id
    );

    const heightLostSincePrevDiagram: number =
      previousDiagram?.nodes
        .filter((prevNode) => prevNode.parentId === node.id && !prevNode.hidden)
        .filter((prevNode) => !directChildren.map((child) => child.id).includes(prevNode.id))
        .reduce<number>((height, node) => height + (node.height ?? 0), 0) ?? 0;

    if (!forceDimensions) {
      let previousChildrenContentBoxWidthToConsider: number = getDefaultOrMinWidth(0, node) - borderWidth * 2;
      let previousChildrenContentBoxHeightToConsider: number = 0;
      if (node.data.resizedByUser) {
        previousChildrenContentBoxWidthToConsider = (previousNode?.width ?? node.width ?? 0) - borderWidth * 2;
        previousChildrenContentBoxHeightToConsider =
          (previousNode?.height ?? node.height ?? 0) -
          borderWidth * 2 -
          (withHeader ? node.data.insideLabel?.height ?? 0 : 0);
      }
      const fixedWidth: number = Math.max(
        directNodesChildren
          .filter((child) => child.type !== 'customImageNode')
          .reduce<number>(
            (widerWidth, child) => Math.max(child.width ?? 0, widerWidth),
            getInsideLabelWidthConstraint(node.data.insideLabel)
          ),
        northBorderNodeFootprintWidth,
        southBorderNodeFootprintWidth,
        previousChildrenContentBoxWidthToConsider
      );
      const nonGrowableChilds = directNodesChildren.filter(
        (child) => !node.data.growableNodeIds.includes(child.data.descriptionId) || child.data.resizedByUser
      );
      nonGrowableChilds.forEach((nonGrowableChild) => {
        layoutEngine.layoutNodes(previousDiagram, visibleNodes, [nonGrowableChild], newlyAddedNodes, {
          width: fixedWidth,
          height: null,
        });
      });
      previousChildrenContentBoxHeightToConsider -= nonGrowableChilds.reduce<number>(
        (height, node) => height + (node.height ?? 0),
        0
      );
      previousChildrenContentBoxHeightToConsider -= node.data.topGap + node.data.bottomGap;

      const growableChilds = directNodesChildren.filter(
        (child) => node.data.growableNodeIds.includes(child.data.descriptionId) && !child.data.resizedByUser
      );
      const childHeight: number = previousChildrenContentBoxHeightToConsider / growableChilds.length;
      growableChilds.forEach((growableChild) => {
        layoutEngine.layoutNodes(previousDiagram, visibleNodes, [growableChild], newlyAddedNodes, {
          width: fixedWidth,
          height: Math.max(growableChild.height ?? 0, childHeight),
        });
      });
    }

    directNodesChildren.forEach((child, index) => {
      child.position = {
        x: borderWidth,
        y: borderWidth + (withHeader ? node.data.insideLabel?.height ?? 0 : 0) + node.data.topGap,
      };
      const previousSibling = directNodesChildren[index - 1];
      if (previousSibling) {
        child.position = { ...child.position, y: previousSibling.position.y + (previousSibling.height ?? 0) };
      }
    });

    const childrenContentBox = computeNodesBox(visibleNodes, directNodesChildren);

    const labelOnlyWidth = getInsideLabelWidthConstraint(node.data.insideLabel);
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

    const nodeWidth = forceDimensions?.width ?? getDefaultOrMinWidth(nodeMinComputeWidth, node);
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
        node.height = getDefaultOrMinHeight(previousDimensions.height - heightLostSincePrevDiagram, node);
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
}

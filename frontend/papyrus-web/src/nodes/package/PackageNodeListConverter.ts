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
  BorderNodePosition,
  ConnectionHandle,
  convertHandles,
  convertInsideLabel,
  convertLineStyle,
  convertOutsideLabels,
  GQLDiagram,
  GQLDiagramDescription,
  GQLEdge,
  GQLNode,
  GQLNodeDescription,
  GQLNodeLayoutData,
  GQLNodeStyle,
  GQLViewModifier,
  IConvertEngine,
  INodeConverter,
  isListLayoutStrategy,
  NodeData,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Node, XYPosition } from '@xyflow/react';
import { GQLPackageNodeStyle, PackageNodeListData } from './PackageNode.types';

const defaultPosition: XYPosition = { x: 0, y: 0 };

const toPackageListNode = (
  gqlDiagram: GQLDiagram,
  gqlNode: GQLNode<GQLPackageNodeStyle>,
  gqlParentNode: GQLNode<GQLNodeStyle> | null,
  nodeDescription: GQLNodeDescription,
  isBorderNode: boolean,
  gqlEdges: GQLEdge[]
): Node<PackageNodeListData> => {
  const {
    targetObjectId,
    targetObjectLabel,
    targetObjectKind,
    descriptionId,
    insideLabel,
    outsideLabels,
    id,
    state,
    pinned,
    style,
    labelEditable,
  } = gqlNode;

  const connectionHandles: ConnectionHandle[] = convertHandles(gqlNode, gqlEdges);
  const gqlNodeLayoutData: GQLNodeLayoutData | undefined = gqlDiagram.layoutData.nodeLayoutData.find(
    (nodeLayoutData) => nodeLayoutData.id === id
  );
  const isNew = gqlNodeLayoutData === undefined;
  const resizedByUser = gqlNodeLayoutData?.resizedByUser ?? false;

  const data: PackageNodeListData = {
    targetObjectId,
    targetObjectLabel,
    targetObjectKind,
    descriptionId,
    style: {
      background: style.background,
      borderTopColor: style.borderColor,
      borderBottomColor: style.borderColor,
      borderLeftColor: style.borderColor,
      borderRightColor: style.borderColor,
      borderTopWidth: style.borderSize,
      borderBottomWidth: style.borderSize,
      borderLeftWidth: style.borderSize,
      borderRightWidth: style.borderSize,
      borderStyle: convertLineStyle(style.borderStyle),
    },
    insideLabel: null,
    outsideLabels: convertOutsideLabels(outsideLabels),
    isBorderNode: isBorderNode,
    borderNodePosition: isBorderNode ? BorderNodePosition.WEST : null,
    faded: state === GQLViewModifier.Faded,
    pinned,
    labelEditable,
    nodeDescription,
    connectionHandles,
    defaultWidth: gqlNode.defaultWidth,
    defaultHeight: gqlNode.defaultHeight,
    isNew,
    areChildNodesDraggable: isListLayoutStrategy(gqlNode.childrenLayoutStrategy)
      ? gqlNode.childrenLayoutStrategy.areChildNodesDraggable
      : true,
    topGap: isListLayoutStrategy(gqlNode.childrenLayoutStrategy) ? gqlNode.childrenLayoutStrategy.topGap : 0,
    bottomGap: isListLayoutStrategy(gqlNode.childrenLayoutStrategy) ? gqlNode.childrenLayoutStrategy.bottomGap : 0,
    isListChild: isListLayoutStrategy(gqlParentNode?.childrenLayoutStrategy),
    resizedByUser,
    growableNodeIds: isListLayoutStrategy(gqlNode.childrenLayoutStrategy)
      ? gqlNode.childrenLayoutStrategy.growableNodeIds
      : [],
    isDropNodeTarget: false,
    isDropNodeCandidate: false,
    isHovered: false,
  };

  data.insideLabel = convertInsideLabel(
    insideLabel,
    data,
    `${style.borderSize}px ${style.borderStyle} ${style.borderColor}`
  );

  if (data.insideLabel) {
    data.insideLabel.isHeader = true;
    data.insideLabel.headerPosition = 'TOP';
  }

  const node: Node<PackageNodeListData> = {
    id,
    type: 'packageNodeList',
    data,
    position: defaultPosition,
    hidden: state === GQLViewModifier.Hidden,
  };

  if (gqlParentNode) {
    node.parentId = gqlParentNode.id;
  }

  const nodeLayoutData = gqlDiagram.layoutData.nodeLayoutData.filter((data) => data.id === id)[0];
  if (nodeLayoutData) {
    const {
      position,
      size: { height, width },
    } = nodeLayoutData;
    node.position = position;
    node.height = height;
    node.width = width;
    node.style = {
      ...node.style,
      width: `${node.width}px`,
      height: `${node.height}px`,
    };
  } else {
    node.height = data.defaultHeight ?? 150;
    node.width = data.defaultWidth ?? 70;
  }

  return node;
};

const adaptChildrenBorderNodes = (nodes: Node[], gqlChildrenNodes: GQLNode<GQLNodeStyle>[]): void => {
  const visibleChildrenNodes = nodes
    .filter(
      (child) =>
        gqlChildrenNodes.map((gqlChild) => gqlChild.id).find((gqlChildId) => gqlChildId === child.id) !== undefined
    )
    .filter((child) => !child.hidden);
  visibleChildrenNodes.forEach((child, index) => {
    let childData = child.data as NodeData;
    child.data.style = {
      ...childData.style,
      borderTopWidth: childData.style.borderWidth,
      borderLeftWidth: childData.style.borderWidth,
      borderRightWidth: childData.style.borderWidth,
      borderBottomWidth: childData.style.borderWidth,
    };

    if (index === visibleChildrenNodes.length - 1) {
      child.style = {
        ...child.style,
        borderBottomWidth: '0',
      };
    }
  });
};

export class PackageNodeListConverter implements INodeConverter {
  canHandle(gqlNode: GQLNode<GQLNodeStyle>) {
    return gqlNode.style.__typename === 'PackageNodeStyle' && gqlNode.childrenLayoutStrategy?.kind === 'List';
  }

  handle(
    convertEngine: IConvertEngine,
    gqlDiagram: GQLDiagram,
    gqlNode: GQLNode<GQLPackageNodeStyle>,
    gqlEdges: GQLEdge[],
    parentNode: GQLNode<GQLNodeStyle> | null,
    isBorderNode: boolean,
    nodes: Node[],
    diagramDescription: GQLDiagramDescription,
    nodeDescriptions: GQLNodeDescription[]
  ) {
    const nodeDescription = nodeDescriptions.find((description) => description.id === gqlNode.descriptionId);
    if (nodeDescription) {
      nodes.push(toPackageListNode(gqlDiagram, gqlNode, parentNode, nodeDescription, isBorderNode, gqlEdges));
    }

    const borderNodeDescriptions: GQLNodeDescription[] = (nodeDescription?.borderNodeDescriptionIds ?? []).flatMap(
      (nodeDescriptionId) =>
        diagramDescription.nodeDescriptions.filter((nodeDescription) => nodeDescription.id === nodeDescriptionId)
    );
    const childNodeDescriptions: GQLNodeDescription[] = (nodeDescription?.childNodeDescriptionIds ?? []).flatMap(
      (nodeDescriptionId) =>
        diagramDescription.nodeDescriptions.filter((nodeDescription) => nodeDescription.id === nodeDescriptionId)
    );

    convertEngine.convertNodes(
      gqlDiagram,
      gqlNode.borderNodes ?? [],
      gqlNode,
      nodes,
      diagramDescription,
      borderNodeDescriptions
    );
    convertEngine.convertNodes(
      gqlDiagram,
      gqlNode.childNodes ?? [],
      gqlNode,
      nodes,
      diagramDescription,
      childNodeDescriptions
    );
    adaptChildrenBorderNodes(nodes, gqlNode.childNodes ?? []);
  }
}

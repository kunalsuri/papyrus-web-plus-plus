/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo.
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
  GQLDiagram,
  GQLDiagramDescription,
  GQLEdge,
  GQLHandleLayoutData,
  GQLNode,
  GQLNodeDescription,
  GQLNodeLayoutData,
  GQLNodeStyle,
  GQLViewModifier,
  IConvertEngine,
  INodeConverter,
  NodeData,
  convertHandles,
  convertInsideLabel,
  convertLineStyle,
  convertOutsideLabels,
  isListLayoutStrategy,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Node, XYPosition } from '@xyflow/react';
import { GQLPackageNodeStyle, PackageNodeData } from './PackageNode.types';

const defaultPosition: XYPosition = { x: 0, y: 0 };

const toPackageNode = (
  gqlDiagram: GQLDiagram,
  gqlNode: GQLNode<GQLPackageNodeStyle>,
  gqlParentNode: GQLNode<GQLNodeStyle> | null,
  nodeDescription: GQLNodeDescription | undefined,
  isBorderNode: boolean,
  gqlEdge: GQLEdge[]
): Node<PackageNodeData> => {
  const {
    targetObjectId,
    targetObjectLabel,
    targetObjectKind,
    descriptionId,
    id,
    insideLabel,
    outsideLabels,
    state,
    pinned,
    style,
    labelEditable,
  } = gqlNode;

  const handleLayoutData: GQLHandleLayoutData[] = gqlDiagram.layoutData.nodeLayoutData
    .filter((nodeLayoutData) => nodeLayoutData.id === id)
    .flatMap((nodeLayoutData) => nodeLayoutData.handleLayoutData);

  const connectionHandles: ConnectionHandle[] = convertHandles(gqlNode.id, gqlEdge, handleLayoutData);
  const gqlNodeLayoutData: GQLNodeLayoutData | undefined = gqlDiagram.layoutData.nodeLayoutData.find(
    (nodeLayoutData) => nodeLayoutData.id === id
  );
  const isNew = gqlNodeLayoutData === undefined;
  const resizedByUser = gqlNodeLayoutData?.resizedByUser ?? false;

  const data: PackageNodeData = {
    targetObjectId,
    targetObjectLabel,
    targetObjectKind,
    descriptionId,
    style: {
      display: 'flex',
      background: style.background,
      borderColor: style.borderColor,
      borderWidth: style.borderSize,
      borderStyle: convertLineStyle(style.borderStyle),
    },
    insideLabel: null,
    outsideLabels: convertOutsideLabels(outsideLabels),
    faded: state === GQLViewModifier.Faded,
    pinned,
    isBorderNode: isBorderNode,
    nodeDescription,
    defaultWidth: gqlNode.defaultWidth,
    defaultHeight: gqlNode.defaultHeight,
    borderNodePosition: isBorderNode ? BorderNodePosition.EAST : null,
    connectionHandles,
    labelEditable,
    isNew,
    resizedByUser,
    isListChild: isListLayoutStrategy(gqlParentNode?.childrenLayoutStrategy),
    areChildNodesDraggable: isListLayoutStrategy(gqlNode.childrenLayoutStrategy)
      ? gqlNode.childrenLayoutStrategy.areChildNodesDraggable
      : true,
    isDropNodeTarget: false,
    isDropNodeCandidate: false,
    isHovered: false,
    growableNodeIds: isListLayoutStrategy(gqlNode.childrenLayoutStrategy)
      ? gqlNode.childrenLayoutStrategy.growableNodeIds
      : [],
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

  const node: Node<PackageNodeData> = {
    id,
    type: 'packageNode',
    data,
    position: defaultPosition,
    hidden: gqlNode.state === GQLViewModifier.Hidden,
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
    node.height = data.defaultHeight;
    node.width = data.defaultWidth;
  }
  return node;
};

const adaptChildrenBorderNodes = (nodes: Node<NodeData>[], gqlChildrenNodes: GQLNode<GQLNodeStyle>[]): void => {
  const visibleChildrenNodes = nodes
    .filter(
      (child) =>
        gqlChildrenNodes.map((gqlChild) => gqlChild.id).find((gqlChildId) => gqlChildId === child.id) !== undefined
    )
    .filter((child) => !child.hidden);
  visibleChildrenNodes.forEach((child, index) => {
    // Hide children node borders to prevent a 'bold' aspect, except for the bottom one to mark the separation between child
    child.data.style = {
      ...child.data.style,
      borderTopWidth: '1',
      borderLeftWidth: '1',
      borderRightWidth: '1',
      borderBottomWidth: '1',
    };

    if (index === visibleChildrenNodes.length - 1) {
      child.data.style = {
        ...child.data.style,
        borderBottomWidth: '1',
      };
    }
  });
};

export class PackageNodeConverter implements INodeConverter {
  canHandle(gqlNode: GQLNode<GQLNodeStyle>) {
    return gqlNode.style.__typename === 'PackageNodeStyle' && gqlNode.childrenLayoutStrategy?.kind !== 'List';
  }

  handle(
    convertEngine: IConvertEngine,
    gqlDiagram: GQLDiagram,
    gqlNode: GQLNode<GQLPackageNodeStyle>,
    gqlEdges: GQLEdge[],
    parentNode: GQLNode<GQLNodeStyle> | null,
    isBorderNode: boolean,
    nodes: Node<NodeData>[],
    diagramDescription: GQLDiagramDescription,
    nodeDescriptions: GQLNodeDescription[]
  ) {
    const nodeDescription = nodeDescriptions.find((description) => description.id === gqlNode.descriptionId);
    nodes.push(toPackageNode(gqlDiagram, gqlNode, parentNode, nodeDescription ?? undefined, isBorderNode, gqlEdges));

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

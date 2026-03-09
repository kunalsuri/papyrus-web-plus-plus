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
  BorderNodePosition,
  ConnectionHandle,
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
  convertHandles,
  convertLineStyle,
  convertOutsideLabels,
  isListLayoutStrategy,
  convertInsideLabel,
  GQLHandleLayoutData,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Node, XYPosition } from '@xyflow/react';
import {
  GQLRectangleWithExternalLabelNodeStyle,
  RectangleWithExternalLabelNodeData,
} from './RectangleWithExternalLabelNode.types';

const defaultPosition: XYPosition = { x: 0, y: 0 };

const toRectangleWithExternalLabelNode = (
  gqlDiagram: GQLDiagram,
  gqlNode: GQLNode<GQLRectangleWithExternalLabelNodeStyle>,
  gqlParentNode: GQLNode<GQLNodeStyle> | null,
  nodeDescription: GQLNodeDescription,
  isBorderNode: boolean,
  gqlEdges: GQLEdge[]
): Node<RectangleWithExternalLabelNodeData> => {
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
    deletable,
  } = gqlNode;

  const handleLayoutData: GQLHandleLayoutData[] = gqlDiagram.layoutData.nodeLayoutData
    .filter((nodeLayoutData) => nodeLayoutData.id === id)
    .flatMap((nodeLayoutData) => nodeLayoutData.handleLayoutData);

  const connectionHandles: ConnectionHandle[] = convertHandles(gqlNode.id, gqlEdges, handleLayoutData);
  const gqlNodeLayoutData: GQLNodeLayoutData | undefined = gqlDiagram.layoutData.nodeLayoutData.find(
    (nodeLayoutData) => nodeLayoutData.id === id
  );
  const isNew = gqlNodeLayoutData === undefined;
  const resizedByUser = gqlNodeLayoutData?.resizedByUser ?? false;
  const movedByUser = gqlNodeLayoutData?.movedByUser ?? false;

  const data: RectangleWithExternalLabelNodeData = {
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
    outsideLabels: convertOutsideLabels(outsideLabels, gqlDiagram.layoutData.labelLayoutData),
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
    movedByUser,
    isListChild: isListLayoutStrategy(gqlParentNode?.style.childrenLayoutStrategy),
    isDraggedNode: false,
    isDropNodeTarget: false,
    isDropNodeCandidate: false,
    isHovered: false,
    connectionLinePositionOnNode: 'none',
    nodeAppearanceData: {
      gqlStyle: style,
      customizedStyleProperties: [],
    },
    minComputedWidth: gqlNodeLayoutData?.minComputedSize.width ?? null,
    minComputedHeight: gqlNodeLayoutData?.minComputedSize.height ?? null,
    deletable,
    isLastNodeSelected: false,
  };

  data.insideLabel = convertInsideLabel(
    insideLabel,
    gqlDiagram.layoutData.labelLayoutData,
    data,
    `${style.borderSize}px ${style.borderStyle} ${style.borderColor}`
  );

  const node: Node<RectangleWithExternalLabelNodeData> = {
    id,
    type: 'rectangleWithExternalLabelNode',
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
  }

  return node;
};

export class RectangleWithExternalLabelNodeConverter implements INodeConverter {
  canHandle(gqlNode: GQLNode<GQLNodeStyle>) {
    return gqlNode.style.__typename === 'RectangleWithExternalLabelNodeStyle';
  }

  handle(
    convertEngine: IConvertEngine,
    gqlDiagram: GQLDiagram,
    gqlNode: GQLNode<GQLRectangleWithExternalLabelNodeStyle>,
    gqlEdges: GQLEdge[],
    parentNode: GQLNode<GQLNodeStyle> | null,
    isBorderNode: boolean,
    nodes: Node[],
    diagramDescription: GQLDiagramDescription,
    nodeDescriptions: GQLNodeDescription[]
  ) {
    const nodeDescription = nodeDescriptions.find((description) => description.id === gqlNode.descriptionId);
    if (nodeDescription) {
      nodes.push(
        toRectangleWithExternalLabelNode(gqlDiagram, gqlNode, parentNode, nodeDescription, isBorderNode, gqlEdges)
      );
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
  }
}

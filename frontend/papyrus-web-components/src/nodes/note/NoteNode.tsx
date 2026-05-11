/*****************************************************************************
 * Copyright (c) 2023, 2026 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 224
 *****************************************************************************/

import { getCSSColor } from '@eclipse-sirius/sirius-components-core';
import {
  ConnectionCreationHandles,
  ConnectionHandles,
  ConnectionTargetHandle,
  DecoratorContainer,
  defaultHeight,
  defaultWidth,
  DiagramContext,
  DiagramContextValue,
  useConnectorNodeStyle,
  useDrop,
  useDropNodeStyle,
  useRefreshConnectionHandles,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Theme, useTheme } from '@mui/material/styles';
import { Node, NodeProps, NodeResizer, useReactFlow } from '@xyflow/react';
import React, { memo, useContext } from 'react';
import { NoteLabel } from './NoteLabel';
import { NoteNodeData } from './NoteNode.types';

const resizeLineStyle = (theme: Theme): React.CSSProperties => {
  return { borderWidth: theme.spacing(0.15) };
};

const resizeHandleStyle = (theme: Theme): React.CSSProperties => {
  return {
    width: theme.spacing(1),
    height: theme.spacing(1),
    borderRadius: '100%',
  };
};

const noteNodeStyle = (
  theme: Theme,
  style: React.CSSProperties,
  selected: boolean,
  hovered: boolean,
  faded: boolean
): React.CSSProperties => {
  const noteNodeStyle: React.CSSProperties = {
    display: 'flex',
    padding: '0px',
    width: '100%',
    height: '100%',
    position: 'relative',
    opacity: faded ? '0.4' : '',
    ...style,
    // No border nor background color: this is handled by the SVG image
    border: 'none',
    backgroundColor: 'transparent',
  };

  if (selected || hovered) {
    noteNodeStyle.outline = `${theme.palette.selected} solid 1px`;
  }

  return noteNodeStyle;
};

const svgPathStyle = (theme: Theme, style: React.CSSProperties, faded: boolean): React.CSSProperties => {
  const svgPathStyle: React.CSSProperties = {
    stroke: getCSSColor(String(style.borderColor), theme),
    fill: getCSSColor(String(style.background), theme),
    fillOpacity: faded ? '0.4' : '1',
    strokeOpacity: faded ? '0.4' : '1',
    strokeWidth: style.borderWidth,
    vectorEffect: 'non-scaling-stroke',
  };
  return svgPathStyle;
};

export const NoteNode = memo(({ data, id, selected, dragging }: NodeProps<Node<NoteNodeData>>) => {
  const { readOnly } = useContext<DiagramContextValue>(DiagramContext);
  const theme = useTheme();
  const { onDrop, onDragOver } = useDrop();
  const { style: connectionFeedbackStyle } = useConnectorNodeStyle(id, data.nodeDescription.id);
  const { style: dropFeedbackStyle } = useDropNodeStyle(
    data.isDropNodeTarget,
    data.isDragNodeSource,
    data.isDropNodeCandidate,
    dragging
  );
  const { getNodes } = useReactFlow<Node<NoteNodeData>>();
  const node = getNodes().find((node) => node.id === id);
  const nodeHeight = node?.height ?? defaultHeight;
  const nodeWidth = node?.width ?? defaultWidth;

  const handleOnDrop = (event: React.DragEvent) => {
    onDrop(event, id);
  };

  const updatedLabel: any = {
    ...data.insideLabel,
    style: {
      ...data?.insideLabel?.style,
      paddingLeft: parseInt(data.style.borderWidth?.toString() ?? '0') + 8 + 'px',
      paddingTop: parseInt(data.style.borderWidth?.toString() ?? '0') + 8 + 'px',
      paddingRight: parseInt(data.style.borderWidth?.toString() ?? '1') / 2 + 20 + 'px',
      paddingBottom: parseInt(data.style.borderWidth?.toString() ?? '0') + 8 + 'px',
      //justifyContent: 'left',
    },
  };

  useRefreshConnectionHandles(id, data.connectionHandles);

  const borderOffset = data.style.borderWidth ? parseInt(data.style.borderWidth.toString()) / 2 : 0;
  return (
    <>
      {data.nodeDescription?.userResizable !== 'NONE' && !readOnly ? (
        <NodeResizer
          handleStyle={{ ...resizeHandleStyle(theme) }}
          lineStyle={{ ...resizeLineStyle(theme) }}
          color={theme.palette.selected}
          isVisible={!!selected}
          keepAspectRatio={data.nodeDescription?.keepAspectRatio}
        />
      ) : null}
      <div
        style={{
          ...noteNodeStyle(theme, data.style, !!selected, data.isHovered, data.faded),
          ...connectionFeedbackStyle,
          ...dropFeedbackStyle,
        }}
        onDragOver={onDragOver}
        onDrop={handleOnDrop}
        data-testid={`Note - ${data?.insideLabel?.text}`}>
        <div
          style={{
            width: '100%',
            height: '100%',
            position: 'absolute',
            top: '0px',
            left: '0px',
            zIndex: '-1',
          }}>
          <svg viewBox={`0 0 ${nodeWidth} ${nodeHeight}`}>
            <path
              style={svgPathStyle(theme, data.style, data.faded)}
              d={`M ${borderOffset},${borderOffset} H ${nodeWidth - 15} L ${nodeWidth - borderOffset} 15 V ${
                nodeHeight - borderOffset
              } H ${borderOffset} Z`}
            />
            <path
              style={{
                ...svgPathStyle(theme, data.style, data.faded),
                fillOpacity: 0,
              }}
              d={`M ${nodeWidth - 15},${borderOffset} V 15 H ${nodeWidth - borderOffset}`}
            />
          </svg>
        </div>
        {data.insideLabel ? <NoteLabel diagramElementId={id} label={updatedLabel} faded={data.faded} /> : null}
        <DecoratorContainer decorators={data.decorators}></DecoratorContainer>
        {!!selected ? <ConnectionCreationHandles nodeId={id} /> : null}
        <ConnectionTargetHandle nodeId={id} nodeDescription={data.nodeDescription} isHovered={data.isHovered} />
        <ConnectionHandles connectionHandles={data.connectionHandles} />
      </div>
    </>
  );
});

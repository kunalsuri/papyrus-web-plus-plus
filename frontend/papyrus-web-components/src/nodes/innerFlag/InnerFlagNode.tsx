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

import { getCSSColor } from '@eclipse-sirius/sirius-components-core';
import {
  ConnectionCreationHandles,
  ConnectionHandles,
  ConnectionTargetHandle,
  DecoratorContainer,
  DiagramContext,
  DiagramContextValue,
  Label,
  useDrop,
  useDropNodeStyle,
  useConnectionLineNodeStyle,
  useRefreshConnectionHandles,
  useConnectorNodeStyle,
  defaultHeight,
  defaultWidth,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Theme, useTheme } from '@mui/material/styles';
import React, { memo, useContext } from 'react';
import { Node, NodeProps, NodeResizer, useReactFlow } from '@xyflow/react';
import { InnerFlagNodeData } from './InnerFlagNode.types';

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

const innerFlagNodeStyle = (
  theme: Theme,
  style: React.CSSProperties,
  selected: boolean,
  hovered: boolean,
  faded: boolean
): React.CSSProperties => {
  const innerFlagNodeStyle: React.CSSProperties = {
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
    innerFlagNodeStyle.outline = `${theme.palette.selected} solid 1px`;
  }

  return innerFlagNodeStyle;
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

export const InnerFlagNode = memo(({ data, id, selected, dragging }: NodeProps<Node<InnerFlagNodeData>>) => {
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
  const { style: connectionLineActiveNodeStyle } = useConnectionLineNodeStyle(data.connectionLinePositionOnNode);
  const { getNodes } = useReactFlow<Node<InnerFlagNodeData>>();
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
      paddingLeft: parseInt(data.style.borderWidth?.toString() ?? '0') + 20 + 8 + 'px',
      paddingTop: parseInt(data.style.borderWidth?.toString() ?? '0') + 8 + 'px',
      paddingRight: parseInt(data.style.borderWidth?.toString() ?? '1') / 2 + 8 + 'px',
      paddingBottom: parseInt(data.style.borderWidth?.toString() ?? '0') + 8 + 'px',
      height: '100%',
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
          ...innerFlagNodeStyle(theme, data.style, !!selected, data.isHovered, data.faded),
          ...connectionFeedbackStyle,
          ...dropFeedbackStyle,
          ...connectionLineActiveNodeStyle,
        }}
        onDragOver={onDragOver}
        onDrop={handleOnDrop}
        data-testid={`InnerFlag - ${data?.insideLabel?.text}`}>
        <div style={{ position: 'absolute', inset: '0px' }}>
          {data.insideLabel ? <Label diagramElementId={id} label={updatedLabel} faded={data.faded} /> : null}
        </div>
        <DecoratorContainer decorators={data.decorators}></DecoratorContainer>
        {!!selected ? <ConnectionCreationHandles nodeId={id} /> : null}
        <ConnectionTargetHandle nodeId={id} nodeDescription={data.nodeDescription} isHovered={data.isHovered} />
        <ConnectionHandles connectionHandles={data.connectionHandles} />
        <svg viewBox={`0 0 ${nodeWidth} ${nodeHeight}`}>
          <path
            style={svgPathStyle(theme, data.style, data.faded)}
            d={`M ${borderOffset},${borderOffset} H ${nodeWidth - borderOffset} V ${
              nodeHeight - borderOffset
            } H ${borderOffset} L ${20}, ${nodeHeight / 2} Z`}
          />
        </svg>
      </div>
    </>
  );
});

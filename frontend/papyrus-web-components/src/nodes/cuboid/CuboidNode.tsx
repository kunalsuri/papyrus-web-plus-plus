/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo.
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
  DiagramContext,
  DiagramContextValue,
  DiagramElementPalette,
  Label,
  useDrop,
  useDropNodeStyle,
  useConnectionLineNodeStyle,
  useRefreshConnectionHandles,
  useConnectorNodeStyle,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Theme, useTheme } from '@mui/material/styles';
import React, { memo, useContext } from 'react';
import { Node, NodeProps, NodeResizer, useReactFlow } from '@xyflow/react';
import { CuboidNodeData } from './CuboidNode.types';

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

const cuboidNodeStyle = (
  theme: Theme,
  style: React.CSSProperties,
  selected: boolean,
  hovered: boolean,
  faded: boolean
): React.CSSProperties => {
  const cuboidNodeStyle: React.CSSProperties = {
    display: 'flex',
    padding: '0px',
    width: '100%',
    height: '100%',
    opacity: faded ? '0.4' : '',
    ...style,
    // No border nor background color: this is handled by the SVG image
    border: 'none',
    backgroundColor: 'transparent',
  };

  if (selected || hovered) {
    cuboidNodeStyle.outline = `${theme.palette.selected} solid 1px`;
  }

  return cuboidNodeStyle;
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

// The number of px reserved on the right & top of the cuboid node to draw perspective lines.
const cuboidBorder: number = 20;

export const CuboidNode = memo(({ data, id, selected, dragging }: NodeProps<Node<CuboidNodeData>>) => {
  const { readOnly } = useContext<DiagramContextValue>(DiagramContext);
  const theme = useTheme();
  const { onDrop, onDragOver } = useDrop();
  const { style: connectionFeedbackStyle } = useConnectorNodeStyle(id, data.nodeDescription.id);
  const { style: dropFeedbackStyle } = useDropNodeStyle(data.isDropNodeTarget, data.isDropNodeCandidate, dragging);
  const { style: connectionLineActiveNodeStyle } = useConnectionLineNodeStyle(data.connectionLinePositionOnNode);

  const { getNodes } = useReactFlow<Node<CuboidNodeData>>();
  const node = getNodes().find((node) => node.id === id);

  const handleOnDrop = (event: React.DragEvent) => {
    onDrop(event, id);
  };

  useRefreshConnectionHandles(id, data.connectionHandles);

  const borderOffset = data.style.borderWidth ? parseInt(data.style.borderWidth.toString()) / 2 : 0;

  return (
    <>
      {data.nodeDescription?.userResizable && !readOnly ? (
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
          ...cuboidNodeStyle(theme, data.style, !!selected, data.isHovered, data.faded),
          ...connectionFeedbackStyle,
          ...dropFeedbackStyle,
          ...connectionLineActiveNodeStyle,
        }}
        onDragOver={onDragOver}
        onDrop={handleOnDrop}
        data-testid={`Cuboid - ${data?.insideLabel?.text}`}>
        <div
          // Add a 1px space around the absolute div to make sure it doesn't overlap with the cuboid SVG stroke.
          style={{
            position: 'absolute',
            top: cuboidBorder + 1 + 'px',
            bottom: '1px',
            right: cuboidBorder + 1 + 'px',
            left: '1px',
          }}>
          <div>
            {/* Label */}
            {data.insideLabel ? <Label diagramElementId={id} label={data.insideLabel} faded={data.faded} /> : null}
          </div>

          <div style={{ borderTop: `1px solid ${getCSSColor(String(data.style.borderColor), theme)}` }}>
            {/* Children */}
          </div>
        </div>
        {!!selected ? (
          <DiagramElementPalette
            diagramElementId={id}
            targetObjectId={data.targetObjectId}
            labelId={data.insideLabel ? data.insideLabel.id : null}
          />
        ) : null}
        {!!selected ? <ConnectionCreationHandles nodeId={id} /> : null}
        <ConnectionTargetHandle nodeId={id} nodeDescription={data.nodeDescription} isHovered={data.isHovered} />
        <ConnectionHandles connectionHandles={data.connectionHandles} />
        <svg viewBox={`0 0 ${node.width} ${node.height}`}>
          {/* This path represents the external borders of the cuboid */}
          <path
            style={svgPathStyle(theme, data.style, data.faded)}
            d={`M ${borderOffset}, ${borderOffset + cuboidBorder} L ${cuboidBorder + borderOffset}, ${borderOffset} H ${
              node.width - borderOffset
            } V ${node.height - (cuboidBorder + borderOffset)} L ${node.width - (cuboidBorder + borderOffset)}, ${
              node.height - borderOffset
            } H ${borderOffset} Z`}
          />

          {/* This path represents the top and right borders of the cuboid front face */}
          <path
            style={svgPathStyle(theme, data.style, data.faded)}
            d={`M ${borderOffset},${borderOffset + cuboidBorder} H ${node.width - (borderOffset + cuboidBorder)} V ${
              node.height - borderOffset - 0.3
            }`}
            // Add a 0.3 offset to avoid a glitch at the junction of the lines on the bottom right corner of the cuboid.
          />

          {/* This path represents the diagonal edges on the top-right of the cuboid */}
          <path
            style={svgPathStyle(theme, data.style, data.faded)}
            d={`M ${node.width - (borderOffset + cuboidBorder)},${borderOffset + cuboidBorder} L ${
              node.width - borderOffset
            }, ${borderOffset}`}
          />
        </svg>
      </div>
    </>
  );
});

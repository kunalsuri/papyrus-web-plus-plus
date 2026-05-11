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
  useRefreshConnectionHandles,
  useConnectorNodeStyle,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Theme, useTheme } from '@mui/material/styles';
import React, { memo, useContext } from 'react';
import { Node, NodeProps, NodeResizer } from '@xyflow/react';
import { RectangleWithExternalLabelNodeData } from './RectangleWithExternalLabelNode.types';

const rectangleWithExternalLabelInnerRectangleStyle = (
  theme: Theme,
  style: React.CSSProperties,
  selected: boolean,
  hovered: boolean,
  faded: boolean
): React.CSSProperties => {
  const rectangleWithExternalLabelNodeStyle: React.CSSProperties = {
    display: 'flex',
    padding: '8px',
    width: '100%',
    height: '100%',
    position: 'relative',
    opacity: faded ? '0.4' : '',
    ...style,
    borderColor: getCSSColor(String(style.borderColor), theme),
    background: getCSSColor(String(style.background), theme),
  };

  if (selected || hovered) {
    rectangleWithExternalLabelNodeStyle.outline = `${theme.palette.selected} solid 1px`;
  }

  return rectangleWithExternalLabelNodeStyle;
};

const rectangleWithExternalLabelNodeStyle = (
  theme: Theme,
  style: React.CSSProperties,
  selected: boolean,
  hovered: boolean,
  faded: boolean
): React.CSSProperties => {
  const rectangleWithExternalLabelNodeStyle: React.CSSProperties = {
    display: 'flex',
    padding: '0px',
    width: '100%',
    height: '100%',
    opacity: faded ? '0.4' : '',
    ...style,
    border: 'none',
    backgroundColor: 'transparent',
  };

  if (selected || hovered) {
    rectangleWithExternalLabelNodeStyle.outline = `${theme.palette.selected} solid 1px`;
  }

  return rectangleWithExternalLabelNodeStyle;
};

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

export const RectangleWithExternalLabelNode = memo(
  ({ data, id, selected, dragging }: NodeProps<Node<RectangleWithExternalLabelNodeData>>) => {
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

    const handleOnDrop = (event: React.DragEvent) => {
      onDrop(event, id);
    };

    useRefreshConnectionHandles(id, data.connectionHandles);

    return (
      <>
        {data.nodeDescription?.userResizable !== 'NONE' && !readOnly ? (
          <NodeResizer
            handleStyle={{ ...resizeHandleStyle(theme) }}
            lineStyle={{ ...resizeLineStyle(theme) }}
            color={theme.palette.selected}
            isVisible={!!selected}
            // Force false here to handle mutualized NodeDescriptions for SMD PseudoStates.
            // The other nodes need to have the aspect ratio, but RectanguleWithExternalLabelNodes
            // should not have it.
            keepAspectRatio={false}
          />
        ) : null}
        <div
          style={{
            ...rectangleWithExternalLabelNodeStyle(theme, data.style, !!selected, data.isHovered, data.faded),
            ...connectionFeedbackStyle,
            ...dropFeedbackStyle,
          }}
          onDragOver={onDragOver}
          onDrop={handleOnDrop}
          data-testid={`RectangleWithExternalLabel - ${data?.insideLabel?.text}`}>
          {data.insideLabel ? <Label diagramElementId={id} label={data.insideLabel} faded={data.faded} /> : null}
          <DecoratorContainer decorators={data.decorators}></DecoratorContainer>
          {!!selected ? <ConnectionCreationHandles nodeId={id} /> : null}
          <ConnectionTargetHandle nodeId={id} nodeDescription={data.nodeDescription} isHovered={data.isHovered} />
          <ConnectionHandles connectionHandles={data.connectionHandles} />
          <div
            style={{
              ...rectangleWithExternalLabelInnerRectangleStyle(
                theme,
                data.style,
                !!selected,
                data.isHovered,
                data.faded
              ),
              ...connectionFeedbackStyle,
              ...dropFeedbackStyle,
            }}
            onDragOver={onDragOver}
            onDrop={handleOnDrop}
            data-testid={`RectangleWithExternalLabel - ${data?.insideLabel?.text}`}></div>
        </div>
      </>
    );
  }
);

/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 218
 *****************************************************************************/

import { getCSSColor, ServerContext, ServerContextValue, useMultiToast } from '@eclipse-sirius/sirius-components-core';
import {
  DiagramContext,
  DiagramContextValue,
  useConnectorNodeStyle,
  useDropNodeStyle,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Theme, useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { Node, NodeProps, NodeResizer } from '@xyflow/react';
import { memo, useContext, useEffect, useState } from 'react';
import { CustomImageNodeData } from './CustomImageNode.types';

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

const defaultErrorMessage = 'The provided shape for this node is not a valid image';

interface CustomImageNodeState {
  url: string;
  validImage: boolean;
}

const customImageNodeStyle = (
  theme: Theme,
  style: React.CSSProperties,
  selected: boolean,
  hovered: boolean,
  faded: boolean
): React.CSSProperties => {
  const customImageNodeStyle: React.CSSProperties = {
    display: 'flex',
    padding: '0px',
    width: '100%',
    height: '100%',
    opacity: faded ? '0.4' : '',
    ...style,
    border: 'none',
    background: getCSSColor(String(style.background), theme),
    alignItems: 'center',
    justifyContent: 'center',
  };

  if (selected || hovered) {
    customImageNodeStyle.outline = `${theme.palette.selected} solid 1px`;
  }

  return customImageNodeStyle;
};

export const CustomImageNode = memo(({ data, id, selected, dragging }: NodeProps<Node<CustomImageNodeData>>) => {
  const { readOnly } = useContext<DiagramContextValue>(DiagramContext);
  const theme = useTheme();
  const { addErrorMessage } = useMultiToast();
  const { style: connectionFeedbackStyle } = useConnectorNodeStyle(id, data.nodeDescription.id);
  const { style: dropFeedbackStyle } = useDropNodeStyle(data.isDropNodeTarget, data.isDropNodeCandidate, dragging);
  const { httpOrigin } = useContext<ServerContextValue>(ServerContext);
  const [state, setState] = useState<CustomImageNodeState>({
    url: data.shape && data.shape !== '' ? httpOrigin + data.shape : '',
    validImage: data.shape !== undefined && data.shape !== '',
  });

  const onErrorLoadingImage = () => {
    setState((prevState) => ({ ...prevState, validImage: false }));
    addErrorMessage(defaultErrorMessage);
  };

  useEffect(() => {
    setState((prevState) => ({
      ...prevState,
      url: data.shape && data.shape !== '' ? httpOrigin + data.shape : '',
      validImage: data.shape !== undefined && data.shape !== '',
    }));
  }, [data.shape]);

  return (
    <>
      {data.nodeDescription?.userResizable && !readOnly ? (
        <NodeResizer
          handleStyle={{ ...resizeHandleStyle(theme) }}
          lineStyle={{ ...resizeLineStyle(theme) }}
          color={theme.palette.selected}
          isVisible={selected}
          keepAspectRatio={data.nodeDescription?.keepAspectRatio}
        />
      ) : null}
      <div
        style={{
          ...customImageNodeStyle(theme, data.style, selected, data.isHovered, data.faded),
          ...connectionFeedbackStyle,
          ...dropFeedbackStyle,
        }}>
        {state.validImage ? (
          <img id={id} src={state.url} width="25%" onError={onErrorLoadingImage} />
        ) : (
          <Typography data-testid="custom-image-node-no-image" variant="caption">
            No image
          </Typography>
        )}
      </div>
    </>
  );
});

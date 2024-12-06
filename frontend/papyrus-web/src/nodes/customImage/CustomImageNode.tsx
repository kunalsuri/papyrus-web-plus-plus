/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo, Artal Technologies.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 218
 *  Aurelien Didier (Artal Technologies) - Issue 218
 *****************************************************************************/
import { getCSSColor, ServerContext, ServerContextValue, useMultiToast } from '@eclipse-sirius/sirius-components-core';
import { DiagramContext, DiagramContextValue } from '@eclipse-sirius/sirius-components-diagrams';
import { Theme, useTheme } from '@mui/material/styles';
import { ResizeControlVariant } from '@xyflow/system';
import Typography from '@mui/material/Typography';
import { Edge, Node, NodeProps, useStoreApi, NodeResizeControl } from '@xyflow/react';
import { memo, useContext, useEffect, useState } from 'react';
import { CustomImageNodeData, NodeComponentsMap } from './CustomImageNode.types';
import { EdgeData, NodeData } from '@eclipse-sirius/sirius-components-diagrams';

const resizeControlLineStyle = (theme: Theme): React.CSSProperties => {
  return { borderColor: 'transparent', borderWidth: theme.spacing(0.25) };
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

  if (!!selected || hovered) {
    customImageNodeStyle.outline = `${theme.palette.selected} solid 1px`;
  }
  return customImageNodeStyle;
};

export const CustomImageNode: NodeComponentsMap['customImageNode'] = memo(
  ({ data, id, selected, dragging }: NodeProps<Node<CustomImageNodeData>>) => {
    const { readOnly } = useContext<DiagramContextValue>(DiagramContext);
    const theme = useTheme();
    const { addErrorMessage } = useMultiToast();
    const { httpOrigin } = useContext<ServerContextValue>(ServerContext);
    const [state, setState] = useState<CustomImageNodeState>({
      url: data.shape && data.shape !== '' ? httpOrigin + data.shape : '',
      validImage: data.shape !== undefined && data.shape !== '',
    });

    const onErrorLoadingImage = () => {
      setState((prevState) => ({ ...prevState, validImage: false }));
      addErrorMessage(defaultErrorMessage);
    };

    const storeApi = useStoreApi<Node<NodeData>, Edge<EdgeData>>();
    const getNodeById = (id: string) => storeApi.getState().nodeLookup.get(id);
    const node = getNodeById(id);

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
          <>
            <NodeResizeControl
              variant={ResizeControlVariant.Line}
              position={'top'}
              style={{ ...resizeControlLineStyle(theme) }}
            />
            <NodeResizeControl
              variant={ResizeControlVariant.Line}
              position={'bottom'}
              style={{ ...resizeControlLineStyle(theme) }}
            />
          </>
        ) : null}
        <div
          style={{
            ...customImageNodeStyle(theme, data.style, selected, data.isHovered, data.faded),
          }}>
          {state.validImage ? (
            <img
              id={id}
              src={state.url}
              width={node.width - 5}
              height={node.height - 5}
              draggable={false}
              onError={onErrorLoadingImage}
              style={{
                objectFit: 'contain',
                display: 'block',
                margin: 'auto',
              }}
            />
          ) : (
            <Typography data-testid="custom-image-node-no-image" variant="caption">
              No image
            </Typography>
          )}
        </div>
      </>
    );
  }
);

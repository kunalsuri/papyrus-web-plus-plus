/*****************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 210
 *****************************************************************************/

import { gql, useMutation } from '@apollo/client';
import { ServerContext, ServerContextValue, getCSSColor, useMultiToast } from '@eclipse-sirius/sirius-components-core';
import {
  PropertySectionComponentProps,
  PropertySectionLabel,
  getTextDecorationLineValue,
} from '@eclipse-sirius/sirius-components-forms';
import { UploadImageModal, useProjectImages } from '@eclipse-sirius/sirius-web-application';
import DeleteIcon from '@mui/icons-material/Delete';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import IconButton from '@mui/material/IconButton';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import Typography from '@mui/material/Typography';
import { useContext, useEffect, useState } from 'react';
import { makeStyles } from 'tss-react/mui';
import { AddImageIcon } from './AddImageIcon';
import {
  CustomImageWidgetState,
  CustomImageWidgetStyleProps,
  GQLCustomImageWidget,
  GQLErrorPayload,
  GQLNewUuidData,
  GQLNewUuidInput,
  GQLNewUuidPayload,
  GQLNewUuidVariables,
  GQLRemoveUuidData,
  GQLRemoveUuidInput,
  GQLRemoveUuidPayload,
  GQLRemoveUuidVariables,
  GQLSuccessPayload,
  SelectImageModalProps,
} from './CustomImageFragment.types';

export const newUuidMutation = gql`
  mutation newUuid($input: NewUuidInput!) {
    newUuid(input: $input) {
      __typename
      ... on ErrorPayload {
        messages {
          body
          level
        }
      }
      ... on SuccessPayload {
        messages {
          body
          level
        }
      }
    }
  }
`;

export const removeUuidMutation = gql`
  mutation removeUuid($input: RemoveUuidInput!) {
    removeUuid(input: $input) {
      __typename
      ... on ErrorPayload {
        messages {
          body
          level
        }
      }
      ... on SuccessPayload {
        messages {
          body
          level
        }
      }
    }
  }
`;

const defaultErrorMessage = 'An unexpected error has occurred, please refresh the page';

const useStylesSection = makeStyles<CustomImageWidgetStyleProps>()(
  (theme, { color, fontSize, italic, bold, underline, strikeThrough }) => ({
    labelItemStyle: {
      color: color ? getCSSColor(color, theme) : null,
      fontSize: fontSize ? fontSize : null,
      fontStyle: italic ? 'italic' : null,
      fontWeight: bold ? 'bold' : null,
      textDecorationLine: getTextDecorationLineValue(underline, strikeThrough),
    },
    toolbar: {
      marginLeft: 'auto',
      display: 'flex',
      alignItems: 'center',
    },
  })
);

export const CustomImageSection = ({
  widget,
  editingContextId,
  formId,
  readOnly,
}: PropertySectionComponentProps<GQLCustomImageWidget>) => {
  const { addErrorMessage, addMessages } = useMultiToast();
  const { httpOrigin } = useContext<ServerContextValue>(ServerContext);
  const [state, setState] = useState<CustomImageWidgetState>({
    modal: null,
    url: widget.currentUuid ? httpOrigin + widget.currentUuid : '',
    validImage: widget.currentUuid !== undefined && widget.currentUuid !== '',
  });
  const { data, loading, refreshImages } = useProjectImages(editingContextId);
  const [newUuidApi, { loading: newUuidLoading, data: newUuidData, error: newUuidError }] = useMutation<
    GQLNewUuidData,
    GQLNewUuidVariables
  >(newUuidMutation);
  const [removeUuidApi, { loading: removeUuidLoading, data: removeUuidData, error: removeUuidError }] = useMutation<
    GQLRemoveUuidData,
    GQLRemoveUuidVariables
  >(removeUuidMutation);

  const isErrorPayload = (payload: GQLNewUuidPayload | GQLRemoveUuidPayload): payload is GQLErrorPayload =>
    payload.__typename === 'ErrorPayload';
  const isSuccessPayload = (payload: GQLNewUuidPayload | GQLRemoveUuidPayload): payload is GQLSuccessPayload =>
    payload.__typename === 'SuccessPayload';

  const styleProps: CustomImageWidgetStyleProps = {
    color: widget.style?.color ?? null,
    fontSize: widget.style?.fontSize ?? null,
    italic: widget.style?.italic ?? null,
    bold: widget.style?.bold ?? null,
    underline: widget.style?.underline ?? null,
    strikeThrough: widget.style?.strikeThrough ?? null,
  };
  const { classes } = useStylesSection(styleProps);

  const onErrorLoadingImage = () => {
    setState((prevState) => ({ ...prevState, validImage: false }));
    addErrorMessage(defaultErrorMessage);
  };
  const handleAddImageButton = () => setState((prevState) => ({ ...prevState, modal: 'Upload' }));
  const handleSelectImageButton = () => setState((prevState) => ({ ...prevState, modal: 'Select' }));
  const handleRemoveImageButton = () => {
    const input: GQLRemoveUuidInput = {
      id: crypto.randomUUID(),
      editingContextId,
      representationId: formId,
      customImageId: widget.id,
      removeUuid: '',
    };
    const variables: GQLRemoveUuidVariables = { input };
    removeUuidApi({ variables });
  };
  const closeModal = () => setState((prevState) => ({ ...prevState, modal: null }));

  const onImageSelected = (newUuid: string) => {
    const input: GQLNewUuidInput = {
      id: crypto.randomUUID(),
      editingContextId,
      representationId: formId,
      customImageId: widget.id,
      newUuid: newUuid,
    };
    const variables: GQLNewUuidVariables = { input };
    newUuidApi({ variables });
  };

  useEffect(() => {
    setState((prevState) => ({
      ...prevState,
      url: widget.currentUuid ? httpOrigin + widget.currentUuid : '',
      validImage: widget.currentUuid !== undefined && widget.currentUuid !== '',
    }));
  }, [widget.currentUuid]);

  useEffect(() => {
    if (!newUuidLoading) {
      if (newUuidError) {
        addErrorMessage(defaultErrorMessage);
      }
      if (newUuidData) {
        const { newUuid } = newUuidData;
        if (isErrorPayload(newUuid) || isSuccessPayload(newUuid)) {
          addMessages(newUuid.messages);
        }
      }
    }
  }, [newUuidLoading, newUuidError, newUuidData]);

  useEffect(() => {
    if (!removeUuidLoading) {
      if (removeUuidError) {
        addErrorMessage(defaultErrorMessage);
      }
      if (removeUuidData) {
        const { removeUuid } = removeUuidData;
        if (isErrorPayload(removeUuid) || isSuccessPayload(removeUuid)) {
          addMessages(removeUuid.messages);
        }
      }
    }
  }, [removeUuidLoading, removeUuidError, removeUuidData]);

  let modal = null;
  if (state.modal === 'Upload') {
    modal = (
      <UploadImageModal
        projectId={editingContextId}
        onImageUploaded={() => {
          refreshImages();
          closeModal();
        }}
        onClose={closeModal}
      />
    );
  } else if (state.modal === 'Select') {
    let images = [];
    if (!loading && data) {
      images = data?.viewer.project?.images ?? [];
    }
    modal = (
      <SelectImageModal
        currentUuid={widget.currentUuid}
        images={images}
        onImageSelected={onImageSelected}
        onClose={closeModal}
      />
    );
  }

  return (
    <>
      <PropertySectionLabel
        editingContextId={editingContextId}
        formId={formId}
        widget={widget}
        data-testid={widget.label}
      />
      <div data-testid={`custom-image-${widget.label}`} style={{ display: 'flex' }}>
        {state.validImage ? (
          <img id={widget.id} src={state.url} width="25%" onError={onErrorLoadingImage} />
        ) : (
          <Typography data-testid="custom-image-widget-no-image" variant="caption">
            No image
          </Typography>
        )}
        <div className={classes.toolbar} data-testid="custom-image-widget-toolbar">
          {modal}
          <IconButton data-testid="custom-image-widget-add" onClick={handleAddImageButton} size="small">
            <AddImageIcon />
          </IconButton>
          <IconButton data-testid="custom-image-widget-select" onClick={handleSelectImageButton} size="small">
            <MoreHorizIcon />
          </IconButton>
          <IconButton data-testid="custom-image-widget-remove" onClick={handleRemoveImageButton} size="small">
            <DeleteIcon />
          </IconButton>
        </div>
      </div>
    </>
  );
};

const useStylesModal = makeStyles()((theme) => ({
  form: {
    display: 'flex',
    flexDirection: 'column',
    paddingTop: theme.spacing(1),
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
    '& > *': {
      marginBottom: theme.spacing(2),
    },
  },
}));

export const SelectImageModal = (props: SelectImageModalProps) => {
  const { currentUuid, images, onImageSelected, onClose } = props;
  const { classes } = useStylesModal();
  const [symbol, setSymbol] = useState(currentUuid);
  const { httpOrigin } = useContext<ServerContextValue>(ServerContext);

  const handleChange = (event) => {
    setSymbol(event.target.value);
  };

  return (
    <>
      <Dialog open={true} onClose={onClose} aria-labelledby="dialog-title" fullWidth>
        <DialogTitle id="dialog-title">Select a Symbol</DialogTitle>
        <DialogContent>
          <form id="select-image-form-id" encType="multipart/form-data" className={classes.form}>
            <InputLabel>Symbol</InputLabel>
            <Select data-testid="select-image-select" value={symbol} onChange={handleChange} displayEmpty>
              <MenuItem data-testid="select-image-select-None" value={''}>
                None
              </MenuItem>
              {images.map((image, index) => {
                return (
                  <MenuItem data-testid={'select-image-select-' + image.label} key={index} value={image.url}>
                    <img
                      id={index.toString()}
                      src={httpOrigin + image.url}
                      width="5%"
                      style={{ marginLeft: '0.5rem', marginRight: '0.5rem' }}
                    />
                    {image.label}
                  </MenuItem>
                );
              })}
            </Select>
          </form>
        </DialogContent>
        <DialogActions>
          <Button
            variant="contained"
            color="primary"
            form="upload-form-id"
            data-testid="select-image-confirm"
            onClick={() => {
              onImageSelected(symbol);
              onClose();
            }}>
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

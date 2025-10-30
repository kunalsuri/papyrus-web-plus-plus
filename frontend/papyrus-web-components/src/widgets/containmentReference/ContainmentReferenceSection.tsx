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
 ***************************************************************************/

import { gql, useLazyQuery, useMutation } from '@apollo/client';
import { IconOverlay, getCSSColor, useMultiToast, useSelection } from '@eclipse-sirius/sirius-components-core';
import {
  PropertySectionComponentProps,
  PropertySectionLabel,
  getTextDecorationLineValue,
  useClickHandler,
} from '@eclipse-sirius/sirius-components-forms';
import {
  GQLErrorPayload,
  GQLReferenceWidgetStyle,
  GQLSuccessPayload,
} from '@eclipse-sirius/sirius-components-widget-reference';
import AddIcon from '@mui/icons-material/Add';
import { Typography } from '@mui/material';
import Chip from '@mui/material/Chip';
import IconButton from '@mui/material/IconButton';
import { useEffect, useState } from 'react';
import { makeStyles } from 'tss-react/mui';
import ReorderItemsDialog from '../dialogs/ReorderItemsDialog';
import {
  ContainmentReferenceDialogKind,
  GQLClickContainmentReferenceItemMutationData,
  GQLClickContainmentReferenceItemMutationVariables,
  GQLClickContainmentReferenceItemPayload,
  GQLContainmentReferenceItem,
  GQLContainmentReferenceWidget,
  GQLCreateElementInReferenceMutationData,
  GQLCreateElementInReferenceMutationVariables,
  GQLGetChildCreationDescriptionsQueryData,
  GQLGetChildCreationDescriptionsQueryVariables,
  GQLMoveContainmentReferenceItemMutationData,
  GQLMoveContainmentReferenceItemMutationVariables,
  GQLRemoveContainmentReferenceItemMutationData,
  GQLRemoveContainmentReferenceItemMutationVariables,
  GQLRemoveContainmentReferenceItemPayload,
} from './ContainmentReferenceFragment.types';
import ReorderIcon from './ReorderIcon';
import CreateNewChildDialog from './dialogs/CreateNewChildDialog';
import { ChildCreationDescription } from './dialogs/CreateNewChildDialog.types';

const useStyles = makeStyles<GQLReferenceWidgetStyle>()(
  (theme, { color, fontSize, italic, bold, underline, strikeThrough }) => ({
    labelItemStyle: {
      color: color ? getCSSColor(color, theme) : null,
      fontSize: fontSize ? fontSize : null,
      fontStyle: italic ? 'italic' : null,
      fontWeight: bold ? 'bold' : null,
      textDecorationLine: getTextDecorationLineValue(underline, strikeThrough),
    },
    chip: {
      margin: '3px',
    },
    toolbar: {
      marginLeft: 'auto',
      display: 'flex',
      alignItems: 'center',
    },
    empty: {
      color: '#B3BFC5',
    },
  })
);

export const clickContainmentReferenceItemMutation = gql`
  mutation clickContainmentReferenceItem($input: ClickContainmentReferenceItemInput!) {
    clickContainmentReferenceItem(input: $input) {
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

export const removeContainmentReferenceItemMutation = gql`
  mutation removeContainmentReferenceItem($input: RemoveContainmentReferenceItemInput!) {
    removeContainmentReferenceItem(input: $input) {
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

export const moveContainmentReferenceItemMutation = gql`
  mutation moveContainmentReferenceItem($input: MoveContainmentReferenceItemInput!) {
    moveContainmentReferenceItem(input: $input) {
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

const getChildCreationDescriptionsQuery = gql`
  query getChildCreationDescriptions(
    $editingContextId: ID!
    $containerId: ID!
    $referenceKind: String
    $descriptionId: String!
  ) {
    viewer {
      editingContext(editingContextId: $editingContextId) {
        referenceWidgetChildCreationDescriptions(
          containerId: $containerId
          referenceKind: $referenceKind
          descriptionId: $descriptionId
        ) {
          id
          label
          iconURL
        }
      }
    }
  }
`;

const createElementInReferenceMutation = gql`
  mutation createElementInReference($input: CreateElementInReferenceInput!) {
    createElementInReference(input: $input) {
      __typename
      ... on CreateElementInReferenceSuccessPayload {
        object {
          id
          label
          kind
        }
        messages {
          body
          level
        }
      }
      ... on ErrorPayload {
        messages {
          body
          level
        }
      }
    }
  }
`;

const isErrorPayload = (
  payload: GQLClickContainmentReferenceItemPayload | GQLRemoveContainmentReferenceItemPayload
): payload is GQLErrorPayload => payload.__typename === 'ErrorPayload';
const isSuccessPayload = (
  payload: GQLClickContainmentReferenceItemPayload | GQLRemoveContainmentReferenceItemPayload
): payload is GQLSuccessPayload => payload.__typename === 'SuccessPayload';

const ContainmentReferenceSection = ({
  editingContextId,
  formId,
  widget,
  readOnly,
}: PropertySectionComponentProps<GQLContainmentReferenceWidget>) => {
  const styleProps: GQLReferenceWidgetStyle = {
    color: widget.style?.color ?? null,
    fontSize: widget.style?.fontSize ?? null,
    italic: widget.style?.italic ?? null,
    bold: widget.style?.bold ?? null,
    underline: widget.style?.underline ?? null,
    strikeThrough: widget.style?.strikeThrough ?? null,
  };
  const { classes } = useStyles(styleProps);

  const [openDialog, setOpenDialog] = useState<ContainmentReferenceDialogKind | null>(null);
  const [childTypes, setChildTypes] = useState<ChildCreationDescription[]>([]);

  const { setSelection } = useSelection();

  const { addErrorMessage, addMessages } = useMultiToast();

  const [clickReferenceValue, { loading: clickLoading, error: clickError, data: clickData }] = useMutation<
    GQLClickContainmentReferenceItemMutationData,
    GQLClickContainmentReferenceItemMutationVariables
  >(clickContainmentReferenceItemMutation);
  const [removeContainmentReferenceItem, { loading: removeLoading, error: removeError, data: removeData }] =
    useMutation<GQLRemoveContainmentReferenceItemMutationData, GQLRemoveContainmentReferenceItemMutationVariables>(
      removeContainmentReferenceItemMutation
    );
  const [moveReferenceValue, { loading: moveLoading, error: moveError, data: moveData }] = useMutation<
    GQLMoveContainmentReferenceItemMutationData,
    GQLMoveContainmentReferenceItemMutationVariables
  >(moveContainmentReferenceItemMutation);
  const [
    getChildCreationDescription,
    {
      loading: childCreationDescriptionsLoading,
      data: childCreationDescriptionsData,
      error: childCreationDescriptionsError,
    },
  ] = useLazyQuery<GQLGetChildCreationDescriptionsQueryData, GQLGetChildCreationDescriptionsQueryVariables>(
    getChildCreationDescriptionsQuery
  );
  const [
    createElementInReference,
    { loading: createElementLoading, error: createElementError, data: createElementData },
  ] = useMutation<GQLCreateElementInReferenceMutationData, GQLCreateElementInReferenceMutationVariables>(
    createElementInReferenceMutation
  );

  useEffect(() => {
    if (!clickLoading) {
      if (clickError) {
        addErrorMessage('An unexpected error has occurred, please refresh the page');
      }
      if (clickData) {
        const { clickContainmentReferenceItem } = clickData;
        if (isErrorPayload(clickContainmentReferenceItem) || isSuccessPayload(clickContainmentReferenceItem)) {
          addMessages(clickContainmentReferenceItem.messages);
        }
      }
    }
  }, [clickLoading, clickError, clickData]);
  useEffect(() => {
    if (!removeLoading) {
      if (removeError) {
        addErrorMessage('An unexpected error has occurred, please refresh the page');
      }
      if (removeData) {
        const { removeContainmentReferenceItem } = removeData;
        if (isErrorPayload(removeContainmentReferenceItem) || isSuccessPayload(removeContainmentReferenceItem)) {
          addMessages(removeContainmentReferenceItem.messages);
        }
      }
    }
  }, [removeLoading, removeError, removeData]);
  useEffect(() => {
    if (!moveLoading) {
      if (moveError) {
        addErrorMessage('An unexpected error has occurred, please refresh the page');
      }
      if (moveData) {
        const { moveContainmentReferenceItem } = moveData;
        if (isErrorPayload(moveContainmentReferenceItem) || isSuccessPayload(moveContainmentReferenceItem)) {
          addMessages(moveContainmentReferenceItem.messages);
        }
      }
    }
  }, [moveLoading, moveError, moveData]);
  useEffect(() => {
    if (!childCreationDescriptionsLoading) {
      if (childCreationDescriptionsError) {
        addErrorMessage('An unexpected error has occurred, please refresh the page');
      }
      if (childCreationDescriptionsData) {
        const { referenceWidgetChildCreationDescriptions } = childCreationDescriptionsData.viewer.editingContext;
        if (referenceWidgetChildCreationDescriptions.length > 1) {
          setChildTypes(referenceWidgetChildCreationDescriptions);
          setOpenDialog('NEW_INSTANCE');
        } else if (referenceWidgetChildCreationDescriptions.length === 1) {
          callCreateElementInReference(referenceWidgetChildCreationDescriptions[0].id);
        }
      }
    }
  }, [childCreationDescriptionsLoading, childCreationDescriptionsData, childCreationDescriptionsError]);
  useEffect(() => {
    if (!createElementLoading) {
      if (createElementError) {
        addErrorMessage('An unexpected error has occurred, please refresh the page');
      }
      if (createElementData) {
        const { createElementInReference } = createElementData;
        if (isErrorPayload(createElementInReference) || isSuccessPayload(createElementInReference)) {
          const { messages } = createElementInReference;
          addMessages(messages);
        }
      }
    }
  }, [createElementLoading, createElementData, createElementError]);

  const callMoveContainmentReferenceItem = (valueId: string, fromIndex: number, toIndex: number) => {
    if (valueId && fromIndex !== -1 && toIndex !== -1) {
      const variables: GQLMoveContainmentReferenceItemMutationVariables = {
        input: {
          id: crypto.randomUUID(),
          editingContextId,
          representationId: formId,
          referenceWidgetId: widget.id,
          referenceItemId: valueId,
          fromIndex,
          toIndex,
        },
      };
      moveReferenceValue({ variables });
    }
  };

  const handleReferenceItemSimpleClick = (item: GQLContainmentReferenceItem) => {
    const { id } = item;
    setSelection({ entries: [{ id }] });
    if (item.hasClickAction) {
      const variables: GQLClickContainmentReferenceItemMutationVariables = {
        input: {
          id: crypto.randomUUID(),
          editingContextId,
          representationId: formId,
          referenceWidgetId: widget.id,
          referenceItemId: item.id,
          clickEventKind: 'SINGLE_CLICK',
        },
      };
      clickReferenceValue({ variables });
    }
  };
  const handleReferenceItemDoubleClick = (item: GQLContainmentReferenceItem) => {
    const { id } = item;
    setSelection({ entries: [{ id }] });
    if (item.hasClickAction) {
      const variables: GQLClickContainmentReferenceItemMutationVariables = {
        input: {
          id: crypto.randomUUID(),
          editingContextId,
          representationId: formId,
          referenceWidgetId: widget.id,
          referenceItemId: item.id,
          clickEventKind: 'DOUBLE_CLICK',
        },
      };
      clickReferenceValue({ variables });
    }
  };

  const callCreateElementInReference = (newChildDescriptionId: string) => {
    if (newChildDescriptionId !== null) {
      const input = {
        id: crypto.randomUUID(),
        editingContextId,
        representationId: formId,
        referenceWidgetId: widget.id,
        containerId: widget.ownerId,
        domainId: null,
        creationDescriptionId: newChildDescriptionId,
        descriptionId: widget.descriptionId,
      };
      createElementInReference({ variables: { input } });
    }
  };

  const handleCloseCreateNewChildDialog = (newChildDescriptionId: string | null) => {
    setChildTypes([]);
    setOpenDialog(null);
    callCreateElementInReference(newChildDescriptionId);
  };

  const clickHandler = useClickHandler<GQLContainmentReferenceItem>(
    handleReferenceItemSimpleClick,
    handleReferenceItemDoubleClick
  );

  const handleDeleteItem = (item: GQLContainmentReferenceItem) => {
    if (item?.id) {
      if (widget.referenceValues.find((value) => value.id === item.id)) {
        const variables: GQLRemoveContainmentReferenceItemMutationVariables = {
          input: {
            id: crypto.randomUUID(),
            editingContextId,
            representationId: formId,
            referenceWidgetId: widget.id,
            referenceItemId: item.id,
          },
        };
        removeContainmentReferenceItem({ variables });
      }
    }
  };

  const handleOpenCreateNewChildDialog = () => {
    getChildCreationDescription({
      variables: {
        editingContextId,
        containerId: widget.ownerId,
        referenceKind: widget.containmentReference.referenceKind,
        descriptionId: widget.descriptionId,
      },
    });
  };

  const getDialog = () => {
    if (!openDialog) return null;
    if (openDialog === 'REORDER') {
      return (
        <ReorderItemsDialog
          items={widget.referenceValues.map(({ label, id, iconURL }) => ({ label, id, iconURL }))}
          moveElement={callMoveContainmentReferenceItem}
          onClose={() => setOpenDialog(null)}
        />
      );
    } else if (openDialog === 'NEW_INSTANCE') {
      return <CreateNewChildDialog childTypes={childTypes} onClose={handleCloseCreateNewChildDialog} />;
    }
    return undefined;
  };

  const dialog: JSX.Element | null = getDialog();

  const canReorder =
    !readOnly && !widget.readOnly && widget.containmentReference.isMany && widget.referenceValues.length > 1;

  return (
    <>
      <div data-testid={`containment-reference-${widget.label}`}>
        <div style={{ display: 'flex' }}>
          <PropertySectionLabel
            editingContextId={editingContextId}
            formId={formId}
            widget={widget}
            data-testid={widget.label}
          />
          <div className={classes.toolbar} data-testid="containment-reference-toolbar">
            <IconButton
              data-testid="containment-reference-create-child"
              onClick={handleOpenCreateNewChildDialog}
              disabled={
                readOnly ||
                widget.readOnly ||
                (!widget.containmentReference.isMany && widget.referenceValues.length > 0)
              }
              size="small">
              <AddIcon />
            </IconButton>
            {widget.containmentReference.canMove && (
              <IconButton
                data-testid="containment-reference-reorder-children"
                onClick={() => setOpenDialog('REORDER')}
                disabled={!canReorder}
                size="small">
                <ReorderIcon fill={`${canReorder ? '#00000077' : '#B3BFC5'}`} />
              </IconButton>
            )}
          </div>
        </div>
        {widget.referenceValues.length > 0 ? (
          widget.referenceValues.map((item, index) => (
            <Chip
              key={index}
              classes={{ label: classes.labelItemStyle, root: classes.chip }}
              label={item.label}
              data-testid={`${item.label}`}
              icon={
                <div>
                  <IconOverlay iconURL={item.iconURL} alt={item.label} />
                </div>
              }
              clickable={!readOnly && !widget.readOnly}
              onClick={() => clickHandler(item)}
              onDelete={() => handleDeleteItem(item)}
            />
          ))
        ) : (
          <Typography className={classes.empty}>None</Typography>
        )}
      </div>
      {dialog}
    </>
  );
};

export default ContainmentReferenceSection;

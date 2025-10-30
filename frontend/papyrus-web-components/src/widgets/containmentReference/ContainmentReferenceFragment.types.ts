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

import { GQLMessage } from '@eclipse-sirius/sirius-components-core';
import { GQLWidget } from '@eclipse-sirius/sirius-components-forms';
import { GQLReferenceWidgetStyle } from '@eclipse-sirius/sirius-components-widget-reference';

export type ContainmentReferenceDialogKind = 'REORDER' | 'NEW_INSTANCE';

export interface GQLContainmentReferenceWidget extends GQLWidget {
  label: string;
  ownerId: string;
  descriptionId: string;
  containmentReference: GQLContainmentReference;
  referenceValues: GQLContainmentReferenceItem[] | null;
  style: GQLReferenceWidgetStyle | null;
}

export interface GQLContainmentReference {
  ownerKind: string;
  referenceKind: string;
  isMany: boolean;
  canMove: boolean;
}

export interface GQLContainmentReferenceItem {
  id: string;
  label: string;
  kind: string;
  iconURL: string[];
  hasClickAction: boolean;
}

export interface GQLGetChildCreationDescriptionsQueryVariables {
  editingContextId: string;
  containerId: string;
  referenceKind?: string;
  descriptionId: string;
}

export interface GQLGetChildCreationDescriptionsQueryData {
  viewer: GQLViewer;
}

export interface GQLViewer {
  editingContext: GQLEditingContext;
}

export interface GQLEditingContext {
  referenceWidgetChildCreationDescriptions: GQLChildCreationDescription[];
}

export interface GQLChildCreationDescription {
  id: string;
  label: string;
  iconURL: string;
}

export interface GQLObject {
  id: string;
  label: string;
  kind: string;
}

export interface GQLCreateElementInReferenceMutationVariables {
  input: GQLCreateElementInReferenceInput;
}

export interface GQLCreateElementInReferenceInput {
  id: string;
  editingContextId: string;
  representationId: string;
  referenceWidgetId: string;
  containerId: string;
  domainId: string | null;
  creationDescriptionId: string;
  descriptionId: string;
}

export interface GQLCreateElementInReferenceMutationData {
  createElementInReference: GQLCreateElementInReferencePayload;
}

export interface GQLCreateElementInReferencePayload {
  __typename: string;
}

export interface GQLCreateElementInReferenceSuccessPayload extends GQLCreateElementInReferencePayload {
  id: string;
  object: GQLObject;
  messages: GQLMessage[];
}

export interface GQLErrorPayload
  extends GQLClickContainmentReferenceItemPayload,
    GQLRemoveContainmentReferenceItemPayload {
  messages: GQLMessage[];
}

export interface GQLSuccessPayload
  extends GQLClickContainmentReferenceItemPayload,
    GQLRemoveContainmentReferenceItemPayload {
  messages: GQLMessage[];
}

export interface GQLClickContainmentReferenceItemMutationData {
  clickContainmentReferenceItem: GQLClickContainmentReferenceItemPayload;
}

export interface GQLClickContainmentReferenceItemPayload {
  __typename: string;
}

export interface GQLClickContainmentReferenceItemMutationVariables {
  input: GQLClickContainmentReferenceItemInput;
}

export interface GQLClickContainmentReferenceItemInput {
  id: string;
  editingContextId: string;
  representationId: string;
  referenceWidgetId: string;
  referenceItemId: string;
  clickEventKind: 'SINGLE_CLICK' | 'DOUBLE_CLICK';
}

export interface GQLRemoveContainmentReferenceItemMutationData {
  removeContainmentReferenceItem: GQLRemoveContainmentReferenceItemPayload;
}

export interface GQLRemoveContainmentReferenceItemPayload {
  __typename: string;
}

export interface GQLRemoveContainmentReferenceItemMutationVariables {
  input: GQLRemoveContainmentReferenceItemInput;
}

export interface GQLRemoveContainmentReferenceItemInput {
  id: string;
  editingContextId: string;
  representationId: string;
  referenceWidgetId: string;
  referenceItemId: string;
}

export interface GQLMoveContainmentReferenceItemMutationData {
  moveContainmentReferenceItem: GQLMoveContainmentReferenceItemPayload;
}

export interface GQLMoveContainmentReferenceItemPayload {
  __typename: string;
}

export interface GQLMoveContainmentReferenceItemMutationVariables {
  input: GQLMoveContainmentReferenceItemInput;
}

export interface GQLMoveContainmentReferenceItemInput {
  id: string;
  editingContextId: string;
  representationId: string;
  referenceWidgetId: string;
  referenceItemId: string;
  fromIndex: number;
  toIndex: number;
}

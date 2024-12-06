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
import { GQLMessage } from '@eclipse-sirius/sirius-components-core';
import { GQLWidget } from '@eclipse-sirius/sirius-components-forms';
import { GQLReferenceWidgetStyle } from '@eclipse-sirius/sirius-components-widget-reference';
import { GQLImageMetadata, ProjectImagesSettingsModal } from '@eclipse-sirius/sirius-web-application';

export type CustomImageWidgetModal = 'Select' | ProjectImagesSettingsModal;

export interface CustomImageWidgetState {
  modal: CustomImageWidgetModal | null;
  url: string;
  validImage: boolean;
}

export interface GQLCustomImageWidget extends GQLWidget {
  label: string;
  currentUuid: string;
  style: GQLReferenceWidgetStyle | null;
}

export interface CustomImageWidgetStyleProps {
  color: string | null;
  fontSize: number | null;
  italic: boolean | null;
  bold: boolean | null;
  underline: boolean | null;
  strikeThrough: boolean | null;
}

export interface GQLNewUuidInput {
  id: string;
  editingContextId: string;
  representationId: string;
  customImageId: string;
  newUuid: string;
}

export interface GQLRemoveUuidInput {
  id: string;
  editingContextId: string;
  representationId: string;
  customImageId: string;
  removeUuid: string;
}

export interface GQLNewUuidData {
  newUuid: GQLNewUuidPayload;
}

export interface GQLRemoveUuidData {
  removeUuid: GQLRemoveUuidPayload;
}

export interface GQLNewUuidVariables {
  input: GQLNewUuidInput;
}

export interface GQLRemoveUuidVariables {
  input: GQLRemoveUuidInput;
}

export interface GQLNewUuidPayload {
  __typename: string;
}

export interface GQLRemoveUuidPayload {
  __typename: string;
}

export interface GQLSuccessPayload extends GQLNewUuidPayload, GQLRemoveUuidPayload {
  messages: GQLMessage[];
}

export interface GQLErrorPayload extends GQLNewUuidPayload, GQLRemoveUuidPayload {
  messages: GQLMessage[];
}

export interface SelectImageModalProps {
  currentUuid: string;
  images: GQLImageMetadata[];
  onImageSelected: (newUuid: string) => void;
  onClose: () => void;
}

export type CustomImageParams = 'projectId';

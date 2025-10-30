/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo, Artal Technologies.
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
import { GQLNodeStyle, NodeData } from '@eclipse-sirius/sirius-components-diagrams';

export type HeaderPosition = 'TOP' | 'BOTTOM';

export interface NoteNodeData extends NodeData {}

export interface GQLNoteNodeStyle extends GQLNodeStyle {
  background: string;
  borderColor: string;
  borderStyle: string;
  borderSize: number;
}

export interface Label {
  id: string;
  text: string;
  iconURL: string[];
  style: React.CSSProperties;
  contentStyle: React.CSSProperties;
  displayHeaderSeparator: boolean;
  headerSeparatorStyle: React.CSSProperties;
  headerPosition: HeaderPosition | undefined;
}

export interface NoteLabelProps {
  diagramElementId: string;
  label: Label;
  faded: boolean;
}

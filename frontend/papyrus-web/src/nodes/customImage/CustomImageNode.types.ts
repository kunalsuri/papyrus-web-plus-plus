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
import { GQLNodeStyle, NodeData } from '@eclipse-sirius/sirius-components-diagrams';

export interface CustomImageNodeData extends NodeData {
  shape: string;
}

export interface GQLCustomImageNodeStyle extends GQLNodeStyle {
  shape: string;
  background: string;
  borderColor: string;
  borderStyle: string;
  borderSize: string;
}

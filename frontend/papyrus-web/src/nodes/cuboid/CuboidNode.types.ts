/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo.
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
import { GQLNodeStyle, NodeData } from '@eclipse-sirius/sirius-components-diagrams';

export interface CuboidNodeData extends NodeData {}

export interface CuboidNodeListData extends NodeData {
  areChildNodesDraggable: boolean;
  topGap: number;
  bottomGap: number;
  growableNodeIds: string[];
}

export interface GQLCuboidNodeStyle extends GQLNodeStyle {
  background: string;
  borderColor: string;
  borderStyle: string;
  borderSize: string;
}

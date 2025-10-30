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
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.dto;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramInputReferencePositionProvider;
import org.eclipse.sirius.components.collaborative.diagrams.dto.ReferencePosition;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.diagrams.layoutdata.Position;
import org.springframework.stereotype.Service;

/**
 * Provides diagram input reference position when user creates Metaclass node on a Profile diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public class CreateMetaclassToolReferencePositionProvider implements IDiagramInputReferencePositionProvider {

    @Override
    public boolean canHandle(IInput diagramInput) {
        return diagramInput instanceof CreateMetaclassImportInput;
    }

    @Override
    public ReferencePosition getReferencePosition(IInput diagramInput, DiagramContext diagramContext) {
        if (diagramInput instanceof CreateMetaclassImportInput createMetaclassImportInput) {
            String parentId = null;
            if (!diagramContext.diagram().getId().equals(createMetaclassImportInput.diagramElementId())) {
                // null parentId means that the parent is the diagram
                parentId = createMetaclassImportInput.diagramElementId();
            }
            return new ReferencePosition(parentId, new Position(createMetaclassImportInput.x(), createMetaclassImportInput.y()), diagramInput.getClass().getSimpleName());
        }
        return null;
    }

}

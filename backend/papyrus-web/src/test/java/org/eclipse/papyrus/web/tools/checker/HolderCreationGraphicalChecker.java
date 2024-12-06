/*****************************************************************************
 * Copyright (c) 2025 CEA LIST, Obeo, Artal Technologies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.tools.checker;

import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.uml2.uml.Interface;

/**
 * A specific graphical checker for {@link Interface} in the Component diagram.
 * <p>
 * Interface have 3 compartment, this changes the expected number of created elements in the diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class HolderCreationGraphicalChecker extends NodeCreationGraphicalChecker {

    public HolderCreationGraphicalChecker(Supplier<Diagram> diagramSupplier, Supplier<IDiagramElement> graphicalOwnerSupplier, String mappingType,
            Map<NodeDescription, org.eclipse.sirius.components.diagrams.description.NodeDescription> convertedNodes) {
        super(diagramSupplier, graphicalOwnerSupplier, mappingType, convertedNodes);
    }

    @Override
    protected int getExpectedNumberOfCreatedElements() {
        return 2;
    }

    @Override
    protected int getExpectedNumberOfGraphicalOwnerDirectChildren() {
        return 1;
    }
}

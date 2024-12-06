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

/**
 * Utility class to check that a graphical node has been moved to a new graphical container.
 * <p>
 * This checker validates that the expected graphical node has been moved at the right location in the diagram (in the
 * expected graphical parent). It also validates that the diagram doesn't contain any new graphical element, since
 * moving an element shouldn't change the overall number of displayed elements.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class HolderNodeGraphicalDnDGraphicalChecker extends NodeCreationGraphicalChecker {

    /**
     * Initializes the checker with the provided parameters.
     * <p>
     * The provided {@code graphicalOwnerSupplier} can be set to {@code null} to check that the element has been moved
     * on the diagram.
     * </p>
     *
     * @param diagramSupplier
     *            a supplier to access and reload the diagram
     * @param graphicalOwnerSupplier
     *            a supplier to access and reload the expected graphical owner of the checked node
     * @param mappingType
     *            the expected mapping type of the checked node
     * @param convertedNodes
     *            the diagram-to-description mappings for the nodes of the current diagram
     */
    public HolderNodeGraphicalDnDGraphicalChecker(Supplier<Diagram> diagramSupplier,
            Supplier<IDiagramElement> graphicalOwnerSupplier, String mappingType,
            Map<NodeDescription, org.eclipse.sirius.components.diagrams.description.NodeDescription> convertedNodes) {
        super(diagramSupplier, graphicalOwnerSupplier, mappingType, convertedNodes);
    }

    @Override
    protected int getExpectedNumberOfCreatedElements() {
        // A graphical drag & drop shouldn't create new elements.
        return 0;
    }

    @Override
    protected int getExpectedNumberOfGraphicalOwnerDirectChildren() {
        return 1;
    }

}

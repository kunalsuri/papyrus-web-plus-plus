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

import java.util.function.Supplier;

import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.uml2.uml.Interface;

/**
 * A specific graphical checker for {@link Interface} in the Component diagram.
 * <p>
 * Interfaces have 3 compartment, this changes the expected number of deleted elements in the diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class HolderDeletionGraphicalChecker extends DeletionGraphicalChecker {

    public HolderDeletionGraphicalChecker(Supplier<Diagram> diagramSupplier, Supplier<IDiagramElement> graphicalOwnerSupplier) {
        super(diagramSupplier, graphicalOwnerSupplier);
    }

    @Override
    protected int getExpectedNumerOfDeletedElements() {
        return 2;
    }

    @Override
    protected int getExpectedNumberOfDeletedGraphicalOwnerDirectChildren() {
        // Interface compartments aren't direct children of the classifier's graphical owner
        return 1;
    }

}

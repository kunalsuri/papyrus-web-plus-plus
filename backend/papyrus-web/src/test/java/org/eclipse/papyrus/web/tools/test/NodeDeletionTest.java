/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.tools.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.tools.checker.Checker;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Utility class to help the definition of node deletion tool tests.
 * <p>
 * Concrete node deletion tool tests can extend this class and reuse {@link #deleteGraphicalNode(String, Checker)} and
 * {@link #deleteSemanticNode(String, Checker)} to invoke the graphical or semantic node deletion tool and check the
 * result.
 * </p>
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class NodeDeletionTest extends AbstractPapyrusWebTest {

    /**
     * Initializes the test with the provided {@code representationName} and {@code rootElementEClass}.
     *
     * @param documentName
     *            the name of the document to create
     * @param representationName
     *            the name of the representation to create
     * @param rootElementEClass
     *            the type of the root semantic element to create
     *
     * @see AbstractPapyrusWebTest#AbstractPapyrusWebTest(String, String, EClass)
     */
    public NodeDeletionTest(String documentName, String representationName, EClass rootElementEClass) {
        super(documentName, representationName, rootElementEClass);
    }

    /**
     * Delete a node named {@code elementToDeleteName} from the diagram or a node container.
     *
     * @param elementToDeleteName
     *            name of the element to delete
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void deleteGraphicalNode(String elementToDeleteName, Checker checker) {
        assertThat(checker).as("checker cannot be null").isNotNull();
        IDiagramElement elementToDelete = this.getElementFromItsName(elementToDeleteName);
        this.applyNodeGraphicalDeletionTool(elementToDelete.getId());
        checker.validateRepresentationElement(elementToDelete);
    }

    /**
     * Delete semantically a node named {@code elementToDeleteName} from the diagram or a node container.
     *
     * @param elementToDeleteName
     *            name of the element to delete
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void deleteSemanticNode(String elementToDeleteName, Checker checker) {
        assertThat(checker).as("checker cannot be null").isNotNull();
        IDiagramElement elementToDelete = this.getElementFromItsName(elementToDeleteName);
        this.applyNodeSemanticDeletionTool(elementToDelete.getId());
        checker.validateRepresentationElement(elementToDelete);
    }

    private IDiagramElement getElementFromItsName(String elementToDeleteName) {
        IDiagramElement elementToDelete = this.findGraphicalElementExcludingContentByLabel(elementToDeleteName);
        assertThat(elementToDelete).as("Node to delete should be a Node").isInstanceOf(Node.class);
        assertThat(elementToDelete).as("Cannot find Node with label " + elementToDeleteName).isNotNull();
        return elementToDelete;
    }
}

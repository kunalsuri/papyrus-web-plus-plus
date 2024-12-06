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
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Utility class to help the definition of edge creation tool tests.
 * <p>
 * Concrete edge creation tool tests can extend this class and reuse
 * {@link #createEdge(String, String, String, String, Checker)} to invoke the edge creation tool and check the result.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class EdgeCreationTest extends AbstractPapyrusWebTest {

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
    public EdgeCreationTest(String documentName, String representationName, EClass rootElementEClass) {
        super(documentName, representationName, rootElementEClass);
    }

    /**
     * Creates an edge between {@code sourceElementLabel} and {@code targetElementLabel} with the provided
     * {@code edgeCreationTool}.
     *
     * @param sourceElementLabel
     *            the label of the graphical source of the edge to create
     * @param targetElementLabel
     *            the label of the graphical target of the edge to create
     * @param edgeCreationTool
     *            the {@link CreationTool} specifying the tool section and name in the palette
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void createEdge(String sourceElementLabel, String targetElementLabel, CreationTool edgeCreationTool, Checker checker) {
        assertThat(checker).as("checker cannot be null").isNotNull();
        IDiagramElement sourceElement = this.findGraphicalElementExcludingContentByLabel(sourceElementLabel);
        assertThat(sourceElement).isNotNull();
        assertThat(sourceElement).isInstanceOf(Node.class);
        IDiagramElement targetElement = this.findGraphicalElementExcludingContentByLabel(targetElementLabel);
        assertThat(targetElement).isNotNull();
        assertThat(targetElement).isInstanceOf(Node.class);
        int initialNumberOfEdges = this.getDiagram().getEdges().size();
        this.applyEdgeCreationTool(sourceElement.getId(), targetElement.getId(), edgeCreationTool);
        assertThat(this.getDiagram().getEdges()).hasSize(1);
        // We assume the created element is always added at the end of the getChildNodes list
        Edge createdElement = this.getDiagram().getEdges().get(initialNumberOfEdges);
        checker.validateRepresentationElement(createdElement);
    }
}

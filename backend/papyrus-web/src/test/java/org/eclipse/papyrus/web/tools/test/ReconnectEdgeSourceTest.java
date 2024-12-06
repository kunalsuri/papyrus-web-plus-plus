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

/**
 * Utility class to help the definition of reconnect edge source tool tests.
 * <p>
 * Concrete reconnect edge source tool tests can extend this class and reuse
 * {@link #reconnectEdgeSource(String, String, Checker)} to invoke the reconnect edge source tool and check the result.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ReconnectEdgeSourceTest extends AbstractPapyrusWebTest {

    /**
     * The suffix for the elements that are old sources of the tested reconnection.
     */
    protected static final String OLD_SOURCE = "OldSource";

    /**
     * The suffix for the elements that are new sources of the tested reconnection.
     */
    protected static final String NEW_SOURCE = "NewSource";

    /**
     * The suffix for the elements that are targets of the tested reconnection.
     */
    protected static final String TARGET = "Target";

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
    public ReconnectEdgeSourceTest(String documentName, String representationName, EClass rootElementEClass) {
        super(documentName, representationName, rootElementEClass);
    }

    /**
     * Reconnects the source of the provided {@code edgeId} to {@code newSourceLabel}.
     *
     * @param edgeId
     *            the graphical identifier of the edge to reconnect the source of
     * @param newSourceLabel
     *            the label of the graphical element to reconnect the source to
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void reconnectEdgeSource(String edgeId, String newSourceLabel, Checker checker) {
        assertThat(checker).as("checker cannot be null").isNotNull();
        IDiagramElement newSourceElement = this.findGraphicalElementExcludingContentByLabel(newSourceLabel);
        this.applyReconnectEdgeSourceTool(edgeId, newSourceElement.getId());
        Edge edge = this.getDiagram().getEdges().get(0);
        checker.validateRepresentationElement(edge);
    }

    /**
     * Creates two source and one target nodes with the provided {@code creationTool} in the provided
     * {@code parentElementId}.
     * <p>
     * This method create three nodes and sets their label with
     * {@code elementType + (OLD_SOURCE | NEW_SOURCE | TARGET)}, where {@code elementType} is the type of the created
     * element.
     * </p>
     * <p>
     * This method is typically used to initialize a source reconnection test, where two sources are required to
     * actually perform the reconnection.
     * </p>
     *
     * @param parentElementId
     *            the identifier of the graphical parent to create the nodes into
     * @param creationTool
     *            the tool to use to create the source and target nodes
     *
     * @see #createSourceAndTargetTopNodes(CreationTool) to create two source and one target elements at the diagram
     *      level
     */
    @Override
    protected void createSourceAndTargetNodes(String parentElementId, CreationTool creationTool) {
        EClass toolEClass = creationTool.getToolEClass();
        this.createNodeWithLabel(parentElementId, creationTool, toolEClass.getName() + OLD_SOURCE);
        this.createNodeWithLabel(parentElementId, creationTool, toolEClass.getName() + NEW_SOURCE);
        this.createNodeWithLabel(parentElementId, creationTool, toolEClass.getName() + TARGET);
    }
}

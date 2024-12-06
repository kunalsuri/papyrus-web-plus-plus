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
 * Utility class to help the definition of reconnect edge target tool tests.
 * <p>
 * Concrete reconnect edge target tool tests can extend this class and reuse
 * {@link #reconnectEdgeTarget(String, String, Checker)} to invoke the reconnect edge target tool and check the result.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ReconnectEdgeTargetTest extends AbstractPapyrusWebTest {

    /**
     * The suffix for the elements that are sources of the tested reconnection.
     */
    protected static final String SOURCE = "Source";

    /**
     * The suffix for the elements that are old targets of the tested reconnection.
     */
    protected static final String OLD_TARGET = "OldTarget";

    /**
     * The suffix for the elements that are new targets of the tested reconnection.
     */
    protected static final String NEW_TARGET = "NewTarget";

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
    public ReconnectEdgeTargetTest(String documentName, String representationName, EClass rootElementEClass) {
        super(documentName, representationName, rootElementEClass);
    }

    /**
     * Reconnects the target of the provided {@code edgeId} to {@code newTargetLabel}.
     *
     * @param edgeId
     *            the graphical identifier of the edge to reconnect the target of
     * @param newTargetLabel
     *            the label of the graphical element to reconnect the target to
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void reconnectEdgeTarget(String edgeId, String newTargetLabel, Checker checker) {
        assertThat(checker).as("checker cannot be null").isNotNull();
        IDiagramElement newTargetElement = this.findGraphicalElementExcludingContentByLabel(newTargetLabel);
        this.applyReconnectEdgeTargetTool(edgeId, newTargetElement.getId());
        Edge edge = this.getDiagram().getEdges().get(0);
        checker.validateRepresentationElement(edge);
    }

    /**
     * Creates one source and two target nodes with the provided {@code creationTool} in the provided
     * {@code parentElementId}.
     * <p>
     * This method create three nodes and sets their label with
     * {@code elementType + (OLD_TARGET | NEW_TARGET | SOURCE)}, where {@code elementType} is the type of the created
     * element.
     * </p>
     * <p>
     * This method is typically used to initialize a target reconnection test, where two targets are required to
     * actually perform the reconnection.
     * </p>
     *
     * @param parentElementId
     *            the identifier of the graphical parent to create the nodes into
     * @param creationTool
     *            the tool to use to create the source and target nodes
     *
     * @see #createSourceAndTargetTopNodes(CreationTool) to create one source and two target elements at the diagram
     *      level
     */
    @Override
    protected void createSourceAndTargetNodes(String parentElementId, CreationTool creationTool) {
        EClass toolEClass = creationTool.getToolEClass();
        this.createNodeWithLabel(parentElementId, creationTool, toolEClass.getName() + OLD_TARGET);
        this.createNodeWithLabel(parentElementId, creationTool, toolEClass.getName() + NEW_TARGET);
        this.createNodeWithLabel(parentElementId, creationTool, toolEClass.getName() + SOURCE);
    }
}

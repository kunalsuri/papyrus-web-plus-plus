/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.tools.communication;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.application.representations.uml.CODDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.Checker;
import org.eclipse.papyrus.web.tools.checker.EdgeTargetGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.ReconnectEdgeTargetTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests reconnect edge target tools in the Communication Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CODReconnectEdgeTargetTest extends ReconnectEdgeTargetTest {

    private static final String ROOT_INTERACTION = "rootInteraction";

    private static final String LIFELINE_EDGE_SOURCE = "Lifeline" + SOURCE;

    private static final String LIFELINE_EDGE_TARGET = "Lifeline" + OLD_TARGET;

    public CODReconnectEdgeTargetTest() {
        super(DEFAULT_DOCUMENT, CODDiagramDescriptionBuilder.COD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> messageParameters() {
        return Stream.of(//
                Arguments.of(UML.getLifeline()));
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_INTERACTION, UML.getInteraction());
        Node rootContent = (Node) this.findGraphicalElementContentByLabel(ROOT_INTERACTION);
        this.createSourceAndTargetNodes(rootContent.getId(), new CreationTool(ToolSections.NODES, UML.getLifeline()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("messageParameters")
    public void testReconnectMessageTarget(EClass newTargetType) {
        String associationId = this.createEdge(LIFELINE_EDGE_SOURCE, LIFELINE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getMessage()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(associationId, newTargetLabel, graphicalChecker);
    }
}

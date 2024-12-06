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
package org.eclipse.papyrus.web.tools.profile;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.Checker;
import org.eclipse.papyrus.web.tools.checker.EdgeTargetGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.ReconnectEdgeTargetTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests reconnect edge target tools in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDReconnectEdgeTargetTest extends ReconnectEdgeTargetTest {

    private static final String CLASS_EDGE_SOURCE = "Class" + SOURCE;

    private static final String CLASS_EDGE_TARGET = "Class" + OLD_TARGET;

    private static final String STEREOTYPE_EDGE_SOURCE = "Stereotype" + SOURCE;

    private static final String STEREOTYPE_EDGE_TARGET = "Stereotype" + OLD_TARGET;

    public PRDReconnectEdgeTargetTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> associationAndGeneralizationParameters() {
        return Stream.of(//
                Arguments.of(UML.getClass_()), //
                Arguments.of(UML.getDataType()), //
                Arguments.of(UML.getEnumeration()), //
                Arguments.of(UML.getPrimitiveType()), //
                Arguments.of(UML.getStereotype()) //
        );
    }

    private static Stream<Arguments> extensionParameters() {
        return Stream.of(//
                Arguments.of(UML.getClass_()), //
                Arguments.of(UML.getStereotype())//
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getClass_()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getDataType()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getEnumeration()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getPrimitiveType()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getStereotype()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testReconnectAssociationTarget(EClass newTargetType) {
        String associationId = this.createEdge(CLASS_EDGE_SOURCE, CLASS_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getAssociation()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(associationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testReconnectGeneralizationSource(EClass newTargetType) {
        String generalizationId = this.createEdge(CLASS_EDGE_SOURCE, CLASS_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getGeneralization()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(generalizationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("extensionParameters")
    public void testReconnectExtensionSource(EClass newTargetType) {
        String extensionId = this.createEdge(STEREOTYPE_EDGE_SOURCE, STEREOTYPE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getExtension()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(extensionId, newTargetLabel, graphicalChecker);
    }
}

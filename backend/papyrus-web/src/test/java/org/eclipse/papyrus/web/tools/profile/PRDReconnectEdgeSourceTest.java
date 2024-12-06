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
import org.eclipse.papyrus.web.tools.checker.EdgeSourceGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.ReconnectEdgeSourceTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests reconnect edge source tools in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDReconnectEdgeSourceTest extends ReconnectEdgeSourceTest {

    private static final String CLASS_EDGE_SOURCE = "Class" + OLD_SOURCE;

    private static final String CLASS_EDGE_TARGET = "Class" + TARGET;

    private static final String STEREOTYPE_EDGE_SOURCE = "Stereotype" + OLD_SOURCE;

    private static final String STEREOTYPE_EDGE_TARGET = "Stereotype" + TARGET;

    public PRDReconnectEdgeSourceTest() {
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
    public void testReconnectAssociationSource(EClass newSourceType) {
        String associationId = this.createEdge(CLASS_EDGE_SOURCE, CLASS_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getAssociation()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(associationId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testReconnectGeneralizationSource(EClass newSourceType) {
        String generalizationId = this.createEdge(CLASS_EDGE_SOURCE, CLASS_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getGeneralization()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(generalizationId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("extensionParameters")
    public void testReconnectExtensionSource(EClass newSourceType) {
        String extensionId = this.createEdge(STEREOTYPE_EDGE_SOURCE, STEREOTYPE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getExtension()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(extensionId, newSourceLabel, graphicalChecker);
    }
}

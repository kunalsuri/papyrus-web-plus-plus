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
package org.eclipse.papyrus.web.tools.component;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
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
 * Tests reconnect edge source tools in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDReconnectEdgeSourceTest extends ReconnectEdgeSourceTest {

    private static final String COMPONENT_CONTAINER = "ComponentContainer";

    private static final String COMPONENT_EDGE_SOURCE = "Component" + OLD_SOURCE;

    private static final String COMPONENT_EDGE_TARGET = "Component" + TARGET;

    private static final String INTERFACE_EDGE_TARGET = "Interface" + TARGET;

    public CPDReconnectEdgeSourceTest() {
        super(DEFAULT_DOCUMENT, CPDDiagramDescriptionBuilder.CPD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> abstractionAndDependencyAndUsageParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent()), //
                Arguments.of(UML.getConstraint()), //
                Arguments.of(UML.getInterface()), //
                Arguments.of(UML.getModel()), //
                Arguments.of(UML.getPackage()), //
                Arguments.of(UML.getPort()), //
                Arguments.of(UML.getProperty()) //
        );
    }

    private static Stream<Arguments> componentRealizationAndGeneralizationAndSubstitutionParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent()), //
                Arguments.of(UML.getInterface())//
        );
    }

    private static Stream<Arguments> interfaceRealizationParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent()) //
        );
    }

    private static Stream<Arguments> manifestationParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent()), //
                Arguments.of(UML.getConstraint()), //
                Arguments.of(UML.getInterface()), //
                Arguments.of(UML.getModel()), //
                Arguments.of(UML.getPackage()), //
                Arguments.of(UML.getProperty()) //
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getComponent()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getConstraint()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getInterface()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getModel()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getPackage()));
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_CONTAINER);
        String componentContainerId = this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER).getId();
        // String componentHolderId = this.findGraphicalElementExcludingContentByLabel(COMPONENT_CONTAINER).getId();
        this.createSourceAndTargetNodes(componentContainerId, new CreationTool(ToolSections.NODES, UML.getPort()));
        this.createSourceAndTargetNodes(componentContainerId, new CreationTool(ToolSections.NODES, UML.getProperty()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testReconnectAbstractionSource(EClass newSourceType) {
        String abstractionId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getAbstraction()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(abstractionId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("componentRealizationAndGeneralizationAndSubstitutionParameters")
    public void testReconnectComponentRealizationSource(EClass newSourceType) {
        String componentRealizationId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getComponentRealization()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(componentRealizationId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testReconnectDependencySource(EClass newSourceType) {
        String dependencyId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getDependency()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(dependencyId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("componentRealizationAndGeneralizationAndSubstitutionParameters")
    public void testReconnectGeneralizationSource(EClass newSourceType) {
        String generalizationId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getGeneralization()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(generalizationId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("interfaceRealizationParameters")
    public void testReconnectInterfaceRealizationSource(EClass newSourceType) {
        String interfaceRealizationId = this.createEdge(COMPONENT_EDGE_SOURCE, INTERFACE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getInterfaceRealization()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(interfaceRealizationId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("manifestationParameters")
    public void testReconnectManifestationSource(EClass newSourceType) {
        String manifestationId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getManifestation()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(manifestationId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("componentRealizationAndGeneralizationAndSubstitutionParameters")
    public void testReconnectSubstitutionSource(EClass newSourceType) {
        String substitutionId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getSubstitution()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(substitutionId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testReconnectUsageSource(EClass newSourceType) {
        String usageId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getUsage()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(usageId, newSourceLabel, graphicalChecker);
    }
}

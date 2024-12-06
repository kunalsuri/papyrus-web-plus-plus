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
 * Tests reconnect edge target tools in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDReconnectEdgeTargetTest extends ReconnectEdgeTargetTest {

    private static final String COMPONENT_CONTAINER = "ComponentContainer";

    private static final String COMPONENT_EDGE_SOURCE = "Component" + SOURCE;

    private static final String COMPONENT_EDGE_TARGET = "Component" + OLD_TARGET;

    private static final String INTERFACE_EDGE_TARGET = "Interface" + OLD_TARGET;

    public CPDReconnectEdgeTargetTest() {
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

    private static Stream<Arguments> componentRealizationParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent())//
        );
    }

    private static Stream<Arguments> generalizationAndSubstitutionParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent()), //
                Arguments.of(UML.getInterface())//
        );
    }

    private static Stream<Arguments> interfaceRealizationParameters() {
        return Stream.of(//
                Arguments.of(UML.getInterface())//
        );
    }

    private static Stream<Arguments> manifestationParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent()), //
                Arguments.of(UML.getConstraint()), //
                Arguments.of(UML.getInterface()), //
                Arguments.of(UML.getModel()), //
                Arguments.of(UML.getPackage())//
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
    public void testReconnectAbstractionTarget(EClass newTargetType) {
        String abstractionId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getAbstraction()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(abstractionId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("componentRealizationParameters")
    public void testReconnectComponentRealizationTarget(EClass newTargetType) {
        String componentRealizationId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getComponentRealization()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(componentRealizationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testReconnectDependencyTarget(EClass newTargetType) {
        String dependencyId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getDependency()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(dependencyId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("generalizationAndSubstitutionParameters")
    public void testReconnectGeneralizationTarget(EClass newTargetType) {
        String generalizationId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getGeneralization()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(generalizationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("interfaceRealizationParameters")
    public void testReconnectInterfaceRealizationTarget(EClass newTargetType) {
        String interfaceRealizationId = this.createEdge(COMPONENT_EDGE_SOURCE, INTERFACE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getInterfaceRealization()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(interfaceRealizationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("manifestationParameters")
    public void testReconnectManifestationTarget(EClass newTargetType) {
        String manifestationId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getManifestation()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(manifestationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("generalizationAndSubstitutionParameters")
    public void testReconnectSubstitutionTarget(EClass newTargetType) {
        String substitutionId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getSubstitution()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(substitutionId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testReconnectUsageTarget(EClass newTargetType) {
        String usageId = this.createEdge(COMPONENT_EDGE_SOURCE, COMPONENT_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getUsage()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(usageId, newTargetLabel, graphicalChecker);
    }
}

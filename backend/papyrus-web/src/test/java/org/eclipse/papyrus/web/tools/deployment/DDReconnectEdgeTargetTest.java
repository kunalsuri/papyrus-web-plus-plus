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
package org.eclipse.papyrus.web.tools.deployment;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.application.representations.uml.DDDiagramDescriptionBuilder;
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
 * Tests reconnect edge target tools in the Deployment Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class DDReconnectEdgeTargetTest extends ReconnectEdgeTargetTest {

    private static final String ARTIFACT_EDGE_SOURCE = "Artifact" + SOURCE;

    private static final String DEVICE_EDGE_SOURCE = "Device" + SOURCE;

    private static final String DEVICE_EDGE_TARGET = "Device" + OLD_TARGET;

    public DDReconnectEdgeTargetTest() {
        super(DEFAULT_DOCUMENT, DDDiagramDescriptionBuilder.DD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> communicationPathParameters() {
        return Stream.of(
                Arguments.of(UML.getDevice()),
                Arguments.of(UML.getExecutionEnvironment()),
                Arguments.of(UML.getNode()));
    }

    private static Stream<Arguments> dependencyAndManifestationParameters() {
        return Stream.of(
                Arguments.of(UML.getArtifact()),
                Arguments.of(UML.getConstraint()),
                Arguments.of(UML.getDeploymentSpecification()),
                Arguments.of(UML.getDevice()),
                Arguments.of(UML.getExecutionEnvironment()),
                Arguments.of(UML.getModel()),
                Arguments.of(UML.getNode()),
                Arguments.of(UML.getPackage()));
    }

    private static Stream<Arguments> deploymentParameters() {
        return Stream.of(
                Arguments.of(UML.getDevice()),
                Arguments.of(UML.getExecutionEnvironment()),
                Arguments.of(UML.getNode()));
    }

    private static Stream<Arguments> generalizationParameters() {
        return Stream.of(
                Arguments.of(UML.getArtifact()),
                Arguments.of(UML.getDeploymentSpecification()),
                Arguments.of(UML.getDevice()),
                Arguments.of(UML.getExecutionEnvironment()),
                Arguments.of(UML.getNode()));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getArtifact()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getConstraint()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getDevice()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getModel()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getNode()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getPackage()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("communicationPathParameters")
    public void testReconnectCommunicationPathTarget(EClass newTargetType) {
        String communicationPathId = this.createEdge(DEVICE_EDGE_SOURCE, DEVICE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getCommunicationPath()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(communicationPathId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dependencyAndManifestationParameters")
    public void testReconnectDependencyTarget(EClass newTargetType) {
        String dependencyId = this.createEdge(DEVICE_EDGE_SOURCE, DEVICE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getDependency()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(dependencyId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("deploymentParameters")
    public void testReconnectDeploymentTarget(EClass newTargetType) {
        String deploymentPathId = this.createEdge(ARTIFACT_EDGE_SOURCE, DEVICE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getDeployment()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(deploymentPathId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("generalizationParameters")
    public void testReconnectGeneralizationTarget(EClass newTargetType) {
        String generalizationId = this.createEdge(DEVICE_EDGE_SOURCE, DEVICE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getGeneralization()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(generalizationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dependencyAndManifestationParameters")
    public void testReconnectManifestationTarget(EClass newTargetType) {
        String manifestationId = this.createEdge(DEVICE_EDGE_SOURCE, DEVICE_EDGE_TARGET, new CreationTool(ToolSections.EDGES, UML.getDependency()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(manifestationId, newTargetLabel, graphicalChecker);
    }
}

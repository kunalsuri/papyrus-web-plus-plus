/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.web.tools.deployment;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.DDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EdgeDeletionTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Edge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edge creation tools in the Deployment Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class DDEdgeSemanticDeletionTest extends EdgeDeletionTest {

    private static final String ARTIFACT_SOURCE = "ArtifactSource";

    private static final String ARTIFACT_TARGET = "ArtifactTarget";

    private static final String CONSTRAINT_SOURCE = "ConstraintSource";

    private static final String CONSTRAINT_TARGET = "ConstraintTarget";

    private static final String DEPLOYMENT_SPECIFICATION_SOURCE = "DeploymentSpecificationSource";

    private static final String DEPLOYMENT_SPECIFICATION_TARGET = "DeploymentSpecificationTarget";

    private static final String DEVICE_SOURCE = "DeviceSource";

    private static final String DEVICE_TARGET = "DeviceTarget";

    private static final String EXECUTION_ENVIRONMENT_SOURCE = "ExecutionEnvironmentSource";

    private static final String EXECUTION_ENVIRONMENT_TARGET = "ExecutionEnvironmentTarget";

    private static final String MODEL_SOURCE = "ModelSource";

    private static final String MODEL_TARGET = "ModelTarget";

    private static final String NODE_SOURCE = "NodeSource";

    private static final String NODE_TARGET = "NodeTarget";

    private static final String PACKAGE_SOURCE = "PackageSource";

    private static final String PACKAGE_TARGET = "PackageTarget";

    public DDEdgeSemanticDeletionTest() {
        super(DEFAULT_DOCUMENT, DDDiagramDescriptionBuilder.DD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> communicationPathParameters() {
        List<String> sources = List.of(DEVICE_SOURCE, EXECUTION_ENVIRONMENT_SOURCE, NODE_SOURCE);
        List<String> targets = List.of(DEVICE_TARGET, EXECUTION_ENVIRONMENT_TARGET, NODE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> dependencyAndManifestationParameters() {
        List<String> sources = List.of(ARTIFACT_SOURCE, CONSTRAINT_SOURCE, DEPLOYMENT_SPECIFICATION_SOURCE, DEVICE_SOURCE, EXECUTION_ENVIRONMENT_SOURCE, MODEL_SOURCE, NODE_SOURCE, PACKAGE_SOURCE);
        List<String> targets = List.of(ARTIFACT_TARGET, CONSTRAINT_TARGET, DEPLOYMENT_SPECIFICATION_TARGET, DEVICE_TARGET, EXECUTION_ENVIRONMENT_TARGET, MODEL_TARGET, NODE_TARGET, PACKAGE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> deploymentParameters() {
        List<String> sources = List.of(ARTIFACT_SOURCE, DEPLOYMENT_SPECIFICATION_SOURCE);
        List<String> targets = List.of(DEVICE_TARGET, EXECUTION_ENVIRONMENT_TARGET, NODE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> generalizationParameters() {
        List<String> sources = List.of(ARTIFACT_SOURCE, DEPLOYMENT_SPECIFICATION_SOURCE, DEVICE_SOURCE, EXECUTION_ENVIRONMENT_SOURCE, NODE_SOURCE);
        List<String> targets = List.of(ARTIFACT_TARGET, DEPLOYMENT_SPECIFICATION_TARGET, DEVICE_TARGET, EXECUTION_ENVIRONMENT_TARGET, NODE_TARGET);
        return cartesianProduct(sources, targets);
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
    public void testDeleteCommunicationPath(String sourceElementLabel, String targetElementLabel) {
        this.testDeleteEdge(sourceElementLabel, targetElementLabel, UML.getCommunicationPath());
    }

    @ParameterizedTest
    @MethodSource("dependencyAndManifestationParameters")
    public void testDeleteDependency(String sourceElementLabel, String targetElementLabel) {
        this.testDeleteEdge(sourceElementLabel, targetElementLabel, UML.getDependency());
    }

    @ParameterizedTest
    @MethodSource("deploymentParameters")
    public void testDeleteDeployment(String sourceElementLabel, String targetElementLabel) {
        this.testDeleteEdge(sourceElementLabel, targetElementLabel, UML.getDeployment());
    }

    @ParameterizedTest
    @MethodSource("generalizationParameters")
    public void testDeleteGeneralization(String sourceElementLabel, String targetElementLabel) {
        this.testDeleteEdge(sourceElementLabel, targetElementLabel, UML.getGeneralization());
    }

    @ParameterizedTest
    @MethodSource("dependencyAndManifestationParameters")
    public void testDeleteManifestation(String sourceElementLabel, String targetElementLabel) {
        this.testDeleteEdge(sourceElementLabel, targetElementLabel, UML.getManifestation());
    }

    private void testDeleteEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, edgeType));
        Edge edge = this.getDiagram().getEdges().get(0);
        this.testDeleteEdge(edge, null, UML.getPackage_PackagedElement());
    }

    private void testDeleteEdge(Edge edge, String oldOwnerLabel, EReference oldContainmentReference) {
        final Supplier<EObject> oldOwnerSupplier;
        if (oldOwnerLabel == null) {
            oldOwnerSupplier = this::getRootSemanticElement;
        } else {
            oldOwnerSupplier = () -> this.findSemanticElementByName(oldOwnerLabel);
        }
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, null);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext, oldOwnerSupplier, oldContainmentReference);
        this.deleteSemanticEdge(edge, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

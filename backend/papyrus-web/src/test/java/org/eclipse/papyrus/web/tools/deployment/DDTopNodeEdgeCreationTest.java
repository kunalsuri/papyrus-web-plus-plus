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
import org.eclipse.papyrus.web.tools.checker.EdgeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.deployment.utils.DDMappingTypes;
import org.eclipse.papyrus.web.tools.test.EdgeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
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
public class DDTopNodeEdgeCreationTest extends EdgeCreationTest {

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

    public DDTopNodeEdgeCreationTest() {
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
    public void testCreateCommunicationPath(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getCommunicationPath());
    }

    @ParameterizedTest
    @MethodSource("dependencyAndManifestationParameters")
    public void testCreateDependency(String sourceElementLabel, String targetElementLabel) {
        if (List.of(MODEL_SOURCE, PACKAGE_SOURCE).contains(sourceElementLabel)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDependency(), sourceElementLabel, UML.getPackage_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDependency());
        }
    }

    @ParameterizedTest
    @MethodSource("deploymentParameters")
    public void testCreateDeployment(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDeployment(), targetElementLabel, UML.getDeploymentTarget_Deployment());
    }

    @ParameterizedTest
    @MethodSource("generalizationParameters")
    public void testCreateGeneralization(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getGeneralization(), sourceElementLabel, UML.getClassifier_Generalization());
    }

    @ParameterizedTest
    @MethodSource("dependencyAndManifestationParameters")
    public void testCreateManifestation(String sourceElementLabel, String targetElementLabel) {
        if (List.of(ARTIFACT_SOURCE, DEPLOYMENT_SPECIFICATION_SOURCE).contains(sourceElementLabel)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getManifestation(), sourceElementLabel, UML.getArtifact_Manifestation());
        } else if (List.of(MODEL_SOURCE, PACKAGE_SOURCE).contains(sourceElementLabel)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getManifestation(), sourceElementLabel, UML.getPackage_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getManifestation());
        }
    }

    private void testCreateEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, edgeType, null, UML.getPackage_PackagedElement());
    }

    private void testCreateEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType, String expectedSemanticOwnerName, EReference expectedContainmentReference) {
        Supplier<EObject> expectedSemanticOwnerSupplier;
        if (expectedSemanticOwnerName == null) {
            expectedSemanticOwnerSupplier = () -> this.getRootSemanticElement();
        } else {
            expectedSemanticOwnerSupplier = () -> this.findSemanticElementByName(expectedSemanticOwnerName);
        }
        EdgeCreationGraphicalChecker graphicalChecker = new EdgeCreationGraphicalChecker(this::getDiagram, null, DDMappingTypes.getMappingType(edgeType), this.getCapturedEdges());
        EdgeCreationSemanticChecker semanticChecker = new EdgeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, edgeType, expectedSemanticOwnerSupplier,
                expectedContainmentReference);
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, edgeType), new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

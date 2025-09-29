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
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.DDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.HolderDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.test.NodeDeletionTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node semantic deletion tools at the root of the diagram in the Deployment Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class DDSubNodeSemanticDeletionTest extends NodeDeletionTest {

    private static final String DEPLOYMENT_SPECIFICATION = "DeploymentSpecification";

    private static final String ARTIFACT_CONTAINER = "ArtifactContainer";

    private static final String DEVICE_CONTAINER = "DeviceContainer";

    private static final String EXECUTION_ENVIRONMENT_CONTAINER = "ExecutionEnvironmentContainer";

    private static final String MODEL_CONTAINER = "ModelContainer";

    private static final String NODE_CONTAINER = "NodeContainer";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final String ARTIFACT_SUB_NODE_SUFFIX = "In_Artifact";

    private static final String DEVICE_SUB_NODE_SUFFIX = "In_Device";

    private static final String EXECUTION_ENVIRONMENT_SUB_NODE_SUFFIX = "In_ExecutionEnvironment";

    private static final String MODEL_SUB_NODE_SUFFIX = "In_Model";

    private static final String NODE_SUB_NODE_SUFFIX = "In_Node";

    private static final String PACKAGE_SUB_NODE_SUFFIX = "In_Package";

    public DDSubNodeSemanticDeletionTest() {
        super(DEFAULT_DOCUMENT, DDDiagramDescriptionBuilder.DD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> artifactChildrenParameters() {
        return Stream.of(//
                Arguments.of(UML.getArtifact(), UML.getArtifact_NestedArtifact()),
                Arguments.of(UML.getDeploymentSpecification(), UML.getArtifact_NestedArtifact()));
    }

    private static Stream<Arguments> deviceChildrenParameters() {
        return Stream.of(//
                Arguments.of(UML.getDeploymentSpecification(), UML.getClass_NestedClassifier()),
                Arguments.of(UML.getDevice(), UML.getNode_NestedNode()),
                Arguments.of(UML.getExecutionEnvironment(), UML.getNode_NestedNode()),
                Arguments.of(UML.getNode(), UML.getNode_NestedNode()));
    }

    private static Stream<Arguments> executionEnvironmentChildrenParameters() {
        return Stream.of(//
                Arguments.of(UML.getArtifact(), UML.getClass_NestedClassifier()),
                Arguments.of(UML.getDeploymentSpecification(), UML.getClass_NestedClassifier()),
                Arguments.of(UML.getExecutionEnvironment(), UML.getNode_NestedNode()));
    }

    private static Stream<Arguments> modelAndPackageChildrenParameters() {
        return Stream.of(//
                Arguments.of(UML.getArtifact(), UML.getPackage_PackagedElement()), //
                Arguments.of(UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(UML.getDeploymentSpecification(), UML.getPackage_PackagedElement()), //
                Arguments.of(UML.getDevice(), UML.getPackage_PackagedElement()), //
                Arguments.of(UML.getExecutionEnvironment(), UML.getPackage_PackagedElement()), //
                Arguments.of(UML.getModel(), UML.getPackage_PackagedElement()), //
                Arguments.of(UML.getNode(), UML.getPackage_PackagedElement()), //
                Arguments.of(UML.getPackage(), UML.getPackage_PackagedElement()) //
        );
    }

    private static Stream<Arguments> nodeChildrenParameters() {
        return Stream.of(//
                Arguments.of(UML.getArtifact(), UML.getClass_NestedClassifier()),
                Arguments.of(UML.getDeploymentSpecification(), UML.getClass_NestedClassifier()),
                Arguments.of(UML.getDevice(), UML.getNode_NestedNode()),
                Arguments.of(UML.getExecutionEnvironment(), UML.getNode_NestedNode()),
                Arguments.of(UML.getNode(), UML.getNode_NestedNode()));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    private void createArtifactSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification().getName() + suffix);
    }

    private void createDeviceSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getDevice()), UML.getDevice().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getNode()), UML.getNode().getName() + suffix);
    }

    private void createExecutionEnvironmentSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment().getName() + suffix);
    }

    private void createModelAndPackageSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getDevice()), UML.getDevice().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getModel()), UML.getModel().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getNode()), UML.getNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage().getName() + suffix);
    }

    private void createNodeSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getDevice()), UML.getDevice().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getNode()), UML.getNode().getName() + suffix);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("artifactChildrenParameters")
    public void testDeleteSemanticNodeInArtifact(EClass elementType, EReference containmentReference) {
        // Create all the Artifact sub-nodes to delete
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getArtifact()), ARTIFACT_CONTAINER);
        Node artifactContainer = (Node) this.findGraphicalElementContentByLabel(ARTIFACT_CONTAINER);

        this.createArtifactSubNodes(artifactContainer, ARTIFACT_SUB_NODE_SUFFIX);

        DeletionGraphicalChecker graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(ARTIFACT_CONTAINER));

        if (elementType.getName().equals(DEPLOYMENT_SPECIFICATION)) {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(ARTIFACT_CONTAINER));
        }

        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(ARTIFACT_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + ARTIFACT_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("deviceChildrenParameters")
    public void testDeleteSemanticNodeInDevice(EClass elementType, EReference containmentReference) {
        // Create all the Device sub-nodes to delete
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getDevice()), DEVICE_CONTAINER);
        Node deviceContainer = (Node) this.findGraphicalElementContentByLabel(DEVICE_CONTAINER);
        this.createDeviceSubNodes(deviceContainer, DEVICE_SUB_NODE_SUFFIX);

        DeletionGraphicalChecker graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(DEVICE_CONTAINER));
        if (elementType.getName().equals(DEPLOYMENT_SPECIFICATION)) {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(DEVICE_CONTAINER));
        }

        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(DEVICE_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + DEVICE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("executionEnvironmentChildrenParameters")
    public void testDeleteSemanticNodeInExecutionEnvironment(EClass elementType, EReference containmentReference) {
        // Create all the ExecutionEnvironment sub-nodes to delete
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), EXECUTION_ENVIRONMENT_CONTAINER);
        Node executionEnvironmentContainer = (Node) this.findGraphicalElementContentByLabel(EXECUTION_ENVIRONMENT_CONTAINER);

        this.createExecutionEnvironmentSubNodes(executionEnvironmentContainer, EXECUTION_ENVIRONMENT_SUB_NODE_SUFFIX);

        DeletionGraphicalChecker graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(EXECUTION_ENVIRONMENT_CONTAINER));
        if (elementType.getName().equals(DEPLOYMENT_SPECIFICATION)) {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(EXECUTION_ENVIRONMENT_CONTAINER));
        }

        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(EXECUTION_ENVIRONMENT_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + EXECUTION_ENVIRONMENT_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("modelAndPackageChildrenParameters")
    public void testDeleteSemanticNodeInModel(EClass elementType, EReference containmentReference) {
        // Create all the Model sub-nodes to delete
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getModel()), MODEL_CONTAINER);
        Node modelContainer = (Node) this.findGraphicalElementContentByLabel(MODEL_CONTAINER);

        this.createModelAndPackageSubNodes(modelContainer, MODEL_SUB_NODE_SUFFIX);

        DeletionGraphicalChecker graphicalChecker;
        if (elementType.getName().equals(DEPLOYMENT_SPECIFICATION) || elementType.getName().equals("Constraint")) {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER));
        } else {
            graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER));
        }

        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(MODEL_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + MODEL_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("nodeChildrenParameters")
    public void testDeleteSemanticNodeInNode(EClass elementType, EReference containmentReference) {
        // Create all the Node sub-nodes to delete
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getNode()), NODE_CONTAINER);
        Node nodeContainer = (Node) this.findGraphicalElementContentByLabel(NODE_CONTAINER);
        this.createNodeSubNodes(nodeContainer, NODE_SUB_NODE_SUFFIX);

        DeletionGraphicalChecker graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(NODE_CONTAINER));
        if (elementType.getName().equals(DEPLOYMENT_SPECIFICATION)) {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(NODE_CONTAINER));
        }
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(NODE_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + NODE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("modelAndPackageChildrenParameters")
    public void testDeleteSemanticNodeInPackage(EClass elementType, EReference containmentReference) {
        // Create all the Package sub-nodes to delete
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
        Node packageContainer = (Node) this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER);
        this.createModelAndPackageSubNodes(packageContainer, PACKAGE_SUB_NODE_SUFFIX);

        DeletionGraphicalChecker graphicalChecker = switch (elementType.getName()) {
            case "Constraint", DEPLOYMENT_SPECIFICATION -> new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
            default -> new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        };

        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + PACKAGE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

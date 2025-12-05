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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.DDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.checker.NodeGraphicalDnDGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.deployment.utils.DDMappingTypes;
import org.eclipse.papyrus.web.tools.test.GraphicalDropTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test graphical drop tools in the Deployment Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class DDGraphicalDropTest extends GraphicalDropTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    private static final String ARTIFACT_CONTAINER = "ArtifactContainer";

    private static final String DEVICE_CONTAINER = "DeviceContainer";

    private static final String EXECUTION_ENVIRONMENT_CONTAINER = "ExecutionEnvironmentContainer";

    private static final String MODEL_CONTAINER = "ModelContainer";

    private static final String NODE_CONTAINER = "NodeContainer";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    public DDGraphicalDropTest() {
        super(DEFAULT_DOCUMENT, DDDiagramDescriptionBuilder.DD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> artifactDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact(), UML.getArtifact_NestedArtifact()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), UML.getArtifact_NestedArtifact()));
    }

    private static Stream<Arguments> deviceDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), UML.getClass_NestedClassifier()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDevice()), UML.getDevice(), UML.getNode_NestedNode()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment(), UML.getNode_NestedNode()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getNode()), UML.getNode(), UML.getNode_NestedNode()));
    }

    private static Stream<Arguments> executionEnvironmentDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact(), UML.getClass_NestedClassifier()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), UML.getClass_NestedClassifier()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment(), UML.getNode_NestedNode()));
    }

    private static Stream<Arguments> modelAndPackageAndDiagramDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDevice()), UML.getDevice(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getModel()), UML.getModel(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getNode()), UML.getNode(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT));
    }

    private static Stream<Arguments> nodeDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact(), UML.getClass_NestedClassifier()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), UML.getClass_NestedClassifier()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDevice()), UML.getDevice(), UML.getNode_NestedNode()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment(), UML.getNode_NestedNode()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getNode()), UML.getNode(), UML.getNode_NestedNode()));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getArtifact()), ARTIFACT_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getDevice()), DEVICE_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), EXECUTION_ENVIRONMENT_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getModel()), MODEL_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getNode()), NODE_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("modelAndPackageAndDiagramDropParameters")
    public void testDropOnDiagram(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        Node containerNode = (Node) this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER);
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(containerNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, null,
                DDMappingTypes.getMappingType(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                this::getRootSemanticElement, containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), UML.getPackage_PackagedElement());

        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnDiagram(nodesToDrop, checker);
    }

    @ParameterizedTest
    @MethodSource("modelAndPackageAndDiagramDropParameters")
    public void testDropOnPackage(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                DDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodesToDrop, PACKAGE_CONTAINER, checker);
    }

    @ParameterizedTest
    @MethodSource("modelAndPackageAndDiagramDropParameters")
    public void testDropOnModel(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER),
                DDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(MODEL_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodesToDrop, MODEL_CONTAINER, checker);
    }

    @ParameterizedTest
    @MethodSource("artifactDropParameters")
    public void testDropOnArtifact(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(ARTIFACT_CONTAINER),
                DDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(ARTIFACT_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodesToDrop, ARTIFACT_CONTAINER, checker);
    }

    @ParameterizedTest
    @MethodSource("deviceDropParameters")
    public void testDropOnDevice(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(DEVICE_CONTAINER),
                DDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(DEVICE_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodesToDrop, DEVICE_CONTAINER, checker);
    }

    @ParameterizedTest
    @MethodSource("executionEnvironmentDropParameters")
    public void testDropOnExecutionEnvironment(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram,
                () -> this.findGraphicalElementContentByLabel(EXECUTION_ENVIRONMENT_CONTAINER),
                DDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(EXECUTION_ENVIRONMENT_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodesToDrop, EXECUTION_ENVIRONMENT_CONTAINER, checker);
    }

    @ParameterizedTest
    @MethodSource("nodeDropParameters")
    public void testDropOnNode(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(NODE_CONTAINER),
                DDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(NODE_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodesToDrop, NODE_CONTAINER, checker);
    }
}

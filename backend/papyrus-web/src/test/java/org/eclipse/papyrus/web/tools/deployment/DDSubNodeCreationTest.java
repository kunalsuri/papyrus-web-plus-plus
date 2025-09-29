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
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.deployment.utils.DDMappingTypes;
import org.eclipse.papyrus.web.tools.test.NodeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node creation tools inside graphical parents in the Deployment Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class DDSubNodeCreationTest extends NodeCreationTest {

    private static final String DEPLOYMENT_SPECIFICATION = "DeploymentSpecification";

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    private static final String ARTIFACT_CONTAINER = "ArtifactContainer";

    private static final String DEVICE_CONTAINER = "DeviceContainer";

    private static final String EXECUTION_ENVIRONMENT_CONTAINER = "ExecutionEnvironmentContainer";

    private static final String MODEL_CONTAINER = "ModelContainer";

    private static final String NODE_CONTAINER = "NodeContainer";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    public DDSubNodeCreationTest() {
        super(DEFAULT_DOCUMENT, DDDiagramDescriptionBuilder.DD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> artifactChildrenParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact(), UML.getArtifact_NestedArtifact()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), UML.getArtifact_NestedArtifact()));
    }

    private static Stream<Arguments> deviceChildrenParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), UML.getClass_NestedClassifier()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDevice()), UML.getDevice(), UML.getNode_NestedNode()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment(), UML.getNode_NestedNode()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getNode()), UML.getNode(), UML.getNode_NestedNode()));
    }

    private static Stream<Arguments> executionEnvironmentChildrenParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact(), UML.getClass_NestedClassifier()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), UML.getClass_NestedClassifier()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment(), UML.getNode_NestedNode()));
    }

    private static Stream<Arguments> modelAndPackageChildrenParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDevice()), UML.getDevice(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getModel()), UML.getModel(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getNode()), UML.getNode(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT));
    }

    private static Stream<Arguments> nodeChildrenParameters() {
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

    @ParameterizedTest(name = "[{index}] Create Node {1} in Artifact")
    @MethodSource("artifactChildrenParameters")
    public void testCreateNodeInArtifact(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = DDMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker;
        if (expectedType.isSuperTypeOf(UML.getArtifact())) {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(ARTIFACT_CONTAINER), mappingType,
                    this.getCapturedNodes());
        } else {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(ARTIFACT_CONTAINER), mappingType,
                    this.getCapturedNodes());
        }
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(ARTIFACT_CONTAINER), expectedContainmentReference);
        this.createSubNode(ARTIFACT_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Device")
    @MethodSource("deviceChildrenParameters")
    public void testCreateNodeInDevice(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = DDMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker;
        if (expectedType.isSuperTypeOf(UML.getDeploymentSpecification())) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(DEVICE_CONTAINER), mappingType,
                    this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(DEVICE_CONTAINER), mappingType,
                    this.getCapturedNodes());
        }

        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(DEVICE_CONTAINER), expectedContainmentReference);
        this.createSubNode(DEVICE_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in ExecutionEnvironment")
    @MethodSource("executionEnvironmentChildrenParameters")
    public void testCreateNodeInExecutionEnvironment(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = DDMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker;
        if (expectedType.getName().equals(DEPLOYMENT_SPECIFICATION)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(EXECUTION_ENVIRONMENT_CONTAINER), mappingType,
                    this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(EXECUTION_ENVIRONMENT_CONTAINER), mappingType,
                    this.getCapturedNodes());
        }

        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(EXECUTION_ENVIRONMENT_CONTAINER), expectedContainmentReference);
        this.createSubNode(EXECUTION_ENVIRONMENT_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Model")
    @MethodSource("modelAndPackageChildrenParameters")
    public void testCreateNodeInModel(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = DDMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = switch (expectedType.getName()) {
            case "Comment", "Constraint", DEPLOYMENT_SPECIFICATION -> new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER), mappingType,
                    this.getCapturedNodes());
            default -> new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER), mappingType,
                    this.getCapturedNodes());
        };

        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(MODEL_CONTAINER), expectedContainmentReference);
        this.createSubNode(MODEL_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Node")
    @MethodSource("nodeChildrenParameters")
    public void testCreateNodeInNode(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = DDMappingTypes.getMappingTypeAsSubNode(expectedType);

        NodeCreationGraphicalChecker graphicalChecker;
        if (expectedType.getName().equals(DEPLOYMENT_SPECIFICATION)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(NODE_CONTAINER), mappingType,
                    this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(NODE_CONTAINER), mappingType,
                    this.getCapturedNodes());
        }

        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(NODE_CONTAINER), expectedContainmentReference);
        this.createSubNode(NODE_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Package")
    @MethodSource("modelAndPackageChildrenParameters")
    public void testCreateNodeInPackage(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = DDMappingTypes.getMappingTypeAsSubNode(expectedType);

        NodeCreationGraphicalChecker graphicalChecker = switch (expectedType.getName()) {
            case "Comment", "Constraint", DEPLOYMENT_SPECIFICATION -> new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER), mappingType,
                    this.getCapturedNodes());
            default -> new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER), mappingType,
                    this.getCapturedNodes());
        };

        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), expectedContainmentReference);
        this.createSubNode(PACKAGE_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

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
package org.eclipse.papyrus.web.tools.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.checker.NodeGraphicalDnDGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.profile.utils.PRDMappingTypes;
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
 * Test graphical drop tools in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDGraphicalDropTest extends GraphicalDropTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    private static final String CLASS_CONTAINER = "ClassContainer";

    private static final String CLASS_SOURCE = "ClassSource";

    private static final String CLASS_ATTRIBUTE_COMPARTMENT = "PRD_Class_Attributes_SHARED_CompartmentNode";

    private static final String CLASS_OPERATION_COMPARTMENT = "PRD_Class_Operations_SHARED_CompartmentNode";

    private static final String DATA_TYPE_CONTAINER = "DataTypeContainer";

    private static final String DATA_TYPE_ATTRIBUTE_COMPARTMENT = "PRD_DataType_Attributes_SHARED_CompartmentNode";

    private static final String DATA_TYPE_OPERATION_COMPARTMENT = "PRD_DataType_Operations_SHARED_CompartmentNode";

    private static final String ENUMERATION_CONTAINER = "EnumerationContainer";

    private static final String ENUMERATION_LITERAL_COMPARTMENT = "PRD_Enumeration_Literals_SHARED_CompartmentNode";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final String PROFILE_CONTAINER = "ProfileContainer";

    private static final String STEREOTYPE_CONTAINER = "StereotypeContainer";

    private static final String STEREOTYPE_ATTRIBUTE_COMPARTMENT = "PRD_Stereotype_Attributes_SHARED_CompartmentNode";

    private static final String STEREOTYPE_OPERATION_COMPARTMENT = "PRD_Stereotype_Operations_SHARED_CompartmentNode";

    public PRDGraphicalDropTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> packageAndProfileAndDiagramDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getClass_()), UML.getClass_(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDataType()), UML.getDataType(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getEnumeration()), UML.getEnumeration(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPrimitiveType()), UML.getPrimitiveType(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProfile()), UML.getProfile(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getStereotype()), UML.getStereotype(), PACKAGED_ELEMENT)//
        );
    }

    private static Stream<Arguments> classAndStereotypeDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getOperation()), UML.getOperation(), UML.getClass_OwnedOperation()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty(), UML.getStructuredClassifier_OwnedAttribute())//
        );
    }

    private static Stream<Arguments> dataTypeDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getOperation()), UML.getOperation(), UML.getDataType_OwnedOperation()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty(), UML.getDataType_OwnedAttribute())//
        );
    }

    private static Stream<Arguments> enumerationDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getEnumerationLiteral()), UML.getEnumerationLiteral(), UML.getEnumeration_OwnedLiteral())//
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getClass_()), CLASS_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getDataType()), DATA_TYPE_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getEnumeration()), ENUMERATION_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getProfile()), PROFILE_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getStereotype()), STEREOTYPE_CONTAINER);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("packageAndProfileAndDiagramDropParameters")
    public void testDropOnDiagram(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        Node containerNode = (Node) this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER);
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(containerNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, null,
                PRDMappingTypes.getMappingType(expectedType), this.getCapturedNodes());
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
    @MethodSource("packageAndProfileAndDiagramDropParameters")
    public void testDropOnPackage(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                PRDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
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
    @MethodSource("packageAndProfileAndDiagramDropParameters")
    public void testDropOnProfile(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER),
                PRDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(PROFILE_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodesToDrop, PROFILE_CONTAINER, checker);
    }

    @ParameterizedTest
    @MethodSource("classAndStereotypeDropParameters")
    public void testDropOnClass(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(expectedType)) {
            compartmentMapping = CLASS_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(expectedType)) {
            compartmentMapping = CLASS_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }

        Node parentClassNode = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getClass_()), CLASS_SOURCE);
        Node parentClassCompartmentNode = this.getSubNode(parentClassNode, compartmentMapping);
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(parentClassCompartmentNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.getSubNode(CLASS_CONTAINER, compartmentMapping),
                PRDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(CLASS_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(CLASS_SOURCE), containmentReference);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainerCompartment(nodesToDrop, CLASS_CONTAINER, compartmentMapping, checker);
    }

    @ParameterizedTest
    @MethodSource("classAndStereotypeDropParameters")
    public void testDropOnStereotype(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(expectedType)) {
            compartmentMapping = STEREOTYPE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(expectedType)) {
            compartmentMapping = STEREOTYPE_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }

        Node parentStereotypeNode = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getStereotype()), "StereotypeSource");
        Node parentStereotypeCompartmentNode = this.getSubNode(parentStereotypeNode, compartmentMapping);
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(parentStereotypeCompartmentNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.getSubNode(STEREOTYPE_CONTAINER, compartmentMapping),
                PRDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(STEREOTYPE_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName("StereotypeSource"), containmentReference);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainerCompartment(nodesToDrop, STEREOTYPE_CONTAINER, compartmentMapping, checker);
    }

    @ParameterizedTest
    @MethodSource("dataTypeDropParameters")
    public void testDropOnDataType(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(expectedType)) {
            compartmentMapping = DATA_TYPE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(expectedType)) {
            compartmentMapping = DATA_TYPE_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }

        Node parentDataTypeNode = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getDataType()), "DataTypeSource");
        Node parentDataTypeCompartmentNode = this.getSubNode(parentDataTypeNode, compartmentMapping);
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(parentDataTypeCompartmentNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.getSubNode(DATA_TYPE_CONTAINER, compartmentMapping),
                PRDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(DATA_TYPE_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName("DataTypeSource"), containmentReference);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainerCompartment(nodesToDrop, DATA_TYPE_CONTAINER, compartmentMapping, checker);
    }

    @ParameterizedTest
    @MethodSource("enumerationDropParameters")
    public void testDropOnEnumeration(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        final String compartmentMapping = ENUMERATION_LITERAL_COMPARTMENT;

        Node parentEnumerationNode = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getEnumeration()), "EnumerationSource");
        Node parentEnumerationCompartmentNode = this.getSubNode(parentEnumerationNode, compartmentMapping);
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(parentEnumerationCompartmentNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.getSubNode(ENUMERATION_CONTAINER, compartmentMapping),
                PRDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(ENUMERATION_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName("EnumerationSource"), containmentReference);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainerCompartment(nodesToDrop, ENUMERATION_CONTAINER, compartmentMapping, checker);
    }
}

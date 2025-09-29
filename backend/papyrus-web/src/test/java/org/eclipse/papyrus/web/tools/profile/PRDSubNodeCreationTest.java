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
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDClassifierCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDEnumerationCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.profile.utils.PRDMappingTypes;
import org.eclipse.papyrus.web.tools.test.NodeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node creation tools at the root of the diagram in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDSubNodeCreationTest extends NodeCreationTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    private static final String CLASS_CONTAINER = "ClassContainer";

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

    public PRDSubNodeCreationTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> packageAndProfileChildrenParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getClass_()), UML.getClass_(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDataType()), UML.getDataType(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getEnumeration()), UML.getEnumeration(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPrimitiveType()), UML.getPrimitiveType(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProfile()), UML.getProfile(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getStereotype()), UML.getStereotype(), PACKAGED_ELEMENT)//
        );
    }

    private static Stream<Arguments> classAndStereotypeParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getOperation()), UML.getOperation(), UML.getClass_OwnedOperation()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty(), UML.getStructuredClassifier_OwnedAttribute())//
        );
    }

    private static Stream<Arguments> dataTypeParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getOperation()), UML.getOperation(), UML.getDataType_OwnedOperation()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty(), UML.getDataType_OwnedAttribute())//
        );
    }

    private static Stream<Arguments> enumerationParameters() {
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

    @ParameterizedTest(name = "[{index}] Create Node {1} in Package")
    @MethodSource("packageAndProfileChildrenParameters")
    public void testCreateNodeInPackage(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = PRDMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker;

        if (UML.getEnumeration().isSuperTypeOf(expectedType)) {
            graphicalChecker = new PRDEnumerationCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER), mappingType,
                    this.getCapturedNodes());
        } else if (UML.getClassifier().isSuperTypeOf(expectedType) && !UML.getPrimitiveType().isSuperTypeOf(expectedType)) {
            graphicalChecker = new PRDClassifierCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER), mappingType,
                    this.getCapturedNodes());
        } else if (UML.getProfile().isSuperTypeOf(expectedType) || UML.getPackage().isSuperTypeOf(expectedType)) {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER), mappingType,
                    this.getCapturedNodes());
        } else {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER), mappingType, this.getCapturedNodes());
        }
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), expectedContainmentReference);
        this.createSubNode(PACKAGE_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Profile")
    @MethodSource("packageAndProfileChildrenParameters")
    public void testCreateNodeInProfile(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = PRDMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker;

        if (UML.getEnumeration().isSuperTypeOf(expectedType)) {
            graphicalChecker = new PRDEnumerationCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER), mappingType, this.getCapturedNodes());
        } else if (UML.getClassifier().isSuperTypeOf(expectedType) && !UML.getPrimitiveType().isSuperTypeOf(expectedType)) {
            graphicalChecker = new PRDClassifierCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER), mappingType, this.getCapturedNodes());
        } else if (UML.getProfile().isSuperTypeOf(expectedType) || UML.getPackage().isSuperTypeOf(expectedType)) {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER), mappingType, this.getCapturedNodes());
        } else {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER), mappingType, this.getCapturedNodes());
        }
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(PROFILE_CONTAINER), expectedContainmentReference);
        this.createSubNode(PROFILE_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Class")
    @MethodSource("classAndStereotypeParameters")
    public void testCreateNodeInClass(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = PRDMappingTypes.getMappingTypeAsSubNode(expectedType);
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(expectedType)) {
            compartmentMapping = CLASS_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(expectedType)) {
            compartmentMapping = CLASS_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(CLASS_CONTAINER, compartmentMapping), mappingType,
                this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(CLASS_CONTAINER), expectedContainmentReference);
        this.createSubNodeOnCompartment(CLASS_CONTAINER, compartmentMapping, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in DataType")
    @MethodSource("dataTypeParameters")
    public void testCreateNodeInDataType(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = PRDMappingTypes.getMappingTypeAsSubNode(expectedType);
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(expectedType)) {
            compartmentMapping = DATA_TYPE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(expectedType)) {
            compartmentMapping = DATA_TYPE_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(DATA_TYPE_CONTAINER, compartmentMapping), mappingType,
                this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(DATA_TYPE_CONTAINER), expectedContainmentReference);
        this.createSubNodeOnCompartment(DATA_TYPE_CONTAINER, compartmentMapping, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Stereotype")
    @MethodSource("classAndStereotypeParameters")
    public void testCreateNodeInStereotype(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = PRDMappingTypes.getMappingTypeAsSubNode(expectedType);
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(expectedType)) {
            compartmentMapping = STEREOTYPE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(expectedType)) {
            compartmentMapping = STEREOTYPE_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(STEREOTYPE_CONTAINER, compartmentMapping), mappingType,
                this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(STEREOTYPE_CONTAINER), expectedContainmentReference);
        this.createSubNodeOnCompartment(STEREOTYPE_CONTAINER, compartmentMapping, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Enumeration")
    @MethodSource("enumerationParameters")
    public void testCreateNodeInEnumeration(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = PRDMappingTypes.getMappingTypeAsSubNode(expectedType);
        final String compartmentMapping;
        if (UML.getEnumerationLiteral().isSuperTypeOf(expectedType)) {
            compartmentMapping = ENUMERATION_LITERAL_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(ENUMERATION_CONTAINER, compartmentMapping), mappingType,
                this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(ENUMERATION_CONTAINER), expectedContainmentReference);
        this.createSubNodeOnCompartment(ENUMERATION_CONTAINER, compartmentMapping, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

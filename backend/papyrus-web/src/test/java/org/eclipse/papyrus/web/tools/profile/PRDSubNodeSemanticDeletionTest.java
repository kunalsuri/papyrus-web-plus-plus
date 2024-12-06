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
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.HolderDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDClassifierDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDEnumerationDeletionGraphicalChecker;
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
 * Tests node semantic deletion tools at the root of the diagram in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDSubNodeSemanticDeletionTest extends NodeDeletionTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    private static final String CLASS_CONTAINER = "ClassContainer";

    private static final String CLASS_ATTRIBUTE_COMPARTMENT = "PRD_Class_Attributes_SHARED_CompartmentNode";

    private static final String CLASS_OPERATION_COMPARTMENT = "PRD_Class_Operations_SHARED_CompartmentNode";

    private static final String CLASS_SUB_NODE_SUFFIX = "_In_Class";

    private static final String DATA_TYPE_CONTAINER = "DataTypeContainer";

    private static final String DATA_TYPE_ATTRIBUTE_COMPARTMENT = "PRD_DataType_Attributes_SHARED_CompartmentNode";

    private static final String DATA_TYPE_OPERATION_COMPARTMENT = "PRD_DataType_Operations_SHARED_CompartmentNode";

    private static final String DATA_TYPE_SUB_NODE_SUFFIX = "_In_DataType";

    private static final String ENUMERATION_CONTAINER = "EnumerationContainer";

    private static final String ENUMERATION_SUB_NODE_SUFFIX = "_In_Enumeration";

    private static final String ENUMERATION_LITERAL_COMPARTMENT = "PRD_Enumeration_Literals_SHARED_CompartmentNode";

    private static final String ENUMERATION_LITERAL_SUB_NODE = "EnumerationLiteral_In_Enumeration";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final String PACKAGE_SUB_NODE_SUFFIX = "_In_Package";

    private static final String PROFILE_CONTAINER = "ProfileContainer";

    private static final String PROFILE_SUB_NODE_SUFFIX = "_In_Profile";

    private static final String STEREOTYPE_CONTAINER = "StereotypeContainer";

    private static final String STEREOTYPE_ATTRIBUTE_COMPARTMENT = "PRD_Stereotype_Attributes_SHARED_CompartmentNode";

    private static final String STEREOTYPE_OPERATION_COMPARTMENT = "PRD_Stereotype_Operations_SHARED_CompartmentNode";

    private static final String STEREOTYPE_SUB_NODE_SUFFIX = "_In_Stereotype";

    public PRDSubNodeSemanticDeletionTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> packageAndProfileChildrenParameters() {
        return Stream.of(//
                Arguments.of(UML.getClass_(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(UML.getDataType(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getEnumeration(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getPackage(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getPrimitiveType(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getProfile(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getStereotype(), PACKAGED_ELEMENT) //
        );
    }

    private static Stream<Arguments> classAndStereotypeParameters() {
        return Stream.of(//
                Arguments.of(UML.getOperation(), UML.getClass_OwnedOperation()), //
                Arguments.of(UML.getProperty(), UML.getStructuredClassifier_OwnedAttribute())//
        );
    }

    private static Stream<Arguments> dataTypeParameters() {
        return Stream.of(//
                Arguments.of(UML.getOperation(), UML.getDataType_OwnedOperation()), //
                Arguments.of(UML.getProperty(), UML.getDataType_OwnedAttribute()) //
        );
    }

    private static Stream<Arguments> enumerationParameters() {
        return Stream.of(//
                Arguments.of(UML.getEnumerationLiteral(), UML.getEnumeration_OwnedLiteral()));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        Node classContainer = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getClass_()), CLASS_CONTAINER);
        this.createOperationAndProperty(classContainer, CLASS_SUB_NODE_SUFFIX);
        Node dataTypeContainer = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getDataType()), DATA_TYPE_CONTAINER);
        this.createOperationAndProperty(dataTypeContainer, DATA_TYPE_SUB_NODE_SUFFIX);
        Node enumerationContainer = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getEnumeration()), ENUMERATION_CONTAINER);
        this.createNodeWithLabel(enumerationContainer.getId(), new CreationTool(ToolSections.NODES, UML.getEnumerationLiteral()), ENUMERATION_LITERAL_SUB_NODE);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
        Node packageContent = (Node) this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER);
        this.createPackageAndProfileSubNodes(packageContent, PACKAGE_SUB_NODE_SUFFIX);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getProfile()), PROFILE_CONTAINER);
        Node profileContent = (Node) this.findGraphicalElementContentByLabel(PROFILE_CONTAINER);
        this.createPackageAndProfileSubNodes(profileContent, PROFILE_SUB_NODE_SUFFIX);
        Node stereotypeContainer = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getStereotype()), STEREOTYPE_CONTAINER);
        this.createOperationAndProperty(stereotypeContainer, STEREOTYPE_SUB_NODE_SUFFIX);
    }

    private void createOperationAndProperty(Node classifierNode, String classifierSuffix) {
        String classifierNodeId = classifierNode.getId();
        this.createNodeWithLabel(classifierNodeId, new CreationTool(ToolSections.NODES, UML.getOperation()), UML.getOperation().getName() + classifierSuffix);
        this.createNodeWithLabel(classifierNodeId, new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty().getName() + classifierSuffix);
    }

    private void createPackageAndProfileSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getClass_()), UML.getClass_().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getDataType()), UML.getDataType().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getEnumeration()), UML.getEnumeration().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getPrimitiveType()), UML.getPrimitiveType().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getProfile()), UML.getProfile().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getStereotype()), UML.getStereotype().getName() + suffix);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("packageAndProfileChildrenParameters")
    public void testDeleteSemanticNodeInPackage(EClass elementType, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker;
        if (UML.getEnumeration().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDEnumerationDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        } else if (UML.getClassifier().isSuperTypeOf(elementType) && !UML.getPrimitiveType().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDClassifierDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        } else if (UML.getPackage().isSuperTypeOf(elementType)) {
            graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        } else {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        }
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + PACKAGE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("packageAndProfileChildrenParameters")
    public void testDeleteSemanticNodeInProfile(EClass elementType, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker;
        if (UML.getEnumeration().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDEnumerationDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER));
        } else if (UML.getClassifier().isSuperTypeOf(elementType) && !UML.getPrimitiveType().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDClassifierDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER));
        } else if (UML.getPackage().isSuperTypeOf(elementType)) {
            graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER));
        } else {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER));
        }
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(PROFILE_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + PROFILE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("classAndStereotypeParameters")
    public void testDeleteSemanticNodeInClass(EClass elementType, EReference containmentReference) {
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(elementType)) {
            compartmentMapping = CLASS_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(elementType)) {
            compartmentMapping = CLASS_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.getSubNode(CLASS_CONTAINER, compartmentMapping));
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(CLASS_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + CLASS_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("classAndStereotypeParameters")
    public void testDeleteSemanticNodeInStereotype(EClass elementType, EReference containmentReference) {
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(elementType)) {
            compartmentMapping = STEREOTYPE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(elementType)) {
            compartmentMapping = STEREOTYPE_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.getSubNode(STEREOTYPE_CONTAINER, compartmentMapping));
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(STEREOTYPE_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + STEREOTYPE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("dataTypeParameters")
    public void testDeleteSemanticNodeInDataType(EClass elementType, EReference containmentReference) {
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(elementType)) {
            compartmentMapping = DATA_TYPE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(elementType)) {
            compartmentMapping = DATA_TYPE_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.getSubNode(DATA_TYPE_CONTAINER, compartmentMapping));
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(DATA_TYPE_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + DATA_TYPE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("enumerationParameters")
    public void testDeleteSemanticNodeInEnumeration(EClass elementType, EReference containmentReference) {
        final String compartmentMapping = ENUMERATION_LITERAL_COMPARTMENT;
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.getSubNode(ENUMERATION_CONTAINER, compartmentMapping));
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(ENUMERATION_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + ENUMERATION_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

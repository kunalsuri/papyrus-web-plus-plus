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

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.component.checker.CPDInterfaceCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.component.utils.CPDMappingTypes;
import org.eclipse.papyrus.web.tools.test.NodeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node creation tools at the root of the diagram in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDSubNodeCreationTest extends NodeCreationTest {

    private static final String COMPONENT_CONTAINER = "ComponentContainer";

    private static final String COMPONENT_TYPE = "ComponentType";

    private static final String INTERFACE_CONTAINER = "InterfaceContainer";

    private static final String INTERFACE_ATTRIBUTE_COMPARTMENT = "CPD_Interface_Attributes_SHARED_CompartmentNode";

    private static final String INTERFACE_OPERATION_COMPARTMENT = "CPD_Interface_Operations_SHARED_CompartmentNode";

    private static final String INTERFACE_RECEPTION_COMPARTMENT = "CPD_Interface_Receptions_SHARED_CompartmentNode";

    private static final String MODEL_CONTAINER = "ModelContainer";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final String PROPERTY_CONTAINER = "PropertyContainer";

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    public CPDSubNodeCreationTest() {
        super(DEFAULT_DOCUMENT, CPDDiagramDescriptionBuilder.CPD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> packageAndModelChildrenParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComponent()), UML.getComponent(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getInterface()), UML.getInterface(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getModel()), UML.getModel(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT)); //
    }

    private static Stream<Arguments> componentChildrenParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComponent()), UML.getComponent(), UML.getComponent_PackagedElement()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPort()), UML.getPort(), UML.getStructuredClassifier_OwnedAttribute()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty(), UML.getStructuredClassifier_OwnedAttribute())//
        );
    }

    private static Stream<Arguments> interfaceParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getOperation()), UML.getOperation(), UML.getInterface_OwnedOperation()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty(), UML.getInterface_OwnedAttribute()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getReception()), UML.getReception(), UML.getInterface_OwnedReception()) //
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getInterface()), INTERFACE_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getModel()), MODEL_CONTAINER);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Package")
    @MethodSource("packageAndModelChildrenParameters")
    public void testCreateNodeInPackage(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = CPDMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker;

        if (UML.getInterface().isSuperTypeOf(expectedType)) {
            graphicalChecker = new CPDInterfaceCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER), mappingType, this.getCapturedNodes());
        } else if (UML.getComment().isSuperTypeOf(expectedType) || UML.getConstraint().isSuperTypeOf(expectedType)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER), mappingType, this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER), mappingType, this.getCapturedNodes());
        }
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), expectedContainmentReference);
        this.createSubNode(PACKAGE_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Model")
    @MethodSource("packageAndModelChildrenParameters")
    public void testCreateNodeInModel(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = CPDMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker;
        if (UML.getInterface().isSuperTypeOf(expectedType)) {
            graphicalChecker = new CPDInterfaceCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER), mappingType, this.getCapturedNodes());
        } else if (UML.getComment().isSuperTypeOf(expectedType) || UML.getConstraint().isSuperTypeOf(expectedType)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER), mappingType, this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER), mappingType, this.getCapturedNodes());
        }
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(MODEL_CONTAINER), expectedContainmentReference);
        this.createSubNode(MODEL_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Component")
    @MethodSource("componentChildrenParameters")
    public void testCreateNodeInComponent(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = CPDMappingTypes.getMappingTypeAsSubNode(expectedType);

        NodeCreationGraphicalChecker graphicalChecker;
        if (UML.getPort().isSuperTypeOf(expectedType)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER), mappingType,
                    this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER), mappingType,
                    this.getCapturedNodes());
        }

        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(COMPONENT_CONTAINER), expectedContainmentReference);
        this.createSubNode(COMPONENT_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Interface")
    @MethodSource("interfaceParameters")
    public void testCreateNodeInInterface(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType;
        if (UML.getProperty().isSuperTypeOf(expectedType)) {
            // Property mapping in an Interface is CPD_Property (because it isn't shared).
            // This is a workaround because the component diagram contains 2 mappings for properties (one in component
            // and one in interface). This means that getMappingTypeAsSubNode returns only one of them (the component
            // one).
            mappingType = CPDMappingTypes.getMappingType(expectedType);
        } else {
            mappingType = CPDMappingTypes.getMappingTypeAsSubNode(expectedType);
        }

        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(expectedType)) {
            compartmentMapping = INTERFACE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(expectedType)) {
            compartmentMapping = INTERFACE_ATTRIBUTE_COMPARTMENT;
        } else if (UML.getReception().isSuperTypeOf(expectedType)) {
            compartmentMapping = INTERFACE_RECEPTION_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(INTERFACE_CONTAINER, compartmentMapping), mappingType,
                this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(INTERFACE_CONTAINER), expectedContainmentReference);
        this.createSubNodeOnCompartment(INTERFACE_CONTAINER, compartmentMapping, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @Test
    public void testCreatePropertyInTypedProperty() {
        EClass expectedType = UML.getProperty();
        EReference expectedContainmentReference = UML.getStructuredClassifier_OwnedAttribute();

        // create Property container typed with COMPONENT_TYPE
        Node componentTypeNode = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_TYPE);
        String componentContentId = this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER).getId();
        Node propertyContainerNode = this.createNodeWithLabel(componentContentId, new CreationTool(ToolSections.NODES, expectedType), PROPERTY_CONTAINER);
        // type the property container
        IEMFEditingContext editingContext = (IEMFEditingContext) this.getEditingContext();
        Optional<Object> propertyContainerOptional = this.getObjectSearchService()
                .getObject(editingContext, propertyContainerNode.getTargetObjectId());
        Optional<Object> componentTypeOptional = this.getObjectSearchService()
                .getObject(editingContext, componentTypeNode.getTargetObjectId());
        Property propertyContainer = (Property) propertyContainerOptional.get();
        propertyContainer.setType((Component) componentTypeOptional.get());
        String mappingType = CPDMappingTypes.getMappingTypeAsSubNode(UML.getProperty());

        NodeCreationGraphicalChecker graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROPERTY_CONTAINER), mappingType,
                this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(COMPONENT_TYPE), expectedContainmentReference);
        CreationTool nodeCreationTool = new CreationTool(ToolSections.NODES, UML.getProperty());
        this.createSubNode(PROPERTY_CONTAINER, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

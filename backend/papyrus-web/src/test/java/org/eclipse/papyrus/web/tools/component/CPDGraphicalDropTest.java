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

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.checker.NodeGraphicalDnDGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.component.utils.CPDMappingTypes;
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
 * Test graphical drop tools in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDGraphicalDropTest extends GraphicalDropTest {

    private static final String COMPONENT_CONTAINER = "ComponentContainer";

    private static final String COMPONENT_SOURCE = "ComponentSource";

    private static final String INTERFACE_CONTAINER = "InterfaceContainer";

    private static final String INTERFACE_SOURCE = "InterfaceSource";

    private static final String INTERFACE_ATTRIBUTE_COMPARTMENT = "CPD_Interface_Attributes_SHARED_CompartmentNode";

    private static final String INTERFACE_OPERATION_COMPARTMENT = "CPD_Interface_Operations_SHARED_CompartmentNode";

    private static final String INTERFACE_RECEPTION_COMPARTMENT = "CPD_Interface_Receptions_SHARED_CompartmentNode";

    private static final String MODEL_CONTAINER = "ModelContainer";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    public CPDGraphicalDropTest() {
        super(DEFAULT_DOCUMENT, CPDDiagramDescriptionBuilder.CPD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> packageAndModelAndDiagramDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComponent()), UML.getComponent(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getInterface()), UML.getInterface(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getModel()), UML.getModel(), PACKAGED_ELEMENT),
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT)); //
    }

    private static Stream<Arguments> componentDropParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComponent()), UML.getComponent(), UML.getComponent_PackagedElement()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty(), UML.getStructuredClassifier_OwnedAttribute())//
        );
    }

    private static Stream<Arguments> interfaceDropParameters() {
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

    @ParameterizedTest
    @MethodSource("packageAndModelAndDiagramDropParameters")
    public void testDropOnDiagram(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        Node containerNode = (Node) this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER);
        Node nodeToDrop = this.createNodeWithLabel(containerNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);

        NodeGraphicalDnDGraphicalChecker graphicalChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, null,
                CPDMappingTypes.getMappingType(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                this::getRootSemanticElement, containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), UML.getPackage_PackagedElement());

        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnDiagram(nodeToDrop, checker);
    }

    @ParameterizedTest
    @MethodSource("packageAndModelAndDiagramDropParameters")
    public void testDropOnPackage(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        Node nodeToDrop = this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX);

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                CPDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodeToDrop, PACKAGE_CONTAINER, checker);
    }

    @ParameterizedTest
    @MethodSource("packageAndModelAndDiagramDropParameters")
    public void testDropOnModel(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        Node nodeToDrop = this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX);

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER),
                CPDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(MODEL_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodeToDrop, MODEL_CONTAINER, checker);
    }

    @ParameterizedTest
    @MethodSource("componentDropParameters")
    public void testDropOnComponent(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_SOURCE);
        Node parentComponentNode = (Node) this.findGraphicalElementContentByLabel(COMPONENT_SOURCE);
        Node nodeToDrop = this.createNodeWithLabel(parentComponentNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER),
                CPDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(COMPONENT_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(COMPONENT_SOURCE), containmentReference);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodeToDrop, COMPONENT_CONTAINER, checker);
    }

    @ParameterizedTest
    @MethodSource("interfaceDropParameters")
    public void testDropOnInterface(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
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
        // Operation, Reception, and Property are not shared, so we can use getMappingType to retrieve the mapping.
        final String mappingType = CPDMappingTypes.getMappingType(expectedType);

        Node parentInterfaceNode = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getInterface()), INTERFACE_SOURCE);
        Node parentInterfaceNodeCompartment = this.getSubNode(parentInterfaceNode, compartmentMapping);
        Node nodeToDrop = this.createNodeWithLabel(parentInterfaceNodeCompartment.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.getSubNode(INTERFACE_CONTAINER, compartmentMapping),
                mappingType, this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(INTERFACE_CONTAINER), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(INTERFACE_SOURCE), containmentReference);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainerCompartment(nodeToDrop, INTERFACE_CONTAINER, compartmentMapping, checker);
    }
}

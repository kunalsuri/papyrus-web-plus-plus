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
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.HolderDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.component.checker.CPDInterfaceDeletionGraphicalChecker;
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
 * Tests node semantic deletion tools at the root of the diagram in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDSubNodeSemanticDeletionTest extends NodeDeletionTest {

    private static final String COMPONENT_CONTAINER = "ComponentContainer";

    private static final String COMPONENT_SUB_NODE_SUFFIX = "_In_Component";

    private static final String INTERFACE_CONTAINER = "InterfaceContainer";

    private static final String INTERFACE_ATTRIBUTE_COMPARTMENT = "CPD_Interface_Attributes_SHARED_CompartmentNode";

    private static final String INTERFACE_OPERATION_COMPARTMENT = "CPD_Interface_Operations_SHARED_CompartmentNode";

    private static final String INTERFACE_RECEPTION_COMPARTMENT = "CPD_Interface_Receptions_SHARED_CompartmentNode";

    private static final String INTERFACE_SUB_NODE_SUFFIX = "_In_Interface";

    private static final String MODEL_CONTAINER = "ModelContainer";

    private static final String MODEL_SUB_NODE_SUFFIX = "_In_Model";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final String PACKAGE_SUB_NODE_SUFFIX = "_In_Package";

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    public CPDSubNodeSemanticDeletionTest() {
        super(DEFAULT_DOCUMENT, CPDDiagramDescriptionBuilder.CPD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> packageAndModelChildrenParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(UML.getInterface(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getModel(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getPackage(), PACKAGED_ELEMENT) //
        );
    }

    private static Stream<Arguments> componentParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent(), UML.getComponent_PackagedElement()), //
                Arguments.of(UML.getPort(), UML.getStructuredClassifier_OwnedAttribute()), //
                Arguments.of(UML.getProperty(), UML.getStructuredClassifier_OwnedAttribute())//
        );
    }

    private static Stream<Arguments> interfaceParameters() {
        return Stream.of(//
                Arguments.of(UML.getOperation(), UML.getInterface_OwnedOperation()), //
                Arguments.of(UML.getReception(), UML.getInterface_OwnedReception()), //
                Arguments.of(UML.getProperty(), UML.getInterface_OwnedAttribute()) //
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_CONTAINER);
        Node componentContainer = (Node) this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER);
        this.createNodeWithLabel(componentContainer.getId(), new CreationTool(ToolSections.NODES, UML.getComponent()), UML.getComponent().getName() + COMPONENT_SUB_NODE_SUFFIX);
        this.createNodeWithLabel(componentContainer.getId(), new CreationTool(ToolSections.NODES, UML.getPort()), UML.getPort().getName() + COMPONENT_SUB_NODE_SUFFIX);
        this.createNodeWithLabel(componentContainer.getId(), new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty().getName() + COMPONENT_SUB_NODE_SUFFIX);
        Node interfaceContainer = this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getInterface()), INTERFACE_CONTAINER);
        this.createNodeWithLabel(interfaceContainer.getId(), new CreationTool(ToolSections.NODES, UML.getOperation()), UML.getOperation().getName() + INTERFACE_SUB_NODE_SUFFIX);
        this.createNodeWithLabel(interfaceContainer.getId(), new CreationTool(ToolSections.NODES, UML.getReception()), UML.getReception().getName() + INTERFACE_SUB_NODE_SUFFIX);
        this.createNodeWithLabel(interfaceContainer.getId(), new CreationTool(ToolSections.NODES, UML.getProperty()), UML.getProperty().getName() + INTERFACE_SUB_NODE_SUFFIX);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
        Node packageContainer = (Node) this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER);
        this.createPackageAndModelSubNodes(packageContainer, PACKAGE_SUB_NODE_SUFFIX);
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getModel()), MODEL_CONTAINER);
        Node modelContainer = (Node) this.findGraphicalElementContentByLabel(MODEL_CONTAINER);
        this.createPackageAndModelSubNodes(modelContainer, MODEL_SUB_NODE_SUFFIX);
    }

    private void createPackageAndModelSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getComponent()), UML.getComponent().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getInterface()), UML.getInterface().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getModel()), UML.getModel().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage().getName() + suffix);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("componentParameters")
    public void testDeleteSemanticNodeInComponent(EClass elementType, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker;
        if (UML.getPort().isSuperTypeOf(elementType)) {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementExcludingContentByLabel(COMPONENT_CONTAINER));
        } else {
            graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER));
        }
        new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER));
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(COMPONENT_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + COMPONENT_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("interfaceParameters")
    public void testDeleteSemanticNodeInInterface(EClass elementType, EReference containmentReference) {
        final String compartmentMapping;
        if (UML.getOperation().isSuperTypeOf(elementType)) {
            compartmentMapping = INTERFACE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(elementType)) {
            compartmentMapping = INTERFACE_ATTRIBUTE_COMPARTMENT;
        } else if (UML.getReception().isSuperTypeOf(elementType)) {
            compartmentMapping = INTERFACE_RECEPTION_COMPARTMENT;
        } else {
            compartmentMapping = null;
        }
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.getSubNode(INTERFACE_CONTAINER, compartmentMapping));
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(INTERFACE_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + INTERFACE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("packageAndModelChildrenParameters")
    public void testDeleteSemanticNodeInModel(EClass elementType, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker;
        if (UML.getInterface().isSuperTypeOf(elementType)) {
            graphicalChecker = new CPDInterfaceDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER));
        } else if (UML.getConstraint().isSuperTypeOf(elementType)) {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER));
        } else {
            graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER));
        }
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(MODEL_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + MODEL_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("packageAndModelChildrenParameters")
    public void testDeleteSemanticNodeInPackage(EClass elementType, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker;
        if (UML.getInterface().isSuperTypeOf(elementType)) {
            graphicalChecker = new CPDInterfaceDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        } else if (UML.getConstraint().isSuperTypeOf(elementType)) {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        } else {
            graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        }
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), containmentReference);
        this.deleteSemanticNode(elementType.getName() + PACKAGE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

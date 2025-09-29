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

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.component.checker.CPDInterfaceCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.component.utils.CPDMappingTypes;
import org.eclipse.papyrus.web.tools.test.SemanticDropTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests semantic drop tools in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDSemanticDropTest extends SemanticDropTest {

    private static final String COMPONENT_CONTAINER = "ComponentContainer";

    private static final String INTERFACE_CONTAINER = "InterfaceContainer";

    private static final String INTERFACE_ATTRIBUTE_COMPARTMENT = "CPD_Interface_Attributes_SHARED_CompartmentNode";

    private static final String INTERFACE_OPERATION_COMPARTMENT = "CPD_Interface_Operations_SHARED_CompartmentNode";

    private static final String INTERFACE_RECEPTION_COMPARTMENT = "CPD_Interface_Receptions_SHARED_CompartmentNode";

    private static final String MODEL_CONTAINER = "ModelContainer";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final String DROP_SUFFIX = "Drop";

    public CPDSemanticDropTest() {
        super(DEFAULT_DOCUMENT, CPDDiagramDescriptionBuilder.CPD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> dropOnDiagramAndPackageAndModelParameters() {
        return Stream.of(//
                Arguments.of(UML.getPackage_PackagedElement(), UML.getComponent()), //
                Arguments.of(UML.getElement_OwnedComment(), UML.getComment()), //
                Arguments.of(UML.getNamespace_OwnedRule(), UML.getConstraint()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getInterface()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getModel()),
                Arguments.of(UML.getPackage_PackagedElement(), UML.getPackage())); //
    }

    private static Stream<Arguments> dropOnComponentParameters() {
        return Stream.of(//
                Arguments.of(UML.getComponent_PackagedElement(), UML.getComponent()), //
                Arguments.of(UML.getStructuredClassifier_OwnedAttribute(), UML.getPort()), //
                Arguments.of(UML.getStructuredClassifier_OwnedAttribute(), UML.getProperty()) //
        );
    }

    private static Stream<Arguments> dropOnInterfaceParameters() {
        return Stream.of(//
                Arguments.of(UML.getInterface_OwnedOperation(), UML.getOperation()), //
                Arguments.of(UML.getInterface_OwnedAttribute(), UML.getProperty()), //
                Arguments.of(UML.getInterface_OwnedReception(), UML.getReception()) //
        );
    }

    private static Stream<Arguments> dropAbstractionAndDependencyAndUsageParameters() {
        List<CreationTool> sources = List.of(new CreationTool(ToolSections.NODES, UML.getComponent()));
        List<CreationTool> targets = List.of(
                new CreationTool(ToolSections.NODES, UML.getComponent()),
                new CreationTool(ToolSections.NODES, UML.getConstraint()),
                new CreationTool(ToolSections.NODES, UML.getInterface()),
                new CreationTool(ToolSections.NODES, UML.getModel()),
                new CreationTool(ToolSections.NODES, UML.getPackage()),
                new CreationTool(ToolSections.NODES, UML.getPort()),
                new CreationTool(ToolSections.NODES, UML.getProperty()));
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> dropComponentRealizationAndGeneralizationAndSubstitutionParameters() {
        return Stream.of(Arguments.of(new CreationTool(ToolSections.NODES, UML.getInterface()), new CreationTool(ToolSections.NODES, UML.getComponent())));
    }

    private static Stream<Arguments> dropInterfaceRealizationParameters() {
        return Stream.of(Arguments.of(new CreationTool(ToolSections.NODES, UML.getComponent()), new CreationTool(ToolSections.NODES, UML.getInterface())));
    }

    private static Stream<Arguments> dropManifestationParameters() {
        List<CreationTool> sources = List.of(
                new CreationTool(ToolSections.NODES, UML.getProperty()),
                new CreationTool(ToolSections.NODES, UML.getPort()));
        List<CreationTool> targets = List.of(
                new CreationTool(ToolSections.NODES, UML.getComponent()),
                new CreationTool(ToolSections.NODES, UML.getConstraint()),
                new CreationTool(ToolSections.NODES, UML.getInterface()),
                new CreationTool(ToolSections.NODES, UML.getModel()),
                new CreationTool(ToolSections.NODES, UML.getPackage()));
        return cartesianProduct(sources, targets);
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("dropOnComponentParameters")
    public void testSemanticDropOnComponent(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()),
                COMPONENT_CONTAINER);
        EObject parentElement = this.findSemanticElementByName(COMPONENT_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType,
                elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker;

        if (UML.getPort().isSuperTypeOf(elementType)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER),
                    CPDMappingTypes.getMappingTypeAsSubNode(elementType),
                    this.getCapturedNodes());
            this.semanticDropOnHolder(COMPONENT_CONTAINER, this.getIdentityService().getId(elementToDrop),
                    graphicalChecker);
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER),
                    CPDMappingTypes.getMappingTypeAsSubNode(elementType),
                    this.getCapturedNodes());
            this.semanticDropOnContent(COMPONENT_CONTAINER, this.getIdentityService().getId(elementToDrop),
                    graphicalChecker);
        }
    }

    @ParameterizedTest
    @MethodSource("dropOnDiagramAndPackageAndModelParameters")
    public void testSemanticDropOnDiagram(EReference containmentReference, EClass elementType) {
        EObject elementToDrop = this.createSemanticElement(this.getRootSemanticElement(), containmentReference,
                elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker;
        if (UML.getInterface().isSuperTypeOf(elementType)) {
            graphicalChecker = new CPDInterfaceCreationGraphicalChecker(this::getDiagram, null,
                    CPDMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        } else if (UML.getComment().isSuperTypeOf(elementType) || UML.getConstraint().isSuperTypeOf(elementType)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, null,
                    CPDMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, null,
                    CPDMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        }
        this.semanticDropOnDiagram(this.getIdentityService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnInterfaceParameters")
    public void testSemanticDropOnInterface(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getInterface()), INTERFACE_CONTAINER);
        EObject parentElement = this.findSemanticElementByName(INTERFACE_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        String compartmentName;
        if (UML.getOperation().isSuperTypeOf(elementType)) {
            compartmentName = INTERFACE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(elementType)) {
            compartmentName = INTERFACE_ATTRIBUTE_COMPARTMENT;
        } else if (UML.getReception().isSuperTypeOf(elementType)) {
            compartmentName = INTERFACE_RECEPTION_COMPARTMENT;
        } else {
            compartmentName = null;
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(INTERFACE_CONTAINER, compartmentName),
                CPDMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        this.semanticDropOnContentCompartment(INTERFACE_CONTAINER, compartmentName,
                this.getIdentityService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnDiagramAndPackageAndModelParameters")
    public void testSemanticDropOnModel(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getModel()),
                MODEL_CONTAINER);

        EObject parentElement = this.findSemanticElementByName(MODEL_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType,
                elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker;
        if (UML.getInterface().isSuperTypeOf(elementType)) {
            graphicalChecker = new CPDInterfaceCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER),
                    CPDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else if (UML.getComment().isSuperTypeOf(elementType) || UML.getConstraint().isSuperTypeOf(elementType)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER),
                    CPDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(MODEL_CONTAINER),
                    CPDMappingTypes.getMappingTypeAsSubNode(elementType),
                    this.getCapturedNodes());
        }
        this.semanticDropOnContent(MODEL_CONTAINER, this.getIdentityService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnDiagramAndPackageAndModelParameters")
    public void testSemanticDropOnPackage(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()),
                PACKAGE_CONTAINER);

        EObject parentElement = this.findSemanticElementByName(PACKAGE_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType,
                elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker;
        if (UML.getInterface().isSuperTypeOf(elementType)) {
            graphicalChecker = new CPDInterfaceCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                    CPDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else if (UML.getComment().isSuperTypeOf(elementType) || UML.getConstraint().isSuperTypeOf(elementType)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                    CPDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                    CPDMappingTypes.getMappingTypeAsSubNode(elementType),
                    this.getCapturedNodes());
        }
        this.semanticDropOnContent(PACKAGE_CONTAINER, this.getIdentityService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropAbstractionAndDependencyAndUsageParameters")
    public void testSemanticDropAbstraction(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        final String targetContainerLabel;
        if (UML.getProperty().isSuperTypeOf(targetCreationTool.getToolEClass())) {
            targetContainerLabel = COMPONENT_CONTAINER;
            this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_CONTAINER);
        } else {
            targetContainerLabel = DIAGRAM_LABEL;
        }
        this.edgeSemanticDropOnContainers(sourceCreationTool, DIAGRAM_LABEL, targetCreationTool, targetContainerLabel, new CreationTool(ToolSections.EDGES, UML.getAbstraction()), DIAGRAM_LABEL,
                CPDMappingTypes.getMappingType(UML.getAbstraction()));
    }

    @ParameterizedTest
    @MethodSource("dropComponentRealizationAndGeneralizationAndSubstitutionParameters")
    public void testSemanticDropComponentRealization(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getComponentRealization()),
                CPDMappingTypes.getMappingType(UML.getComponentRealization()));
    }

    @ParameterizedTest
    @MethodSource("dropAbstractionAndDependencyAndUsageParameters")
    public void testSemanticDropDependency(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        final String targetContainerLabel;
        if (UML.getProperty().isSuperTypeOf(targetCreationTool.getToolEClass())) {
            targetContainerLabel = COMPONENT_CONTAINER;
            this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_CONTAINER);
        } else {
            targetContainerLabel = DIAGRAM_LABEL;
        }
        this.edgeSemanticDropOnContainers(sourceCreationTool, DIAGRAM_LABEL, targetCreationTool, targetContainerLabel, new CreationTool(ToolSections.EDGES, UML.getDependency()), DIAGRAM_LABEL,
                CPDMappingTypes.getMappingType(UML.getDependency()));
    }

    @ParameterizedTest
    @MethodSource("dropComponentRealizationAndGeneralizationAndSubstitutionParameters")
    public void testSemanticDropGeneralization(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getGeneralization()), CPDMappingTypes.getMappingType(UML.getGeneralization()));
    }

    @ParameterizedTest
    @MethodSource("dropInterfaceRealizationParameters")
    public void testSemanticDropInterfaceRealization(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getInterfaceRealization()),
                CPDMappingTypes.getMappingType(UML.getInterfaceRealization()));
    }

    @ParameterizedTest
    @MethodSource("dropManifestationParameters")
    public void testSemanticDropManifestation(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        final String sourceContainerLabel;
        if (UML.getProperty().isSuperTypeOf(sourceCreationTool.getToolEClass())) {
            sourceContainerLabel = COMPONENT_CONTAINER;
            this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_CONTAINER);
        } else {
            sourceContainerLabel = DIAGRAM_LABEL;
        }
        this.edgeSemanticDropOnContainers(sourceCreationTool, sourceContainerLabel, targetCreationTool, DIAGRAM_LABEL, new CreationTool(ToolSections.EDGES, UML.getManifestation()), DIAGRAM_LABEL,
                CPDMappingTypes.getMappingType(UML.getManifestation()));
    }

    @ParameterizedTest
    @MethodSource("dropComponentRealizationAndGeneralizationAndSubstitutionParameters")
    public void testSemanticDropSubstitution(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getSubstitution()), CPDMappingTypes.getMappingType(UML.getSubstitution()));
    }

    @ParameterizedTest
    @MethodSource("dropAbstractionAndDependencyAndUsageParameters")
    public void testSemanticDropUsage(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        final String targetContainerLabel;
        if (UML.getProperty().isSuperTypeOf(targetCreationTool.getToolEClass())) {
            targetContainerLabel = COMPONENT_CONTAINER;
            this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_CONTAINER);
        } else {
            targetContainerLabel = DIAGRAM_LABEL;
        }
        this.edgeSemanticDropOnContainers(sourceCreationTool, DIAGRAM_LABEL, targetCreationTool, targetContainerLabel, new CreationTool(ToolSections.EDGES, UML.getUsage()), DIAGRAM_LABEL,
                CPDMappingTypes.getMappingType(UML.getUsage()));
    }

}

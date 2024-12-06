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
package org.eclipse.papyrus.web.tools.usecase;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.UCDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.test.NodeCreationTest;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDCreationTool;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDMappingTypes;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDToolSections;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node creation tools inside graphical parents in the Use Case Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class UCDSubNodeCreationTest extends NodeCreationTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    private static final String PACKAGE_NAME = "PackageContainer";

    private static final String ACTIVITY_NAME = "ActivityContainer";

    private static final String CLASS_NAME = "ClassContainer";

    private static final String COMPONENT_NAME = "ComponentContainer";

    private static final String INTERACTION_NAME = "InteractionContainer";

    private static final String STATE_MACHINE_NAME = "StateMachineContainer";

    public UCDSubNodeCreationTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> packageChildrenParameters() {
        return Stream.of(//
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()), UML.getActivity(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getActor()), UML.getActor(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()), UML.getClass_(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()), //
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()), UML.getComponent(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()), UML.getInteraction(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()), UML.getStateMachine(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()), UML.getUseCase(), PACKAGED_ELEMENT) //
        );
    }

    private static Stream<Arguments> subjectChildrenParameters() {
        return Stream.of(//
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()), UML.getUseCase(), UML.getClassifier_OwnedUseCase()) //
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()), PACKAGE_NAME);
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()), ACTIVITY_NAME);
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()), CLASS_NAME);
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()), COMPONENT_NAME);
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()), INTERACTION_NAME);
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()), STATE_MACHINE_NAME);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in Package")
    @MethodSource("packageChildrenParameters")
    public void testCreateNodeInDiagram(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        String mappingType = UCDMappingTypes.getMappingType(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = switch (expectedType.getName()) {
            case "Activity", "Class", "Component", "Interaction", "Package", "StateMachine" -> new HolderCreationGraphicalChecker(this::getDiagram,
                    () -> this.findGraphicalElementContentByLabel(PACKAGE_NAME), mappingType,
                    this.getCapturedNodes());
            default -> new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_NAME), mappingType,
                    this.getCapturedNodes());
        };
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(PACKAGE_NAME), containmentReference);
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in Activity")
    @MethodSource("subjectChildrenParameters")
    public void testCreateNodeInActivity(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testCreateNodeInSubject(nodeCreationTool, expectedType, containmentReference, ACTIVITY_NAME);
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in Class")
    @MethodSource("subjectChildrenParameters")
    public void testCreateNodeInClass(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testCreateNodeInSubject(nodeCreationTool, expectedType, containmentReference, CLASS_NAME);
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in Component")
    @MethodSource("subjectChildrenParameters")
    public void testCreateNodeInComponent(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testCreateNodeInSubject(nodeCreationTool, expectedType, containmentReference, COMPONENT_NAME);
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in Interaction")
    @MethodSource("subjectChildrenParameters")
    public void testCreateNodeInInteraction(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testCreateNodeInSubject(nodeCreationTool, expectedType, containmentReference, INTERACTION_NAME);
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in StateMachine")
    @MethodSource("subjectChildrenParameters")
    public void testCreateNodeInStateMachine(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testCreateNodeInSubject(nodeCreationTool, expectedType, containmentReference, STATE_MACHINE_NAME);
    }

    private void testCreateNodeInSubject(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference, String containerName) {
        String mappingType = UCDMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(containerName), mappingType,
                this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(containerName), containmentReference);
        this.createSubNode(containerName, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

}

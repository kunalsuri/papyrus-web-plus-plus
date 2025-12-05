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
package org.eclipse.papyrus.web.tools.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.UCDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.checker.NodeGraphicalDnDGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.test.GraphicalDropTest;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDCreationTool;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDMappingTypes;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDToolSections;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test graphical drop tools in the Use Case Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class UCDGraphicalDropTest extends GraphicalDropTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    private static final String PACKAGE_NAME = "PackageContainer";

    private static final String ACTIVITY_NAME = "ActivityContainer";

    private static final String CLASS_NAME = "ClassContainer";

    private static final String COMPONENT_NAME = "ComponentContainer";

    private static final String INTERACTION_NAME = "InteractionContainer";

    private static final String STATE_MACHINE_NAME = "StateMachineContainer";

    public UCDGraphicalDropTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> packageAndDiagramDropParameters() {
        return Stream.of(//
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()), UML.getActivity(), PACKAGED_ELEMENT),
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getActor()), UML.getActor(), PACKAGED_ELEMENT),
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()), UML.getClass_(), PACKAGED_ELEMENT),
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()), UML.getComponent(), PACKAGED_ELEMENT),
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()),
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()), UML.getInteraction(), PACKAGED_ELEMENT),
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT),
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()), UML.getStateMachine(), PACKAGED_ELEMENT),
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()), UML.getUseCase(), PACKAGED_ELEMENT));
    }

    private static Stream<Arguments> subjectDropParameters() {
        return Stream.of(//
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

    @ParameterizedTest
    @MethodSource("packageAndDiagramDropParameters")
    public void testDropOnDiagram(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        Node containerNode = (Node) this.findGraphicalElementContentByLabel(PACKAGE_NAME);
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(containerNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, null,
                UCDMappingTypes.getMappingType(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                this::getRootSemanticElement, containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                () -> this.findSemanticElementByName(PACKAGE_NAME), UML.getPackage_PackagedElement());

        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnDiagram(nodesToDrop, checker);

    }

    @ParameterizedTest
    @MethodSource("packageAndDiagramDropParameters")
    public void testDropOnPackage(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_NAME),
                UCDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(PACKAGE_NAME), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodesToDrop, PACKAGE_NAME, checker);
    }

    @ParameterizedTest
    @MethodSource("subjectDropParameters")
    public void testDropNodeOnActivity(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testDropNodeOnSubject(nodeCreationTool, expectedType, containmentReference, ACTIVITY_NAME);
    }

    @ParameterizedTest
    @MethodSource("subjectDropParameters")
    public void testDropNodeOnClass(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testDropNodeOnSubject(nodeCreationTool, expectedType, containmentReference, CLASS_NAME);
    }

    @ParameterizedTest
    @MethodSource("subjectDropParameters")
    public void testDropNodeOnComponent(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testDropNodeOnSubject(nodeCreationTool, expectedType, containmentReference, COMPONENT_NAME);
    }

    @ParameterizedTest
    @MethodSource("subjectDropParameters")
    public void testDropNodeOnInteraction(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testDropNodeOnSubject(nodeCreationTool, expectedType, containmentReference, INTERACTION_NAME);
    }

    @ParameterizedTest
    @MethodSource("subjectDropParameters")
    public void testDropNodeOnStateMachine(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.testDropNodeOnSubject(nodeCreationTool, expectedType, containmentReference, STATE_MACHINE_NAME);
    }

    private void testDropNodeOnSubject(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference, String containerName) {
        List<Node> nodesToDrop = new ArrayList<>();
        nodesToDrop.add(this.createNodeWithLabel(this.representationId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX));

        NodeGraphicalDnDGraphicalChecker graphicalCreationChecker = new NodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(containerName),
                UCDMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(containerName), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext,
                this::getRootSemanticElement, PACKAGED_ELEMENT);

        CombinedChecker checker = new CombinedChecker(graphicalCreationChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodesToDrop, containerName, checker);
    }

}

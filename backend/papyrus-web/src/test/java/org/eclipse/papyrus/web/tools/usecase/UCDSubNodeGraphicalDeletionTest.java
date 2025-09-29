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
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.HolderDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeGraphicalDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.test.NodeDeletionTest;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDCreationTool;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDToolSections;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests graphical deletion node tool inside graphical parents in the Use Case Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class UCDSubNodeGraphicalDeletionTest extends NodeDeletionTest {

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    private static final String PACKAGE_SUB_NODE_SUFFIX = "_In_Package";

    private static final String CLASS_CONTAINER = "ClassContainer";

    private static final String CLASS_SUB_NODE_SUFFIX = "_In_Class";

    public UCDSubNodeGraphicalDeletionTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> packageParameters() {
        return Stream.of(//
                Arguments.of(UML.getActor(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(UML.getUseCase(), PACKAGED_ELEMENT)//
        );
    }

    private static Stream<Arguments> packageHolderParameters() {
        return Stream.of(//
                Arguments.of(UML.getActivity(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getClass_(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getComponent(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getInteraction(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getPackage(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getStateMachine(), PACKAGED_ELEMENT) //
        );
    }

    private static Stream<Arguments> classParameters() {
        return Stream.of(//
                Arguments.of(UML.getUseCase(), UML.getClassifier_UseCase())//
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
        Node packageContentContainer = (Node) this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER);
        this.createPackageSubNodes(packageContentContainer, PACKAGE_SUB_NODE_SUFFIX);
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()), CLASS_CONTAINER);
        Node classContentContainer = (Node) this.findGraphicalElementContentByLabel(CLASS_CONTAINER);
        this.createClassSubNodes(classContentContainer, CLASS_SUB_NODE_SUFFIX);
    }

    private void createPackageSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()), UML.getActivity().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.NODES, UML.getActor()), UML.getActor().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()), UML.getClass_().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()), UML.getComponent().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()), UML.getInteraction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()), UML.getPackage().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()), UML.getStateMachine().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()), UML.getUseCase().getName() + suffix);
    }

    private void createClassSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()), UML.getUseCase().getName() + suffix);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("packageParameters")
    public void testDeleteGraphicalNodeInPackage(EClass elementType, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        NodeGraphicalDeletionSemanticChecker semanticChecker = new NodeGraphicalDeletionSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), containmentReference);
        this.deleteGraphicalNode(elementType.getName() + PACKAGE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("packageHolderParameters")
    public void testDeleteGraphicalHolderNodeInPackage(EClass elementType, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER));
        NodeGraphicalDeletionSemanticChecker semanticChecker = new NodeGraphicalDeletionSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext,
                () -> this.findSemanticElementByName(PACKAGE_CONTAINER), containmentReference);
        this.deleteGraphicalNode(elementType.getName() + PACKAGE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("classParameters")
    public void testDeleteGraphicalNodeInClass(EClass elementType, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(CLASS_CONTAINER));
        NodeGraphicalDeletionSemanticChecker semanticChecker = new NodeGraphicalDeletionSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext,
                () -> this.findSemanticElementByName(CLASS_CONTAINER), containmentReference);
        this.deleteGraphicalNode(elementType.getName() + CLASS_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

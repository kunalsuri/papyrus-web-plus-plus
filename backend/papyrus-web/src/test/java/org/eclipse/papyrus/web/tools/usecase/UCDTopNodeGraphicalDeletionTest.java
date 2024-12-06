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

import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.UCDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.HolderDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeGraphicalDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.test.NodeDeletionTest;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDCreationTool;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests graphical deletion node tool at the root of the diagram in the Use Case Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class UCDTopNodeGraphicalDeletionTest extends NodeDeletionTest {

    private static final String ACTIVITY1 = "Activity1";

    private static final String ACTOR1 = "Actor1";

    private static final String CLASS1 = "Class1";

    private static final String COMPONENT1 = "Component1";

    private static final String CONSTRAINT1 = "Constraint1";

    private static final String INTERACTION1 = "Interaction1";

    private static final String PACKAGE1 = "Package1";

    private static final String STATE_MACHINE1 = "StateMachine1";

    private static final String USE_CASE1 = "UseCase1";

    public UCDTopNodeGraphicalDeletionTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(ACTOR1, UML.getPackage_PackagedElement()), //
                Arguments.of(CONSTRAINT1, UML.getNamespace_OwnedRule()), //
                Arguments.of(USE_CASE1, UML.getPackage_PackagedElement())//
        );
    }

    private static Stream<Arguments> holderParameterProvider() {
        return Stream.of(//
                Arguments.of(ACTIVITY1, UML.getPackage_PackagedElement()), //
                Arguments.of(CLASS1, UML.getPackage_PackagedElement()), //
                Arguments.of(COMPONENT1, UML.getPackage_PackagedElement()), //
                Arguments.of(INTERACTION1, UML.getPackage_PackagedElement()), //
                Arguments.of(PACKAGE1, UML.getPackage_PackagedElement()), //
                Arguments.of(STATE_MACHINE1, UML.getPackage_PackagedElement()) //
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getActor()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getConstraint()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testDeleteGraphicalNode(String elementName, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, null);

        NodeGraphicalDeletionSemanticChecker semanticChecker = new NodeGraphicalDeletionSemanticChecker(this.getObjectService(), this::getEditingContext, this::getRootSemanticElement,
                containmentReference);
        this.deleteGraphicalNode(elementName, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("holderParameterProvider")
    public void testDeleteGraphicalNodeWithHolder(String elementName, EReference containmentReference) {
        HolderDeletionGraphicalChecker graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, null);

        NodeGraphicalDeletionSemanticChecker semanticChecker = new NodeGraphicalDeletionSemanticChecker(this.getObjectService(), this::getEditingContext, this::getRootSemanticElement,
                containmentReference);
        this.deleteGraphicalNode(elementName, new CombinedChecker(graphicalChecker, semanticChecker));
    }

}

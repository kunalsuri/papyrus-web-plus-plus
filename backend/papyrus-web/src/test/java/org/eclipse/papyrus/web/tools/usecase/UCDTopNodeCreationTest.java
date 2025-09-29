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
 * Tests node creation tools at the root of the diagram in the Use Case Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class UCDTopNodeCreationTest extends NodeCreationTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    public UCDTopNodeCreationTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getActor()), UML.getActor(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()), UML.getUseCase(), PACKAGED_ELEMENT) //
        );
    }

    private static Stream<Arguments> holderParameterProvider() {
        return Stream.of(//
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()), UML.getActivity(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()), UML.getClass_(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()), UML.getComponent(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()), UML.getInteraction(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT), //
                Arguments.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()), UML.getStateMachine(), PACKAGED_ELEMENT) //
        );
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

    @ParameterizedTest(name = "[{index}] Create node {1} on diagram")
    @MethodSource("parameterProvider")
    public void testCreateNode(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = UCDMappingTypes.getMappingType(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType, this::getRootSemanticElement,
                expectedContainmentReference);
        this.createTopNode(nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create node {1} on diagram")
    @MethodSource("holderParameterProvider")
    public void testCreateHolderNode(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = UCDMappingTypes.getMappingType(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType, this::getRootSemanticElement,
                expectedContainmentReference);
        this.createTopNode(nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

}

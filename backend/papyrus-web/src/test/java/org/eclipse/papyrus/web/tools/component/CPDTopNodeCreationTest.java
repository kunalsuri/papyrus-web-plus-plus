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
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.component.checker.CPDInterfaceCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.component.utils.CPDMappingTypes;
import org.eclipse.papyrus.web.tools.test.NodeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node creation tools at the root of the diagram in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDTopNodeCreationTest extends NodeCreationTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    public CPDTopNodeCreationTest() {
        super(DEFAULT_DOCUMENT, CPDDiagramDescriptionBuilder.CPD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComponent()), UML.getComponent(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getInterface()), UML.getInterface(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getModel()), UML.getModel(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT));
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

    @ParameterizedTest(name = "[{index}] Create Node {1} on diagram")
    @MethodSource("parameterProvider")
    public void testCreateNode(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = CPDMappingTypes.getMappingType(expectedType);
        NodeCreationGraphicalChecker graphicalChecker;

        if (UML.getInterface().isSuperTypeOf(expectedType)) {
            graphicalChecker = new CPDInterfaceCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        } else if (UML.getComment().isSuperTypeOf(expectedType) || UML.getConstraint().isSuperTypeOf(expectedType)) {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        } else {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        }
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType, this::getRootSemanticElement,
                expectedContainmentReference);
        this.createTopNode(nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

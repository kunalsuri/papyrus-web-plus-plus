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
package org.eclipse.papyrus.web.tools.communication;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.CODDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.communication.utils.CODMappingTypes;
import org.eclipse.papyrus.web.tools.test.NodeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node creation tools on the main node of the diagram in the Communication Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CODSubNodeCreationTest extends NodeCreationTest {

    private static final String ROOT_INTERACTION = "rootInteraction";

    public CODSubNodeCreationTest() {
        super(DEFAULT_DOCUMENT, CODDiagramDescriptionBuilder.COD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> interactionParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getLifeline()), UML.getLifeline(), UML.getInteraction_Lifeline()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDurationObservation()), UML.getDurationObservation(), UML.getPackage_PackagedElement()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getTimeObservation()), UML.getTimeObservation(), UML.getPackage_PackagedElement()));
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_INTERACTION, UML.getInteraction());
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} in Interaction")
    @MethodSource("interactionParameters")
    public void testCreateNode(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = CODMappingTypes.getMappingType(expectedType);

        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(ROOT_INTERACTION), mappingType,
                this.getCapturedNodes());

        NodeCreationSemanticChecker semanticChecker;
        if (UML.getDurationObservation().isSuperTypeOf(expectedType) || UML.getTimeObservation().isSuperTypeOf(expectedType)) {
            semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType, () -> this.getRootSemanticElement(), expectedContainmentReference);
        } else {
            semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType, () -> this.findSemanticElementByName(ROOT_INTERACTION),
                    expectedContainmentReference);
        }
        this.createSubNode(ROOT_INTERACTION, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

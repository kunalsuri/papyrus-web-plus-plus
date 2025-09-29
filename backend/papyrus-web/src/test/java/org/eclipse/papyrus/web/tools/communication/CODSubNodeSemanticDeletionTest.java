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
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
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
 * Tests node semantic deletion tools at the main node of the diagram in the Communication Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CODSubNodeSemanticDeletionTest extends NodeDeletionTest {

    private static final String ROOT_INTERACTION = "rootInteraction";

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    private static final EReference LIFELINES = UML.getInteraction_Lifeline();

    public CODSubNodeSemanticDeletionTest() {
        super(DEFAULT_DOCUMENT, CODDiagramDescriptionBuilder.COD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> interactionParameters() {
        return Stream.of(//
                Arguments.of(UML.getLifeline(), LIFELINES), //
                Arguments.of(UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(UML.getDurationObservation(), PACKAGED_ELEMENT), //
                Arguments.of(UML.getTimeObservation(), PACKAGED_ELEMENT));
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_INTERACTION, UML.getInteraction());
        Node rootContent = (Node) this.findGraphicalElementContentByLabel(ROOT_INTERACTION);
        String interactionId = rootContent.getId();
        this.createNodeWithLabel(interactionId, new CreationTool(ToolSections.NODES, UML.getLifeline()), UML.getLifeline().getName());
        this.createNodeWithLabel(interactionId, new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName());
        this.createNodeWithLabel(interactionId, new CreationTool(ToolSections.NODES, UML.getDurationObservation()), UML.getDurationObservation().getName());
        this.createNodeWithLabel(interactionId, new CreationTool(ToolSections.NODES, UML.getTimeObservation()), UML.getTimeObservation().getName());
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("interactionParameters")
    public void testDeleteSemanticNodeInInteraction(EClass elementType, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(ROOT_INTERACTION));
        NodeSemanticDeletionSemanticChecker semanticChecker;
        if (UML.getDurationObservation().isSuperTypeOf(elementType) || UML.getTimeObservation().isSuperTypeOf(elementType)) {
            semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectSearchService(),
                    this::getEditingContext, () -> this.getRootSemanticElement(), containmentReference);
        } else {
            semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectSearchService(),
                    this::getEditingContext, () -> this.findSemanticElementByName(ROOT_INTERACTION),
                    containmentReference);
        }
        this.deleteSemanticNode(elementType.getName(), new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

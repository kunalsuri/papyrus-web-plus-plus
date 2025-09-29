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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.CODDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EdgeDeletionTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edge creation tools in the Communication Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CODSubNodeEdgeSemanticDeletionTest extends EdgeDeletionTest {

    private static final String ROOT_INTERACTION = "rootInteraction";

    private static final String LIFELINE_SOURCE = "LifelineSource";

    private static final String LIFELINE_TARGET = "LifelineTarget";

    public CODSubNodeEdgeSemanticDeletionTest() {
        super(DEFAULT_DOCUMENT, CODDiagramDescriptionBuilder.COD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> messageParameters() {
        List<String> sources = List.of(LIFELINE_SOURCE);
        List<String> targets = List.of(LIFELINE_TARGET);
        return cartesianProduct(sources, targets);
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_INTERACTION, UML.getInteraction());
        Node rootContent = (Node) this.findGraphicalElementContentByLabel(ROOT_INTERACTION);
        this.createSourceAndTargetNodes(rootContent.getId(), new CreationTool(ToolSections.NODES, UML.getLifeline()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("messageParameters")
    public void testDeleteMessage(String sourceElementLabel, String targetElementLabel) {
        this.testDeleteEdge(sourceElementLabel, targetElementLabel, UML.getMessage());
    }

    private void testDeleteEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, edgeType));
        Edge edge = this.getDiagram().getEdges().get(0);
        this.testDeleteEdge(edge, ROOT_INTERACTION, UML.getInteraction_Message());
    }

    private void testDeleteEdge(Edge edge, String oldOwnerLabel, EReference oldContainmentReference) {
        final Supplier<EObject> oldOwnerSupplier;
        if (oldOwnerLabel == null) {
            oldOwnerSupplier = this::getRootSemanticElement;
        } else {
            oldOwnerSupplier = () -> this.findSemanticElementByName(oldOwnerLabel);
        }
        DeletionGraphicalChecker graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, null);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(
                this.getObjectSearchService(), this::getEditingContext, oldOwnerSupplier, oldContainmentReference);
        this.deleteSemanticEdge(edge, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.web.tools.profile;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EdgeDeletionTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Edge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edge creation tools in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDEdgeSemanticDeletionTest extends EdgeDeletionTest {

    private static final String CLASS_SOURCE = "ClassSource";

    private static final String CLASS_TARGET = "ClassTarget";

    private static final String DATA_TYPE_SOURCE = "DataTypeSource";

    private static final String DATA_TYPE_TARGET = "DataTypeTarget";

    private static final String ENUMERATION_SOURCE = "EnumerationSource";

    private static final String ENUMERATION_TARGET = "EnumerationTarget";

    private static final String PRIMITIVE_TYPE_SOURCE = "PrimitiveTypeSource";

    private static final String PRIMITIVE_TYPE_TARGET = "PrimitiveTypeTarget";

    private static final String STEREOTYPE_SOURCE = "StereotypeSource";

    private static final String STEREOTYPE_TARGET = "StereotypeTarget";

    public PRDEdgeSemanticDeletionTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> associationAndGeneralizationParameters() {
        List<String> sources = List.of(CLASS_SOURCE, DATA_TYPE_SOURCE, ENUMERATION_SOURCE, PRIMITIVE_TYPE_SOURCE, STEREOTYPE_SOURCE);
        List<String> targets = List.of(CLASS_TARGET, DATA_TYPE_TARGET, ENUMERATION_TARGET, PRIMITIVE_TYPE_TARGET, STEREOTYPE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> extensionParameters() {
        List<String> sources = List.of(STEREOTYPE_SOURCE);
        List<String> targets = List.of(CLASS_TARGET, STEREOTYPE_TARGET);
        return cartesianProduct(sources, targets);
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        // No need to create constraint, package, and profile: these elements aren't sources or targets of PRD edges.
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getClass_()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getDataType()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getEnumeration()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getPrimitiveType()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getStereotype()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testDeleteAssociation(String sourceElementLabel, String targetElementLabel) {
        this.testDeleteEdge(sourceElementLabel, targetElementLabel, UML.getAssociation());

    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testDeleteGeneralization(String sourceElementLabel, String targetElementLabel) {
        this.testDeleteEdge(sourceElementLabel, targetElementLabel, UML.getGeneralization());

    }

    @ParameterizedTest
    @MethodSource("extensionParameters")
    public void testDeleteExtension(String sourceElementLabel, String targetElementLabel) {
        this.testDeleteEdge(sourceElementLabel, targetElementLabel, UML.getExtension());
    }

    private void testDeleteEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, edgeType));
        Edge edge = this.getDiagram().getEdges().get(0);
        this.testDeleteEdge(edge, null, UML.getPackage_PackagedElement());
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

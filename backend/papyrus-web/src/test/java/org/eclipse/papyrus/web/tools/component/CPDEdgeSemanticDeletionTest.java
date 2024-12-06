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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
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
 * Tests semantic deletion edge tool at the root of the diagram in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDEdgeSemanticDeletionTest extends EdgeDeletionTest {

    private static final String COMPONENT_CONTAINER = "ComponentContainer";

    private static final String COMPONENT_SOURCE = "ComponentSource";

    private static final String COMPONENT_TARGET = "ComponentTarget";

    private static final String CONSTRAINT_SOURCE = "ConstraintSource";

    private static final String CONSTRAINT_TARGET = "ConstraintTarget";

    private static final String INTERFACE_SOURCE = "InterfaceSource";

    private static final String INTERFACE_TARGET = "InterfaceTarget";

    private static final String MODEL_SOURCE = "ModelSource";

    private static final String MODEL_TARGET = "ModelTarget";

    private static final String PACKAGE_SOURCE = "PackageSource";

    private static final String PACKAGE_TARGET = "PackageTarget";

    private static final String PORT_SOURCE = "PortSource";

    private static final String PORT_TARGET = "PortTarget";

    private static final String PROPERTY_SOURCE = "PropertySource";

    private static final String PROPERTY_TARGET = "PropertyTarget";

    public CPDEdgeSemanticDeletionTest() {
        super(DEFAULT_DOCUMENT, CPDDiagramDescriptionBuilder.CPD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> abstractionAndDependencyAndUsageParameters() {
        List<String> sources = List.of(COMPONENT_SOURCE, CONSTRAINT_SOURCE, INTERFACE_SOURCE, MODEL_SOURCE, PACKAGE_SOURCE, PORT_SOURCE, PROPERTY_SOURCE);
        List<String> targets = List.of(COMPONENT_TARGET, CONSTRAINT_TARGET, INTERFACE_TARGET, MODEL_TARGET, PACKAGE_TARGET, PORT_TARGET, PROPERTY_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> componentRealizationParameters() {
        List<String> sources = List.of(COMPONENT_SOURCE, INTERFACE_SOURCE);
        List<String> targets = List.of(COMPONENT_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> generalizationAndSubstitutionParameters() {
        List<String> sources = List.of(COMPONENT_SOURCE, INTERFACE_SOURCE);
        List<String> targets = List.of(COMPONENT_TARGET, INTERFACE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> interfaceRealizationParameters() {
        List<String> sources = List.of(COMPONENT_SOURCE);
        List<String> targets = List.of(INTERFACE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> manifestationParameters() {
        List<String> sources = List.of(COMPONENT_SOURCE, CONSTRAINT_SOURCE, INTERFACE_SOURCE, MODEL_SOURCE, PACKAGE_SOURCE, PROPERTY_SOURCE);
        List<String> targets = List.of(COMPONENT_TARGET, CONSTRAINT_TARGET, INTERFACE_TARGET, MODEL_TARGET, PACKAGE_TARGET);
        return cartesianProduct(sources, targets);
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getComponent()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getConstraint()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getInterface()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getPackage()));
        this.createSourceAndTargetTopNodes(new CreationTool(ToolSections.NODES, UML.getModel()));
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()), COMPONENT_CONTAINER);
        String componentContainerId = this.findGraphicalElementContentByLabel(COMPONENT_CONTAINER).getId();
        this.createSourceAndTargetNodes(componentContainerId, new CreationTool(ToolSections.NODES, UML.getPort()));
        this.createSourceAndTargetNodes(componentContainerId, new CreationTool(ToolSections.NODES, UML.getProperty()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testDeleteAbstraction(String sourceElementLabel, String targetElementLabel) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, UML.getAbstraction()));
        Edge edge = this.getDiagram().getEdges().get(0);
        if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testDeleteEdge(edge, sourceElementLabel, UML.getComponent_PackagedElement());
        } else if (sourceElementLabel.equals(PACKAGE_SOURCE) || sourceElementLabel.equals(MODEL_SOURCE)) {
            this.testDeleteEdge(edge, sourceElementLabel, UML.getPackage_PackagedElement());
        } else {
            this.testDeleteEdge(edge);
        }
    }

    @ParameterizedTest
    @MethodSource("componentRealizationParameters")
    public void testDeleteComponentRealization(String sourceElementLabel, String targetElementLabel) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, UML.getComponentRealization()));
        Edge edge = this.getDiagram().getEdges().get(0);
        this.testDeleteEdge(edge, targetElementLabel, UML.getComponent_Realization());
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testDeleteDependency(String sourceElementLabel, String targetElementLabel) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, UML.getDependency()));
        Edge edge = this.getDiagram().getEdges().get(0);
        if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testDeleteEdge(edge, sourceElementLabel, UML.getComponent_PackagedElement());
        } else if (sourceElementLabel.equals(PACKAGE_SOURCE) || sourceElementLabel.equals(MODEL_SOURCE)) {
            this.testDeleteEdge(edge, sourceElementLabel, UML.getPackage_PackagedElement());
        } else {
            this.testDeleteEdge(edge);
        }
    }

    @ParameterizedTest
    @MethodSource("generalizationAndSubstitutionParameters")
    public void testDeleteGeneralization(String sourceElementLabel, String targetElementLabel) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, UML.getGeneralization()));
        Edge edge = this.getDiagram().getEdges().get(0);
        this.testDeleteEdge(edge, sourceElementLabel, UML.getClassifier_Generalization());
    }

    @ParameterizedTest
    @MethodSource("interfaceRealizationParameters")
    public void testDeleteInterfaceRealization(String sourceElementLabel, String targetElementLabel) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, UML.getInterfaceRealization()));
        Edge edge = this.getDiagram().getEdges().get(0);
        this.testDeleteEdge(edge, sourceElementLabel, UML.getBehavioredClassifier_InterfaceRealization());
    }

    @ParameterizedTest
    @MethodSource("manifestationParameters")
    public void testDeleteManifestation(String sourceElementLabel, String targetElementLabel) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, UML.getManifestation()));
        Edge edge = this.getDiagram().getEdges().get(0);
        if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testDeleteEdge(edge, sourceElementLabel, UML.getComponent_PackagedElement());
        } else if (sourceElementLabel.equals(PACKAGE_SOURCE) || sourceElementLabel.equals(MODEL_SOURCE)) {
            this.testDeleteEdge(edge, sourceElementLabel, UML.getPackage_PackagedElement());
        } else {
            this.testDeleteEdge(edge);
        }
    }

    @ParameterizedTest
    @MethodSource("generalizationAndSubstitutionParameters")
    public void testDeleteSubstitution(String sourceElementLabel, String targetElementLabel) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, UML.getSubstitution()));
        Edge edge = this.getDiagram().getEdges().get(0);
        this.testDeleteEdge(edge, sourceElementLabel, UML.getClassifier_Substitution());
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testDeleteUsage(String sourceElementLabel, String targetElementLabel) {
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, UML.getUsage()));
        Edge edge = this.getDiagram().getEdges().get(0);
        if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testDeleteEdge(edge, sourceElementLabel, UML.getComponent_PackagedElement());
        } else if (sourceElementLabel.equals(PACKAGE_SOURCE) || sourceElementLabel.equals(MODEL_SOURCE)) {
            this.testDeleteEdge(edge, sourceElementLabel, UML.getPackage_PackagedElement());
        } else {
            this.testDeleteEdge(edge);
        }
    }

    private void testDeleteEdge(Edge edge) {
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
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext, oldOwnerSupplier, oldContainmentReference);
        this.deleteSemanticEdge(edge, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

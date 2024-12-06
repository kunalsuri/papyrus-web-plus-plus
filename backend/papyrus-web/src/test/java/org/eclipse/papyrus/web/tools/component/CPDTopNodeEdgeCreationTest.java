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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.component.utils.CPDMappingTypes;
import org.eclipse.papyrus.web.tools.test.EdgeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edge creation tools in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDTopNodeEdgeCreationTest extends EdgeCreationTest {

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

    public CPDTopNodeEdgeCreationTest() {
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
    public void testCreateAbstraction(String sourceElementLabel, String targetElementLabel) {
        if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAbstraction(), sourceElementLabel,
                    UML.getComponent_PackagedElement());
        } else if (sourceElementLabel.equals(PACKAGE_SOURCE) || sourceElementLabel.equals(MODEL_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAbstraction(), sourceElementLabel,
                    UML.getPackage_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAbstraction());
        }
    }

    @ParameterizedTest
    @MethodSource("componentRealizationParameters")
    public void testCreateComponentRealization(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getComponentRealization(), targetElementLabel,
                UML.getComponent_Realization());
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testCreateDependency(String sourceElementLabel, String targetElementLabel) {
        if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDependency(), sourceElementLabel,
                    UML.getComponent_PackagedElement());
        } else if (sourceElementLabel.equals(PACKAGE_SOURCE) || sourceElementLabel.equals(MODEL_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDependency(), sourceElementLabel,
                    UML.getPackage_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDependency());
        }
    }

    @ParameterizedTest
    @MethodSource("generalizationAndSubstitutionParameters")
    public void testCreateGeneralization(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getGeneralization(), sourceElementLabel,
                UML.getClassifier_Generalization());
    }

    @ParameterizedTest
    @MethodSource("interfaceRealizationParameters")
    public void testCreateInterfaceRealization(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getInterfaceRealization(), sourceElementLabel, UML.getBehavioredClassifier_InterfaceRealization());
    }

    @ParameterizedTest
    @MethodSource("manifestationParameters")
    public void testCreateManifestation(String sourceElementLabel, String targetElementLabel) {
        if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getManifestation(), sourceElementLabel,
                    UML.getComponent_PackagedElement());
        } else if (sourceElementLabel.equals(PACKAGE_SOURCE) || sourceElementLabel.equals(MODEL_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getManifestation(), sourceElementLabel,
                    UML.getPackage_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getManifestation());
        }
    }

    @ParameterizedTest
    @MethodSource("generalizationAndSubstitutionParameters")
    public void testCreateSubstitution(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getSubstitution(), sourceElementLabel,
                UML.getClassifier_Substitution());
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndUsageParameters")
    public void testCreateUsage(String sourceElementLabel, String targetElementLabel) {
        if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getUsage(), sourceElementLabel, UML.getComponent_PackagedElement());
        } else if (sourceElementLabel.equals(PACKAGE_SOURCE) || sourceElementLabel.equals(MODEL_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getUsage(), sourceElementLabel, UML.getPackage_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getUsage());
        }
    }

    private void testCreateEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, edgeType, null, UML.getPackage_PackagedElement());
    }

    private void testCreateEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType, String expectedSemanticOwnerName, EReference expectedContainmentReference) {
        Supplier<EObject> expectedSemanticOwnerSupplier;
        if (expectedSemanticOwnerName == null) {
            expectedSemanticOwnerSupplier = () -> this.getRootSemanticElement();
        } else {
            expectedSemanticOwnerSupplier = () -> this.findSemanticElementByName(expectedSemanticOwnerName);
        }
        EdgeCreationGraphicalChecker graphicalChecker = new EdgeCreationGraphicalChecker(this::getDiagram, null, CPDMappingTypes.getMappingType(edgeType), this.getCapturedEdges());
        EdgeCreationSemanticChecker semanticChecker = new EdgeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, edgeType, expectedSemanticOwnerSupplier,
                expectedContainmentReference);
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, edgeType), new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

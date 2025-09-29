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
package org.eclipse.papyrus.web.tools.usecase;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.UCDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EdgeCreationTest;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDCreationTool;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDMappingTypes;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edge creation tools in the Use Case Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class UCDTopNodeEdgeCreationTest extends EdgeCreationTest {

    private static final String ACTIVITY_SOURCE = "ActivitySource";

    private static final String ACTIVITY_TARGET = "ActivityTarget";

    private static final String ACTOR_SOURCE = "ActorSource";

    private static final String ACTOR_TARGET = "ActorTarget";

    private static final String CLASS_SOURCE = "ClassSource";

    private static final String CLASS_TARGET = "ClassTarget";

    private static final String COMPONENT_SOURCE = "ComponentSource";

    private static final String COMPONENT_TARGET = "ComponentTarget";

    private static final String CONSTRAINT_SOURCE = "ConstraintSource";

    private static final String CONSTRAINT_TARGET = "ConstraintTarget";

    private static final String INTERACTION_SOURCE = "InteractionSource";

    private static final String INTERACTION_TARGET = "InteractionTarget";

    private static final String PACKAGE_SOURCE = "PackageSource";

    private static final String PACKAGE_TARGET = "PackageTarget";

    private static final String STATE_MACHINE_SOURCE = "StateMachineSource";

    private static final String STATE_MACHINE_TARGET = "StateMachineTarget";

    private static final String USE_CASE_SOURCE = "UseCaseSource";

    private static final String USE_CASE_TARGET = "UseCaseTarget";

    public UCDTopNodeEdgeCreationTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> abstractionAndDependencyAndRealizationAndUsageParameters() {
        List<String> sources = List.of(ACTOR_SOURCE, PACKAGE_SOURCE, ACTIVITY_SOURCE, CLASS_SOURCE, COMPONENT_SOURCE, CONSTRAINT_SOURCE, INTERACTION_SOURCE, STATE_MACHINE_SOURCE, USE_CASE_SOURCE);
        List<String> targets = List.of(ACTOR_TARGET, PACKAGE_TARGET, ACTIVITY_TARGET, CLASS_TARGET, COMPONENT_TARGET, CONSTRAINT_TARGET, INTERACTION_TARGET, STATE_MACHINE_TARGET, USE_CASE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> associationAndGeneralizationParameters() {
        List<String> sources = List.of(ACTOR_SOURCE, ACTIVITY_SOURCE, CLASS_SOURCE, COMPONENT_SOURCE, INTERACTION_SOURCE, STATE_MACHINE_SOURCE, USE_CASE_SOURCE);
        List<String> targets = List.of(ACTOR_TARGET, ACTIVITY_TARGET, CLASS_TARGET, COMPONENT_TARGET, INTERACTION_TARGET, STATE_MACHINE_TARGET, USE_CASE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> extendAndIncludeParameters() {
        return Stream.of(Arguments.of(USE_CASE_SOURCE, USE_CASE_TARGET));
    }

    private static Stream<Arguments> packageImportAndPackageMergeParameters() {
        return Stream.of(Arguments.of(PACKAGE_SOURCE, PACKAGE_TARGET));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createSourceAndTargetTopNodes(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()));
        this.createSourceAndTargetTopNodes(new UCDCreationTool(UCDToolSections.NODES, UML.getActor()));
        this.createSourceAndTargetTopNodes(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()));
        this.createSourceAndTargetTopNodes(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()));
        this.createSourceAndTargetTopNodes(new UCDCreationTool(UCDToolSections.NODES, UML.getConstraint()));
        this.createSourceAndTargetTopNodes(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()));
        this.createSourceAndTargetTopNodes(new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()));
        this.createSourceAndTargetTopNodes(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()));
        this.createSourceAndTargetTopNodes(new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testCreateAbstraction(String sourceElementLabel, String targetElementLabel) {
        // Abstraction is contained in its source if it is a package or component
        if (sourceElementLabel.equals(PACKAGE_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAbstraction(), sourceElementLabel, UML.getPackage_PackagedElement());
        } else if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAbstraction(), sourceElementLabel, UML.getComponent_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAbstraction());
        }
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testCreateAssociation(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAssociation());
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testCreateDependency(String sourceElementLabel, String targetElementLabel) {
        // Dependency is contained in its source if it is a package or component
        if (sourceElementLabel.equals(PACKAGE_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDependency(), sourceElementLabel, UML.getPackage_PackagedElement());
        } else if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDependency(), sourceElementLabel, UML.getComponent_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDependency());
        }
    }

    @ParameterizedTest
    @MethodSource("extendAndIncludeParameters")
    public void testCreateExtend(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getExtend(), sourceElementLabel, UML.getUseCase_Extend());
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testCreateGeneralization(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getGeneralization(), sourceElementLabel, UML.getClassifier_Generalization());
    }

    @ParameterizedTest
    @MethodSource("extendAndIncludeParameters")
    public void testCreateInclude(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getInclude(), sourceElementLabel, UML.getUseCase_Include());
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testCreateRealization(String sourceElementLabel, String targetElementLabel) {
        // Realization is contained in its source if it is a package or component
        if (sourceElementLabel.equals(PACKAGE_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getRealization(), sourceElementLabel, UML.getPackage_PackagedElement());
        } else if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getRealization(), sourceElementLabel, UML.getComponent_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getRealization());
        }
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testCreateUsage(String sourceElementLabel, String targetElementLabel) {
        // Usage is contained in its source if it is a package or component
        if (sourceElementLabel.equals(PACKAGE_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getUsage(), sourceElementLabel, UML.getPackage_PackagedElement());
        } else if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getUsage(), sourceElementLabel, UML.getComponent_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getUsage());
        }
    }

    @ParameterizedTest
    @MethodSource("packageImportAndPackageMergeParameters")
    public void testCreatePackageImport(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getPackageImport(), sourceElementLabel, UML.getNamespace_PackageImport());
    }

    @ParameterizedTest
    @MethodSource("packageImportAndPackageMergeParameters")
    public void testCreatePackageMerge(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getPackageMerge(), sourceElementLabel, UML.getPackage_PackageMerge());
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
        EdgeCreationGraphicalChecker graphicalChecker = new EdgeCreationGraphicalChecker(this::getDiagram, null, UCDMappingTypes.getMappingType(edgeType), this.getCapturedEdges());
        EdgeCreationSemanticChecker semanticChecker = new EdgeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(),
                this::getEditingContext, edgeType, expectedSemanticOwnerSupplier,
                expectedContainmentReference);
        this.createEdge(sourceElementLabel, targetElementLabel, new UCDCreationTool(UCDToolSections.EDGES, edgeType), new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

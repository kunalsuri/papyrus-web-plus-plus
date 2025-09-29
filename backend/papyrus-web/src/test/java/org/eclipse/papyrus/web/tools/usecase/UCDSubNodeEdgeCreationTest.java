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
import org.eclipse.sirius.components.diagrams.Node;
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
public class UCDSubNodeEdgeCreationTest extends EdgeCreationTest {

    private static final String ACTOR_SOURCE = "ActorSource";

    private static final String ACTOR_TARGET = "ActorTarget";

    private static final String COMPONENT_SOURCE = "ComponentSource";

    private static final String COMPONENT_TARGET = "ComponentTarget";

    private static final String PACKAGE_SOURCE = "PackageSource";

    private static final String PACKAGE_TARGET = "PackageTarget";

    private static final String USE_CASE_SOURCE = "UseCaseSource";

    private static final String USE_CASE_TARGET = "UseCaseTarget";

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private String packageContainerId;

    public UCDSubNodeEdgeCreationTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> abstractionAndDependencyAndRealizationAndUsageParameters() {
        List<String> sources = List.of(ACTOR_SOURCE, PACKAGE_SOURCE, COMPONENT_SOURCE, USE_CASE_SOURCE);
        List<String> targets = List.of(ACTOR_TARGET, PACKAGE_TARGET, COMPONENT_TARGET, USE_CASE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> associationAndGeneralizationParameters() {
        List<String> sources = List.of(ACTOR_SOURCE, COMPONENT_SOURCE, USE_CASE_SOURCE);
        List<String> targets = List.of(ACTOR_TARGET, COMPONENT_TARGET, USE_CASE_TARGET);
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
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
        Node packageContainerNode = (Node) this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER);
        this.packageContainerId = packageContainerNode.getId();
        this.createSourceAndTargetNodes(this.packageContainerId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()));
        this.createSourceAndTargetNodes(this.packageContainerId, new UCDCreationTool(UCDToolSections.NODES, UML.getActor()));
        // Only keep components for subjects to limit the number of test cases.
        this.createSourceAndTargetNodes(this.packageContainerId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()));
        this.createSourceAndTargetNodes(this.packageContainerId, new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()));
        this.createSourceAndTargetNodes(this.packageContainerId, new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()));
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
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAbstraction(), PACKAGE_CONTAINER, UML.getPackage_PackagedElement());
        }
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testCreateAssociation(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAssociation(), null, UML.getPackage_PackagedElement());
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
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getDependency(), PACKAGE_CONTAINER, UML.getPackage_PackagedElement());
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
    public void testCreateRealizationTest(String sourceElementLabel, String targetElementLabel) {
        // Realization is contained in its source if it is a package or component
        if (sourceElementLabel.equals(PACKAGE_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getRealization(), sourceElementLabel, UML.getPackage_PackagedElement());
        } else if (sourceElementLabel.equals(COMPONENT_SOURCE)) {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getRealization(), sourceElementLabel, UML.getComponent_PackagedElement());
        } else {
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getRealization(), PACKAGE_CONTAINER, UML.getPackage_PackagedElement());
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
            this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getUsage(), PACKAGE_CONTAINER, UML.getPackage_PackagedElement());
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

    private void testCreateEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType, String expectedSemanticOwnerName, EReference expectedContainmentReference) {
        Supplier<EObject> expectedSemanticOwnerSupplier;
        if (expectedSemanticOwnerName == null) {
            expectedSemanticOwnerSupplier = () -> this.getRootSemanticElement();
        } else {
            expectedSemanticOwnerSupplier = () -> this.findSemanticElementByName(expectedSemanticOwnerName);
        }
        EdgeCreationGraphicalChecker graphicalChecker = new EdgeCreationGraphicalChecker(this::getDiagram, null, UCDMappingTypes.getMappingType(edgeType), this.getCapturedEdges());
        EdgeCreationSemanticChecker semanticChecker = new EdgeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, edgeType, expectedSemanticOwnerSupplier,
                expectedContainmentReference);
        this.createEdge(sourceElementLabel, targetElementLabel, new UCDCreationTool(UCDToolSections.EDGES, edgeType), new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

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
import org.eclipse.papyrus.web.application.representations.uml.UCDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.Checker;
import org.eclipse.papyrus.web.tools.checker.EdgeSourceGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.ReconnectEdgeSourceTest;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDCreationTool;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests reconnect edge source tools in the Use Case Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class UCDReconnectEdgeSourceTest extends ReconnectEdgeSourceTest {

    private static final String ACTIVITY_EDGE_SOURCE = "Activity" + OLD_SOURCE;

    private static final String ACTIVITY_EDGE_TARGET = "Activity" + TARGET;

    private static final String PACKAGE_EDGE_SOURCE = "Package" + OLD_SOURCE;

    private static final String PACKAGE_EDGE_TARGET = "Package" + TARGET;

    private static final String USE_CASE_EDGE_SOURCE = "UseCase" + OLD_SOURCE;

    private static final String USE_CASE_EDGE_TARGET = "UseCase" + TARGET;

    public UCDReconnectEdgeSourceTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> abstractionAndDependencyAndRealizationAndUsageParameters() {
        return Stream.of(//
                Arguments.of(UML.getActivity()), //
                Arguments.of(UML.getActor()), //
                Arguments.of(UML.getClass_()), //
                Arguments.of(UML.getComponent()), //
                Arguments.of(UML.getConstraint()), //
                Arguments.of(UML.getInteraction()), //
                Arguments.of(UML.getPackage()), //
                Arguments.of(UML.getStateMachine()), //
                Arguments.of(UML.getUseCase()) //
        );
    }

    private static Stream<Arguments> associationAndGeneralizationParameters() {
        return Stream.of(//
                Arguments.of(UML.getActivity()), //
                Arguments.of(UML.getActor()), //
                Arguments.of(UML.getClass_()), //
                Arguments.of(UML.getComponent()), //
                Arguments.of(UML.getInteraction()), //
                Arguments.of(UML.getStateMachine()), //
                Arguments.of(UML.getUseCase()) //
        );
    }

    private static Stream<Arguments> extendAndIncludeParameters() {
        return Stream.of(Arguments.of(UML.getUseCase()));
    }

    private static Stream<Arguments> packageImportAndPackageMergeParameters() {
        return Stream.of(Arguments.of(UML.getPackage()));
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
    public void testReconnectAbstractionSource(EClass newSourceType) {
        String abstractionId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getAbstraction()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(abstractionId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testReconnectAssociationSource(EClass newSourceType) {
        String associationId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getAssociation()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(associationId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testReconnectDependencySource(EClass newSourceType) {
        String dependencyId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getDependency()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(dependencyId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("extendAndIncludeParameters")
    public void testReconnectExtendSource(EClass newSourceType) {
        String extendId = this.createEdge(USE_CASE_EDGE_SOURCE, USE_CASE_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getExtend()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(extendId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testReconnectGeneralizationSource(EClass newSourceType) {
        String generalizationId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getGeneralization()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(generalizationId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("extendAndIncludeParameters")
    public void testReconnectIncludeSource(EClass newSourceType) {
        String includeId = this.createEdge(USE_CASE_EDGE_SOURCE, USE_CASE_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getInclude()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(includeId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("packageImportAndPackageMergeParameters")
    public void testReconnectPackageImportSource(EClass newSourceType) {
        String packageImportId = this.createEdge(PACKAGE_EDGE_SOURCE, PACKAGE_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getPackageImport()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(packageImportId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("packageImportAndPackageMergeParameters")
    public void testReconnectPackageMergeSource(EClass newSourceType) {
        String packageMergeId = this.createEdge(PACKAGE_EDGE_SOURCE, PACKAGE_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getPackageMerge()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(packageMergeId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testReconnectRealizationSource(EClass newSourceType) {
        String realizationId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getRealization()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(realizationId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testReconnectUsageSource(EClass newSourceType) {
        String usageId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getUsage()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(usageId, newSourceLabel, graphicalChecker);
    }
}

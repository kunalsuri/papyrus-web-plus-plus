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
import org.eclipse.papyrus.web.tools.checker.EdgeTargetGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.ReconnectEdgeTargetTest;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDCreationTool;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests reconnect edge target tools in the Use Case Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class UCDReconnectEdgeTargetTest extends ReconnectEdgeTargetTest {

    private static final String ACTIVITY_EDGE_SOURCE = "Activity" + SOURCE;

    private static final String ACTIVITY_EDGE_TARGET = "Activity" + OLD_TARGET;

    private static final String PACKAGE_EDGE_SOURCE = "Package" + SOURCE;

    private static final String PACKAGE_EDGE_TARGET = "Package" + OLD_TARGET;

    private static final String USE_CASE_EDGE_SOURCE = "UseCase" + SOURCE;

    private static final String USE_CASE_EDGE_TARGET = "UseCase" + OLD_TARGET;

    public UCDReconnectEdgeTargetTest() {
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
    public void testReconnectAbstractionTarget(EClass newTargetType) {
        String abstractionId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getAbstraction()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(abstractionId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testReconnectAssociationTarget(EClass newTargetType) {
        String associationId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getAssociation()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(associationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testReconnectDependencyTarget(EClass newTargetType) {
        String dependencyId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getDependency()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(dependencyId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("extendAndIncludeParameters")
    public void testReconnectExtendTarget(EClass newTargetType) {
        String extendId = this.createEdge(USE_CASE_EDGE_SOURCE, USE_CASE_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getExtend()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(extendId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testReconnectGeneralizationTarget(EClass newTargetType) {
        String generalizationId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getGeneralization()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(generalizationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("extendAndIncludeParameters")
    public void testReconnectIncludeTarget(EClass newTargetType) {
        String includeId = this.createEdge(USE_CASE_EDGE_SOURCE, USE_CASE_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getInclude()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(includeId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("packageImportAndPackageMergeParameters")
    public void testReconnectPackageImportTarget(EClass newTargetType) {
        String packageImportId = this.createEdge(PACKAGE_EDGE_SOURCE, PACKAGE_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getPackageImport()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(packageImportId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("packageImportAndPackageMergeParameters")
    public void testReconnectPackageMergeTarget(EClass newTargetType) {
        String packageMergeId = this.createEdge(PACKAGE_EDGE_SOURCE, PACKAGE_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getPackageMerge()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(packageMergeId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testReconnectRealizationTarget(EClass newTargetType) {
        String realizationId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getRealization()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(realizationId, newTargetLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("abstractionAndDependencyAndRealizationAndUsageParameters")
    public void testReconnectUsageTarget(EClass newTargetType) {
        String usageId = this.createEdge(ACTIVITY_EDGE_SOURCE, ACTIVITY_EDGE_TARGET, new UCDCreationTool(UCDToolSections.EDGES, UML.getUsage()));
        String newTargetLabel = newTargetType.getName() + NEW_TARGET;
        Checker graphicalChecker = new EdgeTargetGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newTargetLabel));
        this.reconnectEdgeTarget(usageId, newTargetLabel, graphicalChecker);
    }
}

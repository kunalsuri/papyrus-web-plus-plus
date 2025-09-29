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
package org.eclipse.papyrus.web.tools.activity;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.application.representations.uml.ADDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.activity.utils.ADCreationTool;
import org.eclipse.papyrus.web.tools.activity.utils.ADToolSections;
import org.eclipse.papyrus.web.tools.checker.Checker;
import org.eclipse.papyrus.web.tools.checker.EdgeSourceGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.ReconnectEdgeSourceTest;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.uml2.uml.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests reconnect edge source tools in the Activity Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ADReconnectEdgeSourceTest extends ReconnectEdgeSourceTest {

    private static final String ROOT_ACTIVITY = "rootActivity";

    private static final String FORK_NODE_SOURCE = "ForkNode" + OLD_SOURCE;

    private static final String JOIN_NODE_TARGET = "JoinNode" + TARGET;

    private static final String EXPANSION_REGION_CONTAINER = "ExpansionRegionContainer";

    private static final String OPAQUE_ACTION_CONTAINER = "OpaqueActionContainer";

    public ADReconnectEdgeSourceTest() {
        super(DEFAULT_DOCUMENT, ADDiagramDescriptionBuilder.AD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> controlFlowParameters() {
        return Stream.of(//
                Arguments.of(UML.getActivityParameterNode()), //
                Arguments.of(UML.getCallBehaviorAction()), //
                Arguments.of(UML.getDecisionNode()), //
                Arguments.of(UML.getForkNode()), //
                Arguments.of(UML.getExpansionNode()), //
                Arguments.of(UML.getInitialNode()), //
                Arguments.of(UML.getInputPin()), //
                Arguments.of(UML.getJoinNode()), //
                Arguments.of(UML.getMergeNode()), //
                Arguments.of(UML.getOpaqueAction()), //
                Arguments.of(UML.getOutputPin()), //
                Arguments.of(UML.getStructuredActivityNode()) //
        );
    }

    private static Stream<Arguments> objectFlowParameters() {
        return Stream.of(//
                Arguments.of(UML.getActivityParameterNode()), //
                Arguments.of(UML.getDecisionNode()), //
                Arguments.of(UML.getForkNode()), //
                Arguments.of(UML.getJoinNode()), //
                Arguments.of(UML.getMergeNode()), //
                Arguments.of(UML.getOpaqueAction()), //
                Arguments.of(UML.getExpansionNode()), //
                Arguments.of(UML.getOutputPin())//
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_ACTIVITY, UML.getActivity());
        String rootActivityId = this.findGraphicalElementContentByLabel(ROOT_ACTIVITY).getId();
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.NODES, UML.getActivityParameterNode()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()));
        this.createNodeWithLabel(rootActivityId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), EXPANSION_REGION_CONTAINER);
        String expansionRegionContainerId = this.findGraphicalElementContentByLabel(EXPANSION_REGION_CONTAINER).getId();
        this.createSourceAndTargetNodes(expansionRegionContainerId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionNode()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()));
        String opaqueActionContainerId = this.createNodeWithLabel(rootActivityId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), OPAQUE_ACTION_CONTAINER).getId();
        this.createSourceAndTargetNodes(opaqueActionContainerId, new ADCreationTool(ADToolSections.PIN, UML.getInputPin()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()));
        this.createSourceAndTargetNodes(opaqueActionContainerId, new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()));
        this.createSourceAndTargetNodes(rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()));
    }

    /**
     * Sets the {@code controlType} attribute of the provided elements to {@code true}.
     * <p>
     * This method accepts labels of elements that are {@link ObjectNode}s.
     * </p>
     *
     * @param elementLabels
     *            the labels of the {@link ObjectNode}s to configure
     */
    private void setIsControlType(List<String> elementLabels) {
        EditingContext editingContext = (EditingContext) this.getEditingContext();
        for (String elementLabel : elementLabels) {
            String semanticTargetId = ((Node) this.findGraphicalElementExcludingContentByLabel(elementLabel)).getTargetObjectId();
            ObjectNode objectNode = (ObjectNode) this.getObjectSearchService()
                    .getObject(editingContext, semanticTargetId).get();
            objectNode.setIsControlType(true);
        }
        this.persistenceService.persist(new ICause.NoOp(), editingContext);
        this.editingContextEventProcessorRegistry.disposeEditingContextEventProcessor(editingContext.getId());
        this.diagramEventSubscriptionRunner.createSubscription(this.editingContextId, this.representationId);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("controlFlowParameters")
    public void testReconnectControlFlowSource(EClass newSourceType) {
        if (UML.getObjectNode().isSuperTypeOf(newSourceType)) {
            // Set the control type attribute of the new source node to true to make sure the edge can be reconnected.
            this.setIsControlType(List.of(newSourceType.getName() + NEW_SOURCE));
        }
        String controlFlowId = this.createEdge(FORK_NODE_SOURCE, JOIN_NODE_TARGET, new ADCreationTool(ADToolSections.EDGES, UML.getControlFlow()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(controlFlowId, newSourceLabel, graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("objectFlowParameters")
    public void testReconnectObjectFlowSource(EClass newSourceType) {
        String objectFlowId = this.createEdge(FORK_NODE_SOURCE, JOIN_NODE_TARGET, new ADCreationTool(ADToolSections.EDGES, UML.getObjectFlow()));
        String newSourceLabel = newSourceType.getName() + NEW_SOURCE;
        Checker graphicalChecker = new EdgeSourceGraphicalChecker(() -> this.findGraphicalElementExcludingContentByLabel(newSourceLabel));
        this.reconnectEdgeSource(objectFlowId, newSourceLabel, graphicalChecker);
    }
}

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
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.web.application.representations.uml.ADDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.activity.checker.ADObjectFlowOnOpaqueActionCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.activity.utils.ADCreationTool;
import org.eclipse.papyrus.web.tools.activity.utils.ADMappingTypes;
import org.eclipse.papyrus.web.tools.activity.utils.ADToolSections;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EdgeCreationTest;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.uml2.uml.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edge creation tools in the Activity Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ADSubNodeEdgeCreationTest extends EdgeCreationTest {

    private static final String ROOT_ACTIVITY = "rootActivity";

    private static final String ACTIVITY_FINAL_NODE_TARGET = "ActivityFinalNodeTarget";

    private static final String ACTIVITY_PARAMETER_NODE_SOURCE = "ActivityParameterNodeSource";

    private static final String ACTIVITY_PARAMETER_NODE_TARGET = "ActivityParameterNodeTarget";

    private static final String CALL_BEHAVIOR_ACTION_SOURCE = "CallBehaviorActionSource";

    private static final String CALL_BEHAVIOR_ACTION_TARGET = "CallBehaviorActionTarget";

    private static final String DECISION_NODE_SOURCE = "DecisionNodeSource";

    private static final String DECISION_NODE_TARGET = "DecisionNodeTarget";

    private static final String EXPANSION_NODE_SOURCE = "ExpansionNodeSource";

    private static final String EXPANSION_NODE_TARGET = "ExpansionNodeTarget";

    private static final String FORK_NODE_SOURCE = "ForkNodeSource";

    private static final String FORK_NODE_TARGET = "ForkNodeTarget";

    private static final String FLOW_FINAL_NODE_TARGET = "FlowFinalNodeTarget";

    private static final String INITIAL_NODE_SOURCE = "InitialNodeSource";

    private static final String INPUT_PIN_SOURCE = "InputPinSource";

    private static final String INPUT_PIN_TARGET = "InputPinTarget";

    private static final String JOIN_NODE_SOURCE = "JoinNodeSource";

    private static final String JOIN_NODE_TARGET = "JoinNodeTarget";

    private static final String MERGE_NODE_SOURCE = "MergeNodeSource";

    private static final String MERGE_NODE_TARGET = "MergeNodeTarget";

    private static final String OPAQUE_ACTION_SOURCE = "OpaqueActionSource";

    private static final String OPAQUE_ACTION_TARGET = "OpaqueActionTarget";

    private static final String OUTPUT_PIN_SOURCE = "OutputPinSource";

    private static final String OUTPUT_PIN_TARGET = "OutputPinTarget";

    private static final String STRUCTURED_ACTIVITY_NODE_SOURCE = "StructuredActivityNodeSource";

    private static final String STRUCTURED_ACTIVITY_NODE_TARGET = "StructuredActivityNodeTarget";

    private static final String EXPANSION_REGION_CONTAINER = "ExpansionRegionContainer";

    private static final String OPAQUE_ACTION_CONTAINER = "OpaqueActionContainer";

    public ADSubNodeEdgeCreationTest() {
        super(DEFAULT_DOCUMENT, ADDiagramDescriptionBuilder.AD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> controlFlowParameters() {
        List<String> sources = List.of(ACTIVITY_PARAMETER_NODE_SOURCE, //
                CALL_BEHAVIOR_ACTION_SOURCE, //
                DECISION_NODE_SOURCE, //
                FORK_NODE_SOURCE, //
                EXPANSION_NODE_SOURCE, //
                INITIAL_NODE_SOURCE, //
                INPUT_PIN_SOURCE, //
                JOIN_NODE_SOURCE, //
                MERGE_NODE_SOURCE, //
                OPAQUE_ACTION_SOURCE, //
                OUTPUT_PIN_SOURCE, //
                STRUCTURED_ACTIVITY_NODE_SOURCE//
        );
        List<String> targets = List.of(ACTIVITY_FINAL_NODE_TARGET, //
                ACTIVITY_PARAMETER_NODE_TARGET, //
                CALL_BEHAVIOR_ACTION_TARGET, //
                DECISION_NODE_TARGET, //
                FORK_NODE_TARGET, //
                EXPANSION_NODE_TARGET, //
                FLOW_FINAL_NODE_TARGET, //
                INPUT_PIN_TARGET, //
                JOIN_NODE_TARGET, //
                MERGE_NODE_TARGET, //
                OPAQUE_ACTION_TARGET, //
                OUTPUT_PIN_TARGET, //
                STRUCTURED_ACTIVITY_NODE_TARGET//
        );
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> objectFlowParameters() {
        List<String> sources = List.of(ACTIVITY_PARAMETER_NODE_SOURCE, //
                DECISION_NODE_SOURCE, //
                FORK_NODE_SOURCE, //
                JOIN_NODE_SOURCE, //
                MERGE_NODE_SOURCE, //
                OPAQUE_ACTION_SOURCE, //
                EXPANSION_NODE_SOURCE, //
                OUTPUT_PIN_SOURCE //
        );
        List<String> tragets = List.of(ACTIVITY_PARAMETER_NODE_TARGET, //
                ACTIVITY_FINAL_NODE_TARGET, //
                DECISION_NODE_TARGET, //
                FLOW_FINAL_NODE_TARGET, //
                FORK_NODE_TARGET, //
                JOIN_NODE_TARGET, //
                MERGE_NODE_TARGET, //
                OPAQUE_ACTION_TARGET, //
                EXPANSION_NODE_TARGET, //
                INPUT_PIN_TARGET//
        );
        return cartesianProduct(sources, tragets);
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
        this.createNodeWithLabel(rootActivityId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), OPAQUE_ACTION_CONTAINER);
        String opaqueActionContainerId = this.findGraphicalElementExcludingContentByLabel(OPAQUE_ACTION_CONTAINER).getId();
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
            ObjectNode objectNode = (ObjectNode) this.getObjectService().getObject(editingContext, semanticTargetId).get();
            objectNode.setIsControlType(true);
        }
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("controlFlowParameters")
    public void testCreateControlFlow(String sourceElementLabel, String targetElementLabel) {
        // Set the control type attribute for nodes used in ControlFlow tests.
        this.setIsControlType(List.of(ACTIVITY_PARAMETER_NODE_SOURCE, ACTIVITY_PARAMETER_NODE_TARGET, EXPANSION_NODE_SOURCE, EXPANSION_NODE_TARGET, INPUT_PIN_SOURCE, INPUT_PIN_TARGET,
                OUTPUT_PIN_SOURCE, OUTPUT_PIN_TARGET));
        final EdgeCreationGraphicalChecker graphicalChecker = new EdgeCreationGraphicalChecker(this::getDiagram, null, ADMappingTypes.getMappingType(UML.getControlFlow()), this.getCapturedEdges());
        final EdgeCreationSemanticChecker semanticChecker;
        if (sourceElementLabel.equals(EXPANSION_NODE_SOURCE) && targetElementLabel.equals(EXPANSION_NODE_TARGET)) {
            Supplier<EObject> expectedSemanticOwnerSupplier = () -> this.findSemanticElementByName(EXPANSION_REGION_CONTAINER);
            semanticChecker = new EdgeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, UML.getControlFlow(), expectedSemanticOwnerSupplier,
                    UML.getStructuredActivityNode_Edge());
        } else {
            Supplier<EObject> expectedSemanticOwnerSupplier = () -> this.findSemanticElementByName(ROOT_ACTIVITY);
            semanticChecker = new EdgeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, UML.getControlFlow(), expectedSemanticOwnerSupplier, UML.getActivity_Edge());
        }
        this.createEdge(sourceElementLabel, targetElementLabel, new ADCreationTool(ADToolSections.EDGES, UML.getControlFlow()), new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("objectFlowParameters")
    public void testCreateObjectFlow(String sourceElementLabel, String targetElementLabel) {
        int expectedNumberOfPins = 0;
        if (sourceElementLabel.equals(OPAQUE_ACTION_SOURCE)) {
            expectedNumberOfPins++;
        }
        if (targetElementLabel.equals(OPAQUE_ACTION_TARGET)) {
            expectedNumberOfPins++;
        }
        EdgeCreationGraphicalChecker graphicalChecker;
        if (expectedNumberOfPins != 0) {
            graphicalChecker = new ADObjectFlowOnOpaqueActionCreationGraphicalChecker(this::getDiagram, null, ADMappingTypes.getMappingType(UML.getObjectFlow()), this.getCapturedEdges(),
                    expectedNumberOfPins);
        } else {
            graphicalChecker = new EdgeCreationGraphicalChecker(this::getDiagram, null, ADMappingTypes.getMappingType(UML.getObjectFlow()), this.getCapturedEdges());
        }
        EdgeCreationSemanticChecker semanticChecker;
        if (sourceElementLabel.equals(EXPANSION_NODE_SOURCE) && targetElementLabel.equals(EXPANSION_NODE_TARGET)) {
            Supplier<EObject> expectedSemanticOwnerSupplier = () -> this.findSemanticElementByName(EXPANSION_REGION_CONTAINER);
            semanticChecker = new EdgeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, UML.getObjectFlow(), expectedSemanticOwnerSupplier,
                    UML.getStructuredActivityNode_Edge());
        } else {
            Supplier<EObject> expectedSemanticOwnerSupplier = () -> this.findSemanticElementByName(ROOT_ACTIVITY);
            semanticChecker = new EdgeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, UML.getObjectFlow(), expectedSemanticOwnerSupplier, UML.getActivity_Edge());
        }
        this.createEdge(sourceElementLabel, targetElementLabel, new ADCreationTool(ADToolSections.EDGES, UML.getObjectFlow()), new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

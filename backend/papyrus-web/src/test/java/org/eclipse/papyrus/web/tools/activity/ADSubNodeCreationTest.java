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

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.ADDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.activity.checker.ADActivityNodeWithPinsCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.activity.utils.ADCreationTool;
import org.eclipse.papyrus.web.tools.activity.utils.ADMappingTypes;
import org.eclipse.papyrus.web.tools.activity.utils.ADToolSections;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.test.NodeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node creation tools inside graphical parents in the Activity Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ADSubNodeCreationTest extends NodeCreationTest {

    private static final String ROOT_ACTIVITY = "rootActivity";

    private static final EReference ACCEPT_EVENT_ACTION_RESULT = UML.getAcceptEventAction_Result();

    private static final EReference ACTIVITY_OWNED_GROUP = UML.getActivity_OwnedGroup();

    private static final EReference ACTIVITY_OWNED_NODE = UML.getActivity_OwnedNode();

    private static final EReference ACTIVITY_PARTITION_SUB_PARTITION = UML.getActivityPartition_Subpartition();

    private static final EReference ACTIVITY_STRUCTURED_NODE = UML.getActivity_StructuredNode();

    private static final EReference ELEMENT_OWNED_COMMENT = UML.getElement_OwnedComment();

    private static final EReference NAMESPACE_OWNED_RULE = UML.getNamespace_OwnedRule();

    private static final EReference CALL_ACTION_RESULT = UML.getCallAction_Result();

    private static final EReference CLASS_NESTED_CLASSIFIER = UML.getClass_NestedClassifier();

    private static final EReference CONDITIONAL_NODE_RESULT = UML.getConditionalNode_Result();

    private static final EReference INVOCATION_ACTION_ARGUMENT = UML.getInvocationAction_Argument();

    private static final EReference OPAQUE_ACTION_INPUT_VALUE = UML.getOpaqueAction_InputValue();

    private static final EReference OPAQUE_ACTION_OUTPUT_VALUE = UML.getOpaqueAction_OutputValue();

    private static final EReference SEQUENCE_NODE_EXECUTABLE_NODE = UML.getSequenceNode_ExecutableNode();

    private static final EReference STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT = UML.getStructuredActivityNode_StructuredNodeInput();

    private static final EReference STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_OUTPUT = UML.getStructuredActivityNode_StructuredNodeOutput();

    private static final EReference STRUCTURED_ACTIVITY_NODE_NODE = UML.getStructuredActivityNode_Node();

    private static final String ACCEPT_EVENT_ACTION_LABEL = "AcceptEventActionContainer";

    private static final String ACTIVITY_PARTITION_LABEL = "ActivityPartitionContainer";

    private static final String CALL_BEHAVIOR_ACTION_LABEL = "CallBehaviorActionContainer";

    private static final String CALL_OPERATION_ACTION_LABEL = "CallOperationActionContainer";

    private static final String BROADCAST_SIGNAL_ACTION_LABEL = "BroadcastSignalActionContainer";

    private static final String CONDITIONAL_NODE_LABEL = "ConditionalNodeContainer";

    private static final String INTERRUPTIBLE_ACTIVITY_REGION_LABEL = "InterruptibleActivityRegion1";

    private static final String EXPANSION_REGION_LABEL = "ExpansionRegionContainer";

    private static final String LOOP_NODE_LABEL = "LoopNodeContainer";

    private static final String OPAQUE_ACTION_LABEL = "OpaqueActionContainer";

    private static final String SEND_SIGNAL_ACTION_LABEL = "SendSignalActionContainer";

    private static final String SEQUENCE_NODE_LABEL = "SequenceNodeContainer";

    private static final String START_OBJECT_BEHAVIOR_ACTION_LABEL = "StartObjectBehaviorActionContainer";

    private static final String STRUCTURED_ACTIVITY_NODE_LABEL = "StructuredActivityNodeContainer";

    private String rootActivityId;

    public ADSubNodeCreationTest() {
        super(DEFAULT_DOCUMENT, ADDiagramDescriptionBuilder.AD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> activityChildrenParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()),
                        UML.getAcceptCallAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()),
                        UML.getAcceptEventAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()),
                        UML.getActivityFinalNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getActivityParameterNode()),
                        UML.getActivityParameterNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()), UML.getActivityPartition(), ACTIVITY_OWNED_GROUP),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getComment()), UML.getComment(), ELEMENT_OWNED_COMMENT),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint(), NAMESPACE_OWNED_RULE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getInterruptibleActivityRegion()), UML.getInterruptibleActivityRegion(), ACTIVITY_OWNED_GROUP),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()), UML.getReadIsClassifiedObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivity()), UML.getActivity(), CLASS_NESTED_CLASSIFIER),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), ACTIVITY_OWNED_NODE));
    }

    private static Stream<Arguments> activityPartitionChildrenParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()), UML.getActivityPartition(), ACTIVITY_PARTITION_SUB_PARTITION),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getComment()), UML.getComment(), ELEMENT_OWNED_COMMENT),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()), UML.getReadIsClassifiedObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), ACTIVITY_OWNED_NODE));
    }

    private static Stream<Arguments> conditionalNodeChildrenParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getComment()), UML.getComment(), ELEMENT_OWNED_COMMENT),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint(), NAMESPACE_OWNED_RULE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin(), CONDITIONAL_NODE_RESULT),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE));
    }

    private static Stream<Arguments> expansionRegionChildrenParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getComment()), UML.getComment(), ELEMENT_OWNED_COMMENT),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint(), NAMESPACE_OWNED_RULE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionNode()), UML.getExpansionNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_OUTPUT),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE));
    }

    private static Stream<Arguments> interruptibleActivityRegionChildrenParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getComment()), UML.getComment(), ELEMENT_OWNED_COMMENT),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()), UML.getReadIsClassifiedObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode(), ACTIVITY_STRUCTURED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), ACTIVITY_OWNED_NODE));
    }

    private static Stream<Arguments> loopNodeAndStructuredActivityNodeChildrenParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getComment()), UML.getComment(), ELEMENT_OWNED_COMMENT),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint(), NAMESPACE_OWNED_RULE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_OUTPUT),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE));
    }

    private static Stream<Arguments> sequenceNodeChildrenParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getComment()), UML.getComment(), ELEMENT_OWNED_COMMENT),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint(), NAMESPACE_OWNED_RULE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_OUTPUT),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction(), SEQUENCE_NODE_EXECUTABLE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()), UML.getReadIsClassifiedObjectAction(), SEQUENCE_NODE_EXECUTABLE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction(), SEQUENCE_NODE_EXECUTABLE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE));
    }

    private static Stream<Arguments> pinAsChildrenParameters() {
        // This argument list provides both the parent element to create (the one containing the pins), as well as the
        // specification of the pin to create and check.
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), ACCEPT_EVENT_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()),
                        UML.getOutputPin(), ACCEPT_EVENT_ACTION_RESULT),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), ACCEPT_EVENT_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()),
                        UML.getOutputPin(), ACCEPT_EVENT_ACTION_RESULT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), BROADCAST_SIGNAL_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), BROADCAST_SIGNAL_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), BROADCAST_SIGNAL_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), CALL_BEHAVIOR_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), CALL_BEHAVIOR_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getInputPin()),
                        UML.getInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), CALL_BEHAVIOR_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getValuePin()),
                        UML.getValuePin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), CALL_BEHAVIOR_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()),
                        UML.getOutputPin(), CALL_ACTION_RESULT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), CALL_OPERATION_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), CALL_OPERATION_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getInputPin()),
                        UML.getInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), CALL_OPERATION_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getValuePin()),
                        UML.getValuePin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), CALL_OPERATION_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin(), CALL_ACTION_RESULT),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), OPAQUE_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()),
                        UML.getActionInputPin(), OPAQUE_ACTION_INPUT_VALUE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), OPAQUE_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getInputPin()),
                        UML.getInputPin(), OPAQUE_ACTION_INPUT_VALUE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), OPAQUE_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getValuePin()),
                        UML.getValuePin(), OPAQUE_ACTION_INPUT_VALUE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), OPAQUE_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()),
                        UML.getOutputPin(), OPAQUE_ACTION_OUTPUT_VALUE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), SEND_SIGNAL_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()),
                        UML.getActionInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), SEND_SIGNAL_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getInputPin()),
                        UML.getInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), SEND_SIGNAL_ACTION_LABEL, new ADCreationTool(ADToolSections.PIN, UML.getValuePin()),
                        UML.getValuePin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), START_OBJECT_BEHAVIOR_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), START_OBJECT_BEHAVIOR_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), START_OBJECT_BEHAVIOR_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin(), INVOCATION_ACTION_ARGUMENT),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), START_OBJECT_BEHAVIOR_ACTION_LABEL,
                        new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin(), CALL_ACTION_RESULT));
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_ACTIVITY, UML.getActivity());
        this.rootActivityId = this.findGraphicalElementContentByLabel(ROOT_ACTIVITY).getId();
        // We can't create other container elements because they would clash with the graphical checks performed on the
        // root activity children.
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in Activity")
    @MethodSource("activityChildrenParameters")
    public void testCreateNodeInActivity(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        String mappingType = ADMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(expectedType, mappingType, ROOT_ACTIVITY);
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(ROOT_ACTIVITY), containmentReference);
        this.createSubNode(ROOT_ACTIVITY, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in ActivityPartition")
    @MethodSource("activityPartitionChildrenParameters")
    public void testCreateNodeInActivityPartition(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        // Create the container node
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()), ACTIVITY_PARTITION_LABEL);
        String mappingType = ADMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(expectedType, mappingType, ACTIVITY_PARTITION_LABEL);
        // The created node is semantically contained in the root activity that contains the partition
        final String semanticOwnerLabel;
        if (UML.getActivityPartition().equals(expectedType) || UML.getComment().equals(expectedType)) {
            // ActivityPartitions and Comments are semantically contained in their parent ActivityPartition
            semanticOwnerLabel = ACTIVITY_PARTITION_LABEL;
        } else {
            semanticOwnerLabel = ROOT_ACTIVITY;
        }
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(semanticOwnerLabel), containmentReference);
        this.createSubNode(ACTIVITY_PARTITION_LABEL, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in ConditionalNode")
    @MethodSource("conditionalNodeChildrenParameters")
    public void testCreateNodeInConditionalNode(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        // Create the container node
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), CONDITIONAL_NODE_LABEL);
        String mappingType = ADMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(expectedType, mappingType, CONDITIONAL_NODE_LABEL);
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(CONDITIONAL_NODE_LABEL), containmentReference);
        this.createSubNode(CONDITIONAL_NODE_LABEL, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in ExpansionRegion")
    @MethodSource("expansionRegionChildrenParameters")
    public void testCreateNodeInExpansionRegion(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        // Create the container node
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), EXPANSION_REGION_LABEL);
        final String mappingType;
        if (UML.getExpansionNode().equals(expectedType)) {
            // ExpansionNode isn't shared since it's used only in one place
            mappingType = ADMappingTypes.getMappingType(expectedType);
        } else {
            mappingType = ADMappingTypes.getMappingTypeAsSubNode(expectedType);
        }
        NodeCreationGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(expectedType, mappingType, EXPANSION_REGION_LABEL);
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(EXPANSION_REGION_LABEL), containmentReference);
        this.createSubNode(EXPANSION_REGION_LABEL, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create node {1} in InterruptibleActivityRegion")
    @MethodSource("interruptibleActivityRegionChildrenParameters")
    public void testCreateNodeInInterruptibleActivityRegion(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        // Create the container node
        this.createNode(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getInterruptibleActivityRegion()));
        String mappingType = ADMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(expectedType, mappingType, INTERRUPTIBLE_ACTIVITY_REGION_LABEL);
        // The created node is semantically contained in the root activity that contains the InterruptibleActivityRegion
        final String semanticOwnerLabel;
        if (UML.getComment().equals(expectedType)) {
            // Comments are semantically contained in their parent InterruptibleActivityRegion
            semanticOwnerLabel = INTERRUPTIBLE_ACTIVITY_REGION_LABEL;
        } else {
            semanticOwnerLabel = ROOT_ACTIVITY;
        }
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(semanticOwnerLabel), containmentReference);
        this.createSubNode(INTERRUPTIBLE_ACTIVITY_REGION_LABEL, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index} Create node {1} in LoopNode")
    @MethodSource("loopNodeAndStructuredActivityNodeChildrenParameters")
    public void testCreateNodeInLoopNode(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        // Create the container node
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), LOOP_NODE_LABEL);
        String mappingType = ADMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(expectedType, mappingType, LOOP_NODE_LABEL);
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(LOOP_NODE_LABEL), containmentReference);
        this.createSubNode(LOOP_NODE_LABEL, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index} Creation node {1} in SequenceNode")
    @MethodSource("sequenceNodeChildrenParameters")
    public void testCreateNodeInSequenceNode(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        // Create the container node
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), SEQUENCE_NODE_LABEL);
        String mappingType = ADMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(expectedType, mappingType, SEQUENCE_NODE_LABEL);
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(SEQUENCE_NODE_LABEL), containmentReference);
        this.createSubNode(SEQUENCE_NODE_LABEL, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index} Create node {1} in StructuredActivityNode")
    @MethodSource("loopNodeAndStructuredActivityNodeChildrenParameters")
    public void testCreateNodeInStructuredActivityNode(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        // Create the container node
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), STRUCTURED_ACTIVITY_NODE_LABEL);
        String mappingType = ADMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(expectedType, mappingType, STRUCTURED_ACTIVITY_NODE_LABEL);
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(STRUCTURED_ACTIVITY_NODE_LABEL), containmentReference);
        this.createSubNode(STRUCTURED_ACTIVITY_NODE_LABEL, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index} Create pin {3} in {1}")
    @MethodSource("pinAsChildrenParameters")
    public void testCreatePinInNode(CreationTool parentCreationTool, String parentLabel, CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        // Create the container node
        this.createNodeWithLabel(this.rootActivityId, parentCreationTool, parentLabel);
        String mappingType = ADMappingTypes.getMappingTypeAsSubNode(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(expectedType, mappingType, parentLabel);
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType, () -> this.findSemanticElementByName(parentLabel),
                containmentReference);
        this.createSubNode(parentLabel, nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    private NodeCreationGraphicalChecker getGraphicalCheckerFor(EClass expectedType, String mappingType, String graphicalContainerLabel) {
        int expectedNumberOfElement = ADMappingTypes.getExpectedNumberOfCreatedElement(expectedType);
        NodeCreationGraphicalChecker checker;
        checker = new ADActivityNodeWithPinsCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(graphicalContainerLabel), mappingType,
                this.getCapturedNodes(),
                expectedNumberOfElement);
        return checker;
    }

}

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
import org.eclipse.papyrus.web.tools.activity.checker.ADActivityNodeWithPinsDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.activity.utils.ADCreationTool;
import org.eclipse.papyrus.web.tools.activity.utils.ADMappingTypes;
import org.eclipse.papyrus.web.tools.activity.utils.ADToolSections;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.test.NodeDeletionTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests graphical deletion node tool inside semantic parents in the Activity Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ADSubNodeSemanticDeletionTest extends NodeDeletionTest {

    private static final String ROOT_ACTIVITY = "rootActivity";

    private static final EReference ACCEPT_EVENT_ACTION_RESULT = UML.getAcceptEventAction_Result();

    private static final EReference ACTIVITY_OWNED_GROUP = UML.getActivity_OwnedGroup();

    private static final EReference ACTIVITY_OWNED_NODE = UML.getActivity_OwnedNode();

    private static final EReference ACTIVITY_PARTITION_SUB_PARTITION = UML.getActivityPartition_Subpartition();

    private static final EReference ACTIVITY_STRUCTURED_NODE = UML.getActivity_StructuredNode();

    private static final EReference NAMESPACE_OWNED_RULE = UML.getNamespace_OwnedRule();

    private static final EReference CALL_ACTION_RESULT = UML.getCallAction_Result();

    private static final EReference CLASS_NESTED_CLASSIFIER = UML.getClass_NestedClassifier();

    private static final EReference INVOCATION_ACTION_ARGUMENT = UML.getInvocationAction_Argument();

    private static final EReference OPAQUE_ACTION_INPUT_VALUE = UML.getOpaqueAction_InputValue();

    private static final EReference OPAQUE_ACTION_OUTPUT_VALUE = UML.getOpaqueAction_OutputValue();

    private static final EReference SEQUENCE_NODE_EXECUTABLE_NODE = UML.getSequenceNode_ExecutableNode();

    private static final EReference STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT = UML.getStructuredActivityNode_StructuredNodeInput();

    private static final EReference STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_OUTPUT = UML.getStructuredActivityNode_StructuredNodeOutput();

    private static final EReference STRUCTURED_ACTIVITY_NODE_NODE = UML.getStructuredActivityNode_Node();

    private static final String ACCEPT_EVENT_ACTION_LABEL = "AcceptEventActionContainer";

    private static final String ACTIVITY_LABEL = "ActivityContainer";

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

    private static final String ACTIVITY_SUB_NODE_SUFFIX = "In_Activity";

    private static final String ACTIVITY_PARTITION_SUB_NODE_SUFFIX = "In_ActivityPartition";

    private static final String CONDITIONAL_NODE_SUB_NODE_SUFFIX = "In_ConditionalNode";

    private static final String EXPANSION_REGION_SUB_NODE_SUFFIX = "In_ExpansionRegion";

    private static final String INTERRUPTIBLE_ACTIVITY_REGION_SUB_NODE_SUFFIX = "In_InterruptibleActivityRegion";

    private static final String LOOP_NODE_SUB_NODE_SUFFIX = "In_LoopNode";

    private static final String STRUCTURED_ACTIVITY_NODE_SUB_NODE_SUFFIX = "In_StructuredActivityNode";

    private static final String SEQUENCE_NODE_SUB_NODE_SUFFIX = "In_SequenceNode";

    private String rootActivityId;

    public ADSubNodeSemanticDeletionTest() {
        super(DEFAULT_DOCUMENT, ADDiagramDescriptionBuilder.AD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> activityParameters() {
        return Stream.of(//
                Arguments.of(UML.getAcceptCallAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getAcceptEventAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getActivityFinalNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getActivityParameterNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getActivityPartition(), ACTIVITY_OWNED_GROUP), //
                Arguments.of(UML.getAddStructuralFeatureValueAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getBroadcastSignalAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getCallBehaviorAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getCallOperationAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getClearAssociationAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getClearStructuralFeatureAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getConditionalNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getConstraint(), NAMESPACE_OWNED_RULE), //
                Arguments.of(UML.getCreateObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getDecisionNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getDestroyObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getExpansionRegion(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getFlowFinalNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getForkNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getInitialNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getInterruptibleActivityRegion(), ACTIVITY_OWNED_GROUP), //
                Arguments.of(UML.getJoinNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getLoopNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getMergeNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getOpaqueAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadExtentAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadIsClassifiedObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadSelfAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadStructuralFeatureAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReclassifyObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReduceAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getSendObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getSendSignalAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getSequenceNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getStartClassifierBehaviorAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getStartObjectBehaviorAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getStructuredActivityNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getActivity(), CLASS_NESTED_CLASSIFIER), //
                Arguments.of(UML.getTestIdentityAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getValueSpecificationAction(), ACTIVITY_OWNED_NODE) //
        );
    }

    private static Stream<Arguments> activityPartitionParameters() {
        return Stream.of(//
                Arguments.of(UML.getAcceptCallAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getAcceptEventAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getActivityFinalNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getActivityPartition(), ACTIVITY_PARTITION_SUB_PARTITION), //
                Arguments.of(UML.getAddStructuralFeatureValueAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getBroadcastSignalAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getCallBehaviorAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getCallOperationAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getClearAssociationAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getClearStructuralFeatureAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getConditionalNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getCreateObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getDecisionNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getDestroyObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getExpansionRegion(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getFlowFinalNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getForkNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getInitialNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getJoinNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getLoopNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getMergeNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getOpaqueAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadExtentAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadIsClassifiedObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadSelfAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadStructuralFeatureAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReclassifyObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReduceAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getSendObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getSendSignalAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getSequenceNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getStartClassifierBehaviorAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getStartObjectBehaviorAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getStructuredActivityNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getTestIdentityAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getValueSpecificationAction(), ACTIVITY_OWNED_NODE)//
        );
    }

    private static Stream<Arguments> conditionalNodeParameters() {
        return Stream.of(//
                Arguments.of(UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getActivityFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getConstraint(), NAMESPACE_OWNED_RULE), //
                Arguments.of(UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getDecisionNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getFlowFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getForkNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getInitialNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getJoinNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getMergeNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReadSelfAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReadStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSendObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSendSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSequenceNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStructuredActivityNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE) //
        );
    }

    private static Stream<Arguments> expansionRegionParameters() {
        return Stream.of(//
                Arguments.of(UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getActivityFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getBroadcastSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getClearAssociationAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getConstraint(), NAMESPACE_OWNED_RULE), //
                Arguments.of(UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getDecisionNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getFlowFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getForkNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getInitialNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getExpansionNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getJoinNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getMergeNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReadSelfAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReadStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReclassifyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReduceAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSendObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSendSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSequenceNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStartClassifierBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStartObjectBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStructuredActivityNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getTestIdentityAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE) //
        );
    }

    private static Stream<Arguments> interruptibleActivityRegionParameters() {
        return Stream.of(//
                Arguments.of(UML.getAcceptCallAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getAcceptEventAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getActivityFinalNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getAddStructuralFeatureValueAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getBroadcastSignalAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getCallBehaviorAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getCallOperationAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getClearAssociationAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getClearStructuralFeatureAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getConditionalNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getCreateObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getDecisionNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getDestroyObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getExpansionRegion(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getFlowFinalNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getForkNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getInitialNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getJoinNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getLoopNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getMergeNode(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getOpaqueAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadExtentAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadIsClassifiedObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadSelfAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReadStructuralFeatureAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReclassifyObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getReduceAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getSendObjectAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getSendSignalAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getSequenceNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getStartClassifierBehaviorAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getStartObjectBehaviorAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getStructuredActivityNode(), ACTIVITY_STRUCTURED_NODE), //
                Arguments.of(UML.getTestIdentityAction(), ACTIVITY_OWNED_NODE), //
                Arguments.of(UML.getValueSpecificationAction(), ACTIVITY_OWNED_NODE) //
        );
    }

    private static Stream<Arguments> loopNodeAndStructuredActivityNodeParameters() {
        return Stream.of(//
                Arguments.of(UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getActionInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT), //
                Arguments.of(UML.getActivityFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getBroadcastSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getClearAssociationAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getConstraint(), NAMESPACE_OWNED_RULE), //
                Arguments.of(UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getDecisionNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getFlowFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getForkNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getInitialNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT), //
                Arguments.of(UML.getJoinNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getMergeNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getOutputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_OUTPUT), //
                Arguments.of(UML.getReadSelfAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReadStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReclassifyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReduceAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSendObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSendSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSequenceNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStartClassifierBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStartObjectBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStructuredActivityNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getValuePin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT), //
                Arguments.of(UML.getTestIdentityAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE) //
        );
    }

    private static Stream<Arguments> sequenceNodeParameters() {
        return Stream.of(//
                Arguments.of(UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getActionInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT), //
                Arguments.of(UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getBroadcastSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getClearAssociationAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getConstraint(), NAMESPACE_OWNED_RULE), //
                Arguments.of(UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getInputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT), //
                Arguments.of(UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getOutputPin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_OUTPUT), //
                Arguments.of(UML.getReadExtentAction(), SEQUENCE_NODE_EXECUTABLE_NODE), //
                Arguments.of(UML.getReadIsClassifiedObjectAction(), SEQUENCE_NODE_EXECUTABLE_NODE), //
                Arguments.of(UML.getReadSelfAction(), SEQUENCE_NODE_EXECUTABLE_NODE), //
                Arguments.of(UML.getReadStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReclassifyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getReduceAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSendObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSendSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getSequenceNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStartClassifierBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStartObjectBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getStructuredActivityNode(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getValuePin(), STRUCTURED_ACTIVITY_NODE_STRUCTURED_NODE_INPUT), //
                Arguments.of(UML.getTestIdentityAction(), STRUCTURED_ACTIVITY_NODE_NODE), //
                Arguments.of(UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE) //
        );
    }

    private static Stream<Arguments> pinAsChildrenParameters() {
        // This argument list provides both the parent element to create (the one containing the pins), as well as the
        // specification of the pin to delete and check.
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
        super.setUpWithIntermediateRoot(ROOT_ACTIVITY, UML.getActivity());
        this.rootActivityId = this.findGraphicalElementContentByLabel(ROOT_ACTIVITY).getId();
        // Nodes to delete are created in individual test cases to speed up the test suite
    }

    private void createActivitySubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getActivityParameterNode()), UML.getActivityParameterNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()), UML.getActivityPartition().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()),
                UML.getAddStructuralFeatureValueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode().getName() + suffix);
        this.createNode(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getInterruptibleActivityRegion()));
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()),
                UML.getReadIsClassifiedObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivity()), UML.getActivity().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction().getName() + suffix);
    }

    private void createActivityPartitionSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()), UML.getActivityPartition().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()),
                UML.getAddStructuralFeatureValueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()),
                UML.getReadIsClassifiedObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction().getName() + suffix);
    }

    private void createConditionalNodeSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()),
                UML.getAddStructuralFeatureValueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction().getName() + suffix);
    }

    private void createExpansionRegionSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()),
                UML.getAddStructuralFeatureValueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionNode()), UML.getExpansionNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction().getName() + suffix);
    }

    private void createInterruptibleActivityRegionSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()),
                UML.getAddStructuralFeatureValueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()),
                UML.getReadIsClassifiedObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction().getName() + suffix);
    }

    private void createLoopNodeAndStructuredActivityNodeSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()),
                UML.getAddStructuralFeatureValueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction().getName() + suffix);
    }

    private void createSequenceNodeSubNodes(Node parentNode, String suffix) {
        String parentNodeId = parentNode.getId();
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()), UML.getActionInputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()),
                UML.getAddStructuralFeatureValueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getInputPin()), UML.getInputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()), UML.getOutputPin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()),
                UML.getReadIsClassifiedObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()), UML.getReclassifyObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()), UML.getReduceAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), UML.getStartClassifierBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()), UML.getStartObjectBehaviorAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.PIN, UML.getValuePin()), UML.getValuePin().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction().getName() + suffix);
        this.createNodeWithLabel(parentNodeId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction().getName() + suffix);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("activityParameters")
    public void testDeleteSemanticNodeInActivity(EClass elementType, EReference containmentReference) {
        // Create all the Activity sub-nodes to delete
        Node activityContainerHolder = this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivity()), ACTIVITY_LABEL);
        Node activityContainer = this.getSubNode(activityContainerHolder, ADMappingTypes.getMappingTypeContentAsSubNode(UML.getActivity()));

        this.createActivitySubNodes(activityContainer, ACTIVITY_SUB_NODE_SUFFIX);
        DeletionGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(elementType, ACTIVITY_LABEL);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(ACTIVITY_LABEL), containmentReference);
        final String elementLabel;
        if (UML.getInterruptibleActivityRegion().equals(elementType)) {
            elementLabel = elementType.getName() + "1";
        } else {
            elementLabel = elementType.getName() + ACTIVITY_SUB_NODE_SUFFIX;
        }
        this.deleteSemanticNode(elementLabel, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("activityPartitionParameters")
    public void testDeleteSemanticNodeInActivityPartition(EClass elementType, EReference containmentReference) {
        // Create all the ActivityPartition sub-nodes to delete
        Node activityPartitionContainerHolder = this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()), ACTIVITY_PARTITION_LABEL);
        Node activityPartitionContainer = this.getSubNode(activityPartitionContainerHolder, ADMappingTypes.getMappingTypeContentAsSubNode(UML.getActivityPartition()));

        this.createActivityPartitionSubNodes(activityPartitionContainer, ACTIVITY_PARTITION_SUB_NODE_SUFFIX);
        DeletionGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(elementType, ACTIVITY_PARTITION_LABEL);
        final String expectedSemanticOwner;
        if (UML.getActivityPartition().equals(elementType)) {
            expectedSemanticOwner = ACTIVITY_PARTITION_LABEL;
        } else {
            expectedSemanticOwner = ROOT_ACTIVITY;
        }
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(expectedSemanticOwner), containmentReference);
        this.deleteSemanticNode(elementType.getName() + ACTIVITY_PARTITION_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("conditionalNodeParameters")
    public void testDeleteSemanticNodeInConditionalNode(EClass elementType, EReference containmentReference) {
        // Create all the ConditionalNode sub-nodes to delete
        Node conditionalNodeContainerHolder = this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()),
                CONDITIONAL_NODE_LABEL);
        Node conditionalNodeContainer = this.getSubNode(conditionalNodeContainerHolder, ADMappingTypes.getMappingTypeContentAsSubNode(UML.getConditionalNode()));

        this.createConditionalNodeSubNodes(conditionalNodeContainer, CONDITIONAL_NODE_SUB_NODE_SUFFIX);
        DeletionGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(elementType, CONDITIONAL_NODE_LABEL);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(CONDITIONAL_NODE_LABEL), containmentReference);
        this.deleteSemanticNode(elementType.getName() + CONDITIONAL_NODE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("expansionRegionParameters")
    public void testDeleteSemanticNodeInExpansionRegion(EClass elementType, EReference containmentReference) {
        // Create all the ExpansionRegion sub-nodes to delete
        Node expansionRegionNodeContainerHolder = this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()),
                EXPANSION_REGION_LABEL);
        Node expansionRegionNodeContainer = this.getSubNode(expansionRegionNodeContainerHolder, ADMappingTypes.getMappingTypeContentAsSubNode(UML.getExpansionRegion()));

        this.createExpansionRegionSubNodes(expansionRegionNodeContainer, EXPANSION_REGION_SUB_NODE_SUFFIX);
        DeletionGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(elementType, EXPANSION_REGION_LABEL);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(EXPANSION_REGION_LABEL), containmentReference);
        this.deleteSemanticNode(elementType.getName() + EXPANSION_REGION_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("interruptibleActivityRegionParameters")
    public void testDeleteSemanticNodeInInterruptibleActivityRegion(EClass elementType, EReference containmentReference) {
        // Create all the InterruptibleActivityRegion sub-nodes to delete
        Node activityPartitionContainerHolder = this.createNode(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getInterruptibleActivityRegion()));
        Node activityPartitionContainer = this.getSubNode(activityPartitionContainerHolder, ADMappingTypes.getMappingTypeContentAsSubNode(UML.getInterruptibleActivityRegion()));
        this.createInterruptibleActivityRegionSubNodes(activityPartitionContainer, INTERRUPTIBLE_ACTIVITY_REGION_SUB_NODE_SUFFIX);
        DeletionGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(elementType, INTERRUPTIBLE_ACTIVITY_REGION_LABEL);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(ROOT_ACTIVITY), containmentReference);
        this.deleteSemanticNode(elementType.getName() + INTERRUPTIBLE_ACTIVITY_REGION_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("loopNodeAndStructuredActivityNodeParameters")
    public void testDeleteSemanticNodeInLoopNode(EClass elementType, EReference containmentReference) {
        // Create all the LoopNode sub-nodes to delete
        Node loopNodeContainerHolder = this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()),
                LOOP_NODE_LABEL);
        Node loopNodeContainer = this.getSubNode(loopNodeContainerHolder, ADMappingTypes.getMappingTypeContentAsSubNode(UML.getLoopNode()));

        this.createLoopNodeAndStructuredActivityNodeSubNodes(loopNodeContainer, LOOP_NODE_SUB_NODE_SUFFIX);
        DeletionGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(elementType, LOOP_NODE_LABEL);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(LOOP_NODE_LABEL), containmentReference);
        this.deleteSemanticNode(elementType.getName() + LOOP_NODE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("loopNodeAndStructuredActivityNodeParameters")
    public void testDeleteSemanticNodeInStructuredActivityNode(EClass elementType, EReference containmentReference) {
        // Create all the StructuredActivityNode sub-nodes to delete
        Node structuredActivityNodeContainerHolder = this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()),
                STRUCTURED_ACTIVITY_NODE_LABEL);
        Node structuredActivityNodeContainer = this.getSubNode(structuredActivityNodeContainerHolder, ADMappingTypes.getMappingTypeContentAsSubNode(UML.getStructuredActivityNode()));

        this.createLoopNodeAndStructuredActivityNodeSubNodes(structuredActivityNodeContainer, STRUCTURED_ACTIVITY_NODE_SUB_NODE_SUFFIX);
        DeletionGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(elementType, STRUCTURED_ACTIVITY_NODE_LABEL);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(STRUCTURED_ACTIVITY_NODE_LABEL), containmentReference);
        this.deleteSemanticNode(elementType.getName() + STRUCTURED_ACTIVITY_NODE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("sequenceNodeParameters")
    public void testDeleteSemanticNodeInSequenceNode(EClass elementType, EReference containmentReference) {
        // Create all the SequenceNode sub-nodes to delete
        Node sequenceNodeContainerHolder = this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()),
                SEQUENCE_NODE_LABEL);
        Node sequenceNodeContainer = this.getSubNode(sequenceNodeContainerHolder, ADMappingTypes.getMappingTypeContentAsSubNode(UML.getSequenceNode()));

        this.createSequenceNodeSubNodes(sequenceNodeContainer, SEQUENCE_NODE_SUB_NODE_SUFFIX);
        DeletionGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(elementType, SEQUENCE_NODE_LABEL);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(SEQUENCE_NODE_LABEL), containmentReference);
        this.deleteSemanticNode(elementType.getName() + SEQUENCE_NODE_SUB_NODE_SUFFIX, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest
    @MethodSource("pinAsChildrenParameters")
    public void testDeleteSemanticPinInNode(CreationTool parentCreationTool, String parentLabel, CreationTool nodeCreationTool, EClass elementType, EReference containmentReference) {
        // Create the container node
        this.createNodeWithLabel(this.rootActivityId, parentCreationTool, parentLabel);
        Node parentNode = (Node) this.findGraphicalElementExcludingContentByLabel(parentLabel);
        this.createNodeWithLabel(parentNode.getId(), nodeCreationTool, elementType.getName() + "In_Node");
        DeletionGraphicalChecker graphicalChecker = this.getGraphicalCheckerFor(elementType, parentLabel);
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(parentLabel), containmentReference);
        this.deleteSemanticNode(elementType.getName() + "In_Node", new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @Override
    protected Node createNodeWithLabel(String parentElementId, CreationTool creationTool, String label) {
        if (UML.getReadSelfAction().equals(creationTool.getToolEClass())) {
            this.applyNodeCreationTool(parentElementId, creationTool);
            Node createdNode = (Node) this.findGraphicalElementExcludingContentByLabel("this");
            this.applyEditLabelTool(createdNode.getInsideLabel().getId(), label);
            return (Node) this.findGraphicalElementById(createdNode.getId());
        } else {
            return super.createNodeWithLabel(parentElementId, creationTool, label);
        }
    }

    private DeletionGraphicalChecker getGraphicalCheckerFor(EClass expectedType, String graphicalContainerLabel) {
        int expectedNumberOfPins = ADMappingTypes.getExpectedNumberOfCreatedElement(expectedType);
        DeletionGraphicalChecker checker;
        checker = new ADActivityNodeWithPinsDeletionGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(graphicalContainerLabel),
                expectedNumberOfPins);
        return checker;
    }
}

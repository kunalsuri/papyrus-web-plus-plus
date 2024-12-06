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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.ADDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.activity.utils.ADCreationTool;
import org.eclipse.papyrus.web.tools.activity.utils.ADMappingTypes;
import org.eclipse.papyrus.web.tools.activity.utils.ADToolSections;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.HolderNodeGraphicalDnDGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.test.GraphicalDropTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.uml2.uml.InterruptibleActivityRegion;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test graphical drop tools in the Activity Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ADGraphicalDropTest extends GraphicalDropTest {

    private static final String ROOT_ACTIVITY = "rootActivity";

    private static final EReference ACTIVITY_OWNED_GROUP = UML.getActivity_OwnedGroup();

    private static final EReference ACTIVITY_OWNED_NODE = UML.getActivity_OwnedNode();

    private static final EReference ACTIVITY_PARTITION_SUB_PARTITION = UML.getActivityPartition_Subpartition();

    private static final EReference ACTIVITY_STRUCTURED_NODE = UML.getActivity_StructuredNode();

    private static final EReference NAMESPACE_OWNED_RULE = UML.getNamespace_OwnedRule();

    private static final EReference CLASS_NESTED_CLASSIFIER = UML.getClass_NestedClassifier();

    private static final EReference SEQUENCE_NODE_EXECUTABLE_NODE = UML.getSequenceNode_ExecutableNode();

    private static final EReference STRUCTURED_ACTIVITY_NODE_NODE = UML.getStructuredActivityNode_Node();

    private static final String ACTIVITY_LABEL = "ActivityContainer";

    private static final String ACTIVITY_PARTITION_LABEL = "ActivityPartitionContainer";

    private static final String ACTIVITY_PARTITION_SOURCE_LABEL = "ActivityPartitionSource";

    private static final String CONDITIONAL_NODE_LABEL = "ConditionalNodeContainer";

    private static final String CONDITIONAL_NODE_SOURCE_LABEL = "ConditionalNodeSource";

    private static final String INTERRUPTIBLE_ACTIVITY_REGION_LABEL = "InterruptibleActivityRegionContainer";

    private static final String INTERRUPTIBLE_ACTIVITY_REGION_SOURCE_LABEL = "InterruptibleActivityRegionSource";

    private static final String EXPANSION_REGION_LABEL = "ExpansionRegionContainer";

    private static final String EXPANSION_REGION_SOURCE_LABEL = "ExpansionRegionSource";

    private static final String LOOP_NODE_LABEL = "LoopNodeContainer";

    private static final String LOOP_NODE_SOURCE_LABEL = "LoopNodeSource";

    private static final String SEQUENCE_NODE_LABEL = "SequenceNodeContainer";

    private static final String SEQUENCE_NODE_SOURCE_LABEL = "SequenceNodeSource";

    private static final String STRUCTURED_ACTIVITY_NODE_LABEL = "StructuredActivityNodeContainer";

    private static final String STRUCTURED_ACTIVITY_NODE_SOURCE_LABEL = "StructuredActivityNodeSource";

    private String rootActivityId;

    public ADGraphicalDropTest() {
        super(DEFAULT_DOCUMENT, ADDiagramDescriptionBuilder.AD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> activityDropParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()), UML.getActivityPartition(), ACTIVITY_OWNED_GROUP),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), ACTIVITY_OWNED_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), ACTIVITY_OWNED_NODE),
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

    private static Stream<Arguments> activityPartitionDropParameters() {
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

    private static Stream<Arguments> conditionalNodeDropParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint(), NAMESPACE_OWNED_RULE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()), UML.getReadSelfAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()), UML.getReadStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()), UML.getSendObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()), UML.getSendSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), UML.getSequenceNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), UML.getStructuredActivityNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE));
    }

    private static Stream<Arguments> expansionRegionDropParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint(), NAMESPACE_OWNED_RULE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()), UML.getReadIsClassifiedObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
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
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE));
    }

    private static Stream<Arguments> interruptibleActivityRegionDropParameters() {
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

    private static Stream<Arguments> loopNodeAndStructuredActivityNodeDropParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()), UML.getActivityFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint(), NAMESPACE_OWNED_RULE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()), UML.getDecisionNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()), UML.getFlowFinalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()), UML.getForkNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()), UML.getInitialNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()), UML.getJoinNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()), UML.getMergeNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()), UML.getReadExtentAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()), UML.getReadIsClassifiedObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
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
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE));
    }

    private static Stream<Arguments> sequenceNodeDropParameters() {
        return Stream.of(//
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()), UML.getAcceptCallAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()), UML.getAcceptEventAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()), UML.getAddStructuralFeatureValueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()), UML.getBroadcastSignalAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()), UML.getCallBehaviorAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()), UML.getCallOperationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()), UML.getClearAssociationAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()), UML.getClearStructuralFeatureAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), UML.getConditionalNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getConstraint()), UML.getConstraint(), NAMESPACE_OWNED_RULE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()), UML.getCreateObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()), UML.getDestroyObjectAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), UML.getExpansionRegion(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), UML.getLoopNode(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()), UML.getOpaqueAction(), STRUCTURED_ACTIVITY_NODE_NODE),
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
                Arguments.of(new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()), UML.getTestIdentityAction(), STRUCTURED_ACTIVITY_NODE_NODE),
                Arguments.of(new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()), UML.getValueSpecificationAction(), STRUCTURED_ACTIVITY_NODE_NODE));
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_ACTIVITY, UML.getActivity());
        this.rootActivityId = this.findGraphicalElementContentByLabel(ROOT_ACTIVITY).getId();
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivity()), ACTIVITY_LABEL);
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()), ACTIVITY_PARTITION_LABEL);
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), CONDITIONAL_NODE_LABEL);
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), EXPANSION_REGION_LABEL);
        Node interruptibleActivityRegionNode = this.createNode(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getInterruptibleActivityRegion()));
        // InterruptibleActivityRegions don't have a label, so we need to rename their semantic object
        Object interruptibleActivityRegionObject = this.getObjectService().getObject(this.getEditingContext(), interruptibleActivityRegionNode.getTargetObjectId()).orElse(null);
        if (interruptibleActivityRegionObject instanceof InterruptibleActivityRegion interruptibleActivityRegion) {
            interruptibleActivityRegion.setName(INTERRUPTIBLE_ACTIVITY_REGION_LABEL);
        }
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), LOOP_NODE_LABEL);
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), SEQUENCE_NODE_LABEL);
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), STRUCTURED_ACTIVITY_NODE_LABEL);
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("activityDropParameters")
    public void testDropOnActivity(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        final Node nodeToDrop;
        if (UMLPackage.eINSTANCE.getInterruptibleActivityRegion().equals(expectedType)) {
            nodeToDrop = this.createNode(this.rootActivityId, nodeCreationTool);
        } else {
            nodeToDrop = this.createNodeWithLabel(this.rootActivityId, nodeCreationTool, expectedType.getName() + DROP_SUFFIX);
        }
        HolderNodeGraphicalDnDGraphicalChecker graphicalChecker = new HolderNodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(ACTIVITY_LABEL),
                ADMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(ACTIVITY_LABEL), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(ROOT_ACTIVITY), containmentReference);

        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodeToDrop, ACTIVITY_LABEL, checker);
    }

    @ParameterizedTest
    @MethodSource("activityPartitionDropParameters")
    public void testDropOnActivityPartition(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()),
                ACTIVITY_PARTITION_SOURCE_LABEL);
        Node parentActivityPartitionNode = (Node) this.findGraphicalElementContentByLabel(ACTIVITY_PARTITION_SOURCE_LABEL);

        Node nodeToDrop = this.createNodeWithLabel(parentActivityPartitionNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);
        HolderNodeGraphicalDnDGraphicalChecker graphicalChecker = new HolderNodeGraphicalDnDGraphicalChecker(this::getDiagram,
                () -> this.findGraphicalElementContentByLabel(ACTIVITY_PARTITION_LABEL),
                ADMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());

        final String semanticOwnerLabel;
        if (UML.getActivityPartition().equals(expectedType)) {
            semanticOwnerLabel = ACTIVITY_PARTITION_LABEL;
        } else {
            semanticOwnerLabel = ROOT_ACTIVITY;
        }

        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(semanticOwnerLabel), containmentReference);
        // No semantic deletion check: the elements are still in the activity root.
        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker);
        this.graphicalDropOnContainer(nodeToDrop, ACTIVITY_PARTITION_LABEL, checker);
    }

    @ParameterizedTest
    @MethodSource("conditionalNodeDropParameters")
    public void testDropOnConditionalNode(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()),
                CONDITIONAL_NODE_SOURCE_LABEL);
        Node parentConditionalNode = (Node) this.findGraphicalElementContentByLabel(CONDITIONAL_NODE_SOURCE_LABEL);

        Node nodeToDrop = this.createNodeWithLabel(parentConditionalNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);
        HolderNodeGraphicalDnDGraphicalChecker graphicalChecker = new HolderNodeGraphicalDnDGraphicalChecker(this::getDiagram,
                () -> this.findGraphicalElementContentByLabel(CONDITIONAL_NODE_LABEL),
                ADMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(CONDITIONAL_NODE_LABEL), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(CONDITIONAL_NODE_SOURCE_LABEL), containmentReference);
        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodeToDrop, CONDITIONAL_NODE_LABEL, checker);
    }

    @ParameterizedTest
    @MethodSource("expansionRegionDropParameters")
    public void testDropOnExpansionRegion(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()),
                EXPANSION_REGION_SOURCE_LABEL);
        Node parentExpansionRegionNode = (Node) this.findGraphicalElementContentByLabel(EXPANSION_REGION_SOURCE_LABEL);

        Node nodeToDrop = this.createNodeWithLabel(parentExpansionRegionNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);
        HolderNodeGraphicalDnDGraphicalChecker graphicalChecker = new HolderNodeGraphicalDnDGraphicalChecker(this::getDiagram,
                () -> this.findGraphicalElementContentByLabel(EXPANSION_REGION_LABEL),
                ADMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(EXPANSION_REGION_LABEL), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(EXPANSION_REGION_SOURCE_LABEL), containmentReference);
        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodeToDrop, EXPANSION_REGION_LABEL, checker);
    }

    @ParameterizedTest
    @MethodSource("interruptibleActivityRegionDropParameters")
    public void testDropOnInterruptibleActivityRegion(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        Node parentInterruptibleActivityRegionNodeHolder = this.createNode(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getInterruptibleActivityRegion()));
        Node parentInterruptibleActivityRegionNode = this.getSubNode(parentInterruptibleActivityRegionNodeHolder, ADMappingTypes.getMappingTypeContentAsSubNode(UML.getInterruptibleActivityRegion()));

        Object parentObject = this.getObjectService().getObject(this.getEditingContext(), parentInterruptibleActivityRegionNode.getTargetObjectId()).orElse(null);
        if (parentObject instanceof InterruptibleActivityRegion parentInterruptibleActivityRegion) {
            parentInterruptibleActivityRegion.setName(INTERRUPTIBLE_ACTIVITY_REGION_SOURCE_LABEL);
        }
        Node nodeToDrop = this.createNodeWithLabel(parentInterruptibleActivityRegionNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);
        HolderNodeGraphicalDnDGraphicalChecker graphicalChecker = new HolderNodeGraphicalDnDGraphicalChecker(this::getDiagram,
                () -> this.findGraphicalElementContentByLabel(INTERRUPTIBLE_ACTIVITY_REGION_LABEL),
                ADMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());

        final String semanticOwnerLabel;
        if (UML.getActivityPartition().equals(expectedType)) {
            semanticOwnerLabel = INTERRUPTIBLE_ACTIVITY_REGION_LABEL;
        } else {
            semanticOwnerLabel = ROOT_ACTIVITY;
        }

        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(semanticOwnerLabel), containmentReference);
        // No semantic deletion check: the elements are still in the activity root.
        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker);
        this.graphicalDropOnContainer(nodeToDrop, INTERRUPTIBLE_ACTIVITY_REGION_LABEL, checker);
    }

    @ParameterizedTest
    @MethodSource("loopNodeAndStructuredActivityNodeDropParameters")
    public void testDropOnLoopNode(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()),
                LOOP_NODE_SOURCE_LABEL);
        Node parentLoopNode = (Node) this.findGraphicalElementContentByLabel(LOOP_NODE_SOURCE_LABEL);

        Node nodeToDrop = this.createNodeWithLabel(parentLoopNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);
        HolderNodeGraphicalDnDGraphicalChecker graphicalChecker = new HolderNodeGraphicalDnDGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(LOOP_NODE_LABEL),
                ADMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(LOOP_NODE_LABEL), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(LOOP_NODE_SOURCE_LABEL), containmentReference);
        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodeToDrop, LOOP_NODE_LABEL, checker);
    }

    @ParameterizedTest
    @MethodSource("sequenceNodeDropParameters")
    public void testDropOnSequenceNode(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()),
                SEQUENCE_NODE_SOURCE_LABEL);
        Node parentSequenceNode = (Node) this.findGraphicalElementContentByLabel(SEQUENCE_NODE_SOURCE_LABEL);

        Node nodeToDrop = this.createNodeWithLabel(parentSequenceNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);
        HolderNodeGraphicalDnDGraphicalChecker graphicalChecker = new HolderNodeGraphicalDnDGraphicalChecker(this::getDiagram,
                () -> this.findGraphicalElementContentByLabel(SEQUENCE_NODE_LABEL),
                ADMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(SEQUENCE_NODE_LABEL), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(SEQUENCE_NODE_SOURCE_LABEL), containmentReference);
        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodeToDrop, SEQUENCE_NODE_LABEL, checker);
    }

    @ParameterizedTest
    @MethodSource("loopNodeAndStructuredActivityNodeDropParameters")
    public void testDropOnStructuredActivityNode(CreationTool nodeCreationTool, EClass expectedType, EReference containmentReference) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()),
                STRUCTURED_ACTIVITY_NODE_SOURCE_LABEL);
        Node parentLoopNode = (Node) this.findGraphicalElementContentByLabel(STRUCTURED_ACTIVITY_NODE_SOURCE_LABEL);
        Node nodeToDrop = this.createNodeWithLabel(parentLoopNode.getId(), nodeCreationTool, expectedType.getName() + DROP_SUFFIX);
        HolderNodeGraphicalDnDGraphicalChecker graphicalChecker = new HolderNodeGraphicalDnDGraphicalChecker(this::getDiagram,
                () -> this.findGraphicalElementContentByLabel(STRUCTURED_ACTIVITY_NODE_LABEL),
                ADMappingTypes.getMappingTypeAsSubNode(expectedType), this.getCapturedNodes());
        NodeCreationSemanticChecker semanticCreationChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType,
                () -> this.findSemanticElementByName(STRUCTURED_ACTIVITY_NODE_LABEL), containmentReference);
        NodeSemanticDeletionSemanticChecker semanticDeletionChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext,
                () -> this.findSemanticElementByName(STRUCTURED_ACTIVITY_NODE_SOURCE_LABEL), containmentReference);
        CombinedChecker checker = new CombinedChecker(graphicalChecker, semanticCreationChecker, semanticDeletionChecker);
        this.graphicalDropOnContainer(nodeToDrop, STRUCTURED_ACTIVITY_NODE_LABEL, checker);
    }

    @Override
    protected Node createNodeWithLabel(String parentElementId, CreationTool creationTool, String label) {
        this.applyNodeCreationTool(parentElementId, creationTool);
        final String createdNodeLabel;
        if (UML.getReadSelfAction().equals(creationTool.getToolEClass())) {
            createdNodeLabel = "this";
        } else {
            createdNodeLabel = creationTool.getToolEClass().getName() + "1";
        }
        Node createdNode = (Node) this.findGraphicalElementExcludingContentByLabel(createdNodeLabel);
        if (createdNode.getInsideLabel() != null) {
            this.applyEditLabelTool(createdNode.getInsideLabel().getId(), label);
        } else if (createdNode.getOutsideLabels() != null && !createdNode.getOutsideLabels().isEmpty()) {
            this.applyEditLabelTool(createdNode.getOutsideLabels().get(0).id(), label);
        } else {
            fail("No label found for element " + createdNode.getId());
        }
        // Reload the node to ensure that the new label is present
        return (Node) this.findGraphicalElementById(createdNode.getId());
    }
}

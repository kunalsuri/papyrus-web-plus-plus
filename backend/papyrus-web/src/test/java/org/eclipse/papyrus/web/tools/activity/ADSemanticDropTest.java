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

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.ADDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.activity.utils.ADCreationTool;
import org.eclipse.papyrus.web.tools.activity.utils.ADMappingTypes;
import org.eclipse.papyrus.web.tools.activity.utils.ADToolSections;
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.SemanticDropTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.uml2.uml.ActivityGroup;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityPartition;
import org.eclipse.uml2.uml.ExpansionNode;
import org.eclipse.uml2.uml.ExpansionRegion;
import org.eclipse.uml2.uml.InterruptibleActivityRegion;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Pin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests semantic drop tools in the Activity Diagram.
 * <p>
 * This class doesn't test the semantic drag & drop of {@link Pin} elements since these elements are synchronized, and
 * will always be displayed on the diagram if their container is visible.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ADSemanticDropTest extends SemanticDropTest {

    private static final String ROOT_ACTIVITY = "rootActivity";

    private static final EReference ACTIVITY_PARTITION = UML.getActivity_Partition();

    private static final EReference ACTIVITY_GROUP = UML.getActivity_OwnedGroup();

    private static final EReference ACTIVITY_NODE = UML.getActivity_OwnedNode();

    private static final EReference ACTIVITY_PARTITION_SUB_PARTITION = UML.getActivityPartition_Subpartition();

    private static final EReference ACTIVITY_STRUCTURED_NODE = UML.getActivity_StructuredNode();

    private static final EReference NAMESPACE_OWNED_RULE = UML.getNamespace_OwnedRule();

    private static final EReference CLASS_NESTED_CLASSIFIER = UML.getClass_NestedClassifier();

    private static final EReference SEQUENCE_NODE_EXECUTABLE_NODE = UML.getSequenceNode_ExecutableNode();

    private static final EReference STRUCTURED_ACTIVITY_NODE_NODE = UML.getStructuredActivityNode_Node();

    private static final String ACTIVITY_PARTITION_LABEL = "ActivityPartitionContainer";

    private static final String CONDITIONAL_NODE_LABEL = "ConditionalNodeContainer";

    private static final String INTERRUPTIBLE_ACTIVITY_REGION_LABEL = "InterruptibleActivityRegion1";

    private static final String EXPANSION_REGION_LABEL = "ExpansionRegionContainer";

    private static final String LOOP_NODE_LABEL = "LoopNodeContainer";

    private static final String SEQUENCE_NODE_LABEL = "SequenceNodeContainer";

    private static final String STRUCTURED_ACTIVITY_NODE_LABEL = "StructuredActivityNodeContainer";

    private static final String DROP_SUFFIX = "Drop";

    private String rootActivityId;

    public ADSemanticDropTest() {
        super(DEFAULT_DOCUMENT, ADDiagramDescriptionBuilder.AD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> dropOnActivityParameters() {
        return Stream.of(//
                Arguments.of(ACTIVITY_NODE, UML.getAcceptCallAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getAcceptEventAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getActivityFinalNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getActivityParameterNode()), //
                Arguments.of(ACTIVITY_PARTITION, UML.getActivityPartition()), //
                Arguments.of(ACTIVITY_NODE, UML.getAddStructuralFeatureValueAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getBroadcastSignalAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getCallBehaviorAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getCallOperationAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getClearAssociationAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getClearStructuralFeatureAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getConditionalNode()), //
                Arguments.of(NAMESPACE_OWNED_RULE, UML.getConstraint()), //
                Arguments.of(ACTIVITY_NODE, UML.getCreateObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getDecisionNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getDestroyObjectAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getExpansionRegion()), //
                Arguments.of(ACTIVITY_NODE, UML.getFlowFinalNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getForkNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getInitialNode()), //
                Arguments.of(ACTIVITY_GROUP, UML.getInterruptibleActivityRegion()), //
                Arguments.of(ACTIVITY_NODE, UML.getJoinNode()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getLoopNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getMergeNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getOpaqueAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadExtentAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadIsClassifiedObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadSelfAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadStructuralFeatureAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReclassifyObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReduceAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getSendObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getSendSignalAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getSequenceNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getStartClassifierBehaviorAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getStartObjectBehaviorAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getStructuredActivityNode()), //
                Arguments.of(CLASS_NESTED_CLASSIFIER, UML.getActivity()), //
                Arguments.of(ACTIVITY_NODE, UML.getTestIdentityAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getValueSpecificationAction()) //
        );
    }

    private static Stream<Arguments> dropOnActivityPartitionParameters() {
        return Stream.of(//
                Arguments.of(ACTIVITY_NODE, UML.getAcceptCallAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getAcceptEventAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getActivityFinalNode()), //
                Arguments.of(ACTIVITY_PARTITION_SUB_PARTITION, UML.getActivityPartition()), //
                Arguments.of(ACTIVITY_NODE, UML.getAddStructuralFeatureValueAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getBroadcastSignalAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getCallBehaviorAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getCallOperationAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getClearAssociationAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getClearStructuralFeatureAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getConditionalNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getCreateObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getDecisionNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getDestroyObjectAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getExpansionRegion()), //
                Arguments.of(ACTIVITY_NODE, UML.getFlowFinalNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getForkNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getInitialNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getJoinNode()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getLoopNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getMergeNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getOpaqueAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadExtentAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadIsClassifiedObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadSelfAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadStructuralFeatureAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReclassifyObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReduceAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getSendObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getSendSignalAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getSequenceNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getStartClassifierBehaviorAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getStartObjectBehaviorAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getStructuredActivityNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getTestIdentityAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getValueSpecificationAction()) //
        );
    }

    private static Stream<Arguments> dropOnConditionalNodeParameters() {
        return Stream.of(//
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getAcceptCallAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getAcceptEventAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getActivityFinalNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getAddStructuralFeatureValueAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getCallBehaviorAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getCallOperationAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getConditionalNode()), //
                Arguments.of(NAMESPACE_OWNED_RULE, UML.getConstraint()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getCreateObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getDecisionNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getDestroyObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getExpansionRegion()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getFlowFinalNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getForkNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getInitialNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getJoinNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getLoopNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getMergeNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getOpaqueAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReadSelfAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReadStructuralFeatureAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getSendObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getSendSignalAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getSequenceNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getStructuredActivityNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getValueSpecificationAction()) //
        );
    }

    private static Stream<Arguments> dropOnExpansionRegionParameters() {
        return Stream.of(//
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getAcceptCallAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getAcceptEventAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getActivityFinalNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getAddStructuralFeatureValueAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getBroadcastSignalAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getCallBehaviorAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getCallOperationAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getClearAssociationAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getClearStructuralFeatureAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getConditionalNode()), //
                Arguments.of(NAMESPACE_OWNED_RULE, UML.getConstraint()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getCreateObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getDecisionNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getDestroyObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getExpansionRegion()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getFlowFinalNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getForkNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getInitialNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getExpansionNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getJoinNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getLoopNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getMergeNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getOpaqueAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReadSelfAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReadStructuralFeatureAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReclassifyObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReduceAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getSendObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getSendSignalAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getSequenceNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getStartClassifierBehaviorAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getStartObjectBehaviorAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getStructuredActivityNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getTestIdentityAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getValueSpecificationAction()) //
        );
    }

    private static Stream<Arguments> dropOnInterruptibleActivityRegionParameters() {
        return Stream.of(//
                Arguments.of(ACTIVITY_NODE, UML.getAcceptCallAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getAcceptEventAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getActivityFinalNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getAddStructuralFeatureValueAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getBroadcastSignalAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getCallBehaviorAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getCallOperationAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getClearAssociationAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getClearStructuralFeatureAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getConditionalNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getCreateObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getDecisionNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getDestroyObjectAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getExpansionRegion()), //
                Arguments.of(ACTIVITY_NODE, UML.getFlowFinalNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getForkNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getInitialNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getJoinNode()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getLoopNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getMergeNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getOpaqueAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadExtentAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadIsClassifiedObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadSelfAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReadStructuralFeatureAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReclassifyObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getReduceAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getSendObjectAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getSendSignalAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getSequenceNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getStartClassifierBehaviorAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getStartObjectBehaviorAction()), //
                Arguments.of(ACTIVITY_STRUCTURED_NODE, UML.getStructuredActivityNode()), //
                Arguments.of(ACTIVITY_NODE, UML.getTestIdentityAction()), //
                Arguments.of(ACTIVITY_NODE, UML.getValueSpecificationAction()) //
        );
    }

    private static Stream<Arguments> dropOnLoopNodeAndStructuredActivityNodeParameters() {
        return Stream.of(//
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getAcceptCallAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getAcceptEventAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getActivityFinalNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getAddStructuralFeatureValueAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getBroadcastSignalAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getCallBehaviorAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getCallOperationAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getClearAssociationAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getClearStructuralFeatureAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getConditionalNode()), //
                Arguments.of(NAMESPACE_OWNED_RULE, UML.getConstraint()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getCreateObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getDecisionNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getDestroyObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getExpansionRegion()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getFlowFinalNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getForkNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getInitialNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getJoinNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getLoopNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getMergeNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getOpaqueAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReadSelfAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReadStructuralFeatureAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReclassifyObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getReduceAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getSendObjectAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getSendSignalAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getSequenceNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getStartClassifierBehaviorAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getStartObjectBehaviorAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getStructuredActivityNode()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getTestIdentityAction()), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE_NODE, UML.getValueSpecificationAction()) //
        );
    }

    private static Stream<Arguments> dropOnSequenceNodeParameters() {
        return Stream.of(//
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getAcceptCallAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getAcceptEventAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getAddStructuralFeatureValueAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getBroadcastSignalAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getCallBehaviorAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getCallOperationAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getClearAssociationAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getClearStructuralFeatureAction()), //
                Arguments.of(NAMESPACE_OWNED_RULE, UML.getConstraint()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getDestroyObjectAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getOpaqueAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getReadSelfAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getReadStructuralFeatureAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getReclassifyObjectAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getReduceAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getSendObjectAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getSendSignalAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getSequenceNode()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getStructuredActivityNode()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getTestIdentityAction()), //
                Arguments.of(SEQUENCE_NODE_EXECUTABLE_NODE, UML.getValueSpecificationAction()) //
        );
    }

    private static Stream<Arguments> dropControlFlowParameters() {
        List<CreationTool> sources = List.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()));
        List<CreationTool> targets = List.of(
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()),
                new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()),
                new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()),
                new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()));
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> dropObjectFlowParameters() {
        List<CreationTool> sources = List.of(new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()));
        List<CreationTool> targets = List.of(
                new ADCreationTool(ADToolSections.NODES, UML.getActivityParameterNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()),
                new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()),
                new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionNode()));
        return cartesianProduct(sources, targets);
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUpWithIntermediateRoot(ROOT_ACTIVITY, UML.getActivity());
        this.rootActivityId = this.findGraphicalElementContentByLabel(ROOT_ACTIVITY).getId();
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest(name = "[{index}] Drop Node {1} on Activity")
    @MethodSource("dropOnActivityParameters")
    public void testSemanticDropOnActivity(EReference containmentReference, EClass elementType) {
        EObject parentElement = this.findSemanticElementByName(ROOT_ACTIVITY);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker = this.getCreationChecker(elementType, ADMappingTypes.getMappingTypeAsSubNode(elementType), ROOT_ACTIVITY);
        if (elementType.isSuperTypeOf(UML.getActivityParameterNode())) {
            this.semanticDropOnHolder(ROOT_ACTIVITY, this.getIdentityService().getId(elementToDrop), graphicalChecker);
        } else {
            this.semanticDropOnContent(ROOT_ACTIVITY, this.getIdentityService().getId(elementToDrop), graphicalChecker);
        }
    }

    @ParameterizedTest(name = "[{index}] Drop Node {1} on ActivityPartition")
    @MethodSource("dropOnActivityPartitionParameters")
    public void testSemanticDropOnActivityPartition(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()), ACTIVITY_PARTITION_LABEL);
        EObject activity = this.findSemanticElementByName(ROOT_ACTIVITY);
        ActivityPartition activityPartition = (ActivityPartition) this.findSemanticElementByName(ACTIVITY_PARTITION_LABEL);
        EObject elementToDrop;
        if (UML.getActivityPartition().equals(elementType)) {
            // The semantic parent of an ActivityPartition in a partition is the enclosing partition
            elementToDrop = this.createSemanticElement(activityPartition, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        } else {
            // The semantic parent of an ActivityNode in a partition is the enclosing Activity
            elementToDrop = this.createSemanticElementInActivityGroup(activity, containmentReference, elementType, elementType.getName() + DROP_SUFFIX, activityPartition);
        }
        NodeCreationGraphicalChecker graphicalChecker = this.getCreationChecker(elementType, ADMappingTypes.getMappingTypeAsSubNode(elementType), ACTIVITY_PARTITION_LABEL);
        this.semanticDropOnContent(ACTIVITY_PARTITION_LABEL, this.getIdentityService().getId(elementToDrop),
                graphicalChecker);
    }

    /**
     * @param elementType
     * @return
     */
    private NodeCreationGraphicalChecker getCreationChecker(EClass elementType, String mapping, String label) {
        NodeCreationGraphicalChecker graphicalChecker;
        if (ADMappingTypes.isHolderContent(elementType)) {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(label),
                    mapping, this.getCapturedNodes());
        } else {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(label),
                    mapping, this.getCapturedNodes());
        }
        return graphicalChecker;
    }

    @ParameterizedTest(name = "[{index}] Drop Node {1} on ConditionalNode")
    @MethodSource("dropOnConditionalNodeParameters")
    public void testSemanticDropOnConditionalNode(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()), CONDITIONAL_NODE_LABEL);
        EObject semanticContainerElement = this.findSemanticElementByName(CONDITIONAL_NODE_LABEL);
        EObject elementToDrop = this.createSemanticElement(semanticContainerElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker = this.getCreationChecker(elementType, ADMappingTypes.getMappingTypeAsSubNode(elementType), CONDITIONAL_NODE_LABEL);
        this.semanticDropOnContent(CONDITIONAL_NODE_LABEL, this.getIdentityService().getId(elementToDrop),
                graphicalChecker);
    }

    @ParameterizedTest(name = "[{index}] Drop Node {1} on ExpansionRegion")
    @MethodSource("dropOnExpansionRegionParameters")
    public void testSemanticDropOnExpansionRegion(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), EXPANSION_REGION_LABEL);
        EObject semanticContainerElement = this.findSemanticElementByName(EXPANSION_REGION_LABEL);
        EObject elemenToDrop = this.createSemanticElement(semanticContainerElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        String expectedMapping;
        if (UML.getExpansionNode().equals(elementType)) {
            expectedMapping = ADMappingTypes.getMappingType(elementType);
        } else {
            expectedMapping = ADMappingTypes.getMappingTypeAsSubNode(elementType);
        }
        NodeCreationGraphicalChecker graphicalChecker = this.getCreationChecker(elementType, expectedMapping, EXPANSION_REGION_LABEL);
        if (elementType.isSuperTypeOf(UML.getExpansionNode())) {
            this.semanticDropOnHolder(EXPANSION_REGION_LABEL, this.getIdentityService().getId(elemenToDrop),
                    graphicalChecker);
        } else {
            this.semanticDropOnContent(EXPANSION_REGION_LABEL, this.getIdentityService().getId(elemenToDrop),
                    graphicalChecker);
        }
    }

    @ParameterizedTest(name = "[{index}] Drop Node {1} on InterruptibleActivityRegion")
    @MethodSource("dropOnInterruptibleActivityRegionParameters")
    public void testSemanticDropOnInterruptibleActivityRegion(EReference containmentReference, EClass elementType) {
        this.createNode(this.rootActivityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getInterruptibleActivityRegion()));
        EObject activity = this.findSemanticElementByName(ROOT_ACTIVITY);
        InterruptibleActivityRegion interruptibleActivityRegion = (InterruptibleActivityRegion) this.findSemanticElementByName(INTERRUPTIBLE_ACTIVITY_REGION_LABEL);
        // The semantic parent of an ActivityNode in a partition is the enclosing Activity
        EObject elementToDrop = this.createSemanticElementInActivityGroup(activity, containmentReference, elementType, elementType.getName() + DROP_SUFFIX, interruptibleActivityRegion);
        NodeCreationGraphicalChecker graphicalChecker = this.getCreationChecker(elementType, ADMappingTypes.getMappingTypeAsSubNode(elementType), INTERRUPTIBLE_ACTIVITY_REGION_LABEL);
        this.semanticDropOnContent(INTERRUPTIBLE_ACTIVITY_REGION_LABEL, this.getIdentityService().getId(elementToDrop),
                graphicalChecker);
    }

    @ParameterizedTest(name = "[{index}] Drop Node {1} on LoopNode")
    @MethodSource("dropOnLoopNodeAndStructuredActivityNodeParameters")
    public void testSemanticDropOnLoopNode(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()), LOOP_NODE_LABEL);
        EObject semanticContainerElement = this.findSemanticElementByName(LOOP_NODE_LABEL);
        EObject elementToDrop = this.createSemanticElement(semanticContainerElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker = this.getCreationChecker(elementType, ADMappingTypes.getMappingTypeAsSubNode(elementType), LOOP_NODE_LABEL);
        this.semanticDropOnContent(LOOP_NODE_LABEL, this.getIdentityService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest(name = "[{index}] Drop Node {1} on StructuredActivityNode")
    @MethodSource("dropOnLoopNodeAndStructuredActivityNodeParameters")
    public void testSemanticDropOnStructuredActivityNode(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()), STRUCTURED_ACTIVITY_NODE_LABEL);
        EObject semanticContainerElement = this.findSemanticElementByName(STRUCTURED_ACTIVITY_NODE_LABEL);
        EObject elementToDrop = this.createSemanticElement(semanticContainerElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker = this.getCreationChecker(elementType, ADMappingTypes.getMappingTypeAsSubNode(elementType), STRUCTURED_ACTIVITY_NODE_LABEL);
        this.semanticDropOnContent(STRUCTURED_ACTIVITY_NODE_LABEL, this.getIdentityService().getId(elementToDrop),
                graphicalChecker);
    }

    @ParameterizedTest(name = "[{index}] Drop Node {1} on SequenceNode")
    @MethodSource("dropOnSequenceNodeParameters")
    public void testSemanticDropOnSequenceNode(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()), SEQUENCE_NODE_LABEL);
        EObject semanticContainerElement = this.findSemanticElementByName(SEQUENCE_NODE_LABEL);
        EObject elementToDrop = this.createSemanticElement(semanticContainerElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker = this.getCreationChecker(elementType, ADMappingTypes.getMappingTypeAsSubNode(elementType), SEQUENCE_NODE_LABEL);
        this.semanticDropOnContent(SEQUENCE_NODE_LABEL, this.getIdentityService().getId(elementToDrop),
                graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropControlFlowParameters")
    public void testSemanticDropControlFlow(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnContainers(sourceCreationTool, ROOT_ACTIVITY, targetCreationTool, ROOT_ACTIVITY, new CreationTool(ToolSections.EDGES, UML.getControlFlow()), ROOT_ACTIVITY,
                ADMappingTypes.getMappingType(UML.getControlFlow()));
    }

    @ParameterizedTest
    @MethodSource("dropObjectFlowParameters")
    public void testSemanticDropObjectFlow(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        final String targetContainerLabel;
        if (UML.getExpansionNode().equals(targetCreationTool.getToolEClass())) {
            targetContainerLabel = EXPANSION_REGION_LABEL;
            this.createNodeWithLabel(this.rootActivityId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()), EXPANSION_REGION_LABEL);
        } else {
            targetContainerLabel = ROOT_ACTIVITY;
        }
        this.edgeSemanticDropOnContainers(sourceCreationTool, ROOT_ACTIVITY, targetCreationTool, targetContainerLabel, new CreationTool(ToolSections.EDGES, UML.getObjectFlow()), ROOT_ACTIVITY,
                ADMappingTypes.getMappingType(UML.getObjectFlow()));
    }

    /**
     * Creates a semantic element of the given {@code type} in the given {@code parentElement}, and adds it to the
     * provided {@code activityGroup}.
     * <p>
     * This method is similar to {@link #createSemanticElement(EObject, EReference, EClass, String)}, but it also adds
     * the created element to the provided {@code activityGroup}.
     * </p>
     *
     * @param parentElement
     *            the semantic element containing the created element
     * @param containmentReference
     *            the reference containing the created element
     * @param type
     *            the type of the created element
     * @param name
     *            the name of the created element
     * @param activityGroup
     *            the activity group to set for the created element
     * @return the created element
     */
    protected EObject createSemanticElementInActivityGroup(EObject parentElement, EReference containmentReference, EClass type, String name, ActivityGroup activityGroup) {
        String parentElementId = this.getIdentityService().getId(parentElement);
        int numberOfChildren = ((List<?>) parentElement.eGet(containmentReference)).size();
        this.applyCreateChildTool(parentElementId, containmentReference, type);
        IEditingContext editingContext = this.getEditingContext();
        EObject updatedParentElement = (EObject) this.getObjectSearchService()
                .getObject(editingContext, parentElementId).get();
        EObject createdObject = (EObject) ((List<?>) updatedParentElement.eGet(containmentReference)).get(numberOfChildren);
        if (createdObject instanceof ActivityNode activityNode) {
            activityNode.setName(name);
            if (activityGroup instanceof ActivityPartition activityPartition) {
                activityPartition.getNodes().add(activityNode);
            } else if (activityGroup instanceof InterruptibleActivityRegion interruptibleActivityRegion) {
                interruptibleActivityRegion.getNodes().add(activityNode);
            }
            this.persistenceService.persist(new ICause.NoOp(), editingContext);
            this.editingContextEventProcessorRegistry.disposeEditingContextEventProcessor(editingContext.getId());
            this.diagramEventSubscriptionRunner.createSubscription(this.editingContextId, this.representationId);

        } else {
            fail(MessageFormat.format("Cannot create a {0} in ActivityPartition", type.getName()));
        }
        return createdObject;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method also handles the creation of {@link ExpansionNode} inside {@link ExpansionRegion}: the created
     * {@link ExpansionNode#setRegionAsInput(ExpansionRegion)} is set with the {@link ExpansionRegion} to enable its
     * semantic drag & drop.
     * </p>
     */
    @Override
    protected EObject createSemanticElement(EObject parentElement, EReference containmentReference, EClass type, String name) {
        String parentElementId = this.getIdentityService().getId(parentElement);
        int numberOfChildren = ((List<?>) parentElement.eGet(containmentReference)).size();
        this.applyCreateChildTool(parentElementId, containmentReference, type);
        IEditingContext editingContext = this.getEditingContext();
        EObject updatedParentElement = (EObject) this.getObjectSearchService()
                .getObject(editingContext, parentElementId).get();
        EObject createdObject = (EObject) ((List<?>) updatedParentElement.eGet(containmentReference)).get(numberOfChildren);
        if (createdObject instanceof NamedElement namedElement) {
            namedElement.setName(name);
            if (createdObject instanceof ExpansionNode expansionNode && parentElement instanceof ExpansionRegion expansionRegion) {
                // ExpansionNode created inside an ExpansionRegion need to define either regionAsInput or regionAsOutput
                // to be drag & droppable
                expansionNode.setRegionAsInput(expansionRegion);
            }
            this.persistenceService.persist(new ICause.NoOp(), editingContext);
            if (createdObject instanceof ExpansionNode && parentElement instanceof ExpansionRegion) {
                this.editingContextEventProcessorRegistry.disposeEditingContextEventProcessor(editingContext.getId());
                this.diagramEventSubscriptionRunner.createSubscription(this.editingContextId, this.representationId);
            }
        }
        return createdObject;
    }
}

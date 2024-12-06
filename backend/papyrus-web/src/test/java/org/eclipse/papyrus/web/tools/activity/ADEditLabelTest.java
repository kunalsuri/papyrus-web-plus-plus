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

import static org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters.ST_LEFT;
import static org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters.ST_RIGHT;

import java.util.stream.Stream;

import org.eclipse.papyrus.web.application.representations.uml.ADDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.activity.utils.ADCreationTool;
import org.eclipse.papyrus.web.tools.activity.utils.ADToolSections;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.LabelGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.LabelSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EditLabelTest;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edit label tools in the Activity Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ADEditLabelTest extends EditLabelTest {

    private static final String ROOT_ACTIVITY = "rootActivity";

    private static final String ACCEPT_CALL_ACTION = "AcceptCallAction1";

    private static final String ACCEPT_EVENT_ACTION = "AcceptEventAction1";

    private static final String ACTION_INPUT_PIN = "ActionInputPin1";

    private static final String ACTIVITY_FINAL_NODE = "ActivityFinalNode1";

    private static final String ACTIVITY_PARAMETER_NODE = "ActivityParameterNode1";

    private static final String ACTIVITY_PARTITION = "ActivityPartition1";

    private static final String ADD_STRUCTURAL_FEATURE_VALUE_ACTION = "AddStructuralFeatureValueAction1";

    private static final String BROADCAST_SIGNAL_ACTION = "BroadcastSignalAction1";

    private static final String CALL_BEHAVIOR_ACTION = "CallBehaviorAction1";

    private static final String CALL_OPERATION_ACTION = "CallOperationAction1";

    private static final String CLEAR_ASSOCIATION_ACTION = "ClearAssociationAction1";

    private static final String CLEAR_STRUCTURAL_FEATURE_ACTION = "ClearStructuralFeatureAction1";

    private static final String CONDITIONAL_NODE = "ConditionalNode1";

    private static final String CONSTRAINT = "Constraint1";

    private static final String CREATE_OBJECT_ACTION = "CreateObjectAction1";

    private static final String DECISION_NODE = "DecisionNode1";

    private static final String DESTROY_OBJECT_ACTION = "DestroyObjectAction1";

    private static final String EXPANSION_REGION = "ExpansionRegion1";

    private static final String EXPANSION_NODE = "ExpansionNode1";

    private static final String FLOW_FINAL_NODE = "FlowFinalNode1";

    private static final String FORK_NODE = "ForkNode1";

    private static final String INITIAL_NODE = "InitialNode1";

    private static final String INPUT_PIN_NODE = "InputPin1";

    private static final String JOIN_NODE = "JoinNode1";

    private static final String LOOP_NODE = "LoopNode1";

    private static final String OUTPUT_PIN_NODE = "OutputPin1";

    private static final String MERGE_NODE = "MergeNode1";

    private static final String OPAQUE_ACTION = "OpaqueAction1";

    private static final String READ_EXTENT_ACTION = "ReadExtentAction1";

    private static final String READ_IS_CLASSIFIED_OBJECT_ACTION = "ReadIsClassifiedObjectAction1";

    private static final String READ_SELF_ACTION = "this";

    private static final String READ_STRUCTURAL_FEATURE_ACTION = "ReadStructuralFeatureAction1";

    private static final String RECLASSIFY_OBJECT_ACTION = "ReclassifyObjectAction1";

    private static final String REDUCE_ACTION = "ReduceAction1";

    private static final String SEND_OBJECT_ACTION = "SendObjectAction1";

    private static final String SEND_SIGNAL_ACTION = "SendSignalAction1";

    private static final String SEQUENCE_NODE = "SequenceNode1";

    private static final String START_CLASSIFIER_BEHAVIOR_ACTION = "StartClassifierBehaviorAction1";

    private static final String START_OBJECT_BEHAVIOR_ACTION = "StartObjectBehaviorAction1";

    private static final String STRUCTURED_ACTIVITY_NODE = "StructuredActivityNode1";

    private static final String ACTIVITY = "Activity1";

    private static final String TEST_IDENTITY_ACTION = "TestIdentityAction1";

    private static final String VALUE_PIN_NODE = "ValuePin1";

    private static final String VALUE_SPECIFICATION_ACTION = "ValueSpecificationAction1";

    private static final String NEW_LABEL = "New Label";

    private static final String ACTIVITY_LABEL_PREFIX = ST_LEFT + "activity" + ST_RIGHT + System.lineSeparator();

    private static final String CONDITIONAL_NODE_LABEL = ST_LEFT + "conditional" + ST_RIGHT;

    private static final String CONSTRAINT_LABEL_SUFFIX = System.lineSeparator() + "{{OCL} true}";

    private static final String EXPANSION_REGION_LABEL = ST_LEFT + "iterative" + ST_RIGHT;

    private static final String LOOP_NODE_LABEL = ST_LEFT + "loop node" + ST_RIGHT;

    private static final String SEQUENCE_NODE_LABEL = ST_LEFT + "sequence" + ST_RIGHT;

    private static final String STRUCTURED_ACTIVITY_NODE_LABEL = ST_LEFT + "structured" + ST_RIGHT;

    public ADEditLabelTest() {
        super(DEFAULT_DOCUMENT, ADDiagramDescriptionBuilder.AD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(ROOT_ACTIVITY), //
                Arguments.of(ACCEPT_CALL_ACTION), //
                Arguments.of(ACCEPT_EVENT_ACTION), //
                Arguments.of(ACTION_INPUT_PIN), //
                Arguments.of(ACTIVITY_FINAL_NODE), //
                Arguments.of(ACTIVITY_PARAMETER_NODE), //
                Arguments.of(ACTIVITY_PARTITION), //
                Arguments.of(ADD_STRUCTURAL_FEATURE_VALUE_ACTION), //
                Arguments.of(BROADCAST_SIGNAL_ACTION), //
                Arguments.of(CALL_BEHAVIOR_ACTION), //
                Arguments.of(CALL_OPERATION_ACTION), //
                Arguments.of(CLEAR_ASSOCIATION_ACTION), //
                Arguments.of(CLEAR_STRUCTURAL_FEATURE_ACTION), //
                Arguments.of(CONDITIONAL_NODE), //
                Arguments.of(CONSTRAINT), //
                Arguments.of(CREATE_OBJECT_ACTION), //
                Arguments.of(DECISION_NODE), //
                Arguments.of(DESTROY_OBJECT_ACTION), //
                Arguments.of(EXPANSION_REGION), //
                Arguments.of(EXPANSION_NODE), //
                Arguments.of(FLOW_FINAL_NODE), //
                Arguments.of(FORK_NODE), //
                Arguments.of(INITIAL_NODE), //
                Arguments.of(INPUT_PIN_NODE), //
                Arguments.of(JOIN_NODE), //
                Arguments.of(LOOP_NODE), //
                Arguments.of(OUTPUT_PIN_NODE), //
                Arguments.of(MERGE_NODE), //
                Arguments.of(OPAQUE_ACTION), //
                Arguments.of(READ_EXTENT_ACTION), //
                Arguments.of(READ_IS_CLASSIFIED_OBJECT_ACTION), //
                Arguments.of(READ_SELF_ACTION), //
                Arguments.of(READ_STRUCTURAL_FEATURE_ACTION), //
                Arguments.of(RECLASSIFY_OBJECT_ACTION), //
                Arguments.of(REDUCE_ACTION), //
                Arguments.of(SEND_OBJECT_ACTION), //
                Arguments.of(SEND_SIGNAL_ACTION), //
                Arguments.of(SEQUENCE_NODE), //
                Arguments.of(START_CLASSIFIER_BEHAVIOR_ACTION), //
                Arguments.of(START_OBJECT_BEHAVIOR_ACTION), //
                Arguments.of(STRUCTURED_ACTIVITY_NODE), //
                Arguments.of(ACTIVITY), //
                Arguments.of(TEST_IDENTITY_ACTION), //
                Arguments.of(VALUE_PIN_NODE), //
                Arguments.of(VALUE_SPECIFICATION_ACTION));
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_ACTIVITY, UML.getActivity());
        Node rootActivity = (Node) this.findGraphicalElementContentByLabel(ROOT_ACTIVITY);
        String activityId = rootActivity.getId();
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptCallAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACCEPT_EVENT_ACTION, UML.getAcceptEventAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getActivityFinalNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.NODES, UML.getActivityParameterNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivityPartition()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getAddStructuralFeatureValueAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getBroadcastSignalAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallBehaviorAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getCallOperationAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getClearAssociationAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getClearStructuralFeatureAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getConditionalNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.NODES, UML.getConstraint()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getCreateObjectAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getDecisionNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getDestroyObjectAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionRegion()));
        Node expansionRegion = (Node) this.findGraphicalContentIfExistByLabel(EXPANSION_REGION);
        this.applyNodeCreationTool(expansionRegion.getId(), new ADCreationTool(ADToolSections.EXPANSION_REGION, UML.getExpansionNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getFlowFinalNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getForkNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getInitialNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getJoinNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getLoopNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACTIVITY_NODE, UML.getMergeNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getOpaqueAction()));
        Node opaqueActionNode = (Node) this.findGraphicalContentIfExistByLabel(OPAQUE_ACTION);
        this.applyNodeCreationTool(opaqueActionNode.getId(), new ADCreationTool(ADToolSections.PIN, UML.getActionInputPin()));
        this.applyNodeCreationTool(opaqueActionNode.getId(), new ADCreationTool(ADToolSections.PIN, UML.getInputPin()));
        this.applyNodeCreationTool(opaqueActionNode.getId(), new ADCreationTool(ADToolSections.PIN, UML.getOutputPin()));
        this.applyNodeCreationTool(opaqueActionNode.getId(), new ADCreationTool(ADToolSections.PIN, UML.getValuePin()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReadExtentAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadIsClassifiedObjectAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReadSelfAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.STRUCTURAL_FEATURE, UML.getReadStructuralFeatureAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.CREATE_OBJECT_ACTION, UML.getReclassifyObjectAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getReduceAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendObjectAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getSendSignalAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getSequenceNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getStartClassifierBehaviorAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.INVOCATION_ACTION, UML.getStartObjectBehaviorAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.STRUCTURED_ACTIVITY_NODE, UML.getStructuredActivityNode()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.ACTIVITY_GROUP, UML.getActivity()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.NODES, UML.getTestIdentityAction()));
        this.applyNodeCreationTool(activityId, new ADCreationTool(ADToolSections.EXECUTABLE_NODE, UML.getValueSpecificationAction()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testEditLabel(String elementName) {
        String expectedGraphicalLabel = switch (elementName) {
            case ROOT_ACTIVITY, ACTIVITY -> ACTIVITY_LABEL_PREFIX + NEW_LABEL;
            // Graphical label doesn't change after direct edit
            case CONDITIONAL_NODE -> CONDITIONAL_NODE_LABEL;
            case CONSTRAINT -> NEW_LABEL + CONSTRAINT_LABEL_SUFFIX;
            // Graphical label doesn't change after direct edit
            case EXPANSION_REGION -> EXPANSION_REGION_LABEL;
            // Graphical label doesn't change after direct edit
            case LOOP_NODE -> LOOP_NODE_LABEL;
            // Graphical label doesn't change after direct edit
            case SEQUENCE_NODE -> SEQUENCE_NODE_LABEL;
            // Graphical label doesn't change after direct edit
            case STRUCTURED_ACTIVITY_NODE -> STRUCTURED_ACTIVITY_NODE_LABEL;
            default -> NEW_LABEL;
        };
        LabelGraphicalChecker graphicalChecker = new LabelGraphicalChecker(expectedGraphicalLabel);
        LabelSemanticChecker semanticChecker = new LabelSemanticChecker(this.getObjectService(), this::getEditingContext, NEW_LABEL);
        this.editLabel(elementName, NEW_LABEL, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

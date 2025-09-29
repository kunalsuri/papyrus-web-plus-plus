/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo, Artal Technologies.
 *
 * All rights reserved. This program and the accompanying materials
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
package org.eclipse.papyrus.web.services.activity;

import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.CONTENT_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.HOLDER_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.SHARED_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.activity.ActivityDiagramService;
import org.eclipse.papyrus.web.application.representations.uml.ADDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UMLMetamodelHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.services.AbstractDiagramTest;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.uml2.uml.AcceptCallAction;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.ActionInputPin;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityFinalNode;
import org.eclipse.uml2.uml.ActivityGroup;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityPartition;
import org.eclipse.uml2.uml.AddStructuralFeatureValueAction;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.ExpansionNode;
import org.eclipse.uml2.uml.ExpansionRegion;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.InterruptibleActivityRegion;
import org.eclipse.uml2.uml.MergeNode;
import org.eclipse.uml2.uml.OpaqueAction;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.TimeEvent;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.ValuePin;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class gathering integration tests for the Activity Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@SpringBootTest
@WebAppConfiguration
public class ActivityDiagramServiceTests extends AbstractDiagramTest {

    private static final String TOP_HOLDER_SUFFIX = UNDERSCORE + HOLDER_SUFFIX;

    private static final String TOP_CONTENT_SUFFIX = UNDERSCORE + CONTENT_SUFFIX;

    private static final IdBuilder ID_BUILDER = new IdBuilder(ADDiagramDescriptionBuilder.AD_PREFIX, new UMLMetamodelHelper());

    private static final String AD_ACTION_INPUT_PIN_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getActionInputPin(), SHARED_SUFFIX);

    private static final String AD_ACTIVITY_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getActivity());

    private static final String AD_ACTIVITY_PARTITION_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getActivityPartition(), SHARED_SUFFIX);

    private static final String AD_EXPANSION_REGION_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getExpansionRegion(), SHARED_SUFFIX);

    private static final String AD_EXPANSION_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getExpansionNode());

    private static final String AD_INITIAL_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getInitialNode(), SHARED_SUFFIX);

    private static final String AD_INPUT_PIN_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getInputPin(), SHARED_SUFFIX);

    private static final String AD_INTERRUPTIBLE_ACTIVITY_REGION_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getInterruptibleActivityRegion(), SHARED_SUFFIX);

    private static final String AD_OPAQUE_ACTION_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getOpaqueAction(), SHARED_SUFFIX);

    private static final String AD_OUTPUT_PIN_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getOutputPin(), SHARED_SUFFIX);

    private static final String AD_STRUCTURED_ACTIVITY_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getStructuredActivityNode(), SHARED_SUFFIX);

    private static final String AD_VALUE_PIN_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getValuePin(), SHARED_SUFFIX);

    /**
     * Tests {@link ActivityDiagramService#getActivityNodeCandidatesAD(EObject)} on an {@link Activity}.
     */
    @Test
    public void testGetActivityNodeCandidatesOnActivity() {
        Activity activity = this.create(Activity.class);
        ActivityPartition activityPartition = this.create(ActivityPartition.class);
        InitialNode initialNode = this.create(InitialNode.class);
        ActivityFinalNode finalNode = this.create(ActivityFinalNode.class);
        MergeNode mergeNode = this.create(MergeNode.class);
        activityPartition.getNodes().add(mergeNode);
        activity.getNodes().addAll(List.of(initialNode, finalNode, mergeNode));

        Collection<ActivityNode> activityNodeCandidates = this.getDiagramService().getActivityNodeCandidatesAD(activity);
        assertEquals(2, activityNodeCandidates.size());
        assertTrue(activityNodeCandidates.containsAll(List.of(initialNode, finalNode)));
    }

    /**
     * Tests {@link ActivityDiagramService#getActivityNodeCandidatesAD(EObject)} on an {@link ActivityPartition}.
     */
    @Test
    public void testGetActivityNodeCandidatesOnActivityPartition() {
        Activity activity = this.create(Activity.class);
        ActivityPartition activityPartition = this.create(ActivityPartition.class);
        InitialNode initialNode = this.create(InitialNode.class);
        ActivityFinalNode finalNode = this.create(ActivityFinalNode.class);
        MergeNode mergeNode = this.create(MergeNode.class);
        activity.getNodes().addAll(List.of(initialNode, finalNode, mergeNode));
        activityPartition.getNodes().addAll(List.of(finalNode, mergeNode));

        Collection<ActivityNode> activityNodeCandidates = this.getDiagramService().getActivityNodeCandidatesAD(activityPartition);
        assertEquals(2, activityNodeCandidates.size());
        assertTrue(activityNodeCandidates.containsAll(List.of(mergeNode, finalNode)));
    }

    /**
     * Tests {@link ActivityDiagramService#getActivityNodeCandidatesAD(EObject)} on an
     * {@link InterruptibleActivityRegion}.
     */
    @Test
    public void testGetActivityNodeCandidatesOnInterruptibleActivityRegion() {
        InterruptibleActivityRegion interruptibleActivityRegion = this.create(InterruptibleActivityRegion.class);
        InitialNode initialNode = this.create(InitialNode.class);
        ActivityFinalNode finalNode = this.create(ActivityFinalNode.class);
        MergeNode mergeNode = this.create(MergeNode.class);
        interruptibleActivityRegion.getNodes().addAll(List.of(initialNode, finalNode, mergeNode));

        Collection<ActivityNode> activityNodeCandidates = this.getDiagramService().getActivityNodeCandidatesAD(interruptibleActivityRegion);
        assertEquals(3, activityNodeCandidates.size());
        assertTrue(activityNodeCandidates.containsAll(List.of(initialNode, finalNode, mergeNode)));
    }

    /**
     * Tests {@link ActivityDiagramService#getActivityNodeCandidatesAD(EObject)} on a {@link StructuredActivityNode}.
     */
    @Test
    public void testGetActivityNodeCandidatesOnStructuredActivity() {
        ExpansionRegion expansionRegion = this.create(ExpansionRegion.class);
        InitialNode initialNode = this.create(InitialNode.class);
        ActivityFinalNode finalNode = this.create(ActivityFinalNode.class);
        MergeNode mergeNode = this.create(MergeNode.class);
        expansionRegion.getNodes().addAll(List.of(initialNode, finalNode, mergeNode));

        Collection<ActivityNode> activityNodeCandidates = this.getDiagramService().getActivityNodeCandidatesAD(expansionRegion);
        assertEquals(3, activityNodeCandidates.size());
        assertTrue(activityNodeCandidates.containsAll(List.of(initialNode, finalNode, mergeNode)));
    }

    /**
     * Tests {@link ActivityDiagramService#getActivityNodeCandidatesAD(EObject)} on a {@code null} element.
     */
    @Test
    public void testGetActivityNodeCandidatesOnNull() {
        Collection<ActivityNode> activityNodeCandidates = this.getDiagramService().getActivityNodeCandidatesAD(null);
        assertEquals(0, activityNodeCandidates.size());
    }

    /**
     * Tests {@link ActivityDiagramService#getActivityPartitionCandidatesAD(EObject)} on an {@link Activity}.
     */
    @Test
    public void testGetActivityPartitionCandidatesOnActivity() {
        Activity activity = this.create(Activity.class);
        ActivityPartition activityPartition = this.create(ActivityPartition.class);
        ActivityPartition activityPartition2 = this.create(ActivityPartition.class);
        activity.getOwnedGroups().addAll(List.of(activityPartition, activityPartition2));
        activity.getPartitions().addAll(List.of(activityPartition, activityPartition2));
        Collection<ActivityPartition> activityPartitions = this.getDiagramService().getActivityPartitionCandidatesAD(activity);
        assertEquals(2, activityPartitions.size());
        assertTrue(activityPartitions.containsAll(List.of(activityPartition, activityPartition2)));
    }

    /**
     * Tests {@link ActivityDiagramService#getActivityPartitionCandidatesAD(EObject)} on an {@link ActivityPartition}.
     */
    @Test
    public void testGetActivityPartitionCandidatesOnActivityPartition() {
        ActivityPartition activityPartition = this.create(ActivityPartition.class);
        ActivityPartition subActivityPartition = this.create(ActivityPartition.class);
        ActivityPartition subActivityPartition2 = this.create(ActivityPartition.class);
        activityPartition.getSubpartitions().addAll(List.of(subActivityPartition, subActivityPartition2));
        Collection<ActivityPartition> activityPartitions = this.getDiagramService().getActivityPartitionCandidatesAD(activityPartition);
        assertEquals(2, activityPartitions.size());
        assertTrue(activityPartitions.containsAll(List.of(subActivityPartition, subActivityPartition2)));
    }

    /**
     * Tests {@link ActivityDiagramService#getActivityPartitionCandidatesAD(EObject)} on a non-{@link Activity},
     * non-{@link ActivityPartition}.
     */
    @Test
    public void testGetActivityPartitionCandidatesOnNonActivityNonActivityPartition() {
        Comment comment = this.create(Comment.class);
        Collection<ActivityPartition> activityPartitions = this.getDiagramService().getActivityPartitionCandidatesAD(comment);
        assertTrue(activityPartitions.isEmpty());
    }

    /**
     * Tests {@link ActivityDiagramService#getActivityPartitionCandidatesAD(EObject)} on a {@code null} element.
     */
    @Test
    public void testGetActivityPartitionCandidatesOnNull() {
        Collection<ActivityPartition> activityPartitions = this.getDiagramService().getActivityPartitionCandidatesAD(null);
        assertEquals(0, activityPartitions.size());
    }

    /**
     * Tests {@link ActivityDiagramService#getExpansionNodesCandidatesAD(ExpansionRegion)}.
     */
    @Test
    public void testGetExpansionNodesCandidates() {
        ExpansionRegion expansionRegion = this.create(ExpansionRegion.class);
        ExpansionNode expansionNode = this.create(ExpansionNode.class);
        ExpansionNode expansionNode2 = this.create(ExpansionNode.class);
        ExpansionNode expansionNode3 = this.create(ExpansionNode.class);
        Collection<ExpansionNode> expansionNodesCandidates = this.getDiagramService().getExpansionNodesCandidatesAD(expansionRegion);
        assertTrue(expansionNodesCandidates.isEmpty());

        expansionRegion.getNodes().addAll(List.of(expansionNode, expansionNode2, expansionNode3));
        expansionRegion.getInputElements().add(expansionNode);
        expansionNode.setRegionAsInput(expansionRegion);
        expansionRegion.getOutputElements().addAll(List.of(expansionNode2, expansionNode3));
        expansionNode2.setRegionAsOutput(expansionRegion);
        expansionNode3.setRegionAsOutput(expansionRegion);
        expansionNodesCandidates = this.getDiagramService().getExpansionNodesCandidatesAD(expansionRegion);
        assertEquals(3, expansionNodesCandidates.size());
    }

    /**
     * Tests {@link ActivityDiagramService#getInterruptibleActivityRegionCandidatesAD(EObject)}.
     */
    @Test
    public void testGetInterruptibleActivityRegionCandidates() {
        Activity activity = this.create(Activity.class);
        InterruptibleActivityRegion interruptibleActivityRegion = this.create(InterruptibleActivityRegion.class);
        InterruptibleActivityRegion interruptibleActivityRegion2 = this.create(InterruptibleActivityRegion.class);
        ActivityPartition activityPartition = this.create(ActivityPartition.class);
        activity.getOwnedGroups().addAll(List.of(interruptibleActivityRegion, interruptibleActivityRegion2, activityPartition));

        Collection<InterruptibleActivityRegion> interruptibleActivityRegions = this.getDiagramService().getInterruptibleActivityRegionCandidatesAD(activity);
        assertEquals(2, interruptibleActivityRegions.size());
        assertTrue(interruptibleActivityRegions.containsAll(List.of(interruptibleActivityRegion, interruptibleActivityRegion2)));
    }

    /**
     * Tests {@link ActivityDiagramService#getInterruptibleActivityRegionCandidatesAD(EObject)} on a {@code null}
     * element.
     */
    @Test
    public void testGetInterruptibleActivityRegionCandidatesOnNull() {
        Collection<InterruptibleActivityRegion> interruptibleActivityRegions = this.getDiagramService().getInterruptibleActivityRegionCandidatesAD(null);
        assertEquals(0, interruptibleActivityRegions.size());
    }

    /**
     * Tests {@link ActivityDiagramService#computeAcceptEventActionWidthExpressionAD(AcceptEventAction)} and
     * {@link ActivityDiagramService#computeAcceptEventActionHeightExpressionAD(AcceptEventAction)} on an
     * {@link AcceptCallAction} with and without a trigger.
     * <p>
     * {@link AcceptEventAction} without a trigger are represented as a regular node. It is represented as an hourglass
     * when it has a trigger.
     * </p>
     */
    @Test
    public void testSizeExpressionOnAcceptEventAction() {
        AcceptEventAction acceptEventAction = UMLFactory.eINSTANCE.createAcceptEventAction();
        String hourglassNodeSize = "80";

        // AcceptEvent action with no trigger
        String computedPseudoStateWidthExpression = this.getDiagramService().computeAcceptEventActionWidthExpressionAD(acceptEventAction);
        String computedPseudoStateHeightExpression = this.getDiagramService().computeAcceptEventActionHeightExpressionAD(acceptEventAction);
        assertEquals("170", computedPseudoStateWidthExpression);
        assertEquals("70", computedPseudoStateHeightExpression);

        // AcceptEvent action with trigger and no event
        Trigger trigger = UMLFactory.eINSTANCE.createTrigger();
        acceptEventAction.getTriggers().add(trigger);
        computedPseudoStateWidthExpression = this.getDiagramService().computeAcceptEventActionWidthExpressionAD(acceptEventAction);
        computedPseudoStateHeightExpression = this.getDiagramService().computeAcceptEventActionHeightExpressionAD(acceptEventAction);
        assertEquals("170", computedPseudoStateWidthExpression);
        assertEquals("70", computedPseudoStateHeightExpression);

        // AcceptEvent action with trigger and event
        TimeEvent timeEvent = UMLFactory.eINSTANCE.createTimeEvent();
        trigger.setEvent(timeEvent);
        computedPseudoStateWidthExpression = this.getDiagramService().computeAcceptEventActionWidthExpressionAD(acceptEventAction);
        computedPseudoStateHeightExpression = this.getDiagramService().computeAcceptEventActionHeightExpressionAD(acceptEventAction);
        assertEquals(hourglassNodeSize, computedPseudoStateWidthExpression);
        assertEquals(hourglassNodeSize, computedPseudoStateHeightExpression);
    }

    /**
     * Tests {@link ActivityDiagramService#canCreateIntoParentAD(EObject, String)} for an {@link OutputPin} in an
     * {@link AcceptCallAction}.
     * <p>
     * A detailed test suite for parent/children containment is defined in {@link ActivityFeatureProviderTests}.
     * </p>
     *
     * @see ActivityFeatureProviderTests
     */
    @Test
    public void testCanCreateIntoParentOutputPinInAcceptCallAction() {
        // A detailed test suite for parent/children containment is defined in ActivityFeatureProviderTest
        AcceptCallAction acceptCallAction = this.create(AcceptCallAction.class);
        assertTrue(this.getDiagramService().canCreateIntoParentAD(acceptCallAction, UML.getOutputPin().getName()));
    }

    /**
     * Tests {@link ActivityDiagramService#canCreateIntoParentAD(EObject, String)} for an {@link InitialNode} in a
     * {@link Comment}.
     * <p>
     * A detailed test suite for parent/children containment is defined in {@link ActivityFeatureProviderTests}.
     * </p>
     *
     * @see ActivityFeatureProviderTests
     */
    @Test
    public void testCanCreateIntoParentInitialNodeInComment() {
        // A detailed test suite for parent/children containment is defined in ActivityFeatureProviderTest
        Comment comment = this.create(Comment.class);
        assertFalse(this.getDiagramService().canCreateIntoParentAD(comment, UML.getInitialNode().getName()));
    }

    /**
     * Tests
     * {@link ActivityDiagramService#createActivityNodeAD(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)
     * for an {@link ActivityNode} in an {@link Activity}.
     */
    @Test
    public void testCreateActivityNodeInActivity() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createActivityNodeAD(activity, UML.getInitialNode().getName(), UML.getActivity_OwnedNode().getName(), activityNodeContent, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // Check semantic element creation
        InitialNode initialNode = assertInstanceOf(InitialNode.class, newElement);
        assertEquals(activity, initialNode.eContainer());
        assertTrue(activity.getOwnedNodes().contains(initialNode));

        // Check graphical element creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_INITIAL_NODE_NAME, activityNodeContent, newElement);
    }

    /**
     * Tests
     * {@link ActivityDiagramService#createActivityNodeAD(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)
     * for an {@link ActivityNode} in an {@link ActivityPartition}.
     */
    @Test
    public void testCreateActivityNodeInActivityPartition() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        ActivityPartition activityPartition = this.create(ActivityPartition.class);
        activity.getPartitions().add(activityPartition);
        Node activityPartitionNodeHolder = this.getDiagramHelper().createNodeInParent(AD_ACTIVITY_PARTITION_NODE_NAME + TOP_HOLDER_SUFFIX, activityPartition, activityNodeContent);
        Node activityPartitionNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_ACTIVITY_PARTITION_NODE_NAME + TOP_CONTENT_SUFFIX, activityPartitionNodeHolder,
                activityPartition);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createActivityNodeAD(activityPartition, UML.getInitialNode().getName(), UML.getActivity_OwnedNode().getName(), activityPartitionNodeContent, context,
                            this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // Check semantic element creation. The element is contained in the parent activity, but it is referenced in
        // ActivityPartition#getNode (which is not a containment reference).
        InitialNode initialNode = assertInstanceOf(InitialNode.class, newElement);
        assertEquals(activity, initialNode.eContainer());
        assertEquals(List.of(activityPartition), initialNode.getInPartitions());
        assertTrue(activity.getOwnedNodes().contains(initialNode));
        assertTrue(activityPartition.getNodes().contains(initialNode));

        // Check graphical element creation. The graphical element is contained in the ActivityPartition node.
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_INITIAL_NODE_NAME, activityPartitionNodeContent, newElement);
    }

    /**
     * Tests
     * {@link ActivityDiagramService#createActivityNodeAD(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)
     * for an {@link ActivityNode} in an {@link InterruptibleActivityRegion}.
     */
    @Test
    public void testCreateActivityNodeInInterruptibleActivityRegion() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        InterruptibleActivityRegion interruptibleActivityRegion = this.create(InterruptibleActivityRegion.class);
        activity.getOwnedGroups().add(interruptibleActivityRegion);
        Node interruptibleActivityRegionNodeHolder = this.getDiagramHelper().createNodeInParent(AD_INTERRUPTIBLE_ACTIVITY_REGION_NODE_NAME + TOP_HOLDER_SUFFIX, interruptibleActivityRegion,
                activityNodeContent);
        Node interruptibleActivityRegionNode = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_INTERRUPTIBLE_ACTIVITY_REGION_NODE_NAME + TOP_CONTENT_SUFFIX,
                interruptibleActivityRegionNodeHolder,
                interruptibleActivityRegion);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createActivityNodeAD(interruptibleActivityRegion, UML.getInitialNode().getName(), UML.getActivity_OwnedNode().getName(), interruptibleActivityRegionNode, context,
                            this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // Check semantic element creation. The element is contained in the parent activity, but it is referenced in
        // ActivityPartition#getNode (which is not a containment reference).
        InitialNode initialNode = assertInstanceOf(InitialNode.class, newElement);
        assertEquals(activity, initialNode.eContainer());
        assertEquals(List.of(interruptibleActivityRegion), initialNode.getInGroups());
        assertTrue(activity.getOwnedNodes().contains(initialNode));
        assertTrue(interruptibleActivityRegion.getNodes().contains(initialNode));

        // Check graphical element creation. The graphical element is contained in the ActivityPartition node.
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_INITIAL_NODE_NAME, interruptibleActivityRegionNode, newElement);
    }

    /**
     * Tests
     * {@link ActivityDiagramService#createActivityNodeAD(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)
     * for an {@link ActivityNode} in a {@link StructuredActivityNode}.
     */
    @Test
    public void testCreateActivityNodeInStructuredActivityNode() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        StructuredActivityNode structuredActivityNode = this.create(StructuredActivityNode.class);
        activity.getStructuredNodes().add(structuredActivityNode);
        Node structuredActivityNodeNodeHolder = this.getDiagramHelper().createNodeInParent(AD_STRUCTURED_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, structuredActivityNode, activityNodeContent);
        Node structuredActivityNodeNodeContent = this.getDiagramHelper().createNodeInParent(AD_STRUCTURED_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, structuredActivityNode,
                structuredActivityNodeNodeHolder);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createActivityNodeAD(structuredActivityNode, UML.getInitialNode().getName(), UML.getActivity_OwnedNode().getName(), structuredActivityNodeNodeContent, context,
                            this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // Check semantic element creation.
        InitialNode initialNode = assertInstanceOf(InitialNode.class, newElement);
        assertEquals(structuredActivityNode, initialNode.eContainer());
        assertTrue(structuredActivityNode.getNodes().contains(initialNode));

        // Check graphical element creation.
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_INITIAL_NODE_NAME, structuredActivityNodeNodeContent, newElement);

    }

    /**
     * Tests
     * {@link ActivityDiagramService#createActivityNodeAD(EObject, String, String, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)
     * for an {@link ActivityNode} in a non-{@link Activity}, non-{@link ActivityGroup},
     * non-{@link StructuredActivityNode}.
     */
    @Test
    public void testCreateActivityNodeInNonActivityNonActivityGroupNonStructuredActivityNode() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        InitialNode initialNode = this.create(InitialNode.class);
        activity.getOwnedNodes().add(initialNode);
        Node initialNodeGraphicalNode = this.getDiagramHelper().createNodeInParent(AD_INITIAL_NODE_NAME, initialNode, activityNodeContent);
        // This produces an IllegalArgumentException because createActivityNodeAD returns null
        assertThrows(IllegalArgumentException.class, () -> {
            this.getDiagramHelper().modify(context -> {
                EObject aNewElement = this.getDiagramService() //
                        .createActivityNodeAD(initialNode, UML.getInitialNode().getName(), UML.getActivity_OwnedNode().getName(), initialNodeGraphicalNode, context,
                                this.getDiagramHelper().getConvertedNodes());
                return aNewElement;
            });
        });
    }

    /**
     * Tests
     * {@link ActivityDiagramService#createExpansionNodeAD(EObject, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map, boolean)}
     * for an input {@link ExpansionNode} in an {@link ExpansionRegion}.
     */
    @Test
    public void testCreateInputExpansionNodeInExpansionRegion() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        ExpansionRegion expansionRegion = this.create(ExpansionRegion.class);
        activity.getOwnedNodes().add(expansionRegion);
        Node expansionRegionNodeHolder = this.getDiagramHelper().createNodeInParent(AD_EXPANSION_REGION_NODE_NAME + TOP_HOLDER_SUFFIX, expansionRegion, activityNodeContent);
        Node expansionRegionNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_EXPANSION_REGION_NODE_NAME + TOP_CONTENT_SUFFIX, expansionRegion);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createExpansionNodeAD(expansionRegion, expansionRegionNodeContent, context, this.getDiagramHelper().getConvertedNodes(), true);
            return aNewElement;
        });

        // Check semantic element creation.
        ExpansionNode expansionNode = assertInstanceOf(ExpansionNode.class, newElement);
        assertEquals(expansionRegion, expansionNode.getRegionAsInput());
        assertTrue(expansionRegion.getInputElements().contains(expansionNode));

        // Check graphical element creation.
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_EXPANSION_NODE_NAME, expansionRegionNodeHolder, newElement);

    }

    /**
     * Tests
     * {@link ActivityDiagramService#createExpansionNodeAD(EObject, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map, boolean)}
     * for an output {@link ExpansionNode} in an {@link ExpansionRegion}.
     */
    @Test
    public void testCreateOutputExpansionNodeInExpansionRegion() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        ExpansionRegion expansionRegion = this.create(ExpansionRegion.class);
        activity.getOwnedNodes().add(expansionRegion);
        Node expansionRegionNodeHolder = this.getDiagramHelper().createNodeInParent(AD_EXPANSION_REGION_NODE_NAME + TOP_HOLDER_SUFFIX, expansionRegion, activityNodeContent);
        Node expansionRegionNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_EXPANSION_REGION_NODE_NAME + TOP_CONTENT_SUFFIX, expansionRegion);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createExpansionNodeAD(expansionRegion, expansionRegionNodeContent, context, this.getDiagramHelper().getConvertedNodes(), false);
            return aNewElement;
        });

        // Check semantic element creation.
        ExpansionNode expansionNode = assertInstanceOf(ExpansionNode.class, newElement);
        assertEquals(expansionRegion, expansionNode.getRegionAsOutput());
        assertTrue(expansionRegion.getOutputElements().contains(expansionNode));

        // Check graphical element creation.
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_EXPANSION_NODE_NAME, expansionRegionNodeHolder, newElement);
    }

    /**
     * Tests
     * {@link ActivityDiagramService#createActionInputPinAD(org.eclipse.uml2.uml.Element, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * for an {@link ActionInputPin} in an {@link OpaqueAction}.
     */
    @Test
    public void testCreateActionInputPinInOpaqueAction() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        OpaqueAction opaqueAction = this.create(OpaqueAction.class);
        activity.getOwnedNodes().add(opaqueAction);
        Node opaqueActionNode = this.getDiagramHelper().createNodeInParent(AD_OPAQUE_ACTION_NODE_NAME, opaqueAction, activityNodeContent);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createActionInputPinAD(opaqueAction, opaqueActionNode, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // Check semantic element creation.
        ActionInputPin actionInputPin = assertInstanceOf(ActionInputPin.class, newElement);
        assertEquals(opaqueAction, actionInputPin.eContainer());
        assertTrue(opaqueAction.getInputValues().contains(actionInputPin));

        // Check graphical element creation.
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_ACTION_INPUT_PIN_NODE_NAME, opaqueActionNode, newElement);
    }

    /**
     * Tests
     * {@link ActivityDiagramService#createInputPinAD(org.eclipse.uml2.uml.Element, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * for an {@link InputPin} in an {@link OpaqueAction}.
     */
    @Test
    public void testCreateInputPinInOpaqueAction() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        OpaqueAction opaqueAction = this.create(OpaqueAction.class);
        activity.getOwnedNodes().add(opaqueAction);
        Node opaqueActionNode = this.getDiagramHelper().createNodeInParent(AD_OPAQUE_ACTION_NODE_NAME, opaqueAction, activityNodeContent);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createInputPinAD(opaqueAction, opaqueActionNode, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // Check semantic element creation.
        InputPin inputPin = assertInstanceOf(InputPin.class, newElement);
        assertEquals(opaqueAction, inputPin.eContainer());
        assertTrue(opaqueAction.getInputValues().contains(inputPin));

        // Check graphical element creation.
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_INPUT_PIN_NODE_NAME, opaqueActionNode, newElement);
    }

    /**
     * Tests
     * {@link ActivityDiagramService#createOutputPinAD(org.eclipse.uml2.uml.Element, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * for an {@link OutputPin} in an {@link OpaqueAction}.
     */
    @Test
    public void testCreateOutputPinInOpaqueAction() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        OpaqueAction opaqueAction = this.create(OpaqueAction.class);
        activity.getOwnedNodes().add(opaqueAction);
        Node opaqueActionNode = this.getDiagramHelper().createNodeInParent(AD_OPAQUE_ACTION_NODE_NAME, opaqueAction, activityNodeContent);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createOutputPinAD(opaqueAction, opaqueActionNode, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // Check semantic element creation.
        OutputPin outputPin = assertInstanceOf(OutputPin.class, newElement);
        assertEquals(opaqueAction, outputPin.eContainer());
        assertTrue(opaqueAction.getOutputValues().contains(outputPin));

        // Check graphical element creation.
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_OUTPUT_PIN_NODE_NAME, opaqueActionNode, newElement);
    }

    /**
     * Tests
     * {@link ActivityDiagramService#createValuePinAD(org.eclipse.uml2.uml.Element, Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * for a {@link ValuePin} in an {@link OpaqueAction}.
     */
    @Test
    public void testCreateValuePinInOpaqueAction() {
        Activity activity = this.init();
        this.getDiagramHelper().createNodeInDiagram(AD_ACTIVITY_NODE_NAME + TOP_HOLDER_SUFFIX, activity);
        Node activityNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(AD_ACTIVITY_NODE_NAME + TOP_CONTENT_SUFFIX, activity);
        OpaqueAction opaqueAction = this.create(OpaqueAction.class);
        activity.getOwnedNodes().add(opaqueAction);
        Node opaqueActionNode = this.getDiagramHelper().createNodeInParent(AD_OPAQUE_ACTION_NODE_NAME, opaqueAction, activityNodeContent);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService() //
                    .createValuePinAD(opaqueAction, opaqueActionNode, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // Check semantic element creation.
        ValuePin valuePin = assertInstanceOf(ValuePin.class, newElement);
        assertEquals(opaqueAction, valuePin.eContainer());
        assertTrue(opaqueAction.getInputValues().contains(valuePin));

        // Check graphical element creation.
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(AD_VALUE_PIN_NODE_NAME, opaqueActionNode, newElement);
    }

    /**
     * Tests {@link ActivityDiagramService#getActionInputPinCandidatesAD(EObject)} on a {@code null} element.
     */
    @Test
    public void testGetActionInputPinCandidatesOnNull() {
        Collection<ActionInputPin> actionInputPinNodeCandidates = this.getDiagramService().getActionInputPinCandidatesAD(null);
        assertEquals(0, actionInputPinNodeCandidates.size());
    }

    /**
     * Tests {@link ActivityDiagramService#getActionInputPinCandidatesAD(EObject)} on an {@link org.eclipse.uml2.uml.Action} with
     * {@link ValuePin}.
     */
    @Test
    public void testGetActionInputPinCandidates() {
        AddStructuralFeatureValueAction addStructuralFeatureValueAction = this.create(AddStructuralFeatureValueAction.class);

        Collection<ActionInputPin> actionInputPinNodeCandidates = this.getDiagramService().getActionInputPinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(0, actionInputPinNodeCandidates.size());

        InputPin inputPin = this.create(InputPin.class);
        addStructuralFeatureValueAction.setInsertAt(inputPin);
        actionInputPinNodeCandidates = this.getDiagramService().getActionInputPinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(0, actionInputPinNodeCandidates.size());

        ActionInputPin actionInputPin = this.create(ActionInputPin.class);
        addStructuralFeatureValueAction.setValue(actionInputPin);
        actionInputPinNodeCandidates = this.getDiagramService().getActionInputPinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(1, actionInputPinNodeCandidates.size());
        assertTrue(actionInputPinNodeCandidates.contains(actionInputPin));
    }

    /**
     * Tests {@link ActivityDiagramService#getInputPinCandidatesAD(EObject)} on a {@code null} element.
     */
    @Test
    public void testGetInputPinCandidatesOnNull() {
        Collection<InputPin> inputPinNodeCandidates = this.getDiagramService().getInputPinCandidatesAD(null);
        assertEquals(0, inputPinNodeCandidates.size());
    }

    /**
     * Tests {@link ActivityDiagramService#getInputPinCandidatesAD(EObject)} on an {@link org.eclipse.uml2.uml.Action} with {@link InputPin}.
     */
    @Test
    public void testGetInputPinCandidates() {
        AddStructuralFeatureValueAction addStructuralFeatureValueAction = this.create(AddStructuralFeatureValueAction.class);

        Collection<InputPin> inputPinNodeCandidates = this.getDiagramService().getInputPinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(0, inputPinNodeCandidates.size());

        ValuePin valuePin = this.create(ValuePin.class);
        addStructuralFeatureValueAction.setValue(valuePin);
        inputPinNodeCandidates = this.getDiagramService().getInputPinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(0, inputPinNodeCandidates.size());

        InputPin inputPin = this.create(InputPin.class);
        addStructuralFeatureValueAction.setInsertAt(inputPin);
        inputPinNodeCandidates = this.getDiagramService().getInputPinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(1, inputPinNodeCandidates.size());
        assertTrue(inputPinNodeCandidates.contains(inputPin));
    }

    /**
     * Tests {@link ActivityDiagramService#getOutputPinCandidatesAD(EObject)} on a {@code null} element.
     */
    @Test
    public void testGetOutputPinCandidatesOnNull() {
        Collection<OutputPin> outputPinNodeCandidates = this.getDiagramService().getOutputPinCandidatesAD(null);
        assertEquals(0, outputPinNodeCandidates.size());
    }

    /**
     * Tests {@link ActivityDiagramService#getOutputPinCandidatesAD(EObject)} on an {@link org.eclipse.uml2.uml.Action} with
     * {@link InputPin}.
     */
    @Test
    public void testGetOutputPinCandidates() {
        AddStructuralFeatureValueAction addStructuralFeatureValueAction = this.create(AddStructuralFeatureValueAction.class);

        Collection<OutputPin> outputPinNodeCandidates = this.getDiagramService().getOutputPinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(0, outputPinNodeCandidates.size());

        ValuePin valuePin = this.create(ValuePin.class);
        addStructuralFeatureValueAction.setValue(valuePin);
        outputPinNodeCandidates = this.getDiagramService().getOutputPinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(0, outputPinNodeCandidates.size());

        OutputPin outputPin = this.create(OutputPin.class);
        addStructuralFeatureValueAction.setResult(outputPin);
        outputPinNodeCandidates = this.getDiagramService().getOutputPinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(1, outputPinNodeCandidates.size());
        assertTrue(outputPinNodeCandidates.contains(outputPin));
    }

    /**
     * Tests {@link ActivityDiagramService#getValuePinCandidatesAD(EObject)} on a {@code null} element.
     */
    @Test
    public void testGetValuePinCandidatesOnNull() {
        Collection<ValuePin> valuePinNodeCandidates = this.getDiagramService().getValuePinCandidatesAD(null);
        assertEquals(0, valuePinNodeCandidates.size());
    }

    /**
     * Tests {@link ActivityDiagramService#getValuePinCandidatesAD(EObject)} on an {@link org.eclipse.uml2.uml.Action} with {@link ValuePin}.
     */
    @Test
    public void testGetValuePinCandidates() {
        AddStructuralFeatureValueAction addStructuralFeatureValueAction = this.create(AddStructuralFeatureValueAction.class);

        Collection<ValuePin> valuePinNodeCandidates = this.getDiagramService().getValuePinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(0, valuePinNodeCandidates.size());

        InputPin inputPin = this.create(InputPin.class);
        addStructuralFeatureValueAction.setInsertAt(inputPin);
        valuePinNodeCandidates = this.getDiagramService().getValuePinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(0, valuePinNodeCandidates.size());

        ValuePin valuePin = this.create(ValuePin.class);
        addStructuralFeatureValueAction.setValue(valuePin);
        valuePinNodeCandidates = this.getDiagramService().getValuePinCandidatesAD(addStructuralFeatureValueAction);
        assertEquals(1, valuePinNodeCandidates.size());
        assertTrue(valuePinNodeCandidates.contains(valuePin));
    }

    /**
     * Tests {@link ActivityDiagramService#getDecisionInputNoteLabel(DecisionNode)} for {@link DecisionNode}s with and
     * without {@code decisionInput}.
     */
    @Test
    public void testGetDecisionInputNoteLabel() {
        DecisionNode decisionNode = this.create(DecisionNode.class);
        assertEquals(UMLCharacters.EMPTY, this.getDiagramService().getDecisionInputNoteLabel(decisionNode));
        Activity activity = this.create(Activity.class);
        activity.setName("TestActivity");
        decisionNode.setDecisionInput(activity);
        assertEquals(UMLCharacters.ST_LEFT + "decisionInput" + UMLCharacters.ST_RIGHT + UMLCharacters.SPACE + activity.getName(), this.getDiagramService().getDecisionInputNoteLabel(decisionNode));
    }

    private Activity init() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);
        Activity activity = this.createIn(Activity.class, pack);
        this.getDiagramHelper().init(activity, ADDiagramDescriptionBuilder.AD_REP_NAME);
        this.getDiagramHelper().refresh();
        return activity;

    }

    @Override
    protected AbstractDiagramService buildService() {
        return new ActivityDiagramService(getIdentityService(), getLabelService(), getObjectSearchService(), this.getDiagramNavigationService(), this.getDiagramOperationsService(), e -> true,
                this.getViewDiagramDescriptionService(),
                new MockLogger());
    }

    @Override
    protected ActivityDiagramService getDiagramService() {
        return (ActivityDiagramService) super.getDiagramService();
    }

}

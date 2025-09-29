/*****************************************************************************
 * Copyright (c) 2022, 2025 CEA LIST, Obeo, Artal Technologies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 190
 *****************************************************************************/
package org.eclipse.papyrus.web.services.statemachine;

import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.SHARED_SUFFIX;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.statemachine.StateMachineDiagramService;
import org.eclipse.papyrus.web.application.representations.uml.SMDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UMLMetamodelHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.services.AbstractDiagramTest;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.papyrus.web.utils.ElementMatcher;
import org.eclipse.papyrus.web.utils.LabelStyleCheck;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.uml2.uml.FinalState;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.UMLFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class related to the State Machine Diagram.
 *
 * @author Laurent Fasani
 */
@SpringBootTest
@WebAppConfiguration
public class StateMachineDiagramTests extends AbstractDiagramTest {

    private static final IdBuilder ID_BUILDER = new IdBuilder(SMDDiagramDescriptionBuilder.SMD_PREFIX, new UMLMetamodelHelper());

    private static final String SMD_COMMENT = ID_BUILDER.getDomainNodeName(UML.getComment());

    private static final String SMD_COMMENT_SHARED = ID_BUILDER.getSpecializedDomainNodeName(UML.getComment(), SHARED_SUFFIX);

    private static final String SMD_STATEMACHINE_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getStateMachine());

    private static final String SMD_REGION_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getRegion(), SHARED_SUFFIX);

    private static final String SMD_STATE_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getState(), SHARED_SUFFIX);

    private static final String SMD_FINALSTATE_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getFinalState(), SHARED_SUFFIX);

    private static final String SMD_PSEUDOSTATE_IN_REGION_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getPseudostate(), SHARED_SUFFIX);

    private static final String SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_MACHINE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getPseudostate(), "StateMachine_BorderedNode_SHARED");

    private static final String SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getPseudostate(), "State_BorderedNode_SHARED");

    private static final String SMD_TRANSITION_EDGE_NAME = ID_BUILDER.getDomainBaseEdgeId(UML.getTransition());

    private static final String STATE1 = "state1";

    private static final String ENTRYPOINT_IN_SM = "pseudostateInSM";

    private static final String ENTRYPOINT_IN_STATE1 = "pseudostateInState1";

    private State state1;

    private StateMachine stateMachine;

    private Pseudostate entryPointInSM;

    private Pseudostate entryPointInState1;

    private Transition transition;

    private State stateInState1;

    private Region regionInSM;

    private Region regionInState1;

    @Test
    public void checkConditionalLabelStyleOnStateMachine() {
        this.init();

        Node stateMachineNode = this.getDiagramHelper().assertGetUniqueMatchingNode(SMD_STATEMACHINE_NODE_NAME, this.stateMachine);
        LabelStyleCheck.build(stateMachineNode).assertIsNotItalic().assertIsNotUnderline();

        this.stateMachine.setIsAbstract(true);

        this.getDiagramHelper().refresh();
        stateMachineNode = this.getDiagramHelper().assertGetUniqueMatchingNode(SMD_STATEMACHINE_NODE_NAME, this.stateMachine);

        LabelStyleCheck.build(stateMachineNode).assertIsItalic().assertIsNotUnderline();
    }

    @Test
    public void checkSizeExpressionOnPseudoState() {
        Pseudostate pseudostate = UMLFactory.eINSTANCE.createPseudostate();

        // DeepHistory Pseudostate
        pseudostate.setKind(PseudostateKind.DEEP_HISTORY_LITERAL);
        String computedPseudoStateWidthExpression = this.getDiagramService().computePseudoStateWidthExpression(pseudostate);
        String computedPseudoStateHeightExpression = this.getDiagramService().computePseudoStateHeightExpression(pseudostate);
        assertEquals(StateMachineDiagramService.ROUND_ICON_NODE_DEFAULT_DIAMETER, computedPseudoStateWidthExpression);
        assertEquals(StateMachineDiagramService.ROUND_ICON_NODE_DEFAULT_DIAMETER, computedPseudoStateHeightExpression);

        // Fork Pseudostate
        pseudostate.setKind(PseudostateKind.FORK_LITERAL);
        computedPseudoStateWidthExpression = this.getDiagramService().computePseudoStateWidthExpression(pseudostate);
        computedPseudoStateHeightExpression = this.getDiagramService().computePseudoStateHeightExpression(pseudostate);
        assertEquals(StateMachineDiagramService.FORK_NODE_DEFAULT_WIDTH, computedPseudoStateWidthExpression);
        assertEquals(StateMachineDiagramService.FORK_NODE_DEFAULT_HEIGHT, computedPseudoStateHeightExpression);

        // Join Pseudostate
        pseudostate.setKind(PseudostateKind.JOIN_LITERAL);
        computedPseudoStateWidthExpression = this.getDiagramService().computePseudoStateWidthExpression(pseudostate);
        computedPseudoStateHeightExpression = this.getDiagramService().computePseudoStateHeightExpression(pseudostate);
        assertEquals(StateMachineDiagramService.FORK_NODE_DEFAULT_WIDTH, computedPseudoStateWidthExpression);
        assertEquals(StateMachineDiagramService.FORK_NODE_DEFAULT_HEIGHT, computedPseudoStateHeightExpression);
    }

    /**
     * Checks all the possible owner of comments node (root, region in state machine, region in state).
     */
    @Test
    public void checkCommentParent() {
        this.init();

        this.getServiceTester().assertRootCreation(UML.getComment(), UML.getElement_OwnedComment(), SMD_COMMENT);

        Node regionInStateMachine = this.getRegionInSMNode();
        this.getServiceTester().assertChildCreation(regionInStateMachine, UML.getComment(), UML.getElement_OwnedComment(), SMD_COMMENT_SHARED, this.regionInSM);

        // Drop the state + synchronized region
        this.getServiceTester().assertSemanticDrop(this.state1, regionInStateMachine, SMD_STATE_NODE_NAME);

        Node regionInStateNode = this.getRegionInStateNode();
        this.getServiceTester().assertChildCreation(regionInStateNode, UML.getComment(), UML.getElement_OwnedComment(), SMD_COMMENT_SHARED, this.regionInState1);
    }

    @Test
    public void checkRootStateMachine() {
        this.init();

        // check that the StateMachine node is created
        Node stateMachineNode = this.getDiagramHelper().assertGetUniqueMatchingNode(SMD_STATEMACHINE_NODE_NAME, this.stateMachine);
        assertNotNull("The StateMachine node should be created at the diagram refresh because it is synchronized", stateMachineNode);

        // check that the Region node is created
        Node regionNode = this.getRegionInSMNode();
        assertNotNull("The Region node should be created because it is synchronized", regionNode);
    }

    @Test
    public void checkSemanticDropStateAndPseudostate() {
        this.init();

        Node stateMachineNode = this.getDiagramHelper().assertGetUniqueMatchingNode(SMD_STATEMACHINE_NODE_NAME, this.stateMachine);

        this.getServiceTester().assertSemanticDrop(this.entryPointInSM, stateMachineNode, SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_MACHINE_NAME);

        Node sMRegionNode = this.getRegionInSMNode();
        this.getServiceTester().assertSemanticDrop(this.state1, sMRegionNode, SMD_STATE_NODE_NAME);

        Node state1Node = this.getDiagramHelper().assertGetUniqueMatchingNode(SMD_STATE_NODE_NAME, this.state1);
        this.getServiceTester().assertSemanticDrop(this.entryPointInState1, state1Node, SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_NAME);

        Node regionInState1Node = this.getRegionInStateNode();
        this.getServiceTester().assertSemanticDrop(this.stateInState1, regionInState1Node, SMD_STATE_NODE_NAME);
    }

    private Node getRegionInStateNode() {
        return this.getDiagramHelper().assertGetUniqueMatchingNode(SMD_REGION_NODE_NAME, this.state1.getRegions().get(0));
    }

    private Node getRegionInSMNode() {
        return this.getDiagramHelper().assertGetUniqueMatchingNode(SMD_REGION_NODE_NAME, this.stateMachine.getRegions().get(0));
    }

    /**
     * Check the display and reconnection of the synchronized Transition with multiple combinaison of source and target
     * mapping.
     */
    @Test
    public void checkTransition() {
        this.init();

        Node stateMachineNode = this.getDiagramHelper().assertGetUniqueMatchingNode(SMD_STATEMACHINE_NODE_NAME, this.stateMachine);
        Node entryPointOnSMNode = this.getDiagramHelper().createNodeInParent(SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_MACHINE_NAME, this.entryPointInSM, stateMachineNode);
        Node sMRegionNode = this.getRegionInSMNode();
        Node state1Node = this.getDiagramHelper().createNodeInParent(SMD_STATE_NODE_NAME, this.state1, sMRegionNode);
        this.getDiagramHelper().createNodeInParent(SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_NAME, this.entryPointInState1, state1Node);
        Node regionInState1Node = this.getRegionInStateNode();
        this.getDiagramHelper().createNodeInParent(SMD_STATE_NODE_NAME, this.stateInState1, regionInState1Node);

        // check that the transition edge is displayed (synchronized)
        // check SM BorderNode To State node
        Edge transitionEdge = this.getDiagramHelper()//
                .getMatchingEdge(//
                        Optional.of(SMD_TRANSITION_EDGE_NAME), //
                        Optional.of(this.getIdentityService().getId(this.transition)), //
                        Optional.of(entryPointOnSMNode.getId()), Optional.of(state1Node.getId()));

        assertNotNull(transitionEdge);

        State state2InState1 = this.createIn(State.class, this.regionInState1);
        this.getDiagramHelper().createNodeInParent(SMD_STATE_NODE_NAME, state2InState1, regionInState1Node);

        Pseudostate forkInState1 = this.createIn(Pseudostate.class, this.regionInState1);
        forkInState1.setKind(PseudostateKind.FORK_LITERAL);
        this.getDiagramHelper().createNodeInParent(SMD_PSEUDOSTATE_IN_REGION_NAME, forkInState1, regionInState1Node);

        State finalState = this.createIn(FinalState.class, this.regionInSM);
        this.getDiagramHelper().createNodeInParent(SMD_FINALSTATE_NODE_NAME, finalState, sMRegionNode);

        // Check target reconnection
        // check SM BorderNode To State border node
        this.getServiceTester().assertTargetReconnection(new ElementMatcher(this.transition, SMD_TRANSITION_EDGE_NAME), //
                new ElementMatcher(this.state1, SMD_STATE_NODE_NAME), //
                new ElementMatcher(this.entryPointInState1, SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_NAME), //
                new ElementMatcher(this.entryPointInSM, SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_MACHINE_NAME));

        // Check source reconnection
        // check State node To State border node
        this.getServiceTester().assertSourceReconnection(new ElementMatcher(this.transition, SMD_TRANSITION_EDGE_NAME), //
                new ElementMatcher(this.entryPointInSM, SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_MACHINE_NAME), //
                new ElementMatcher(this.stateInState1, SMD_STATE_NODE_NAME), //
                new ElementMatcher(this.entryPointInState1, SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_NAME));

        // Check target reconnection
        // check State node To FinalState node
        this.getServiceTester().assertTargetReconnection(new ElementMatcher(this.transition, SMD_TRANSITION_EDGE_NAME), //
                new ElementMatcher(this.entryPointInState1, SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_NAME), //
                new ElementMatcher(finalState, SMD_FINALSTATE_NODE_NAME), //
                new ElementMatcher(this.stateInState1, SMD_STATE_NODE_NAME));

        // Check target reconnection
        // check State node To State node
        this.getServiceTester().assertTargetReconnection(new ElementMatcher(this.transition, SMD_TRANSITION_EDGE_NAME), //
                new ElementMatcher(finalState, SMD_FINALSTATE_NODE_NAME), //
                new ElementMatcher(state2InState1, SMD_STATE_NODE_NAME), //
                new ElementMatcher(this.stateInState1, SMD_STATE_NODE_NAME));

        // Check source reconnection
        // check Pseudostate node To State node
        this.getServiceTester().assertSourceReconnection(new ElementMatcher(this.transition, SMD_TRANSITION_EDGE_NAME), //
                new ElementMatcher(this.stateInState1, SMD_STATE_NODE_NAME), //
                new ElementMatcher(forkInState1, SMD_PSEUDOSTATE_IN_REGION_NAME), //
                new ElementMatcher(state2InState1, SMD_STATE_NODE_NAME));
    }

    @Test
    public void checkNodeCreation() {
        this.init();

        Node regionInSMNode = this.getRegionInSMNode();

        Node stateMachineNode = this.getDiagramHelper().assertGetUniqueMatchingNode(SMD_STATEMACHINE_NODE_NAME, this.stateMachine);
        Node newRegionInSMNode = this.getServiceTester().assertChildCreation(stateMachineNode, UML.getRegion(), UML.getStateMachine_Region(), SMD_REGION_NODE_NAME);

        this.assertPseudostateCreation(stateMachineNode, UML.getStateMachine_ConnectionPoint(), SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_MACHINE_NAME, PseudostateKind.ENTRY_POINT_LITERAL);
        this.assertPseudostateCreation(stateMachineNode, UML.getStateMachine_ConnectionPoint(), SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_MACHINE_NAME, PseudostateKind.EXIT_POINT_LITERAL);

        this.assertPseudostateCreation(newRegionInSMNode, UML.getRegion_Subvertex(), SMD_PSEUDOSTATE_IN_REGION_NAME, PseudostateKind.INITIAL_LITERAL);
        this.assertPseudostateCreation(newRegionInSMNode, UML.getRegion_Subvertex(), SMD_PSEUDOSTATE_IN_REGION_NAME, PseudostateKind.TERMINATE_LITERAL);
        this.assertPseudostateCreation(newRegionInSMNode, UML.getRegion_Subvertex(), SMD_PSEUDOSTATE_IN_REGION_NAME, PseudostateKind.FORK_LITERAL);
        this.assertPseudostateCreation(newRegionInSMNode, UML.getRegion_Subvertex(), SMD_PSEUDOSTATE_IN_REGION_NAME, PseudostateKind.JOIN_LITERAL);
        this.assertPseudostateCreation(newRegionInSMNode, UML.getRegion_Subvertex(), SMD_PSEUDOSTATE_IN_REGION_NAME, PseudostateKind.CHOICE_LITERAL);
        this.assertPseudostateCreation(newRegionInSMNode, UML.getRegion_Subvertex(), SMD_PSEUDOSTATE_IN_REGION_NAME, PseudostateKind.SHALLOW_HISTORY_LITERAL);
        this.assertPseudostateCreation(newRegionInSMNode, UML.getRegion_Subvertex(), SMD_PSEUDOSTATE_IN_REGION_NAME, PseudostateKind.DEEP_HISTORY_LITERAL);
        this.assertPseudostateCreation(newRegionInSMNode, UML.getRegion_Subvertex(), SMD_PSEUDOSTATE_IN_REGION_NAME, PseudostateKind.JOIN_LITERAL);

        this.getServiceTester().assertChildCreation(newRegionInSMNode, UML.getFinalState(), UML.getRegion_Subvertex(), SMD_FINALSTATE_NODE_NAME);

        Node stateInSMRegion = this.getServiceTester().assertChildCreation(regionInSMNode, UML.getState(), UML.getRegion_Subvertex(), SMD_STATE_NODE_NAME);
        this.getServiceTester().assertChildCreation(stateInSMRegion, UML.getRegion(), UML.getState_Region(), SMD_REGION_NODE_NAME);

        this.assertPseudostateCreation(stateInSMRegion, UML.getState_ConnectionPoint(), SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_NAME, PseudostateKind.ENTRY_POINT_LITERAL);
        this.assertPseudostateCreation(stateInSMRegion, UML.getState_ConnectionPoint(), SMD_PSEUDOSTATE_BORDERNODE_IN_STATE_NAME, PseudostateKind.EXIT_POINT_LITERAL);
    }

    /**
     * Copied from super.assertChildCreation<br/>
     * This method calls createPseudoState instead of diagramService.create.
     */
    protected Node assertPseudostateCreation(Node visualParent, EReference containementRef, String expectedNodeDescriptionId, PseudostateKind entryPointLiteral) {
        EObject semanticOwner = this.getDiagramHelper().getSemanticElement(visualParent);
        EClass type = UML.getPseudostate();

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService().createPseudoState(semanticOwner, type.getName(), containementRef.getName(), visualParent, context,
                    this.getDiagramHelper().getConvertedNodes(), entryPointLiteral.toString());
            assertTrue(type.isInstance(aNewElement));
            assertEquals(semanticOwner, aNewElement.eContainer());
            if (containementRef.isMany()) {
                assertTrue(((Collection<?>) semanticOwner.eGet(containementRef)).contains(aNewElement));
            } else {
                assertEquals(semanticOwner.eGet(containementRef), aNewElement);
            }
            return aNewElement;
        });
        assertTrue(newElement instanceof Pseudostate);
        assertEquals(entryPointLiteral, ((Pseudostate) newElement).getKind());

        return this.getDiagramHelper().assertGetUniqueMatchingNodeIn(expectedNodeDescriptionId, visualParent, newElement);
    }

    private void init() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);
        this.stateMachine = this.createIn(StateMachine.class, pack);
        this.entryPointInSM = this.createIn(Pseudostate.class, this.stateMachine);
        this.entryPointInSM.setName(ENTRYPOINT_IN_SM);
        this.entryPointInSM.setKind(PseudostateKind.ENTRY_POINT_LITERAL);

        this.regionInSM = this.createIn(Region.class, this.stateMachine);
        this.state1 = this.createIn(State.class, this.regionInSM);
        this.state1.setName(STATE1);
        this.entryPointInState1 = this.createIn(Pseudostate.class, this.state1);
        this.entryPointInState1.setName(ENTRYPOINT_IN_STATE1);
        this.entryPointInState1.setKind(PseudostateKind.ENTRY_POINT_LITERAL);

        this.regionInState1 = this.createIn(Region.class, this.state1);
        this.stateInState1 = this.createIn(State.class, this.regionInState1);

        this.transition = this.createIn(Transition.class, this.regionInSM);
        this.transition.setSource(this.entryPointInSM);
        this.transition.setTarget(this.state1);

        this.getDiagramHelper().init(this.stateMachine, SMDDiagramDescriptionBuilder.SMD_REP_NAME);

        this.getDiagramHelper().refresh();
    }

    @Override
    protected AbstractDiagramService buildService() {
        return new StateMachineDiagramService(this.getIdentityService(), this.getLabelService(),
                this.getObjectSearchService(), this.getDiagramNavigationService(), this.getDiagramOperationsService(),
                e -> true, this.getViewDiagramDescriptionService(),
                new MockLogger());
    }

    @Override
    protected StateMachineDiagramService getDiagramService() {
        return (StateMachineDiagramService) super.getDiagramService();
    }

}

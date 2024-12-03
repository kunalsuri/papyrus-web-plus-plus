/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo.
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
 *  Aurelien Didier (Artal Technologies) - Issue 199
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issues 219, 227
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.CONVERTED_NODES;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DIAGRAM_CONTEXT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SELECTED_NODE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.uml.domain.services.create.ElementCreator;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.papyrus.web.application.representations.view.aql.Variables;
import org.eclipse.papyrus.web.application.representations.view.builders.NodeDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.view.builders.NoteStyleDescriptionBuilder;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesFactory;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.HeaderSeparatorDisplayMode;
import org.eclipse.sirius.components.view.diagram.ImageNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.NodeToolSection;
import org.eclipse.sirius.components.view.diagram.OutsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.OutsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.uml2.uml.AcceptCallAction;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.ActionInputPin;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityFinalNode;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.uml2.uml.ActivityPartition;
import org.eclipse.uml2.uml.AddStructuralFeatureValueAction;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.CallOperationAction;
import org.eclipse.uml2.uml.ClearAssociationAction;
import org.eclipse.uml2.uml.ClearStructuralFeatureAction;
import org.eclipse.uml2.uml.ConditionalNode;
import org.eclipse.uml2.uml.ControlFlow;
import org.eclipse.uml2.uml.CreateObjectAction;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.DestroyObjectAction;
import org.eclipse.uml2.uml.ExpansionNode;
import org.eclipse.uml2.uml.ExpansionRegion;
import org.eclipse.uml2.uml.FlowFinalNode;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.InterruptibleActivityRegion;
import org.eclipse.uml2.uml.JoinNode;
import org.eclipse.uml2.uml.LoopNode;
import org.eclipse.uml2.uml.MergeNode;
import org.eclipse.uml2.uml.ObjectFlow;
import org.eclipse.uml2.uml.OpaqueAction;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.ReadExtentAction;
import org.eclipse.uml2.uml.ReadIsClassifiedObjectAction;
import org.eclipse.uml2.uml.ReadSelfAction;
import org.eclipse.uml2.uml.ReadStructuralFeatureAction;
import org.eclipse.uml2.uml.ReclassifyObjectAction;
import org.eclipse.uml2.uml.ReduceAction;
import org.eclipse.uml2.uml.SendObjectAction;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.SequenceNode;
import org.eclipse.uml2.uml.StartClassifierBehaviorAction;
import org.eclipse.uml2.uml.StartObjectBehaviorAction;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.TestIdentityAction;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValuePin;
import org.eclipse.uml2.uml.ValueSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder of the "Activity Diagram" diagram representation.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class ADDiagramDescriptionBuilder extends AbstractRepresentationDescriptionBuilder {

    /**
     * The prefix of the representation handled by this builder.
     */
    public static final String AD_PREFIX = "AD_";

    /**
     * The name of the representation handled by this builder.
     */
    public static final String AD_REP_NAME = "Activity Diagram";

    /**
     * The suffix used to identify <i>receptions</i> compartments.
     */
    public static final String SYMBOLS_COMPARTMENT_SUFFIX = "Symbols";

    /**
     * The name used to identify the Tool section.
     */
    public static final String SHOW_HIDE = "SHOW_HIDE";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementCreator.class);

    /**
     * The width/height to use for border nodes.
     */
    private static final String BORDER_NODE_SIZE = "10";

    /**
     * The size of the border radius to use for rounded nodes.
     *
     * @see NodeStyleDescription#setBorderRadius(int)
     */
    private static final int BORDER_RADIUS_SIZE = 10;

    /**
     * The width/height to use for container nodes.
     */
    private static final String CONTAINER_NODE_SIZE = "100";

    /**
     * The condition to use in input/output pin conditional styles.
     */
    private static final String PIN_CONDITIONAL_STYLE_CONDITION = "aql:not(self.incoming->isEmpty()) or not(self.outgoing->isEmpty())";

    /**
     * 50px size Node.
     */
    private static final String SIZE_50 = "50";

    /**
     * 30px size Node.
     */
    private static final String SIZE_30 = "30";

    /**
     * Activity Group tool section name.
     */
    private static final String ACTIVITY_GROUP = "Activity Group";

    /**
     * Activity Node tool section name.
     */
    private static final String ACTIVITY_NODE = "Activity Node";

    /**
     * Pin tool section name.
     */
    private static final String PIN = "Pin";

    /**
     * Expansion Region tool section name.
     */
    private static final String EXPANSION_REGION = "Expansion Region";

    /**
     * Invocation Action tool section name.
     */
    private static final String INVOCATION_ACTION = "Invocation Action";

    /**
     * Create Object Action tool section name.
     */
    private static final String CREATE_OBJECT_ACTION = "Create Object Action";

    /**
     * Structured Activity Node tool section name.
     */
    private static final String STRUCTURED_ACTIVITY_NODE = "Structured Activity Node";

    /**
     * Structural Feature tool section name.
     */
    private static final String STRUCTURAL_FEATURE = "Structural Feature";

    /**
     * Executable node tool section name.
     */
    private static final String EXECUTABLE_NODE = "Executable Node";

    /**
     * Accept Event Action tool section name.
     */
    private static final String ACCEPT_EVENT_ACTION = "Accept Event Action";

    /**
     * The image to use for connected pins.
     */
    private static final String CONNECTED_PIN_IMAGE = "view/images/ConnectedPin.svg";

    /**
     * The {@link UMLPackage} used to access the UML metamodel.
     */
    private UMLPackage umlPackage = UMLPackage.eINSTANCE;

    /**
     * The list of semantic types that are represented as border nodes.
     * <p>
     * This list is used to filter drag & drop targets and prevent border nodes from being droppable.
     * </p>
     */
    private final List<EClass> borderNodeTypes = List.of(
            this.umlPackage.getActionInputPin(),
            this.umlPackage.getActivityParameterNode(),
            this.umlPackage.getExpansionNode(),
            this.umlPackage.getInputPin(),
            this.umlPackage.getOutputPin(),
            this.umlPackage.getValuePin());

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription adSharedDescription;

    /**
     * Initializes the builder.
     */
    public ADDiagramDescriptionBuilder() {
        super(AD_PREFIX, AD_REP_NAME, UMLPackage.eINSTANCE.getNamedElement());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {
        diagramDescription.setPreconditionExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.CAN_CREATE_DIAGRAM));

        DiagramToolSection showHideToolSection = this.getViewBuilder().createDiagramToolSection(SHOW_HIDE);
        diagramDescription.getPalette().getToolSections().add(showHideToolSection);
        this.createHideSymbolTool(diagramDescription,
                SHOW_HIDE);
        this.createShowSymbolTool(diagramDescription, SHOW_HIDE);
        this.createHideAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);
        this.createShowAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);

        this.createActivityTopNodeDescription(diagramDescription);
        this.createObjectFlowEdgeDescription(diagramDescription);
        this.createControlFlowEdgeDescription(diagramDescription);
        this.createSharedNodeDescriptions(diagramDescription);

        List<EClass> symbolOwners = List.of(
                this.umlPackage.getAction(),
                this.umlPackage.getActivity(),
                this.umlPackage.getActivityGroup(),
                this.umlPackage.getActivityPartition(),
                this.umlPackage.getRegion(),
                this.umlPackage.getClass_(),
                this.umlPackage.getSignal());
        List<EClass> forbiddenOwners = List.of(
                this.umlPackage.getObjectNode(),
                this.umlPackage.getControlNode(),
                this.umlPackage.getAcceptEventAction(),
                this.umlPackage.getSendSignalAction(),
                this.umlPackage.getActivityParameterNode());
        this.createSymbolSharedNodeDescription(diagramDescription, this.adSharedDescription, symbolOwners, forbiddenOwners, SYMBOLS_COMPARTMENT_SUFFIX);

        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));
    }

    private void createSharedNodeDescriptions(DiagramDescription diagramDescription) {
        List<EClass> commentOwners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityPartition(), //
                this.umlPackage.getConditionalNode(), //
                this.umlPackage.getExpansionRegion(), //
                this.umlPackage.getInterruptibleActivityRegion(), //
                this.umlPackage.getLoopNode(), //
                this.umlPackage.getSequenceNode(), //
                this.umlPackage.getStructuredActivityNode());
        List<EClass> constraintOwners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getConditionalNode(), //
                this.umlPackage.getExpansionRegion(), //
                this.umlPackage.getLoopNode(), //
                this.umlPackage.getSequenceNode(), //
                this.umlPackage.getStructuredActivityNode());
        this.adSharedDescription = this.createSharedDescription(diagramDescription);
        this.createCommentSubNodeDescription(diagramDescription, this.adSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getComment(), SHARED_SUFFIX), commentOwners);
        this.createConstraintSubNodeDescription(diagramDescription, this.adSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getConstraint(), SHARED_SUFFIX), constraintOwners);
        this.createAcceptCallActionSharedNodeDescription(diagramDescription);
        this.createAcceptEventActionSharedNodeDescription(diagramDescription);
        this.createActionInputPinSharedNodeDescription(diagramDescription);
        this.createActivityFinalNodeSharedNodeDescription(diagramDescription);
        this.createActivityParameterNodeSharedNodeDescription(diagramDescription);
        this.createActivityPartitionSharedNodeDescription(diagramDescription);
        this.createAddStructuralFeatureValueActionSharedNodeDescription(diagramDescription);
        this.createBroadcastSignalActionSharedNodeDescription(diagramDescription);
        this.createCallBehaviorActionSharedNodeDescription(diagramDescription);
        this.createCallOperationActionSharedNodeDescription(diagramDescription);
        this.createClearAssociationActionSharedNodeDescription(diagramDescription);
        this.createClearStructuralFeatureActionSharedNodeDescription(diagramDescription);
        this.createConditionalNodeSharedNodeDescription(diagramDescription);
        this.createCreateObjectActionSharedNodeDescription(diagramDescription);
        this.createDecisionNodeSharedNodeDescription(diagramDescription);
        this.createDestroyObjectActionSharedNodeDescription(diagramDescription);
        this.createExpansionRegionSharedNodeDescription(diagramDescription);
        this.createFlowFinalNodeSharedNodeDescription(diagramDescription);
        this.createForkNodeSharedNodeDescription(diagramDescription);
        this.createInitialNodeSharedNodeDescription(diagramDescription);
        this.createInputPinSharedNodeDescription(diagramDescription);
        this.createInterruptibleActivityRegionSharedNodeDescription(diagramDescription);
        this.createJoinNodeSharedNodeDescription(diagramDescription);
        this.createLoopNodeSharedNodeDescription(diagramDescription);
        this.createMergeNodeSharedNodeDescription(diagramDescription);
        this.createOpaqueActionSharedNodeDescription(diagramDescription);
        this.createOutputPinSharedNodeDescription(diagramDescription);
        this.createReadExtentActionSharedNodeDescription(diagramDescription);
        this.createReadIsClassifiedObjectActionSharedNodeDescription(diagramDescription);
        this.createReadSelfActionSharedNodeDescription(diagramDescription);
        this.createReadStructuralFeatureActionSharedNodeDescription(diagramDescription);
        this.createReclassifyObjectActionSharedNodeDescription(diagramDescription);
        this.createReduceActionSharedNodeDescription(diagramDescription);
        this.createSendObjectActionSharedNodeDescription(diagramDescription);
        this.createSendSignalActionSharedNodeDescription(diagramDescription);
        this.createSequenceNodeSharedNodeDescription(diagramDescription);
        this.createStartClassifierBehaviorActionSharedNodeDescription(diagramDescription);
        this.createStartObjectBehaviorActionSharedNodeDescription(diagramDescription);
        this.createStructuredActivityNodeSharedNodeDescription(diagramDescription);
        this.createActivitySharedNodeDescription(diagramDescription);
        this.createTestIdentityActionSharedNodeDescription(diagramDescription);
        this.createValuePinSharedNodeDescription(diagramDescription);
        this.createValueSpecificationActionSharedNodeDescription(diagramDescription);
    }

    /**
     * Creates the {@link NodeDescription} representing the UML root {@link Activity}.
     * <p>
     * This method creates the {@link NodeDescription} that represents the root activity of the diagram. See
     * {@link #createSharedSubActivityDescription(NodeDescription, DiagramDescription)} to create the
     * {@link NodeDescription} representing sub-{@link Activity} elements.
     * </p>
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription} containing the created {@link NodeDescription}
     *
     * @see #createSharedSubActivityDescription(NodeDescription, DiagramDescription)
     */
    private NodeDescription createActivityTopNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);

        EClass activityEClass = this.umlPackage.getActivity();
        NodeDescription adActivityTopNodeDescription = this.newNodeBuilder(activityEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getDomainNodeName(activityEClass)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(this.getQueryBuilder().querySelf()) //
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(activityEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        adActivityTopNodeDescription.setDefaultWidthExpression(ROOT_ELEMENT_WIDTH);
        adActivityTopNodeDescription.setDefaultHeightExpression(ROOT_ELEMENT_HEIGHT);
        diagramDescription.getNodeDescriptions().add(adActivityTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adActivityTopNodeDescription);
        this.addToolSections(adActivityTopNodeDescription, ACTIVITY_GROUP, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        DropNodeTool adActivityGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adActivityTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityNode(), this.umlPackage.getActivityPartition(), this.umlPackage.getComment(),
                this.umlPackage.getConstraint(), this.umlPackage.getInterruptibleActivityRegion());
        this.registerCallback(adActivityTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            adActivityGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adActivityTopNodeDescription.getPalette().setDropNodeTool(adActivityGraphicalDropTool);

        return adActivityTopNodeDescription;
    }

    private void addToolSections(NodeDescription parentNodeDescription, String... toolSectionNames) {
        for (String toolSectionName : toolSectionNames) {
            NodeToolSection toolSection = this.getViewBuilder().createNodeToolSection(toolSectionName);
            parentNodeDescription.getPalette().getToolSections().add(toolSection);
        }
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link AcceptCallAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createAcceptCallActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adAcceptCallActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getAcceptCallAction());
        this.adSharedDescription.getChildrenDescriptions().add(adAcceptCallActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adAcceptCallActionSharedNodeDescription);
        this.addToolSections(adAcceptCallActionSharedNodeDescription, PIN);

        NodeTool adAcceptCallActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getAcceptCallAction());
        this.reuseNodeAndCreateTool(adAcceptCallActionSharedNodeDescription, diagramDescription, adAcceptCallActionSharedNodeCreationTool, ACCEPT_EVENT_ACTION, this.umlPackage.getActivity(),
                this.umlPackage.getActivityGroup());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link AcceptEventAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createAcceptEventActionSharedNodeDescription(DiagramDescription diagramDescription) {
        // create hourglass conditional style
        ImageNodeStyleDescription adHourglassNodeStyleDescription = this.getViewBuilder().createImageNodeStyle("view/images/AcceptTimeEventAction.svg");
        String timeTriggeredCondition = "aql:self.isEventTriggeredAcceptEventAction()";
        ConditionalNodeStyle hourglassConditionalStyle = this.getViewBuilder().createConditionalNodeStyle(timeTriggeredCondition, //
                adHourglassNodeStyleDescription);

        // create AcceptEventAction node description
        EClass acceptEventActionEClass = this.umlPackage.getAcceptEventAction();
        NodeStyleDescription adFlagNodeStyleDescription = PapyrusCustomNodesFactory.eINSTANCE.createInnerFlagNodeStyleDescription();
        adFlagNodeStyleDescription.setBorderColor(this.styleProvider.getBorderNodeColor());
        adFlagNodeStyleDescription.setBorderRadius(this.styleProvider.getNodeBorderRadius());

        InsideLabelDescription insideLabelDescription = DiagramFactory.eINSTANCE.createInsideLabelDescription();
        insideLabelDescription.setTextAlign(LabelTextAlign.CENTER);
        insideLabelDescription.setLabelExpression("aql:self.getAcceptEventActionLabel(true)");
        InsideLabelStyle style = this.getViewBuilder().createDefaultInsideLabelStyle(false, false);
        style.setShowIconExpression("aql:not self.isEventTriggeredAcceptEventAction()");
        insideLabelDescription.setStyle(style);

        OutsideLabelDescription outsiteLabelDescription = this.getViewBuilder().createOutsideLabelDescription("aql:self.getAcceptEventActionLabel(false)", false);
        outsiteLabelDescription.setPosition(OutsideLabelPosition.BOTTOM_CENTER);
        outsiteLabelDescription.getStyle().setShowIconExpression(timeTriggeredCondition);

        NodeDescription adAcceptEventActionSharedNodeDescription = this.newNodeBuilder(acceptEventActionEClass, adFlagNodeStyleDescription) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(acceptEventActionEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(acceptEventActionEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(acceptEventActionEClass.getName())) //
                .conditionalStyles(List.of(hourglassConditionalStyle)) //
                .insideLabelDescription(insideLabelDescription)
                .addOutsideLabelDescription(outsiteLabelDescription)
                .build();
        adAcceptEventActionSharedNodeDescription.setDefaultWidthExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.COMPUTE_ACCEPT_EVENT_ACTION_WIDTH));
        adAcceptEventActionSharedNodeDescription.setDefaultHeightExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.COMPUTE_ACCEPT_EVENT_ACTION_HEIGHT));

        this.adSharedDescription.getChildrenDescriptions().add(adAcceptEventActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adAcceptEventActionSharedNodeDescription);
        this.addToolSections(adAcceptEventActionSharedNodeDescription, PIN);

        NodeTool adAcceptEventActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getAcceptEventAction());
        this.reuseNodeAndCreateTool(adAcceptEventActionSharedNodeDescription, diagramDescription, adAcceptEventActionSharedNodeCreationTool, ACCEPT_EVENT_ACTION, this.umlPackage.getActivity(),
                this.umlPackage.getActivityGroup());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ActionInputPin}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createActionInputPinSharedNodeDescription(DiagramDescription diagramDescription) {
        ImageNodeStyleDescription actionInputPinStyle = this.getViewBuilder().createImageNodeStyle(this.getImageForDomainType(this.umlPackage.getActionInputPin()));
        actionInputPinStyle.setPositionDependentRotation(true);
        NodeStyleDescription incomingOutgoingNodeStyleDescription = this.getViewBuilder().createImageNodeStyle(CONNECTED_PIN_IMAGE);
        ConditionalNodeStyle incomingOutgoingConditionalStyle = this.getViewBuilder().createConditionalNodeStyle(PIN_CONDITIONAL_STYLE_CONDITION, incomingOutgoingNodeStyleDescription);

        NodeDescription adActionInputPinSharedNodeDescription = new NodeDescriptionBuilder(this.getIdBuilder(), this.getQueryBuilder(), this.umlPackage.getActionInputPin(), actionInputPinStyle,
                this.getUmlMetaModelHelper()).name(this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getActionInputPin(), SHARED_SUFFIX)) //
                        .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED) //
                        .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTION_INPUT_PIN_CANDIDATES)).conditionalStyles(List.of(incomingOutgoingConditionalStyle)) //
                        .addOutsideLabelDescription(this.getViewBuilder().createOutsideLabelDescription(this.getQueryBuilder().queryRenderLabel(), true))
                        .build();
        adActionInputPinSharedNodeDescription.setDefaultWidthExpression(BORDER_NODE_SIZE);
        adActionInputPinSharedNodeDescription.setDefaultHeightExpression(BORDER_NODE_SIZE);
        this.getViewBuilder().addDirectEditTool(adActionInputPinSharedNodeDescription);
        this.getViewBuilder().addDefaultDeleteTool(adActionInputPinSharedNodeDescription);
        this.adSharedDescription.getBorderNodesDescriptions().add(adActionInputPinSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adActionInputPinSharedNodeDescription);

        NodeTool adActionInputPinSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getActionInputPin().getName(),
                this.getIdBuilder().getCreationToolId(this.umlPackage.getActionInputPin()),
                ActivityDiagramServices.CREATE_ACTION_INPUT_PIN, List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        adActionInputPinSharedNodeCreationTool.setPreconditionExpression(
                CallQuery.queryServiceOnSelf(ActivityDiagramServices.CAN_CREATE_INTO_PARENT, this.getQueryBuilder().aqlString(this.umlPackage.getActionInputPin().getName())));
        List<EClass> owners = List.of(this.umlPackage.getAddStructuralFeatureValueAction(), //
                this.umlPackage.getAddVariableValueAction(), //
                this.umlPackage.getBroadcastSignalAction(), //
                this.umlPackage.getClearAssociationAction(), //
                this.umlPackage.getClearStructuralFeatureAction(), //
                this.umlPackage.getConditionalNode(), //
                this.umlPackage.getCreateLinkAction(), //
                this.umlPackage.getCreateLinkObjectAction(), //
                this.umlPackage.getDestroyLinkAction(), //
                this.umlPackage.getDestroyObjectAction(), //
                this.umlPackage.getExpansionRegion(), //
                this.umlPackage.getLoopNode(), //
                this.umlPackage.getOpaqueAction(), //
                this.umlPackage.getReadIsClassifiedObjectAction(), //
                this.umlPackage.getReadLinkAction(), //
                this.umlPackage.getReclassifyObjectAction(), //
                this.umlPackage.getReduceAction(), //
                this.umlPackage.getSequenceNode(), //
                this.umlPackage.getStartObjectBehaviorAction(), //
                this.umlPackage.getStructuredActivityNode(), //
                this.umlPackage.getUnmarshallAction(), //
                this.umlPackage.getCallBehaviorAction(), //
                this.umlPackage.getCallOperationAction(), //
                this.umlPackage.getReadStructuralFeatureAction(), //
                this.umlPackage.getSendObjectAction(), //
                this.umlPackage.getSendSignalAction(), //
                this.umlPackage.getStartClassifierBehaviorAction(), //
                this.umlPackage.getTestIdentityAction() //
        );
        this.reuseNodeAndCreateTool(adActionInputPinSharedNodeDescription, diagramDescription, adActionInputPinSharedNodeCreationTool, PIN, owners.toArray(EClass[]::new));
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link ActivityFinalNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     *
     * @see #createSharedCustomImageNodeDescription(NodeDescription, EClass, EReference, DiagramDescription)
     */
    private void createActivityFinalNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createSharedCustomImageActivityNodeDescription(this.umlPackage.getActivityFinalNode(), this.umlPackage.getActivity_OwnedNode(), diagramDescription);
    }

    /**
     * Utility method easing the definition of {@link ActivityNode} creation tools.
     * <p>
     * This method is a shortcut for {@link #createCreationTool(String, String, List)} with a preset service that can be
     * used for all the {@link ActivityNode} subclasses. Use {@link #createCreationTool(String, String, List)} to create
     * a creation tool with a custom creation service.
     * </p>
     *
     * @return the created {@link NodeTool}
     * @see #createCreationTool(String, String, List)
     */
    private NodeTool createActivityNodeCreationTool(EClass newType) {
        return this.getViewBuilder().createCreationTool(newType.getName(), this.getIdBuilder().getCreationToolId(newType), ActivityDiagramServices.CREATE_ACTIVITY_NODE,
                List.of(this.getQueryBuilder().aqlString(newType.getName()), this.getQueryBuilder().aqlString(this.umlPackage.getActivity_OwnedNode().getName()), SELECTED_NODE, DIAGRAM_CONTEXT,
                        CONVERTED_NODES));
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ActivityParameterNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createActivityParameterNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeStyleDescription adActivityParameterNodeNodeStyleDescription = this.getViewBuilder().createRectangularNodeStyle();
        EClass activityParameterNodeEClass = this.umlPackage.getActivityParameterNode();

        NodeDescription adActivityParameterNodeSharedNodeDescription = this.newNodeBuilder(activityParameterNodeEClass, adActivityParameterNodeNodeStyleDescription)
                .name(this.getIdBuilder().getSpecializedDomainNodeName(activityParameterNodeEClass, SHARED_SUFFIX))
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES))
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(activityParameterNodeEClass.getName()))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(activityParameterNodeEClass.getName()))
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, false))
                .build();

        adActivityParameterNodeSharedNodeDescription.setDefaultWidthExpression("80");
        adActivityParameterNodeSharedNodeDescription.setDefaultHeightExpression("20");

        this.adSharedDescription.getBorderNodesDescriptions().add(adActivityParameterNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adActivityParameterNodeSharedNodeDescription);

        NodeTool adActivityParameterNodeSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getActivity_OwnedNode(), this.umlPackage.getActivityParameterNode());
        List<EClass> owners = List.of(this.umlPackage.getActivity());
        this.reuseNodeAndCreateTool(adActivityParameterNodeSharedNodeDescription, diagramDescription, adActivityParameterNodeSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ActivityPartition}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createActivityPartitionSharedNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();

        EClass activityPartitionEClass = this.umlPackage.getActivityPartition();
        InsideLabelStyle labelStyle = this.getViewBuilder().createDefaultInsideLabelStyleIcon();
        labelStyle.setWithHeader(true);
        labelStyle.setHeaderSeparatorDisplayMode(HeaderSeparatorDisplayMode.IF_CHILDREN);
        NodeDescription adActivityPartitionSharedNodeDescription = this.newNodeBuilder(activityPartitionEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(activityPartitionEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_PARTITION_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(activityPartitionEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(activityPartitionEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), labelStyle)
                .build();
        adActivityPartitionSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adActivityPartitionSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.adSharedDescription.getChildrenDescriptions().add(adActivityPartitionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adActivityPartitionSharedNodeDescription);
        this.addToolSections(adActivityPartitionSharedNodeDescription, ACTIVITY_GROUP, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE,
                STRUCTURAL_FEATURE, EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adActivityPartitionSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getActivityPartition_Subpartition(), activityPartitionEClass);
        this.reuseNodeAndCreateTool(adActivityPartitionSharedNodeDescription, diagramDescription, adActivityPartitionSharedNodeCreationTool, ACTIVITY_GROUP, activityPartitionEClass);
        NodeTool activityPartitionNodeTool = this.getViewBuilder().createCreationTool(this.umlPackage.getActivity_Partition(), activityPartitionEClass);
        this.reuseNodeAndCreateTool(adActivityPartitionSharedNodeDescription, diagramDescription, activityPartitionNodeTool, ACTIVITY_GROUP, this.umlPackage.getActivity());

        DropNodeTool adActivityPartitionGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adActivityPartitionSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getActivityPartition(), this.umlPackage.getComment());
        this.registerCallback(adActivityPartitionSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            adActivityPartitionGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adActivityPartitionSharedNodeDescription.getPalette().setDropNodeTool(adActivityPartitionGraphicalDropTool);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link AddStructuralFeatureValueAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createAddStructuralFeatureValueActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adAddStructuralFeatureValueActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getAddStructuralFeatureValueAction());
        this.adSharedDescription.getChildrenDescriptions().add(adAddStructuralFeatureValueActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adAddStructuralFeatureValueActionSharedNodeDescription);
        this.addToolSections(adAddStructuralFeatureValueActionSharedNodeDescription, PIN);

        NodeTool adAddStructuralFeatureValueActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getAddStructuralFeatureValueAction());
        this.reuseNodeAndCreateTool(adAddStructuralFeatureValueActionSharedNodeDescription, diagramDescription, adAddStructuralFeatureValueActionSharedNodeCreationTool, STRUCTURAL_FEATURE,
                this.umlPackage.getActivity(), this.umlPackage.getActivityGroup());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link BroadCastSignalAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createBroadcastSignalActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adBroadcastSignalActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getBroadcastSignalAction());
        this.adSharedDescription.getChildrenDescriptions().add(adBroadcastSignalActionSharedNodeDescription);
        this.createDefaultToolSectionsInNodeDescription(adBroadcastSignalActionSharedNodeDescription);
        this.addToolSections(adBroadcastSignalActionSharedNodeDescription, PIN);

        NodeTool adBroadcastSignalActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getBroadcastSignalAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adBroadcastSignalActionSharedNodeDescription, diagramDescription, adBroadcastSignalActionSharedNodeCreationTool, INVOCATION_ACTION, owners,
                List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link CallBehaviorAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createCallBehaviorActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adCallBehaviorActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getCallBehaviorAction());
        this.adSharedDescription.getChildrenDescriptions().add(adCallBehaviorActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adCallBehaviorActionSharedNodeDescription);
        this.addToolSections(adCallBehaviorActionSharedNodeDescription, PIN);

        NodeTool adCallBehaviorActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getCallBehaviorAction());
        this.reuseNodeAndCreateTool(adCallBehaviorActionSharedNodeDescription, diagramDescription, adCallBehaviorActionSharedNodeCreationTool, INVOCATION_ACTION, this.umlPackage.getActivity(),
                this.umlPackage.getActivityGroup());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link CallOperationAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createCallOperationActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adCallOperationActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getCallOperationAction());
        this.adSharedDescription.getChildrenDescriptions().add(adCallOperationActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adCallOperationActionSharedNodeDescription);
        this.addToolSections(adCallOperationActionSharedNodeDescription, PIN);

        NodeTool adCallOperationActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getCallOperationAction());
        this.reuseNodeAndCreateTool(adCallOperationActionSharedNodeDescription, diagramDescription, adCallOperationActionSharedNodeCreationTool, INVOCATION_ACTION, this.umlPackage.getActivity(),
                this.umlPackage.getActivityGroup());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ClearAssociationAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createClearAssociationActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adClearAssociationActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getClearAssociationAction());
        this.adSharedDescription.getChildrenDescriptions().add(adClearAssociationActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adClearAssociationActionSharedNodeDescription);
        this.addToolSections(adClearAssociationActionSharedNodeDescription, PIN);

        NodeTool adClearAssociationActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getClearAssociationAction());
        this.reuseNodeAndCreateTool(adClearAssociationActionSharedNodeDescription, diagramDescription, adClearAssociationActionSharedNodeCreationTool, EXECUTABLE_NODE,
                List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ClearStructuralFeatureAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createClearStructuralFeatureActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adClearStructuralFeatureActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getClearStructuralFeatureAction());
        this.adSharedDescription.getChildrenDescriptions().add(adClearStructuralFeatureActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adClearStructuralFeatureActionSharedNodeDescription);
        this.addToolSections(adClearStructuralFeatureActionSharedNodeDescription, PIN);

        NodeTool adClearStructuralFeatureActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getClearStructuralFeatureAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adClearStructuralFeatureActionSharedNodeDescription, diagramDescription, adClearStructuralFeatureActionSharedNodeCreationTool, STRUCTURAL_FEATURE, owners,
                List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ConditionalNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createConditionalNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);
        rectangularNodeStyle.setBorderLineStyle(LineStyle.DASH);
        EClass conditionalNodeEClass = this.umlPackage.getConditionalNode();
        NodeDescription adConditionalNodeSharedNodeDescription = this.newNodeBuilder(conditionalNodeEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(conditionalNodeEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(conditionalNodeEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(conditionalNodeEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, true))
                .build();
        adConditionalNodeSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adConditionalNodeSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.adSharedDescription.getChildrenDescriptions().add(adConditionalNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adConditionalNodeSharedNodeDescription);
        this.addToolSections(adConditionalNodeSharedNodeDescription, PIN, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adConditionalNodeSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(conditionalNodeEClass);
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adConditionalNodeSharedNodeDescription, diagramDescription, adConditionalNodeSharedNodeCreationTool, STRUCTURED_ACTIVITY_NODE, owners, List.of());

        DropNodeTool adConditionalNodeGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adConditionalNodeSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adConditionalNodeSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            adConditionalNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adConditionalNodeSharedNodeDescription.getPalette().setDropNodeTool(adConditionalNodeGraphicalDropTool);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link CreateObjectAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createCreateObjectActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adCreateObjectActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getCreateObjectAction());
        this.adSharedDescription.getChildrenDescriptions().add(adCreateObjectActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adCreateObjectActionSharedNodeDescription);
        this.addToolSections(adCreateObjectActionSharedNodeDescription, PIN);

        NodeTool adCreateObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getCreateObjectAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adCreateObjectActionSharedNodeDescription, diagramDescription, adCreateObjectActionSharedNodeCreationTool, CREATE_OBJECT_ACTION, owners,
                List.of());
    }

    /**
     * Creates an image-based {@link NodeDescription} representing the provided {@code domainType} with the given
     * {@code containmentReference}.
     *
     * @param domainType
     *            the type of the element to represent
     * @param containmentReference
     *            the containment reference of the element to represent
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createSharedCustomImageActivityNodeDescription(EClass domainType, EReference containmentReference, DiagramDescription diagramDescription) {
        ImageNodeStyleDescription imageNodeStyle = this.getViewBuilder().createImageNodeStyle(this.getImageForDomainType(domainType));

        NodeDescription adCustomImageActivityNodeSharedNodeDescription = this.newNodeBuilder(domainType, imageNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(domainType, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(domainType.getName())) //
                .addOutsideLabelDescription(this.getViewBuilder().createDefaultOutsideLabelDescription(true))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(domainType.getName())).build();
        adCustomImageActivityNodeSharedNodeDescription.setDefaultWidthExpression(SIZE_30);
        adCustomImageActivityNodeSharedNodeDescription.setDefaultHeightExpression(SIZE_30);
        this.adSharedDescription.getChildrenDescriptions().add(adCustomImageActivityNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adCustomImageActivityNodeSharedNodeDescription);

        NodeTool adCustomImageActivityNodeSharedNodeCreationTool = this.createActivityNodeCreationTool(domainType);
        this.reuseNodeAndCreateTool(adCustomImageActivityNodeSharedNodeDescription, diagramDescription, adCustomImageActivityNodeSharedNodeCreationTool, ACTIVITY_NODE,
                List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of(this.umlPackage.getSequenceNode()));
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link DecisionNode}.
     * <p>
     * This method also creates the {@link NodeDescription} and {@link EdgeDescription} for the note associated to
     * {@link DecisionNode}s with a decision input.
     * </p>
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     *
     * @see #createSharedCustomImageNodeDescription(NodeDescription, EClass, EReference, DiagramDescription)
     */
    private void createDecisionNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createSharedCustomImageActivityNodeDescription(this.umlPackage.getDecisionNode(), this.umlPackage.getActivity_OwnedNode(), diagramDescription);

        NoteNodeStyleDescription nodeStyleDescription = this.getViewBuilder().createNoteNodeStyle();
        nodeStyleDescription.setBackground(this.styleProvider.getNoteColor());

        InsideLabelStyle labelStyle = this.getViewBuilder().createDefaultInsideLabelStyle(false, false);

        NodeDescription adDecisionNodeNoteSharedNodeDescription = this.newNodeBuilder(this.umlPackage.getDecisionNode(), nodeStyleDescription) //
                .name(this.getIdBuilder().getDomainNodeName(this.umlPackage.getDecisionNode()) + "_Note_" + SHARED_SUFFIX) //
                .insideLabelDescription(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_DECISION_INPUT_NOTE_LABEL), labelStyle)
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED) //
                .build();
        adDecisionNodeNoteSharedNodeDescription
                .setPreconditionExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.SHOW_DECISION_NODE_NOTE, Variables.DIAGRAM_CONTEXT, "previousDiagram", Variables.EDITING_CONTEXT));

        adDecisionNodeNoteSharedNodeDescription.setDefaultWidthExpression(NoteStyleDescriptionBuilder.DEFAULT_NOTE_WIDTH);
        adDecisionNodeNoteSharedNodeDescription.setDefaultHeightExpression(NoteStyleDescriptionBuilder.DEFAULT_NOTE_HEIGHT);

        this.adSharedDescription.getChildrenDescriptions().add(adDecisionNodeNoteSharedNodeDescription);

        this.registerCallback(adDecisionNodeNoteSharedNodeDescription, () -> {
            Supplier<List<NodeDescription>> ownerNodeDescriptions = () -> this.collectNodesWithDomainAndFilter(diagramDescription,
                    List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of(this.umlPackage.getSequenceNode()));
            this.reusedNodeDescriptionInOwners(adDecisionNodeNoteSharedNodeDescription, ownerNodeDescriptions.get());
        });

        Predicate<NodeDescription> isDecisionNodeNoteDescription = nodeDescription -> Objects.equals(nodeDescription.getName(), adDecisionNodeNoteSharedNodeDescription.getName());

        EdgeDescription adDecisionNodeNoteEdgeDescription = this.getViewBuilder().createFeatureEdgeDescription(//
                this.getIdBuilder().getFeatureBaseEdgeId(this.umlPackage.getDecisionNode_DecisionInput()), //
                this.getQueryBuilder().emptyString(), //
                this.getQueryBuilder().querySelf(),
                () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getDecisionNode()).stream().filter(isDecisionNodeNoteDescription.negate()).toList(), //
                () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getDecisionNode()).stream().filter(isDecisionNodeNoteDescription).toList());

        adDecisionNodeNoteEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.NONE);
        adDecisionNodeNoteEdgeDescription.getStyle().setLineStyle(LineStyle.DASH);
        diagramDescription.getEdgeDescriptions().add(adDecisionNodeNoteEdgeDescription);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link DestroyObjectAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createDestroyObjectActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adDestroyObjectActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getDestroyObjectAction());
        this.adSharedDescription.getChildrenDescriptions().add(adDestroyObjectActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adDestroyObjectActionSharedNodeDescription);
        this.addToolSections(adDestroyObjectActionSharedNodeDescription, PIN);

        NodeTool adDestroyObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getDestroyObjectAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adDestroyObjectActionSharedNodeDescription, diagramDescription, adDestroyObjectActionSharedNodeCreationTool, CREATE_OBJECT_ACTION, owners,
                List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ExpansionRegion}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createExpansionRegionSharedNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);
        rectangularNodeStyle.setBorderLineStyle(LineStyle.DASH);
        EClass expansionRegionEClass = this.umlPackage.getExpansionRegion();
        NodeDescription adExpansionRegionSharedNodeDescription = this.newNodeBuilder(expansionRegionEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(expansionRegionEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(expansionRegionEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, true))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(expansionRegionEClass.getName())) //
                .build();
        adExpansionRegionSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adExpansionRegionSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.adSharedDescription.getChildrenDescriptions().add(adExpansionRegionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adExpansionRegionSharedNodeDescription);
        this.addToolSections(adExpansionRegionSharedNodeDescription, ACTIVITY_NODE, PIN, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        this.createInputOutputExpansionNodeNodeDescription(adExpansionRegionSharedNodeDescription, diagramDescription);

        NodeTool adExpansionRegionSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(expansionRegionEClass);
        List<EClass> owners = List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adExpansionRegionSharedNodeDescription, diagramDescription, adExpansionRegionSharedNodeCreationTool, EXPANSION_REGION, owners, List.of());

        DropNodeTool adExpansionRegionGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adExpansionRegionSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adExpansionRegionSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            adExpansionRegionGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adExpansionRegionSharedNodeDescription.getPalette().setDropNodeTool(adExpansionRegionGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link FlowFinalNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     *
     * @see #createSharedCustomImageNodeDescription(NodeDescription, EClass, EReference, DiagramDescription)
     */
    private void createFlowFinalNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createSharedCustomImageActivityNodeDescription(this.umlPackage.getFlowFinalNode(), this.umlPackage.getActivity_OwnedNode(), diagramDescription);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ForkNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createForkNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass forkNodeEClass = this.umlPackage.getForkNode();
        RectangleWithExternalLabelNodeStyleDescription nodeStyle = this.getViewBuilder().createRectangleWithExternalLabelNodeStyle();

        NodeDescription adForkNodeSharedNodeDescription = this.newNodeBuilder(forkNodeEClass, nodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(forkNodeEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(forkNodeEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon())
                .labelEditTool(this.getViewBuilder().createDirectEditTool(forkNodeEClass.getName())).build();
        adForkNodeSharedNodeDescription.setDefaultWidthExpression(SIZE_50);
        adForkNodeSharedNodeDescription.setDefaultHeightExpression("150");
        this.adSharedDescription.getChildrenDescriptions().add(adForkNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adForkNodeSharedNodeDescription);

        NodeTool adForkNodeSharedNodeCreationTool = this.createActivityNodeCreationTool(forkNodeEClass);
        this.reuseNodeAndCreateTool(adForkNodeSharedNodeDescription, diagramDescription, adForkNodeSharedNodeCreationTool, ACTIVITY_NODE,
                List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of(this.umlPackage.getSequenceNode()));
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link InitialNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     *
     * @see #createSharedCustomImageNodeDescription(NodeDescription, EClass, EReference, DiagramDescription)
     */
    private void createInitialNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createSharedCustomImageActivityNodeDescription(this.umlPackage.getInitialNode(), this.umlPackage.getActivity_OwnedNode(), diagramDescription);
    }

    /**
     * Creates the {@link NodeDescription}s representing UML input/output {@link ExpansionNode}.
     * <p>
     * Input and output {@link ExpansionNode} are represented with the same EClass, but are stored using different
     * containment references. This method creates the two creation tools that allow to create input and output
     * {@link ExpansionNode}.
     * </p>
     *
     * @param parentDescription
     *            the {@link NodeDescription} containing the created {@link NodeDescription}
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createInputOutputExpansionNodeNodeDescription(NodeDescription parentDescription, DiagramDescription diagramDescription) {
        ImageNodeStyleDescription imageNodeStyle = this.getViewBuilder().createImageNodeStyle(this.getImageForDomainType(this.umlPackage.getExpansionNode()));
        EClass expansionNodeEClass = this.umlPackage.getExpansionNode();

        NodeDescription adInputExpansionNodeSharedNodeDescription = this.newNodeBuilder(expansionNodeEClass, imageNodeStyle)
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_EXPANSION_NODE_CANDIDATES))
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(expansionNodeEClass.getName()))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(expansionNodeEClass.getName()))
                .addOutsideLabelDescription(this.getViewBuilder().createOutsideLabelDescription(this.getQueryBuilder().queryRenderLabel(), true))
                .build();

        adInputExpansionNodeSharedNodeDescription.setDefaultWidthExpression(SIZE_30);
        adInputExpansionNodeSharedNodeDescription.setDefaultHeightExpression("10");
        parentDescription.getBorderNodesDescriptions().add(adInputExpansionNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adInputExpansionNodeSharedNodeDescription);

        NodeTool adInputExpansionNodeSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getExpansionNode().getName(),
                "New Input " + this.umlPackage.getExpansionNode().getName(),
                ActivityDiagramServices.CREATE_EXPANSION_NODE, List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES, String.valueOf(true)));
        this.reuseNodeAndCreateTool(adInputExpansionNodeSharedNodeDescription, diagramDescription, adInputExpansionNodeSharedNodeCreationTool, EXPANSION_REGION, this.umlPackage.getExpansionRegion());
        NodeTool outputExpansionNodeTool = this.getViewBuilder().createCreationTool(this.umlPackage.getExpansionNode().getName(), "New Output " + this.umlPackage.getExpansionNode().getName(),
                ActivityDiagramServices.CREATE_EXPANSION_NODE,
                List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES, String.valueOf(false)));
        this.reuseNodeAndCreateTool(adInputExpansionNodeSharedNodeDescription, diagramDescription, outputExpansionNodeTool, EXPANSION_REGION, this.umlPackage.getExpansionRegion());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link InputPin}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createInputPinSharedNodeDescription(DiagramDescription diagramDescription) {
        ImageNodeStyleDescription inputPinStyle = this.getViewBuilder().createImageNodeStyle(this.getImageForDomainType(this.umlPackage.getInputPin()));
        inputPinStyle.setPositionDependentRotation(true);
        NodeStyleDescription incomingOutgoingNodeStyleDescription = this.getViewBuilder().createImageNodeStyle(CONNECTED_PIN_IMAGE);
        ConditionalNodeStyle incomingOutgoingConditionalStyle = this.getViewBuilder().createConditionalNodeStyle(PIN_CONDITIONAL_STYLE_CONDITION, incomingOutgoingNodeStyleDescription);

        NodeDescription adInputPinSharedNodeDescription = new NodeDescriptionBuilder(this.getIdBuilder(), this.getQueryBuilder(), this.umlPackage.getInputPin(), inputPinStyle,
                this.getUmlMetaModelHelper())
                        .name(this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getInputPin(), SHARED_SUFFIX)) //
                        .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED) //
                        .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_INPUT_PIN_CANDIDATES)).conditionalStyles(List.of(incomingOutgoingConditionalStyle)) //
                        .addOutsideLabelDescription(this.getViewBuilder().createOutsideLabelDescription(this.getQueryBuilder().queryRenderLabel(), true))
                        .build();
        adInputPinSharedNodeDescription.setDefaultWidthExpression(BORDER_NODE_SIZE);
        adInputPinSharedNodeDescription.setDefaultHeightExpression(BORDER_NODE_SIZE);
        this.getViewBuilder().addDirectEditTool(adInputPinSharedNodeDescription);
        this.getViewBuilder().addDefaultDeleteTool(adInputPinSharedNodeDescription);
        this.adSharedDescription.getBorderNodesDescriptions().add(adInputPinSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adInputPinSharedNodeDescription);

        NodeTool adInputPinSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getInputPin().getName(),
                this.getIdBuilder().getCreationToolId(this.umlPackage.getInputPin()),
                ActivityDiagramServices.CREATE_INPUT_PIN, List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        adInputPinSharedNodeCreationTool
                .setPreconditionExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.CAN_CREATE_INTO_PARENT, this.getQueryBuilder().aqlString(this.umlPackage.getInputPin().getName())));
        List<EClass> owners = List.of(this.umlPackage.getBroadcastSignalAction(), //
                this.umlPackage.getConditionalNode(), //
                this.umlPackage.getCreateLinkAction(), //
                this.umlPackage.getCreateLinkObjectAction(), //
                this.umlPackage.getDestroyLinkAction(), //
                this.umlPackage.getExpansionRegion(), //
                this.umlPackage.getLoopNode(), //
                this.umlPackage.getOpaqueAction(), //
                this.umlPackage.getClearAssociationAction(), //
                this.umlPackage.getReduceAction(), //
                this.umlPackage.getStartClassifierBehaviorAction(), //
                this.umlPackage.getReadLinkAction(), //
                this.umlPackage.getSequenceNode(), //
                this.umlPackage.getStartObjectBehaviorAction(), //
                this.umlPackage.getStructuredActivityNode(), //
                this.umlPackage.getAddStructuralFeatureValueAction(), //
                this.umlPackage.getAddVariableValueAction(), //
                this.umlPackage.getCallBehaviorAction(), //
                this.umlPackage.getCallOperationAction(), //
                this.umlPackage.getClearStructuralFeatureAction(), //
                this.umlPackage.getDestroyObjectAction(), //
                this.umlPackage.getReadIsClassifiedObjectAction(), //
                this.umlPackage.getReadStructuralFeatureAction(), //
                this.umlPackage.getReclassifyObjectAction(), //
                this.umlPackage.getSendObjectAction(), //
                this.umlPackage.getSendSignalAction(), //
                this.umlPackage.getStartClassifierBehaviorAction(), //
                this.umlPackage.getTestIdentityAction(), //
                this.umlPackage.getUnmarshallAction()//
        );
        this.reuseNodeAndCreateTool(adInputPinSharedNodeDescription, diagramDescription, adInputPinSharedNodeCreationTool, PIN, owners.toArray(EClass[]::new));
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link InterruptibleActivityRegion}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createInterruptibleActivityRegionSharedNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);
        rectangularNodeStyle.setBorderLineStyle(LineStyle.DASH);
        EClass interruptibleActivityRegionEClass = this.umlPackage.getInterruptibleActivityRegion();
        NodeDescription adInterruptibleActivityRegionSharedNodeDescription = this.newNodeBuilder(interruptibleActivityRegionEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(interruptibleActivityRegionEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_INTERRUPTIBLE_ACTIVITY_REGION_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(interruptibleActivityRegionEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(interruptibleActivityRegionEClass.getName())) //
                .build();
        adInterruptibleActivityRegionSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adInterruptibleActivityRegionSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.adSharedDescription.getChildrenDescriptions().add(adInterruptibleActivityRegionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adInterruptibleActivityRegionSharedNodeDescription);
        this.addToolSections(adInterruptibleActivityRegionSharedNodeDescription, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adInterruptibleActivityRegionSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getActivity_OwnedGroup(), interruptibleActivityRegionEClass);
        this.reuseNodeAndCreateTool(adInterruptibleActivityRegionSharedNodeDescription, diagramDescription, adInterruptibleActivityRegionSharedNodeCreationTool, ACTIVITY_GROUP,
                this.umlPackage.getActivity());

        DropNodeTool adInterruptibleActivityRegionGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adInterruptibleActivityRegionSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment());
        this.registerCallback(adInterruptibleActivityRegionSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            adInterruptibleActivityRegionGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adInterruptibleActivityRegionSharedNodeDescription.getPalette().setDropNodeTool(adInterruptibleActivityRegionGraphicalDropTool);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link JoinNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createJoinNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass joinNodeEClass = this.umlPackage.getJoinNode();
        RectangleWithExternalLabelNodeStyleDescription nodeStyle = this.getViewBuilder().createRectangleWithExternalLabelNodeStyle();

        NodeDescription adJoinNodeSharedNodeDescription = this.newNodeBuilder(joinNodeEClass, nodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(joinNodeEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(joinNodeEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon()) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(joinNodeEClass.getName())).build();
        adJoinNodeSharedNodeDescription.setDefaultWidthExpression(SIZE_50);
        adJoinNodeSharedNodeDescription.setDefaultHeightExpression("150");
        this.adSharedDescription.getChildrenDescriptions().add(adJoinNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adJoinNodeSharedNodeDescription);

        NodeTool adJoinNodeSharedNodeCreationTool = this.createActivityNodeCreationTool(joinNodeEClass);
        this.reuseNodeAndCreateTool(adJoinNodeSharedNodeDescription, diagramDescription, adJoinNodeSharedNodeCreationTool, ACTIVITY_NODE,
                List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of(this.umlPackage.getSequenceNode()));
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link LoopNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createLoopNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);
        rectangularNodeStyle.setBorderLineStyle(LineStyle.DASH);
        EClass loopNodeEClass = this.umlPackage.getLoopNode();
        NodeDescription adLoopNodeSharedNodeDescription = this.newNodeBuilder(loopNodeEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(loopNodeEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(loopNodeEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(loopNodeEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, true))
                .build();
        adLoopNodeSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adLoopNodeSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.adSharedDescription.getChildrenDescriptions().add(adLoopNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adLoopNodeSharedNodeDescription);
        this.addToolSections(adLoopNodeSharedNodeDescription, ACTIVITY_NODE, PIN, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adLoopNodeSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(loopNodeEClass);
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adLoopNodeSharedNodeDescription, diagramDescription, adLoopNodeSharedNodeCreationTool, STRUCTURED_ACTIVITY_NODE, owners, List.of());

        DropNodeTool adLoopNodeGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adLoopNodeSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adLoopNodeSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            adLoopNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adLoopNodeSharedNodeDescription.getPalette().setDropNodeTool(adLoopNodeGraphicalDropTool);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link MergeNode}
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     *
     * @see #createSharedCustomImageNodeDescription(NodeDescription, EClass, EReference, DiagramDescription)
     */
    private void createMergeNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createSharedCustomImageActivityNodeDescription(this.umlPackage.getMergeNode(), this.umlPackage.getActivity_OwnedNode(), diagramDescription);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link OpaqueAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createOpaqueActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adOpaqueActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getOpaqueAction());
        this.adSharedDescription.getChildrenDescriptions().add(adOpaqueActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adOpaqueActionSharedNodeDescription);
        this.addToolSections(adOpaqueActionSharedNodeDescription, PIN);

        NodeTool adOpaqueActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getOpaqueAction());
        this.reuseNodeAndCreateTool(adOpaqueActionSharedNodeDescription, diagramDescription, adOpaqueActionSharedNodeCreationTool, EXECUTABLE_NODE,
                List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of());

    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link OutputPin}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createOutputPinSharedNodeDescription(DiagramDescription diagramDescription) {
        ImageNodeStyleDescription outputPinStyle = this.getViewBuilder().createImageNodeStyle(this.getImageForDomainType(this.umlPackage.getOutputPin()));
        outputPinStyle.setPositionDependentRotation(true);
        NodeStyleDescription incomingOutgoingNodeStyleDescription = this.getViewBuilder().createImageNodeStyle(CONNECTED_PIN_IMAGE);
        ConditionalNodeStyle incomingOutgoingConditionalStyle = this.getViewBuilder().createConditionalNodeStyle(PIN_CONDITIONAL_STYLE_CONDITION, incomingOutgoingNodeStyleDescription);

        NodeDescription adOutputPinSharedNodeDescription = new NodeDescriptionBuilder(this.getIdBuilder(), this.getQueryBuilder(), this.umlPackage.getOutputPin(), outputPinStyle,
                this.getUmlMetaModelHelper())
                        .name(this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getOutputPin(), SHARED_SUFFIX)) //
                        .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED) //
                        .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_OUTPUT_PIN_CANDIDATES)) //
                        .conditionalStyles(List.of(incomingOutgoingConditionalStyle)) //
                        .addOutsideLabelDescription(this.getViewBuilder().createOutsideLabelDescription(this.getQueryBuilder().queryRenderLabel(), true))
                        .build();
        adOutputPinSharedNodeDescription.setDefaultWidthExpression(BORDER_NODE_SIZE);
        adOutputPinSharedNodeDescription.setDefaultHeightExpression(BORDER_NODE_SIZE);
        this.getViewBuilder().addDirectEditTool(adOutputPinSharedNodeDescription);
        this.getViewBuilder().addDefaultDeleteTool(adOutputPinSharedNodeDescription);
        this.adSharedDescription.getBorderNodesDescriptions().add(adOutputPinSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adOutputPinSharedNodeDescription);

        NodeTool adOutputPinSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getOutputPin().getName(),
                this.getIdBuilder().getCreationToolId(this.umlPackage.getOutputPin()),
                ActivityDiagramServices.CREATE_OUTPUT_PIN, List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        adOutputPinSharedNodeCreationTool
                .setPreconditionExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.CAN_CREATE_INTO_PARENT, this.getQueryBuilder().aqlString(this.umlPackage.getOutputPin().getName())));
        List<EClass> owners = List.of(this.umlPackage.getAcceptEventAction(), //
                this.umlPackage.getAddStructuralFeatureValueAction(), //
                this.umlPackage.getClearStructuralFeatureAction(), //
                this.umlPackage.getConditionalNode(), //
                this.umlPackage.getCreateLinkObjectAction(), //
                this.umlPackage.getExpansionRegion(), //
                this.umlPackage.getLoopNode(), //
                this.umlPackage.getOpaqueAction(), //
                this.umlPackage.getValueSpecificationAction(), //
                this.umlPackage.getReadExtentAction(), //
                this.umlPackage.getReadIsClassifiedObjectAction(), //
                this.umlPackage.getReadLinkAction(), //
                this.umlPackage.getReduceAction(), //
                this.umlPackage.getSequenceNode(), //
                this.umlPackage.getStartObjectBehaviorAction(), //
                this.umlPackage.getStructuredActivityNode(), //
                this.umlPackage.getUnmarshallAction(), //
                this.umlPackage.getAcceptCallAction(), //
                this.umlPackage.getCallBehaviorAction(), //
                this.umlPackage.getCallOperationAction(), //
                this.umlPackage.getCreateObjectAction(), //
                this.umlPackage.getReadStructuralFeatureAction(), //
                this.umlPackage.getReadVariableAction(), //
                this.umlPackage.getReadSelfAction(), //
                this.umlPackage.getTestIdentityAction(), //
                this.umlPackage.getValueSpecificationAction());
        this.reuseNodeAndCreateTool(adOutputPinSharedNodeDescription, diagramDescription, adOutputPinSharedNodeCreationTool, PIN, owners.toArray(EClass[]::new));
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReadExtentAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReadExtentActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adReadExtentActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getReadExtentAction());
        this.adSharedDescription.getChildrenDescriptions().add(adReadExtentActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adReadExtentActionSharedNodeDescription);
        this.addToolSections(adReadExtentActionSharedNodeDescription, PIN);

        NodeTool adReadExtentActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getReadExtentAction());
        this.reuseNodeAndCreateTool(adReadExtentActionSharedNodeDescription, diagramDescription, adReadExtentActionSharedNodeCreationTool, EXECUTABLE_NODE,
                List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReadIsClassifiedObjectAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReadIsClassifiedObjectActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adReadIsClassifiedObjectActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getReadIsClassifiedObjectAction());
        this.adSharedDescription.getChildrenDescriptions().add(adReadIsClassifiedObjectActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adReadIsClassifiedObjectActionSharedNodeDescription);
        this.addToolSections(adReadIsClassifiedObjectActionSharedNodeDescription, PIN);

        NodeTool adReadIsClassifiedObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getReadIsClassifiedObjectAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adReadIsClassifiedObjectActionSharedNodeDescription, diagramDescription, adReadIsClassifiedObjectActionSharedNodeCreationTool, CREATE_OBJECT_ACTION, owners,
                List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReadSelfAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReadSelfActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adReadSelfActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getReadSelfAction());
        this.adSharedDescription.getChildrenDescriptions().add(adReadSelfActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adReadSelfActionSharedNodeDescription);
        this.addToolSections(adReadSelfActionSharedNodeDescription, PIN);

        NodeTool adReadSelfActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getReadSelfAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adReadSelfActionSharedNodeDescription, diagramDescription, adReadSelfActionSharedNodeCreationTool, CREATE_OBJECT_ACTION, owners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReadStructuralFeatureAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReadStructuralFeatureActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adReadStructuralFeatureActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getReadStructuralFeatureAction());
        this.adSharedDescription.getChildrenDescriptions().add(adReadStructuralFeatureActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adReadStructuralFeatureActionSharedNodeDescription);
        this.addToolSections(adReadStructuralFeatureActionSharedNodeDescription, PIN);

        NodeTool adReadStructuralFeatureActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getReadStructuralFeatureAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adReadStructuralFeatureActionSharedNodeDescription, diagramDescription, adReadStructuralFeatureActionSharedNodeCreationTool, STRUCTURAL_FEATURE, owners,
                List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReclassifyObjectAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReclassifyObjectActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adReclassifyObjectActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getReclassifyObjectAction());
        this.adSharedDescription.getChildrenDescriptions().add(adReclassifyObjectActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adReclassifyObjectActionSharedNodeDescription);
        this.addToolSections(adReclassifyObjectActionSharedNodeDescription, PIN);

        NodeTool adReclassifyObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getReclassifyObjectAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adReclassifyObjectActionSharedNodeDescription, diagramDescription, adReclassifyObjectActionSharedNodeCreationTool, CREATE_OBJECT_ACTION, owners,
                List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReduceAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReduceActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adReduceActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getReduceAction());
        this.adSharedDescription.getChildrenDescriptions().add(adReduceActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adReduceActionSharedNodeDescription);
        this.addToolSections(adReduceActionSharedNodeDescription, PIN);

        NodeTool adReduceActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getReduceAction());
        this.reuseNodeAndCreateTool(adReduceActionSharedNodeDescription, diagramDescription, adReduceActionSharedNodeCreationTool, EXECUTABLE_NODE,
                List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of());
    }

    /**
     * Creates a rounded-rectangle {@link NodeDescription} representing the {@code domainType} UML action.
     *
     * @param domainType
     *            the type of the UML action to represent
     * @return the created {@link NodeDescription}
     */
    private NodeDescription createSharedRoundedRectangleActionNodeDescription(EClass domainType) {
        NodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);
        NodeDescription adRoundedRectangleSharedNodeDescription = this.newNodeBuilder(domainType, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(domainType, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(domainType.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(domainType.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, false))
                .build();
        adRoundedRectangleSharedNodeDescription.setDefaultHeightExpression(SIZE_50);
        adRoundedRectangleSharedNodeDescription.setDefaultWidthExpression("200");
        return adRoundedRectangleSharedNodeDescription;
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link SendObjectAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createSendObjectActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adSendObjectActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getSendObjectAction());
        this.adSharedDescription.getChildrenDescriptions().add(adSendObjectActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adSendObjectActionSharedNodeDescription);
        this.addToolSections(adSendObjectActionSharedNodeDescription, PIN);

        NodeTool adSendObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getSendObjectAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adSendObjectActionSharedNodeDescription, diagramDescription, adSendObjectActionSharedNodeCreationTool, INVOCATION_ACTION, owners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link SendSignalAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createSendSignalActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass sendSignalActionEClass = this.umlPackage.getSendSignalAction();
        NodeStyleDescription adSendSignalActionNodeStyleDescription = PapyrusCustomNodesFactory.eINSTANCE.createOuterFlagNodeStyleDescription();
        adSendSignalActionNodeStyleDescription.setBorderColor(this.styleProvider.getBorderNodeColor());
        adSendSignalActionNodeStyleDescription.setBorderRadius(this.styleProvider.getNodeBorderRadius());

        NodeDescription adSendSignalActionSharedNodeDescription = this.newNodeBuilder(sendSignalActionEClass, adSendSignalActionNodeStyleDescription) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(sendSignalActionEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(sendSignalActionEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(sendSignalActionEClass.getName()))
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, false))
                .build();
        adSendSignalActionSharedNodeDescription.setDefaultWidthExpression("170");
        adSendSignalActionSharedNodeDescription.setDefaultHeightExpression("70");
        this.adSharedDescription.getChildrenDescriptions().add(adSendSignalActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adSendSignalActionSharedNodeDescription);
        this.addToolSections(adSendSignalActionSharedNodeDescription, PIN);

        NodeTool adSendSignalActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getSendSignalAction());
        this.reuseNodeAndCreateTool(adSendSignalActionSharedNodeDescription, diagramDescription, adSendSignalActionSharedNodeCreationTool, INVOCATION_ACTION, this.umlPackage.getActivity(),
                this.umlPackage.getActivityGroup());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link SequenceNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createSequenceNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);
        rectangularNodeStyle.setBorderLineStyle(LineStyle.DASH);
        EClass sequenceNodeEClass = this.umlPackage.getSequenceNode();
        NodeDescription adSequenceNodeSharedNodeDescription = this.newNodeBuilder(sequenceNodeEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(sequenceNodeEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(sequenceNodeEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(sequenceNodeEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, true))
                .build();
        adSequenceNodeSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adSequenceNodeSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.adSharedDescription.getChildrenDescriptions().add(adSequenceNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adSequenceNodeSharedNodeDescription);
        this.addToolSections(adSequenceNodeSharedNodeDescription, PIN, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE,
                ACCEPT_EVENT_ACTION);
        NodeTool adSequenceNodeSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(sequenceNodeEClass);
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adSequenceNodeSharedNodeDescription, diagramDescription, adSequenceNodeSharedNodeCreationTool, STRUCTURED_ACTIVITY_NODE, owners, List.of());

        DropNodeTool adSequenceNodeGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adSequenceNodeSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adSequenceNodeSharedNodeDescription, () -> {
            List<EClass> forbiddenTypes = new ArrayList<>();
            forbiddenTypes.addAll(this.borderNodeTypes);
            forbiddenTypes.addAll(List.of(
                    this.umlPackage.getActivityFinalNode(),
                    this.umlPackage.getDecisionNode(),
                    this.umlPackage.getFlowFinalNode(),
                    this.umlPackage.getForkNode(),
                    this.umlPackage.getInitialNode(),
                    this.umlPackage.getInputPin(),
                    this.umlPackage.getJoinNode(),
                    this.umlPackage.getMergeNode()));
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, forbiddenTypes);
            adSequenceNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adSequenceNodeSharedNodeDescription.getPalette().setDropNodeTool(adSequenceNodeGraphicalDropTool);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link StartClassifierBehaviorAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     *
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createStartClassifierBehaviorActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adStartClassifierBehaviorActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getStartClassifierBehaviorAction());
        this.adSharedDescription.getChildrenDescriptions().add(adStartClassifierBehaviorActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adStartClassifierBehaviorActionSharedNodeDescription);
        this.addToolSections(adStartClassifierBehaviorActionSharedNodeDescription, PIN);

        NodeTool adStartClassifierBehaviorActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getStartClassifierBehaviorAction());
        this.reuseNodeAndCreateTool(adStartClassifierBehaviorActionSharedNodeDescription, diagramDescription, adStartClassifierBehaviorActionSharedNodeCreationTool, EXECUTABLE_NODE,
                List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link StartObjectBehaviorAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createStartObjectBehaviorActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adStartObjectBehaviorActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getStartObjectBehaviorAction());
        this.adSharedDescription.getChildrenDescriptions().add(adStartObjectBehaviorActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adStartObjectBehaviorActionSharedNodeDescription);
        this.addToolSections(adStartObjectBehaviorActionSharedNodeDescription, PIN);

        NodeTool adStartObjectBehaviorActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getStartObjectBehaviorAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adStartObjectBehaviorActionSharedNodeDescription, diagramDescription, adStartObjectBehaviorActionSharedNodeCreationTool, INVOCATION_ACTION, owners,
                List.of());
    }

    /**
     * Utility method easing the definition of {@link StructuredActivityNode} creation tools.
     * <p>
     * This method is a shortcut for {@link #createCreationTool(String, String, List)} with a preset service that can be
     * used for all the {@link StructuredActivityNode} subclasses. Use {@link #createCreationTool(String, String, List)}
     * to create a creation tool with a custom creation service.
     * </p>
     *
     * @return the created {@link NodeTool}
     * @see #createCreationTool(String, String, List)
     */
    private NodeTool createStructuredActivityNodeCreationTool(EClass newType) {
        return this.getViewBuilder().createCreationTool(newType.getName(), this.getIdBuilder().getCreationToolId(newType), ActivityDiagramServices.CREATE_ACTIVITY_NODE,
                List.of(this.getQueryBuilder().aqlString(newType.getName()), this.getQueryBuilder().aqlString(this.umlPackage.getActivity_StructuredNode().getName()), SELECTED_NODE, DIAGRAM_CONTEXT,
                        CONVERTED_NODES));
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link StructuredActivityNode}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createStructuredActivityNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);
        rectangularNodeStyle.setBorderLineStyle(LineStyle.DASH);
        EClass structuredActivityNodeEClass = this.umlPackage.getStructuredActivityNode();
        NodeDescription adStructuredActivityNodeSharedNodeDescription = this.newNodeBuilder(structuredActivityNodeEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(structuredActivityNodeEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(structuredActivityNodeEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(structuredActivityNodeEClass.getName())) //
                .build();
        adStructuredActivityNodeSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adStructuredActivityNodeSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.adSharedDescription.getChildrenDescriptions().add(adStructuredActivityNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adStructuredActivityNodeSharedNodeDescription);
        this.addToolSections(adStructuredActivityNodeSharedNodeDescription, ACTIVITY_NODE, PIN, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adStructuredActivityNodeSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(structuredActivityNodeEClass);
        List<EClass> owners = List.of(this.umlPackage.getActivity(), //
                this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adStructuredActivityNodeSharedNodeDescription, diagramDescription, adStructuredActivityNodeSharedNodeCreationTool, STRUCTURED_ACTIVITY_NODE, owners,
                List.of());

        DropNodeTool adStructuredActivityNodeGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adStructuredActivityNodeSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adStructuredActivityNodeSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            adStructuredActivityNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adStructuredActivityNodeSharedNodeDescription.getPalette().setDropNodeTool(adStructuredActivityNodeGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an {@link Activity} inside another {@link Activity}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createActivitySharedNodeDescription(DiagramDescription diagramDescription) {
        // We need a custom NodeDescription for sub-activity, otherwise there is no way to differentiate the root
        // Activity from Sub Activities, and the semantic candidate expression doesn't work (self): it produces an
        // infinite loop.
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);
        EClass activityEClass = this.umlPackage.getActivity();
        NodeDescription adActivitySharedNodeDescription = this.newNodeBuilder(activityEClass, rectangularNodeStyle) //
                // Explicitly set the name because IdBuilder can't add a prefix before the domain type name.
                .name("AD_SubActivity_SHARED") //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression("aql:self.oclAsType(uml::Activity).nestedClassifier")//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(activityEClass.getName())) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(activityEClass.getName())) //
                .build();
        adActivitySharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adActivitySharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);

        this.adSharedDescription.getChildrenDescriptions().add(adActivitySharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adActivitySharedNodeDescription);
        this.addToolSections(adActivitySharedNodeDescription, ACTIVITY_GROUP, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adActivitySharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getClass_NestedClassifier(), activityEClass);
        this.reuseNodeAndCreateTool(adActivitySharedNodeDescription, diagramDescription, adActivitySharedNodeCreationTool, ACTIVITY_GROUP, activityEClass);

        DropNodeTool adActivityGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adActivitySharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityNode(), this.umlPackage.getActivityPartition(), this.umlPackage.getComment(),
                this.umlPackage.getConstraint(), this.umlPackage.getInterruptibleActivityRegion());
        this.registerCallback(adActivitySharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            adActivityGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adActivitySharedNodeDescription.getPalette().setDropNodeTool(adActivityGraphicalDropTool);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link TestIdentityAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createTestIdentityActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adTestIdentityActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getTestIdentityAction());
        this.adSharedDescription.getChildrenDescriptions().add(adTestIdentityActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adTestIdentityActionSharedNodeDescription);
        this.addToolSections(adTestIdentityActionSharedNodeDescription, PIN);

        NodeTool adTestIdentityActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getTestIdentityAction());
        List<EClass> owners = List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup());
        this.reuseNodeAndCreateTool(adTestIdentityActionSharedNodeDescription, diagramDescription, adTestIdentityActionSharedNodeCreationTool, NODES, owners,
                List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ValuePin}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createValuePinSharedNodeDescription(DiagramDescription diagramDescription) {
        ImageNodeStyleDescription valuePinStyle = this.getViewBuilder().createImageNodeStyle(this.getImageForDomainType(this.umlPackage.getValuePin()));
        valuePinStyle.setPositionDependentRotation(true);
        NodeStyleDescription incomingOutgoingNodeStyleDescription = this.getViewBuilder().createImageNodeStyle(CONNECTED_PIN_IMAGE);
        ConditionalNodeStyle incomingOutgoingConditionalStyle = this.getViewBuilder().createConditionalNodeStyle(PIN_CONDITIONAL_STYLE_CONDITION, incomingOutgoingNodeStyleDescription);

        NodeDescription adValuePinSharedNodeDescription = new NodeDescriptionBuilder(this.getIdBuilder(), this.getQueryBuilder(), this.umlPackage.getValuePin(), valuePinStyle,
                this.getUmlMetaModelHelper())
                        .name(this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getValuePin(), SHARED_SUFFIX)) //
                        .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED) //
                        .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_VALUE_PIN_CANDIDATES)) //
                        .addOutsideLabelDescription(this.getViewBuilder().createOutsideLabelDescription(this.getQueryBuilder().queryRenderLabel(), true))
                        .conditionalStyles(List.of(incomingOutgoingConditionalStyle)) //
                        .build();
        adValuePinSharedNodeDescription.setDefaultWidthExpression(BORDER_NODE_SIZE);
        adValuePinSharedNodeDescription.setDefaultHeightExpression(BORDER_NODE_SIZE);
        this.getViewBuilder().addDirectEditTool(adValuePinSharedNodeDescription);
        this.getViewBuilder().addDefaultDeleteTool(adValuePinSharedNodeDescription);
        this.adSharedDescription.getBorderNodesDescriptions().add(adValuePinSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adValuePinSharedNodeDescription);

        NodeTool adValuePinSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getValuePin().getName(),
                this.getIdBuilder().getCreationToolId(this.umlPackage.getValuePin()),
                ActivityDiagramServices.CREATE_VALUE_PIN, List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));

        adValuePinSharedNodeCreationTool
                .setPreconditionExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.CAN_CREATE_INTO_PARENT, this.getQueryBuilder().aqlString(this.umlPackage.getValuePin().getName())));
        List<EClass> owners = List.of(this.umlPackage.getBroadcastSignalAction(), //
                this.umlPackage.getConditionalNode(), //
                this.umlPackage.getCreateLinkAction(), //
                this.umlPackage.getCreateLinkObjectAction(), //
                this.umlPackage.getDestroyLinkAction(), //
                this.umlPackage.getExpansionRegion(), //
                this.umlPackage.getLoopNode(), //
                this.umlPackage.getOpaqueAction(), //
                this.umlPackage.getReadLinkAction(), //
                this.umlPackage.getSequenceNode(), //
                this.umlPackage.getStartObjectBehaviorAction(), //
                this.umlPackage.getStructuredActivityNode(), //
                this.umlPackage.getAddStructuralFeatureValueAction(), //
                this.umlPackage.getAddVariableValueAction(), //
                this.umlPackage.getCallBehaviorAction(), //
                this.umlPackage.getCallOperationAction(), //
                this.umlPackage.getClearAssociationAction(), //
                this.umlPackage.getClearStructuralFeatureAction(), //
                this.umlPackage.getDestroyObjectAction(), //
                this.umlPackage.getReadIsClassifiedObjectAction(), //
                this.umlPackage.getReadStructuralFeatureAction(), //
                this.umlPackage.getReclassifyObjectAction(), //
                this.umlPackage.getReduceAction(), //
                this.umlPackage.getSendObjectAction(), //
                this.umlPackage.getSendSignalAction(), //
                this.umlPackage.getStartClassifierBehaviorAction(), //
                this.umlPackage.getTestIdentityAction(), //
                this.umlPackage.getUnmarshallAction()//
        );
        this.reuseNodeAndCreateTool(adValuePinSharedNodeDescription, diagramDescription, adValuePinSharedNodeCreationTool, PIN, owners.toArray(EClass[]::new));
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ValueSpecification}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createValueSpecificationActionSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription adValueSpecificationActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getValueSpecificationAction());
        this.adSharedDescription.getChildrenDescriptions().add(adValueSpecificationActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adValueSpecificationActionSharedNodeDescription);
        this.addToolSections(adValueSpecificationActionSharedNodeDescription, PIN);

        NodeTool adValueSpecificationActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getValueSpecificationAction());
        this.reuseNodeAndCreateTool(adValueSpecificationActionSharedNodeDescription, diagramDescription, adValueSpecificationActionSharedNodeCreationTool, EXECUTABLE_NODE,
                List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()), List.of());
    }

    /**
     * Creates an {@link EdgeDescription} representing an UML {@link ControlFlow}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createControlFlowEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceDescriptionSupplier = () -> this.collectNodesWithDomainAndFilter(diagramDescription, List.of(this.umlPackage.getActivityNode()),
                List.of(this.umlPackage.getFinalNode()));
        Supplier<List<NodeDescription>> targetDescriptionSupplier = () -> this.collectNodesWithDomainAndFilter(diagramDescription, List.of(this.umlPackage.getActivityNode()),
                List.of(this.umlPackage.getInitialNode()));

        EdgeDescription adControlFlowEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getControlFlow(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getControlFlow()), sourceDescriptionSupplier, targetDescriptionSupplier);
        adControlFlowEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        adControlFlowEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        EdgeTool adControlFlowEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(adControlFlowEdgeDescription, this.umlPackage.getActivity_Edge());
        this.registerCallback(adControlFlowEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceDescriptionSupplier.get(), adControlFlowEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(adControlFlowEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(adControlFlowEdgeDescription);
    }

    /**
     * Creates an {@link EdgeDescription} representing an UMLL {@link ObjectFlow}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createObjectFlowEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceDescriptionSupplier = () -> this.collectNodesWithDomainAndFilter(diagramDescription, List.of(
                this.umlPackage.getActionInputPin(),
                this.umlPackage.getOutputPin(),
                this.umlPackage.getValuePin(),
                this.umlPackage.getExpansionNode(),
                this.umlPackage.getDecisionNode(),
                this.umlPackage.getForkNode(),
                this.umlPackage.getJoinNode(),
                this.umlPackage.getMergeNode(),
                this.umlPackage.getOpaqueAction(),
                this.umlPackage.getActivityParameterNode()),
                List.of());
        Supplier<List<NodeDescription>> targetDescriptionSupplier = () -> this.collectNodesWithDomainAndFilter(diagramDescription, List.of(
                this.umlPackage.getActionInputPin(),
                this.umlPackage.getInputPin(),
                this.umlPackage.getOutputPin(),
                this.umlPackage.getValuePin(),
                this.umlPackage.getActivityFinalNode(),
                this.umlPackage.getDecisionNode(),
                this.umlPackage.getFlowFinalNode(),
                this.umlPackage.getForkNode(),
                this.umlPackage.getJoinNode(),
                this.umlPackage.getMergeNode(),
                this.umlPackage.getOpaqueAction(),
                this.umlPackage.getExpansionNode(),
                this.umlPackage.getActivityParameterNode()),
                List.of());

        EdgeDescription adObjectFlowEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getObjectFlow(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getObjectFlow()), sourceDescriptionSupplier, targetDescriptionSupplier);
        adObjectFlowEdgeDescription.setEndLabelExpression(this.getQueryBuilder().createDomainBaseEdgeTargetLabelExpression());
        adObjectFlowEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        adObjectFlowEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        EdgeTool adObjectFlowEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(adObjectFlowEdgeDescription, this.umlPackage.getActivity_Edge());
        this.registerCallback(adObjectFlowEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceDescriptionSupplier.get(), adObjectFlowEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(adObjectFlowEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(adObjectFlowEdgeDescription);
    }

    /**
     * Utility method that computes the {@link UUID} of the image to use to represent the provided {@code domainType}.
     *
     * @param domainType
     *            the type to retrieve the image from
     * @return a {@link String} representing the image
     */
    private String getImageForDomainType(EClass domainType) {
        if (domainType == null) {
            LOGGER.warn("Cannot find the image for domain type null");
            return null;
        }
        String imageName = switch (domainType.getClassifierID()) {

            case UMLPackage.ACTION_INPUT_PIN, UMLPackage.INPUT_PIN, UMLPackage.VALUE_PIN -> "InputPin.svg";
            case UMLPackage.ACTIVITY_FINAL_NODE -> "ActivityFinalNode.svg";
            case UMLPackage.DECISION_NODE -> "DecisionNode.svg";
            case UMLPackage.EXPANSION_NODE -> "ExpansionNode.svg";
            case UMLPackage.FLOW_FINAL_NODE -> "FlowFinalNode.svg";
            // Use Fork.svg instead of ForkNode.svg because it has been defined in SMD.
            case UMLPackage.FORK_NODE -> "Fork.svg";
            case UMLPackage.INITIAL_NODE -> "InitialNode.svg";
            // Use Join.svg instead of ForkNode.svg because it has been defined in SMD.
            case UMLPackage.JOIN_NODE -> "Join.svg";
            case UMLPackage.MERGE_NODE -> "MergeNode.svg";
            case UMLPackage.OUTPUT_PIN -> "OutputPin.svg";
            default -> null;
        };
        String result = null;
        if (imageName != null) {
            result = "view/images/" + imageName;
        } else {
            LOGGER.warn("Cannot find the image for domain type {0}", domainType.getName());
        }
        return result;
    }

    /**
     * Collects all the {@link NodeDescription} matching the given {@code domains}, excluding the ones matching the
     * provided {@code forbiddenDomains}.
     * <p>
     * This method is typically used to collect a given domain class and exclude some of its sub-classes. It also
     * excludes {@code AD_DecisionNode_Note_SHARED}, which shouldn't be searchable with this method.
     * </p>
     *
     * @param description
     *            the diagram description
     * @param domains
     *            the list of matching domain types
     * @param forbiddenDomains
     *            the list of domain types to exclude
     * @return a list of matching {@link NodeDescription}
     */
    @Override
    protected List<NodeDescription> collectNodesWithDomainAndFilter(DiagramDescription description, List<EClass> domains, List<EClass> forbiddenDomains) {
        List<NodeDescription> forbiddenDescription = this.collectNodesWithDomain(description, forbiddenDomains.toArray(EClass[]::new));
        return this.collectNodesWithDomain(description, domains.toArray(EClass[]::new)).stream() //
                .filter(nd -> !SHARED_DESCRIPTIONS.equals(nd.getName())) //
                .filter(nd -> !Objects.equals(nd.getName(), "AD_DecisionNode_Note_SHARED")) //
                .filter(nd -> !forbiddenDescription.contains(nd)) //
                .toList();
    }
}

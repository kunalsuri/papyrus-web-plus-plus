/*****************************************************************************
 * Copyright (c) 2024, 2026 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issues 219, 227
 *  Aurelien Didier (Artal Technologies) - Issue 229
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
import org.eclipse.sirius.components.view.diagram.DiagramLayoutOption;
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
import org.springframework.stereotype.Service;

/**
 * Builder of the "Activity Diagram" diagram representation.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
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
     * 70px size Node.
     */
    private static final String SIZE_70 = "70";

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

    private NodeDescription symbolNodeDescription;

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

    private List<EClass> activityOwners = List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup());

    /**
     * Initializes the builder.
     */
    public ADDiagramDescriptionBuilder() {
        super(AD_PREFIX, AD_REP_NAME, UMLPackage.eINSTANCE.getNamedElement());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {
        diagramDescription.setPreconditionExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.CAN_CREATE_DIAGRAM));

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
        this.adSharedDescription = this.createSharedDescription(diagramDescription);
        this.symbolNodeDescription = this.createSymbolSharedNodeDescription(diagramDescription, symbolOwners, forbiddenOwners, SYMBOLS_COMPARTMENT_SUFFIX);

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

        this.adSharedDescription.getChildrenDescriptions().add(this.symbolNodeDescription);
        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));

        diagramDescription.setToolbar(this.getViewBuilder().createDefaultDiagramToolbar());

        diagramDescription.setLayoutOption(DiagramLayoutOption.NONE);

        diagramDescription.setMinimapVisible(true);

        diagramDescription.setStyle(this.getDiagramBuilders().newDiagramStyleDescription().build());
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
        this.createCommentSubNodeDescription(diagramDescription, this.adSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getComment(), SHARED_SUFFIX),
                commentOwners);
        this.createConstraintSubNodeDescription(diagramDescription, this.adSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getConstraint(), SHARED_SUFFIX),
                constraintOwners);
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
        this.createTestIdentityActionSharedNodeDescription(diagramDescription);
        this.createValuePinSharedNodeDescription(diagramDescription);
        this.createValueSpecificationActionSharedNodeDescription(diagramDescription);
        this.createActivitySharedNodeDescription(diagramDescription);
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
    private void createActivityTopNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(BORDER_RADIUS_SIZE);

        EClass activityEClass = this.umlPackage.getActivity();
        NodeDescription adActivityHolderTopNodeDescription = this.newNodeBuilder(activityEClass, rectangularNodeStyle) //
                .semanticCandidateExpression(this.getQueryBuilder().querySelf()) //
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(activityEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();

        adActivityHolderTopNodeDescription.setDefaultWidthExpression(ROOT_ELEMENT_WIDTH);
        adActivityHolderTopNodeDescription.setDefaultHeightExpression(ROOT_ELEMENT_HEIGHT);

        NodeDescription adActivityContentTopNodeDescription = this.createContentNodeDescription(activityEClass, false);
        this.addContent(activityEClass, false, adActivityHolderTopNodeDescription, adActivityContentTopNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adActivityHolderTopNodeDescription, adActivityContentTopNodeDescription);
        diagramDescription.getNodeDescriptions().add(adActivityHolderTopNodeDescription);

        this.addToolSections(adActivityContentTopNodeDescription, ACTIVITY_GROUP, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE,
                STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        DropNodeTool adActivityGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adActivityContentTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityNode(), this.umlPackage.getActivityPartition(), this.umlPackage.getComment(),
                this.umlPackage.getConstraint(), this.umlPackage.getInterruptibleActivityRegion());
        this.registerCallback(adActivityContentTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, this.borderNodeTypes);
            adActivityGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adActivityContentTopNodeDescription.getPalette().setDropNodeTool(adActivityGraphicalDropTool);
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
        this.reuseNodeAndCreateTool(adAcceptCallActionSharedNodeDescription, diagramDescription, adAcceptCallActionSharedNodeCreationTool, ACCEPT_EVENT_ACTION, this.activityOwners, List.of());
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
        this.reuseNodeAndCreateTool(adAcceptEventActionSharedNodeDescription, diagramDescription, adAcceptEventActionSharedNodeCreationTool, ACCEPT_EVENT_ACTION, this.activityOwners, List.of());
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
        this.reuseNodeAndCreateTool(adActionInputPinSharedNodeDescription, diagramDescription, adActionInputPinSharedNodeCreationTool, PIN, owners, List.of());
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

        NodeTool adActivityParameterNodeSharedNodeCreationTool = this.getViewBuilder().createCreationToolInHolder(this.umlPackage.getActivity_OwnedNode(), this.umlPackage.getActivityParameterNode());
        this.reuseNodeAndCreateTool(adActivityParameterNodeSharedNodeDescription, diagramDescription, adActivityParameterNodeSharedNodeCreationTool, NODES,
                List.of(this.umlPackage.getActivity()).toArray(EClass[]::new));
    }

    private InsideLabelStyle createDefaultLabelStyle(boolean withIcon, boolean withHeader, boolean withHeaderSeparator) {
        InsideLabelStyle labelStyle = this.getViewBuilder().createDefaultInsideLabelStyle(withIcon, withHeader);
        labelStyle.setWithHeader(true);
        if (withHeaderSeparator) {
            labelStyle.setHeaderSeparatorDisplayMode(HeaderSeparatorDisplayMode.IF_CHILDREN);
        } else {
            labelStyle.setHeaderSeparatorDisplayMode(HeaderSeparatorDisplayMode.NEVER);
        }
        return labelStyle;
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ActivityPartition}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     */
    private void createActivityPartitionSharedNodeDescription(DiagramDescription diagramDescription) {

        EClass activityPartitionEClass = this.umlPackage.getActivityPartition();
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();

        NodeDescription adActivityPartitionHolderSharedNodeDescription = this.newNodeBuilder(activityPartitionEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(activityPartitionEClass, SHARED_SUFFIX + UNDERSCORE + HOLDER_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_PARTITION_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(activityPartitionEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(activityPartitionEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.createDefaultLabelStyle(true, true, true))
                .build();

        NodeDescription adActivityPartitionContentSharedNodeDescription = this.createContentNodeDescription(activityPartitionEClass, true);
        adActivityPartitionHolderSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adActivityPartitionHolderSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.addContent(activityPartitionEClass, true, adActivityPartitionHolderSharedNodeDescription, adActivityPartitionContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adActivityPartitionHolderSharedNodeDescription, adActivityPartitionContentSharedNodeDescription);

        this.adSharedDescription.getChildrenDescriptions().add(adActivityPartitionHolderSharedNodeDescription);
        this.addToolSections(adActivityPartitionContentSharedNodeDescription, ACTIVITY_GROUP, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE,
                STRUCTURAL_FEATURE, EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adActivityPartitionSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getActivityPartition_Subpartition(), activityPartitionEClass);
        this.reuseNodeAndCreateTool(adActivityPartitionHolderSharedNodeDescription, diagramDescription, adActivityPartitionSharedNodeCreationTool, ACTIVITY_GROUP, List.of(activityPartitionEClass),
                List.of());
        NodeTool activityPartitionNodeTool = this.getViewBuilder().createCreationTool(this.umlPackage.getActivity_Partition(), activityPartitionEClass);
        this.reuseNodeAndCreateTool(adActivityPartitionHolderSharedNodeDescription, diagramDescription, activityPartitionNodeTool, ACTIVITY_GROUP, List.of(this.umlPackage.getActivity()), List.of());

        DropNodeTool adActivityPartitionGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adActivityPartitionContentSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getActivityPartition(), this.umlPackage.getComment());
        this.registerCallback(adActivityPartitionContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, this.borderNodeTypes);
            adActivityPartitionGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adActivityPartitionContentSharedNodeDescription.getPalette().setDropNodeTool(adActivityPartitionGraphicalDropTool);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link AddStructuralFeatureValueAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createAddStructuralFeatureValueActionSharedNodeDescription(DiagramDescription diagramDescription) {

        EClass addStructuralFeatureValueAction = this.umlPackage.getAddStructuralFeatureValueAction();
        NodeDescription adAddStructuralFeatureValueActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(addStructuralFeatureValueAction);
        this.allowSymbol(adAddStructuralFeatureValueActionSharedNodeDescription);
        this.adSharedDescription.getChildrenDescriptions().add(adAddStructuralFeatureValueActionSharedNodeDescription);
        this.addToolSections(adAddStructuralFeatureValueActionSharedNodeDescription, PIN);

        NodeTool adAddStructuralFeatureValueActionSharedNodeCreationTool = this.createActivityNodeCreationTool(addStructuralFeatureValueAction);
        this.reuseNodeAndCreateTool(adAddStructuralFeatureValueActionSharedNodeDescription, diagramDescription, adAddStructuralFeatureValueActionSharedNodeCreationTool, STRUCTURAL_FEATURE,
                this.activityOwners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link BroadCastSignalAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createBroadcastSignalActionSharedNodeDescription(DiagramDescription diagramDescription) {

        EClass broadcastSignalAction = this.umlPackage.getBroadcastSignalAction();

        NodeDescription adBroadcastSignalActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(broadcastSignalAction);
        this.allowSymbol(adBroadcastSignalActionSharedNodeDescription);

        this.adSharedDescription.getChildrenDescriptions().add(adBroadcastSignalActionSharedNodeDescription);
        this.addToolSections(adBroadcastSignalActionSharedNodeDescription, PIN);

        NodeTool adBroadcastSignalActionSharedNodeCreationTool = this.createActivityNodeCreationTool(broadcastSignalAction);
        this.reuseNodeAndCreateTool(adBroadcastSignalActionSharedNodeDescription, diagramDescription, adBroadcastSignalActionSharedNodeCreationTool, INVOCATION_ACTION, this.activityOwners,
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

        EClass callBehaviorAction = this.umlPackage.getCallBehaviorAction();

        NodeDescription adCallBehaviorActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(this.umlPackage.getCallBehaviorAction());
        this.allowSymbol(adCallBehaviorActionSharedNodeDescription);
        this.adSharedDescription.getChildrenDescriptions().add(adCallBehaviorActionSharedNodeDescription);
        this.addToolSections(adCallBehaviorActionSharedNodeDescription, PIN);
        this.addToolSections(adCallBehaviorActionSharedNodeDescription, EDGES);

        NodeTool adCallBehaviorActionSharedNodeCreationTool = this.createActivityNodeCreationTool(callBehaviorAction);
        this.reuseNodeAndCreateTool(adCallBehaviorActionSharedNodeDescription, diagramDescription, adCallBehaviorActionSharedNodeCreationTool, INVOCATION_ACTION, this.activityOwners,
                List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link CallOperationAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createCallOperationActionSharedNodeDescription(DiagramDescription diagramDescription) {

        EClass callOperationAction = this.umlPackage.getCallOperationAction();

        NodeDescription adCallOperationActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(callOperationAction);
        this.allowSymbol(adCallOperationActionSharedNodeDescription);
        this.adSharedDescription.getChildrenDescriptions().add(adCallOperationActionSharedNodeDescription);
        this.addToolSections(adCallOperationActionSharedNodeDescription, PIN);

        NodeTool adCallOperationActionSharedNodeCreationTool = this.createActivityNodeCreationTool(callOperationAction);
        this.reuseNodeAndCreateTool(adCallOperationActionSharedNodeDescription, diagramDescription, adCallOperationActionSharedNodeCreationTool, INVOCATION_ACTION,
                this.activityOwners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ClearAssociationAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createClearAssociationActionSharedNodeDescription(DiagramDescription diagramDescription) {

        EClass clearAssociationAction = this.umlPackage.getClearAssociationAction();
        NodeDescription adClearAssociationActionHolderSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(clearAssociationAction);
        NodeDescription adClearAssociationActionContentSharedNodeDescription = this.createContentNodeDescription(clearAssociationAction, true);
        this.addContent(clearAssociationAction, true, adClearAssociationActionHolderSharedNodeDescription, adClearAssociationActionContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adClearAssociationActionHolderSharedNodeDescription, adClearAssociationActionContentSharedNodeDescription);
        this.adSharedDescription.getChildrenDescriptions().add(adClearAssociationActionHolderSharedNodeDescription);

        this.addToolSections(adClearAssociationActionContentSharedNodeDescription, PIN);

        NodeTool adClearAssociationActionSharedNodeCreationTool = this.createActivityNodeCreationTool(clearAssociationAction);
        this.reuseNodeAndCreateTool(adClearAssociationActionHolderSharedNodeDescription, diagramDescription, adClearAssociationActionSharedNodeCreationTool, EXECUTABLE_NODE,
                this.activityOwners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ClearStructuralFeatureAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createClearStructuralFeatureActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass clearStructuralFeatureAction = this.umlPackage.getClearStructuralFeatureAction();
        NodeDescription adClearStructuralFeatureActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(clearStructuralFeatureAction);

        this.adSharedDescription.getChildrenDescriptions().add(adClearStructuralFeatureActionSharedNodeDescription);
        this.allowSymbol(adClearStructuralFeatureActionSharedNodeDescription);
        this.addToolSections(adClearStructuralFeatureActionSharedNodeDescription, PIN);

        NodeTool adClearStructuralFeatureActionSharedNodeCreationTool = this.createActivityNodeCreationTool(clearStructuralFeatureAction);
        this.reuseNodeAndCreateTool(adClearStructuralFeatureActionSharedNodeDescription, diagramDescription, adClearStructuralFeatureActionSharedNodeCreationTool, STRUCTURAL_FEATURE,
                this.activityOwners, List.of());
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
        NodeDescription adConditionalNodeHolderSharedNodeDescription = this.newNodeBuilder(conditionalNodeEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(conditionalNodeEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(conditionalNodeEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(conditionalNodeEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, true))
                .build();

        adConditionalNodeHolderSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adConditionalNodeHolderSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);

        NodeDescription adConditionalNodeContentSharedNodeDescription = this.createContentNodeDescription(conditionalNodeEClass, true);
        this.addContent(conditionalNodeEClass, true, adConditionalNodeHolderSharedNodeDescription, adConditionalNodeContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adConditionalNodeHolderSharedNodeDescription, adConditionalNodeContentSharedNodeDescription);
        this.adSharedDescription.getChildrenDescriptions().add(adConditionalNodeHolderSharedNodeDescription);

        this.addToolSections(adConditionalNodeContentSharedNodeDescription, PIN, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adConditionalNodeSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(conditionalNodeEClass);
        this.reuseNodeAndCreateTool(adConditionalNodeHolderSharedNodeDescription, diagramDescription, adConditionalNodeSharedNodeCreationTool, STRUCTURED_ACTIVITY_NODE, this.activityOwners,
                List.of());

        DropNodeTool adConditionalNodeGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adConditionalNodeContentSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adConditionalNodeContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, this.borderNodeTypes);
            adConditionalNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adConditionalNodeContentSharedNodeDescription.getPalette().setDropNodeTool(adConditionalNodeGraphicalDropTool);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link CreateObjectAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createCreateObjectActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass createObjectAction = this.umlPackage.getCreateObjectAction();
        NodeDescription adCreateObjectActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(createObjectAction);
        this.adSharedDescription.getChildrenDescriptions().add(adCreateObjectActionSharedNodeDescription);
        this.allowSymbol(adCreateObjectActionSharedNodeDescription);
        this.addToolSections(adCreateObjectActionSharedNodeDescription, PIN);

        NodeTool adCreateObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(createObjectAction);
        this.reuseNodeAndCreateTool(adCreateObjectActionSharedNodeDescription, diagramDescription, adCreateObjectActionSharedNodeCreationTool, CREATE_OBJECT_ACTION, this.activityOwners,
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
                this.activityOwners, List.of(this.umlPackage.getSequenceNode()));
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

        this.reuseNode(adDecisionNodeNoteSharedNodeDescription, diagramDescription, List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityGroup()),
                List.of(this.umlPackage.getSequenceNode()));

        Predicate<NodeDescription> isDecisionNodeNoteDescription = nodeDescription -> Objects.equals(nodeDescription.getName(), adDecisionNodeNoteSharedNodeDescription.getName());

        EdgeDescription adDecisionNodeNoteEdgeDescription = this.getViewBuilder().createFeatureEdgeDescription(//
                this.getIdBuilder().getFeatureBaseEdgeId(this.umlPackage.getDecisionNode_DecisionInput()), //
                this.getQueryBuilder().emptyString(), //
                this.getQueryBuilder().querySelf(),
                () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getDecisionNode()).stream().filter(isDecisionNodeNoteDescription.negate()).toList(), //
                () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getDecisionNode()).stream().filter(isDecisionNodeNoteDescription).toList());

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
        EClass destroyObjectAction = this.umlPackage.getDestroyObjectAction();
        NodeDescription adDestroyObjectActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(destroyObjectAction);
        this.adSharedDescription.getChildrenDescriptions().add(adDestroyObjectActionSharedNodeDescription);

        this.allowSymbol(adDestroyObjectActionSharedNodeDescription);

        this.addToolSections(adDestroyObjectActionSharedNodeDescription, PIN);

        NodeTool adDestroyObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(destroyObjectAction);
        this.reuseNodeAndCreateTool(adDestroyObjectActionSharedNodeDescription, diagramDescription, adDestroyObjectActionSharedNodeCreationTool, CREATE_OBJECT_ACTION, this.activityOwners,
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
        NodeDescription adExpansionRegionHolderSharedNodeDescription = this.newNodeBuilder(expansionRegionEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(expansionRegionEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(expansionRegionEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.createDefaultLabelStyle(false, false, true))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(expansionRegionEClass.getName())) //
                .build();
        adExpansionRegionHolderSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adExpansionRegionHolderSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);

        NodeDescription adExpansionRegionContentSharedNodeDescription = this.createContentNodeDescription(expansionRegionEClass, true);
        this.addContent(expansionRegionEClass, true, adExpansionRegionHolderSharedNodeDescription, adExpansionRegionContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adExpansionRegionHolderSharedNodeDescription, adExpansionRegionContentSharedNodeDescription);

        this.adSharedDescription.getChildrenDescriptions().add(adExpansionRegionHolderSharedNodeDescription);

        this.addToolSections(adExpansionRegionContentSharedNodeDescription, ACTIVITY_NODE, PIN, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        this.createInputOutputExpansionNodeNodeDescription(adExpansionRegionHolderSharedNodeDescription, diagramDescription);

        NodeTool adExpansionRegionSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(expansionRegionEClass);
        this.reuseNodeAndCreateTool(adExpansionRegionHolderSharedNodeDescription, diagramDescription, adExpansionRegionSharedNodeCreationTool, EXPANSION_REGION, this.activityOwners, List.of());

        DropNodeTool adExpansionRegionGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adExpansionRegionContentSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adExpansionRegionContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, this.borderNodeTypes);
            adExpansionRegionGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adExpansionRegionContentSharedNodeDescription.getPalette().setDropNodeTool(adExpansionRegionGraphicalDropTool);
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
                this.activityOwners, List.of(this.umlPackage.getSequenceNode()));
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
        this.adSharedDescription.getBorderNodesDescriptions().add(adInputExpansionNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adInputExpansionNodeSharedNodeDescription);

        NodeTool adInputExpansionNodeSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getExpansionNode().getName(),
                "New Input Expansion Node",
                ActivityDiagramServices.CREATE_EXPANSION_NODE, List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES, String.valueOf(true)));
        this.reuseNodeAndCreateTool(adInputExpansionNodeSharedNodeDescription, diagramDescription, adInputExpansionNodeSharedNodeCreationTool, EXPANSION_REGION,
                List.of(this.umlPackage.getExpansionRegion()), List.of());
        NodeTool outputExpansionNodeTool = this.getViewBuilder().createCreationTool(this.umlPackage.getExpansionNode().getName(), "New Output Expansion Node",
                ActivityDiagramServices.CREATE_EXPANSION_NODE,
                List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES, String.valueOf(false)));
        this.reuseNodeAndCreateTool(adInputExpansionNodeSharedNodeDescription, diagramDescription, outputExpansionNodeTool, EXPANSION_REGION, List.of(this.umlPackage.getExpansionRegion()), List.of());
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
        this.reuseNodeAndCreateTool(adInputPinSharedNodeDescription, diagramDescription, adInputPinSharedNodeCreationTool, PIN, owners, List.of());
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
        NodeDescription adInterruptibleActivityRegionHolderSharedNodeDescription = this.newNodeBuilder(interruptibleActivityRegionEClass, rectangularNodeStyle) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_INTERRUPTIBLE_ACTIVITY_REGION_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(interruptibleActivityRegionEClass.getName())) //
                .insideLabelDescription("", this.createDefaultLabelStyle(false, false, false))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(interruptibleActivityRegionEClass.getName())) //
                .build();
        adInterruptibleActivityRegionHolderSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adInterruptibleActivityRegionHolderSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.adSharedDescription.getChildrenDescriptions().add(adInterruptibleActivityRegionHolderSharedNodeDescription);

        NodeDescription adInterruptibleActivityRegionContentSharedNodeDescription = this.createContentNodeDescription(interruptibleActivityRegionEClass, true);
        this.addContent(interruptibleActivityRegionEClass, true, adInterruptibleActivityRegionHolderSharedNodeDescription, adInterruptibleActivityRegionContentSharedNodeDescription,
                this.symbolNodeDescription);
        this.copyDimension(adInterruptibleActivityRegionHolderSharedNodeDescription, adInterruptibleActivityRegionContentSharedNodeDescription);
        this.addToolSections(adInterruptibleActivityRegionContentSharedNodeDescription, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE,
                STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adInterruptibleActivityRegionSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getActivity_OwnedGroup(), interruptibleActivityRegionEClass);
        this.reuseNodeAndCreateTool(adInterruptibleActivityRegionHolderSharedNodeDescription, diagramDescription, adInterruptibleActivityRegionSharedNodeCreationTool, ACTIVITY_GROUP,
                List.of(this.umlPackage.getActivity()), List.of());

        DropNodeTool adInterruptibleActivityRegionGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adInterruptibleActivityRegionContentSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment());
        this.registerCallback(adInterruptibleActivityRegionContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, this.borderNodeTypes);
            adInterruptibleActivityRegionGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adInterruptibleActivityRegionContentSharedNodeDescription.getPalette().setDropNodeTool(adInterruptibleActivityRegionGraphicalDropTool);
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
                this.activityOwners, List.of(this.umlPackage.getSequenceNode()));
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
        NodeDescription adLoopNodeSharedHolderNodeDescription = this.newNodeBuilder(loopNodeEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(loopNodeEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(loopNodeEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(loopNodeEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, true))
                .build();

        adLoopNodeSharedHolderNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adLoopNodeSharedHolderNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);

        NodeDescription adLoopNodeSharedContentNodeDescription = this.createContentNodeDescription(loopNodeEClass, true);
        this.addContent(loopNodeEClass, true, adLoopNodeSharedHolderNodeDescription, adLoopNodeSharedContentNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adLoopNodeSharedHolderNodeDescription, adLoopNodeSharedContentNodeDescription);
        this.adSharedDescription.getChildrenDescriptions().add(adLoopNodeSharedHolderNodeDescription);

        this.addToolSections(adLoopNodeSharedContentNodeDescription, ACTIVITY_NODE, PIN, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adLoopNodeSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(loopNodeEClass);
        this.reuseNodeAndCreateTool(adLoopNodeSharedHolderNodeDescription, diagramDescription, adLoopNodeSharedNodeCreationTool, STRUCTURED_ACTIVITY_NODE, this.activityOwners, List.of());

        DropNodeTool adLoopNodeGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adLoopNodeSharedContentNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adLoopNodeSharedContentNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, this.borderNodeTypes);
            adLoopNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adLoopNodeSharedContentNodeDescription.getPalette().setDropNodeTool(adLoopNodeGraphicalDropTool);
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
        EClass opaqueAction = this.umlPackage.getOpaqueAction();
        NodeDescription adOpaqueActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(opaqueAction);
        this.adSharedDescription.getChildrenDescriptions().add(adOpaqueActionSharedNodeDescription);

        this.allowSymbol(adOpaqueActionSharedNodeDescription);
        this.createDefaultToolSectionsInNodeDescription(adOpaqueActionSharedNodeDescription);
        this.addToolSections(adOpaqueActionSharedNodeDescription, PIN);

        NodeTool adOpaqueActionSharedNodeCreationTool = this.createActivityNodeCreationTool(opaqueAction);
        this.reuseNodeAndCreateTool(adOpaqueActionSharedNodeDescription, diagramDescription, adOpaqueActionSharedNodeCreationTool, EXECUTABLE_NODE,
                this.activityOwners, List.of());
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
        this.reuseNodeAndCreateTool(adOutputPinSharedNodeDescription, diagramDescription, adOutputPinSharedNodeCreationTool, PIN, owners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReadExtentAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReadExtentActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass readExtentAction = this.umlPackage.getReadExtentAction();
        NodeDescription adReadExtentActionHolderSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(readExtentAction);
        this.adSharedDescription.getChildrenDescriptions().add(adReadExtentActionHolderSharedNodeDescription);

        NodeDescription adReadExtentActionContentSharedNodeDescription = this.createContentNodeDescription(readExtentAction, true);
        this.addContent(readExtentAction, true, adReadExtentActionHolderSharedNodeDescription, adReadExtentActionContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adReadExtentActionHolderSharedNodeDescription, adReadExtentActionContentSharedNodeDescription);
        this.addToolSections(adReadExtentActionContentSharedNodeDescription, PIN);

        NodeTool adReadExtentActionSharedNodeCreationTool = this.createActivityNodeCreationTool(readExtentAction);
        this.reuseNodeAndCreateTool(adReadExtentActionHolderSharedNodeDescription, diagramDescription, adReadExtentActionSharedNodeCreationTool, EXECUTABLE_NODE,
                this.activityOwners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReadIsClassifiedObjectAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReadIsClassifiedObjectActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass readIsClassifiedObjectAction = this.umlPackage.getReadIsClassifiedObjectAction();
        NodeDescription adReadIsClassifiedObjectActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(readIsClassifiedObjectAction);
        this.adSharedDescription.getChildrenDescriptions().add(adReadIsClassifiedObjectActionSharedNodeDescription);
        this.allowSymbol(adReadIsClassifiedObjectActionSharedNodeDescription);

        this.addToolSections(adReadIsClassifiedObjectActionSharedNodeDescription, PIN);

        NodeTool adReadIsClassifiedObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(readIsClassifiedObjectAction);
        this.reuseNodeAndCreateTool(adReadIsClassifiedObjectActionSharedNodeDescription, diagramDescription, adReadIsClassifiedObjectActionSharedNodeCreationTool, CREATE_OBJECT_ACTION,
                this.activityOwners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReadSelfAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReadSelfActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass readSelfAction = this.umlPackage.getReadSelfAction();
        NodeDescription adReadSelfActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(readSelfAction);
        this.adSharedDescription.getChildrenDescriptions().add(adReadSelfActionSharedNodeDescription);
        this.allowSymbol(adReadSelfActionSharedNodeDescription);
        this.addToolSections(adReadSelfActionSharedNodeDescription, PIN);

        NodeTool adReadSelfActionSharedNodeCreationTool = this.createActivityNodeCreationTool(readSelfAction);
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
        EClass readStructuralFeatureAction = this.umlPackage.getReadStructuralFeatureAction();
        NodeDescription adReadStructuralFeatureActionHolderSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(readStructuralFeatureAction);
        this.adSharedDescription.getChildrenDescriptions().add(adReadStructuralFeatureActionHolderSharedNodeDescription);

        NodeDescription adReadStructuralFeatureActionContentSharedNodeDescription = this.createContentNodeDescription(readStructuralFeatureAction, true);
        this.addContent(readStructuralFeatureAction, true, adReadStructuralFeatureActionHolderSharedNodeDescription, adReadStructuralFeatureActionContentSharedNodeDescription,
                this.symbolNodeDescription);
        this.copyDimension(adReadStructuralFeatureActionHolderSharedNodeDescription, adReadStructuralFeatureActionContentSharedNodeDescription);
        this.addToolSections(adReadStructuralFeatureActionContentSharedNodeDescription, PIN);

        NodeTool adReadStructuralFeatureActionSharedNodeCreationTool = this.createActivityNodeCreationTool(readStructuralFeatureAction);
        this.reuseNodeAndCreateTool(adReadStructuralFeatureActionHolderSharedNodeDescription, diagramDescription, adReadStructuralFeatureActionSharedNodeCreationTool, STRUCTURAL_FEATURE,
                this.activityOwners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ReclassifyObjectAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createReclassifyObjectActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass reclassifyObjectAction = this.umlPackage.getReclassifyObjectAction();
        NodeDescription adReclassifyObjectActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(reclassifyObjectAction);
        this.adSharedDescription.getChildrenDescriptions().add(adReclassifyObjectActionSharedNodeDescription);

        this.allowSymbol(adReclassifyObjectActionSharedNodeDescription);
        this.addToolSections(adReclassifyObjectActionSharedNodeDescription, PIN);

        NodeTool adReclassifyObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(reclassifyObjectAction);
        this.reuseNodeAndCreateTool(adReclassifyObjectActionSharedNodeDescription, diagramDescription, adReclassifyObjectActionSharedNodeCreationTool, CREATE_OBJECT_ACTION, this.activityOwners,
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
        EClass reduceAction = this.umlPackage.getReduceAction();
        NodeDescription adReduceActionHolderSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(reduceAction);
        this.adSharedDescription.getChildrenDescriptions().add(adReduceActionHolderSharedNodeDescription);
        adReduceActionHolderSharedNodeDescription.setDefaultHeightExpression(SIZE_70);
        NodeDescription adReduceActionContentSharedNodeDescription = this.createContentNodeDescription(reduceAction, true);
        this.addContent(reduceAction, true, adReduceActionHolderSharedNodeDescription, adReduceActionContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adReduceActionHolderSharedNodeDescription, adReduceActionContentSharedNodeDescription);
        this.addToolSections(adReduceActionContentSharedNodeDescription, PIN);

        NodeTool adReduceActionSharedNodeCreationTool = this.createActivityNodeCreationTool(reduceAction);
        this.reuseNodeAndCreateTool(adReduceActionHolderSharedNodeDescription, diagramDescription, adReduceActionSharedNodeCreationTool, EXECUTABLE_NODE,
                this.activityOwners, List.of());
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
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.createDefaultLabelStyle(true, false, false))
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
        EClass sendObjectAction = this.umlPackage.getSendObjectAction();
        NodeDescription adSendObjectActionHolderSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(sendObjectAction);
        this.allowSymbol(adSendObjectActionHolderSharedNodeDescription);
        this.adSharedDescription.getChildrenDescriptions().add(adSendObjectActionHolderSharedNodeDescription);
        adSendObjectActionHolderSharedNodeDescription.setDefaultHeightExpression(SIZE_50);
        this.addToolSections(adSendObjectActionHolderSharedNodeDescription, PIN);

        NodeTool adSendObjectActionSharedNodeCreationTool = this.createActivityNodeCreationTool(sendObjectAction);
        this.reuseNodeAndCreateTool(adSendObjectActionHolderSharedNodeDescription, diagramDescription, adSendObjectActionSharedNodeCreationTool, INVOCATION_ACTION, this.activityOwners, List.of());
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
        adSendSignalActionSharedNodeDescription.setDefaultHeightExpression(SIZE_70);
        this.adSharedDescription.getChildrenDescriptions().add(adSendSignalActionSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adSendSignalActionSharedNodeDescription);
        this.addToolSections(adSendSignalActionSharedNodeDescription, PIN);

        NodeTool adSendSignalActionSharedNodeCreationTool = this.createActivityNodeCreationTool(this.umlPackage.getSendSignalAction());
        this.reuseNodeAndCreateTool(adSendSignalActionSharedNodeDescription, diagramDescription, adSendSignalActionSharedNodeCreationTool, INVOCATION_ACTION, this.activityOwners, List.of());
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
        NodeDescription adSequenceNodeHolderSharedNodeDescription = this.newNodeBuilder(sequenceNodeEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(sequenceNodeEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(sequenceNodeEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(sequenceNodeEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, true))
                .build();

        NodeDescription adSequenceNodeContentSharedNodeDescription = this.createContentNodeDescription(sequenceNodeEClass, true);
        this.addContent(sequenceNodeEClass, true, adSequenceNodeHolderSharedNodeDescription, adSequenceNodeContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adSequenceNodeHolderSharedNodeDescription, adSequenceNodeContentSharedNodeDescription);
        adSequenceNodeHolderSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adSequenceNodeHolderSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        this.adSharedDescription.getChildrenDescriptions().add(adSequenceNodeHolderSharedNodeDescription);

        this.addToolSections(adSequenceNodeContentSharedNodeDescription, PIN, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE, STRUCTURAL_FEATURE,
                EXECUTABLE_NODE,
                ACCEPT_EVENT_ACTION);
        NodeTool adSequenceNodeSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(sequenceNodeEClass);
        this.reuseNodeAndCreateTool(adSequenceNodeHolderSharedNodeDescription, diagramDescription, adSequenceNodeSharedNodeCreationTool, STRUCTURED_ACTIVITY_NODE, this.activityOwners, List.of());

        DropNodeTool adSequenceNodeGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adSequenceNodeContentSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adSequenceNodeContentSharedNodeDescription, () -> {
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
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, forbiddenTypes);
            adSequenceNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adSequenceNodeContentSharedNodeDescription.getPalette().setDropNodeTool(adSequenceNodeGraphicalDropTool);
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
        EClass startClassifierBehaviorAction = this.umlPackage.getStartClassifierBehaviorAction();
        NodeDescription adStartClassifierBehaviorActionHolderSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(startClassifierBehaviorAction);
        this.adSharedDescription.getChildrenDescriptions().add(adStartClassifierBehaviorActionHolderSharedNodeDescription);

        NodeDescription adStartClassifierBehaviorActionContentSharedNodeDescription = this.createContentNodeDescription(startClassifierBehaviorAction, true);
        this.addContent(startClassifierBehaviorAction, true, adStartClassifierBehaviorActionHolderSharedNodeDescription, adStartClassifierBehaviorActionContentSharedNodeDescription,
                this.symbolNodeDescription);
        this.copyDimension(adStartClassifierBehaviorActionHolderSharedNodeDescription, adStartClassifierBehaviorActionContentSharedNodeDescription);
        this.addToolSections(adStartClassifierBehaviorActionContentSharedNodeDescription, PIN);

        NodeTool adStartClassifierBehaviorActionSharedNodeCreationTool = this.createActivityNodeCreationTool(startClassifierBehaviorAction);
        this.reuseNodeAndCreateTool(adStartClassifierBehaviorActionHolderSharedNodeDescription, diagramDescription, adStartClassifierBehaviorActionSharedNodeCreationTool, EXECUTABLE_NODE,
                this.activityOwners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link StartObjectBehaviorAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createStartObjectBehaviorActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass startObjectBehaviorAction = this.umlPackage.getStartObjectBehaviorAction();
        NodeDescription adStartObjectBehaviorActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(startObjectBehaviorAction);
        this.adSharedDescription.getChildrenDescriptions().add(adStartObjectBehaviorActionSharedNodeDescription);
        this.allowSymbol(adStartObjectBehaviorActionSharedNodeDescription);
        this.addToolSections(adStartObjectBehaviorActionSharedNodeDescription, PIN);

        NodeTool adStartObjectBehaviorActionSharedNodeCreationTool = this.createActivityNodeCreationTool(startObjectBehaviorAction);
        this.reuseNodeAndCreateTool(adStartObjectBehaviorActionSharedNodeDescription, diagramDescription, adStartObjectBehaviorActionSharedNodeCreationTool, INVOCATION_ACTION,
                this.activityOwners, List.of());
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
        NodeDescription adStructuredActivityNodeHolderSharedNodeDescription = this.newNodeBuilder(structuredActivityNodeEClass, rectangularNodeStyle) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(structuredActivityNodeEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ActivityDiagramServices.GET_ACTIVITY_NODE_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(structuredActivityNodeEClass.getName())) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(structuredActivityNodeEClass.getName())) //
                .build();
        adStructuredActivityNodeHolderSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adStructuredActivityNodeHolderSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);

        NodeDescription adStructuredActivityNodeContentSharedNodeDescription = this.createContentNodeDescription(structuredActivityNodeEClass, true);
        this.addContent(structuredActivityNodeEClass, true, adStructuredActivityNodeHolderSharedNodeDescription, adStructuredActivityNodeContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adStructuredActivityNodeHolderSharedNodeDescription, adStructuredActivityNodeContentSharedNodeDescription);
        this.adSharedDescription.getChildrenDescriptions().add(adStructuredActivityNodeHolderSharedNodeDescription);

        this.addToolSections(adStructuredActivityNodeContentSharedNodeDescription, ACTIVITY_NODE, PIN, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE,
                STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adStructuredActivityNodeSharedNodeCreationTool = this.createStructuredActivityNodeCreationTool(structuredActivityNodeEClass);
        this.reuseNodeAndCreateTool(adStructuredActivityNodeHolderSharedNodeDescription, diagramDescription, adStructuredActivityNodeSharedNodeCreationTool, STRUCTURED_ACTIVITY_NODE,
                this.activityOwners, List.of());

        DropNodeTool adStructuredActivityNodeGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adStructuredActivityNodeContentSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivityNode(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(adStructuredActivityNodeContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, this.borderNodeTypes);
            adStructuredActivityNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adStructuredActivityNodeContentSharedNodeDescription.getPalette().setDropNodeTool(adStructuredActivityNodeGraphicalDropTool);
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
        NodeDescription adActivityHolderSharedNodeDescription = this.newNodeBuilder(activityEClass, rectangularNodeStyle) //
                // Explicitly set the name because IdBuilder can't add a prefix before the domain type name.
                .semanticCandidateExpression("aql:self.oclAsType(uml::Activity).nestedClassifier")//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(activityEClass.getName())) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(activityEClass.getName())) //
                .build();

        adActivityHolderSharedNodeDescription.setName("AD_SubActivity_SHARED");
        adActivityHolderSharedNodeDescription.setDefaultWidthExpression(CONTAINER_NODE_SIZE);
        adActivityHolderSharedNodeDescription.setDefaultHeightExpression(CONTAINER_NODE_SIZE);
        NodeDescription adActivityContentSharedNodeDescription = this.createContentNodeDescription(activityEClass, true);
        this.addContent(activityEClass, true, adActivityHolderSharedNodeDescription, adActivityContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adActivityHolderSharedNodeDescription, adActivityContentSharedNodeDescription);
        this.adSharedDescription.getChildrenDescriptions().add(adActivityHolderSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(adActivityContentSharedNodeDescription);
        this.addToolSections(adActivityContentSharedNodeDescription, ACTIVITY_GROUP, ACTIVITY_NODE, EXPANSION_REGION, INVOCATION_ACTION, CREATE_OBJECT_ACTION, STRUCTURED_ACTIVITY_NODE,
                STRUCTURAL_FEATURE,
                EXECUTABLE_NODE, ACCEPT_EVENT_ACTION);

        NodeTool adActivitySharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getClass_NestedClassifier(), activityEClass);
        this.reuseNodeAndCreateTool(adActivityHolderSharedNodeDescription, diagramDescription,
                adActivitySharedNodeCreationTool, ACTIVITY_GROUP, List.of(activityEClass), List.of());

        DropNodeTool adActivityGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(adActivityContentSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getActivity(), this.umlPackage.getActivityNode(), this.umlPackage.getActivityPartition(), this.umlPackage.getComment(),
                this.umlPackage.getConstraint(), this.umlPackage.getInterruptibleActivityRegion());
        this.registerCallback(adActivityContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, this.borderNodeTypes).stream()
                    .filter(nd -> nd.equals(adActivityHolderSharedNodeDescription))
                    .toList();
            adActivityGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        adActivityContentSharedNodeDescription.getPalette().setDropNodeTool(adActivityGraphicalDropTool);
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link TestIdentityAction}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createTestIdentityActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass testIdentityAction = this.umlPackage.getTestIdentityAction();
        NodeDescription adTestIdentityActionSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(testIdentityAction);
        this.adSharedDescription.getChildrenDescriptions().add(adTestIdentityActionSharedNodeDescription);

        this.allowSymbol(adTestIdentityActionSharedNodeDescription);

        this.addToolSections(adTestIdentityActionSharedNodeDescription, PIN);

        NodeTool adTestIdentityActionSharedNodeCreationTool = this.createActivityNodeCreationTool(testIdentityAction);
        this.reuseNodeAndCreateTool(adTestIdentityActionSharedNodeDescription, diagramDescription, adTestIdentityActionSharedNodeCreationTool, NODES, this.activityOwners, List.of());
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
        this.reuseNodeAndCreateTool(adValuePinSharedNodeDescription, diagramDescription, adValuePinSharedNodeCreationTool, PIN, owners, List.of());
    }

    /**
     * Creates a {@link NodeDescription} representing an UML {@link ValueSpecification}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}
     * @see #createSharedRoundedRectangleActionNodeDescription(EClass)
     */
    private void createValueSpecificationActionSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass valueSpecificationAction = this.umlPackage.getValueSpecificationAction();
        NodeDescription adValueSpecificationActionHolderSharedNodeDescription = this.createSharedRoundedRectangleActionNodeDescription(valueSpecificationAction);
        this.adSharedDescription.getChildrenDescriptions().add(adValueSpecificationActionHolderSharedNodeDescription);

        NodeDescription adValueSpecificationActionContentSharedNodeDescription = this.createContentNodeDescription(valueSpecificationAction, true);
        this.addContent(valueSpecificationAction, true, adValueSpecificationActionHolderSharedNodeDescription, adValueSpecificationActionContentSharedNodeDescription, this.symbolNodeDescription);
        this.copyDimension(adValueSpecificationActionHolderSharedNodeDescription, adValueSpecificationActionContentSharedNodeDescription);
        this.addToolSections(adValueSpecificationActionContentSharedNodeDescription, PIN);

        NodeTool adValueSpecificationActionSharedNodeCreationTool = this.createActivityNodeCreationTool(valueSpecificationAction);
        this.reuseNodeAndCreateTool(adValueSpecificationActionHolderSharedNodeDescription, diagramDescription, adValueSpecificationActionSharedNodeCreationTool, EXECUTABLE_NODE,
                this.activityOwners, List.of());
    }

    /**
     * Creates an {@link EdgeDescription} representing an UML {@link ControlFlow}.
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createControlFlowEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceDescriptionSupplier = () -> this.collectNodesWithoutContent(diagramDescription, List.of(this.umlPackage.getActivityNode()),
                List.of(this.umlPackage.getFinalNode()));
        Supplier<List<NodeDescription>> targetDescriptionSupplier = () -> this.collectNodesWithoutContent(diagramDescription, List.of(this.umlPackage.getActivityNode()),
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
                .filter(nd -> !SHARED_DESCRIPTIONS.equals(nd.getName()))
                .filter(nd -> !nd.getName().contains(HOLDER_SUFFIX))
                .filter(nd -> !Objects.equals(nd.getName(), "AD_DecisionNode_Note_SHARED")) //
                .filter(nd -> !forbiddenDescription.contains(nd)) //
                .toList();
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
    protected List<NodeDescription> collectNodesWithoutContent(DiagramDescription description, List<EClass> domains, List<EClass> forbiddenDomains) {
        List<NodeDescription> forbiddenDescription = this.collectNodesWithDomain(description, forbiddenDomains.toArray(EClass[]::new));
        return this.collectNodesWithDomain(description, domains.toArray(EClass[]::new)).stream() //
                .filter(nd -> !SHARED_DESCRIPTIONS.equals(nd.getName()))
                .filter(nd -> !nd.getName().contains(CONTENT_SUFFIX))
                .filter(nd -> !Objects.equals(nd.getName(), "AD_DecisionNode_Note_SHARED")) //
                .filter(nd -> !forbiddenDescription.contains(nd)) //
                .toList();
    }
}

/*****************************************************************************
 * Copyright (c) 2022, 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 199, Issue 190
 *  Titouan BOUËTE-GIRAUD (Artal Technologies) - titouan.bouete-giraud@artal.fr - Issues 219, 227
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.view.CreationToolsUtil;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.sirius.components.view.ChangeContext;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.ImageNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.ListLayoutStrategyDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.OutsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Builder of the "State machine" diagram representation description.
 *
 * @author Laurent Fasani
 */
public class SMDDiagramDescriptionBuilder extends AbstractRepresentationDescriptionBuilder {

    public static final String SMD_REP_NAME = "State Machine Diagram";

    public static final String SMD_PREFIX = "SMD_";

    public static final String SYMBOLS_COMPARTMENT_SUFFIX = "Symbols";

    public static final int STATEMACHINE_NODE_BORDER_RADIUS = 10;

    public static final String SHOW_HIDE = "SHOW_HIDE";

    private static final String PSEUDO_STATE = "Pseudostate";

    private static final String ROUND_ICON_NODE_DEFAULT_DIAMETER = "30";

    private static final String ICON_PATH = "/icons-override/full/obj16/";

    private static final String ICON_SVG_EXTENSION = ".svg";

    private final UMLPackage umlPackage = UMLPackage.eINSTANCE;

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription smSharedDescription;

    public SMDDiagramDescriptionBuilder() {
        super(SMD_PREFIX, SMD_REP_NAME, UMLPackage.eINSTANCE.getStateMachine());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {

        this.createDefaultToolSectionInDiagramDescription(diagramDescription);

        this.smSharedDescription = this.createSharedDescription(diagramDescription);

        NodeDescription stateMachineNodeDescription = this.createStateMachineNodeDescription(diagramDescription);
        this.createTransitionEdgeDescription(diagramDescription);
        this.createCommentTopNodeDescription(diagramDescription, NODES);

        this.createRegionSharedNodeDescription(diagramDescription);
        NodeDescription stateNodeDescription = this.createStateSharedNodeDescription(diagramDescription);
        this.createFinalStateSharedNodeDescription(diagramDescription);
        this.createPseudostateSharedNodeDescription(diagramDescription);
        this.createPseudostateBorderSharedNodeDescription(stateMachineNodeDescription, diagramDescription, this.umlPackage.getStateMachine_ConnectionPoint(), this.umlPackage.getStateMachine(),
                "StateMachine");
        this.createPseudostateBorderSharedNodeDescription(stateNodeDescription, diagramDescription, this.umlPackage.getState_ConnectionPoint(), this.umlPackage.getState(), "State");

        this.createCommentSubNodeDescription(diagramDescription, this.smSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getComment(), SHARED_SUFFIX),
                List.of(this.umlPackage.getRegion(), this.umlPackage.getStateMachine()));

        DiagramToolSection showHideToolSection = this.getViewBuilder().createDiagramToolSection(SHOW_HIDE);
        diagramDescription.getPalette().getToolSections().add(showHideToolSection);
        this.createHideSymbolTool(diagramDescription,
                SHOW_HIDE);
        this.createShowSymbolTool(diagramDescription, SHOW_HIDE);
        this.createHideAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);
        this.createShowAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);

        List<EClass> symbolOwners = List.of(
                this.umlPackage.getRegion(),
                this.umlPackage.getState(),
                this.umlPackage.getStateMachine());
        this.createSymbolSharedNodeDescription(diagramDescription, this.smSharedDescription, symbolOwners, List.of(), SYMBOLS_COMPARTMENT_SUFFIX);

        // There is a unique DropTool for the DiagramDescription
        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));

        DropNodeTool smdGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getDiagramGraphicalDropToolName());
        List<EClass> children = List.of(this.umlPackage.getRegion(), this.umlPackage.getState(), this.umlPackage.getPseudostate(), this.umlPackage.getState(), this.umlPackage.getFinalState(),
                this.umlPackage.getComment());
        this.registerCallback(diagramDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            // Filter Entry and Exit points
            droppedNodeDescriptions.stream().filter(tool -> tool.getName().endsWith("_BorderedNode_SHARED"));
            smdGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        diagramDescription.getPalette().setDropNodeTool(smdGraphicalDropTool);

    }

    private NodeDescription createStateMachineNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(STATEMACHINE_NODE_BORDER_RADIUS);
        ListLayoutStrategyDescription listLayoutStrategyDescription = DiagramFactory.eINSTANCE.createListLayoutStrategyDescription();
        NodeDescription smdStateMachineNodeDesc = this.newNodeBuilder(this.umlPackage.getStateMachine(), rectangularNodeStyle)//
                .layoutStrategyDescription(listLayoutStrategyDescription)//
                .semanticCandidateExpression(this.getQueryBuilder().querySelf())//
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(this.umlPackage.getStateMachine().getName()))//
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyle(false, true))
                .build();
        diagramDescription.getNodeDescriptions().add(smdStateMachineNodeDesc);

        // workaround to overcome missing enhancement https://github.com/PapyrusSirius/papyrus-web/issues/121
        // It is not possible to define that there is no delete tool.
        // The only way is to define a delete tool that does nothing
        smdStateMachineNodeDesc.getPalette().setDeleteTool(DiagramFactory.eINSTANCE.createDeleteTool());

        DropNodeTool smGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(smdStateMachineNodeDesc));
        List<EClass> children = List.of(this.umlPackage.getRegion(), this.umlPackage.getPseudostate());
        this.registerCallback(smdStateMachineNodeDesc, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            smGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        return smdStateMachineNodeDesc;
    }

    private NodeDescription createRegionSharedNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyleDescription = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyleDescription.setBackground(this.styleProvider.getTransparentColor());

        // We need here to add a non empty label so the renderer displays a label and the label separator
        // This is the only way we can have compartment with no border but still display a line between each compartment
        NodeDescription regionNodeDesc = this.newNodeBuilder(this.umlPackage.getRegion(), rectangularNodeStyleDescription)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getStateMachine_Region()))//
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(this.umlPackage.getRegion().getName()))//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(this.umlPackage.getRegion().getName()))//
                .build();

        regionNodeDesc.setName(regionNodeDesc.getName() + UNDERSCORE + SHARED_SUFFIX);

        this.smSharedDescription.getChildrenDescriptions().add(regionNodeDesc);
        this.createDefaultToolSectionsInNodeDescription(regionNodeDesc);

        NodeTool regionStateSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getState_Region(), this.umlPackage.getRegion());
        // This reuse shall also add the regionNodeDescription as a growable node in its parent.
        this.reuseNodeAndCreateTool(regionNodeDesc, diagramDescription, regionStateSharedNodeCreationTool, NODES, List.of(this.umlPackage.getState()), List.of(this.umlPackage.getFinalState()),
                List.of(this.umlPackage.getState(), this.umlPackage.getStateMachine()));

        NodeTool regionStateMachineSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getStateMachine_Region(), this.umlPackage.getRegion());
        this.reuseNodeAndCreateTool(regionNodeDesc, diagramDescription, regionStateMachineSharedNodeCreationTool, NODES, List.of(this.umlPackage.getStateMachine()).toArray(EClass[]::new));

        DropNodeTool smRegionGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(regionNodeDesc));
        List<EClass> children = List.of(this.umlPackage.getState(), this.umlPackage.getPseudostate(), this.umlPackage.getComment());
        this.registerCallback(regionNodeDesc, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            smRegionGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        regionNodeDesc.getPalette().setDropNodeTool(smRegionGraphicalDropTool);

        return regionNodeDesc;
    }

    private NodeDescription createStateSharedNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(STATEMACHINE_NODE_BORDER_RADIUS);
        InsideLabelDescription labelDescription = this.getViewBuilder().createDefaultInsideLabelDescription(false, false);

        // Display the header separator only if there is a region in the state
        String condition = "aql:self.region->size() > 0";
        labelDescription.getConditionalStyles()
                .add(this.getViewBuilder().createConditionalInsideLabelStyle(condition, this.getViewBuilder().createDefaultInsideLabelStyle(false, true)));

        RectangularNodeStyleDescription headerStyle = this.getViewBuilder().createRectangularNodeStyle();
        headerStyle.setBorderRadius(STATEMACHINE_NODE_BORDER_RADIUS);

        ConditionalNodeStyle conditionalHeaderStyle = this.getViewBuilder().createConditionalNodeStyle(condition, headerStyle);

        NodeDescription stateNodeDesc = this.newNodeBuilder(this.umlPackage.getState(), rectangularNodeStyle)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createListLayoutStrategyDescription())//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getRegion_Subvertex()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .conditionalStyles(List.of(conditionalHeaderStyle)) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(this.umlPackage.getState().getName()))//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(this.umlPackage.getState().getName()))//
                .insideLabelDescription(labelDescription)
                .build();

        stateNodeDesc.setName(stateNodeDesc.getName() + UNDERSCORE + SHARED_SUFFIX);

        this.smSharedDescription.getChildrenDescriptions().add(stateNodeDesc);
        this.createDefaultToolSectionsInNodeDescription(stateNodeDesc);

        NodeTool stateSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getRegion_Subvertex(), this.umlPackage.getState());
        List<EClass> owners = List.of(this.umlPackage.getRegion());
        this.reuseNodeAndCreateTool(stateNodeDesc, diagramDescription, stateSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Package container
        DropNodeTool stateGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(stateNodeDesc));
        List<EClass> children = List.of(this.umlPackage.getRegion());
        this.registerCallback(stateGraphicalDropTool, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            stateGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        stateNodeDesc.getPalette().setDropNodeTool(stateGraphicalDropTool);

        return stateNodeDesc;
    }

    private void createFinalStateSharedNodeDescription(DiagramDescription diagramDescription) {
        ImageNodeStyleDescription imageNodeStyle = this.getViewBuilder().createImageNodeStyle("view/images/FinalState_24dp.svg");

        NodeDescription finalStateNodeDesc = this.newNodeBuilder(this.umlPackage.getFinalState(), imageNodeStyle)//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getRegion_Subvertex()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(this.umlPackage.getFinalState().getName()))//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(this.umlPackage.getFinalState().getName()))//
                .addOutsideLabelDescription(this.getViewBuilder().createDefaultOutsideLabelDescription(true))
                .build();

        finalStateNodeDesc.setName(finalStateNodeDesc.getName() + UNDERSCORE + SHARED_SUFFIX);

        finalStateNodeDesc.setDefaultWidthExpression(ROUND_ICON_NODE_DEFAULT_DIAMETER);
        finalStateNodeDesc.setDefaultHeightExpression(ROUND_ICON_NODE_DEFAULT_DIAMETER);

        this.smSharedDescription.getChildrenDescriptions().add(finalStateNodeDesc);

        NodeTool finalStateSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getRegion_Subvertex(), this.umlPackage.getFinalState());
        List<EClass> owners = List.of(this.umlPackage.getRegion());
        this.reuseNodeAndCreateTool(finalStateNodeDesc, diagramDescription, finalStateSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

    }

    private void createPseudostateSharedNodeDescription(DiagramDescription diagramDescription) {
        List<PseudostateKind> pseudostateKinds = new ArrayList<>(List.of(PseudostateKind.values()));
        pseudostateKinds.removeAll(List.of(PseudostateKind.ENTRY_POINT_LITERAL, PseudostateKind.EXIT_POINT_LITERAL));

        String specializedDomainNodeName = this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getPseudostate(), SHARED_SUFFIX);
        NodeDescription pseudostateNodeDesc = this.createPseudostateNodeDescription(diagramDescription, this.umlPackage.getRegion_Subvertex(), this.umlPackage.getRegion(), pseudostateKinds,
                specializedDomainNodeName);

        //
        this.smSharedDescription.getChildrenDescriptions().add(pseudostateNodeDesc);

        NodeTool pseudoStateNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getRegion_Subvertex(), this.umlPackage.getPseudostate());
        List<EClass> owners = List.of(this.umlPackage.getRegion());
        this.reuseNodeAndCreateTool(pseudostateNodeDesc, diagramDescription, pseudoStateNodeCreationTool, NODES, owners.toArray(EClass[]::new));

    }

    private void createPseudostateBorderSharedNodeDescription(NodeDescription parentNode, DiagramDescription diagramDescription, EReference containmentFeature, EClass owner, String name) {
        List<PseudostateKind> pseudostateKinds = List.of(PseudostateKind.ENTRY_POINT_LITERAL, PseudostateKind.EXIT_POINT_LITERAL);
        String suffix = name + "_BorderedNode_SHARED";
        String specializedDomainNodeName = this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getPseudostate(), suffix);
        NodeDescription pseudostateBorderNodeDesc = this.createPseudostateNodeDescription(diagramDescription, containmentFeature, owner, pseudostateKinds, specializedDomainNodeName);

        parentNode.getBorderNodesDescriptions().add(pseudostateBorderNodeDesc);

    }

    private NodeDescription createPseudostateNodeDescription(DiagramDescription diagramDescription, EReference containmentFeature, EClass ownerClass,
            List<PseudostateKind> pseudostateKinds, String name) {
        List<ConditionalNodeStyle> conditionalNodeStyles = new ArrayList<>();
        List<NodeTool> creationTools = new ArrayList<>();

        final OutsideLabelDescription labelStyle = this.getViewBuilder().createDefaultOutsideLabelDescription(true);

        NodeDescription pseudostateNodeDesc = this.newNodeBuilder(this.umlPackage.getPseudostate(), null)//
                .name(name)//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(containmentFeature))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(this.umlPackage.getPseudostate().getName()))//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(this.umlPackage.getPseudostate().getName()))//
                .addOutsideLabelDescription(labelStyle) //
                .build();

        for (PseudostateKind pseudostateKind : pseudostateKinds) {
            String condition = "aql:self.kind = uml::PseudostateKind::" + pseudostateKind.getLiteral();
            String literal = pseudostateKind.getLiteral();
            String literalName = literal.substring(0, 1).toUpperCase() + literal.substring(1);
            String imageName = literalName + ".svg";

            NodeStyleDescription nodeStyle;
            if (pseudostateKind.equals(PseudostateKind.FORK_LITERAL) || pseudostateKind.equals(PseudostateKind.JOIN_LITERAL)) {
                nodeStyle = this.getViewBuilder().createRectangularNodeStyle();
            } else {
                nodeStyle = this.getViewBuilder().createImageNodeStyle("view/images/" + imageName);
            }

            ConditionalNodeStyle conditionalNodeStyle = this.getViewBuilder().createConditionalNodeStyle(condition,
                    nodeStyle);
            conditionalNodeStyles.add(conditionalNodeStyle);

            // Node creation tool
            NodeTool creationTool = DiagramFactory.eINSTANCE.createNodeTool();
            creationTool.setName("New " + literalName);
            creationTool.setIconURLsExpression(ICON_PATH + PSEUDO_STATE + UNDERSCORE + literal + ICON_SVG_EXTENSION);

            // Create instance and init
            ChangeContext createElement = this.getViewBuilder().createChangeContextOperation(CallQuery.queryServiceOnSelf(StateMachineDiagramServices.CREATE_PSEUDO_STATE, //
                    "'uml::Pseudostate'", //
                    String.format("'%s'", containmentFeature.getName()), //
                    "selectedNode", //
                    "diagramContext", //
                    "convertedNodes", //
                    String.format("'%s'", pseudostateKind)));

            creationTool.getBody().add(createElement);

            creationTools.add(creationTool);

            List<EClass> owners = List.of(ownerClass);
            this.reuseTool(pseudostateNodeDesc, diagramDescription, creationTool, owners, List.of(),
                    nodeDescription -> nodeDescription != null);

        }

        pseudostateNodeDesc.getConditionalStyles().addAll(conditionalNodeStyles);

        pseudostateNodeDesc.setDefaultWidthExpression(CallQuery.queryServiceOnSelf(StateMachineDiagramServices.COMPUTE_PSEUDO_STATE_WITDTH));
        pseudostateNodeDesc.setDefaultHeightExpression(CallQuery.queryServiceOnSelf(StateMachineDiagramServices.COMPUTE_PSEUDO_STATE_HEIGHT));
        pseudostateNodeDesc.setKeepAspectRatio(true);

        return pseudostateNodeDesc;
    }

    private void createTransitionEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> vertexNodeDescriptions = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getVertex());
        EdgeDescription transitionEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getTransition(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getTransition()), vertexNodeDescriptions, vertexNodeDescriptions);
        transitionEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        diagramDescription.getEdgeDescriptions().add(transitionEdgeDescription);
        transitionEdgeDescription.getPalette().setCenterLabelEditTool(null);
        EdgeTool edgeTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(transitionEdgeDescription, this.umlPackage.getRegion_Transition());
        this.registerCallback(transitionEdgeDescription, () -> {
            CreationToolsUtil.addEdgeCreationTool(vertexNodeDescriptions, edgeTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(transitionEdgeDescription);
    }
}

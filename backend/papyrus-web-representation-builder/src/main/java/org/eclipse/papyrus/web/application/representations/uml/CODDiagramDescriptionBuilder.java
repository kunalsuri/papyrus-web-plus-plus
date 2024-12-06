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
package org.eclipse.papyrus.web.application.representations.uml;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.papyrus.web.application.representations.view.builders.CallbackAdapter;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.uml2.uml.DurationObservation;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.TimeObservation;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Builder of the "Communication" diagram representation.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public final class CODDiagramDescriptionBuilder extends AbstractRepresentationDescriptionBuilder {

    /**
     * Size of TimeObservation and DurationObservation nodes.
     */
    public static final String OBSERVATION_SIZE = "25";

    /**
     * The name of the representation handled by this builder.
     */
    public static final String COD_REP_NAME = "Communication Diagram";

    /**
     * The prefix of the representation handled by this builder.
     */
    public static final String COD_PREFIX = "COD_";

    /**
     * The suffix used to identify <i>receptions</i> compartments.
     */
    public static final String SYMBOLS_COMPARTMENT_SUFFIX = "Symbols";

    /**
     * Size of the radius corner of the Interaction Node initialized on the diagram at its creation.
     */
    public static final int INTERACTION_NODE_BORDER_RADIUS = 10;

    /**
     * Factory used to create UML elements.
     */
    private final UMLPackage umlPackage = UMLPackage.eINSTANCE;

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription codSharedDescription;

    private NodeDescription symbolNodeDescription;

    /**
     * Initializes the builder.
     */
    public CODDiagramDescriptionBuilder() {
        super(COD_PREFIX, COD_REP_NAME, UMLPackage.eINSTANCE.getNamedElement());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {
        diagramDescription.setPreconditionExpression(CallQuery.queryServiceOnSelf(CommunicationDiagramServices.CAN_CREATE_DIAGRAM));
        this.createDefaultToolSectionInDiagramDescription(diagramDescription);

        this.codSharedDescription = this.createSharedDescription(diagramDescription);
        List<EClass> symbolOwners = List.of(
                this.umlPackage.getInteraction(),
                this.umlPackage.getLifeline());
        this.symbolNodeDescription = this.createSymbolSharedNodeDescription(diagramDescription, symbolOwners, List.of(), SYMBOLS_COMPARTMENT_SUFFIX);

        DiagramToolSection showHideToolSection = this.getViewBuilder().createDiagramToolSection(SHOW_HIDE);
        diagramDescription.getPalette().getToolSections().add(showHideToolSection);
        this.createHideSymbolTool(diagramDescription,
                SHOW_HIDE);
        this.createShowSymbolTool(diagramDescription, SHOW_HIDE);
        this.createHideAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);
        this.createShowAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);

        NodeDescription codInteractionDescription = this.createInteractionTopNodeDescription(diagramDescription);
        this.createLifelineSubNodeDescription(codInteractionDescription);
        this.createDurationObservationSubNodeDescription(codInteractionDescription);
        this.createTimeObservationSubNodeDescription(codInteractionDescription);
        this.createMessageEdgeDescription(diagramDescription);

        this.createCommentSubNodeDescription(diagramDescription, codInteractionDescription, NODES, this.getIdBuilder().getDomainNodeName(this.umlPackage.getComment()),
                List.of(this.umlPackage.getInteraction()));
        this.createConstraintSubNodeDescription(diagramDescription, codInteractionDescription, NODES, this.getIdBuilder().getDomainNodeName(this.umlPackage.getConstraint()),
                List.of(this.umlPackage.getInteraction()));

        this.codSharedDescription.getChildrenDescriptions().add(this.symbolNodeDescription);

        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Interaction}.
     *
     * @param diagramDescription
     *            the Communication {@link DiagramDescription} containing the created {@link NodeDescription}
     * @return the {@link NodeDescription} representing an UML {@link Interaction}.
     */
    protected NodeDescription createInteractionTopNodeDescription(DiagramDescription diagramDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularNodeStyle.setBorderRadius(INTERACTION_NODE_BORDER_RADIUS);

        EClass interactionEClass = this.umlPackage.getInteraction();
        NodeDescription codInteractionHolderTopNodeDescription = this.newNodeBuilder(interactionEClass, rectangularNodeStyle)//
                .semanticCandidateExpression(this.getQueryBuilder().querySelf())//
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(interactionEClass.getName()))//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        NodeDescription codInteractionContentTopNodeDescription = this.createContentNodeDescription(interactionEClass, false);
        this.addContent(interactionEClass, false, codInteractionHolderTopNodeDescription, codInteractionContentTopNodeDescription, this.symbolNodeDescription);
        codInteractionHolderTopNodeDescription.setDefaultWidthExpression(ROOT_ELEMENT_WIDTH);
        codInteractionHolderTopNodeDescription.setDefaultHeightExpression(ROOT_ELEMENT_HEIGHT);
        this.copyDimension(codInteractionHolderTopNodeDescription, codInteractionContentTopNodeDescription);
        diagramDescription.getNodeDescriptions().add(codInteractionHolderTopNodeDescription);

        return codInteractionContentTopNodeDescription;
    }

    /**
     * Create the {@link NodeDescription} and creation tool representing an UML {@link Lifeline}.
     *
     * @param parentNodeDescription
     *            the {@link NodeDescription} containing the {@link Lifeline} {@link NodeDescription}
     */
    private void createLifelineSubNodeDescription(NodeDescription parentNodeDescription) {
        RectangularNodeStyleDescription rectangularNodeStyle = this.getViewBuilder().createRectangularNodeStyle();

        EClass lifelineEClass = this.umlPackage.getLifeline();

        NodeDescription codLifelineSubNodeDescription = this.newNodeBuilder(lifelineEClass, rectangularNodeStyle) //
                .layoutStrategyDescription(this.createListLayoutStrategy())//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(UMLPackage.eINSTANCE.getInteraction_Lifeline()))
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(lifelineEClass.getName())) //
                .name(this.getIdBuilder().getDomainNodeName(lifelineEClass))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(lifelineEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        this.createDefaultToolSectionsInNodeDescription(codLifelineSubNodeDescription);

        this.allowSymbol(codLifelineSubNodeDescription);
        parentNodeDescription.getChildrenDescriptions().add(codLifelineSubNodeDescription);

        NodeTool codLifelineSubNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getInteraction_Lifeline(), lifelineEClass);
        this.addNodeToolInToolSection(List.of(parentNodeDescription), codLifelineSubNodeCreationTool, NODES);

    }

    /**
     * Create the {@link NodeDescription} and creation tool representing an UML {@link DurationObservation}.
     *
     * @param parentNodeDescription
     *            the {@link NodeDescription} containing the {@link DurationObservation} {@link NodeDescription}
     */
    private void createDurationObservationSubNodeDescription(NodeDescription parentNodeDescription) {
        NodeStyleDescription durationObservationNodeStyle = this.getViewBuilder().createImageNodeStyle("view/images/DurationObservation.svg");
        durationObservationNodeStyle.setBorderSize(1);

        EClass durationObservationEClass = this.umlPackage.getDurationObservation();
        NodeDescription codDurationObservationSubNodeDescription = this.newNodeBuilder(durationObservationEClass, durationObservationNodeStyle)//
                .name(this.getIdBuilder().getDomainNodeName(durationObservationEClass)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(CommunicationDiagramServices.GET_DURATION_OBSERVATION_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(durationObservationEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(durationObservationEClass.getName())) //
                .addOutsideLabelDescription(this.getViewBuilder().createDefaultOutsideLabelDescription(true))
                .build();
        codDurationObservationSubNodeDescription.setDefaultWidthExpression(OBSERVATION_SIZE);
        codDurationObservationSubNodeDescription.setDefaultHeightExpression(OBSERVATION_SIZE);
        parentNodeDescription.getChildrenDescriptions().add(codDurationObservationSubNodeDescription);

        // create durationObservation tool sections
        this.createDefaultToolSectionsInNodeDescription(codDurationObservationSubNodeDescription);

        NodeTool codDurationObservationSubNodeCreationTool = this.getViewBuilder().createCreationTool(this.getIdBuilder().getCreationToolId(durationObservationEClass),
                CallQuery.queryServiceOnSelf(CommunicationDiagramServices.GET_PACKAGE_CONTAINER), this.umlPackage.getPackage_PackagedElement(), durationObservationEClass);
        this.addNodeToolInToolSection(List.of(parentNodeDescription), codDurationObservationSubNodeCreationTool, NODES);
    }

    /**
     * Create the {@link NodeDescription} and creation tool representing an UML {@link TimeObservation}.
     *
     * @param parentNodeDescription
     *            the {@link NodeDescription} containing the {@link TimeObservation} {@link NodeDescription}
     */
    private void createTimeObservationSubNodeDescription(NodeDescription parentNodeDescription) {
        NodeStyleDescription timeObservationNodeStyle = this.getViewBuilder().createImageNodeStyle("view/images/TimeObservation.svg");
        timeObservationNodeStyle.setBorderSize(1);

        EClass timeObservationEClass = this.umlPackage.getTimeObservation();
        NodeDescription codTimeObservationSubNodeDescription = this.newNodeBuilder(timeObservationEClass, timeObservationNodeStyle)//
                .name(this.getIdBuilder().getDomainNodeName(timeObservationEClass)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(CommunicationDiagramServices.GET_TIME_OBSERVATION_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(timeObservationEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(timeObservationEClass.getName())) //
                .addOutsideLabelDescription(this.getViewBuilder().createDefaultOutsideLabelDescription(true))
                .build();
        codTimeObservationSubNodeDescription.setDefaultWidthExpression(OBSERVATION_SIZE);
        codTimeObservationSubNodeDescription.setDefaultHeightExpression(OBSERVATION_SIZE);

        parentNodeDescription.getChildrenDescriptions().add(codTimeObservationSubNodeDescription);

        // create timeObservation tool sections
        this.createDefaultToolSectionsInNodeDescription(codTimeObservationSubNodeDescription);

        NodeTool codTimeObservationSubNodeCreationTool = this.getViewBuilder().createCreationTool(this.getIdBuilder().getCreationToolId(timeObservationEClass),
                CallQuery.queryServiceOnSelf(CommunicationDiagramServices.GET_PACKAGE_CONTAINER), this.umlPackage.getPackage_PackagedElement(), timeObservationEClass);
        this.addNodeToolInToolSection(List.of(parentNodeDescription), codTimeObservationSubNodeCreationTool, NODES);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Message}.
     *
     * @param diagramDescription
     *            the Communication {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createMessageEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceAndTargetDescriptionsSupplier = () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getLifeline());

        EClass messageEClass = this.umlPackage.getMessage();
        EdgeDescription codMessageEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(messageEClass, this.getQueryBuilder().queryAllReachable(messageEClass),
                sourceAndTargetDescriptionsSupplier, sourceAndTargetDescriptionsSupplier);
        // codMessageEdgeDescription.setSynchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED);
        codMessageEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        codMessageEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);

        EdgeTool codMessageEdgeCreationTool = this.getViewBuilder().createDomainBasedEdgeToolWithService("New Message", CommunicationDiagramServices.CREATE_MESSAGE);

        codMessageEdgeDescription.eAdapters().add(new CallbackAdapter(() -> {
            List<DiagramElementDescription> targetNodeDescriptions = codMessageEdgeDescription.getTargetDescriptions();
            codMessageEdgeCreationTool.getTargetElementDescriptions().addAll(targetNodeDescriptions);
        }));

        this.registerCallback(codMessageEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceAndTargetDescriptionsSupplier.get(), codMessageEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(codMessageEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(codMessageEdgeDescription);
    }

}

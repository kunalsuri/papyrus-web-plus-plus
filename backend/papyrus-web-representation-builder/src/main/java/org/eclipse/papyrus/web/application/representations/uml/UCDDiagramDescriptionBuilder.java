/*****************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo.
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
 *  Titouan BOUËTE-GIRAUD (Artal Technologies) - titouan.bouete-giraud@artal.fr - Issue 219, 227
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import static java.util.stream.Collectors.joining;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.CONVERTED_NODES;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DIAGRAM_CONTEXT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SELECTED_NODE;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.papyrus.web.application.representations.view.aql.Services;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.NodeToolSection;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.customnodes.EllipseNodeStyleDescription;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Extend;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Include;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.PackageMerge;
import org.eclipse.uml2.uml.Realization;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Usage;
import org.eclipse.uml2.uml.UseCase;

/**
 * Builder of the "UseCase Diagram " diagram representation.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public final class UCDDiagramDescriptionBuilder extends AbstractRepresentationDescriptionBuilder {

    /**
     * The name of the representation handled by this builder.
     */
    public static final String UCD_REP_NAME = "Use Case Diagram";

    /**
     * The prefix of the representation handled by this builder.
     */
    public static final String UCD_PREFIX = "UCD_";

    /**
     * The suffix used to identify <i>receptions</i> compartments.
     */
    public static final String SYMBOLS_COMPARTMENT_SUFFIX = "Symbols";

    /**
     * The name used to identify the Tool section.
     */
    public static final String SHOW_HIDE = "SHOW_HIDE";

    /**
     * The suffix of "Classifier as Subject" creation tool
     */
    private static final String AS_SUBJECT = " as Subject";

    /**
     * Subject tool section name.
     */
    private static final String SUBJECT = "Subject";

    /**
     * Factory used to create UML elements.
     */
    private final UMLPackage umlPackage = UMLPackage.eINSTANCE;

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription ucdSharedDescription;

    public UCDDiagramDescriptionBuilder() {
        super(UCD_PREFIX, UCD_REP_NAME, UMLPackage.eINSTANCE.getPackage());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {

        // create diagram tool sections
        this.createToolSectionsWithSubjectInDiagramDescription(diagramDescription);
        diagramDescription.setPreconditionExpression(CallQuery.queryServiceOnSelf(Services.IS_NOT_PROFILE_MODEL));

        // create show/hide tool section
        DiagramToolSection showHideToolSection = this.getViewBuilder().createDiagramToolSection(SHOW_HIDE);
        diagramDescription.getPalette().getToolSections().add(showHideToolSection);
        this.createHideSymbolTool(diagramDescription,
                SHOW_HIDE);
        this.createShowSymbolTool(diagramDescription, SHOW_HIDE);
        this.createHideAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);
        this.createShowAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);

        // create node descriptions with their tools
        this.createActivityAsSubjectTopNodeDescription(diagramDescription);
        this.createActorTopNodeDescription(diagramDescription);
        this.createClassAsSubjectTopNodeDescription(diagramDescription);
        this.createCommentTopNodeDescription(diagramDescription, NODES);
        this.createComponentAsSubjectTopNodeDescription(diagramDescription);
        this.createConstraintTopNodeDescription(diagramDescription, NODES);
        this.createInteractionAsSubjectTopNodeDescription(diagramDescription);
        this.createPackageTopNodeDescription(diagramDescription);
        this.createStateMachineAsSubjectTopNodeDescription(diagramDescription);
        this.createUseCaseTopNodeDescription(diagramDescription);

        // create shared node descriptions with their tools
        List<EClass> commentAndConstraintOwners = List.of(this.umlPackage.getPackage(), //
                this.umlPackage.getActivity(), //
                this.umlPackage.getClass_(), //
                this.umlPackage.getComponent(), //
                this.umlPackage.getInteraction(), //
                this.umlPackage.getStateMachine());
        this.ucdSharedDescription = this.createSharedDescription(diagramDescription);
        this.createActivityAsSubjectSharedNodeDescription(diagramDescription);
        this.createActorSharedNodeDescription(diagramDescription);
        this.createClassAsSubjectSharedNodeDescription(diagramDescription);
        this.createCommentSubNodeDescription(diagramDescription, this.ucdSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getComment(), SHARED_SUFFIX), commentAndConstraintOwners);
        this.createComponentAsSubjectSharedNodeDescription(diagramDescription);
        this.createConstraintSubNodeDescription(diagramDescription, this.ucdSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getConstraint(), SHARED_SUFFIX), commentAndConstraintOwners);
        this.createInteractionAsSubjectSharedNodeDescription(diagramDescription);
        this.createStateMachineAsSubjectSharedNodeDescription(diagramDescription);
        this.createUseCaseSharedNodeDescription(diagramDescription);

        // create edge descriptions with their tools
        this.createAbstractionEdgeDescription(diagramDescription);
        this.createAssociationEdgeDescription(diagramDescription);
        this.createDependencyEdgeDescription(diagramDescription);
        this.createExtendEdgeDescription(diagramDescription);
        this.createGeneralizationEdgeDescription(diagramDescription);
        this.createIncludeEdgeDescription(diagramDescription);
        this.createPackageImportEdgeDescription(diagramDescription);
        this.createPackageMergeEdgeDescription(diagramDescription);
        this.createRealizationEdgeDescription(diagramDescription);
        this.createUsageEdgeDescription(diagramDescription);

        List<EClass> symbolOwners = List.of(
                this.umlPackage.getActivity(),
                this.umlPackage.getClass_(),
                this.umlPackage.getInteraction(),
                this.umlPackage.getStateMachine(),
                this.umlPackage.getPackage());
        this.createSymbolSharedNodeDescription(diagramDescription, this.ucdSharedDescription, symbolOwners, List.of(), SYMBOLS_COMPARTMENT_SUFFIX);

        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));

        // Add dropped tool on diagram
        DropNodeTool ucdGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getDiagramGraphicalDropToolName());
        List<EClass> children = List.of(this.umlPackage.getUseCase(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getActor(), this.umlPackage.getPackage(),
                this.umlPackage.getClass_());
        this.registerCallback(diagramDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ucdGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        diagramDescription.getPalette().setDropNodeTool(ucdGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Activity} as Subject on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createActivityAsSubjectTopNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectTopNodeDescription(diagramDescription, this.umlPackage.getActivity());
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Actor} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createActorTopNodeDescription(DiagramDescription diagramDescription) {
        NodeStyleDescription actorNodeStyle = this.getViewBuilder().createImageNodeStyle("view/images/Actor.svg");
        EClass actorEClass = this.umlPackage.getActor();
        NodeDescription ucdActorTopNodeDescription = this.newNodeBuilder(actorEClass, actorNodeStyle)//
                .name(this.getIdBuilder().getDomainNodeName(actorEClass)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(actorEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(actorEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(actorEClass.getName())) //
                .addOutsideLabelDescription(this.getViewBuilder().createDefaultOutsideLabelDescription(true))
                .build();
        ucdActorTopNodeDescription.setDefaultWidthExpression("70");
        ucdActorTopNodeDescription.setDefaultHeightExpression("100");
        ucdActorTopNodeDescription.setKeepAspectRatio(true);

        diagramDescription.getNodeDescriptions().add(ucdActorTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ucdActorTopNodeDescription);

        NodeTool ucdActorTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), actorEClass);
        this.addDiagramToolInToolSection(diagramDescription, ucdActorTopNodeCreationTool, NODES);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Class} as Subject on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createClassAsSubjectTopNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectTopNodeDescription(diagramDescription, this.umlPackage.getClass_());
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Component} as Subject on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createComponentAsSubjectTopNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectTopNodeDescription(diagramDescription, this.umlPackage.getComponent());
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Interaction} as Subject on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createInteractionAsSubjectTopNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectTopNodeDescription(diagramDescription, this.umlPackage.getInteraction());
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Package} on the Diagram.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageTopNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.umlPackage.getPackage();
        NodeDescription ucdPackageTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                this.getQueryBuilder().queryAllReachableExactType(packageEClass));
        diagramDescription.getNodeDescriptions().add(ucdPackageTopNodeDescription);

        this.createToolSectionsWithSubjectInNodeDescription(ucdPackageTopNodeDescription);

        NodeTool ucdPackageTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        this.addDiagramToolInToolSection(diagramDescription, ucdPackageTopNodeCreationTool, NODES);

        this.createPackageSubNodeDescription(diagramDescription, ucdPackageTopNodeDescription);

        // Add dropped tool on Package container
        DropNodeTool ucdPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ucdPackageTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getUseCase(), this.umlPackage.getComment(), this.umlPackage.getPackage(), this.umlPackage.getConstraint(), this.umlPackage.getActor(),
                this.umlPackage.getClass_());
        this.registerCallback(ucdPackageTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ucdPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ucdPackageTopNodeDescription.getPalette().setDropNodeTool(ucdPackageGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link StateMachine} as Subject on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createStateMachineAsSubjectTopNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectTopNodeDescription(diagramDescription, this.umlPackage.getStateMachine());
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link UseCase} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createUseCaseTopNodeDescription(DiagramDescription diagramDescription) {
        EClass useCaseEClass = this.umlPackage.getUseCase();

        EllipseNodeStyleDescription useCaseNodeStyle = this.getViewBuilder().createEllipseNodeStyle();

        InsideLabelDescription description = DiagramFactory.eINSTANCE.createInsideLabelDescription();
        description.setTextAlign(LabelTextAlign.CENTER);
        description.setLabelExpression(this.getQueryBuilder().queryRenderLabel());
        InsideLabelStyle style = this.getViewBuilder().createDefaultInsideLabelStyle(true, false);
        description.setStyle(style);
        description.setPosition(InsideLabelPosition.MIDDLE_CENTER);

        NodeDescription ucdUseCaseTopNodeDescription = this.newNodeBuilder(useCaseEClass, useCaseNodeStyle)//
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(UseCaseDiagramServices.GET_USECASE_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(useCaseEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(useCaseEClass.getName())) //
                .insideLabelDescription(description)
                .build();
        ucdUseCaseTopNodeDescription.setDefaultWidthExpression("204");
        ucdUseCaseTopNodeDescription.setDefaultHeightExpression("104");

        diagramDescription.getNodeDescriptions().add(ucdUseCaseTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ucdUseCaseTopNodeDescription);

        // create tools
        NodeTool ucdUseCaseTopNodeCreationTool = this.getViewBuilder().createCreationTool(useCaseEClass.getName(), this.getIdBuilder().getCreationToolId(useCaseEClass),
                UseCaseDiagramServices.CREATE_USECASE,
                List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        this.addDiagramToolInToolSection(diagramDescription, ucdUseCaseTopNodeCreationTool, NODES);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Package} in {@code parentNodeDescription}.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param parentNodeDescription
     *            the parent {@link NodeDescription} which contain definition of the new {@link NodeDescription}
     */
    private void createPackageSubNodeDescription(DiagramDescription diagramDescription, NodeDescription parentNodeDescription) {
        EClass packageEClass = this.umlPackage.getPackage();
        NodeDescription ucdPackageSubNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()));
        ucdPackageSubNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(packageEClass, PACKAGE_CHILD));

        this.createToolSectionsWithSubjectInNodeDescription(ucdPackageSubNodeDescription);

        parentNodeDescription.getChildrenDescriptions().add(ucdPackageSubNodeDescription);
        NodeTool ucdPackageSubNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        this.getNodeToolSection(parentNodeDescription, NODES).getNodeTools().add(ucdPackageSubNodeCreationTool);

        // Add dropped tool on Sub-Package container
        DropNodeTool ucdPackageGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ucdPackageSubNodeDescription, PACKAGE_CHILD));
        List<EClass> children = List.of(this.umlPackage.getUseCase(), this.umlPackage.getComment(), this.umlPackage.getPackage(), this.umlPackage.getConstraint(), this.umlPackage.getActor(),
                this.umlPackage.getClass_());
        this.registerCallback(ucdPackageSubNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ucdPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ucdPackageSubNodeDescription.getPalette().setDropNodeTool(ucdPackageGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Activity} as Subject.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createActivityAsSubjectSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectSharedNodeDescription(diagramDescription, this.umlPackage.getActivity());
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Actor}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createActorSharedNodeDescription(DiagramDescription diagramDescription) {
        NodeStyleDescription actorNodeStyle = this.getViewBuilder().createImageNodeStyle("view/images/Actor.svg");
        EClass actorEClass = this.umlPackage.getActor();
        NodeDescription ucdActorSharedNodeDescription = this.newNodeBuilder(actorEClass, actorNodeStyle)//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(actorEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(actorEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(actorEClass.getName())) //
                .addOutsideLabelDescription(this.getViewBuilder().createDefaultOutsideLabelDescription(true))
                .build();
        ucdActorSharedNodeDescription.setDefaultWidthExpression("70");
        ucdActorSharedNodeDescription.setDefaultHeightExpression("100");
        ucdActorSharedNodeDescription.setKeepAspectRatio(true);
        this.ucdSharedDescription.getChildrenDescriptions().add(ucdActorSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ucdActorSharedNodeDescription);

        NodeTool ucdActorSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), actorEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ucdActorSharedNodeDescription, diagramDescription, ucdActorSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Class} as Subject.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createClassAsSubjectSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectSharedNodeDescription(diagramDescription, this.umlPackage.getClass_());
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Component} as Subject.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createComponentAsSubjectSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectSharedNodeDescription(diagramDescription, this.umlPackage.getComponent());
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Interaction} as Subject.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createInteractionAsSubjectSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectSharedNodeDescription(diagramDescription, this.umlPackage.getInteraction());
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link StateMachine} as Subject.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createStateMachineAsSubjectSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierAsSubjectSharedNodeDescription(diagramDescription, this.umlPackage.getStateMachine());
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link UseCase}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createUseCaseSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass useCaseEClass = this.umlPackage.getUseCase();

        EllipseNodeStyleDescription useCaseNodeStyle = this.getViewBuilder().createEllipseNodeStyle();

        InsideLabelDescription description = DiagramFactory.eINSTANCE.createInsideLabelDescription();
        description.setLabelExpression(this.getQueryBuilder().queryRenderLabel());
        description.setTextAlign(LabelTextAlign.CENTER);
        InsideLabelStyle style = this.getViewBuilder().createDefaultInsideLabelStyle(true, false);
        description.setStyle(style);
        description.setPosition(InsideLabelPosition.MIDDLE_CENTER);

        NodeDescription ucdUseCaseSharedNodeDescription = this.newNodeBuilder(useCaseEClass, useCaseNodeStyle)//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(useCaseEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(UseCaseDiagramServices.GET_USECASE_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(useCaseEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(useCaseEClass.getName())) //
                .insideLabelDescription(description)
                .build();

        ucdUseCaseSharedNodeDescription.setDefaultWidthExpression("204");
        ucdUseCaseSharedNodeDescription.setDefaultHeightExpression("104");

        this.ucdSharedDescription.getChildrenDescriptions().add(ucdUseCaseSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ucdUseCaseSharedNodeDescription);

        NodeTool ucdUseCaseSharedNodeCreationTool = this.getViewBuilder().createCreationTool(useCaseEClass.getName(), this.getIdBuilder().getCreationToolId(useCaseEClass),
                UseCaseDiagramServices.CREATE_USECASE,
                List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));

        List<EClass> owners = List.of(this.umlPackage.getClass_(), //
                this.umlPackage.getActivity(), //
                this.umlPackage.getComponent(), //
                this.umlPackage.getInteraction(), //
                this.umlPackage.getStateMachine(), //
                this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ucdUseCaseSharedNodeDescription, diagramDescription, ucdUseCaseSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Abstraction}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createAbstractionEdgeDescription(DiagramDescription diagramDescription) {
        this.createDependencyOrSubTypeEdgeDescription(diagramDescription, this.umlPackage.getAbstraction(), LineStyle.DASH, ArrowStyle.INPUT_ARROW);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Association}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createAssociationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceAndTargetDescriptionsSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClassifier());

        EClass associationEClass = this.umlPackage.getAssociation();
        EdgeDescription ucdAssociationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(associationEClass,
                this.getQueryBuilder().queryAllReachableExactType(associationEClass),
                sourceAndTargetDescriptionsSupplier, sourceAndTargetDescriptionsSupplier);
        ucdAssociationEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        ucdAssociationEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.NONE);
        ucdAssociationEdgeDescription.getStyle().setSourceArrowStyle(ArrowStyle.NONE);

        EdgeTool ucdAssociationEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ucdAssociationEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(ucdAssociationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceAndTargetDescriptionsSupplier.get(), ucdAssociationEdgeCreationTool);
        });

        ucdAssociationEdgeDescription.setBeginLabelExpression(this.getQueryBuilder().createDomainBaseEdgeSourceLabelExpression());
        ucdAssociationEdgeDescription.getPalette().setBeginLabelEditTool(this.getViewBuilder().createDirectEditTool(CallQuery.queryServiceOnSelf(ClassDiagramServices.GET_ASSOCIATION_TARGET)));

        ucdAssociationEdgeDescription.setEndLabelExpression(this.getQueryBuilder().createDomainBaseEdgeTargetLabelExpression());
        ucdAssociationEdgeDescription.getPalette().setEndLabelEditTool(this.getViewBuilder().createDirectEditTool(CallQuery.queryServiceOnSelf(ClassDiagramServices.GET_ASSOCIATION_SOURCE)));

        diagramDescription.getEdgeDescriptions().add(ucdAssociationEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(ucdAssociationEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Dependency}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createDependencyEdgeDescription(DiagramDescription diagramDescription) {
        this.createDependencyOrSubTypeEdgeDescription(diagramDescription, this.umlPackage.getDependency(), LineStyle.DASH, ArrowStyle.INPUT_ARROW);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Extend}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createExtendEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> useCaseCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getUseCase());
        EClass extendEClass = this.umlPackage.getExtend();
        EdgeDescription ucdExtendEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(extendEClass,
                this.getQueryBuilder().queryAllReachableExactType(extendEClass),
                useCaseCollector, useCaseCollector);
        EdgeStyle style = ucdExtendEdgeDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        diagramDescription.getEdgeDescriptions().add(ucdExtendEdgeDescription);

        EdgeTool ucdExtendEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ucdExtendEdgeDescription, this.umlPackage.getUseCase_Extend());
        this.registerCallback(ucdExtendEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(useCaseCollector.get(), ucdExtendEdgeCreationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(ucdExtendEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Generalization}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createGeneralizationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceAndTargetDescriptionsSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClassifier());

        EClass generalizationEClass = this.umlPackage.getGeneralization();
        EdgeDescription ucdGeneralizationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(generalizationEClass,
                this.getQueryBuilder().queryAllReachableExactType(generalizationEClass), sourceAndTargetDescriptionsSupplier, sourceAndTargetDescriptionsSupplier, false);
        ucdGeneralizationEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        ucdGeneralizationEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        EdgeTool ucdGeneralizationEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ucdGeneralizationEdgeDescription, this.umlPackage.getClassifier_Generalization());
        this.registerCallback(ucdGeneralizationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceAndTargetDescriptionsSupplier.get(), ucdGeneralizationEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(ucdGeneralizationEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(ucdGeneralizationEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Include}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createIncludeEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> useCaseCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getUseCase());
        EdgeDescription ucdIncludeEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getInclude(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getInclude()), useCaseCollector, useCaseCollector);
        EdgeStyle style = ucdIncludeEdgeDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        diagramDescription.getEdgeDescriptions().add(ucdIncludeEdgeDescription);

        EdgeTool ucdIncludeEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ucdIncludeEdgeDescription, this.umlPackage.getUseCase_Include());
        this.registerCallback(ucdIncludeEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(useCaseCollector.get(), ucdIncludeEdgeCreationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(ucdIncludeEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link PackageImport}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createPackageImportEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceDescriptions = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getPackage(), this.umlPackage.getNamespace());
        Supplier<List<NodeDescription>> targetDescriptions = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getPackage());
        EdgeDescription ucdPackageImportEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getPackageImport(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getPackageImport()), sourceDescriptions, targetDescriptions, false);
        ucdPackageImportEdgeDescription.getStyle().setLineStyle(LineStyle.DASH);
        ucdPackageImportEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);

        EdgeTool ucdPackageImportEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ucdPackageImportEdgeDescription, this.umlPackage.getNamespace_PackageImport());
        this.registerCallback(ucdPackageImportEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceDescriptions.get(), ucdPackageImportEdgeCreationTool);
        });
        diagramDescription.getEdgeDescriptions().add(ucdPackageImportEdgeDescription);
        this.getViewBuilder().addDefaultReconnectionTools(ucdPackageImportEdgeDescription);

    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link PackageMerge}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createPackageMergeEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> packageDescriptions = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getPackage());
        EdgeDescription ucdPackageMergeEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getPackageMerge(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getPackageMerge()), packageDescriptions, packageDescriptions, false);
        ucdPackageMergeEdgeDescription.getStyle().setLineStyle(LineStyle.DASH);
        ucdPackageMergeEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        EdgeTool ucdPackageMergeEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ucdPackageMergeEdgeDescription, this.umlPackage.getPackage_PackageMerge());
        this.registerCallback(ucdPackageMergeEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(packageDescriptions.get(), ucdPackageMergeEdgeCreationTool);
        });
        diagramDescription.getEdgeDescriptions().add(ucdPackageMergeEdgeDescription);
        this.getViewBuilder().addDefaultReconnectionTools(ucdPackageMergeEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Realization}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createRealizationEdgeDescription(DiagramDescription diagramDescription) {
        this.createDependencyOrSubTypeEdgeDescription(diagramDescription, this.umlPackage.getRealization(), LineStyle.DASH, ArrowStyle.INPUT_CLOSED_ARROW);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Usage}.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createUsageEdgeDescription(DiagramDescription diagramDescription) {
        this.createDependencyOrSubTypeEdgeDescription(diagramDescription, this.umlPackage.getUsage(), LineStyle.DASH, ArrowStyle.INPUT_ARROW);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Classifier} as Subject on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param classifierAsSubject
     *            the type of {@link Classifier} to create
     */
    private void createClassifierAsSubjectTopNodeDescription(DiagramDescription diagramDescription, EClass classifierAsSubject) {
        NodeDescription ucdClassifierTopNodeDescription = this.newNodeBuilder(classifierAsSubject, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getDomainNodeName(classifierAsSubject)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(classifierAsSubject))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(classifierAsSubject.getName()))//
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyle(true, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(classifierAsSubject.getName())) //
                .build();
        diagramDescription.getNodeDescriptions().add(ucdClassifierTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ucdClassifierTopNodeDescription);

        NodeTool ucdClassifierTopNodeCreationTool = this.createAsSubjectCreationTool(this.umlPackage.getPackage_PackagedElement(), classifierAsSubject);
        this.addDiagramToolInToolSection(diagramDescription, ucdClassifierTopNodeCreationTool, SUBJECT);

        // Add dropped tool on Classifier container
        DropNodeTool ucdClassifierGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ucdClassifierTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getUseCase(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(ucdClassifierTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ucdClassifierGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ucdClassifierTopNodeDescription.getPalette().setDropNodeTool(ucdClassifierGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Classifier} as Subject.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param classifierAsSubject
     *            the type of {@link Classifier} to create
     */
    private void createClassifierAsSubjectSharedNodeDescription(DiagramDescription diagramDescription, EClass classifierAsSubject) {
        NodeDescription ucdClassifierSharedNodeDescription = this.newNodeBuilder(classifierAsSubject, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(classifierAsSubject, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(classifierAsSubject.getName()))//
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyle(true, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(classifierAsSubject.getName())) //
                .build();
        this.ucdSharedDescription.getChildrenDescriptions().add(ucdClassifierSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ucdClassifierSharedNodeDescription);

        NodeTool ucdClassifierSharedNodeCreationTool = this.createAsSubjectCreationTool(this.umlPackage.getPackage_PackagedElement(), classifierAsSubject);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ucdClassifierSharedNodeDescription, diagramDescription, ucdClassifierSharedNodeCreationTool, SUBJECT, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Classifier container
        DropNodeTool ucdClassifierGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ucdClassifierSharedNodeDescription, SHARED_SUFFIX));
        List<EClass> children = List.of(this.umlPackage.getUseCase(), this.umlPackage.getComment(), this.umlPackage.getConstraint());
        this.registerCallback(ucdClassifierSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ucdClassifierGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ucdClassifierSharedNodeDescription.getPalette().setDropNodeTool(ucdClassifierGraphicalDropTool);
    }

    /**
     * Suffix creation tool with "as Subject".
     *
     * @param containementRef
     *            the reference used to contain the new type
     * @param newType
     *            the new type to create,
     * @return the creation tool
     */
    private NodeTool createAsSubjectCreationTool(EReference containementRef, EClass newType) {
        String domainTypeName = this.getIdBuilder().findWordsInMixedCase(newType.getName()).stream().collect(joining(IdBuilder.SPACE));
        return this.getViewBuilder().createCreationTool(IdBuilder.NEW + domainTypeName + AS_SUBJECT, containementRef, newType);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Dependency} or subType.
     *
     * @param diagramDescription
     *            the UseCase {@link DiagramDescription} containing the created {@link EdgeDescription}
     * @param edgeToCreate
     *            kind of edge to create which should be a Dependency or a subType
     * @param lineStyle
     *            the line style of the edge
     * @param arrowStyle
     *            the arrow style of the edge
     */
    private void createDependencyOrSubTypeEdgeDescription(DiagramDescription diagramDescription, EClass edgeToCreate, LineStyle lineStyle, ArrowStyle arrowStyle) {
        Supplier<List<NodeDescription>> namedElementCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getNamedElement());
        EdgeDescription ucdDependencyEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(edgeToCreate,
                this.getQueryBuilder().queryAllReachableExactType(edgeToCreate), namedElementCollector, namedElementCollector);
        EdgeStyle style = ucdDependencyEdgeDescription.getStyle();
        style.setLineStyle(lineStyle);
        style.setTargetArrowStyle(arrowStyle);
        diagramDescription.getEdgeDescriptions().add(ucdDependencyEdgeDescription);
        EdgeTool ucdDependencyEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ucdDependencyEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(ucdDependencyEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(namedElementCollector.get(), ucdDependencyEdgeCreationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(ucdDependencyEdgeDescription);
    }

    /**
     * Create tools sections "Nodes", "Edges" and "Subject" in the palette tool of a given {@link NodeDescription}.
     *
     * @param nodeDescription
     *            the node description with the palette to complete with tool sections
     */
    private void createToolSectionsWithSubjectInNodeDescription(NodeDescription nodeDescription) {
        NodeToolSection subjectToolSection = this.getViewBuilder().createNodeToolSection(SUBJECT);
        NodeToolSection nodesToolSection = this.getViewBuilder().createNodeToolSection(NODES);
        NodeToolSection edgesToolSection = this.getViewBuilder().createNodeToolSection(EDGES);
        nodeDescription.getPalette().getToolSections().addAll(List.of(subjectToolSection, nodesToolSection, edgesToolSection));
    }

    /**
     * Create tools sections "Nodes", "Edges" and "Subject" in the palette tool of a given {@link DiagramDescription}.
     *
     * @param nodeDescription
     *            the node description with the palette to complete with tool sections
     */
    protected void createToolSectionsWithSubjectInDiagramDescription(DiagramDescription diagramDescription) {
        DiagramToolSection subjectToolSection = this.getViewBuilder().createDiagramToolSection(SUBJECT);
        DiagramToolSection nodesToolSection = this.getViewBuilder().createDiagramToolSection(NODES);
        DiagramToolSection edgesToolSection = this.getViewBuilder().createDiagramToolSection(EDGES);
        diagramDescription.getPalette().getToolSections().addAll(List.of(subjectToolSection, nodesToolSection, edgesToolSection));

    }

}

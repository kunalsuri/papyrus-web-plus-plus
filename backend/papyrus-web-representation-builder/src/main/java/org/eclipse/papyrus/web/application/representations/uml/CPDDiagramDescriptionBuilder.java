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
 *  Titouan BOUËTE-GIRAUD (Artal Technologies) - titouan.bouete-giraud@artal.fr - Issues 219, 227
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.CONVERTED_NODES;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DIAGRAM_CONTEXT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SELECTED_NODE;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.papyrus.web.application.representations.view.aql.Services;
import org.eclipse.papyrus.web.application.representations.view.aql.Variables;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.ConditionalInsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.ListLayoutStrategyDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.ComponentRealization;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.Manifestation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Usage;

/**
 * Builder of the "Component" diagram representation.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public final class CPDDiagramDescriptionBuilder extends AbstractRepresentationDescriptionBuilder {

    /**
     * The suffix used to identify <i>operations</i> compartments.
     */
    public static final String OPERATIONS_COMPARTMENT_SUFFIX = "Operations";

    /**
     * The suffix used to identify <i>attributes</i> compartments.
     */
    public static final String ATTRIBUTES_COMPARTMENT_SUFFIX = "Attributes";

    /**
     * The suffix used to identify <i>receptions</i> compartments.
     */
    public static final String RECEPTIONS_COMPARTMENT_SUFFIX = "Receptions";

    /**
     * The suffix used to identify <i>receptions</i> compartments.
     */
    public static final String SYMBOLS_COMPARTMENT_SUFFIX = "Symbols";

    /**
     * The name of the representation handled by this builder.
     */
    public static final String CPD_REP_NAME = "Component Diagram";

    /**
     * The prefix of the representation handled by this builder.
     */
    public static final String CPD_PREFIX = "CPD_";

    /**
     * Suffix used for nodeDescriptions defined inside Component Description.
     */
    public static final String IN_COMPONENT = "inComponent";

    /**
     * The name used to identify the Tool section.
     */
    public static final String SHOW_HIDE = "SHOW_HIDE";

    /**
     * AQL expression to set children not draggable from its container.
     */
    private static final String CHILD_NOT_DRAGGABLE_EXPRESSION = "aql:false";

    /**
     * Factory used to create UML elements.
     */
    private final UMLPackage umlPackage = UMLPackage.eINSTANCE;

    /**
     * The list of semantic types that are represented as border nodes.
     * <p>
     * This list is used to filter drag & drop targets and prevent border nodes from being droppable.
     * </p>
     */
    private List<EClass> borderNodeTypes = List.of(this.umlPackage.getPort());

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription cpdSharedDescription;

    public CPDDiagramDescriptionBuilder() {
        super(CPD_PREFIX, CPD_REP_NAME, UMLPackage.eINSTANCE.getPackage());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {

        // create diagram tool sections
        this.createDefaultToolSectionInDiagramDescription(diagramDescription);
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
        this.createCommentTopNodeDescription(diagramDescription, NODES);
        this.createConstraintTopNodeDescription(diagramDescription, NODES);
        this.createComponentTopNodeDescription(diagramDescription);
        this.createInterfaceTopNodeDescription(diagramDescription);
        this.createModelTopNodeDescription(diagramDescription);
        this.createPackageTopNodeDescription(diagramDescription);

        // create shared node descriptions with their tools
        this.cpdSharedDescription = this.createSharedDescription(diagramDescription);
        this.createCommentSubNodeDescription(diagramDescription, this.cpdSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getComment(), SHARED_SUFFIX), List.of(this.umlPackage.getPackage()));
        this.createConstraintSubNodeDescription(diagramDescription, this.cpdSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getConstraint(), SHARED_SUFFIX), List.of(this.umlPackage.getPackage()));

        this.createComponentSharedNodeDescription(diagramDescription);
        this.createInterfaceSharedNodeDescription(diagramDescription);
        this.createModelSharedNodeDescription(diagramDescription);
        this.createPackageSharedNodeDescription(diagramDescription);
        this.createPortSharedNodeDescription(diagramDescription);
        this.createPropertySharedNodeDescription(diagramDescription);

        // create shared compartments
        NodeDescription attributesCompartment = this.createCompartmentForInterfaceSharedNodeDescription(diagramDescription, ATTRIBUTES_COMPARTMENT_SUFFIX);
        NodeDescription operationsCompartment = this.createCompartmentForInterfaceSharedNodeDescription(diagramDescription, OPERATIONS_COMPARTMENT_SUFFIX);
        NodeDescription receptionsCompartment = this.createCompartmentForInterfaceSharedNodeDescription(diagramDescription, RECEPTIONS_COMPARTMENT_SUFFIX);
        this.createAttributeListSubNodeDescription(diagramDescription, attributesCompartment);
        this.createOperationListSubNodeDescription(diagramDescription, operationsCompartment);
        this.createReceptionListSubNodeDescription(diagramDescription, receptionsCompartment);

        // create edge descriptions with their tools
        this.createAbstractionEdgeDescription(diagramDescription);
        this.createComponentRealizationEdgeDescription(diagramDescription);
        this.createConnectorEdgeDescription(diagramDescription);
        this.createDependencyEdgeDescription(diagramDescription);
        this.createGeneralizationEdgeDescription(diagramDescription);
        this.createInterfaceRealizationEdgeDescription(diagramDescription);
        this.createManifestationEdgeDescription(diagramDescription);
        this.createSubstitutionEdgeDescription(diagramDescription);
        this.createUsageEdgeDescription(diagramDescription);

        List<EClass> symbolOwners = List.of(
                this.umlPackage.getComponent(),
                this.umlPackage.getInterface(),
                this.umlPackage.getProperty(),
                this.umlPackage.getPackage());

        this.createSymbolSharedNodeDescription(diagramDescription, this.cpdSharedDescription, symbolOwners, List.of(), SYMBOLS_COMPARTMENT_SUFFIX);

        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));

        // Add dropped tool on diagram
        DropNodeTool cpdGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getDiagramGraphicalDropToolName());
        List<EClass> children = List.of(this.umlPackage.getComponent(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getInterface(), this.umlPackage.getModel(),
                this.umlPackage.getPackage());
        this.registerCallback(diagramDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cpdGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        diagramDescription.getPalette().setDropNodeTool(cpdGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Component} at Diagram and {@link Component}
     * levels.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createComponentTopNodeDescription(DiagramDescription diagramDescription) {
        EClass componentEClass = this.umlPackage.getComponent();
        NodeDescription cpdComponentTopNodeDescription = this.newNodeBuilder(componentEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getDomainNodeName(componentEClass)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(componentEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(componentEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(componentEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        diagramDescription.getNodeDescriptions().add(cpdComponentTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cpdComponentTopNodeDescription);

        NodeTool cpdComponentTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getComponent_PackagedElement(), componentEClass);
        this.addDiagramToolInToolSection(diagramDescription, cpdComponentTopNodeCreationTool, NODES);

        // Add dropped tool on Component container
        DropNodeTool cpdComponentGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cpdComponentTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getComponent(), this.umlPackage.getProperty());
        this.registerCallback(cpdComponentTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            cpdComponentGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cpdComponentTopNodeDescription.getPalette().setDropNodeTool(cpdComponentGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Interface} at diagram level.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createInterfaceTopNodeDescription(DiagramDescription diagramDescription) {
        ListLayoutStrategyDescription listLayoutStrategyDescription = DiagramFactory.eINSTANCE.createListLayoutStrategyDescription();
        listLayoutStrategyDescription.setAreChildNodesDraggableExpression(CHILD_NOT_DRAGGABLE_EXPRESSION);
        EClass interfaceEClass = this.umlPackage.getInterface();
        NodeDescription cpdInterfaceTopNodeDescription = this.newNodeBuilder(interfaceEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .layoutStrategyDescription(listLayoutStrategyDescription)//
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(interfaceEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(interfaceEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(interfaceEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        diagramDescription.getNodeDescriptions().add(cpdInterfaceTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cpdInterfaceTopNodeDescription);

        NodeTool cpdInterfaceTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), interfaceEClass);
        this.addDiagramToolInToolSection(diagramDescription, cpdInterfaceTopNodeCreationTool, NODES);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Model} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createModelTopNodeDescription(DiagramDescription diagramDescription) {
        EClass modelEClass = this.umlPackage.getModel();
        NodeDescription cpdModelTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(modelEClass, this.getQueryBuilder().queryAllReachableExactType(modelEClass));
        diagramDescription.getNodeDescriptions().add(cpdModelTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cpdModelTopNodeDescription);

        NodeTool cpdProfileTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), modelEClass);
        this.addDiagramToolInToolSection(diagramDescription, cpdProfileTopNodeCreationTool, NODES);

        // Add dropped tool on Profile container
        DropNodeTool cpdModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cpdModelTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getComponent(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getInterface(), this.umlPackage.getModel(),
                this.umlPackage.getPackage());
        this.registerCallback(cpdModelTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cpdModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cpdModelTopNodeDescription.getPalette().setDropNodeTool(cpdModelGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Package} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageTopNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.umlPackage.getPackage();
        NodeDescription cpdPackageTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getPackage()));
        diagramDescription.getNodeDescriptions().add(cpdPackageTopNodeDescription);

        cpdPackageTopNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        // create Package tool sections
        this.createDefaultToolSectionsInNodeDescription(cpdPackageTopNodeDescription);

        NodeTool cpdPackageTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        this.addDiagramToolInToolSection(diagramDescription, cpdPackageTopNodeCreationTool, NODES);

        // No direct children for Package: the NodeDescriptions it can contain are all defined as shared descriptions.

        // Add dropped tool on Package container
        DropNodeTool cpdPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cpdPackageTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getComponent(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getInterface(), this.umlPackage.getModel(),
                this.umlPackage.getPackage());
        this.registerCallback(cpdPackageTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cpdPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cpdPackageTopNodeDescription.getPalette().setDropNodeTool(cpdPackageGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Component}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createComponentSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass componentEClass = this.umlPackage.getComponent();
        NodeDescription cpdComponentSharedNodeDescription = this.newNodeBuilder(componentEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(componentEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getComponent_PackagedElement()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(componentEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(componentEClass.getName())) //
                .build();
        this.cpdSharedDescription.getChildrenDescriptions().add(cpdComponentSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cpdComponentSharedNodeDescription);

        NodeTool cpdClassifierSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getComponent_PackagedElement(), componentEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage(), this.umlPackage.getModel(), this.umlPackage.getComponent());
        this.reuseNodeAndCreateTool(cpdComponentSharedNodeDescription, diagramDescription, cpdClassifierSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Component container
        DropNodeTool cpdComponentGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cpdComponentSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getComponent(), this.umlPackage.getProperty());
        this.registerCallback(cpdComponentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, this.borderNodeTypes);
            cpdComponentGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cpdComponentSharedNodeDescription.getPalette().setDropNodeTool(cpdComponentGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Interface}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createInterfaceSharedNodeDescription(DiagramDescription diagramDescription) {
        ListLayoutStrategyDescription listLayoutStrategyDescription = DiagramFactory.eINSTANCE.createListLayoutStrategyDescription();
        listLayoutStrategyDescription.setAreChildNodesDraggableExpression(CHILD_NOT_DRAGGABLE_EXPRESSION);
        EClass interfaceEClass = this.umlPackage.getInterface();
        NodeDescription cpdInterfaceSharedNodeDescription = this.newNodeBuilder(interfaceEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(interfaceEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(listLayoutStrategyDescription)//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(interfaceEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(interfaceEClass.getName())) //
                .build();
        this.cpdSharedDescription.getChildrenDescriptions().add(cpdInterfaceSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cpdInterfaceSharedNodeDescription);

        NodeTool cpdClassifierSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), interfaceEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(cpdInterfaceSharedNodeDescription, diagramDescription, cpdClassifierSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Model}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createModelSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass modelEClass = this.umlPackage.getModel();
        NodeDescription cpdModelSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(modelEClass,
                CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()));
        cpdModelSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(modelEClass, SHARED_SUFFIX));
        cpdModelSharedNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        this.cpdSharedDescription.getChildrenDescriptions().add(cpdModelSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cpdModelSharedNodeDescription);

        NodeTool cpdModelSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), modelEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage(), this.umlPackage.getModel());
        this.reuseNodeAndCreateTool(cpdModelSharedNodeDescription, diagramDescription, cpdModelSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Package container
        DropNodeTool cpdModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cpdModelSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getComponent(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getInterface(), this.umlPackage.getModel(),
                this.umlPackage.getPackage());
        this.registerCallback(cpdModelSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cpdModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cpdModelSharedNodeDescription.getPalette().setDropNodeTool(cpdModelGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Package}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.umlPackage.getPackage();
        NodeDescription cpdPackageSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()));
        cpdPackageSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(packageEClass, SHARED_SUFFIX));
        cpdPackageSharedNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        this.cpdSharedDescription.getChildrenDescriptions().add(cpdPackageSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cpdPackageSharedNodeDescription);

        NodeTool cpdPackageSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage(), this.umlPackage.getModel());
        this.reuseNodeAndCreateTool(cpdPackageSharedNodeDescription, diagramDescription, cpdPackageSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Package container
        DropNodeTool cpdPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cpdPackageSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getComponent(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getInterface(), this.umlPackage.getModel(),
                this.umlPackage.getPackage());
        this.registerCallback(cpdPackageSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cpdPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cpdPackageSharedNodeDescription.getPalette().setDropNodeTool(cpdPackageGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Property} at sub-level.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPortSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass portEClass = this.umlPackage.getPort();

        NodeDescription cpdPortSharedNodeDescription = this.getViewBuilder().createSpecializedPortUnsynchonizedNodeDescription(SHARED_SUFFIX, portEClass,
                CallQuery.queryServiceOnSelf(ComponentDiagramServices.GET_PORT_NODE_CANDIDATES));

        this.cpdSharedDescription.getBorderNodesDescriptions().add(cpdPortSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cpdPortSharedNodeDescription);

        // create tools
        NodeTool cpdPortSharedNodeCreationTool = this.getViewBuilder().createCreationTool(portEClass.getName(), this.getIdBuilder().getCreationToolId(portEClass), ComponentDiagramServices.CREATE_PORT,
                List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        cpdPortSharedNodeCreationTool
                .setPreconditionExpression(
                        CallQuery.queryServiceOnSelf(ComponentDiagramServices.CAN_CREATE_PROPERTY_INTO_PARENT));

        List<EClass> owners = List.of(this.umlPackage.getProperty(), //
                this.umlPackage.getStructuredClassifier());
        // Port should be exclude from owners because it is not possible to create BorderNode on BorderNode
        this.reuseNodeAndCreateTool(cpdPortSharedNodeDescription, diagramDescription, cpdPortSharedNodeCreationTool, NODES, owners, List.of(this.umlPackage.getPort()));
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Property} at sub-level.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPropertySharedNodeDescription(DiagramDescription diagramDescription) {
        EClass propertyEClass = this.umlPackage.getProperty();

        // Style for Property with attribute isStatic=false and aggregation=shared
        RectangularNodeStyleDescription rectangularDashNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularDashNodeStyle.setBorderLineStyle(LineStyle.DASH);
        ConditionalNodeStyle dashConditionalStyle = this.getViewBuilder().createConditionalNodeStyle(
                "aql:self.oclIsKindOf(uml::Property) and (uml::AggregationKind::shared = self.oclAsType(uml::Property).aggregation)", //
                rectangularDashNodeStyle);

        InsideLabelDescription insideLabelDescription = this.getViewBuilder().createInsideLabelDescription(this.getQueryBuilder().queryRenderLabel(), true, true);
        // Style for Property with attribute isStatic=true and aggregation=shared
        String condition = "aql:self.oclIsKindOf(uml::Property) and (uml::AggregationKind::shared = self.oclAsType(uml::Property).aggregation) and self.isStatic";
        RectangularNodeStyleDescription rectangularDashAndUnderlineNodeStyle = this.getViewBuilder().createRectangularNodeStyle();
        rectangularDashAndUnderlineNodeStyle.setBorderLineStyle(LineStyle.DASH);
        InsideLabelStyle consitionalStyle = this.getViewBuilder().createDefaultInsideLabelStyle(true, true);
        consitionalStyle.setUnderline(true);
        ConditionalInsideLabelStyle conditionalLabelDescription = this.getViewBuilder().createConditionalInsideLabelStyle(condition, consitionalStyle);
        insideLabelDescription.getConditionalStyles().add(conditionalLabelDescription);

        ConditionalNodeStyle dashAndUnderlineConditionalStyle = this.getViewBuilder().createConditionalNodeStyle(
                condition, //
                rectangularDashAndUnderlineNodeStyle);

        NodeDescription cpdPropertySharedNodeDescription = this.newNodeBuilder(propertyEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(propertyEClass, CPDDiagramDescriptionBuilder.SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ComponentDiagramServices.GET_PROPERTY_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(propertyEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(propertyEClass.getName())) //
                .insideLabelDescription(insideLabelDescription)
                .conditionalStyles(List.of(dashAndUnderlineConditionalStyle, dashConditionalStyle)) //
                .build();
        this.cpdSharedDescription.getChildrenDescriptions().add(cpdPropertySharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cpdPropertySharedNodeDescription);

        // create tools
        NodeTool cpdPropertySharedNodeCreationTool = this.getViewBuilder().createCreationTool(propertyEClass.getName(), this.getIdBuilder().getCreationToolId(propertyEClass),
                ComponentDiagramServices.CREATE_PROPERTY,
                List.of(SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        cpdPropertySharedNodeCreationTool
                .setPreconditionExpression(
                        CallQuery.queryServiceOnSelf(ComponentDiagramServices.CAN_CREATE_PROPERTY_INTO_PARENT));

        List<EClass> owners = List.of(this.umlPackage.getProperty(), //
                this.umlPackage.getStructuredClassifier());
        // Port should be exclude from owners because it is not possible to create Property in Port
        this.reuseNodeAndCreateTool(cpdPropertySharedNodeDescription, diagramDescription, cpdPropertySharedNodeCreationTool, NODES, owners, List.of(this.umlPackage.getPort()));

        // Add dropped tool on Component container
        DropNodeTool cpdPropertyGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cpdPropertySharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getProperty());
        this.registerCallback(cpdPropertySharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cpdPropertyGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cpdPropertySharedNodeDescription.getPalette().setDropNodeTool(cpdPropertyGraphicalDropTool);
    }

    /**
     * Creates a shared compartment reused by <i>Interface</i> {@link NodeDescription}.
     * <p>
     * The created {@link NodeDescription} compartment is added to the <i>shared</i> {@link NodeDescription} of the
     * diagram.
     * <p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param compartmentName
     *            the name of the compartment to create
     */
    private NodeDescription createCompartmentForInterfaceSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass interfaceEClass = this.umlPackage.getInterface();
        List<EClass> owners = List.of(interfaceEClass);
        List<EClass> forbiddenOwners = List.of();
        NodeDescription cpdCompartmentForInterfaceSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.cpdSharedDescription, interfaceEClass, compartmentName,
                owners,
                forbiddenOwners, nodeDescription -> nodeDescription != null);
        return cpdCompartmentForInterfaceSharedNodeDescription;
    }

    /**
     * Creates a <i>Property</i> child reused by <i>Attributes</i> compartments.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createAttributeListSubNodeDescription(DiagramDescription diagramDescription, NodeDescription parentNodeDescription) {
        List<EClass> owners = List.of(this.umlPackage.getInterface());
        List<EClass> forbiddenOwners = List.of();
        NodeDescription attributeSubNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, parentNodeDescription, this.umlPackage.getProperty(),
                ATTRIBUTES_COMPARTMENT_SUFFIX, CallQuery.queryOperationOnSelf(this.umlPackage.getClassifier__GetAllAttributes()), this.umlPackage.getStructuredClassifier_OwnedAttribute(), owners,
                forbiddenOwners, nodeDescription -> nodeDescription != null);

        // Add Attribute Graphical dropped tool on Shared Compartment for Interface
        DropNodeTool cpdAttributeGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(parentNodeDescription));
        cpdAttributeGraphicalDropTool.getAcceptedNodeTypes().addAll(List.of(attributeSubNodeDescription));
        parentNodeDescription.getPalette().setDropNodeTool(cpdAttributeGraphicalDropTool);
    }

    /**
     * Creates a <i>Operation</i> child reused by <i>Operations</i> compartments.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createOperationListSubNodeDescription(DiagramDescription diagramDescription, NodeDescription parentNodeDescription) {
        List<EClass> owners = List.of(this.umlPackage.getInterface());
        List<EClass> forbiddenOwners = List.of();
        NodeDescription operationSubNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, parentNodeDescription, this.umlPackage.getOperation(),
                OPERATIONS_COMPARTMENT_SUFFIX, CallQuery.queryOperationOnSelf(this.umlPackage.getClassifier__GetAllOperations()), this.umlPackage.getClass_OwnedOperation(), owners, forbiddenOwners,
                nodeDescription -> nodeDescription != null);

        // Add Operation Graphical dropped tool on Shared Compartment for Interface
        DropNodeTool cpdOperationGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(parentNodeDescription));
        cpdOperationGraphicalDropTool.getAcceptedNodeTypes().addAll(List.of(operationSubNodeDescription));
        parentNodeDescription.getPalette().setDropNodeTool(cpdOperationGraphicalDropTool);

    }

    /**
     * Creates a <i>Operation</i> child reused by <i>Receptions</i> compartments.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createReceptionListSubNodeDescription(DiagramDescription diagramDescription, NodeDescription parentNodeDescription) {
        List<EClass> owners = List.of(this.umlPackage.getInterface());
        List<EClass> forbiddenOwners = List.of();
        NodeDescription receptionSubNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, parentNodeDescription, this.umlPackage.getReception(),
                RECEPTIONS_COMPARTMENT_SUFFIX, CallQuery.queryAttributeOnSelf(UMLPackage.eINSTANCE.getInterface_OwnedReception()), this.umlPackage.getInterface_OwnedReception(), owners,
                forbiddenOwners, nodeDescription -> nodeDescription != null);
        receptionSubNodeDescription.getInsideLabel().setLabelExpression(CallQuery.queryServiceOnSelf(Services.RENDER_LABEL_ONE_LINE, "true", "true"));

        // Add Reception Graphical dropped tool on Shared Compartment for Interface
        DropNodeTool cpdReceptionGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(parentNodeDescription));
        cpdReceptionGraphicalDropTool.getAcceptedNodeTypes().addAll(List.of(receptionSubNodeDescription));
        parentNodeDescription.getPalette().setDropNodeTool(cpdReceptionGraphicalDropTool);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Abstraction}.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createAbstractionEdgeDescription(DiagramDescription diagramDescription) {
        this.createDependencyOrSubTypeEdgeDescription(diagramDescription, this.umlPackage.getAbstraction(), LineStyle.DASH, ArrowStyle.INPUT_ARROW);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link ComponentRealization}.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createComponentRealizationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> componentTargetCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getComponent());
        Supplier<List<NodeDescription>> classifierSourceCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClassifier());
        EdgeDescription cpdComponentRealizationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getComponentRealization(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getComponentRealization()), classifierSourceCollector, componentTargetCollector);
        EdgeStyle style = cpdComponentRealizationEdgeDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        diagramDescription.getEdgeDescriptions().add(cpdComponentRealizationEdgeDescription);
        EdgeTool cpdComponentRealizationEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cpdComponentRealizationEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(cpdComponentRealizationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(classifierSourceCollector.get(), cpdComponentRealizationEdgeCreationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(cpdComponentRealizationEdgeDescription);
    }

    private void createConnectorEdgeDescription(DiagramDescription diagramDescription) {

        Supplier<List<NodeDescription>> sourceAndTargets = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getPort(), this.umlPackage.getProperty());

        EdgeDescription cpdConnectorEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getConnector(),
                this.getQueryBuilder().queryAllReachable(this.umlPackage.getConnector()), sourceAndTargets, sourceAndTargets);
        cpdConnectorEdgeDescription.setBeginLabelExpression(this.getQueryBuilder().createDomainBaseEdgeSourceLabelExpression());
        cpdConnectorEdgeDescription.setEndLabelExpression(this.getQueryBuilder().createDomainBaseEdgeTargetLabelExpression());
        // Use ConnectorEnd#partWithPort to handle complex Connector edges
        cpdConnectorEdgeDescription.setPreconditionExpression(new CallQuery(Variables.SELF)//
                .callService(ComponentDiagramServices.SHOULD_DISPLAY_CONNECTOR, //
                        Variables.SEMANTIC_EDGE_SOURCE, //
                        Variables.SEMANTIC_EDGE_TARGET, //
                        Variables.GRAPHICAL_EDGE_SOURCE, //
                        Variables.GRAPHICAL_EDGE_TARGET, //
                        Variables.CACHE, //
                        Variables.EDITING_CONTEXT));
        diagramDescription.getEdgeDescriptions().add(cpdConnectorEdgeDescription);
        EdgeTool cpdConnectorEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cpdConnectorEdgeDescription, this.umlPackage.getStructuredClassifier_OwnedConnector());
        this.registerCallback(cpdConnectorEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceAndTargets.get(), cpdConnectorEdgeCreationTool);
        });
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Dependency}.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createDependencyEdgeDescription(DiagramDescription diagramDescription) {
        this.createDependencyOrSubTypeEdgeDescription(diagramDescription, this.umlPackage.getDependency(), LineStyle.DASH, ArrowStyle.INPUT_ARROW);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Generalization}.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createGeneralizationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceAndTargetDescriptionsSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClassifier());

        EClass generalizationEClass = this.umlPackage.getGeneralization();
        EdgeDescription cpdGeneralizationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(generalizationEClass,
                this.getQueryBuilder().queryAllReachableExactType(generalizationEClass), sourceAndTargetDescriptionsSupplier, sourceAndTargetDescriptionsSupplier, false);
        cpdGeneralizationEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        cpdGeneralizationEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        EdgeTool cpdGeneralizationEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cpdGeneralizationEdgeDescription, this.umlPackage.getClassifier_Generalization());
        this.registerCallback(cpdGeneralizationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceAndTargetDescriptionsSupplier.get(), cpdGeneralizationEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(cpdGeneralizationEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(cpdGeneralizationEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link InterfaceRealization}.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createInterfaceRealizationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> interfaceTargetCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getInterface());
        Supplier<List<NodeDescription>> behavioredClassifierSourceCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getBehavioredClassifier());
        EdgeDescription cpdInterfaceRealizationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getInterfaceRealization(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getInterfaceRealization()), behavioredClassifierSourceCollector, interfaceTargetCollector);
        EdgeStyle style = cpdInterfaceRealizationEdgeDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        diagramDescription.getEdgeDescriptions().add(cpdInterfaceRealizationEdgeDescription);
        EdgeTool cpdInterfaceRealizationEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cpdInterfaceRealizationEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(cpdInterfaceRealizationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(behavioredClassifierSourceCollector.get(), cpdInterfaceRealizationEdgeCreationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(cpdInterfaceRealizationEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Manifestation}.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createManifestationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> packageableELementTargetCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getPackageableElement());
        Supplier<List<NodeDescription>> namedElementSourceCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getNamedElement());
        EdgeDescription cpdManifestationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getManifestation(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getManifestation()), namedElementSourceCollector, packageableELementTargetCollector);
        EdgeStyle style = cpdManifestationEdgeDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        diagramDescription.getEdgeDescriptions().add(cpdManifestationEdgeDescription);
        EdgeTool cpdManifestationEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cpdManifestationEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(cpdManifestationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(namedElementSourceCollector.get(), cpdManifestationEdgeCreationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(cpdManifestationEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Substitution}.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createSubstitutionEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> classifierSourceTargetCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClassifier());
        EdgeDescription cpdSubstitutionEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getSubstitution(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getSubstitution()), classifierSourceTargetCollector, classifierSourceTargetCollector);
        EdgeStyle style = cpdSubstitutionEdgeDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        diagramDescription.getEdgeDescriptions().add(cpdSubstitutionEdgeDescription);
        EdgeTool cpdSubstitutionEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cpdSubstitutionEdgeDescription, this.umlPackage.getClassifier_Substitution());
        this.registerCallback(cpdSubstitutionEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(classifierSourceTargetCollector.get(), cpdSubstitutionEdgeCreationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(cpdSubstitutionEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Usage}.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createUsageEdgeDescription(DiagramDescription diagramDescription) {
        this.createDependencyOrSubTypeEdgeDescription(diagramDescription, this.umlPackage.getUsage(), LineStyle.DASH, ArrowStyle.INPUT_ARROW);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Dependency} or subType.
     *
     * @param diagramDescription
     *            the Component {@link DiagramDescription} containing the created {@link EdgeDescription}
     * @param edgeToCreate
     *            kind of edge to create which should be a Dependency or a subType
     * @param lineStyle
     *            the line style of the edge
     * @param arrowStyle
     *            the arrow style of the edge
     */
    private void createDependencyOrSubTypeEdgeDescription(DiagramDescription diagramDescription, EClass edgeToCreate, LineStyle lineStyle, ArrowStyle arrowStyle) {
        Supplier<List<NodeDescription>> namedElementCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getNamedElement());
        EdgeDescription cpdDependencyEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(edgeToCreate,
                this.getQueryBuilder().queryAllReachableExactType(edgeToCreate), namedElementCollector, namedElementCollector);
        EdgeStyle style = cpdDependencyEdgeDescription.getStyle();
        style.setLineStyle(lineStyle);
        style.setTargetArrowStyle(arrowStyle);
        diagramDescription.getEdgeDescriptions().add(cpdDependencyEdgeDescription);
        EdgeTool cpdDependencyEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cpdDependencyEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(cpdDependencyEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(namedElementCollector.get(), cpdDependencyEdgeCreationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(cpdDependencyEdgeDescription);
    }

}

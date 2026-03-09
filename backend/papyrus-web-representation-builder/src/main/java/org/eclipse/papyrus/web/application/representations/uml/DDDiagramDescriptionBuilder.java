/*****************************************************************************
 * Copyright (c) 2024, 2026 CEA LIST, Obeo, Artal Technologies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 199, 229
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - titouan.bouete-giraud@artal.fr - Issues 219, 227
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
import org.eclipse.papyrus.web.application.representations.view.builders.CallbackAdapter;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.uml2.uml.Artifact;
import org.eclipse.uml2.uml.CommunicationPath;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Deployment;
import org.eclipse.uml2.uml.DeploymentSpecification;
import org.eclipse.uml2.uml.Device;
import org.eclipse.uml2.uml.ExecutionEnvironment;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Manifestation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.springframework.stereotype.Service;

/**
 * Builder of the "Deployment" diagram representation.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public final class DDDiagramDescriptionBuilder extends AbstractRepresentationDescriptionBuilder {

    /**
     * The name of the representation handled by this builder.
     */
    public static final String DD_REP_NAME = "Deployment Diagram";

    /**
     * The prefix of the representation handled by this builder.
     */
    public static final String DD_PREFIX = "DD_";

    /**
     * The suffix used to identify <i>receptions</i> compartments.
     */
    public static final String SYMBOLS_COMPARTMENT_SUFFIX = "Symbols";

    /**
     * The default width and height of 3D boxes for {@link Node}s and {@link Device}s.
     */
    private static final String SIZE_100 = "100";

    private final UMLPackage umlPackage = UMLPackage.eINSTANCE;

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription ddSharedDescription;

    private NodeDescription symbolNodeDescription;

    public DDDiagramDescriptionBuilder() {
        super(DD_PREFIX, DD_REP_NAME, UMLPackage.eINSTANCE.getPackage());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {

        this.ddSharedDescription = this.createSharedDescription(diagramDescription);
        List<EClass> symbolOwners = List.of(
                this.umlPackage.getArtifact(),
                this.umlPackage.getDeploymentSpecification(),
                this.umlPackage.getExecutionEnvironment(),
                this.umlPackage.getPackage(),
                this.umlPackage.getNode(),
                this.umlPackage.getDevice());
        this.symbolNodeDescription = this.createSymbolSharedNodeDescription(diagramDescription, symbolOwners, List.of(), SYMBOLS_COMPARTMENT_SUFFIX);

        // create diagram tool sections
        this.createDefaultToolSectionInDiagramDescription(diagramDescription);
        diagramDescription.setPreconditionExpression(CallQuery.queryServiceOnSelf(Services.IS_NOT_PROFILE_MODEL));

        DiagramToolSection showHideToolSection = this.getViewBuilder().createDiagramToolSection(SHOW_HIDE);
        diagramDescription.getPalette().getToolSections().add(showHideToolSection);
        this.createHideSymbolTool(diagramDescription,
                SHOW_HIDE);
        this.createShowSymbolTool(diagramDescription, SHOW_HIDE);
        this.createHideAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);
        this.createShowAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);

        // create node descriptions with their tools
        this.createArtifactTopNodeDescription(diagramDescription);
        this.createCommentTopNodeDescription(diagramDescription, NODES);
        this.createConstraintTopNodeDescription(diagramDescription, NODES);
        this.createDeploymentSpecificationTopNodeDescription(diagramDescription);
        this.createDeviceTopNodeDescription(diagramDescription);
        this.createExecutionEnvironmentTopNodeDescription(diagramDescription);
        this.createModelTopNodeDescription(diagramDescription);
        this.createNodeTopNodeDescription(diagramDescription);
        this.createPackageTopNodeDescription(diagramDescription);

        // create shared node descriptions with their tools
        this.createArtifactSharedNodeDescription(diagramDescription);
        this.createCommentSubNodeDescription(diagramDescription, this.ddSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getComment(), SHARED_SUFFIX), List.of(this.umlPackage.getPackage()));
        this.createConstraintSubNodeDescription(diagramDescription, this.ddSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getConstraint(), SHARED_SUFFIX), List.of(this.umlPackage.getPackage()));
        this.createDeploymentSpecificationSharedNodeDescription(diagramDescription);
        this.createDeviceSharedNodeDescription(diagramDescription);
        this.createExecutionEnvironmentSharedNodeDescription(diagramDescription);
        this.createModelSharedNodeDescription(diagramDescription);
        this.createNodeSharedNodeDescription(diagramDescription);
        this.createPackageSharedNodeDescription(diagramDescription);

        // edge descriptions
        this.createCommunicationPathEdgeDescription(diagramDescription);
        this.createDependencyEdgeDescription(diagramDescription);
        this.createDeploymentEdgeDescription(diagramDescription);
        this.createGeneralizationEdgeDescription(diagramDescription);
        this.createManifestationEdgeDescription(diagramDescription);

        this.ddSharedDescription.getChildrenDescriptions().add(this.symbolNodeDescription);
        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));

        // Add dropped tool on diagram
        DropNodeTool ddGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getDiagramGraphicalDropToolName());
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(diagramDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        diagramDescription.getPalette().setDropNodeTool(ddGraphicalDropTool);

        diagramDescription.setToolbar(this.getViewBuilder().createDefaultDiagramToolbar());
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Artifact} on the Diagram.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createArtifactTopNodeDescription(DiagramDescription diagramDescription) {
        EClass artifactEClass = this.umlPackage.getArtifact();
        NodeDescription ddArtifactHolderTopNodeDescription = this.newNodeBuilder(artifactEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(artifactEClass, HOLDER_SUFFIX)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(artifactEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(artifactEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(artifactEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();

        NodeDescription ddArtifactContentTopNodeDescription = this.createContentNodeDescription(artifactEClass, false);
        this.addContent(artifactEClass, false, ddArtifactHolderTopNodeDescription, ddArtifactContentTopNodeDescription, this.symbolNodeDescription);
        this.copyDimension(ddArtifactHolderTopNodeDescription, ddArtifactContentTopNodeDescription);
        diagramDescription.getNodeDescriptions().add(ddArtifactHolderTopNodeDescription);

        // create tool
        NodeTool ddArtifactTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), artifactEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddArtifactTopNodeCreationTool, NODES);

        // Add dropped tool on Diagram Artifact container
        DropNodeTool ddArtifactGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddArtifactContentTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact());
        this.registerCallback(ddArtifactContentTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddArtifactGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddArtifactContentTopNodeDescription.getPalette().setDropNodeTool(ddArtifactGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link DeploymentSpecification} on the Diagram.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createDeploymentSpecificationTopNodeDescription(DiagramDescription diagramDescription) {
        EClass deploymentSpecificationEClass = this.umlPackage.getDeploymentSpecification();
        NodeDescription ddDeploymentSpecificationTopNodeDescription = this.newNodeBuilder(deploymentSpecificationEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getDomainNodeName(deploymentSpecificationEClass)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(deploymentSpecificationEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(this.createListLayoutStrategy())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(deploymentSpecificationEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(deploymentSpecificationEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        diagramDescription.getNodeDescriptions().add(ddDeploymentSpecificationTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ddDeploymentSpecificationTopNodeDescription);

        // create tool
        NodeTool ddDeploymentSpecificationTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), deploymentSpecificationEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddDeploymentSpecificationTopNodeCreationTool, NODES);

        // No graphical Drag&Drop tool on DeploymentSpecification node
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Device} on the Diagram.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createDeviceTopNodeDescription(DiagramDescription diagramDescription) {
        EClass deviceEClass = this.umlPackage.getDevice();
        NodeDescription ddDeviceHolderTopNodeDescription = this.newNodeBuilder(deviceEClass, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(deviceEClass, HOLDER_SUFFIX)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(deviceEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(deviceEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(deviceEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon())
                .build();
        ddDeviceHolderTopNodeDescription.setDefaultWidthExpression(SIZE_100);
        ddDeviceHolderTopNodeDescription.setDefaultHeightExpression(SIZE_100);

        NodeDescription ddDeviceContentTopNodeDescription = this.createContentNodeDescription(deviceEClass, false);
        this.addContent(deviceEClass, false, ddDeviceHolderTopNodeDescription, ddDeviceContentTopNodeDescription, this.symbolNodeDescription);
        this.copyDimension(ddDeviceHolderTopNodeDescription, ddDeviceContentTopNodeDescription);

        diagramDescription.getNodeDescriptions().add(ddDeviceHolderTopNodeDescription);
        ddDeviceHolderTopNodeDescription.getChildrenDescriptions().add(ddDeviceContentTopNodeDescription);

        // create tools
        NodeTool ddDeviceTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), deviceEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddDeviceTopNodeCreationTool, NODES);

        // Add dropped tool on Diagram Device container
        DropNodeTool ddDeviceGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddDeviceHolderTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getDeploymentSpecification(), this.umlPackage.getNode());
        this.registerCallback(ddDeviceContentTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddDeviceGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddDeviceContentTopNodeDescription.getPalette().setDropNodeTool(ddDeviceGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link ExecutionEnvironment} on the Diagram.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createExecutionEnvironmentTopNodeDescription(DiagramDescription diagramDescription) {
        EClass executionEnvironmentEClass = this.umlPackage.getExecutionEnvironment();
        NodeDescription ddExecutionEnvironmentEClassHolderTopNodeDescription = this.newNodeBuilder(executionEnvironmentEClass, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(executionEnvironmentEClass, HOLDER_SUFFIX)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(executionEnvironmentEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(executionEnvironmentEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(executionEnvironmentEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon())
                .build();
        ddExecutionEnvironmentEClassHolderTopNodeDescription.setDefaultWidthExpression(SIZE_100);
        ddExecutionEnvironmentEClassHolderTopNodeDescription.setDefaultHeightExpression(SIZE_100);

        NodeDescription ddExecutionEnvironmentContentTopNodeDescription = this.createContentNodeDescription(executionEnvironmentEClass, false);
        this.addContent(executionEnvironmentEClass, false, ddExecutionEnvironmentEClassHolderTopNodeDescription, ddExecutionEnvironmentContentTopNodeDescription, this.symbolNodeDescription);
        this.copyDimension(ddExecutionEnvironmentEClassHolderTopNodeDescription, ddExecutionEnvironmentContentTopNodeDescription);
        diagramDescription.getNodeDescriptions().add(ddExecutionEnvironmentEClassHolderTopNodeDescription);

        // create tools
        NodeTool ddExecutionEnvironmentTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), executionEnvironmentEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddExecutionEnvironmentTopNodeCreationTool, NODES);

        // Add dropped tool on Diagram Device container
        DropNodeTool ddExecutionEnvironmentGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddExecutionEnvironmentEClassHolderTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getExecutionEnvironment());
        this.registerCallback(ddExecutionEnvironmentContentTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddExecutionEnvironmentGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddExecutionEnvironmentContentTopNodeDescription.getPalette().setDropNodeTool(ddExecutionEnvironmentGraphicalDropTool);

    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Model} on the Diagram.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createModelTopNodeDescription(DiagramDescription diagramDescription) {
        EClass modelEClass = this.umlPackage.getModel();
        NodeDescription ddModelHolderTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(modelEClass,
                this.getQueryBuilder().queryAllReachableExactType(modelEClass));
        ddModelHolderTopNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(modelEClass, HOLDER_SUFFIX));
        ddModelHolderTopNodeDescription.setInsideLabel(this.getViewBuilder().createDefaultInsideLabelDescription(true, true));
        ddModelHolderTopNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        NodeDescription ddModelContentTopNodeDescription = this.createContentNodeDescription(modelEClass, false);
        this.addContent(modelEClass, false, ddModelHolderTopNodeDescription, ddModelContentTopNodeDescription, this.symbolNodeDescription);
        this.copyDimension(ddModelHolderTopNodeDescription, ddModelContentTopNodeDescription);
        diagramDescription.getNodeDescriptions().add(ddModelHolderTopNodeDescription);

        NodeTool ddModelTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), modelEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddModelTopNodeCreationTool, NODES);

        // Add dropped tool on Model container
        DropNodeTool ddModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddModelHolderTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(ddModelContentTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddModelContentTopNodeDescription.getPalette().setDropNodeTool(ddModelGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Node} on the Diagram.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createNodeTopNodeDescription(DiagramDescription diagramDescription) {
        EClass nodeEClass = this.umlPackage.getNode();
        NodeDescription ddNodeHolderTopNodeDescription = this.newNodeBuilder(nodeEClass, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(nodeEClass, HOLDER_SUFFIX)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(nodeEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(nodeEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(nodeEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon())
                .build();
        ddNodeHolderTopNodeDescription.setDefaultWidthExpression(SIZE_100);
        ddNodeHolderTopNodeDescription.setDefaultHeightExpression(SIZE_100);

        NodeDescription ddNodeContentTopNodeDescription = this.createContentNodeDescription(nodeEClass, false);
        this.copyDimension(ddNodeHolderTopNodeDescription, ddNodeContentTopNodeDescription);
        this.addContent(nodeEClass, false, ddNodeHolderTopNodeDescription, ddNodeContentTopNodeDescription, this.symbolNodeDescription);

        diagramDescription.getNodeDescriptions().add(ddNodeHolderTopNodeDescription);
        ddNodeHolderTopNodeDescription.getChildrenDescriptions().add(ddNodeContentTopNodeDescription);

        // create tools
        NodeTool ddNodeTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), nodeEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddNodeTopNodeCreationTool, NODES);

        // Add dropped tool on Diagram Device container
        DropNodeTool ddNodeGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddNodeHolderTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getNode());
        this.registerCallback(ddNodeContentTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddNodeContentTopNodeDescription.getPalette().setDropNodeTool(ddNodeGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Package} on the Diagram.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageTopNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.umlPackage.getPackage();
        NodeDescription ddPackageHolderTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                this.getQueryBuilder().queryAllReachableExactType(packageEClass));
        ddPackageHolderTopNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(packageEClass, HOLDER_SUFFIX));
        ddPackageHolderTopNodeDescription.setInsideLabel(this.getViewBuilder().createDefaultInsideLabelDescription(true, true));
        ddPackageHolderTopNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        NodeDescription ddPackageContentTopNodeDescription = this.createContentNodeDescription(packageEClass, false);
        this.copyDimension(ddPackageHolderTopNodeDescription, ddPackageContentTopNodeDescription);
        this.addContent(packageEClass, false, ddPackageHolderTopNodeDescription, ddPackageContentTopNodeDescription, this.symbolNodeDescription);

        diagramDescription.getNodeDescriptions().add(ddPackageHolderTopNodeDescription);

        NodeTool ddPackageTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddPackageTopNodeCreationTool, NODES);

        // Add dropped tool on Package container
        DropNodeTool ddPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddPackageHolderTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(ddPackageContentTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddPackageContentTopNodeDescription.getPalette().setDropNodeTool(ddPackageGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Artifact}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createArtifactSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass artifactEClass = this.umlPackage.getArtifact();
        NodeDescription ddArtifactHolderSharedNodeDescription = this.newNodeBuilder(artifactEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_ARTIFACT_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(artifactEClass.getName()))//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(artifactEClass.getName())) //
                .build();

        NodeDescription ddArtifactContentSharedNodeDescription = this.createContentNodeDescription(artifactEClass, true);
        this.copyDimension(ddArtifactHolderSharedNodeDescription, ddArtifactContentSharedNodeDescription);
        this.addContent(artifactEClass, true, ddArtifactHolderSharedNodeDescription, ddArtifactContentSharedNodeDescription, this.symbolNodeDescription);
        this.ddSharedDescription.getChildrenDescriptions().add(ddArtifactHolderSharedNodeDescription);

        // create tools
        NodeTool ddArtifactSharedNodeCreationTool = this.getViewBuilder().createCreationTool(artifactEClass.getName(), this.getIdBuilder().getCreationToolId(artifactEClass),
                DeploymentDiagramServices.CREATE_ARTIFACT,
                List.of(this.getQueryBuilder().aqlString(artifactEClass.getName()), SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        List<EClass> owners = List.of(this.umlPackage.getArtifact(), //
                this.umlPackage.getNode(), //
                this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddArtifactHolderSharedNodeDescription, diagramDescription, ddArtifactSharedNodeCreationTool, NODES, owners,
                List.of(this.umlPackage.getDevice(), this.umlPackage.getDeploymentSpecification()));

        // Add dropped tool on Shared Artifact container
        DropNodeTool ddArtifactSharedGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ddArtifactContentSharedNodeDescription, SHARED_SUFFIX));
        List<EClass> children = List.of(this.umlPackage.getArtifact());
        this.registerCallback(ddArtifactContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddArtifactSharedGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddArtifactContentSharedNodeDescription.getPalette().setDropNodeTool(ddArtifactSharedGraphicalDropTool);

    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link DeploymentSpecification}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createDeploymentSpecificationSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass deploymentSepecificationEClass = this.umlPackage.getDeploymentSpecification();
        NodeDescription ddDeploymentSpecificationSharedNodeDescription = this.newNodeBuilder(deploymentSepecificationEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(deploymentSepecificationEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_DEPLOYMENT_SPECIFICATION_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(this.createListLayoutStrategy())//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(deploymentSepecificationEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(deploymentSepecificationEClass.getName())) //
                .build();
        this.ddSharedDescription.getChildrenDescriptions().add(ddDeploymentSpecificationSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ddDeploymentSpecificationSharedNodeDescription);

        // create tools
        NodeTool ddDeploymentSpecificationSharedNodeCreationTool = this.getViewBuilder().createCreationTool(deploymentSepecificationEClass.getName(),
                this.getIdBuilder().getCreationToolId(deploymentSepecificationEClass),
                DeploymentDiagramServices.CREATE_ARTIFACT,
                List.of(this.getQueryBuilder().aqlString(deploymentSepecificationEClass.getName()), SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        List<EClass> owners = List.of(this.umlPackage.getArtifact(), //
                this.umlPackage.getNode(), //
                this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddDeploymentSpecificationSharedNodeDescription, diagramDescription, ddDeploymentSpecificationSharedNodeCreationTool, NODES, owners,
                List.of(this.umlPackage.getDeploymentSpecification()));

        // No Graphical Drag&Drop tool
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Device}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createDeviceSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass deviceEClass = this.umlPackage.getDevice();
        NodeDescription ddDeviceHolderSharedNodeDescription = this.newNodeBuilder(deviceEClass, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(deviceEClass, SHARED_SUFFIX + UNDERSCORE + HOLDER_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_DEVICE_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(deviceEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(deviceEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon()) //
                .build();
        ddDeviceHolderSharedNodeDescription.setDefaultWidthExpression(SIZE_100);
        ddDeviceHolderSharedNodeDescription.setDefaultHeightExpression(SIZE_100);

        NodeDescription ddDeviceContentSharedNodeDescription = this.createContentNodeDescription(deviceEClass, true);
        this.copyDimension(ddDeviceHolderSharedNodeDescription, ddDeviceContentSharedNodeDescription);
        this.addContent(deviceEClass, true, ddDeviceHolderSharedNodeDescription, ddDeviceContentSharedNodeDescription, this.symbolNodeDescription);
        this.ddSharedDescription.getChildrenDescriptions().add(ddDeviceHolderSharedNodeDescription);
        ddDeviceHolderSharedNodeDescription.getChildrenDescriptions().add(ddDeviceContentSharedNodeDescription);

        // create tools
        NodeTool ddDeviceSharedNodeCreationTool = this.getViewBuilder().createCreationTool(deviceEClass.getName(), this.getIdBuilder().getCreationToolId(deviceEClass),
                DeploymentDiagramServices.CREATE_NODE,
                List.of(this.getQueryBuilder().aqlString(deviceEClass.getName()), SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        List<EClass> owners = List.of(this.umlPackage.getNode(), //
                this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddDeviceHolderSharedNodeDescription, diagramDescription, ddDeviceSharedNodeCreationTool, NODES, owners,
                List.of(this.umlPackage.getExecutionEnvironment()));

        // Add dropped tool on Shared Device container
        DropNodeTool ddDeviceSharedGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ddDeviceHolderSharedNodeDescription, SHARED_SUFFIX));
        List<EClass> children = List.of(this.umlPackage.getDeploymentSpecification(), this.umlPackage.getNode());
        this.registerCallback(ddDeviceContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddDeviceSharedGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddDeviceContentSharedNodeDescription.getPalette().setDropNodeTool(ddDeviceSharedGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link ExecutionEnvironment}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createExecutionEnvironmentSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass executionEnvironmentEClass = this.umlPackage.getExecutionEnvironment();
        NodeDescription ddExecutionEnvironmentHolderSharedNodeDescription = this.newNodeBuilder(executionEnvironmentEClass, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(executionEnvironmentEClass, SHARED_SUFFIX + UNDERSCORE + HOLDER_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_EXECUTION_ENVIRONMENT_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(executionEnvironmentEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(executionEnvironmentEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon()) //
                .build();
        ddExecutionEnvironmentHolderSharedNodeDescription.setDefaultWidthExpression(SIZE_100);
        ddExecutionEnvironmentHolderSharedNodeDescription.setDefaultHeightExpression(SIZE_100);

        NodeDescription ddExecutionEnvironmentContentSharedNodeDescription = this.createContentNodeDescription(executionEnvironmentEClass, true);
        this.copyDimension(ddExecutionEnvironmentHolderSharedNodeDescription, ddExecutionEnvironmentContentSharedNodeDescription);
        this.addContent(executionEnvironmentEClass, true, ddExecutionEnvironmentHolderSharedNodeDescription, ddExecutionEnvironmentContentSharedNodeDescription, this.symbolNodeDescription);
        this.ddSharedDescription.getChildrenDescriptions().add(ddExecutionEnvironmentHolderSharedNodeDescription);

        // create tools
        NodeTool ddExecutionEnvironmentSharedNodeCreationTool = this.getViewBuilder().createCreationTool(executionEnvironmentEClass.getName(),
                this.getIdBuilder().getCreationToolId(executionEnvironmentEClass),
                DeploymentDiagramServices.CREATE_NODE,
                List.of(this.getQueryBuilder().aqlString(executionEnvironmentEClass.getName()), SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        List<EClass> owners = List.of(this.umlPackage.getNode(), //
                this.umlPackage.getPackage(), this.umlPackage.getExecutionEnvironment());
        this.reuseNodeAndCreateTool(ddExecutionEnvironmentHolderSharedNodeDescription, diagramDescription, ddExecutionEnvironmentSharedNodeCreationTool, NODES, owners,
                List.of());

        // Add dropped tool on Shared Device container
        DropNodeTool ddExecutionEnvironmentSharedNodeGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ddExecutionEnvironmentHolderSharedNodeDescription, SHARED_SUFFIX));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getExecutionEnvironment());
        this.registerCallback(ddExecutionEnvironmentContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddExecutionEnvironmentSharedNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddExecutionEnvironmentContentSharedNodeDescription.getPalette().setDropNodeTool(ddExecutionEnvironmentSharedNodeGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Node}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass nodeEClass = this.umlPackage.getNode();
        NodeDescription ddNodeHolderSharedNodeDescription = this.newNodeBuilder(nodeEClass, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(nodeEClass, SHARED_SUFFIX + UNDERSCORE + HOLDER_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_NODE_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(nodeEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(nodeEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon()) //
                .build();
        ddNodeHolderSharedNodeDescription.setDefaultWidthExpression(SIZE_100);
        ddNodeHolderSharedNodeDescription.setDefaultHeightExpression(SIZE_100);

        NodeDescription ddNodeContentSharedNodeDescription = this.createContentNodeDescription(nodeEClass, true);
        this.copyDimension(ddNodeHolderSharedNodeDescription, ddNodeContentSharedNodeDescription);
        this.addContent(nodeEClass, true, ddNodeHolderSharedNodeDescription, ddNodeContentSharedNodeDescription, this.symbolNodeDescription);
        this.ddSharedDescription.getChildrenDescriptions().add(ddNodeHolderSharedNodeDescription);
        ddNodeHolderSharedNodeDescription.getChildrenDescriptions().add(ddNodeContentSharedNodeDescription);

        // create tools
        NodeTool ddNodeSharedNodeCreationTool = this.getViewBuilder().createCreationTool(nodeEClass.getName(), this.getIdBuilder().getCreationToolId(nodeEClass),
                DeploymentDiagramServices.CREATE_NODE,
                List.of(this.getQueryBuilder().aqlString(nodeEClass.getName()), SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        List<EClass> owners = List.of(this.umlPackage.getNode(), //
                this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddNodeHolderSharedNodeDescription, diagramDescription, ddNodeSharedNodeCreationTool, NODES, owners,
                List.of(this.umlPackage.getExecutionEnvironment()));

        // Add dropped tool on Shared Device container
        DropNodeTool ddNodeSharedGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ddNodeHolderSharedNodeDescription, SHARED_SUFFIX));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getNode());
        this.registerCallback(ddNodeContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddNodeSharedGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddNodeContentSharedNodeDescription.getPalette().setDropNodeTool(ddNodeSharedGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Package}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.umlPackage.getPackage();
        NodeDescription ddPackageHolderSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()));
        ddPackageHolderSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(packageEClass, SHARED_SUFFIX + UNDERSCORE + HOLDER_SUFFIX));
        ddPackageHolderSharedNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());
        ddPackageHolderSharedNodeDescription.setInsideLabel(this.getViewBuilder().createDefaultInsideLabelDescription(true, true));

        NodeDescription ddPackageContentSharedNodeDescription = this.createContentNodeDescription(packageEClass, true);
        this.copyDimension(ddPackageHolderSharedNodeDescription, ddPackageContentSharedNodeDescription);
        this.addContent(packageEClass, true, ddPackageHolderSharedNodeDescription, ddPackageContentSharedNodeDescription, this.symbolNodeDescription);
        this.ddSharedDescription.getChildrenDescriptions().add(ddPackageHolderSharedNodeDescription);
        ddPackageHolderSharedNodeDescription.getChildrenDescriptions().add(ddPackageContentSharedNodeDescription);

        NodeTool ddPackageSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddPackageHolderSharedNodeDescription, diagramDescription,
                ddPackageSharedNodeCreationTool, NODES, owners, List.of());

        // Add dropped tool on Shared Package container
        DropNodeTool ddPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddPackageContentSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(ddPackageContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddPackageContentSharedNodeDescription.getPalette().setDropNodeTool(ddPackageGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Model}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createModelSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass modelEClass = this.umlPackage.getModel();
        NodeDescription ddModelHolderSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(modelEClass,
                CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()));
        ddModelHolderSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(modelEClass, SHARED_SUFFIX + UNDERSCORE + HOLDER_SUFFIX));
        ddModelHolderSharedNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());
        ddModelHolderSharedNodeDescription.setInsideLabel(this.getViewBuilder().createDefaultInsideLabelDescription(true, true));

        NodeDescription ddModelContentSharedNodeDescription = this.createContentNodeDescription(modelEClass, true);
        this.copyDimension(ddModelHolderSharedNodeDescription, ddModelContentSharedNodeDescription);
        this.addContent(modelEClass, true, ddModelHolderSharedNodeDescription, ddModelContentSharedNodeDescription, this.symbolNodeDescription);
        this.ddSharedDescription.getChildrenDescriptions().add(ddModelHolderSharedNodeDescription);

        NodeTool ddModelSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), modelEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddModelHolderSharedNodeDescription, diagramDescription,
                ddModelSharedNodeCreationTool, NODES, owners, List.of());

        // Add dropped tool on Shared Package container
        DropNodeTool ddModelSharedGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddModelHolderSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(ddModelContentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilterWithoutContent(diagramDescription, children, List.of());
            ddModelSharedGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddModelContentSharedNodeDescription.getPalette().setDropNodeTool(ddModelSharedGraphicalDropTool);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link CommunicationPath}.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createCommunicationPathEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> nodeSourcesAndTargets = () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getNode());
        EClass communicationPathEClass = this.umlPackage.getCommunicationPath();
        EdgeDescription ddCommunicationPathEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(communicationPathEClass,
                this.getQueryBuilder().queryAllReachableExactType(communicationPathEClass), nodeSourcesAndTargets, nodeSourcesAndTargets);

        ddCommunicationPathEdgeDescription.setBeginLabelExpression(this.getQueryBuilder().createDomainBaseEdgeSourceLabelExpression());
        ddCommunicationPathEdgeDescription.setEndLabelExpression(this.getQueryBuilder().createDomainBaseEdgeTargetLabelExpression());

        ddCommunicationPathEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        ddCommunicationPathEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.NONE);
        EdgeTool ddCommunicationPathEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ddCommunicationPathEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(ddCommunicationPathEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(nodeSourcesAndTargets.get(), ddCommunicationPathEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(ddCommunicationPathEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(ddCommunicationPathEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Dependency}.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createDependencyEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> namedElementCollector = () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getNamedElement());
        EdgeDescription ddDependencyEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.umlPackage.getDependency(),
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getDependency()),
                namedElementCollector, namedElementCollector);
        EdgeStyle style = ddDependencyEdgeDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        diagramDescription.getEdgeDescriptions().add(ddDependencyEdgeDescription);
        EdgeTool ddDependencyEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ddDependencyEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(ddDependencyEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(namedElementCollector.get(), ddDependencyEdgeCreationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(ddDependencyEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Deployment}.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createDeploymentEdgeDescription(DiagramDescription diagramDescription) {

        Supplier<List<NodeDescription>> sourceDeployedArtifact = () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getDeployedArtifact());
        Supplier<List<NodeDescription>> targetDeploymentTarget = () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getDeploymentTarget());

        EClass deploymentEClass = this.umlPackage.getDeployment();
        EdgeDescription ddDeploymentEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(deploymentEClass,
                this.getQueryBuilder().queryAllReachableExactType(deploymentEClass),
                sourceDeployedArtifact, targetDeploymentTarget);
        ddDeploymentEdgeDescription.getStyle().setLineStyle(LineStyle.DASH);
        ddDeploymentEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        EdgeTool dddDeploymentEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ddDeploymentEdgeDescription, this.umlPackage.getDeploymentTarget_Deployment());
        this.registerCallback(ddDeploymentEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceDeployedArtifact.get(), dddDeploymentEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(ddDeploymentEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(ddDeploymentEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Generalization}.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createGeneralizationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceAndTargetDescriptionsSupplier = () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getClassifier());

        EClass generalizationEClass = this.umlPackage.getGeneralization();
        EdgeDescription ddGeneralizationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(generalizationEClass,
                this.getQueryBuilder().queryAllReachableExactType(generalizationEClass), sourceAndTargetDescriptionsSupplier, sourceAndTargetDescriptionsSupplier, false);
        ddGeneralizationEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        ddGeneralizationEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        EdgeTool ddGeneralizationEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(ddGeneralizationEdgeDescription, this.umlPackage.getClassifier_Generalization());
        this.registerCallback(ddGeneralizationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceAndTargetDescriptionsSupplier.get(), ddGeneralizationEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(ddGeneralizationEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(ddGeneralizationEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Manifestation}.
     *
     * @param diagramDescription
     *            the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createManifestationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> packageableELementTargetCollector = () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getPackageableElement());
        Supplier<List<NodeDescription>> namedElementSourceCollector = () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.umlPackage.getNamedElement());

        EClass manifestationEClass = this.umlPackage.getManifestation();
        EdgeDescription ddManifestationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(manifestationEClass,
                this.getQueryBuilder().queryAllReachableExactType(manifestationEClass), namedElementSourceCollector, packageableELementTargetCollector, true);
        EdgeStyle style = ddManifestationEdgeDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        diagramDescription.getEdgeDescriptions().add(ddManifestationEdgeDescription);

        EdgeTool ddManifestationEdgeCreationTool = this.getViewBuilder().createDomainBasedEdgeToolWithService("New Manifestation", DeploymentDiagramServices.CREATE_MANIFESTATION);

        ddManifestationEdgeDescription.eAdapters().add(new CallbackAdapter(() -> {
            List<DiagramElementDescription> targetNodeDescriptions = ddManifestationEdgeDescription.getTargetDescriptions();
            ddManifestationEdgeCreationTool.getTargetElementDescriptions().addAll(targetNodeDescriptions);
        }));

        this.registerCallback(ddManifestationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(namedElementSourceCollector.get(), ddManifestationEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(ddManifestationEdgeDescription);

        this.getViewBuilder().addDefaultReconnectionTools(ddManifestationEdgeDescription);
    }

}

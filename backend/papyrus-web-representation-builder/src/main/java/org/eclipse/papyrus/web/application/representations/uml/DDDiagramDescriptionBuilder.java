/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo.
 * Copyright (c) 2024, 2025 CEA LIST, Obeo.
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
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.uml2.uml.Artifact;
import org.eclipse.uml2.uml.Classifier;
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

/**
 * Builder of the "Deployment" diagram representation.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
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
     * The default width and height of 3D boxes for {@link Node}s and {@link Device}s.
     */
    private static final String SIZE_100 = "100";

    /**
     * The default width of 3D boxes for {@link ExecutionEnvironment}.
     * <p>
     * Note that 3D boxes for {@link ExecutionEnvironment} have a height of {@link #SIZE_100}.
     * </p>
     */
    private static final String SIZE_200 = "200";

    private final UMLPackage umlPackage = UMLPackage.eINSTANCE;

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription ddSharedDescription;

    public DDDiagramDescriptionBuilder() {
        super(DD_PREFIX, DD_REP_NAME, UMLPackage.eINSTANCE.getPackage());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {

        // create diagram tool sections
        this.createDefaultToolSectionInDiagramDescription(diagramDescription);
        diagramDescription.setPreconditionExpression(CallQuery.queryServiceOnSelf(Services.IS_NOT_PROFILE_MODEL));

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
        this.ddSharedDescription = this.createSharedDescription(diagramDescription);
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

        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));

        // Add dropped tool on diagram
        DropNodeTool ddGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getDiagramGraphicalDropToolName());
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(diagramDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        diagramDescription.getPalette().setDropNodeTool(ddGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Artifact} on the Diagram.
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createArtifactTopNodeDescription(DiagramDescription diagramDescription) {
        EClass artifactEClass = this.umlPackage.getArtifact();
        NodeDescription ddArtifactTopNodeDescription = this.newNodeBuilder(artifactEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getDomainNodeName(artifactEClass)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(artifactEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(artifactEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(artifactEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        diagramDescription.getNodeDescriptions().add(ddArtifactTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ddArtifactTopNodeDescription);

        // create tool
        NodeTool ddArtifactTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), artifactEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddArtifactTopNodeCreationTool, NODES);

        // Add dropped tool on Diagram Artifact container
        DropNodeTool ddArtifactGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddArtifactTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact());
        this.registerCallback(ddArtifactTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddArtifactGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddArtifactTopNodeDescription.getPalette().setDropNodeTool(ddArtifactGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link DeploymentSpecification} on the Diagram.
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createDeploymentSpecificationTopNodeDescription(DiagramDescription diagramDescription) {
        EClass deploymentSpecificationEClass = this.umlPackage.getDeploymentSpecification();
        NodeDescription ddDeploymentSpecificationTopNodeDescription = this.newNodeBuilder(deploymentSpecificationEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getDomainNodeName(deploymentSpecificationEClass)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(deploymentSpecificationEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(deploymentSpecificationEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(deploymentSpecificationEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, false))
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
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createDeviceTopNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription ddDeviceTopNodeDescription = this.createClassifierTopNodeDescription(diagramDescription, this.umlPackage.getDevice());
        // Add dropped tool on Diagram Device container
        DropNodeTool ddDeviceGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddDeviceTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getDeploymentSpecification(), this.umlPackage.getNode());
        this.registerCallback(ddDeviceTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddDeviceGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddDeviceTopNodeDescription.getPalette().setDropNodeTool(ddDeviceGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link ExecutionEnvironment} on the Diagram.
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createExecutionEnvironmentTopNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription ddExecutionEnvironmentTopNodeDescription = this.createClassifierTopNodeDescription(diagramDescription, this.umlPackage.getExecutionEnvironment());
        ddExecutionEnvironmentTopNodeDescription.setDefaultWidthExpression(SIZE_200);
        // Add dropped tool on Diagram ExecutionEnvironment container
        DropNodeTool ddExecutionEnvironmentGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddExecutionEnvironmentTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getExecutionEnvironment());
        this.registerCallback(ddExecutionEnvironmentTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddExecutionEnvironmentGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddExecutionEnvironmentTopNodeDescription.getPalette().setDropNodeTool(ddExecutionEnvironmentGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Model} on the Diagram.
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createModelTopNodeDescription(DiagramDescription diagramDescription) {
        EClass modelEClass = this.umlPackage.getModel();
        NodeDescription ddModelTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(modelEClass, this.getQueryBuilder().queryAllReachableExactType(modelEClass));
        diagramDescription.getNodeDescriptions().add(ddModelTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ddModelTopNodeDescription);

        NodeTool ddModelTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), modelEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddModelTopNodeCreationTool, NODES);

        // Add dropped tool on Diagram Model container
        DropNodeTool ddModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddModelTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(ddModelTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddModelTopNodeDescription.getPalette().setDropNodeTool(ddModelGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Node} on the Diagram.
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createNodeTopNodeDescription(DiagramDescription diagramDescription) {
        NodeDescription ddNodeTopNodeDescription = this.createClassifierTopNodeDescription(diagramDescription, this.umlPackage.getNode());
        // Add dropped tool on Diagram Node container
        DropNodeTool ddNodeGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddNodeTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getNode());
        this.registerCallback(ddNodeTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddNodeTopNodeDescription.getPalette().setDropNodeTool(ddNodeGraphicalDropTool);

    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Package} on the Diagram.
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageTopNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.umlPackage.getPackage();
        NodeDescription ddPackageTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getPackage()));
        diagramDescription.getNodeDescriptions().add(ddPackageTopNodeDescription);

        ddPackageTopNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        // create Package tool sections
        this.createDefaultToolSectionsInNodeDescription(ddPackageTopNodeDescription);

        NodeTool ddPackageTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        this.addDiagramToolInToolSection(diagramDescription, ddPackageTopNodeCreationTool, NODES);

        // Add dropped tool on Diagram Package container
        DropNodeTool ddPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddPackageTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(ddPackageTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddPackageTopNodeDescription.getPalette().setDropNodeTool(ddPackageGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Classifier} on Diagram with 3D box node.
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param type
     *         type of the {@link Classifier} to create
     * @return the created {@link} NodeDescription.
     */
    private NodeDescription createClassifierTopNodeDescription(DiagramDescription diagramDescription, EClass type) {
        NodeDescription ddClassifierTopNodeDescription = this.newNodeBuilder(type, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getDomainNodeName(type)) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(type))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(type.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(type.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon()) //
                .build();
        ddClassifierTopNodeDescription.setDefaultWidthExpression(SIZE_100);
        ddClassifierTopNodeDescription.setDefaultHeightExpression(SIZE_100);
        diagramDescription.getNodeDescriptions().add(ddClassifierTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ddClassifierTopNodeDescription);

        // create tools
        NodeTool ddClassifierTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), type);
        this.addDiagramToolInToolSection(diagramDescription, ddClassifierTopNodeCreationTool, NODES);

        return ddClassifierTopNodeDescription;
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Artifact}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createArtifactSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass artifactEClass = this.umlPackage.getArtifact();
        NodeDescription ddArtifactSharedNodeDescription = this.newNodeBuilder(artifactEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(artifactEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_ARTIFACT_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(artifactEClass.getName()))//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(artifactEClass.getName())) //
                .build();
        this.ddSharedDescription.getChildrenDescriptions().add(ddArtifactSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ddArtifactSharedNodeDescription);

        NodeTool ddArtifactSharedNodeCreationTool = this.getViewBuilder().createCreationTool(artifactEClass.getName(), this.getIdBuilder().getCreationToolId(artifactEClass),
                DeploymentDiagramServices.CREATE_ARTIFACT,
                List.of(this.getQueryBuilder().aqlString(artifactEClass.getName()), SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        List<EClass> owners = List.of(this.umlPackage.getArtifact(), //
                this.umlPackage.getNode(), //
                this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddArtifactSharedNodeDescription, diagramDescription, ddArtifactSharedNodeCreationTool, NODES, owners,
                List.of(this.umlPackage.getDevice(), this.umlPackage.getDeploymentSpecification()));

        // Add dropped tool on Shared Artifact container
        DropNodeTool ddArtifactGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ddArtifactSharedNodeDescription, SHARED_SUFFIX));
        List<EClass> children = List.of(this.umlPackage.getArtifact());
        this.registerCallback(ddArtifactSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddArtifactGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddArtifactSharedNodeDescription.getPalette().setDropNodeTool(ddArtifactGraphicalDropTool);

    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link DeploymentSpecification}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createDeploymentSpecificationSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass deploymentSepecificationEClass = this.umlPackage.getDeploymentSpecification();
        NodeDescription ddDeploymentSpecificationSharedNodeDescription = this.newNodeBuilder(deploymentSepecificationEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(deploymentSepecificationEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_DEPLOYMENT_SPECIFICATION_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, false))
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
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createDeviceSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass deviceEClass = this.umlPackage.getDevice();
        NodeDescription ddDeviceSharedNodeDescription = this.newNodeBuilder(deviceEClass, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(deviceEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_DEVICE_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(deviceEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(deviceEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon()) //
                .build();
        ddDeviceSharedNodeDescription.setDefaultWidthExpression(SIZE_100);
        ddDeviceSharedNodeDescription.setDefaultHeightExpression(SIZE_100);
        this.ddSharedDescription.getChildrenDescriptions().add(ddDeviceSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ddDeviceSharedNodeDescription);

        // create tools
        NodeTool ddDeviceSharedNodeCreationTool = this.getViewBuilder().createCreationTool(deviceEClass.getName(), this.getIdBuilder().getCreationToolId(deviceEClass),
                DeploymentDiagramServices.CREATE_NODE,
                List.of(this.getQueryBuilder().aqlString(deviceEClass.getName()), SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        List<EClass> owners = List.of(this.umlPackage.getNode(), //
                this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddDeviceSharedNodeDescription, diagramDescription, ddDeviceSharedNodeCreationTool, NODES, owners, List.of(this.umlPackage.getExecutionEnvironment()));

        // Add dropped tool on Shared Device container
        DropNodeTool ddDeviceGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ddDeviceSharedNodeDescription, SHARED_SUFFIX));
        List<EClass> children = List.of(this.umlPackage.getDeploymentSpecification(), this.umlPackage.getNode());
        this.registerCallback(ddDeviceSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddDeviceGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddDeviceSharedNodeDescription.getPalette().setDropNodeTool(ddDeviceGraphicalDropTool);

    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link ExecutionEnvironment}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createExecutionEnvironmentSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass executionEnvironmentEClass = this.umlPackage.getExecutionEnvironment();
        NodeDescription ddExecutionEnvironmentSharedNodeDescription = this.newNodeBuilder(executionEnvironmentEClass, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(executionEnvironmentEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_EXECUTION_ENVIRONMENT_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(executionEnvironmentEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(executionEnvironmentEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon()) //
                .build();
        ddExecutionEnvironmentSharedNodeDescription.setDefaultWidthExpression(SIZE_200);
        ddExecutionEnvironmentSharedNodeDescription.setDefaultHeightExpression(SIZE_100);
        this.ddSharedDescription.getChildrenDescriptions().add(ddExecutionEnvironmentSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ddExecutionEnvironmentSharedNodeDescription);

        // create tools
        NodeTool ddExecutionEnvironmentSharedNodeCreationTool = this.getViewBuilder().createCreationTool(executionEnvironmentEClass.getName(),
                this.getIdBuilder().getCreationToolId(executionEnvironmentEClass),
                DeploymentDiagramServices.CREATE_NODE,
                List.of(this.getQueryBuilder().aqlString(executionEnvironmentEClass.getName()), SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        List<EClass> owners = List.of(this.umlPackage.getNode(), //
                this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddExecutionEnvironmentSharedNodeDescription, diagramDescription, ddExecutionEnvironmentSharedNodeCreationTool, NODES, owners, List.of());

        // Add dropped tool on Shared ExecutionEnvironment container
        DropNodeTool ddExecutionEnvironmentSharedNodeGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ddExecutionEnvironmentSharedNodeDescription, SHARED_SUFFIX));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getExecutionEnvironment());
        this.registerCallback(ddExecutionEnvironmentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddExecutionEnvironmentSharedNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddExecutionEnvironmentSharedNodeDescription.getPalette().setDropNodeTool(ddExecutionEnvironmentSharedNodeGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Node}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createNodeSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass nodeEClass = this.umlPackage.getNode();
        NodeDescription ddNodeSharedNodeDescription = this.newNodeBuilder(nodeEClass, this.getViewBuilder().createCuboidNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(nodeEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(DeploymentDiagramServices.GET_NODE_NODE_CANDIDATES))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(nodeEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(nodeEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyleIcon()) //
                .build();
        ddNodeSharedNodeDescription.setDefaultWidthExpression(SIZE_100);
        ddNodeSharedNodeDescription.setDefaultHeightExpression(SIZE_100);
        this.ddSharedDescription.getChildrenDescriptions().add(ddNodeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(ddNodeSharedNodeDescription);

        // create tools
        NodeTool ddNodeSharedNodeCreationTool = this.getViewBuilder().createCreationTool(nodeEClass.getName(), this.getIdBuilder().getCreationToolId(nodeEClass), DeploymentDiagramServices.CREATE_NODE,
                List.of(this.getQueryBuilder().aqlString(nodeEClass.getName()), SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES));
        List<EClass> owners = List.of(this.umlPackage.getNode(), //
                this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddNodeSharedNodeDescription, diagramDescription, ddNodeSharedNodeCreationTool, NODES, owners, List.of(this.umlPackage.getExecutionEnvironment()));

        // Add dropped tool on Shared Node container
        DropNodeTool ddNodeGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getSpecializedNodeGraphicalDropToolName(ddNodeSharedNodeDescription, SHARED_SUFFIX));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getNode());
        this.registerCallback(ddNodeSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddNodeGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddNodeSharedNodeDescription.getPalette().setDropNodeTool(ddNodeGraphicalDropTool);

    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Package}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.umlPackage.getPackage();
        NodeDescription ddPackageSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()));
        ddPackageSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(packageEClass, SHARED_SUFFIX));
        ddPackageSharedNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        this.createDefaultToolSectionsInNodeDescription(ddPackageSharedNodeDescription);

        this.ddSharedDescription.getChildrenDescriptions().add(ddPackageSharedNodeDescription);

        NodeTool ddPackageSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddPackageSharedNodeDescription, diagramDescription, ddPackageSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Package container
        DropNodeTool ddPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddPackageSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(ddPackageSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddPackageSharedNodeDescription.getPalette().setDropNodeTool(ddPackageGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Model}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createModelSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass modelEClass = this.umlPackage.getModel();
        NodeDescription ddModelSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(modelEClass,
                CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()));
        ddModelSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(modelEClass, SHARED_SUFFIX));
        ddModelSharedNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        this.createDefaultToolSectionsInNodeDescription(ddModelSharedNodeDescription);

        this.ddSharedDescription.getChildrenDescriptions().add(ddModelSharedNodeDescription);

        NodeTool ddModelSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), modelEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(ddModelSharedNodeDescription, diagramDescription, ddModelSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Package container
        DropNodeTool ddSharedModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(ddModelSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getArtifact(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getNode(), this.umlPackage.getPackage());
        this.registerCallback(ddModelSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            ddSharedModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        ddModelSharedNodeDescription.getPalette().setDropNodeTool(ddSharedModelGraphicalDropTool);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link CommunicationPath}.
     *
     * @param diagramDescription
     *         the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createCommunicationPathEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> nodeSourcesAndTargets = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getNode());
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
     *         the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createDependencyEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> namedElementCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getNamedElement());
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
     *         the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createDeploymentEdgeDescription(DiagramDescription diagramDescription) {

        Supplier<List<NodeDescription>> sourceDeployedArtifact = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getDeployedArtifact());
        Supplier<List<NodeDescription>> targetDeploymentTarget = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getDeploymentTarget());

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
     *         the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createGeneralizationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceAndTargetDescriptionsSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClassifier());

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
     *         the Deployment {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createManifestationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> packageableELementTargetCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getPackageableElement());
        Supplier<List<NodeDescription>> namedElementSourceCollector = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getNamedElement());

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

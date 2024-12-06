/*****************************************************************************
 * Copyright (c) 2022, 2025 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.application.representations.view.builders;

import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DIAGRAM_CONTEXT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDGE_SOURCE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDGE_TARGET;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDITING_CONTEXT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_EDGE_SOURCE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_EDGE_TARGET;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.uml.domain.services.UMLHelper;
import org.eclipse.papyrus.web.application.representations.view.IDomainHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.application.representations.view.StyleProvider;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.papyrus.web.application.representations.view.aql.QueryHelper;
import org.eclipse.papyrus.web.application.representations.view.aql.Services;
import org.eclipse.papyrus.web.application.representations.view.aql.Variables;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesFactory;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription;
import org.eclipse.sirius.components.view.ChangeContext;
import org.eclipse.sirius.components.view.Operation;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.diagram.ConditionalInsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.ConditionalOutsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.DeleteTool;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.DropTool;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.HeaderSeparatorDisplayMode;
import org.eclipse.sirius.components.view.diagram.ImageNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelEditTool;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.NodeToolSection;
import org.eclipse.sirius.components.view.diagram.OutsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.OutsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SourceEdgeEndReconnectionTool;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.TargetEdgeEndReconnectionTool;
import org.eclipse.sirius.components.view.diagram.customnodes.CustomnodesFactory;
import org.eclipse.sirius.components.view.diagram.customnodes.EllipseNodeStyleDescription;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Builder in charge of creating elements to fill a {@link DiagramDescription}.
 *
 * @author Arthur Daussy
 */
public class ViewBuilder {

    /**
     * Direct Edit tool name prefix.
     */
    private static final String ICON_PATH = "/icons-override/full/obj16/";

    private static final String ICON_SVG_EXTENSION = ".svg";

    private static final String DIRET_EDIT = "DirectEdit ";

    private QueryHelper queryBuilder;

    private StyleProvider styleProvider;

    private IdBuilder idBuilder;

    private IDomainHelper metamodelHelper;

    public ViewBuilder(QueryHelper queryBuilder, StyleProvider styleProvider, IdBuilder idBuilder, IDomainHelper metamodelHelper) {
        this.metamodelHelper = Objects.requireNonNull(metamodelHelper);
        this.idBuilder = Objects.requireNonNull(idBuilder);
        this.queryBuilder = Objects.requireNonNull(queryBuilder);
        this.styleProvider = Objects.requireNonNull(styleProvider);
    }

    /**
     * Creates a semantic drop tool with the given {@code semanticDropToolId}.
     *
     * @param semanticDropToolId
     *            identifier of the semantic tool to create
     * @return the new semantic drop tool.
     */
    public DropTool createGenericSemanticDropTool(String semanticDropToolId) {
        DropTool dropTool = DiagramFactory.eINSTANCE.createDropTool();
        dropTool.setName(semanticDropToolId);

        ChangeContext changeContextOp = ViewFactory.eINSTANCE.createChangeContext();
        dropTool.getBody().add(changeContextOp);
        changeContextOp.setExpression(this.queryBuilder.querySemanticDrop());
        return dropTool;
    }

    /**
     * Creates a graphical drop tool with the given {@code graphicalDropToolId}.
     *
     * @param graphicalDropToolId
     *            identifier of the graphical tool to create
     * @return the new graphical drop tool.
     */
    public DropNodeTool createGraphicalDropTool(String graphicalDropToolId) {
        DropNodeTool graphicalDropTool = DiagramFactory.eINSTANCE.createDropNodeTool();
        graphicalDropTool.setName(graphicalDropToolId);

        ChangeContext changeContextOp = ViewFactory.eINSTANCE.createChangeContext();
        graphicalDropTool.getBody().add(changeContextOp);
        changeContextOp.setExpression(this.queryBuilder.queryGraphicalDrop());
        return graphicalDropTool;
    }

    public DiagramDescription buildDiagramDescription(String diagramName, EClass modelType) {
        DiagramDescription diargamDescription = this.createDiagram(diagramName);
        diargamDescription.setDomainType(this.metamodelHelper.getDomain(modelType));
        return diargamDescription;
    }

    private DiagramDescription createDiagram(String diagramName) {
        DiagramDescription diagramDescription = DiagramFactory.eINSTANCE.createDiagramDescription();
        diagramDescription.setName(diagramName);
        diagramDescription.setPalette(DiagramFactory.eINSTANCE.createDiagramPalette());
        return diagramDescription;
    }

    private SourceEdgeEndReconnectionTool createDomainBaseEdgeSourceReconnectionTool(EdgeDescription description, String name) {
        return this.createSourceReconnectionTool(description, name, List.of(this.createChangeContextOperation(this.queryBuilder.queryDomainBasedSourceReconnection())));
    }

    public SourceEdgeEndReconnectionTool createSourceReconnectionTool(EdgeDescription description, String name, List<Operation> operations) {
        SourceEdgeEndReconnectionTool sourceReconnectionTool = DiagramFactory.eINSTANCE.createSourceEdgeEndReconnectionTool();
        sourceReconnectionTool.setName(name);
        sourceReconnectionTool.getBody().addAll(operations);
        return sourceReconnectionTool;
    }

    private TargetEdgeEndReconnectionTool createDomainBaseEdgeTargetReconnectionTool(EdgeDescription description, String name) {
        return this.createTargetReconnectionTool(description, name, List.of(this.createChangeContextOperation(this.queryBuilder.queryDomainBasedTargetReconnection())));
    }

    public TargetEdgeEndReconnectionTool createTargetReconnectionTool(EdgeDescription description, String name, List<Operation> operations) {
        TargetEdgeEndReconnectionTool targetReconnectionTool = DiagramFactory.eINSTANCE.createTargetEdgeEndReconnectionTool();
        targetReconnectionTool.setName(name);
        targetReconnectionTool.getBody().addAll(operations);
        return targetReconnectionTool;
    }

    protected NodeDescriptionBuilder newNodeBuilder(EClass semanticDomain, NodeStyleDescription style) {
        return new NodeDescriptionBuilder(this.idBuilder, this.queryBuilder, semanticDomain, style, this.metamodelHelper);
    }

    private NodeDescription createNodeDescription(String id, EClass domainType, String semanticCandidateExpression, NodeStyleDescription style, SynchronizationPolicy synchronizationPolicy) {

        return this.newNodeBuilder(domainType, style)
                .name(id)
                .semanticCandidateExpression(semanticCandidateExpression)
                .synchronizationPolicy(synchronizationPolicy)
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())
                .insideLabelDescription(this.queryBuilder.queryRenderLabel(), this.createDefaultInsideLabelStyleIcon())
                .build();
    }

    private EdgeDescription createSynchonizedDomainBaseEdgeDescription(String id, EClass domainType, String semanticCandidateExpression, Supplier<List<NodeDescription>> sources,
            Supplier<List<NodeDescription>> targets) {
        EdgeDescription edgeDescription = DiagramFactory.eINSTANCE.createEdgeDescription();
        edgeDescription.setName(id);
        edgeDescription.setIsDomainBasedEdge(true);
        edgeDescription.setDomainType(this.metamodelHelper.getDomain(domainType));
        edgeDescription.setSynchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED);
        edgeDescription.setCenterLabelExpression(this.queryBuilder.queryRenderLabel());
        edgeDescription.setSemanticCandidatesExpression(semanticCandidateExpression);
        edgeDescription.setPalette(DiagramFactory.eINSTANCE.createEdgePalette());

        edgeDescription.eAdapters().add(new CallbackAdapter(() -> {
            edgeDescription.getSourceDescriptions().addAll(sources.get());
            edgeDescription.getTargetDescriptions().addAll(targets.get());
        }));

        edgeDescription.setSourceExpression(this.queryBuilder.aqlDomainBaseGetSourceQuery());
        edgeDescription.setTargetExpression(this.queryBuilder.aqlDomainBaseGetTargetsQuery());

        edgeDescription.setStyle(this.createDefaultEdgeStyle());

        return edgeDescription;
    }

    private EdgeStyle createDefaultEdgeStyle() {
        EdgeStyle edgeStyle = DiagramFactory.eINSTANCE.createEdgeStyle();
        edgeStyle.setColor(this.styleProvider.getEdgeColor());
        edgeStyle.setFontSize(this.styleProvider.getFontSize());
        edgeStyle.setLineStyle(this.styleProvider.getEdgeStyle());
        edgeStyle.setSourceArrowStyle(this.styleProvider.getSourceArrowStyle());
        edgeStyle.setTargetArrowStyle(this.styleProvider.getTargetArrowStyle());
        edgeStyle.setEdgeWidth(this.styleProvider.getEdgeWidth());
        edgeStyle.setBorderSize(0);
        return edgeStyle;
    }

    private EdgeTool createDomainBasedEdgeTool(String id, EdgeDescription description, EReference containmentReference) {
        EdgeTool tool = DiagramFactory.eINSTANCE.createEdgeTool();
        tool.setName(id);
        String typeName = UMLHelper.toEClass(description.getDomainType()).getName();
        tool.setIconURLsExpression(getIconPathFromString(this.capitalize(typeName)));
        ChangeContext changeContext = ViewFactory.eINSTANCE.createChangeContext();
        changeContext.setExpression(this.queryBuilder.queryCreateDomainBaseEdge(description, containmentReference));
        // Configure the tool's target element descriptions once the representation has been fully created. This ensures
        // that description.getTargetNodeDescription has been filled with the descriptions.
        description.eAdapters().add(new CallbackAdapter(() -> {
            List<DiagramElementDescription> targetNodeDescriptions = description.getTargetDescriptions();
            tool.getTargetElementDescriptions().addAll(targetNodeDescriptions);
        }));
        tool.getBody().add(changeContext);
        return tool;
    }

    public EdgeTool createFeatureBasedEdgeTool(String id, String serviceExpression, List<? extends DiagramElementDescription> targets) {
        EdgeTool tool = DiagramFactory.eINSTANCE.createEdgeTool();
        tool.setName(id);
        tool.setIconURLsExpression(getIconPathFromString(this.getReferenceNameFromToolId(id)));
        tool.getTargetElementDescriptions().addAll(targets);
        ChangeContext changeContext = ViewFactory.eINSTANCE.createChangeContext();
        changeContext.setExpression(serviceExpression);

        tool.getBody().add(changeContext);

        return tool;
    }

    private NodeDescription createUnsynchonizedPortDescription(String id, EClass domainType, String semanticCandidateExpression) {
        NodeDescription borderNodeDescription = this.createNodeDescription(id, domainType, semanticCandidateExpression, this.createDefaultRectangularNodeStyle(), SynchronizationPolicy.UNSYNCHRONIZED);
        borderNodeDescription.setDefaultWidthExpression(String.valueOf(this.styleProvider.getPortSize()));
        borderNodeDescription.setDefaultHeightExpression(String.valueOf(this.styleProvider.getPortSize()));
        return borderNodeDescription;
    }

    /**
     * Create a creation tool to create a unsynchronized {@link NodeDescription}.
     *
     * @param name
     *            the name of the tool
     * @param containementRef
     *            the containment reference used to contained the new element
     * @param newType
     *            the type of the element to create
     * @return a new {@link NodeTool}
     */
    public NodeTool createCreationTool(String name, EReference containementRef, EClass newType) {
        return this.createCreationTool(name, Variables.SELF, containementRef, newType);
    }

    /**
     * Create a creation tool to create a unsynchronized {@link NodeDescription}.
     *
     * @param name
     *            the name of the tool
     * @param selfValue
     *            the self expression
     * @param containementRef
     *            the containment reference used to contained the new element
     * @param newType
     *            the type of the element to create
     * @return a new {@link NodeTool}
     */
    public NodeTool createCreationTool(String name, String selfValue, EReference containementRef, EClass newType) {
        return this.createCreationTool(name, selfValue, containementRef, this.metamodelHelper.getDomain(newType));
    }

    /**
     * Create a creation tool to create a unsynchronized {@link NodeDescription}.
     *
     * @param name
     *            the name of the tool
     * @param selfValue
     *            the self expression
     * @param containementRef
     *            the containment reference used to contained the new element
     * @param newType
     *            the type of the element to create
     * @return a new {@link NodeTool}
     */
    public NodeTool createCreationToolInHolder(String name, String selfValue, EReference containementRef, EClass newType) {
        return this.createCreationToolInHolder(name, selfValue, containementRef, this.metamodelHelper.getDomain(newType));
    }

    private NodeTool createCreationTool(String name, String selfValue, EReference containementRef, String newType) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName(name);
        nodeTool.setIconURLsExpression(getIconURLFromToolName(newType));
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression(this.queryBuilder.createNodeQuery(newType, selfValue, containementRef));
        nodeTool.getBody().add(createElement);

        return nodeTool;
    }

    /**
     * Creates a creation {@link NodeTool} that delegates to the provided {@code serviceName}.
     * <p>
     * This method is used to create creation tools that rely on diagram-specific creation services. See
     * {@link ViewBuilder#createCreationTool(EReference, EClass)} to create a creation {@link NodeTool} that relies on
     * the default creation mechanism.
     * </p>
     *
     * @param containementRef
     *            the containment reference used to contained the new element
     * @param newType
     *            the type of the element to create
     * @return the created {@link NodeTool}
     */
    public NodeTool createCreationToolInHolder(EReference containementRef, EClass newType) {
        return this.createCreationToolInHolder(this.idBuilder.getCreationToolId(newType), Variables.SELF, containementRef, this.metamodelHelper.getDomain(newType));
    }

    /**
     * Creates a creation {@link NodeTool} that delegates to the provided {@code serviceName}.
     * <p>
     * This method is used to create creation tools that rely on diagram-specific creation services. See
     * {@link ViewBuilder#createCreationTool(EReference, EClass)} to create a creation {@link NodeTool} that relies on
     * the default creation mechanism.
     * </p>
     *
     * @param name
     *            the name of the tool to create
     * @param serviceName
     *            the name of the service to call
     * @param serviceParameters
     *            the parameters provided to the service
     * @return the created {@link NodeTool}
     */
    public NodeTool createCreationToolInHolder(String name, String selfValue, EReference containementRef, String newType) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName(name);
        nodeTool.setIconURLsExpression(getIconURLFromToolName(newType));
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression(this.queryBuilder.createNodeInHolderQuery(newType, selfValue, containementRef));
        nodeTool.getBody().add(createElement);

        return nodeTool;
    }

    /**
     * Creates a creation {@link NodeTool} that delegates to the provided {@code serviceName}.
     * <p>
     * This method is used to create creation tools that rely on diagram-specific creation services. See
     * {@link ViewBuilder#createCreationTool(EReference, EClass)} to create a creation {@link NodeTool} that relies on
     * the default creation mechanism.
     * </p>
     *
     * @param domain
     *            the kind of element to create
     * @param toolName
     *            the name of the tool to create
     * @param serviceName
     *            the name of the service to call
     * @param serviceParameters
     *            the parameters provided to the service
     * @return the created {@link NodeTool}
     */
    public NodeTool createCreationTool(String domain, String toolName, String serviceName, List<String> serviceParameters) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName(toolName);
        nodeTool.setIconURLsExpression(getIconURLFromToolName(domain));
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression(CallQuery.queryServiceOnSelf(serviceName, serviceParameters.toArray(String[]::new)));
        nodeTool.getBody().add(createElement);
        return nodeTool;
    }

    /**
     * Creates a creation {@link NodeTool} that delegates to the provided {@code serviceName}.
     * <p>
     * This method is used to create creation tools that rely on diagram-specific creation services. See
     * {@link ViewBuilder#createCreationTool(EReference, EClass)} to create a creation {@link NodeTool} that relies on
     * the default creation mechanism.
     * </p>
     *
     * @param domain
     *            the kind of element to create
     * @param toolName
     *            the name of the tool to create
     * @param serviceName
     *            the name of the service to call
     * @param serviceParameters
     *            the parameters provided to the service
     * @return the created {@link NodeTool}
     */
    public NodeTool createCreationToolInHolder(String domain, String toolName, String serviceName, List<String> serviceParameters) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName(toolName);
        nodeTool.setIconURLsExpression(getIconURLFromToolName(domain));
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression(CallQuery.queryServiceOnSelf(serviceName, serviceParameters.toArray(String[]::new)));
        nodeTool.getBody().add(createElement);
        return nodeTool;
    }

    /**
     * Creates a creation {@link NodeTool} to create {@code newType} elements inside the {@code compartmentName}
     * compartment of the containing element.
     *
     * @param toolName
     *            the name of the tool to create
     * @param compartmentName
     *            the name of the compartment where to create
     * @param containementRef
     *            the containment reference used to contained the new element
     * @param newType
     *            the new type to create
     * @return the created {@link NodeTool} used to create {@code newType} elements inside the {@code compartmentName}
     *         compartment of the containing element
     */
    public NodeTool createInCompartmentCreationTool(String toolName, String compartmentName, EReference containementRef, String newType) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName(toolName);
        nodeTool.setIconURLsExpression(getIconURLFromToolName(newType));
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression(this.queryBuilder.createInCompartmentNodeQuery(newType, compartmentName, containementRef));
        nodeTool.getBody().add(createElement);

        return nodeTool;
    }

    public NodeTool createSiblingCreationTool(String name, String selfValue, EReference containementRef, EClass newType) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName(name);
        nodeTool.setIconURLsExpression(getIconPathFromString(newType.getName()));

        // Create instance and init
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression(this.queryBuilder.createSiblingNodeQuery(this.metamodelHelper.getDomain(newType), selfValue, containementRef));
        nodeTool.getBody().add(createElement);

        return nodeTool;
    }

    public DeleteTool createNodeDeleteTool(String conceptName) {
        DeleteTool deleteTool = DiagramFactory.eINSTANCE.createDeleteTool();

        deleteTool.setName("Delete " + conceptName);

        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression(this.queryBuilder.queryDestroyNode());
        deleteTool.getBody().add(createElement);

        return deleteTool;
    }

    public DeleteTool createEdgeDeleteTool(String conceptName) {
        DeleteTool deleteTool = DiagramFactory.eINSTANCE.createDeleteTool();

        deleteTool.setName("Delete " + conceptName);

        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression(this.queryBuilder.queryDestroyEdge());
        deleteTool.getBody().add(createElement);

        return deleteTool;
    }

    public ConditionalNodeStyle createConditionalNodeStyle(String condition, NodeStyleDescription nodeStyle) {
        ConditionalNodeStyle conditionalNodeStyle = DiagramFactory.eINSTANCE.createConditionalNodeStyle();
        conditionalNodeStyle.setCondition(condition);
        conditionalNodeStyle.setStyle(nodeStyle);
        return conditionalNodeStyle;
    }

    public ConditionalInsideLabelStyle createConditionalInsideLabelStyle(String condition, InsideLabelStyle nodeStyle) {
        ConditionalInsideLabelStyle conditionalNodeStyle = DiagramFactory.eINSTANCE.createConditionalInsideLabelStyle();
        conditionalNodeStyle.setCondition(condition);
        conditionalNodeStyle.setStyle(nodeStyle);
        return conditionalNodeStyle;
    }

    public ConditionalOutsideLabelStyle createConditionalOutsideLabelStyle(String condition, OutsideLabelStyle nodeStyle) {
        ConditionalOutsideLabelStyle conditionalNodeStyle = DiagramFactory.eINSTANCE.createConditionalOutsideLabelStyle();
        conditionalNodeStyle.setCondition(condition);
        conditionalNodeStyle.setStyle(nodeStyle);
        return conditionalNodeStyle;
    }

    public RectangularNodeStyleDescription createRectangularNodeStyle() {
        RectangularNodeStyleDescription nodeStyle = DiagramFactory.eINSTANCE.createRectangularNodeStyleDescription();
        this.initStyle(nodeStyle);
        return nodeStyle;
    }

    private void initStyle(RectangularNodeStyleDescription nodeStyle) {
        this.defaultInitStyle(nodeStyle);
        nodeStyle.setBackground(this.styleProvider.getBackgroundColor());
    }

    public EllipseNodeStyleDescription createEllipseNodeStyle() {
        EllipseNodeStyleDescription nodeStyle = CustomnodesFactory.eINSTANCE.createEllipseNodeStyleDescription();
        this.initStyle(nodeStyle);
        return nodeStyle;
    }

    private void initStyle(EllipseNodeStyleDescription nodeStyle) {
        this.defaultInitStyle(nodeStyle);
        nodeStyle.setBackground(this.styleProvider.getBackgroundColor());
    }

    public PackageNodeStyleDescription createPackageNodeStyle() {
        PackageNodeStyleDescription nodeStyle = PapyrusCustomNodesFactory.eINSTANCE.createPackageNodeStyleDescription();
        this.initStyle(nodeStyle);
        return nodeStyle;
    }

    private void initStyle(PackageNodeStyleDescription nodeStyle) {
        this.defaultInitStyle(nodeStyle);
        nodeStyle.setBackground(this.styleProvider.getBackgroundColor());
    }

    public NoteNodeStyleDescription createNoteNodeStyle() {
        NoteNodeStyleDescription nodeStyle = PapyrusCustomNodesFactory.eINSTANCE.createNoteNodeStyleDescription();
        this.initStyle(nodeStyle);
        return nodeStyle;
    }

    private void initStyle(NoteNodeStyleDescription nodeStyle) {
        this.defaultInitStyle(nodeStyle);
        nodeStyle.setBackground(this.styleProvider.getBackgroundColor());
    }

    public RectangleWithExternalLabelNodeStyleDescription createRectangleWithExternalLabelNodeStyle() {
        RectangleWithExternalLabelNodeStyleDescription nodeStyle = PapyrusCustomNodesFactory.eINSTANCE.createRectangleWithExternalLabelNodeStyleDescription();
        this.initStyle(nodeStyle);
        return nodeStyle;
    }

    private void initStyle(RectangleWithExternalLabelNodeStyleDescription nodeStyle) {
        this.defaultInitStyle(nodeStyle);
        nodeStyle.setBackground(this.styleProvider.getBackgroundColor());
    }

    public CuboidNodeStyleDescription createCuboidNodeStyle() {
        CuboidNodeStyleDescription nodeStyle = PapyrusCustomNodesFactory.eINSTANCE.createCuboidNodeStyleDescription();
        this.initStyle(nodeStyle);
        return nodeStyle;
    }

    private void initStyle(CuboidNodeStyleDescription nodeStyle) {
        this.defaultInitStyle(nodeStyle);
        nodeStyle.setBackground(this.styleProvider.getBackgroundColor());
    }

    public ImageNodeStyleDescription createImageNodeStyle(String imagePath) {
        ImageNodeStyleDescription nodeStyle = DiagramFactory.eINSTANCE.createImageNodeStyleDescription();
        this.initStyle(nodeStyle);
        nodeStyle.setShape(imagePath);
        nodeStyle.setBorderSize(0);
        return nodeStyle;
    }

    private void initStyle(ImageNodeStyleDescription nodeStyle) {
        this.defaultInitStyle(nodeStyle);
    }

    public EdgeDescription createFeatureEdgeDescription(String id, String labelExpression, String targetNodeExpression, Supplier<List<NodeDescription>> sourcesProvider,
            Supplier<List<NodeDescription>> targetsProvider) {

        EdgeDescription edgeDescription = DiagramFactory.eINSTANCE.createEdgeDescription();
        edgeDescription.setName(id);
        edgeDescription.setCenterLabelExpression(labelExpression);
        edgeDescription.setTargetExpression(targetNodeExpression);
        edgeDescription.setStyle(this.createDefaultEdgeStyle());
        edgeDescription.setPalette(DiagramFactory.eINSTANCE.createEdgePalette());

        edgeDescription.eAdapters().add(new CallbackAdapter(() -> {
            edgeDescription.getSourceDescriptions().addAll(sourcesProvider.get());
            edgeDescription.getTargetDescriptions().addAll(targetsProvider.get());
        }));

        return edgeDescription;
    }

    private void defaultInitStyle(NodeStyleDescription nodeStyle) {
        nodeStyle.setBorderColor(this.styleProvider.getBorderNodeColor());
        nodeStyle.setBorderRadius(this.styleProvider.getNodeBorderRadius());
        nodeStyle.setBorderSize(1);
    }

    private RectangularNodeStyleDescription createDefaultRectangularNodeStyle() {
        RectangularNodeStyleDescription nodeStyle = DiagramFactory.eINSTANCE.createRectangularNodeStyleDescription();
        this.initStyle(nodeStyle);
        return nodeStyle;
    }

    /**
     * Create tool section in the diagram palette.
     *
     * @param name
     *            the name of the tool section to create
     * @return the tool section in the diagram palette
     */
    public DiagramToolSection createDiagramToolSection(String name) {
        DiagramToolSection diagramToolSection = DiagramFactory.eINSTANCE.createDiagramToolSection();
        diagramToolSection.setName(name);
        return diagramToolSection;
    }

    /**
     * Create tool section in the given node description palette.
     *
     * @param name
     *            the name of the tool section to create
     * @return the tool section in the given node description palette
     */
    public NodeToolSection createNodeToolSection(String name) {
        NodeToolSection nodeToolSection = DiagramFactory.eINSTANCE.createNodeToolSection();
        nodeToolSection.setName(name);
        return nodeToolSection;
    }

    public ChangeContext createChangeContextOperation(String string) {
        ChangeContext changeContext = ViewFactory.eINSTANCE.createChangeContext();
        changeContext.setExpression(string);
        return changeContext;
    }

    public LabelEditTool createDirectEditTool(String domainTypeName) {
        return this.createDirectEditTool(domainTypeName, Variables.SELF);
    }

    public LabelEditTool createDirectEditTool(String domainTypeName, String selfExpression) {
        LabelEditTool directEditTool = DiagramFactory.eINSTANCE.createLabelEditTool();
        directEditTool.setInitialDirectEditLabelExpression(new CallQuery(selfExpression).callService(Services.GET_DIRECT_EDIT_INPUT_VALUE_SERVICE));
        directEditTool.getBody().add(this.createChangeContextOperation(new CallQuery(selfExpression).callService(Services.CONSUME_DIRECT_EDIT_VALUE_SERVICE, Variables.ARG0)));
        directEditTool.setName(DIRET_EDIT + domainTypeName);
        return directEditTool;
    }

    public EdgeDescription createDefaultSynchonizedDomainBaseEdgeDescription(EClass eClass, String semanticCandidateExpression, Supplier<List<NodeDescription>> source,
            Supplier<List<NodeDescription>> targets) {
        return this.createDefaultSynchonizedDomainBaseEdgeDescription(eClass, semanticCandidateExpression, source, targets, true);
    }

    public EdgeDescription createDefaultSynchonizedDomainBaseEdgeDescription(EClass eClass, String semanticCandidateExpression, Supplier<List<NodeDescription>> source,
            Supplier<List<NodeDescription>> targets, boolean isDirectEditActivated) {
        EdgeDescription result = this.createSynchonizedDomainBaseEdgeDescription(this.idBuilder.getDomainBaseEdgeId(eClass), eClass, semanticCandidateExpression, source, targets);
        this.addDefaultDeleteTool(result);
        if (isDirectEditActivated) {
            this.addDirectEditTool(result);
        }
        return result;
    }

    public void addDefaultDeleteTool(NodeDescription nodeDescription) {
        nodeDescription.getPalette().setDeleteTool(this.createNodeDeleteTool(this.metamodelHelper.toEClass(nodeDescription.getDomainType()).getName()));
    }

    public void addDefaultDeleteTool(EdgeDescription edgeDescription) {
        if (edgeDescription.isIsDomainBasedEdge()) {
            edgeDescription.getPalette().setDeleteTool(this.createEdgeDeleteTool(this.metamodelHelper.toEClass(edgeDescription.getDomainType()).getName()));
        }
    }

    public void addDirectEditTool(NodeDescription description) {
        description.getPalette().setLabelEditTool(this.createDirectEditTool(this.metamodelHelper.toEClass(description.getDomainType()).getName()));
    }

    public void addDirectEditTool(EdgeDescription description) {
        description.getPalette().setCenterLabelEditTool(this.createDirectEditTool(this.metamodelHelper.toEClass(description.getDomainType()).getName()));
    }

    public NodeDescription createSpecializedUnsynchonizedNodeDescription(EClass domain, String semanticCandidateExpression, String specialization) {
        NodeDescription result = this.createNodeDescription(this.idBuilder.getSpecializedDomainNodeName(domain, specialization), domain, semanticCandidateExpression,
                this.createRectangularNodeStyle(), SynchronizationPolicy.UNSYNCHRONIZED);
        result.getInsideLabel().getStyle().setWithHeader(true);
        result.getInsideLabel().getStyle().setHeaderSeparatorDisplayMode(HeaderSeparatorDisplayMode.IF_CHILDREN);
        this.addDefaultDeleteTool(result);
        this.addDirectEditTool(result);
        return result;
    }

    public NodeDescription createNoteStyleUnsynchonizedNodeDescription(EClass domain, String semanticCandidateExpression) {
        NodeDescription result = this.createNodeDescription(this.idBuilder.getDomainNodeName(domain), domain, semanticCandidateExpression, this.createNoteNodeStyle(),
                SynchronizationPolicy.UNSYNCHRONIZED);
        this.addDefaultDeleteTool(result);
        this.addDirectEditTool(result);
        return result;
    }

    public NodeDescription createPackageStyleUnsynchonizedNodeDescription(EClass domain, String semanticCandidateExpression) {
        NodeDescription result = this.createNodeDescription(this.idBuilder.getDomainNodeName(domain), domain, semanticCandidateExpression, this.createPackageNodeStyle(),
                SynchronizationPolicy.UNSYNCHRONIZED);
        result.setCollapsible(true);
        result.setDefaultWidthExpression("300");
        result.setDefaultHeightExpression("150");
        this.addDefaultDeleteTool(result);
        this.addDirectEditTool(result);
        return result;
    }

    public NodeDescription createSpecializedPortUnsynchonizedNodeDescription(String suffixId, EClass domain, String semanticCandidateExpression) {
        NodeDescription result = this.createUnsynchonizedPortDescription(this.idBuilder.getSpecializedDomainNodeName(domain, suffixId), domain, semanticCandidateExpression);
        result.getInsideLabel().getStyle().setShowIconExpression("aql:false");
        this.addDefaultDeleteTool(result);
        this.addDirectEditTool(result);
        return result;
    }

    public EdgeTool createDefaultDomainBasedEdgeTool(EdgeDescription description, EReference contaimentRef) {
        if (!description.isIsDomainBasedEdge()) {
            throw new IllegalArgumentException("Expecting a domain based edge but got " + description);
        }
        return this.createDomainBasedEdgeTool(this.idBuilder.getCreationToolId(description), description, contaimentRef);
    }

    public NodeTool createCreationTool(EReference containementRef, EClass newType) {
        return this.createCreationTool(this.idBuilder.getCreationToolId(newType), containementRef, newType);
    }

    public NodeTool createCreationTool(NodeDescription node, EReference containementRef) {
        EClass newElementType = this.metamodelHelper.toEClass(node.getDomainType());
        return this.createCreationTool(this.idBuilder.getCreationToolId(newElementType), containementRef, newElementType);
    }

    public void addDefaultReconnectionTools(EdgeDescription edge) {
        edge.getPalette().getEdgeReconnectionTools().add(this.createDomainBaseEdgeSourceReconnectionTool(edge, this.idBuilder.getSourceReconnectionToolId(edge)));
        edge.getPalette().getEdgeReconnectionTools().add(this.createDomainBaseEdgeTargetReconnectionTool(edge, this.idBuilder.getTargetReconnectionToolId(edge)));
    }

    public InsideLabelStyle createDefaultInsideLabelStyleIcon() {
        return this.createDefaultInsideLabelStyle(true, false);
    }

    public InsideLabelStyle createDefaultInsideLabelStyle(boolean showIcon, boolean isHeader) {
        InsideLabelStyle style = DiagramFactory.eINSTANCE.createInsideLabelStyle();

        style.setLabelColor(this.styleProvider.getNodeLabelColor());
        style.setShowIconExpression("aql:" + showIcon);
        style.setBorderSize(0);
        style.setWithHeader(isHeader);
        style.setHeaderSeparatorDisplayMode(HeaderSeparatorDisplayMode.IF_CHILDREN);
        return style;
    }

    public OutsideLabelStyle createDefaultOutsideLabelStyle(boolean showIcon) {
        OutsideLabelStyle style = DiagramFactory.eINSTANCE.createOutsideLabelStyle();

        style.setLabelColor(this.styleProvider.getNodeLabelColor());
        style.setShowIconExpression("aql:" + showIcon);
        style.setBorderSize(0);
        return style;
    }

    public OutsideLabelDescription createOutsideLabelDescription(String expression, boolean showIcon) {
        OutsideLabelDescription outsideLabelDescription = DiagramFactory.eINSTANCE.createOutsideLabelDescription();
        outsideLabelDescription.setLabelExpression(expression);
        outsideLabelDescription.setStyle(this.createDefaultOutsideLabelStyle(showIcon));
        return outsideLabelDescription;
    }

    public EdgeTool createDomainBasedEdgeToolWithService(String specializationName, String serviceName) {
        EdgeTool tool = DiagramFactory.eINSTANCE.createEdgeTool();
        tool.setName(specializationName);
        ChangeContext changeContext = ViewFactory.eINSTANCE.createChangeContext();

        String query = new CallQuery(SEMANTIC_EDGE_SOURCE)//
                .callService(serviceName, //
                        SEMANTIC_EDGE_TARGET, //
                        EDGE_SOURCE, //
                        EDGE_TARGET, //
                        EDITING_CONTEXT, //
                        DIAGRAM_CONTEXT);
        changeContext.setExpression(query);

        tool.getBody().add(changeContext);
        return tool;
    }

    public InsideLabelDescription createInsideLabelDescription(String expression, boolean showIcon, boolean isHeader) {
        InsideLabelDescription description = DiagramFactory.eINSTANCE.createInsideLabelDescription();
        description.setLabelExpression(expression);
        description.setTextAlign(LabelTextAlign.CENTER);
        InsideLabelStyle style = this.createDefaultInsideLabelStyle(showIcon, isHeader);
        description.setStyle(style);
        return description;
    }

    public InsideLabelDescription createDefaultInsideLabelDescription(boolean showIcon, boolean isHeader) {
        return this.createInsideLabelDescription(this.queryBuilder.queryRenderLabel(), showIcon, isHeader);
    }

    public OutsideLabelDescription createDefaultOutsideLabelDescription(boolean showIcon) {
        return this.createOutsideLabelDescription(this.queryBuilder.queryRenderLabel(), showIcon);
    }

    private String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private String getReferenceNameFromToolId(String input) {
        return this.capitalize(input.replaceFirst("New ", "").replaceAll(" ", ""));
    }

    /**
     * Compute the path of the icon.
     *
     * @param name
     *            the name of element kind
     * @return the path of the icon to use for the tool
     */
    public static String getIconPathFromString(String name) {
        return ICON_PATH + name + ICON_SVG_EXTENSION;
    }

    /**
     * Compute the path of the icon.
     *
     * @param toolName
     *            the name of the tool
     * @return the path of the icon to use for the tool
     */
    public static String getIconURLFromToolName(String toolName) {
        EClass eClass = UMLHelper.toEClass(toolName);
        if (eClass != null) {
            String typeName = eClass.getName();
            return getIconPathFromString(typeName);
        }
        return null;
    }

    public NodeDescription createSymbolNodeDescription() {
        EClass domain = UMLPackage.eINSTANCE.getElement();
        String semanticCandidateExpression = "aql:self";
        NodeDescription result = this.createNodeDescription(this.idBuilder.getDomainNodeName(domain), domain,
                semanticCandidateExpression, this.createCustomImageNodeStyle(),
                SynchronizationPolicy.SYNCHRONIZED);
        result.setPreconditionExpression("aql:self.getSymbolValue()<>''");
        // ?
        result.setCollapsible(false);
        return result;
    }

    public CustomImageNodeStyleDescription createCustomImageNodeStyle() {
        CustomImageNodeStyleDescription nodeStyle = PapyrusCustomNodesFactory.eINSTANCE.createCustomImageNodeStyleDescription();
        nodeStyle.setShape("aql:self.getSymbolValue()");
        nodeStyle.setBorderColor(this.styleProvider.getBorderNodeColor());
        nodeStyle.setBorderRadius(this.styleProvider.getNodeBorderRadius());
        // ?
        // this.defaultInitStyle(nodeStyle);
        // nodeStyle.setBackground(this.styleProvider.getBackgroundColor());
        return nodeStyle;
    }
}

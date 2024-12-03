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
 *  Aurelien Didier (Artal Technologies) - Issue 190
 *  Titouan BOUËTE-GIRAUD (Artal Technologies) - titouan.bouete-giraud@artal.fr - Issues 219, 227
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import static org.eclipse.papyrus.web.application.representations.view.aql.CallQuery.queryAttributeOnSelf;
import static org.eclipse.papyrus.web.application.representations.view.aql.CallQuery.queryOperationOnSelf;
import static org.eclipse.papyrus.web.application.representations.view.aql.InstanceOfQuery.instanceOf;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.application.representations.view.CreationToolsUtil;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.papyrus.web.application.representations.view.aql.IfQuery;
import org.eclipse.papyrus.web.application.representations.view.aql.Services;
import org.eclipse.papyrus.web.application.representations.view.aql.Variables;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
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
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Builder of the "Composite Structure Diagram" diagram representation.
 *
 * @author Arthur Daussy
 */
public class CSDDiagramDescriptionBuilder extends AbstractRepresentationDescriptionBuilder {

    public static final String CSD_PREFIX = "CSD_";

    public static final String CSD_REP_NAME = "Composite Structure Diagram";

    public static final String IN_PROPERTY = "InProperty";

    public static final String IN_CLASSIFIER = "InClassifier";

    public static final String SYMBOLS_COMPARTMENT_SUFFIX = "Symbols";

    public static final String SHOW_HIDE = "SHOW_HIDE";

    private static final String AQL_CHECK_TYPED_PROPERTY = "aql:self.oclIsKindOf(uml::Property) and self.type!=null";

    private final UMLPackage pack = UMLPackage.eINSTANCE;

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription csdSharedDescription;

    public CSDDiagramDescriptionBuilder() {
        super(CSD_PREFIX, CSD_REP_NAME, UMLPackage.eINSTANCE.getPackage());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {

        diagramDescription.setPreconditionExpression(CallQuery.queryServiceOnSelf(Services.IS_NOT_PROFILE_MODEL));
        this.createDefaultToolSectionInDiagramDescription(diagramDescription);

        this.createCommentTopNodeDescription(diagramDescription, NODES);
        this.createClassTopNodeDescription(diagramDescription);

        this.csdSharedDescription = this.createSharedDescription(diagramDescription);
        this.createClassSharedDescription(diagramDescription);
        this.createPropertyOnClassifierSharedDescription(diagramDescription);
        this.createPropertyOnPropertySharedDescription(diagramDescription);

        this.createPortDescriptionOnClassifier(diagramDescription);
        this.createPortDescriptionOnProperty(diagramDescription);

        DiagramToolSection showHideToolSection = this.getViewBuilder().createDiagramToolSection(SHOW_HIDE);
        diagramDescription.getPalette().getToolSections().add(showHideToolSection);
        this.createHideSymbolTool(diagramDescription,
                SHOW_HIDE);
        this.createShowSymbolTool(diagramDescription, SHOW_HIDE);
        this.createHideAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);
        this.createShowAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);

        this.createConnectorDescription(diagramDescription);
        this.createUsageDescription(diagramDescription);
        this.createGeneralizationDescription(diagramDescription);

        this.createCommentSubNodeDescription(diagramDescription, this.csdSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.pack.getComment(), SHARED_SUFFIX), List.of(this.pack.getClass_(), this.pack.getProperty()));

        DropNodeTool csdGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getDiagramGraphicalDropToolName());
        List<EClass> children = List.of(this.pack.getClass_(), this.pack.getComment());
        this.registerCallback(diagramDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            csdGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });

        diagramDescription.getPalette().setDropNodeTool(csdGraphicalDropTool);
        List<EClass> symbolOwners = List.of(
                this.pack.getClassifier(),
                this.pack.getProperty());
        this.createSymbolSharedNodeDescription(diagramDescription, this.csdSharedDescription, symbolOwners, List.of(), SYMBOLS_COMPARTMENT_SUFFIX);

        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));
    }

    private NodeDescription createClassSharedDescription(DiagramDescription diagramDescription) {
        EClass classEClass = this.pack.getClass_();
        NodeDescription csdClassifierSharedNodeDescription = this.newNodeBuilder(classEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(classEClass, SHARED_SUFFIX)) //
                .semanticCandidateExpression(queryAttributeOnSelf(this.pack.getClass_NestedClassifier()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(classEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(classEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();

        this.csdSharedDescription.getChildrenDescriptions().add(csdClassifierSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(csdClassifierSharedNodeDescription);

        NodeTool cdClassifierSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.pack.getClass_NestedClassifier(), classEClass);
        List<EClass> owners = List.of(classEClass);
        this.reuseNodeAndCreateTool(csdClassifierSharedNodeDescription, diagramDescription, cdClassifierSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        DropNodeTool cdModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(csdClassifierSharedNodeDescription));
        List<EClass> children = List.of(classEClass, this.pack.getProperty(), this.pack.getComment());
        this.registerCallback(csdClassifierSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cdModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        csdClassifierSharedNodeDescription.getPalette().setDropNodeTool(cdModelGraphicalDropTool);

        return csdClassifierSharedNodeDescription;
    }

    private NodeDescription createPropertyOnClassifierSharedDescription(DiagramDescription diagramDescription) {
        EClass propertyEClass = this.pack.getProperty();
        NodeDescription csdClassifierSharedNodeDescription = this.newNodeBuilder(propertyEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(propertyEClass, IN_CLASSIFIER + UNDERSCORE + SHARED_SUFFIX)) //
                .semanticCandidateExpression(CallQuery.queryOperationOnSelf(this.pack.getClassifier__AllAttributes()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(propertyEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(propertyEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();

        this.csdSharedDescription.getChildrenDescriptions().add(csdClassifierSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(csdClassifierSharedNodeDescription);
        NodeTool cdClassifierSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.pack.getInterface_OwnedAttribute(), propertyEClass);
        List<EClass> owners = List.of(this.pack.getClass_());
        this.reuseNodeAndCreateTool(csdClassifierSharedNodeDescription, diagramDescription, cdClassifierSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        DropNodeTool padModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(csdClassifierSharedNodeDescription));
        List<EClass> children = List.of(this.pack.getProperty(), this.pack.getComment());
        this.registerCallback(csdClassifierSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            padModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        csdClassifierSharedNodeDescription.getPalette().setDropNodeTool(padModelGraphicalDropTool);

        return csdClassifierSharedNodeDescription;
    }

    private void createGeneralizationDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> namedElementCollector = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getClassifier());
        EdgeDescription connectorDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.pack.getGeneralization(),
                this.getQueryBuilder().queryAllReachable(this.pack.getGeneralization()), namedElementCollector, namedElementCollector);
        EdgeStyle style = connectorDescription.getStyle();
        style.setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        diagramDescription.getEdgeDescriptions().add(connectorDescription);

        EdgeTool creationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(connectorDescription, this.pack.getClassifier_Generalization());
        this.registerCallback(connectorDescription, () -> {
            CreationToolsUtil.addEdgeCreationTool(namedElementCollector, creationTool);
        });
    }

    private void createConnectorDescription(DiagramDescription diagramDescription) {

        Supplier<List<NodeDescription>> sourceAndTargets = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getPort(), this.pack.getProperty());

        EdgeDescription connectorDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.pack.getConnector(),
                this.getQueryBuilder().queryAllReachable(this.pack.getConnector()), sourceAndTargets, sourceAndTargets);
        connectorDescription.setBeginLabelExpression(this.getQueryBuilder().createDomainBaseEdgeSourceLabelExpression());
        connectorDescription.setEndLabelExpression(this.getQueryBuilder().createDomainBaseEdgeTargetLabelExpression());
        // Use ConnectorEnd#partWithPort to handle complex Connector edges
        connectorDescription.setPreconditionExpression(new CallQuery(Variables.SELF)//
                .callService("shouldDisplayConnector", Variables.SEMANTIC_EDGE_SOURCE, //
                        Variables.SEMANTIC_EDGE_TARGET, //
                        Variables.GRAPHICAL_EDGE_SOURCE, //
                        Variables.GRAPHICAL_EDGE_TARGET, //
                        Variables.CACHE, //
                        Variables.EDITING_CONTEXT));
        diagramDescription.getEdgeDescriptions().add(connectorDescription);
        EdgeTool creationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(connectorDescription, this.pack.getStructuredClassifier_OwnedConnector());
        this.registerCallback(connectorDescription, () -> {
            CreationToolsUtil.addEdgeCreationTool(sourceAndTargets, creationTool);
        });
    }

    private void createUsageDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> classifierCollector = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getNamedElement());
        EdgeDescription connectorDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.pack.getUsage(),
                this.getQueryBuilder().queryAllReachable(this.pack.getUsage()), classifierCollector, classifierCollector);
        EdgeStyle style = connectorDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        diagramDescription.getEdgeDescriptions().add(connectorDescription);

        EdgeTool creationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(connectorDescription, this.pack.getPackage_PackagedElement());
        this.registerCallback(connectorDescription, () -> {
            CreationToolsUtil.addEdgeCreationTool(classifierCollector, creationTool);
        });
    }

    private void createClassTopNodeDescription(DiagramDescription diagramDescription) {
        EClass classEClass = this.pack.getClass_();
        NodeDescription classTopNodeDescription = this.newNodeBuilder(classEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getDomainNodeName(this.pack.getClassifier())) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachable(classEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(classEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(classEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();

        diagramDescription.getNodeDescriptions().add(classTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(classTopNodeDescription);

        NodeTool creationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), classEClass);
        this.addDiagramToolInToolSection(diagramDescription, creationTool, NODES);

        // Add dropped tool on Model container
        DropNodeTool cdModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(classTopNodeDescription));
        List<EClass> children = List.of(classEClass, this.pack.getComment(), this.pack.getProperty());
        this.registerCallback(classTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cdModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        classTopNodeDescription.getPalette().setDropNodeTool(cdModelGraphicalDropTool);

    }

    private NodeDescription createPortDescriptionOnClassifier(DiagramDescription diagramDescription) {
        EClass portEClass = this.pack.getPort();
        NodeDescription portOnClassifierDescription = this.getViewBuilder().createSpecializedPortUnsynchonizedNodeDescription(IN_CLASSIFIER + UNDERSCORE + SHARED_SUFFIX, portEClass,
                queryOperationOnSelf(this.pack.getClassifier__AllAttributes()));

        this.csdSharedDescription.getBorderNodesDescriptions().add(portOnClassifierDescription);

        this.createDefaultToolSectionsInNodeDescription(portOnClassifierDescription);
        NodeTool cdClassifierSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.pack.getInterface_OwnedAttribute(), portEClass);
        List<EClass> owners = List.of(this.pack.getClass_());
        this.reuseNodeAndCreateTool(portOnClassifierDescription, diagramDescription, cdClassifierSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        return portOnClassifierDescription;
    }

    private NodeDescription createPortDescriptionOnProperty(DiagramDescription diagramDescription) {

        String typeVariable = queryAttributeOnSelf(this.pack.getTypedElement_Type());
        String semanticCandidateExpression = IfQuery.ifExpression(instanceOf(typeVariable, this.pack.getClassifier(), this.getUmlMetaModelHelper()))//
                .then(//
                        new CallQuery(typeVariable)//
                                .callOperation(this.pack.getClassifier__AllAttributes()))
                .orElse("Sequence{}").toQuery();

        EClass portEClass = this.pack.getPort();
        NodeDescription portOnPropertyDescription = this.getViewBuilder().createSpecializedPortUnsynchonizedNodeDescription(IN_PROPERTY + UNDERSCORE + SHARED_SUFFIX, portEClass,
                semanticCandidateExpression);

        this.csdSharedDescription.getBorderNodesDescriptions().add(portOnPropertyDescription);

        this.createDefaultToolSectionsInNodeDescription(portOnPropertyDescription);

        NodeTool portCreationTool = this.getViewBuilder().createCreationTool(this.getIdBuilder().getCreationToolId(this.pack.getPort()), //
                queryAttributeOnSelf(this.pack.getTypedElement_Type()), //
                this.pack.getStructuredClassifier_OwnedAttribute(), //
                this.pack.getPort());
        portCreationTool
                .setPreconditionExpression(AQL_CHECK_TYPED_PROPERTY);

        List<EClass> owners = List.of(this.pack.getProperty());
        List<EClass> forbiddenOwners = List.of(this.pack.getPort());
        this.reuseNodeAndCreateTool(portOnPropertyDescription, diagramDescription, portCreationTool, NODES, owners, forbiddenOwners);

        return portOnPropertyDescription;
    }

    private NodeDescription createPropertyOnPropertySharedDescription(DiagramDescription diagramDescription) {
        EClass propertyEClass = this.pack.getProperty();
        String typeVariable = queryAttributeOnSelf(this.pack.getTypedElement_Type());
        String childrenSemanticCandidateExpression = IfQuery.ifExpression(instanceOf(typeVariable, this.pack.getClassifier(), this.getUmlMetaModelHelper())) //
                .then(new CallQuery(typeVariable).callOperation(this.pack.getClassifier__AllAttributes())) //
                .orElse("Sequence{}").toQuery();

        NodeDescription propertyOnPropertyDescription = this.getViewBuilder().createSpecializedUnsynchonizedNodeDescription(this.pack.getProperty(), childrenSemanticCandidateExpression,
                IN_PROPERTY + UNDERSCORE + SHARED_SUFFIX);

        this.csdSharedDescription.getChildrenDescriptions().add(propertyOnPropertyDescription);

        this.createDefaultToolSectionsInNodeDescription(propertyOnPropertyDescription);
        String propertyTypeVariable = IfQuery.ifExpression("self.oclIsKindOf(uml::Property) and self.type!=null")
                .then(queryAttributeOnSelf(this.pack.getTypedElement_Type()))
                .orElse(Variables.SELF).toQuery();
        NodeTool propertyOnPropertyCreationTool = this.getViewBuilder().createCreationTool(this.getIdBuilder().getCreationToolId(propertyEClass), propertyTypeVariable,
                this.pack.getStructuredClassifier_OwnedAttribute(), propertyEClass);
        propertyOnPropertyCreationTool
                .setPreconditionExpression(AQL_CHECK_TYPED_PROPERTY);

        List<EClass> owners = List.of(this.pack.getProperty());
        List<EClass> forbidenOwners = List.of(this.pack.getPort());

        this.reuseNodeAndCreateTool(propertyOnPropertyDescription, diagramDescription, propertyOnPropertyCreationTool, NODES, owners, forbidenOwners);

        DropNodeTool cdModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(propertyOnPropertyDescription));
        List<EClass> children = List.of(this.pack.getComment(), this.pack.getProperty());
        this.registerCallback(propertyOnPropertyDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cdModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        propertyOnPropertyDescription.getPalette().setDropNodeTool(cdModelGraphicalDropTool);

        return propertyOnPropertyDescription;
    }

}

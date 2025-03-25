/*****************************************************************************
 * Copyright (c) 2022, 2025 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUËTE-GIRAUD (Artal Technologies) - titouan.bouete-giraud@artal.fr - Issue 200, Issue 203
 *  Aurelien Didier (Artal Technologies) - Issue 199, Issue 190
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.CACHE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DIAGRAM_CONTEXT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDGE_SOURCE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDGE_TARGET;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDITING_CONTEXT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.GRAPHICAL_EDGE_SOURCE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.GRAPHICAL_EDGE_TARGET;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_EDGE_SOURCE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_EDGE_TARGET;

import com.google.common.base.Predicate;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.uml.domain.services.EMFUtils;
import org.eclipse.papyrus.web.application.representations.view.CreationToolsUtil;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.papyrus.web.application.representations.view.aql.Services;
import org.eclipse.papyrus.web.application.representations.view.builders.CallbackAdapter;
import org.eclipse.papyrus.web.application.representations.view.builders.ViewBuilder;
import org.eclipse.sirius.components.view.ChangeContext;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.ListLayoutStrategyDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Builder of the "Class Diagram " diagram representation.
 *
 * @author Arthur Daussy
 */
public final class CDDiagramDescriptionBuilder extends AbstractRepresentationDescriptionBuilder {

    public static final String ATTRIBUTES_COMPARTMENT_SUFFIX = "Attributes";

    public static final String LITERAL_COMPARTMENT_SUFFIX = "Literals";

    public static final String NESTED_CLASSIFIERS_COMPARTMENT_SUFFIX = "NestedClassifiers";

    public static final String OPERATIONS_COMPARTMENT_SUFFIX = "Operations";

    public static final String RECEPTION_COMPARTMENT_SUFFIX = "Receptions";

    public static final String CD_REP_NAME = "Class Diagram";

    public static final String CD_PREFIX = "CD_";

    private static final String NEW_CONTAINMENT_LINK_TOOL_LABEL = "New Containment Link";

    public static final String CLASSIFIER_CONTAINMENT_LINK_EDGE_ID = CD_PREFIX + "_ClassifierContainmentLink_FeatureEdge";

    public static final String PACKAGE_CONTAINMENT_LINK_EDGE_ID = CD_PREFIX + "_PackageContainmentLink_FeatureEdge";

    /**
     * AQL expression to set children not draggable from its container.
     */
    private static final String CHILD_NOT_DRAGGABLE_EXPRESSION = "aql:false";

    private final UMLPackage pack = UMLPackage.eINSTANCE;

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription cdSharedDescription;

    public CDDiagramDescriptionBuilder() {
        super(CD_PREFIX, CD_REP_NAME, UMLPackage.eINSTANCE.getPackage());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {

        diagramDescription.setPreconditionExpression(CallQuery.queryServiceOnSelf(Services.IS_NOT_PROFILE_MODEL));
        this.createDefaultToolSectionInDiagramDescription(diagramDescription);

        this.createModelTopNodeDescription(diagramDescription);
        this.createPackageTopNodeDescription(diagramDescription);

        this.createClassTopNodeDescription(diagramDescription);
        this.createInterfaceTopDescription(diagramDescription);
        this.createPrimitiveTypeTopNodeDescription(diagramDescription);
        this.createDataTypeTopNodeDescription(diagramDescription);
        this.createEnumerationTopNodeDescription(diagramDescription);
        this.createSignalTopNodeDescription(diagramDescription);
        this.createCommentTopNodeDescription(diagramDescription, NODES);
        this.createConstraintTopNodeDescription(diagramDescription, NODES);

        // create shared node descriptions with their tools
        this.cdSharedDescription = this.createSharedDescription(diagramDescription);

        this.createAttributeSharedNodeDescription(diagramDescription);
        this.createOperationSharedNodeDescription(diagramDescription);
        this.createNestedClassifierSharedNodeDescription(diagramDescription);
        this.createEnumerationLiteralSharedNodeDescription(diagramDescription);
        this.createReceptionSharedNodeDescription(diagramDescription);

        this.createCommentSubNodeDescription(diagramDescription, this.cdSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.pack.getComment(), SHARED_SUFFIX), List.of(this.pack.getPackage()));
        this.createConstraintSubNodeDescription(diagramDescription, this.cdSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.pack.getConstraint(), SHARED_SUFFIX), List.of(this.pack.getPackage()));

        // create share sub node
        this.createClassifierSharedNodeDescription(diagramDescription, this.pack.getClass_());
        this.createClassifierSharedNodeDescription(diagramDescription, this.pack.getInterface());
        this.createClassifierSharedNodeDescription(diagramDescription, this.pack.getPrimitiveType());
        this.createClassifierSharedNodeDescription(diagramDescription, this.pack.getDataType());
        this.createClassifierSharedNodeDescription(diagramDescription, this.pack.getEnumeration());
        this.createClassifierSharedNodeDescription(diagramDescription, this.pack.getSignal());
        this.createPackageSharedNodeDescription(diagramDescription);
        this.createModelSharedNodeDescription(diagramDescription);
        // create shared compartments
        this.createCompartmentForClassSharedNodeDescription(diagramDescription, ATTRIBUTES_COMPARTMENT_SUFFIX);
        this.createCompartmentForClassSharedNodeDescription(diagramDescription, OPERATIONS_COMPARTMENT_SUFFIX);
        this.createCompartmentForClassSharedNodeDescription(diagramDescription, RECEPTION_COMPARTMENT_SUFFIX);
        this.createCompartmentForClassSharedNodeDescription(diagramDescription, NESTED_CLASSIFIERS_COMPARTMENT_SUFFIX);

        this.createCompartmentForInterfaceSharedNodeDescription(diagramDescription, ATTRIBUTES_COMPARTMENT_SUFFIX);
        this.createCompartmentForInterfaceSharedNodeDescription(diagramDescription, OPERATIONS_COMPARTMENT_SUFFIX);
        this.createCompartmentForInterfaceSharedNodeDescription(diagramDescription, NESTED_CLASSIFIERS_COMPARTMENT_SUFFIX);

        // Reception on Interface is not implement yet on Papyrus Desktop.
        // Uncomment this once to make it available again.
        // this.createCompartmentForInterfaceSharedNodeDescription(diagramDescription, RECEPTION_COMPARTMENT_SUFFIX);

        this.createCompartmentForDataTypeSharedNodeDescription(diagramDescription, ATTRIBUTES_COMPARTMENT_SUFFIX);
        this.createCompartmentForDataTypeSharedNodeDescription(diagramDescription, OPERATIONS_COMPARTMENT_SUFFIX);

        this.createCompartmentForPrimitiveTypeSharedNodeDescription(diagramDescription, ATTRIBUTES_COMPARTMENT_SUFFIX);
        this.createCompartmentForPrimitiveTypeSharedNodeDescription(diagramDescription, OPERATIONS_COMPARTMENT_SUFFIX);

        this.createCompartmentForEnumerationSharedNodeDescription(diagramDescription, LITERAL_COMPARTMENT_SUFFIX);

        this.createCompartmentForSignalSharedNodeDescription(diagramDescription, ATTRIBUTES_COMPARTMENT_SUFFIX);

        // create edge descriptions with their tools
        this.createPackageMergeDescription(diagramDescription);
        this.createPackageImportDescription(diagramDescription);
        this.createAbstractionDescription(diagramDescription);
        this.createDependencyDescription(diagramDescription);
        this.createInterfaceRealizationDescription(diagramDescription);
        this.createGeneralizationDescription(diagramDescription);
        this.createAssociationDescription(diagramDescription);
        this.createUsageDescription(diagramDescription);
        this.createClassifierContainmentLink(diagramDescription);
        this.createPackageContainmentLink(diagramDescription);

        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));

        DropNodeTool cddGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getDiagramGraphicalDropToolName());
        List<EClass> children = List.of(this.pack.getModel(), this.pack.getPackage(), this.pack.getComment(), this.pack.getConstraint(), this.pack.getClass_(), this.pack.getInterface(),
                this.pack.getDataType(), this.pack.getEnumeration(), this.pack.getSignal());
        this.registerCallback(diagramDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cddGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        diagramDescription.getPalette().setDropNodeTool(cddGraphicalDropTool);
    }

    private void createClassTopNodeDescription(DiagramDescription diagramDescription) {
        EClass classEClass = this.pack.getClass_();
        NodeDescription classTopNodeDescription = this.newNodeBuilder(classEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createListLayoutStrategyDescription())//
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachable(classEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(classEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(classEClass.getName())) //
                .build();

        diagramDescription.getNodeDescriptions().add(classTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(classTopNodeDescription);

        NodeTool creationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), classEClass);
        this.addDiagramToolInToolSection(diagramDescription, creationTool, NODES);
    }

    private void createDataTypeTopNodeDescription(DiagramDescription diagramDescription) {

        EClass dataTypeEClass = this.pack.getDataType();
        NodeDescription dataTypeTopNodeDescription = this.newNodeBuilder(dataTypeEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createListLayoutStrategyDescription())//
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachable(dataTypeEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .labelEditTool(this.getViewBuilder().createDirectEditTool(dataTypeEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(dataTypeEClass.getName())) //
                .build();

        diagramDescription.getNodeDescriptions().add(dataTypeTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(dataTypeTopNodeDescription);

        NodeTool creationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), dataTypeEClass);
        this.addDiagramToolInToolSection(diagramDescription, creationTool, NODES);
    }

    private void createEnumerationTopNodeDescription(DiagramDescription diagramDescription) {
        EClass enumerationEClass = this.pack.getEnumeration();
        NodeDescription enumerationLiterals = this.newNodeBuilder(enumerationEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createListLayoutStrategyDescription())//
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachable(enumerationEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(enumerationEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(enumerationEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        diagramDescription.getNodeDescriptions().add(enumerationLiterals);

        this.createDefaultToolSectionsInNodeDescription(enumerationLiterals);

        NodeTool creationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), enumerationEClass);
        this.addDiagramToolInToolSection(diagramDescription, creationTool, NODES);
    }

    private void createInterfaceTopDescription(DiagramDescription diagramDescription) {

        EClass interfaceEClass = this.pack.getInterface();
        NodeDescription interfaceDescription = this.newNodeBuilder(interfaceEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createListLayoutStrategyDescription())//
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachable(interfaceEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(interfaceEClass.getName()))//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(interfaceEClass.getName())) //
                .build();

        diagramDescription.getNodeDescriptions().add(interfaceDescription);

        this.createDefaultToolSectionsInNodeDescription(interfaceDescription);

        NodeTool creationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), interfaceEClass);
        this.addDiagramToolInToolSection(diagramDescription, creationTool, NODES);

    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Model} on the Diagram.
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createModelTopNodeDescription(DiagramDescription diagramDescription) {
        EClass modelEClass = this.pack.getModel();
        NodeDescription cdModelTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(modelEClass, this.getQueryBuilder().queryAllReachableExactType(modelEClass));
        diagramDescription.getNodeDescriptions().add(cdModelTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cdModelTopNodeDescription);

        NodeTool cdProfileTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), modelEClass);
        this.addDiagramToolInToolSection(diagramDescription, cdProfileTopNodeCreationTool, NODES);

        // Add dropped tool on Model container
        DropNodeTool cdModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cdModelTopNodeDescription));
        List<EClass> children = List.of(this.pack.getClass_(), this.pack.getSignal(), this.pack.getDataType(), this.pack.getPrimitiveType(), this.pack.getEnumeration(), this.pack.getComment(),
                this.pack.getConstraint(),
                this.pack.getInterface(), this.pack.getModel(), this.pack.getPackage());
        this.registerCallback(cdModelTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cdModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cdModelTopNodeDescription.getPalette().setDropNodeTool(cdModelGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Package} on the Diagram.
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageTopNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.pack.getPackage();
        NodeDescription cdPackageTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                this.getQueryBuilder().queryAllReachableExactType(this.pack.getPackage()));
        diagramDescription.getNodeDescriptions().add(cdPackageTopNodeDescription);

        cdPackageTopNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        // create Package tool sections
        this.createDefaultToolSectionsInNodeDescription(cdPackageTopNodeDescription);

        NodeTool cdPackageTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), packageEClass);
        this.addDiagramToolInToolSection(diagramDescription, cdPackageTopNodeCreationTool, NODES);

        // No direct children for Package: the NodeDescriptions it can contain are all defined as shared descriptions.

        // Add dropped tool on Package container
        DropNodeTool cdPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cdPackageTopNodeDescription));
        List<EClass> children = List.of(this.pack.getClass_(), this.pack.getSignal(), this.pack.getDataType(), this.pack.getPrimitiveType(), this.pack.getEnumeration(), this.pack.getComment(),
                this.pack.getConstraint(),
                this.pack.getInterface(), this.pack.getModel(), this.pack.getPackage());
        this.registerCallback(cdPackageTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cdPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cdPackageTopNodeDescription.getPalette().setDropNodeTool(cdPackageGraphicalDropTool);
    }

    private void createPrimitiveTypeTopNodeDescription(DiagramDescription diagramDescription) {

        EClass primitiveTypeEClass = this.pack.getPrimitiveType();
        NodeDescription primitiveTypeDescription = this.newNodeBuilder(primitiveTypeEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createListLayoutStrategyDescription())//
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachable(primitiveTypeEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(primitiveTypeEClass.getName()))//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(primitiveTypeEClass.getName())) //
                .build();

        diagramDescription.getNodeDescriptions().add(primitiveTypeDescription);

        this.createDefaultToolSectionsInNodeDescription(primitiveTypeDescription);

        NodeTool creationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), primitiveTypeEClass);
        this.addDiagramToolInToolSection(diagramDescription, creationTool, NODES);
    }

    private void createSignalTopNodeDescription(DiagramDescription diagramDescription) {

        EClass signalEClass = this.pack.getSignal();
        NodeDescription signalDescription = this.newNodeBuilder(signalEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createListLayoutStrategyDescription())//
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachable(signalEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(signalEClass.getName()))//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(signalEClass.getName())) //
                .build();

        diagramDescription.getNodeDescriptions().add(signalDescription);

        this.createDefaultToolSectionsInNodeDescription(signalDescription);

        NodeTool creationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), signalEClass);
        this.addDiagramToolInToolSection(diagramDescription, creationTool, NODES);

    }

    /**
     * Creates a shared compartment reused by <i>Class</i> {@link NodeDescription}.
     * <p>
     * The created {@link NodeDescription} compartment is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * <p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param compartmentName
     *         the name of the compartment to create
     */
    private void createCompartmentForClassSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass classEClass = this.pack.getClass_();
        List<EClass> owners = List.of(classEClass);
        List<EClass> forbiddenOwners = List.of();
        NodeDescription cdCompartmentForClassSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.cdSharedDescription, classEClass, compartmentName, owners,
                forbiddenOwners, nodeDescription -> nodeDescription != null);

        // Add Graphical dropped tool on Shared Compartment for Class
        this.addDropToolOnSharedCompartment(diagramDescription, compartmentName, cdCompartmentForClassSharedNodeDescription);
    }

    /**
     * Creates a shared compartment reused by <i>DataType</i> {@link NodeDescription}.
     * <p>
     * The created {@link NodeDescription} compartment is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * <p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param compartmentName
     *         the name of the compartment to create
     */
    private void createCompartmentForDataTypeSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass dataTypeEClass = this.pack.getDataType();
        List<EClass> owners = List.of(dataTypeEClass);
        List<EClass> forbiddenOwners = List.of(this.pack.getEnumeration(), this.pack.getPrimitiveType());
        NodeDescription cdCompartmentForDataTypeSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.cdSharedDescription, dataTypeEClass, compartmentName,
                owners,
                forbiddenOwners, nodeDescription -> nodeDescription != null);

        // Add Graphical dropped tool on Shared Compartment for Class
        this.addDropToolOnSharedCompartment(diagramDescription, compartmentName, cdCompartmentForDataTypeSharedNodeDescription);
    }

    /**
     * Creates a shared compartment reused by <i>DataType</i> {@link NodeDescription}.
     * <p>
     * The created {@link NodeDescription} compartment is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * <p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param compartmentName
     *         the name of the compartment to create
     * @return
     */
    private NodeDescription createCompartmentForEnumerationSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass enumerationEClass = this.pack.getEnumeration();
        List<EClass> owners = List.of(enumerationEClass);
        NodeDescription cdCompartmentForEnumerationSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.cdSharedDescription, enumerationEClass, compartmentName,
                owners,
                List.of(), nodeDescription -> nodeDescription != null);

        return cdCompartmentForEnumerationSharedNodeDescription;
    }

    /**
     * Creates a shared compartment reused by <i>DataType</i> {@link NodeDescription}.
     * <p>
     * The created {@link NodeDescription} compartment is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * <p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param compartmentName
     *         the name of the compartment to create
     */
    private void createCompartmentForInterfaceSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass interfaceEClass = this.pack.getInterface();
        List<EClass> owners = List.of(interfaceEClass);
        List<EClass> forbiddenOwners = List.of();
        NodeDescription cdCompartmentForDataTypeSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.cdSharedDescription, interfaceEClass, compartmentName,
                owners,
                forbiddenOwners, nodeDescription -> nodeDescription != null);

        // Add Graphical dropped tool on Shared Compartment for Class
        this.addDropToolOnSharedCompartment(diagramDescription, compartmentName, cdCompartmentForDataTypeSharedNodeDescription);
    }

    /**
     * Creates a shared compartment reused by <i>DataType</i> {@link NodeDescription}.
     * <p>
     * The created {@link NodeDescription} compartment is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * <p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param compartmentName
     *         the name of the compartment to create
     */
    private void createCompartmentForPrimitiveTypeSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass primitiveTypeEClass = this.pack.getPrimitiveType();
        List<EClass> owners = List.of(primitiveTypeEClass);
        List<EClass> forbiddenOwners = List.of();
        NodeDescription cdCompartmentForDataTypeSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.cdSharedDescription, primitiveTypeEClass, compartmentName,
                owners,
                forbiddenOwners, nodeDescription -> nodeDescription != null);

        // Add Graphical dropped tool on Shared Compartment for Class
        this.addDropToolOnSharedCompartment(diagramDescription, compartmentName, cdCompartmentForDataTypeSharedNodeDescription);
    }

    /**
     * Creates a shared compartment reused by <i>Class</i> {@link NodeDescription}.
     * <p>
     * The created {@link NodeDescription} compartment is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * <p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param compartmentName
     *         the name of the compartment to create
     */
    private void createCompartmentForSignalSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass signalEClass = this.pack.getSignal();
        List<EClass> owners = List.of(signalEClass);
        List<EClass> forbiddenOwners = List.of();
        NodeDescription cdCompartmentForClassSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.cdSharedDescription, signalEClass, compartmentName, owners,
                forbiddenOwners, nodeDescription -> nodeDescription != null);

        // Add Graphical dropped tool on Shared Compartment for Class
        this.addDropToolOnSharedCompartment(diagramDescription, compartmentName, cdCompartmentForClassSharedNodeDescription);
    }

    /**
     * Creates a <i>Property</i> child reused by <i>Attributes</i> compartments.
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createAttributeSharedNodeDescription(DiagramDescription diagramDescription) {
        List<EClass> owners = List.of(this.pack.getClass_(), this.pack.getDataType(), this.pack.getPrimitiveType(), this.pack.getInterface(), this.pack.getSignal());
        List<EClass> forbiddenOwners = List.of();
        NodeDescription attributeInCompartmentSharedNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, this.cdSharedDescription,
                this.pack.getProperty(), ATTRIBUTES_COMPARTMENT_SUFFIX, CallQuery.queryOperationOnSelf(this.pack.getClassifier__GetAllAttributes()),
                this.pack.getInterface_OwnedAttribute(), owners, forbiddenOwners, nodeDescription -> nodeDescription != null);
        attributeInCompartmentSharedNodeDescription.setName(attributeInCompartmentSharedNodeDescription.getName() + UNDERSCORE + SHARED_SUFFIX);
    }

    /**
     * Creates a shared list {@link NodeDescription} representing {@link Classifier} sub-classes.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param classifierEClass
     *         the classifier sub-type to represent
     * @return the created {@link NodeDescription}
     */
    private NodeDescription createClassifierSharedNodeDescription(DiagramDescription diagramDescription, EClass classifierEClass) {
        ListLayoutStrategyDescription listLayoutStrategyDescription = DiagramFactory.eINSTANCE.createListLayoutStrategyDescription();
        listLayoutStrategyDescription.setAreChildNodesDraggableExpression(CHILD_NOT_DRAGGABLE_EXPRESSION);
        NodeDescription cdClassifierSharedNodeDescription = this.newNodeBuilder(classifierEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(classifierEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(listLayoutStrategyDescription)//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.pack.getPackage_PackagedElement()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(classifierEClass.getName()))//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(classifierEClass.getName())) //
                .build();
        this.cdSharedDescription.getChildrenDescriptions().add(cdClassifierSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cdClassifierSharedNodeDescription);

        NodeTool cdClassifierSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), classifierEClass);
        List<EClass> owners = List.of(this.pack.getPackage());
        this.reuseNodeAndCreateTool(cdClassifierSharedNodeDescription, diagramDescription, cdClassifierSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));
        return cdClassifierSharedNodeDescription;
    }

    /**
     * Creates a <i>Property</i> child reused by <i>Attributes</i> compartments.
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createEnumerationLiteralSharedNodeDescription(DiagramDescription diagramDescription) {
        List<EClass> owners = List.of(this.pack.getEnumeration());
        List<EClass> forbiddenOwners = List.of();
        NodeDescription enumerationLitenralSharedNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, this.cdSharedDescription,
                this.pack.getEnumerationLiteral(), LITERAL_COMPARTMENT_SUFFIX, CallQuery.queryAttributeOnSelf(this.pack.getEnumeration_OwnedLiteral()),
                this.pack.getEnumeration_OwnedLiteral(), owners, forbiddenOwners, nodeDescription -> nodeDescription != null);
        enumerationLitenralSharedNodeDescription.setName(enumerationLitenralSharedNodeDescription.getName() + UNDERSCORE + SHARED_SUFFIX);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Model}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createModelSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass modelEClass = this.pack.getModel();
        NodeDescription cdModelSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(modelEClass,
                CallQuery.queryAttributeOnSelf(this.pack.getPackage_PackagedElement()));
        cdModelSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(modelEClass, SHARED_SUFFIX));
        cdModelSharedNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        this.cdSharedDescription.getChildrenDescriptions().add(cdModelSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cdModelSharedNodeDescription);

        NodeTool cdModelSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), modelEClass);
        List<EClass> owners = List.of(this.pack.getPackage(), this.pack.getModel());
        this.reuseNodeAndCreateTool(cdModelSharedNodeDescription, diagramDescription, cdModelSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Package container
        DropNodeTool cdModelGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cdModelSharedNodeDescription));
        List<EClass> children = List.of(this.pack.getClass_(), this.pack.getInterface(), this.pack.getDataType(), this.pack.getEnumeration(), this.pack.getPrimitiveType(), this.pack.getComment(),
                this.pack.getConstraint(), this.pack.getDataType(), this.pack.getPackage(), this.pack.getModel(), this.pack.getSignal());
        this.registerCallback(cdModelSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cdModelGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cdModelSharedNodeDescription.getPalette().setDropNodeTool(cdModelGraphicalDropTool);
    }

    /**
     * Creates a <i>Property</i> child reused by <i>Attributes</i> compartments.
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createNestedClassifierSharedNodeDescription(DiagramDescription diagramDescription) {
        List<EClass> owners = List.of(this.pack.getClass_(), this.pack.getInterface());
        List<EClass> forbiddenOwners = List.of();
        NodeDescription nestedClassifierInCompartmentSharedNodeDescription = this.createNestedClassifierSubNodeDescriptionInCompartmentDescription(diagramDescription, this.cdSharedDescription,
                this.pack.getClassifier(), NESTED_CLASSIFIERS_COMPARTMENT_SUFFIX, CallQuery.queryAttributeOnSelf(this.pack.getInterface_NestedClassifier()),
                this.pack.getInterface_NestedClassifier(), owners, forbiddenOwners, nodeDescription -> nodeDescription != null);
        nestedClassifierInCompartmentSharedNodeDescription.setName(nestedClassifierInCompartmentSharedNodeDescription.getName() + UNDERSCORE + SHARED_SUFFIX);

    }

    /**
     * Creates a <i>Operation</i> child reused by <i>Operations</i> compartments.
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createOperationSharedNodeDescription(DiagramDescription diagramDescription) {
        List<EClass> owners = List.of(this.pack.getClass_(), this.pack.getDataType(), this.pack.getPrimitiveType(), this.pack.getInterface());
        List<EClass> forbiddenOwners = List.of();
        NodeDescription operationInCompartmentSharedNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, this.cdSharedDescription,
                this.pack.getOperation(), OPERATIONS_COMPARTMENT_SUFFIX, CallQuery.queryOperationOnSelf(this.pack.getClassifier__GetAllOperations()),
                this.pack.getClass_OwnedOperation(), owners, forbiddenOwners, nodeDescription -> nodeDescription != null);
        operationInCompartmentSharedNodeDescription.setName(operationInCompartmentSharedNodeDescription.getName() + UNDERSCORE + SHARED_SUFFIX);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Package}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.pack.getPackage();
        NodeDescription cdPackageSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                CallQuery.queryAttributeOnSelf(this.pack.getPackage_PackagedElement()));
        cdPackageSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(packageEClass, SHARED_SUFFIX));
        cdPackageSharedNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        this.cdSharedDescription.getChildrenDescriptions().add(cdPackageSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(cdPackageSharedNodeDescription);

        NodeTool cdPackageSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), packageEClass);
        List<EClass> owners = List.of(this.pack.getPackage());
        this.reuseNodeAndCreateTool(cdPackageSharedNodeDescription, diagramDescription, cdPackageSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Package container
        DropNodeTool cdPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cdPackageSharedNodeDescription));
        List<EClass> children = List.of(this.pack.getClass_(), this.pack.getInterface(), this.pack.getDataType(), this.pack.getEnumeration(), this.pack.getPrimitiveType(), this.pack.getComment(),
                this.pack.getConstraint(), this.pack.getDataType(), this.pack.getPackage(), this.pack.getModel(), this.pack.getSignal());
        this.registerCallback(cdPackageSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            cdPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cdPackageSharedNodeDescription.getPalette().setDropNodeTool(cdPackageGraphicalDropTool);
    }

    /**
     * Creates a <i>Property</i> child reused by <i>Attributes</i> compartments.
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createReceptionSharedNodeDescription(DiagramDescription diagramDescription) {
        List<EClass> owners = List.of(this.pack.getClass_());
        List<EClass> forbiddenOwners = List.of();
        NodeDescription enumerationLitenralSharedNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, this.cdSharedDescription,
                this.pack.getReception(), RECEPTION_COMPARTMENT_SUFFIX, CallQuery.queryAttributeOnSelf(this.pack.getInterface_OwnedReception()),
                this.pack.getInterface_OwnedReception(), owners, forbiddenOwners, nodeDescription -> nodeDescription != null);
        enumerationLitenralSharedNodeDescription.setName(enumerationLitenralSharedNodeDescription.getName() + UNDERSCORE + SHARED_SUFFIX);
    }

    /**
     * Creates a {@link NodeDescription} reused in a {@link NodeDescription} compartment.
     * <p>
     * The created {@link NodeDescription} is added to the provided {@code parentNodeDescription} {@link NodeDescription} and reused by the {@code owners} {@link NodeDescription}s.
     * <p>
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param parentNodeDescription
     *         the {@link NodeDescription} used to contain the created {@link NodeDescription}
     * @param domainType
     *         the domain type used to define the new {@link NodeDescription}
     * @param compartmentName
     *         the name of the compartment which contain the child {@link NodeDescription} to create
     * @param semanticQuery
     *         the semantic candidate expression to get semantic element
     * @param semanticRefTool
     *         the containment reference to used for the creation
     * @param owners
     *         the semantic types that can contain this {@link NodeDescription}
     * @param forbiddenOwners
     *         the list of domain types to exclude
     * @param forbiddenNodeDescriptionPredicate
     *         predicate on the {@link NodeDescription} to exclude
     * @return the created {@link NodeDescription}
     */
    // CHECKSTYLE:OFF
    protected NodeDescription createNestedClassifierSubNodeDescriptionInCompartmentDescription(DiagramDescription diagramDescription, NodeDescription parentNodeDescription, EClass domainType,
            String compartmentName,
            String semanticQuery, EReference semanticRefTool, List<EClass> owners, List<EClass> forbiddenOwners, Predicate<NodeDescription> forbiddenNodeDescriptionPredicate) {
        // CHECKSTYLE:ON
        String nodeDescriptionName = this.getIdBuilder().getDomainNodeName(domainType);

        InsideLabelDescription insideLabelDescription = DiagramFactory.eINSTANCE.createInsideLabelDescription();
        insideLabelDescription.setLabelExpression(CallQuery.queryServiceOnSelf(Services.RENDER_LABEL_ONE_LINE, "false", "true"));
        insideLabelDescription.setTextAlign(LabelTextAlign.LEFT);
        insideLabelDescription.setPosition(InsideLabelPosition.MIDDLE_LEFT);
        InsideLabelStyle style = this.getViewBuilder().createDefaultInsideLabelStyleIcon();
        insideLabelDescription.setStyle(style);

        NodeDescription createNodeDescriptionInCompartmentDescription = this.newNodeBuilder(domainType, DiagramFactory.eINSTANCE.createIconLabelNodeStyleDescription())//
                .name(nodeDescriptionName) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createListLayoutStrategyDescription())//
                .semanticCandidateExpression(semanticQuery)//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(domainType.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(domainType.getName())) //
                .insideLabelDescription(insideLabelDescription)
                .build();
        parentNodeDescription.getChildrenDescriptions().add(createNodeDescriptionInCompartmentDescription);

        // Tool used to create Node Description in Compartment from the compartment
        NodeTool classNodeDescriptionCreationTool = this.getViewBuilder().createCreationTool(this.pack.getInterface_NestedClassifier(), this.pack.getClass_());
        NodeTool dataTypeNodeDescriptionCreationTool = this.getViewBuilder().createCreationTool(this.pack.getInterface_NestedClassifier(), this.pack.getDataType());
        NodeTool enumerationNodeDescriptionCreationTool = this.getViewBuilder().createCreationTool(semanticRefTool, this.pack.getEnumeration());
        NodeTool primitiveTypeNodeDescriptionCreationTool = this.getViewBuilder().createCreationTool(semanticRefTool, this.pack.getPrimitiveType());
        NodeTool interfaceNodeDescriptionCreationTool = this.getViewBuilder().createCreationTool(semanticRefTool, this.pack.getInterface());
        NodeTool signalNodeDescriptionCreationTool = this.getViewBuilder().createCreationTool(semanticRefTool, this.pack.getSignal());
        this.registerCallback(createNodeDescriptionInCompartmentDescription, () -> {
            List<NodeDescription> ownerCompartmentNodeDescriptions = EMFUtils.allContainedObjectOfType(diagramDescription, NodeDescription.class) //
                    .filter(node -> IdBuilder.isCompartmentNode(node) && node.getName().contains(compartmentName)) //
                    .toList();
            this.addNodeToolInToolSection(ownerCompartmentNodeDescriptions, classNodeDescriptionCreationTool, NODES);
            this.addNodeToolInToolSection(ownerCompartmentNodeDescriptions, dataTypeNodeDescriptionCreationTool, NODES);
            this.addNodeToolInToolSection(ownerCompartmentNodeDescriptions, enumerationNodeDescriptionCreationTool, NODES);
            this.addNodeToolInToolSection(ownerCompartmentNodeDescriptions, primitiveTypeNodeDescriptionCreationTool, NODES);
            this.addNodeToolInToolSection(ownerCompartmentNodeDescriptions, interfaceNodeDescriptionCreationTool, NODES);
            this.addNodeToolInToolSection(ownerCompartmentNodeDescriptions, signalNodeDescriptionCreationTool, NODES);
            this.reusedNodeDescriptionInOwners(createNodeDescriptionInCompartmentDescription, ownerCompartmentNodeDescriptions);
        });

        // TODO: See if we need to reenable this with custo
        // Tool used to create node Node Description in Compartment from the parent of this compartment
        // NodeTool cdSharedNodeDescriptionInCompartmentCreationTool =
        // this.getViewBuilder().createInCompartmentCreationTool(this.getIdBuilder().getCreationToolId(domainType),
        // compartmentName,
        // semanticRefTool, domain);
        // this.reuseTool(createNodeDescriptionInCompartmentDescription, diagramDescription,
        // cdSharedNodeDescriptionInCompartmentCreationTool, owners, forbiddenOwners,
        // forbiddenNodeDescriptionPredicate);

        return createNodeDescriptionInCompartmentDescription;
    }

    private void createAbstractionDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> namedElementDescriptions = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getNamedElement());
        EdgeDescription cdAbstraction = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.pack.getAbstraction(),
                this.getQueryBuilder().queryAllReachableExactType(this.pack.getAbstraction()), namedElementDescriptions, namedElementDescriptions);
        cdAbstraction.getStyle().setLineStyle(LineStyle.DASH);
        cdAbstraction.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        EdgeTool creationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cdAbstraction, this.pack.getPackage_PackagedElement());
        this.registerCallback(cdAbstraction, () -> {
            CreationToolsUtil.addEdgeCreationTool(namedElementDescriptions, creationTool);
        });
        diagramDescription.getEdgeDescriptions().add(cdAbstraction);

        this.getViewBuilder().addDefaultReconnectionTools(cdAbstraction);
    }

    private void createAssociationDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceAndTargetDescriptionsSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getClassifier());

        EClass association = this.pack.getAssociation();
        EdgeDescription cdAssociation = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(association, this.getQueryBuilder().queryAllReachableExactType(association),
                sourceAndTargetDescriptionsSupplier, sourceAndTargetDescriptionsSupplier);
        cdAssociation.getStyle().setLineStyle(LineStyle.SOLID);
        cdAssociation.getStyle().setTargetArrowStyle(ArrowStyle.NONE);
        cdAssociation.getStyle().setSourceArrowStyle(ArrowStyle.NONE);

        EdgeTool associationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cdAssociation, this.pack.getPackage_PackagedElement());
        EdgeTool compositeAssociationTool = this.createSpecializedAssociationDomainBasedEdgeTool("New Composite Association", "Association_composite",
                ClassDiagramServices.CREATION_COMPOSITE_ASSOCIATION, cdAssociation);
        EdgeTool sharedAssociationTool = this.createSpecializedAssociationDomainBasedEdgeTool("New Shared Association", "Association_shared", ClassDiagramServices.CREATION_SHARED_ASSOCIATION,
                cdAssociation);
        this.registerCallback(cdAssociation, () -> {
            CreationToolsUtil.addEdgeCreationTool(sourceAndTargetDescriptionsSupplier, associationTool);
            CreationToolsUtil.addEdgeCreationTool(sourceAndTargetDescriptionsSupplier, compositeAssociationTool);
            CreationToolsUtil.addEdgeCreationTool(sourceAndTargetDescriptionsSupplier, sharedAssociationTool);
        });

        cdAssociation.setBeginLabelExpression(this.getQueryBuilder().createDomainBaseEdgeSourceLabelExpression());
        cdAssociation.getPalette().setBeginLabelEditTool(this.getViewBuilder().createDirectEditTool(CallQuery.queryServiceOnSelf(ClassDiagramServices.GET_ASSOCIATION_TARGET)));

        cdAssociation.setEndLabelExpression(this.getQueryBuilder().createDomainBaseEdgeTargetLabelExpression());
        cdAssociation.getPalette().setEndLabelEditTool(this.getViewBuilder().createDirectEditTool(CallQuery.queryServiceOnSelf(ClassDiagramServices.GET_ASSOCIATION_SOURCE)));

        // Can be improve once https://github.com/PapyrusSirius/papyrus-web/issues/208 is closed
        new AssociationEdgeCustomStyleBuilder(cdAssociation).addCustomArowStyles();

        diagramDescription.getEdgeDescriptions().add(cdAssociation);

        this.getViewBuilder().addDefaultReconnectionTools(cdAssociation);
    }

    private void createClassifierContainmentLink(DiagramDescription diagramDescription) {

        Supplier<List<NodeDescription>> sourceProvider = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getClass_());
        Supplier<List<NodeDescription>> targetProvider = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getClassifier());

        EdgeDescription containmentLinkEdge = this.getViewBuilder().createFeatureEdgeDescription(//
                CLASSIFIER_CONTAINMENT_LINK_EDGE_ID, //
                this.getQueryBuilder().emptyString(), //
                CallQuery.queryAttributeOnSelf(this.pack.getClass_NestedClassifier()), //
                sourceProvider, //
                targetProvider);

        containmentLinkEdge.setPreconditionExpression(new CallQuery(GRAPHICAL_EDGE_SOURCE).callService(Services.IS_NOT_VISUAL_DESCENDANT, GRAPHICAL_EDGE_TARGET, CACHE));

        containmentLinkEdge.getStyle().setSourceArrowStyle(ArrowStyle.CROSSED_CIRCLE);

        diagramDescription.getEdgeDescriptions().add(containmentLinkEdge);

        // Create containment Link tool
        EdgeTool tool = DiagramFactory.eINSTANCE.createEdgeTool();
        tool.setName(NEW_CONTAINMENT_LINK_TOOL_LABEL); //
        tool.setIconURLsExpression(ViewBuilder.getIconPathFromString("ContainmentLink"));

        String toolQuery = new CallQuery(SEMANTIC_EDGE_TARGET).callService(Services.MOVE_IN, SEMANTIC_EDGE_SOURCE, this.getQueryBuilder().aqlString(this.pack.getClass_NestedClassifier().getName()));

        ChangeContext changeContext = this.getViewBuilder().createChangeContextOperation(toolQuery);
        containmentLinkEdge.eAdapters().add(new CallbackAdapter(() -> {
            List<DiagramElementDescription> targetNodeDescriptions = containmentLinkEdge.getTargetDescriptions();
            tool.getTargetElementDescriptions().addAll(targetNodeDescriptions);
        }));
        tool.getBody().add(changeContext);
        this.registerCallback(containmentLinkEdge, () -> {
            CreationToolsUtil.addEdgeCreationTool(sourceProvider, tool);
        });
    }

    private void createDependencyDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> namedElementDescriptions = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getNamedElement());
        EdgeDescription cdDependency = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.pack.getDependency(),
                this.getQueryBuilder().queryAllReachableExactType(this.pack.getDependency()), namedElementDescriptions, namedElementDescriptions);
        cdDependency.getStyle().setLineStyle(LineStyle.DASH);
        cdDependency.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        EdgeTool creationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cdDependency, this.pack.getPackage_PackagedElement());
        this.registerCallback(cdDependency, () -> {
            CreationToolsUtil.addEdgeCreationTool(namedElementDescriptions, creationTool);
        });
        diagramDescription.getEdgeDescriptions().add(cdDependency);

        this.getViewBuilder().addDefaultReconnectionTools(cdDependency);
    }

    private void createGeneralizationDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceAndTargetDescriptionsSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getClassifier());

        EClass generalization = this.pack.getGeneralization();
        EdgeDescription cdGeneralization = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(generalization, this.getQueryBuilder().queryAllReachableExactType(generalization),
                sourceAndTargetDescriptionsSupplier, sourceAndTargetDescriptionsSupplier);
        cdGeneralization.getStyle().setLineStyle(LineStyle.SOLID);
        cdGeneralization.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        EdgeTool cdGeneralizationCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cdGeneralization, this.pack.getClassifier_Generalization());
        this.registerCallback(cdGeneralization, () -> {
            CreationToolsUtil.addEdgeCreationTool(sourceAndTargetDescriptionsSupplier, cdGeneralizationCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(cdGeneralization);

        this.getViewBuilder().addDefaultReconnectionTools(cdGeneralization);
    }

    private void createInterfaceRealizationDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceDescriptionsSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getBehavioredClassifier());
        Supplier<List<NodeDescription>> targetDescriptionsSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getInterface());

        EdgeDescription cdInterfaceRealization = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.pack.getInterfaceRealization(),
                this.getQueryBuilder().queryAllReachableExactType(this.pack.getInterfaceRealization()), sourceDescriptionsSupplier, targetDescriptionsSupplier);
        cdInterfaceRealization.getStyle().setLineStyle(LineStyle.DASH);
        cdInterfaceRealization.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        EdgeTool cdInterfaceRealizationCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cdInterfaceRealization, this.pack.getBehavioredClassifier_InterfaceRealization());
        this.registerCallback(cdInterfaceRealization, () -> {
            CreationToolsUtil.addEdgeCreationTool(sourceDescriptionsSupplier, cdInterfaceRealizationCreationTool);
        });
        diagramDescription.getEdgeDescriptions().add(cdInterfaceRealization);

        this.getViewBuilder().addDefaultReconnectionTools(cdInterfaceRealization);
    }

    private void createPackageContainmentLink(DiagramDescription diagramDescription) {

        Supplier<List<NodeDescription>> sourceProvider = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getPackage());
        Supplier<List<NodeDescription>> targetProvider = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getPackageableElement());

        EdgeDescription containmentLinkEdge = this.getViewBuilder().createFeatureEdgeDescription(//
                PACKAGE_CONTAINMENT_LINK_EDGE_ID, //
                this.getQueryBuilder().emptyString(), //
                CallQuery.queryAttributeOnSelf(this.pack.getPackage_PackagedElement()), //
                sourceProvider, //
                targetProvider);

        containmentLinkEdge.setPreconditionExpression(new CallQuery(GRAPHICAL_EDGE_SOURCE).callService(Services.IS_NOT_VISUAL_DESCENDANT, GRAPHICAL_EDGE_TARGET, CACHE));

        containmentLinkEdge.getStyle().setSourceArrowStyle(ArrowStyle.CROSSED_CIRCLE);

        diagramDescription.getEdgeDescriptions().add(containmentLinkEdge);

        // Create containment Link tool

        EdgeTool tool = DiagramFactory.eINSTANCE.createEdgeTool();
        tool.setName(NEW_CONTAINMENT_LINK_TOOL_LABEL); //
        tool.setIconURLsExpression(ViewBuilder.getIconPathFromString("ContainmentLink"));
        String toolQuery = new CallQuery(SEMANTIC_EDGE_TARGET).callService(Services.MOVE_IN, SEMANTIC_EDGE_SOURCE, this.getQueryBuilder().aqlString(this.pack.getPackage_PackagedElement().getName()));

        ChangeContext changeContext = this.getViewBuilder().createChangeContextOperation(toolQuery);
        containmentLinkEdge.eAdapters().add(new CallbackAdapter(() -> {
            List<DiagramElementDescription> targetNodeDescriptions = containmentLinkEdge.getTargetDescriptions();
            tool.getTargetElementDescriptions().addAll(targetNodeDescriptions);
        }));
        tool.getBody().add(changeContext);
        this.registerCallback(containmentLinkEdge, () -> {
            CreationToolsUtil.addEdgeCreationTool(sourceProvider, tool);
        });
    }

    private void createPackageImportDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> packageDescriptions = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getPackage());
        EdgeDescription cdPackageImport = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.pack.getPackageImport(),
                this.getQueryBuilder().queryAllReachable(this.pack.getPackageImport()), packageDescriptions, packageDescriptions);
        cdPackageImport.getStyle().setLineStyle(LineStyle.DASH);
        cdPackageImport.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);

        EdgeTool creationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cdPackageImport, this.pack.getNamespace_PackageImport());
        this.registerCallback(cdPackageImport, () -> {
            CreationToolsUtil.addEdgeCreationTool(packageDescriptions, creationTool);
        });
        diagramDescription.getEdgeDescriptions().add(cdPackageImport);
        this.getViewBuilder().addDefaultReconnectionTools(cdPackageImport);

    }

    private void createPackageMergeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> packageDescriptions = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getPackage());
        EdgeDescription cdPackageMerge = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.pack.getPackageMerge(),
                this.getQueryBuilder().queryAllReachable(this.pack.getPackageMerge()), packageDescriptions, packageDescriptions);
        cdPackageMerge.getStyle().setLineStyle(LineStyle.DASH);
        cdPackageMerge.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        EdgeTool creationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(cdPackageMerge, this.pack.getPackage_PackageMerge());
        this.registerCallback(cdPackageMerge, () -> {
            CreationToolsUtil.addEdgeCreationTool(packageDescriptions, creationTool);
        });
        diagramDescription.getEdgeDescriptions().add(cdPackageMerge);
        this.getViewBuilder().addDefaultReconnectionTools(cdPackageMerge);
    }

    private EdgeTool createSpecializedAssociationDomainBasedEdgeTool(String specializationName, String iconName, String serviceName, EdgeDescription cdAssociation) {
        ChangeContext changeContext = ViewFactory.eINSTANCE.createChangeContext();

        String query = new CallQuery(SEMANTIC_EDGE_SOURCE)//
                .callService(serviceName, //
                        SEMANTIC_EDGE_TARGET, //
                        EDGE_SOURCE, //
                        EDGE_TARGET, //
                        EDITING_CONTEXT, //
                        DIAGRAM_CONTEXT);
        EdgeTool tool = this.getViewBuilder().createFeatureBasedEdgeTool(specializationName, query, List.of());
        tool.setIconURLsExpression(ViewBuilder.getIconPathFromString(iconName));
        cdAssociation.eAdapters().add(new CallbackAdapter(() -> {
            List<DiagramElementDescription> targetNodeDescriptions = cdAssociation.getTargetDescriptions();
            tool.getTargetElementDescriptions().addAll(targetNodeDescriptions);
        }));
        tool.getBody().add(changeContext);
        return tool;
    }

    private void createUsageDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> classifierCollector = () -> this.collectNodesWithDomain(diagramDescription, this.pack.getNamedElement());
        EdgeDescription usageDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(this.pack.getUsage(), this.getQueryBuilder().queryAllReachable(this.pack.getUsage()),
                classifierCollector, classifierCollector);
        EdgeStyle style = usageDescription.getStyle();
        style.setLineStyle(LineStyle.DASH);
        style.setTargetArrowStyle(ArrowStyle.INPUT_ARROW);
        diagramDescription.getEdgeDescriptions().add(usageDescription);

        EdgeTool creationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(usageDescription, this.pack.getPackage_PackagedElement());
        this.registerCallback(usageDescription, () -> {
            CreationToolsUtil.addEdgeCreationTool(classifierCollector, creationTool);
        });
        this.getViewBuilder().addDefaultReconnectionTools(usageDescription);
    }

    /**
     * Add graphical dropped tool on Shared compartment {@link NodeDescription}.
     *
     * @param diagramDescription
     *         the {@link DiagramDescription} containing the Shared {@link NodeDescription}
     * @param compartmentName
     *         the name of the compartment to complete with the drop tool
     * @param cdSharedCompartmentForDataTypeDescription
     *         the Shared compartment {@link NodeDescription}
     */
    private void addDropToolOnSharedCompartment(DiagramDescription diagramDescription, String compartmentName, NodeDescription cdSharedCompartmentForDataTypeDescription) {
        // Add dropped tool on Shared Compartment container
        DropNodeTool graphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(cdSharedCompartmentForDataTypeDescription));
        this.registerCallback(cdSharedCompartmentForDataTypeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = null;
            if (OPERATIONS_COMPARTMENT_SUFFIX.equals(compartmentName)) {
                List<EClass> children = List.of(this.pack.getOperation());
                droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            } else if (ATTRIBUTES_COMPARTMENT_SUFFIX.equals(compartmentName)) {
                List<EClass> children = List.of(this.pack.getProperty());
                droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            } else if (RECEPTION_COMPARTMENT_SUFFIX.equals(compartmentName)) {
                List<EClass> children = List.of(this.pack.getReception());
                droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            } else if (NESTED_CLASSIFIERS_COMPARTMENT_SUFFIX.equals(compartmentName)) {
                List<EClass> children = List.of(this.pack.getClassifier());
                droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            }
            graphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        cdSharedCompartmentForDataTypeDescription.getPalette().setDropNodeTool(graphicalDropTool);

    }

}

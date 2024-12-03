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
 *  Titouan BOUËTE-GIRAUD (Artal Technologies) - titouan.bouete-giraud@artal.fr - Issues 219, 227
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.ListLayoutStrategyDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Extension;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Builder of the "Profile Diagram" diagram representation.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDDiagramDescriptionBuilder extends AbstractRepresentationDescriptionBuilder {

    /**
     * The suffix used to identify <i>operations</i> compartments.
     */
    public static final String OPERATIONS_COMPARTMENT_SUFFIX = "Operations";

    /**
     * The suffix used to identify <i>attributes</i> compartments.
     */
    public static final String ATTRIBUTES_COMPARTMENT_SUFFIX = "Attributes";

    /**
     * The suffix used to identify <i>literals</i> compartments.
     */
    public static final String LITERALS_COMPARTMENT_SUFFIX = "Literals";

    /**
     * The suffix used to identify <i>receptions</i> compartments.
     */
    public static final String SYMBOLS_COMPARTMENT_SUFFIX = "Symbols";

    /**
     * The name of the representation handled by this builder.
     */
    public static final String PRD_REP_NAME = "Profile Diagram";

    /**
     * The prefix of the representation handled by this builder.
     */
    public static final String PRD_PREFIX = "PRD_";

    /**
     * The name used to identify the Tool section.
     */
    public static final String SHOW_HIDE = "SHOW_HIDE";

    /**
     * Underscore.
     */
    private static final String UNDERSCORE = "_";

    /**
     * AQL expression to set children not draggable from its container.
     */
    private static final String CHILD_NOT_DRAGGABLE_EXPRESSION = "aql:false";

    /**
     * The name of the {@link NodeDescription} representing a metaclass on the diagram.
     */
    public static final String PRD_METACLASS = PRD_PREFIX + "Metaclass";

    /**
     * The name of the {@link NodeDescription} representing a metaclass inside another element.
     */
    public static final String PRD_SHARED_METACLASS = PRD_METACLASS + UNDERSCORE + SHARED_SUFFIX;

    private UMLPackage umlPackage = UMLPackage.eINSTANCE;

    /**
     * The <i>shared</i> {@link NodeDescription} for the diagram.
     */
    private NodeDescription prdSharedDescription;

    /**
     * Predicate used to exclude Metaclass Node Description.
     */
    private Predicate<NodeDescription> excludeMetaclassNodeDescription = nodeDescription -> !nodeDescription.getName().equals(PRD_METACLASS) && !nodeDescription.getName().equals(PRD_SHARED_METACLASS);

    /**
     * Initializes the builder.
     */
    public PRDDiagramDescriptionBuilder() {
        super(PRD_PREFIX, PRD_REP_NAME, UMLPackage.eINSTANCE.getPackage());
    }

    @Override
    protected void fillDescription(DiagramDescription diagramDescription) {

        // create diagram tool sections
        this.createDefaultToolSectionInDiagramDescription(diagramDescription);
        diagramDescription.setPreconditionExpression(CallQuery.queryServiceOnSelf(ProfileDiagramServices.IS_PROFILE_MODEL));

        // create show/hide tool section
        DiagramToolSection showHideToolSection = this.getViewBuilder().createDiagramToolSection(SHOW_HIDE);
        diagramDescription.getPalette().getToolSections().add(showHideToolSection);
        this.createHideSymbolTool(diagramDescription,
                SHOW_HIDE);
        this.createShowSymbolTool(diagramDescription, SHOW_HIDE);
        this.createHideAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);
        this.createShowAllNonSymbolCompartmentTool(diagramDescription, SHOW_HIDE);

        // create node descriptions with their tools
        this.createClassTopNodeDescription(diagramDescription);
        this.createCommentTopNodeDescription(diagramDescription, NODES);
        this.createConstraintTopNodeDescription(diagramDescription, NODES);
        this.createDataTypeTopNodeDescription(diagramDescription);
        this.createEnumerationTopNodeDescription(diagramDescription);
        /*
         * This call needs to be below createDiagramClassDescription: PRD_Class and PRD_Metaclass are defined at the
         * same level, so the method that selects the best mapping candidate will return the first found. Ensuring
         * PRD_Class is found first makes view creation/DnD easier to define: we just have to handle the metaclass case,
         * which is way less common than the class case.
         */
        this.createMetaclassTopNodeDescription(diagramDescription);
        this.createPackageTopNodeDescription(diagramDescription);
        this.createPrimitiveTypeTopNodeDescription(diagramDescription);
        this.createProfileTopNodeDescription(diagramDescription);
        this.createStereotypeTopNodeDescription(diagramDescription);

        // create shared node descriptions with their tools
        this.prdSharedDescription = this.createSharedDescription(diagramDescription);
        this.createAttributeSharedNodeDescription(diagramDescription);
        this.createClassSharedNodeDescription(diagramDescription);
        this.createCommentSubNodeDescription(diagramDescription, this.prdSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getComment(), SHARED_SUFFIX), List.of(this.umlPackage.getPackage()));
        this.createConstraintSubNodeDescription(diagramDescription, this.prdSharedDescription, NODES,
                this.getIdBuilder().getSpecializedDomainNodeName(this.umlPackage.getConstraint(), SHARED_SUFFIX), List.of(this.umlPackage.getPackage()));
        this.createDataTypeSharedNodeDescription(diagramDescription);
        this.createEnumerationSharedNodeDescription(diagramDescription);
        /*
         * This call needs to be below createClassDescriptionInNodeDescription: shared PRD_Class and PRD_Metaclass are
         * defined at the same level, so the method that selects the best mapping candidate will return the first found.
         * Ensuring PRD_Class is found first makes view creation/DnD easier to define: we just have to handle the
         * metaclass case, which is way less common than the class case. Note that we have to define PRD_Metaclass as a
         * shared element even if it is only reused in PRD_Profile, otherwise it takes precedence over PRD_Class in
         * profile.
         */
        this.createMetaclassSharedNodeDescription(diagramDescription);
        this.createOperationSharedNodeDescription(diagramDescription);
        this.createPackageSharedNodeDescription(diagramDescription);
        this.createPrimitiveTypeSharedNodeDescription(diagramDescription);
        this.createProfileSharedNodeDescription(diagramDescription);
        this.createStereotypeSharedNodeDescription(diagramDescription);

        // create shared compartments
        this.createLiteralsCompartmentForEnumerationSharedNodeDescription(diagramDescription);
        this.createCompartmentForClassSharedNodeDescription(diagramDescription, ATTRIBUTES_COMPARTMENT_SUFFIX);
        this.createCompartmentForClassSharedNodeDescription(diagramDescription, OPERATIONS_COMPARTMENT_SUFFIX);
        this.createCompartmentForDataTypeSharedNodeDescription(diagramDescription, ATTRIBUTES_COMPARTMENT_SUFFIX);
        this.createCompartmentForDataTypeSharedNodeDescription(diagramDescription, OPERATIONS_COMPARTMENT_SUFFIX);
        this.createCompartmentForStereotypeSharedNodeDescription(diagramDescription, ATTRIBUTES_COMPARTMENT_SUFFIX);
        this.createCompartmentForStereotypeSharedNodeDescription(diagramDescription, OPERATIONS_COMPARTMENT_SUFFIX);

        // create edge descriptions with their tools
        this.createAssociationEdgeDescription(diagramDescription);
        this.createExtensionEdgeDescription(diagramDescription);
        this.createGeneralizationEdgeDescription(diagramDescription);

        List<EClass> symbolOwners = List.of(
                this.umlPackage.getClass_(),
                this.umlPackage.getDataType(),
                this.umlPackage.getEnumeration(),
                this.umlPackage.getStereotype(),
                this.umlPackage.getPrimitiveType(),
                this.umlPackage.getProfile(),
                this.umlPackage.getPackage());
        this.createSymbolSharedNodeDescription(diagramDescription, this.prdSharedDescription, symbolOwners, List.of(), SYMBOLS_COMPARTMENT_SUFFIX);

        diagramDescription.getPalette().setDropTool(this.getViewBuilder().createGenericSemanticDropTool(this.getIdBuilder().getDiagramSemanticDropToolName()));

        // Add dropped tool on diagram
        DropNodeTool prdGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getDiagramGraphicalDropToolName());
        List<EClass> children = List.of(this.umlPackage.getClass_(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getDataType(), this.umlPackage.getEnumeration(),
                this.umlPackage.getPackage(), this.umlPackage.getPrimitiveType(), this.umlPackage.getStereotype());
        this.registerCallback(diagramDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            prdGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        diagramDescription.getPalette().setDropNodeTool(prdGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Class} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createClassTopNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierTopNodeDescription(diagramDescription, this.umlPackage.getClass_());
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link DataType} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createDataTypeTopNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierTopNodeDescription(diagramDescription, this.umlPackage.getDataType());
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Enumeration} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createEnumerationTopNodeDescription(DiagramDescription diagramDescription) {
        EClass enumerationEClass = this.umlPackage.getEnumeration();
        ListLayoutStrategyDescription listLayoutStrategyDescription = DiagramFactory.eINSTANCE.createListLayoutStrategyDescription();
        listLayoutStrategyDescription.setAreChildNodesDraggableExpression(CHILD_NOT_DRAGGABLE_EXPRESSION);
        NodeDescription prdEnumerationTopNodeDescription = this.newNodeBuilder(enumerationEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .layoutStrategyDescription(listLayoutStrategyDescription)//
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(enumerationEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(enumerationEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(enumerationEClass.getName())) //
                .insideLabelDescription(this.getQueryBuilder().queryRenderLabel(), this.getViewBuilder().createDefaultInsideLabelStyle(true, true))
                .build();
        diagramDescription.getNodeDescriptions().add(prdEnumerationTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdEnumerationTopNodeDescription);

        NodeTool prdEnumerationTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), enumerationEClass);
        this.addDiagramToolInToolSection(diagramDescription, prdEnumerationTopNodeCreationTool, NODES);
    }

    /**
     * Create the {@link NodeDescription} representing an UML Metaclass on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createMetaclassTopNodeDescription(DiagramDescription diagramDescription) {
        EClass metaclassEClass = this.umlPackage.getClass_();
        NodeDescription prdMetaclassTopNodeDescription = this.newNodeBuilder(metaclassEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(PRD_METACLASS) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ProfileDiagramServices.GET_METACLASS_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, false))
                .build();
        diagramDescription.getNodeDescriptions().add(prdMetaclassTopNodeDescription);
        this.createDefaultToolSectionsInNodeDescription(prdMetaclassTopNodeDescription);

        // Custom tool is defined from Frontend nodules :
        // /frontend/src/views/edit-project/EditProjectView.tsx/diagramPaletteToolContributions
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Package} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPackageTopNodeDescription(DiagramDescription diagramDescription) {
        EClass packageEClass = this.umlPackage.getPackage();
        NodeDescription prdPackageTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                this.getQueryBuilder().queryAllReachableExactType(this.umlPackage.getPackage()));
        diagramDescription.getNodeDescriptions().add(prdPackageTopNodeDescription);

        prdPackageTopNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        // create Package tool sections
        this.createDefaultToolSectionsInNodeDescription(prdPackageTopNodeDescription);

        NodeTool prdPackageTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        this.addDiagramToolInToolSection(diagramDescription, prdPackageTopNodeCreationTool, NODES);

        // No direct children for Package: the NodeDescriptions it can contain are all defined as shared descriptions.

        // Add dropped tool on Package container
        DropNodeTool prdPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(prdPackageTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getClass_(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getDataType(), this.umlPackage.getPackage());
        this.registerCallback(prdPackageTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of()).stream()
                    .filter(nodeDescription -> !nodeDescription.getName().contains(PRD_METACLASS)).toList();
            prdPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        prdPackageTopNodeDescription.getPalette().setDropNodeTool(prdPackageGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link PrimitiveType} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPrimitiveTypeTopNodeDescription(DiagramDescription diagramDescription) {
        EClass primitiveTypeEClass = this.umlPackage.getPrimitiveType();
        NodeDescription prdPrimitiveTypeTopNodeDescription = this.newNodeBuilder(primitiveTypeEClass, this.getViewBuilder().createRectangularNodeStyle()) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(primitiveTypeEClass)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(primitiveTypeEClass.getName())) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(primitiveTypeEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, false))
                .build();
        diagramDescription.getNodeDescriptions().add(prdPrimitiveTypeTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdPrimitiveTypeTopNodeDescription);

        NodeTool prdPrimitiveTypeTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), primitiveTypeEClass);
        this.addDiagramToolInToolSection(diagramDescription, prdPrimitiveTypeTopNodeCreationTool, NODES);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Profile} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createProfileTopNodeDescription(DiagramDescription diagramDescription) {
        EClass profileEClass = this.umlPackage.getProfile();
        NodeDescription prdProfileTopNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(profileEClass,
                this.getQueryBuilder().queryAllReachableExactType(profileEClass));
        diagramDescription.getNodeDescriptions().add(prdProfileTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdProfileTopNodeDescription);

        NodeTool prdProfileTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), profileEClass);
        this.addDiagramToolInToolSection(diagramDescription, prdProfileTopNodeCreationTool, NODES);

        // Add dropped tool on Profile container
        DropNodeTool prdProfileGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(prdProfileTopNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getClass_(), this.umlPackage.getElementImport(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getDataType(),
                this.umlPackage.getPackage());
        this.registerCallback(prdProfileTopNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            prdProfileGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        prdProfileTopNodeDescription.getPalette().setDropNodeTool(prdProfileGraphicalDropTool);
    }

    /**
     * Creates the {@link NodeDescription} representing an UML {@link Stereotype} on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createStereotypeTopNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierTopNodeDescription(diagramDescription, this.umlPackage.getStereotype());
    }

    /**
     * Creates a <i>Property</i> child reused by <i>Attributes</i> compartments.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createAttributeSharedNodeDescription(DiagramDescription diagramDescription) {
        List<EClass> owners = List.of(this.umlPackage.getClass_(), this.umlPackage.getDataType(), this.umlPackage.getStereotype());
        List<EClass> forbiddenOwners = List.of(this.umlPackage.getPrimitiveType(), this.umlPackage.getEnumeration());
        NodeDescription attributeInCompartmentSharedNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, this.prdSharedDescription,
                this.umlPackage.getProperty(), ATTRIBUTES_COMPARTMENT_SUFFIX, CallQuery.queryOperationOnSelf(this.umlPackage.getClassifier__GetAllAttributes()),
                this.umlPackage.getStructuredClassifier_OwnedAttribute(), owners, forbiddenOwners, this.excludeMetaclassNodeDescription);
        attributeInCompartmentSharedNodeDescription.setName(attributeInCompartmentSharedNodeDescription.getName() + UNDERSCORE + SHARED_SUFFIX);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Class}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createClassSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierSharedNodeDescription(diagramDescription, this.umlPackage.getClass_());
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link DataType}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param parentNodeDescription
     *            the parent {@link NodeDescription} which contain definition of the new {@link NodeDescription}
     */
    private void createDataTypeSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierSharedNodeDescription(diagramDescription, this.umlPackage.getDataType());
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Enumeration}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createEnumerationSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass enumerationEClass = this.umlPackage.getEnumeration();
        ListLayoutStrategyDescription listLayoutStrategyDescription = DiagramFactory.eINSTANCE.createListLayoutStrategyDescription();
        listLayoutStrategyDescription.setAreChildNodesDraggableExpression(CHILD_NOT_DRAGGABLE_EXPRESSION);
        NodeDescription prdEnumerationSharedNodeDescription = this.newNodeBuilder(enumerationEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(enumerationEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(listLayoutStrategyDescription)//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(enumerationEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(enumerationEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        this.prdSharedDescription.getChildrenDescriptions().add(prdEnumerationSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdEnumerationSharedNodeDescription);

        NodeTool prdEnumerationSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), enumerationEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(prdEnumerationSharedNodeDescription, diagramDescription, prdEnumerationSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));
    }

    /**
     * Create the shared {@link NodeDescription} representing an UML Metaclass.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createMetaclassSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass metaclassEClass = this.umlPackage.getClass_();
        NodeDescription prdMetaclassSharedNodeDescription = this.newNodeBuilder(metaclassEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(PRD_SHARED_METACLASS) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription())//
                .semanticCandidateExpression(CallQuery.queryServiceOnSelf(ProfileDiagramServices.GET_METACLASS_CANDIDATES)) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, false))
                .build();

        this.prdSharedDescription.getChildrenDescriptions().add(prdMetaclassSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdMetaclassSharedNodeDescription);

        // Use reuseNodeAndCreateTool once the tool to create a metaclass is available.
        this.registerCallback(prdMetaclassSharedNodeDescription, () -> {
            List<NodeDescription> owerNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, List.of(this.umlPackage.getProfile()), List.of());
            for (NodeDescription ownerNodeDescription : owerNodeDescriptions) {
                if (ownerNodeDescription != prdMetaclassSharedNodeDescription.eContainer()) {
                    ownerNodeDescription.getReusedChildNodeDescriptions().add(prdMetaclassSharedNodeDescription);
                }
            }
        });
    }

    /**
     * Creates a <i>Operation</i> child reused by <i>Operations</i> compartments.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createOperationSharedNodeDescription(DiagramDescription diagramDescription) {
        List<EClass> owners = List.of(this.umlPackage.getClass_(), this.umlPackage.getDataType(), this.umlPackage.getStereotype());
        List<EClass> forbiddenOwners = List.of(this.umlPackage.getPrimitiveType(), this.umlPackage.getEnumeration());
        NodeDescription operationInCompartmentSharedNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, this.prdSharedDescription,
                this.umlPackage.getOperation(), OPERATIONS_COMPARTMENT_SUFFIX, CallQuery.queryOperationOnSelf(this.umlPackage.getClassifier__GetAllOperations()),
                this.umlPackage.getClass_OwnedOperation(), owners, forbiddenOwners, this.excludeMetaclassNodeDescription);
        operationInCompartmentSharedNodeDescription.setName(operationInCompartmentSharedNodeDescription.getName() + UNDERSCORE + SHARED_SUFFIX);
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
        NodeDescription prdPackageSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(packageEClass,
                CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()));
        prdPackageSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(packageEClass, SHARED_SUFFIX));
        prdPackageSharedNodeDescription.setStyle(this.getViewBuilder().createPackageNodeStyle());

        this.prdSharedDescription.getChildrenDescriptions().add(prdPackageSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdPackageSharedNodeDescription);

        NodeTool prdPackageSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), packageEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(prdPackageSharedNodeDescription, diagramDescription, prdPackageSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Package container
        DropNodeTool prdPackageGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(prdPackageSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getClass_(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getDataType(), this.umlPackage.getPackage());
        this.registerCallback(prdPackageSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of()).stream()
                    .filter(nodeDescription -> !nodeDescription.getName().contains(PRD_METACLASS)).toList();
            prdPackageGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        prdPackageSharedNodeDescription.getPalette().setDropNodeTool(prdPackageGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link PrimitiveType}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createPrimitiveTypeSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass primitiveTypeEClass = this.umlPackage.getPrimitiveType();
        NodeDescription prdPrimitiveTypeSharedNodeDescription = this.newNodeBuilder(primitiveTypeEClass, this.getViewBuilder().createRectangularNodeStyle()) //
                .name(this.getIdBuilder().getSpecializedDomainNodeName(primitiveTypeEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement())) //
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .labelEditTool(this.getViewBuilder().createDirectEditTool(primitiveTypeEClass.getName())) //
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(primitiveTypeEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, false))
                .build();
        this.prdSharedDescription.getChildrenDescriptions().add(prdPrimitiveTypeSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdPrimitiveTypeSharedNodeDescription);

        NodeTool prdPrimitiveTypeSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), primitiveTypeEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(prdPrimitiveTypeSharedNodeDescription, diagramDescription, prdPrimitiveTypeSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Profile}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createProfileSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass profileEClass = this.umlPackage.getProfile();
        NodeDescription prdProfileSharedNodeDescription = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(profileEClass,
                CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()));
        prdProfileSharedNodeDescription.setName(this.getIdBuilder().getSpecializedDomainNodeName(profileEClass, SHARED_SUFFIX));
        this.prdSharedDescription.getChildrenDescriptions().add(prdProfileSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdProfileSharedNodeDescription);

        NodeTool prdProfileSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), profileEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(prdProfileSharedNodeDescription, diagramDescription, prdProfileSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));

        // Add dropped tool on Shared Profile container
        DropNodeTool prdProfileGraphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(prdProfileSharedNodeDescription));
        List<EClass> children = List.of(this.umlPackage.getClass_(), this.umlPackage.getElementImport(), this.umlPackage.getComment(), this.umlPackage.getConstraint(), this.umlPackage.getDataType(),
                this.umlPackage.getPackage());
        this.registerCallback(prdProfileSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            prdProfileGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        prdProfileSharedNodeDescription.getPalette().setDropNodeTool(prdProfileGraphicalDropTool);
    }

    /**
     * Creates the shared {@link NodeDescription} representing an UML {@link Stereotype}.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createStereotypeSharedNodeDescription(DiagramDescription diagramDescription) {
        this.createClassifierSharedNodeDescription(diagramDescription, this.umlPackage.getStereotype());
    }

    /**
     * Creates a shared compartment reused by <i>Class</i> {@link NodeDescription}.
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
    private void createCompartmentForClassSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass classEClass = this.umlPackage.getClass_();
        List<EClass> owners = List.of(classEClass);
        List<EClass> forbiddenOwners = List.of(this.umlPackage.getStereotype());
        NodeDescription prdCompartmentForClassSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.prdSharedDescription, classEClass, compartmentName, owners,
                forbiddenOwners, this.excludeMetaclassNodeDescription);

        // Add Graphical dropped tool on Shared Compartment for Class
        this.addDropToolOnSharedCompartment(diagramDescription, compartmentName, prdCompartmentForClassSharedNodeDescription);
    }

    /**
     * Creates a shared compartment reused by <i>DataType</i> {@link NodeDescription}.
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
    private void createCompartmentForDataTypeSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass dataTypeEClass = this.umlPackage.getDataType();
        List<EClass> owners = List.of(dataTypeEClass);
        List<EClass> forbiddenOwners = List.of(this.umlPackage.getEnumeration(), this.umlPackage.getPrimitiveType());
        NodeDescription prdCompartmentForDataTypeSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.prdSharedDescription, dataTypeEClass, compartmentName,
                owners,
                forbiddenOwners, this.excludeMetaclassNodeDescription);

        // Add Graphical dropped tool on Shared Compartment for Class
        this.addDropToolOnSharedCompartment(diagramDescription, compartmentName, prdCompartmentForDataTypeSharedNodeDescription);
    }

    /**
     * Creates a shared compartment reused by <i>Stereotype</i> {@link NodeDescription}.
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
    private void createCompartmentForStereotypeSharedNodeDescription(DiagramDescription diagramDescription, String compartmentName) {
        EClass stereotypeEClass = this.umlPackage.getStereotype();
        List<EClass> owners = List.of(stereotypeEClass);
        NodeDescription prdCompartmentForStereotypeSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.prdSharedDescription, stereotypeEClass, compartmentName,
                owners, List.of(), this.excludeMetaclassNodeDescription);

        // Add Graphical dropped tool on Shared Compartment for Stereotype
        this.addDropToolOnSharedCompartment(diagramDescription, compartmentName, prdCompartmentForStereotypeSharedNodeDescription);
    }

    /**
     * Creates a list {@link NodeDescription} representing {@link Classifier} sub-classes on the Diagram.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     *
     * @param classifierEClass
     *            the classifier sub-type to represent
     * @return the created {@link NodeDescription}
     */
    private NodeDescription createClassifierTopNodeDescription(DiagramDescription diagramDescription, EClass classifierEClass) {
        ListLayoutStrategyDescription listLayoutStrategyDescription = DiagramFactory.eINSTANCE.createListLayoutStrategyDescription();
        listLayoutStrategyDescription.setAreChildNodesDraggableExpression(CHILD_NOT_DRAGGABLE_EXPRESSION);
        NodeDescription prdClassifierTopNodeDescription = this.newNodeBuilder(classifierEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .layoutStrategyDescription(listLayoutStrategyDescription)//
                .semanticCandidateExpression(this.getQueryBuilder().queryAllReachableExactType(classifierEClass))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(classifierEClass.getName()))//
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(classifierEClass.getName())) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .build();
        diagramDescription.getNodeDescriptions().add(prdClassifierTopNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdClassifierTopNodeDescription);

        NodeTool prdClassifierTopNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), classifierEClass);
        this.addDiagramToolInToolSection(diagramDescription, prdClassifierTopNodeCreationTool, NODES);

        return prdClassifierTopNodeDescription;
    }

    /**
     * Creates a shared list {@link NodeDescription} representing {@link Classifier} sub-classes.
     * <p>
     * The created {@link NodeDescription} is added to the <i>shared</i> {@link NodeDescription} of the diagram.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param classifierEClass
     *            the classifier sub-type to represent
     * @return the created {@link NodeDescription}
     */
    private NodeDescription createClassifierSharedNodeDescription(DiagramDescription diagramDescription, EClass classifierEClass) {
        ListLayoutStrategyDescription listLayoutStrategyDescription = DiagramFactory.eINSTANCE.createListLayoutStrategyDescription();
        listLayoutStrategyDescription.setAreChildNodesDraggableExpression(CHILD_NOT_DRAGGABLE_EXPRESSION);
        NodeDescription prdClassifierSharedNodeDescription = this.newNodeBuilder(classifierEClass, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedDomainNodeName(classifierEClass, SHARED_SUFFIX)) //
                .layoutStrategyDescription(listLayoutStrategyDescription)//
                .semanticCandidateExpression(CallQuery.queryAttributeOnSelf(this.umlPackage.getPackage_PackagedElement()))//
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)//
                .labelEditTool(this.getViewBuilder().createDirectEditTool(classifierEClass.getName()))//
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(true, true))
                .deleteTool(this.getViewBuilder().createNodeDeleteTool(classifierEClass.getName())) //
                .build();
        this.prdSharedDescription.getChildrenDescriptions().add(prdClassifierSharedNodeDescription);

        this.createDefaultToolSectionsInNodeDescription(prdClassifierSharedNodeDescription);

        NodeTool prdClassifierSharedNodeCreationTool = this.getViewBuilder().createCreationTool(this.umlPackage.getPackage_PackagedElement(), classifierEClass);
        List<EClass> owners = List.of(this.umlPackage.getPackage());
        this.reuseNodeAndCreateTool(prdClassifierSharedNodeDescription, diagramDescription, prdClassifierSharedNodeCreationTool, NODES, owners.toArray(EClass[]::new));
        return prdClassifierSharedNodeDescription;
    }

    /**
     * Creates a shared <i>Literals</i> compartment reused by <i>Enumeration</i> {@link NodeDescription}.
     * <p>
     * The created {@link NodeDescription} compartment is added to the <i>shared</i> {@link NodeDescription} of the
     * diagram.
     * <p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     */
    private void createLiteralsCompartmentForEnumerationSharedNodeDescription(DiagramDescription diagramDescription) {
        EClass enumerationEClass = this.umlPackage.getEnumeration();
        List<EClass> owners = List.of(enumerationEClass);
        NodeDescription prdLiteralsCompartmentSharedNodeDescription = this.createSharedCompartmentsDescription(diagramDescription, this.prdSharedDescription, enumerationEClass,
                LITERALS_COMPARTMENT_SUFFIX, owners, List.of(), this.excludeMetaclassNodeDescription);

        NodeDescription enumerationLiteralsSubNodeDescription = this.createEnumerationLiteralsSubNodeDescription(diagramDescription, prdLiteralsCompartmentSharedNodeDescription);

        // Add Graphical dropped tool on Enumeration Compartment container
        DropNodeTool prdLiteralsCompartmentGraphicalDropTool = this.getViewBuilder()
                .createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(prdLiteralsCompartmentSharedNodeDescription));
        this.registerCallback(prdLiteralsCompartmentSharedNodeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = List.of(enumerationLiteralsSubNodeDescription);
            prdLiteralsCompartmentGraphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        prdLiteralsCompartmentSharedNodeDescription.getPalette().setDropNodeTool(prdLiteralsCompartmentGraphicalDropTool);
    }

    /**
     * Creates a <i>Literals</i> child reused by <i>Literals</i> compartments.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @return the created <i>Literals</i> child node description
     */
    private NodeDescription createEnumerationLiteralsSubNodeDescription(DiagramDescription diagramDescription, NodeDescription parent) {
        List<EClass> owners = List.of(this.umlPackage.getEnumeration());
        NodeDescription enumLiteralNodeDescriptionInCompartmentSubNodeDescription = this.createSubNodeDescriptionInCompartmentDescription(diagramDescription, parent,
                this.umlPackage.getEnumerationLiteral(),
                LITERALS_COMPARTMENT_SUFFIX, CallQuery.queryAttributeOnSelf(this.umlPackage.getEnumeration_OwnedLiteral()), this.umlPackage.getEnumeration_OwnedLiteral(), owners, List.of(),
                this.excludeMetaclassNodeDescription);
        return enumLiteralNodeDescriptionInCompartmentSubNodeDescription;
    }

    /**
     * Add graphical dropped tool on Shared compartment {@link NodeDescription}.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the Shared {@link NodeDescription}
     * @param compartmentName
     *            the name of the compartment to complete with the drop tool
     * @param prdSharedCompartmentDescription
     *            the Shared compartment {@link NodeDescription}
     */
    private void addDropToolOnSharedCompartment(DiagramDescription diagramDescription, String compartmentName, NodeDescription prdSharedCompartmentForDataTypeDescription) {
        // Add dropped tool on Shared Compartment container
        DropNodeTool graphicalDropTool = this.getViewBuilder().createGraphicalDropTool(this.getIdBuilder().getNodeGraphicalDropToolName(prdSharedCompartmentForDataTypeDescription));
        this.registerCallback(prdSharedCompartmentForDataTypeDescription, () -> {
            List<NodeDescription> droppedNodeDescriptions = null;
            if (OPERATIONS_COMPARTMENT_SUFFIX.equals(compartmentName)) {
                List<EClass> children = List.of(this.umlPackage.getOperation());
                droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            } else if (ATTRIBUTES_COMPARTMENT_SUFFIX.equals(compartmentName)) {
                List<EClass> children = List.of(this.umlPackage.getProperty());
                droppedNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, children, List.of());
            }
            graphicalDropTool.getAcceptedNodeTypes().addAll(droppedNodeDescriptions);
        });
        prdSharedCompartmentForDataTypeDescription.getPalette().setDropNodeTool(graphicalDropTool);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Association}.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createAssociationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceAndTargetDescriptionSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClassifier()).stream()
                .filter(nodeDescription -> !nodeDescription.getName().equals(PRD_METACLASS) && !nodeDescription.getName().equals(PRD_SHARED_METACLASS)).toList();

        EClass associationEClass = this.umlPackage.getAssociation();
        EdgeDescription prdAssociationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(associationEClass,
                this.getQueryBuilder().queryAllReachableExactType(associationEClass),
                sourceAndTargetDescriptionSupplier, sourceAndTargetDescriptionSupplier);
        prdAssociationEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        prdAssociationEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.NONE);
        prdAssociationEdgeDescription.getStyle().setSourceArrowStyle(ArrowStyle.NONE);

        EdgeTool prdAssociationEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(prdAssociationEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(prdAssociationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceAndTargetDescriptionSupplier.get(), prdAssociationEdgeCreationTool);
        });

        prdAssociationEdgeDescription.setBeginLabelExpression(this.getQueryBuilder().createDomainBaseEdgeSourceLabelExpression());
        prdAssociationEdgeDescription.getPalette().setBeginLabelEditTool(this.getViewBuilder().createDirectEditTool(CallQuery.queryServiceOnSelf(ClassDiagramServices.GET_ASSOCIATION_TARGET)));

        prdAssociationEdgeDescription.setEndLabelExpression(this.getQueryBuilder().createDomainBaseEdgeTargetLabelExpression());
        prdAssociationEdgeDescription.getPalette().setEndLabelEditTool(this.getViewBuilder().createDirectEditTool(CallQuery.queryServiceOnSelf(ClassDiagramServices.GET_ASSOCIATION_SOURCE)));

        diagramDescription.getEdgeDescriptions().add(prdAssociationEdgeDescription);
        this.getViewBuilder().addDefaultReconnectionTools(prdAssociationEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Extension}.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createExtensionEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceDescriptionSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getStereotype());
        Supplier<List<NodeDescription>> targetDescriptionSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClass_());

        EClass extensionEClass = this.umlPackage.getExtension();
        EdgeDescription prdExtensionEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(extensionEClass,
                this.getQueryBuilder().queryAllReachableExactType(extensionEClass),
                sourceDescriptionSupplier, targetDescriptionSupplier);
        prdExtensionEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        prdExtensionEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_FILL_CLOSED_ARROW);
        prdExtensionEdgeDescription.getStyle().setSourceArrowStyle(ArrowStyle.NONE);
        EdgeTool prdExtensionEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(prdExtensionEdgeDescription, this.umlPackage.getPackage_PackagedElement());
        this.registerCallback(prdExtensionEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceDescriptionSupplier.get(), prdExtensionEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(prdExtensionEdgeDescription);
        this.getViewBuilder().addDefaultReconnectionTools(prdExtensionEdgeDescription);
    }

    /**
     * Create the {@link EdgeDescription} representing an UML {@link Generalization}.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link EdgeDescription}
     */
    private void createGeneralizationEdgeDescription(DiagramDescription diagramDescription) {
        Supplier<List<NodeDescription>> sourceDescriptionSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClassifier()).stream()
                .filter(nodeDescription -> !nodeDescription.getName().equals(PRD_METACLASS) && !nodeDescription.getName().equals(PRD_SHARED_METACLASS)).toList();
        Supplier<List<NodeDescription>> targetDescriptionSupplier = () -> this.collectNodesWithDomain(diagramDescription, this.umlPackage.getClassifier());

        EClass generalizationEClass = this.umlPackage.getGeneralization();
        EdgeDescription prdGeneralizationEdgeDescription = this.getViewBuilder().createDefaultSynchonizedDomainBaseEdgeDescription(generalizationEClass,
                this.getQueryBuilder().queryAllReachableExactType(generalizationEClass), sourceDescriptionSupplier, targetDescriptionSupplier, false);
        prdGeneralizationEdgeDescription.getStyle().setLineStyle(LineStyle.SOLID);
        prdGeneralizationEdgeDescription.getStyle().setTargetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW);
        prdGeneralizationEdgeDescription.getStyle().setSourceArrowStyle(ArrowStyle.NONE);
        EdgeTool prdGeneratlizationEdgeCreationTool = this.getViewBuilder().createDefaultDomainBasedEdgeTool(prdGeneralizationEdgeDescription, this.umlPackage.getClassifier_Generalization());
        this.registerCallback(prdGeneralizationEdgeDescription, () -> {
            this.addEdgeToolInEdgesToolSection(sourceDescriptionSupplier.get(), prdGeneratlizationEdgeCreationTool);
        });

        diagramDescription.getEdgeDescriptions().add(prdGeneralizationEdgeDescription);
        this.getViewBuilder().addDefaultReconnectionTools(prdGeneralizationEdgeDescription);
    }

}

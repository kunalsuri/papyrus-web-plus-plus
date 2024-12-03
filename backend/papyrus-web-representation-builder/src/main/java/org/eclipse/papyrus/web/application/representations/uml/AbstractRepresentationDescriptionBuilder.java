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
 *  Titouan BOUËTE-GIRAUD (Artal Technologies) - titouan.bouete-giraud@artal.fr - Issue 219, 227
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import static java.util.stream.Collectors.toList;
import static org.eclipse.papyrus.web.application.representations.view.aql.CallQuery.queryAttributeOnSelf;
import static org.eclipse.papyrus.web.application.representations.view.aql.OperatorQuery.and;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_OTHER_END;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.uml.domain.services.EMFUtils;
import org.eclipse.papyrus.uml.domain.services.UMLHelper;
import org.eclipse.papyrus.web.application.representations.view.IDomainHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.application.representations.view.StyleProvider;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.papyrus.web.application.representations.view.aql.QueryHelper;
import org.eclipse.papyrus.web.application.representations.view.aql.Services;
import org.eclipse.papyrus.web.application.representations.view.aql.Variables;
import org.eclipse.papyrus.web.application.representations.view.builders.CallbackAdapter;
import org.eclipse.papyrus.web.application.representations.view.builders.LabelConditionalStyleBuilder;
import org.eclipse.papyrus.web.application.representations.view.builders.ListCompartmentBuilder;
import org.eclipse.papyrus.web.application.representations.view.builders.NodeDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.view.builders.NodeSemanticCandidateExpressionTransformer;
import org.eclipse.papyrus.web.application.representations.view.builders.NoteStyleDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.view.builders.ViewBuilder;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription;
import org.eclipse.sirius.components.view.ChangeContext;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.DeleteTool;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramPackage;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.ListLayoutStrategyDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.NodeToolSection;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.Tool;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Abstract implementation of a representation builder.
 *
 * @author Arthur Daussy
 */
public abstract class AbstractRepresentationDescriptionBuilder {

    /**
     * Prefix used to identify children of packages and model.
     */
    public static final String PACKAGE_CHILD = "inPackage";

    public static final Predicate<NodeDescription> PACKAGE_CHILDREN_FILTER = n -> n.getName().endsWith(PACKAGE_CHILD);

    /**
     * The String used to suffix the name of shared {@link NodeDescription}s.
     */
    public static final String SHARED_SUFFIX = "SHARED";

    /**
     * The name of the parent {@link NodeDescription} containing all the shared {@link NodeDescription}s.
     */
    public static final String SHARED_DESCRIPTIONS = "SHARED_DESCRIPTIONS";

    /**
     * The width of the root element.
     * <p>
     * This value is typically used to configure the width of the root element on diagrams that represent their root
     * (e.g. Activity or Communication diagrams).
     * </p>
     */
    public static final String ROOT_ELEMENT_WIDTH = "700";

    /**
     * The height of the root element.
     * <p>
     * This value is typically used to configure the height of the root element on diagrams that represent their root
     * (e.g. Activity or Communication diagrams).
     * </p>
     */
    public static final String ROOT_ELEMENT_HEIGHT = "550";

    /**
     * Nodes tool section name.
     */
    public static final String NODES = "Nodes";

    /**
     * Edges tool section name.
     */
    public static final String EDGES = "Edges";

    /**
     * Underscore.
     */
    public static final String UNDERSCORE = "_";

    /**
     * AQL expression used to specify gap size at the end of compartment.
     */
    private static final String GAP_SIZE = "aql:15";

    private static final String ICON_PATH = "/icons/full/obj16/";

    private static final String SHOWTOOL = "ShowTool";

    private static final String HIDETOOL = "HideTool";

    private static final String ICON_SVG_EXTENSION = ".svg";

    protected StyleProvider styleProvider;

    private final UMLPackage pack = UMLPackage.eINSTANCE;

    private ViewBuilder viewBuilder;

    private final QueryHelper queryBuilder;

    private final IdBuilder idBuilder;

    private final String diagramPrefix;

    private final String representationName;

    private final EClass representationDomainClass;

    private final IDomainHelper umlMetaModelHelper = new UMLMetamodelHelper();

    public AbstractRepresentationDescriptionBuilder(String diagramPrefix, String representationName, EClass domainClass) {
        super();
        this.diagramPrefix = diagramPrefix;
        this.representationName = representationName;
        this.representationDomainClass = domainClass;
        this.queryBuilder = new QueryHelper(this.umlMetaModelHelper);
        this.idBuilder = new IdBuilder(diagramPrefix, this.umlMetaModelHelper);

    }

    protected NodeDescriptionBuilder newNodeBuilder(EClass semanticDomain, NodeStyleDescription style) {
        return new NodeDescriptionBuilder(this.idBuilder, this.queryBuilder, semanticDomain, style, this.umlMetaModelHelper);
    }

    protected ListCompartmentBuilder newListCompartmentBuilder() {
        return new ListCompartmentBuilder(this.getIdBuilder(), this.getViewBuilder(), this.getQueryBuilder(), this.getUmlMetaModelHelper())
                .withbottomGapExpression("aql:" + this.styleProvider.getCompartimentBottomGap());
    }

    protected IDomainHelper getUmlMetaModelHelper() {
        return this.umlMetaModelHelper;
    }

    protected void registerCallback(EObject owner, Runnable r) {
        owner.eAdapters().add(new CallbackAdapter(r));
    }

    public DiagramDescription createDiagramDescription(View view) {
        this.styleProvider = new StyleProvider(view, this.diagramPrefix);
        this.viewBuilder = new ViewBuilder(this.queryBuilder, this.styleProvider, this.idBuilder, this.umlMetaModelHelper);

        DiagramDescription diagramDescription = this.getViewBuilder().buildDiagramDescription(this.representationName, this.representationDomainClass);

        diagramDescription.setTitleExpression(MessageFormat.format("aql:''{0}''", this.representationName));

        this.fillDescription(diagramDescription);

        EMFUtils.allContainedObjectOfType(diagramDescription, DiagramElementDescription.class).forEach(this::addConditionalLabelStyle);

        this.runCallbacks(diagramDescription);

        this.sortPaletteTools(diagramDescription);

        view.getDescriptions().add(diagramDescription);

        return diagramDescription;
    }

    public String getRepresentationName() {
        return this.representationName;
    }

    private boolean mayHaveLabelConditionalLabelStyle(DiagramElementDescription description) {
        final boolean result;
        if (description instanceof EdgeDescription) {
            result = ((EdgeDescription) description).isIsDomainBasedEdge();
        } else if (description instanceof NodeDescription) {
            NodeDescription nodeDescription = (NodeDescription) description;
            result = !IdBuilder.isCompartmentNode(nodeDescription);
        } else {
            result = false;
        }
        return result;

    }

    private void addConditionalLabelStyle(DiagramElementDescription description) {
        if (this.mayHaveLabelConditionalLabelStyle(description)) {
            EClass domainType = UMLHelper.toEClass(description.getDomainType());

            if (domainType != null) {
                // We use the feature name here to retrieve feature since the style customization can match more than
                // one feature
                // with the same name. For example:
                // * UMLPackage.eINSTANCE.getClassifier_IsAbstract() and
                // * UMLPackage.eINSTANCE.getBehavioralFeature_IsAbstract()

                // Abstract
                EStructuralFeature abstractFeature = domainType.getEStructuralFeature("isAbstract");
                boolean canBeAbstract = abstractFeature != null;

                // Static
                EStructuralFeature staticFeature = domainType.getEStructuralFeature("isStatic");
                boolean canBeStatic = staticFeature != null;

                if (canBeAbstract && canBeStatic) {

                    new LabelConditionalStyleBuilder(description, and(queryAttributeOnSelf(abstractFeature), queryAttributeOnSelf(staticFeature)).toString())//
                            .fromExistingStyle()//
                            .setItalic(true)//
                            .setUnderline(true);

                }

                if (canBeAbstract) {
                    new LabelConditionalStyleBuilder(description, queryAttributeOnSelf(abstractFeature))//
                            .fromExistingStyle()//
                            .setItalic(true);

                }

                if (canBeStatic) {
                    new LabelConditionalStyleBuilder(description, queryAttributeOnSelf(staticFeature))//
                            .fromExistingStyle()//
                            .setUnderline(true);
                }
            }
        }

    }

    private void runCallbacks(DiagramDescription diagramDescription) {

        EMFUtils.eAllContentStreamWithSelf(diagramDescription).forEach(e -> {
            List<CallbackAdapter> callacks = e.eAdapters().stream()//
                    .filter(adapter -> adapter instanceof CallbackAdapter)//
                    .map(adapter -> (CallbackAdapter) adapter)//
                    .collect(toList());

            for (var callback : callacks) {
                callback.run();
                e.eAdapters().remove(callback);
            }
        });

    }

    protected abstract void fillDescription(DiagramDescription diagramDescription);

    protected ViewBuilder getViewBuilder() {
        return this.viewBuilder;
    }

    protected QueryHelper getQueryBuilder() {
        return this.queryBuilder;
    }

    protected IdBuilder getIdBuilder() {
        return this.idBuilder;
    }

    /**
     * Collects all {@link NodeDescription} matching the given domain (<b>By default the compartment nodes and the list
     * item are excluded).
     *
     * @param description
     *            the diagram description
     * @param domains
     *            the list of matching domain
     * @return a list of matching {@link NodeDescription}
     * @see IdBuilder#isCompartmentNode(NodeDescription)
     */
    protected List<NodeDescription> collectNodesWithDomain(DiagramDescription description, EClass... domains) {
        return this.collectNodesWithDomain(description, false, false, domains);
    }

    /**
     * Collects all {@link NodeDescription} matching the given domain.
     *
     * @param description
     *            the diagram description
     * @param includeCompartment
     *            holds <code>true</code> if the compartment should be included in the result
     * @param domains
     *            the list of matching domain
     * @return a list of matching {@link NodeDescription}
     * @see IdBuilder#isCompartmentNode(NodeDescription)
     */
    protected List<NodeDescription> collectNodesWithDomain(DiagramDescription description, boolean includeCompartment, boolean includeListItem, EClass... domains) {
        return EMFUtils.allContainedObjectOfType(description, NodeDescription.class).filter(node -> this.isValidNodeDescription(node, includeCompartment, includeListItem, domains)).collect(toList());
    }

    /**
     * Collects all the {@link NodeDescription} matching the given {@code domains}, excluding the ones matching the
     * provided {@code forbiddenDomains}.
     * <p>
     * This method is typically used to collect a given domain class and exclude some of its sub-classes.
     * </p>
     *
     * @param description
     *            the diagram description
     * @param domains
     *            the list of matching domain types
     * @param forbiddenDomains
     *            the list of domain types to exclude
     * @return a list of matching {@link NodeDescription}
     */
    protected List<NodeDescription> collectNodesWithDomainAndFilter(DiagramDescription description, List<EClass> domains, List<EClass> forbiddenDomains) {
        List<NodeDescription> forbiddenDescription = this.collectNodesWithDomain(description, forbiddenDomains.toArray(EClass[]::new));
        return this.collectNodesWithDomain(description, domains.toArray(EClass[]::new)).stream() //
                .filter(nd -> !SHARED_DESCRIPTIONS.equals(nd.getName())) //
                .filter(nd -> !forbiddenDescription.contains(nd)) //
                .toList();
    }

    private boolean isCompartmentChildren(NodeDescription n) {
        EObject parent = n.eContainer();
        if (parent instanceof NodeDescription) {
            return IdBuilder.isCompartmentNode((NodeDescription) parent);
        }
        return false;
    }

    protected boolean isValidNodeDescription(NodeDescription node, boolean includeCompartment, boolean includeListItem, EClass... domains) {
        final boolean result;
        if (!includeCompartment && IdBuilder.isCompartmentNode(node)) {
            result = false;
        } else if (!includeCompartment && this.isCompartmentChildren(node)) {
            result = false;
        } else {
            EClass targetDomain = UMLHelper.toEClass(node.getDomainType());
            if (targetDomain != null) {
                result = Stream.of(domains).anyMatch(d -> d.isSuperTypeOf(targetDomain));
            } else {
                result = false;
            }
        }
        return result && !IdBuilder.isFakeChildNode(node);
    }

    /**
     * Creates a {@link NodeDescription} representing a {@link Comment} in the provided {@code diagramDescription}.
     * <p>
     * This method is <b>deprecated</b>. Use {@link NoteStyleDescriptionBuilder} to create note-style
     * {@link NodeDescription}.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the {@link NodeDescription}
     */
    @Deprecated
    protected void createCommentDescription(DiagramDescription diagramDescription) {
        NodeDescription commentDescription = this.getViewBuilder().createNoteStyleUnsynchonizedNodeDescription(this.pack.getComment(),
                this.getQueryBuilder().queryAllReachable(this.pack.getComment()));
        commentDescription.setDefaultWidthExpression("200");
        commentDescription.setDefaultHeightExpression("100");

        NoteNodeStyleDescription style = (NoteNodeStyleDescription) commentDescription.getStyle();
        commentDescription.getInsideLabel().getStyle().setShowIconExpression("aql:true");
        commentDescription.getInsideLabel().setPosition(InsideLabelPosition.TOP_LEFT);
        commentDescription.getInsideLabel().setTextAlign(LabelTextAlign.LEFT);
        style.setBackground(this.styleProvider.getNoteColor());
        diagramDescription.getNodeDescriptions().add(commentDescription);
        diagramDescription.getPalette().getNodeTools().add(this.getViewBuilder().createCreationTool(this.pack.getElement_OwnedComment(), this.pack.getComment()));

        EdgeDescription annotedElementEdge = this.getViewBuilder().createFeatureEdgeDescription(//
                this.getIdBuilder().getFeatureBaseEdgeId(this.pack.getComment_AnnotatedElement()), //
                this.getQueryBuilder().emptyString(), //
                queryAttributeOnSelf(this.pack.getComment_AnnotatedElement()), //
                () -> this.collectNodesWithDomain(diagramDescription, this.pack.getComment()), //
                () -> this.collectNodesWithDomain(diagramDescription, this.pack.getElement()));

        DeleteTool deleteTool = DiagramFactory.eINSTANCE.createDeleteTool();
        deleteTool.setName("Remove annotated element");
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression(
                CallQuery.queryServiceOnSelf(Services.REMOVE_VALUE_FROM, this.getQueryBuilder().aqlString(this.pack.getComment_AnnotatedElement().getName()), Variables.SEMANTIC_EDGE_TARGET));
        deleteTool.getBody().add(createElement);

        annotedElementEdge.getPalette().setDeleteTool(deleteTool);

        this.addAnnotatedElementReconnectionTools(annotedElementEdge);

        annotedElementEdge.getStyle().setTargetArrowStyle(ArrowStyle.NONE);
        annotedElementEdge.getStyle().setLineStyle(LineStyle.DASH);
        diagramDescription.getEdgeDescriptions().add(annotedElementEdge);

        this.registerCallback(commentDescription, () -> {
            EdgeTool creationTool = this.getViewBuilder().createFeatureBasedEdgeTool("Link",
                    this.getQueryBuilder().queryAddValueTo(Variables.SEMANTIC_EDGE_SOURCE, this.pack.getComment_AnnotatedElement(), Variables.SEMANTIC_EDGE_TARGET), //
                    this.collectNodesWithDomain(diagramDescription, this.pack.getElement()));
            commentDescription.getPalette().getEdgeTools().add(creationTool);
        });
    }

    /**
     * Creates the {@link NodeDescription}, {@link EdgeDescription}, and tools representing an UML {@link Comment}.
     * <p>
     * This method is used to create a shared {@link Comment} {@link NodeDescription} that is attached to all the
     * provided {@code owners}. Note that this method replaces {@link #createCommentDescription(DiagramDescription)},
     * which is deprecated and should be removed. The created {@link NodeDescription} is contained in the given
     * {@code parentNodeDescription}.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param parentNodeDescription
     *            the {@link NodeDescription} containing the shared {@link Comment} {@link NodeDescription}
     * @param toolSectionName
     *            name of the tool section to add the comment tool
     * @param owners
     *            the semantic types that can contain a {@link Comment}
     */
    protected void createCommentSubNodeDescription(DiagramDescription diagramDescription, NodeDescription parentNodeDescription, String toolSectionName, String commentNodeDescriptionName,
            List<EClass> owners) {
        NodeDescription commentDescription = new NoteStyleDescriptionBuilder(this.getIdBuilder(), this.getViewBuilder(), this.getQueryBuilder()) //
                .withName(commentNodeDescriptionName) //
                .withColor(this.styleProvider.getNoteColor()) //
                .withDomainType(this.pack.getComment()) //
                .withAnnotedDomainType(this.pack.getElement()) //
                .withReconnectSourceService(Services.RECONNECT_COMMENT_ANNOTATED_ELEMENT_EDGE_SOURCE_SERVICE) //
                .withReconnectTargetService(Services.RECONNECT_COMMENT_ANNOTATED_ELEMENT_EDGE_TARGET_SERVICE) //
                .withContainmentReference(this.pack.getElement_OwnedComment()) //
                .withNoteToElementReference(this.pack.getComment_AnnotatedElement()) //
                .buildIn(diagramDescription, parentNodeDescription);

        NodeTool nodeTool = this.getViewBuilder().createCreationTool(this.pack.getElement_OwnedComment(), this.pack.getComment());
        this.reuseNodeAndCreateTool(commentDescription, diagramDescription, nodeTool, toolSectionName, owners.toArray(EClass[]::new));
    }

    /**
     * Creates the {@link NodeDescription}, {@link EdgeDescription}, and tools representing an UML {@link Comment} on
     * the diagram background.
     * <p>
     * This method is used to create a {@link Comment} {@link NodeDescription} that can be represented on the background
     * of the diagram. The created {@link NodeDescription} can represent any {@link Comment} in the diagram, even if it
     * isn't contained by the diagram itself. The created {@link NodeDescription} is contained in the provided
     * {@code diagramDescription}.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the {@link Comment} {@link NodeDescription}
     * @param toolSectionName
     *            name of the tool section to add the comment tool
     */
    protected void createCommentTopNodeDescription(DiagramDescription diagramDescription, String toolSectionName) {
        new NoteStyleDescriptionBuilder(this.getIdBuilder(), this.getViewBuilder(), this.getQueryBuilder()) //
                .withColor(this.styleProvider.getNoteColor()) //
                .withDomainType(this.pack.getComment()) //
                .withAnnotedDomainType(this.pack.getElement()) //
                .withSemanticCandidateExpression(this.getQueryBuilder().queryAllReachable(this.pack.getComment())) //
                .withReconnectSourceService(Services.RECONNECT_COMMENT_ANNOTATED_ELEMENT_EDGE_SOURCE_SERVICE) //
                .withReconnectTargetService(Services.RECONNECT_COMMENT_ANNOTATED_ELEMENT_EDGE_TARGET_SERVICE) //
                .withContainmentReference(this.pack.getElement_OwnedComment()) //
                .withNoteToElementReference(this.pack.getComment_AnnotatedElement()) //
                .buildIn(diagramDescription);
        NodeTool nodeTool = this.viewBuilder.createCreationTool(this.pack.getElement_OwnedComment(), this.pack.getComment());
        this.addDiagramToolInToolSection(diagramDescription, nodeTool, toolSectionName);
    }

    /**
     * Creates the {@link NodeDescription}, {@link EdgeDescription}, and tools representing an UML {@link Constraint}.
     * <p>
     * This method is used to create a shared {@link Constraint} {@link NodeDescription} that is attached to all the
     * provided {@code owners}. The created {@link NodeDescription} is contained in the given
     * {@code parentNodeDescription}.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param parentNodeDescription
     *            the {@link NodeDescription} containing the shared {@link Constraint} {@link NodeDescription}
     * @param toolSectionName
     *            name of the tool section to add the comment tool
     * @param owners
     *            the semantic types that can contain a {@link Constraint}
     */
    protected void createConstraintSubNodeDescription(DiagramDescription diagramDescription, NodeDescription parentNodeDescription, String toolSectionName,
            String constraintNodeDescriptionName, List<EClass> owners) {
        NodeDescription constraintDescription = new NoteStyleDescriptionBuilder(this.getIdBuilder(), this.getViewBuilder(), this.getQueryBuilder()) //
                .withName(constraintNodeDescriptionName) //
                .withColor(this.styleProvider.getConstraintColor()) //
                .withDomainType(this.pack.getConstraint()) //
                .withAnnotedDomainType(this.pack.getElement()) //
                .withReconnectSourceService(Services.RECONNECT_CONSTRAINT_CONSTRAINED_ELEMENT_EDGE_SOURCE_SERVICE)
                .withReconnectTargetService(Services.RECONNECT_CONSTRAINT_CONSTRAINED_ELEMENT_EDGE_TARGET_SERVICE) //
                .withContainmentReference(this.pack.getNamespace_OwnedRule()) //
                .withNoteToElementReference(this.pack.getConstraint_ConstrainedElement()) //
                .buildIn(diagramDescription, parentNodeDescription);

        NodeTool nodeTool = this.getViewBuilder().createCreationTool(this.pack.getNamespace_OwnedRule(), this.pack.getConstraint());
        this.reuseNodeAndCreateTool(constraintDescription, diagramDescription, nodeTool, toolSectionName, owners.toArray(EClass[]::new));
    }

    /**
     * Creates the {@link NodeDescription}, {@link EdgeDescription}, and tools representing an UML {@link Constraint} on
     * the diagram background.
     * <p>
     * This method is used to create a {@link Constraint} {@link NodeDescription} that can be represented on the
     * background of the diagram. The created {@link NodeDescription} can represent any {@link Constraint} in the
     * diagram, even if it isn't contained by the diagram itself. The created {@link NodeDescription} is contained in
     * the provided {@code diagramDescription}.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the {@link Constraint} {@link NodeDescription}
     * @param toolSectionName
     *            name of the tool section to add the comment tool
     */
    protected void createConstraintTopNodeDescription(DiagramDescription diagramDescription, String toolSectionName) {
        new NoteStyleDescriptionBuilder(this.getIdBuilder(), this.getViewBuilder(), this.getQueryBuilder()) //
                .withColor(this.styleProvider.getConstraintColor()) //
                .withDomainType(this.pack.getConstraint()) //
                .withAnnotedDomainType(this.pack.getElement()) //
                .withSemanticCandidateExpression(this.getQueryBuilder().queryAllReachable(this.pack.getConstraint())) //
                .withReconnectSourceService(Services.RECONNECT_CONSTRAINT_CONSTRAINED_ELEMENT_EDGE_SOURCE_SERVICE)
                .withReconnectTargetService(Services.RECONNECT_CONSTRAINT_CONSTRAINED_ELEMENT_EDGE_TARGET_SERVICE) //
                .withContainmentReference(this.pack.getNamespace_OwnedRule()) //
                .withNoteToElementReference(this.pack.getConstraint_ConstrainedElement()) //
                .buildIn(diagramDescription);
        NodeTool nodeTool = this.viewBuilder.createCreationTool(this.pack.getNamespace_OwnedRule(), this.pack.getConstraint());
        this.addDiagramToolInToolSection(diagramDescription, nodeTool, toolSectionName);
    }

    private void addAnnotatedElementReconnectionTools(EdgeDescription annotedElementEdge) {
        ChangeContext sourceReconnectionOperation = this.getViewBuilder().createChangeContextOperation(new CallQuery(SEMANTIC_OTHER_END)
                .callService(Services.RECONNECT_COMMENT_ANNOTATED_ELEMENT_EDGE_SOURCE_SERVICE, Variables.SEMANTIC_RECONNECTION_SOURCE, Variables.SEMANTIC_RECONNECTION_TARGET));
        annotedElementEdge.getPalette().getEdgeReconnectionTools().add(this.getViewBuilder().createSourceReconnectionTool(annotedElementEdge, //
                this.getIdBuilder().getSourceReconnectionToolId(annotedElementEdge), //
                List.of(sourceReconnectionOperation)));

        ChangeContext targetReconnectionOperation = this.getViewBuilder().createChangeContextOperation(//
                new CallQuery(Variables.EDGE_SEMANTIC_ELEMENT).callService(Services.RECONNECT_COMMENT_ANNOTATED_ELEMENT_EDGE_TARGET_SERVICE, Variables.SEMANTIC_RECONNECTION_SOURCE, //
                        Variables.SEMANTIC_RECONNECTION_TARGET));
        annotedElementEdge.getPalette().getEdgeReconnectionTools().add(this.getViewBuilder().createTargetReconnectionTool(annotedElementEdge, //
                this.getIdBuilder().getTargetReconnectionToolId(annotedElementEdge), //
                List.of(targetReconnectionOperation)));
    }

    /**
     * Configures the provided {@code node} as a {@link Comment} owner.
     * <p>
     * This method ensures that the provided {@code node}'s child descriptions contain the {@link NodeDescription}
     * representing a {@link Comment}, and that the palette allows the creation of {@link Comment} elements inside
     * {@code node}.
     * </p>
     *
     * @param node
     *            the node to configure as a comment owner
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the node
     */
    protected void registerNodeAsCommentOwner(NodeDescription node, DiagramDescription diagramDescription) {
        this.registerCallback(node, () -> {
            node.getReusedChildNodeDescriptions().addAll(this.collectNodesWithDomain(diagramDescription, this.pack.getComment()));
            node.getPalette().getNodeTools().add(this.getViewBuilder().createCreationTool(this.pack.getElement_OwnedComment(), this.pack.getComment()));
        });
    }

    /**
     * Configures the provided {@code node} as a {@link Constraint} owner.
     * <p>
     * This method ensures that the provided {@code node}'s child descriptions contain the {@link NodeDescription}
     * representing a {@link Constraint}, and that the palette allows the creation of {@link Constraint} elements inside
     * {@code node}.
     *
     * @param node
     *            the node to configure as a constraint owner
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the node
     */
    protected void registerNodeAsConstraintOwner(NodeDescription node, DiagramDescription diagramDescription) {
        this.registerCallback(node, () -> {
            node.getReusedChildNodeDescriptions().addAll(this.collectNodesWithDomain(diagramDescription, this.pack.getConstraint()));
            node.getPalette().getNodeTools().add(this.getViewBuilder().createCreationTool(this.pack.getNamespace_OwnedRule(), this.pack.getConstraint()));
        });
    }

    /**
     * Reuses the provided {@code nodeDescription} as a child of the {@link NodeDescription} representing
     * {@code owners}.
     * <p>
     * This method provides a one-line way to reuse mappings and attach creation tools in a diagram. The provided
     * {@code nodeDescription} is set as a reused element for each {@link NodeDescription} representing the provided
     * {@code owners}, and the provided {@code nodeTool} is attached to the owning {@link NodeDescription}s. Note that
     * the provided {@code nodeDescription} is added to either {@link NodeDescription#getReusedChildNodeDescriptions()}
     * or {@link NodeDescription#getReusedBorderNodeDescriptions()}, depending on whether it is a regular node or a
     * border node.
     * <p>
     * See {@link #reuseAsChild(NodeDescription, DiagramDescription, NodeTool, List, List)} to finely tune the which
     * {@link NodeDescription}s can own the provided {@code nodeDescription}.
     * <p>
     * <b>Note</b>: this method relies on the <i>callback</i> mechanism, meaning the the
     * {@link NodeDescription#getReusedChildNodeDescriptions()} and the creation tools are updated once all the
     * descriptions have been created.
     * </p>
     *
     * @param nodeDescription
     *            the {@link NodeDescription} to reuse
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}s
     * @param owners
     *            the type of the {@link NodeDescription} to setup to reuse the provided {@code nodeDescription}
     *
     * @see #reuseAsChild(NodeDescription, DiagramDescription, NodeTool, List, List)
     */
    public void reuseNodeAndCreateTool(NodeDescription nodeDescription, DiagramDescription diagramDescription, NodeTool nodeTool, EClass... owners) {
        this.reuseNodeAndCreateTool(nodeDescription, diagramDescription, nodeTool, null, List.of(owners), List.of());
    }

    /**
     * Reuses the provided {@code nodeDescription} as a child of the {@link NodeDescription} representing
     * {@code owners}.
     * <p>
     * This method provides a one-line way to reuse mappings and attach creation tools in a diagram. The provided
     * {@code nodeDescription} is set as a reused element for each {@link NodeDescription} representing the provided
     * {@code owners}, and the provided {@code nodeTool} is attached to the owning {@link NodeDescription}s. Note that
     * the provided {@code nodeDescription} is added to either {@link NodeDescription#getReusedChildNodeDescriptions()}
     * or {@link NodeDescription#getReusedBorderNodeDescriptions()}, depending on whether it is a regular node or a
     * border node.
     * <p>
     * See {@link #reuseAsChild(NodeDescription, DiagramDescription, NodeTool, List, List)} to finely tune the which
     * {@link NodeDescription}s can own the provided {@code nodeDescription}.
     * <p>
     * <b>Note</b>: this method relies on the <i>callback</i> mechanism, meaning the the
     * {@link NodeDescription#getReusedChildNodeDescriptions()} and the creation tools are updated once all the
     * descriptions have been created.
     * </p>
     *
     * @param nodeDescription
     *            the {@link NodeDescription} to reuse
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}s
     * @param toolSectionName
     *            name of the tool section to add the tool
     * @param owners
     *            the type of the {@link NodeDescription} to setup to reuse the provided {@code nodeDescription}
     *
     * @see #reuseAsChild(NodeDescription, DiagramDescription, NodeTool, List, List)
     */
    public void reuseNodeAndCreateTool(NodeDescription nodeDescription, DiagramDescription diagramDescription, NodeTool nodeTool, String toolSectionName, EClass... owners) {
        this.reuseNodeAndCreateTool(nodeDescription, diagramDescription, nodeTool, toolSectionName, List.of(owners), List.of());
    }

    /**
     * Reuses the provided {@code nodeDescription} as a child of the {@link NodeDescription} representing
     * {@code owners}.
     * <p>
     * This method provides a one-line way to reuse mappings and attach creation tools in a diagram. The provided
     * {@code nodeDescription} is set as a reused element for each {@link NodeDescription} representing the provided
     * {@code owners}, and the provided {@code nodeTool} is attached to the owning {@link NodeDescription}s. Note that
     * the provided {@code nodeDescription} is added to either {@link NodeDescription#getReusedChildNodeDescriptions()}
     * or {@link NodeDescription#getReusedBorderNodeDescriptions()}, depending on whether it is a regular node or a
     * border node.
     * </p>
     * <p>
     * <b>Note</b>: this method relies on the <i>callback</i> mechanism, meaning the the
     * {@link NodeDescription#getReusedChildNodeDescriptions()} and the creation tools are updated once all the
     * descriptions have been created.
     * </p>
     *
     * @param nodeDescription
     *            the {@link NodeDescription} to reuse
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}s
     * @param toolSectionName
     *            name of the tool section to add the tool
     * @param owners
     *            the type of the {@link NodeDescription} to setup to reuse the provided {@code nodeDescription}
     *
     * @see #reuseAsChild(NodeDescription, DiagramDescription, NodeTool, List, List)
     */
    public void reuseNodeAndCreateTool(NodeDescription nodeDescription, DiagramDescription diagramDescription, NodeTool nodeTool, String toolSectionName, List<EClass> owners,
            List<EClass> forbiddenOwners) {
        this.registerCallback(nodeDescription, () -> {
            Supplier<List<NodeDescription>> ownerNodeDescriptions = () -> this.collectNodesWithDomainAndFilter(diagramDescription, owners, forbiddenOwners);
            this.addNodeToolInToolSection(ownerNodeDescriptions.get(), nodeTool, toolSectionName);
            this.reusedNodeDescriptionInOwners(nodeDescription, ownerNodeDescriptions.get());
        });
    }

    /**
     * This method add the node description as a growable node of its {@code resizableParents} list.
     *
     * It reuses the provided {@code nodeDescription} as a child of the {@link NodeDescription} representing
     * {@code owners}, reuse mapping and creation tools using:
     *
     *
     * @see #reuseNodeAndCreateTool(NodeDescription, DiagramDescription, NodeTool, String, List, List)
     *
     * @param nodeDescription
     *            the {@link NodeDescription} to reuse
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}s
     * @param toolSectionName
     *            name of the tool section to add the tool
     * @param owners
     *            the type of the {@link NodeDescription} to setup to reuse the provided {@code nodeDescription}
     * @param resizableParents
     *            the type of the {@link NodeDescription} to setup to reuse the provided {@code nodeDescription}
     */
    public void reuseNodeAndCreateTool(NodeDescription nodeDescription, DiagramDescription diagramDescription, NodeTool nodeTool, String toolSectionName, List<EClass> owners,
            List<EClass> forbiddenOwners, List<EClass> resizableParents) {
        this.reuseNodeAndCreateTool(nodeDescription, diagramDescription, nodeTool, toolSectionName, owners,
                forbiddenOwners);
        this.registerCallback(nodeDescription, () -> {
            Supplier<List<NodeDescription>> growableNodeDescriptions = () -> this.collectNodesWithDomainAndFilter(diagramDescription, resizableParents, forbiddenOwners);
            this.addGrowableNodesDescriptionInOwners(nodeDescription, growableNodeDescriptions.get());
        });
    }

    /**
     * This method add the node description as a growable node of its {@code resizableParents} list.
     *
     * @param nodeDescription
     *            the {@link NodeDescription} to reuse
     * @param resizableParents
     *            the type of the {@link NodeDescription} to setup to reuse the provided {@code nodeDescription}
     */
    private void addGrowableNodesDescriptionInOwners(NodeDescription nodeDescription, List<NodeDescription> resizableParents) {
        for (NodeDescription owner : resizableParents) {
            if (owner != nodeDescription.eContainer()) {
                if (nodeDescription.eContainingFeature() == DiagramPackage.eINSTANCE.getNodeDescription_ChildrenDescriptions()) {
                    if (owner.getChildrenLayoutStrategy() instanceof ListLayoutStrategyDescription listLayoutDescription) {
                        listLayoutDescription.getGrowableNodes().add(nodeDescription);
                    }
                }
            }
        }
    }

    /**
     * Reuses the provided {@code nodeDescription} as a child of the {@link NodeDescription} representing
     * {@code owners}.
     * <p>
     * This method provides a one-line way to reuse mappings and attach creation tools in a diagram. The provided
     * {@code nodeDescription} is set as a reused element for each {@link NodeDescription} representing the provided
     * {@code owners}, and the provided {@code nodeTool} is attached to the owning {@link NodeDescription}s. Note that
     * the provided {@code nodeDescription} is added to either {@link NodeDescription#getReusedChildNodeDescriptions()}
     * or {@link NodeDescription#getReusedBorderNodeDescriptions()}, depending on whether it is a regular node or a
     * border node.
     * </p>
     * <p>
     * <b>Note</b>: this method relies on the <i>callback</i> mechanism, meaning the the
     * {@link NodeDescription#getReusedChildNodeDescriptions()} and the creation tools are updated once all the
     * descriptions have been created.
     * </p>
     *
     * @param nodeDescription
     *            the {@link NodeDescription} to reuse
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}s
     * @param toolSectionName
     *            name of the tool section to add the tool
     * @param owners
     *            the type of the {@link NodeDescription} to setup to reuse the provided {@code nodeDescription}
     *
     * @see #reuseAsChild(NodeDescription, DiagramDescription, NodeTool, List, List)
     */
    public void reuseNode(NodeDescription nodeDescription, DiagramDescription diagramDescription, List<EClass> owners,
            List<EClass> forbiddenOwners) {
        this.registerCallback(nodeDescription, () -> {
            Supplier<List<NodeDescription>> ownerNodeDescriptions = () -> this.collectNodesWithDomainAndFilter(diagramDescription, owners, forbiddenOwners);
            this.reusedNodeDescriptionInOwners(nodeDescription, ownerNodeDescriptions.get());
        });
    }

    /**
     * Reuses the provided {@code nodeDescription} as a child of the {@link NodeDescription} representing
     * {@code owners}.
     * <p>
     * The provided {@code nodeDescription} is set as a reused element for each {@link NodeDescription} representing the
     * provided {@code owners}. Note that the provided {@code nodeDescription} is added to either
     * {@link NodeDescription#getReusedChildNodeDescriptions()} or
     * {@link NodeDescription#getReusedBorderNodeDescriptions()}, depending on whether it is a regular node or a border
     * node.
     *
     *
     * @param nodeDescription
     *            the {@link NodeDescription} to reuse
     * @param owners
     *            the type of the {@link NodeDescription} to setup to reuse the provided {@code owners}
     */
    protected void reusedNodeDescriptionInOwners(NodeDescription nodeDescription, List<NodeDescription> owners) {
        for (NodeDescription owner : owners) {
            // If the owner is the direct parent of the nodeDescription there is no need to add it to its reused
            // descriptions. It is already in the children descriptions of the owner.
            if (owner != nodeDescription.eContainer()) {
                if (nodeDescription.eContainingFeature() == DiagramPackage.eINSTANCE.getNodeDescription_BorderNodesDescriptions()) {
                    owner.getReusedBorderNodeDescriptions().add(nodeDescription);
                } else if (nodeDescription.eContainingFeature() == DiagramPackage.eINSTANCE.getNodeDescription_ChildrenDescriptions()) {
                    owner.getReusedChildNodeDescriptions().add(nodeDescription);
                } else if (nodeDescription.eContainingFeature() == DiagramPackage.eINSTANCE.getDiagramDescription_NodeDescriptions()) {
                    owner.getReusedChildNodeDescriptions().add(nodeDescription);
                }
            }
        }
    }

    /**
     * Collects all {@link NodeDescriptions} with the given type and matching the given predicate. Then add them to the
     * reused children or reused border children feature.
     *
     * @param parent
     *            the parent node
     * @param type
     *            the domain of the {@link NodeDescription} to collect
     * @param diagramDescription
     *            the root container of the candidates {@link NodeDescription}s
     * @param filter
     *            a extra filter to select the candidates
     */
    protected void collectAndReusedChildNodes(NodeDescription parent, EClass type, DiagramDescription diagramDescription, Predicate<NodeDescription> filter) {
        this.registerCallback(parent, () -> {
            List<NodeDescription> childrenCandidates = this.collectNodesWithDomain(diagramDescription, type).stream().filter(filter).collect(toList());
            for (var candidate : childrenCandidates) {
                if (candidate.eContainingFeature() == DiagramPackage.eINSTANCE.getNodeDescription_BorderNodesDescriptions()) {
                    parent.getReusedBorderNodeDescriptions().addAll(childrenCandidates);
                } else if (candidate.eContainingFeature() == DiagramPackage.eINSTANCE.getNodeDescription_ChildrenDescriptions()) {
                    parent.getReusedChildNodeDescriptions().addAll(childrenCandidates);
                }
            }
        });
    }

    @Deprecated
    protected void createModelDescription(DiagramDescription diagramDescription) {
        NodeDescription padModel = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(this.pack.getModel(), this.getQueryBuilder().queryAllReachable(this.pack.getModel()));
        diagramDescription.getNodeDescriptions().add(padModel);
        diagramDescription.getPalette().getNodeTools().add(this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), this.pack.getModel()));

        padModel.getStyle().setBorderColor(this.styleProvider.getBorderNodeColor());
        ((PackageNodeStyleDescription) padModel.getStyle()).setBackground(this.styleProvider.getModelColor());
        this.collectAndReusedChildNodes(padModel, this.pack.getPackageableElement(), diagramDescription, PACKAGE_CHILDREN_FILTER);

        this.registerNodeAsCommentOwner(padModel, diagramDescription);
    }

    @Deprecated
    protected void createPackageDescription(DiagramDescription diagramDescription) {
        NodeDescription padPackage = this.getViewBuilder().createPackageStyleUnsynchonizedNodeDescription(this.pack.getPackage(), this.getQueryBuilder().queryAllReachable(this.pack.getPackage()));
        diagramDescription.getNodeDescriptions().add(padPackage);

        diagramDescription.getPalette().getNodeTools().add(this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), this.pack.getPackage()));

        this.registerCallback(padPackage, () -> {
            List<NodeDescription> packages = this.collectNodesWithDomain(diagramDescription, this.pack.getPackage());
            packages.forEach(p -> {
                p.getPalette().getNodeTools().add(this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), this.pack.getPackage()));
                p.getPalette().getNodeTools().add(this.getViewBuilder().createCreationTool(this.pack.getPackage_PackagedElement(), this.pack.getModel()));
            });
            String childrenCandidateExpression = CallQuery.queryAttributeOnSelf(UMLPackage.eINSTANCE.getPackage_PackagedElement());
            List<NodeDescription> copiedClassifier = diagramDescription.getNodeDescriptions().stream()
                    // Filter out NodeDescription representing a constraint. They can be contained by the
                    // Package.packagedElement reference (they are PackageableElements), but we don't want to support
                    // that, we want to support Namespace.ownedRule as the sole containment reference for constraints.
                    .filter(n -> this.isValidNodeDescription(n, false, false, this.pack.getPackageableElement()) && !this.isValidNodeDescription(n, false, false, this.pack.getConstraint()))
                    .map(n -> this.transformIntoPackageChildNode(n, childrenCandidateExpression, diagramDescription)).toList();
            padPackage.getChildrenDescriptions().addAll(copiedClassifier);
        });

        this.registerNodeAsCommentOwner(padPackage, diagramDescription);
        // Do not use registerNodeAsConstraintOwner here, this would have an impact on CDDiagramDescriptionBuilder,
        // CSDDiagramDescriptionBuilder, PADDiagramDescriptionBuilder, and SMDDigramDescriptionBuilder.
    }

    /**
     * Creates the {@link NodeDescription} containing all the <i>shared</i> {@link NodeDescription} of the diagram.
     * <p>
     * This node is fake: it is not intended to be visually represented in the diagram. It is used as a way to gather
     * all the shared elements in a common place.
     * </p>
     *
     * @param diagramDescription
     *            the Activity {@link DiagramDescription} containing the created {@link NodeDescription}
     * @return the created {@link NodeDescription}
     */
    protected NodeDescription createSharedDescription(DiagramDescription diagramDescription) {
        NodeDescription sharedNodeDescription = this.newNodeBuilder(UMLPackage.eINSTANCE.getElement(), this.getViewBuilder().createRectangularNodeStyle()) //
                .name(SHARED_DESCRIPTIONS) //
                .semanticCandidateExpression("aql:Sequence{}").synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED) //
                .layoutStrategyDescription(DiagramFactory.eINSTANCE.createFreeFormLayoutStrategyDescription()) //
                .insideLabelDescription(this.getViewBuilder().createDefaultInsideLabelDescription(false, false))
                .build();
        diagramDescription.getNodeDescriptions().add(sharedNodeDescription);
        // We do not want any to on that virtual container
        sharedNodeDescription.getPalette().getToolSections().clear();
        return sharedNodeDescription;
    }

    @Deprecated
    private NodeDescription transformIntoPackageChildNode(NodeDescription input, String semanticCandidateExpression, DiagramDescription diagramDescription) {
        EClass eClass = UMLHelper.toEClass(input.getDomainType());
        String id = this.getIdBuilder().getSpecializedDomainNodeName(eClass, PACKAGE_CHILD);
        NodeDescription n = new NodeSemanticCandidateExpressionTransformer().intoNewCanidateExpression(id, input, semanticCandidateExpression);

        if (UMLPackage.eINSTANCE.getPackage().isSuperTypeOf(eClass)) {
            this.collectAndReusedChildNodes(n, this.pack.getPackageableElement(), diagramDescription, PACKAGE_CHILDREN_FILTER);
            this.registerNodeAsCommentOwner(n, diagramDescription);
            // Do not use registerNodeAsConstraintOwner here, this would have an impact on CDDiagramDescriptionBuilder,
            // CSDDiagramDescriptionBuilder, PADDiagramDescriptionBuilder, and SMDDigramDescriptionBuilder.
        }
        return n;
    }

    /**
     * Comparator of {@link Tool} based of alphabetic order of the name of the tool.
     *
     * @author Arthur Daussy
     */
    class ToolComparator implements Comparator<Tool> {
        @Override
        public int compare(Tool obj1, Tool obj2) {
            int res;
            if (obj1 == obj2) {
                res = 0;
            } else if (obj1 == null) {
                res = -1;
            } else if (obj2 == null) {
                res = 1;
            } else {
                res = obj1.getName().compareTo(obj2.getName());
            }
            return res;
        }
    }

    private void sortPaletteTools(DiagramDescription diagramDescription) {
        ToolComparator comparator = new ToolComparator();
        // diagram palette first
        for (DiagramToolSection diagramToolSection : diagramDescription.getPalette().getToolSections()) {
            ECollections.sort(diagramToolSection.getNodeTools(), comparator);
        }
        ECollections.sort(diagramDescription.getPalette().getNodeTools(), comparator);
        // children node then
        diagramDescription.getNodeDescriptions().forEach(node -> this.sortPaletteTools(node, comparator));
        diagramDescription.getEdgeDescriptions().forEach(edge -> {
            ECollections.sort(edge.getPalette().getNodeTools(), comparator);
        });
    }

    private void sortPaletteTools(NodeDescription nodeDescription, ToolComparator comparator) {
        // reorder tools in tool sections of the nodeDescription palette
        for (NodeToolSection toolSection : nodeDescription.getPalette().getToolSections()) {
            ECollections.sort(toolSection.getNodeTools(), comparator);
            ECollections.sort(toolSection.getEdgeTools(), comparator);
        }
        // reorder tools without tool sections of the nodeDescription palette
        ECollections.sort(nodeDescription.getPalette().getNodeTools(), comparator);
        ECollections.sort(nodeDescription.getPalette().getEdgeTools(), comparator);

        nodeDescription.getChildrenDescriptions().forEach(node -> this.sortPaletteTools(node, comparator));
        nodeDescription.getBorderNodesDescriptions().forEach(node -> this.sortPaletteTools(node, comparator));
    }

    /**
     * Get the tool section from the palette of a given {@link NodeDescription} with the given name.
     *
     * @param nodeDescription
     *            the node description with the palette which contain the tool section
     * @param toolSectionName
     *            the name of the tool section to extract
     * @return the tool section from the palette of a given {@link NodeDescription} with the given name
     */
    public NodeToolSection getNodeToolSection(NodeDescription nodeDescription, String toolSectionName) {
        NodeToolSection nodeToolSection = null;
        if (toolSectionName != null) {
            nodeToolSection = nodeDescription.getPalette().getToolSections().stream().filter(toolSection -> toolSectionName.equals(toolSection.getName())).findFirst().orElse(null);
        }
        return nodeToolSection;
    }

    /**
     * Get the tool section from the palette of a given {@link DiagramDescription} with the given name.
     *
     * @param diagramDescription
     *            the diagram description with the palette which contain the tool section
     * @param toolSectionName
     *            the name of the tool section to extract
     * @return the tool section from the palette of a given {@link DiagramDescription} with the given name
     */
    public DiagramToolSection getDiagramToolSection(DiagramDescription diagramDescription, String toolSectionName) {
        DiagramToolSection diagramToolSection = null;
        if (toolSectionName != null) {
            diagramToolSection = diagramDescription.getPalette().getToolSections().stream().filter(toolSection -> toolSectionName.equals(toolSection.getName())).findFirst().orElse(null);
        }
        return diagramToolSection;
    }

    /**
     * Add given {@link EdgeTool} in Edges tool section of list of owners nodes descriptions.
     *
     * @param owners
     *            list of owners which should use the edge tool
     * @param edgeTool
     *            the edge tool to add in tool section
     */
    protected void addEdgeToolInEdgesToolSection(List<NodeDescription> owners, EdgeTool edgeTool) {
        for (NodeDescription owner : owners) {
            NodeToolSection nodeToolSection = this.getNodeToolSection(owner, EDGES);
            if (nodeToolSection == null) {
                owner.getPalette().getEdgeTools().add(EcoreUtil.copy(edgeTool));
            } else {
                nodeToolSection.getEdgeTools().add(EcoreUtil.copy(edgeTool));
            }
        }
    }

    /**
     * Add given {@link Nodes} in given tool section of list of owners nodes descriptions.
     *
     * @param owners
     *            list of owners which should use the node tool
     * @param nodeTool
     *            the node tool to add in tool section
     * @param toolSectionName
     *            name of the tool section which contain edge tool
     */
    protected void addNodeToolInToolSection(List<NodeDescription> owners, NodeTool nodeTool, String toolSectionName) {
        for (NodeDescription owner : owners) {
            NodeToolSection nodeToolSection = this.getNodeToolSection(owner, toolSectionName);
            if (nodeToolSection == null) {
                owner.getPalette().getNodeTools().add(EcoreUtil.copy(nodeTool));
            } else {
                nodeToolSection.getNodeTools().add(EcoreUtil.copy(nodeTool));
            }
        }
    }

    /**
     * Add given {@link Nodes} in given Diagram tool section.
     *
     * @param nodeTool
     *            the node tool to add in tool section
     * @param toolSectionName
     *            name of the tool section which contain edge tool
     */
    protected void addDiagramToolInToolSection(DiagramDescription diagramDescription, NodeTool nodeTool, String toolSectionName) {
        DiagramToolSection diagramToolSection = this.getDiagramToolSection(diagramDescription, toolSectionName);
        if (diagramToolSection == null) {
            diagramDescription.getPalette().getNodeTools().add(EcoreUtil.copy(nodeTool));
        } else {
            diagramToolSection.getNodeTools().add(EcoreUtil.copy(nodeTool));
        }
    }

    /**
     * Create tools sections "Nodes" and "Edges" in the palette tool of a given {@link NodeDescription}.
     *
     * @param nodeDescription
     *            the node description with the palette to complete with tool sections
     */
    protected void createDefaultToolSectionsInNodeDescription(NodeDescription nodeDescription) {
        NodeToolSection nodesToolSection = this.getViewBuilder().createNodeToolSection(NODES);
        NodeToolSection edgesToolSection = this.getViewBuilder().createNodeToolSection(EDGES);
        nodeDescription.getPalette().getToolSections().addAll(List.of(nodesToolSection, edgesToolSection));
    }

    /**
     * Create tools sections "Nodes" and "Edges" in the palette tool of a given {@link DiagramDescription}.
     *
     * @param nodeDescription
     *            the node description with the palette to complete with tool sections
     */
    protected void createDefaultToolSectionInDiagramDescription(DiagramDescription diagramDescription) {
        DiagramToolSection nodesToolSection = this.getViewBuilder().createDiagramToolSection(NODES);
        DiagramToolSection edgesToolSection = this.getViewBuilder().createDiagramToolSection(EDGES);
        diagramDescription.getPalette().getToolSections().addAll(List.of(nodesToolSection, edgesToolSection));
    }

    /**
     * Creates a shared compartment reused by the given owners {@code nodeDescription}.
     * <p>
     * The created {@link NodeDescription} compartment is added to the <i>shared</i> {@link NodeDescription} of the
     * diagram.
     * <p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param parentNodeDescription
     *            the {@link NodeDescription} used to contain the created {@link NodeDescription} compartment
     * @param domainType
     *            the domain type used to define the compartment
     * @param compartmentName
     *            the name of the compartment to create
     * @param owners
     *            the semantic types that can contain this compartment {@link NodeDescription}
     * @param forbiddenOwners
     *            the list of domain types to exclude
     * @param forbiddenNodeDescriptionPredicate
     *            predicate on the {@link NodeDescription} to exclude
     */
    protected NodeDescription createSharedCompartmentsDescription(DiagramDescription diagramDescription, NodeDescription parentNodeDescription, EClass domainType, String compartmentName,
            List<EClass> owners, List<EClass> forbiddenOwners, Predicate<NodeDescription> forbiddenNodeDescriptionPredicate) {
        ListLayoutStrategyDescription listLayoutStrategyDescription = DiagramFactory.eINSTANCE.createListLayoutStrategyDescription();
        listLayoutStrategyDescription.setBottomGapExpression(GAP_SIZE);
        NodeDescription sharedCompartmentDescription = this.newNodeBuilder(domainType, this.getViewBuilder().createRectangularNodeStyle())//
                .name(this.getIdBuilder().getSpecializedCompartmentDomainNodeName(domainType, compartmentName, SHARED_SUFFIX)) //
                .layoutStrategyDescription(listLayoutStrategyDescription)//
                .semanticCandidateExpression(this.getQueryBuilder().querySelf())//
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)//
                .collapsible(true)//
                .build();

        parentNodeDescription.getChildrenDescriptions().add(sharedCompartmentDescription);
        this.createDefaultToolSectionsInNodeDescription(sharedCompartmentDescription);

        // add reuse of compartment in its node description parent
        this.registerCallback(sharedCompartmentDescription, () -> {
            List<NodeDescription> ownerNodeDescriptions = this.collectNodesWithDomainAndFilter(diagramDescription, owners, forbiddenOwners).stream().filter(forbiddenNodeDescriptionPredicate).toList();
            this.reusedNodeDescriptionInOwners(sharedCompartmentDescription, ownerNodeDescriptions);
        });
        return sharedCompartmentDescription;
    }

    /**
     * Creates a {@link NodeDescription} reused in a {@link NodeDescription} compartment.
     * <p>
     * The created {@link NodeDescription} is added to the provided {@code parentNodeDescription}
     * {@link NodeDescription} and reused by the {@code owners} {@link NodeDescription}s.
     * <p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @param parentNodeDescription
     *            the {@link NodeDescription} used to contain the created {@link NodeDescription}
     * @param domainType
     *            the domain type used to define the new {@link NodeDescription}
     * @param compartmentName
     *            the name of the compartment which contain the child {@link NodeDescription} to create
     * @param semanticQuery
     *            the semantic candidate expression to get semantic element
     * @param semanticRefTool
     *            the containment reference to used for the creation
     * @param owners
     *            the semantic types that can contain this {@link NodeDescription}
     * @param forbiddenOwners
     *            the list of domain types to exclude
     * @param forbiddenNodeDescriptionPredicate
     *            predicate on the {@link NodeDescription} to exclude
     * @return the created {@link NodeDescription}
     */
    // CHECKSTYLE:OFF
    protected NodeDescription createSubNodeDescriptionInCompartmentDescription(DiagramDescription diagramDescription, NodeDescription parentNodeDescription, EClass domainType, String compartmentName,
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
        NodeTool prdSharedNodeDescriptionCreationTool = this.getViewBuilder().createCreationTool(semanticRefTool, domainType);
        String domain = this.getUmlMetaModelHelper().getDomain(domainType);
        this.registerCallback(createNodeDescriptionInCompartmentDescription, () -> {
            List<NodeDescription> ownerCompartmentNodeDescriptions = EMFUtils.allContainedObjectOfType(diagramDescription, NodeDescription.class) //
                    .filter(node -> IdBuilder.isCompartmentNode(node) && node.getName().contains(compartmentName)) //
                    .toList();
            this.addNodeToolInToolSection(ownerCompartmentNodeDescriptions, prdSharedNodeDescriptionCreationTool, NODES);
            this.reusedNodeDescriptionInOwners(createNodeDescriptionInCompartmentDescription, ownerCompartmentNodeDescriptions);
        });

        // Tool used to create node Node Description in Compartment from the parent of this compartment
        NodeTool prdSharedNodeDescriptionInCompartmentCreationTool = this.getViewBuilder().createInCompartmentCreationTool(this.getIdBuilder().getCreationToolId(domainType), compartmentName,
                semanticRefTool, domain);
        this.reuseTool(createNodeDescriptionInCompartmentDescription, diagramDescription, prdSharedNodeDescriptionInCompartmentCreationTool, owners, forbiddenOwners,
                forbiddenNodeDescriptionPredicate);

        return createNodeDescriptionInCompartmentDescription;
    }

    /**
     * Reuse the provided {@code nodeTool} in the owning {@link NodeDescription}s according to a given
     * {@code forbiddenNodeDescriptionPredicate}.
     * <p>
     * <b>Note</b>: this method relies on the <i>callback</i> mechanism, meaning tools are updated once all the
     * descriptions have been created.
     * </p>
     *
     * @param nodeDescription
     *            the {@link NodeDescription} to reuse
     * @param diagramDescription
     *            the Activity {@link DiagramDescription}s
     * @param nodeTool
     *            the {@link NodeTool} to reuse
     * @param owners
     *            the type of the {@link NodeDescription} to setup to reuse the provided {@code nodeDescription}
     * @param forbiddenOwners
     *            the type of the {@link NodeDescription} to exclude
     * @param forbiddenNodeDescriptionPredicate
     *            predicate on the {@link NodeDescription} to exclude
     */
    protected void reuseTool(NodeDescription nodeDescription, DiagramDescription diagramDescription, NodeTool nodeTool, List<EClass> owners, List<EClass> forbiddenOwners,
            Predicate<NodeDescription> forbiddenNodeDescriptionPredicate) {
        // Add tool on the parent of Compartment
        this.registerCallback(nodeDescription, () -> {
            List<NodeDescription> ownerToolDescription = this.collectNodesWithDomainAndFilter(diagramDescription, owners, forbiddenOwners) //
                    .stream().filter(forbiddenNodeDescriptionPredicate).toList();
            this.addNodeToolInToolSection(ownerToolDescription, nodeTool, NODES);
        });
    }

    /**
     * Create a Symbol NodeDescription and add it to the the SharedNodeDescription.
     *
     * @param dd
     * @param shared
     * @param owners
     * @param forbiddenOwners
     * @param compartmentName
     */
    public void createSymbolSharedNodeDescription(DiagramDescription dd, NodeDescription shared, List<EClass> owners, List<EClass> forbiddenOwners, String compartmentName) {
        NodeDescription nd = this.getViewBuilder().createSymbolNodeDescription();
        nd.setName(this.getIdBuilder().getSpecializedCompartmentDomainNodeName(this.pack.getElement(), compartmentName, SHARED_SUFFIX));
        shared.getChildrenDescriptions().add(nd);
        this.reuseNode(nd, dd, owners, forbiddenOwners);
    }

    /**
     * Create the 'Hide all symbols' Tool.
     *
     * @param diagramDescription
     * @param toolSectionName
     */
    protected void createHideSymbolTool(DiagramDescription diagramDescription, String toolSectionName) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName("Hide all symbols");
        nodeTool.setIconURLsExpression(ICON_PATH + HIDETOOL + ICON_SVG_EXTENSION);
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression("aql:diagramServices.hide(diagramContext.getAllSymbol(editingContext))");
        nodeTool.getBody().add(createElement);
        this.addDiagramToolInToolSection(diagramDescription, nodeTool, toolSectionName);
    }

    /**
     * Create the 'Show all symbols' Tool.
     *
     * @param diagramDescription
     * @param toolSectionName
     */
    protected void createShowSymbolTool(DiagramDescription diagramDescription, String toolSectionName) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName("Show all symbols");
        nodeTool.setIconURLsExpression(ICON_PATH + SHOWTOOL + ICON_SVG_EXTENSION);
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression("aql:diagramServices.reveal(diagramContext.getAllSymbol(editingContext))");
        nodeTool.getBody().add(createElement);
        this.addDiagramToolInToolSection(diagramDescription, nodeTool, toolSectionName);
    }

    /**
     * Create the 'Hide all other compartments' Tool.
     *
     * @param diagramDescription
     * @param toolSectionName
     */
    protected void createHideAllNonSymbolCompartmentTool(DiagramDescription diagramDescription, String toolSectionName) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName("Hide all other compartments");
        nodeTool.setIconURLsExpression(ICON_PATH + HIDETOOL + ICON_SVG_EXTENSION);
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression("aql:diagramServices.hide(diagramContext.getAllNonSymbol(editingContext))");
        nodeTool.getBody().add(createElement);
        this.addDiagramToolInToolSection(diagramDescription, nodeTool, toolSectionName);
    }

    /**
     * Create the 'Show all other compartments' Tool.
     *
     * @param diagramDescription
     * @param toolSectionName
     */
    protected void createShowAllNonSymbolCompartmentTool(DiagramDescription diagramDescription, String toolSectionName) {
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        nodeTool.setName("Show all other compartments");
        nodeTool.setIconURLsExpression(ICON_PATH + SHOWTOOL + ICON_SVG_EXTENSION);
        ChangeContext createElement = ViewFactory.eINSTANCE.createChangeContext();
        createElement.setExpression("aql:diagramServices.reveal(diagramContext.getAllNonSymbol(editingContext))");
        nodeTool.getBody().add(createElement);
        this.addDiagramToolInToolSection(diagramDescription, nodeTool, toolSectionName);
    }

}

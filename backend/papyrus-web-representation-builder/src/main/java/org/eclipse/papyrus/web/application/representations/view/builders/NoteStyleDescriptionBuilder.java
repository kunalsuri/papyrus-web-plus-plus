/*****************************************************************************
 * Copyright (c) 2023 CEA LIST, Obeo.
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
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.view.builders;

import static java.util.stream.Collectors.toList;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_OTHER_END;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.uml.domain.services.EMFUtils;
import org.eclipse.papyrus.uml.domain.services.UMLHelper;
import org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.application.representations.view.aql.CallQuery;
import org.eclipse.papyrus.web.application.representations.view.aql.QueryHelper;
import org.eclipse.papyrus.web.application.representations.view.aql.Services;
import org.eclipse.papyrus.web.application.representations.view.aql.Variables;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription;
import org.eclipse.sirius.components.view.ChangeContext;
import org.eclipse.sirius.components.view.UserColor;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.DeleteTool;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeToolSection;

/**
 * A builder to create note-styled nodes.
 * <p>
 * This builder handles the creation of the node description itself, but also the creation/deletion tools, as well as
 * the edge description between the note and the annotated elements and its corresponding tools.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@SuppressWarnings("checkstyle:HiddenField")
public class NoteStyleDescriptionBuilder {

    /**
     * The default width of notes created with this builder.
     */
    public static final String DEFAULT_NOTE_WIDTH = "200";

    /**
     * The default height of the notes created with this builder.
     */
    public static final String DEFAULT_NOTE_HEIGHT = "100";

    private IdBuilder idBuilder;

    private ViewBuilder viewBuilder;

    private QueryHelper queryBuilder;

    private EClass domainType;

    private EClass annotedDomainType;

    private EReference containmentReference;

    private EReference noteToElementReference;

    private String reconnectSourceService;

    private String reconnectTargetService;

    private UserColor color;

    private String semanticCandidateExpression;

    private String name;

    private NodeToolSection edgesToolSection;

    /**
     * Creates an instance of the builder with the provided parameters.
     *
     * @param idBuilder
     *            the builder used to generate identifiers
     * @param viewBuilder
     *            the builder used to create view elements
     * @param queryBuilder
     *            the builder used to define queries
     */
    public NoteStyleDescriptionBuilder(IdBuilder idBuilder, ViewBuilder viewBuilder, QueryHelper queryBuilder) {
        this.idBuilder = idBuilder;
        this.viewBuilder = viewBuilder;
        this.queryBuilder = queryBuilder;
    }

    /**
     * Sets the domain type represented by the created note.
     * <p>
     * This parameter is <b>mandatory</b>.
     * </p>
     *
     * @param domainType
     *            the domain type represented by the created note.
     * @return this builder
     */
    public NoteStyleDescriptionBuilder withDomainType(EClass domainType) {
        this.domainType = domainType;
        return this;
    }

    /**
     * Sets the domain type that can be annotated by the created note.
     * <p>
     * This parameter is <b>mandatory</b>.
     * </p>
     *
     * @param annotedDomainType
     *            the domain type that can be annotated by the note
     * @return this builder
     */
    public NoteStyleDescriptionBuilder withAnnotedDomainType(EClass annotedDomainType) {
        this.annotedDomainType = annotedDomainType;
        return this;
    }

    /**
     * Sets the containment reference for the type represented by the created note.
     * <p>
     * This parameter is <b>mandatory</b>.
     * </p>
     *
     * @param containmentReference
     *            the reference
     * @return this builder
     */
    public NoteStyleDescriptionBuilder withContainmentReference(EReference containmentReference) {
        this.containmentReference = containmentReference;
        return this;
    }

    /**
     * Sets the reference from the note to the annotated element.
     * <p>
     * This parameter is <b>mandatory</b>.
     * </p>
     *
     * @param noteToElementReference
     *            the reference
     * @return this builder
     */
    public NoteStyleDescriptionBuilder withNoteToElementReference(EReference noteToElementReference) {
        this.noteToElementReference = noteToElementReference;
        return this;
    }

    /**
     * Sets the color of the note.
     * <p>
     * This parameter is <b>mandatory</b>.
     * </p>
     *
     * @param color
     *            the color
     * @return this builder
     */
    public NoteStyleDescriptionBuilder withColor(UserColor color) {
        this.color = color;
        return this;
    }

    /**
     * Sets the service to use to reconnect the source of an edge between the note and the annotated element.
     * <p>
     * This parameter is <b>mandatory</b>.
     * </p>
     *
     * @param reconnectSourceService
     *            the name of the service
     * @return this builder
     */
    public NoteStyleDescriptionBuilder withReconnectSourceService(String reconnectSourceService) {
        this.reconnectSourceService = reconnectSourceService;
        return this;
    }

    /**
     * Sets the service to use to reconnect the target of an edge between the note and the annotated element.
     * <p>
     * This parameter is <b>mandatory</b>.
     * </p>
     *
     * @param reconnectTargetService
     *            the name of the service
     * @return this builder
     */
    public NoteStyleDescriptionBuilder withReconnectTargetService(String reconnectTargetService) {
        this.reconnectTargetService = reconnectTargetService;
        return this;
    }

    /**
     * Sets the semantic candidate expression of the {@link NodeDescription} to create.
     * <p>
     * This parameter is <b>optional</b>. If it isn't provided, the semantic candidate expression will match the
     * provided containment reference (see {@link #withContainmentReference(EReference)}).
     * </p>
     *
     * @param semanticCandidateExpression
     *            the semantic candidate expression to set in the {@link NodeDescription}
     * @return this builder
     */
    public NoteStyleDescriptionBuilder withSemanticCandidateExpression(String semanticCandidateExpression) {
        this.semanticCandidateExpression = semanticCandidateExpression;
        return this;
    }

    /**
     * Sets the name of the {@link NodeDescription} to create.
     * <p>
     * This parameter is <b>optional</b>. If it isn't provided, the name is computed based on the given
     * {@code domainType} (see {@link #withDomainType(EClass)}).
     * </p>
     *
     * @param name
     *            the name of the {@link NodeDescription}
     * @return this builder
     */
    public NoteStyleDescriptionBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Builds the {@link NodeDescription} representing the note in the provided {@code diagramDescription} at the
     * diagram level.
     * <p>
     * This method creates the node description itself, but also its deletion tool, as well as the edge representation
     * between the note and the annotated elements and its corresponding tools. The {@link NodeDescription} is then
     * added to parent {@code diagramDescription}.
     * </p>
     * <p>
     * This method does not create the creation tool for the {@link NodeDescription}.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @return the created {@link NodeDescription}
     *
     * @throws NullPointerException
     *             if one of the mandatory builder parameters is {@code null}.
     */
    public NodeDescription buildIn(DiagramDescription diagramDescription) {
        NodeDescription noteStyleDescription = this.createNoteStyleDescription(diagramDescription);
        noteStyleDescription.getInsideLabel().setOverflowStrategy(LabelOverflowStrategy.WRAP);
        diagramDescription.getNodeDescriptions().add(noteStyleDescription);
        this.createAnnotatedEdge(diagramDescription, noteStyleDescription);
        return noteStyleDescription;
    }

    /**
     * Builds the {@link NodeDescription} representing the note and adds it as the child of {@code nodeDescription}.
     * <p>
     * This method creates the node description itself, but also the deletion tool, as well as the edge representation
     * between the note and the annotated elements and its corresponding tools. The {@link NodeDescription} is then
     * added to parent {@code nodeDescription}.
     * </p>
     * <p>
     * This method does not create the creation tool for the {@link NodeDescription}.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     * @return the created {@link NodeDescription}
     *
     * @throws NullPointerException
     *             if one of the mandatory builder parameters is {@code null}.
     */
    public NodeDescription buildIn(DiagramDescription diagramDescription, NodeDescription nodeDescription) {
        NodeDescription noteStyleDescription = this.createNoteStyleDescription(diagramDescription);
        nodeDescription.getChildrenDescriptions().add(noteStyleDescription);
        this.createAnnotatedEdge(diagramDescription, noteStyleDescription);
        return noteStyleDescription;
    }

    /**
     * Create {@link NodeDescription} representing the note in the provided {@code diagramDescription}.
     * <p>
     * This method creates the node description itself, but also the creation/deletion tools, as well as the edge
     * representation between the note and the annotated elements and its corresponding tools.
     * </p>
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the created {@link NodeDescription}
     *
     * @throws NullPointerException
     *             if one of the mandatory builder parameters is {@code null}.
     */
    private NodeDescription createNoteStyleDescription(DiagramDescription diagramDescription) {
        Objects.requireNonNull(this.domainType);
        Objects.requireNonNull(this.annotedDomainType);
        Objects.requireNonNull(this.color);
        Objects.requireNonNull(this.containmentReference);
        Objects.requireNonNull(this.noteToElementReference);
        Objects.requireNonNull(this.reconnectSourceService);
        Objects.requireNonNull(this.reconnectTargetService);
        if (this.semanticCandidateExpression == null) {
            this.semanticCandidateExpression = CallQuery.queryAttributeOnSelf(this.containmentReference);
        }

        NodeDescription noteStyleDescription = this.viewBuilder.createNoteStyleUnsynchonizedNodeDescription(this.domainType, this.semanticCandidateExpression);
        if (this.name != null) {
            noteStyleDescription.setName(this.name);
        }

        noteStyleDescription.setDefaultWidthExpression(DEFAULT_NOTE_WIDTH);
        noteStyleDescription.setDefaultHeightExpression(DEFAULT_NOTE_HEIGHT);

        noteStyleDescription.getInsideLabel().getStyle().setShowIconExpression("aql:true");
        noteStyleDescription.getInsideLabel().setPosition(InsideLabelPosition.TOP_LEFT);
        noteStyleDescription.getInsideLabel().setTextAlign(LabelTextAlign.LEFT);

        NoteNodeStyleDescription style = (NoteNodeStyleDescription) noteStyleDescription.getStyle();
        style.setBackground(this.color);

        NodeToolSection nodesToolSection = this.viewBuilder.createNodeToolSection(AbstractRepresentationDescriptionBuilder.NODES);
        this.edgesToolSection = this.viewBuilder.createNodeToolSection(AbstractRepresentationDescriptionBuilder.EDGES);
        noteStyleDescription.getPalette().getToolSections().addAll(List.of(nodesToolSection, this.edgesToolSection));

        return noteStyleDescription;
    }

    /**
     * Builds the {@link EdgeDescription} associated tools for the provided {@code noteStyleDescription}.
     *
     * @param diagramDescription
     *            the {@link DiagramDescription} containing the {@link EdgeDescription}
     *
     * @param noteStyleDescription
     *            the {@link NodeStyleDescription} containing the edge creation tool
     */
    private void createAnnotatedEdge(DiagramDescription diagramDescription, NodeDescription noteStyleDescription) {
        String featureBaseEdgeId = this.idBuilder.getFeatureBaseEdgeId(this.noteToElementReference);
        if (diagramDescription.getEdgeDescriptions().stream().noneMatch(edgeDescription -> edgeDescription.getName().equals(featureBaseEdgeId))) {
            // The diagram doesn't contain the featureBaseEdgeId Edge, we have to create it.
            EdgeDescription annotedElementEdge = this.viewBuilder.createFeatureEdgeDescription(//
                    this.idBuilder.getFeatureBaseEdgeId(this.noteToElementReference), //
                    this.queryBuilder.emptyString(), //
                    CallQuery.queryAttributeOnSelf(this.noteToElementReference), //
                    () -> this.collectNodesWithDomain(diagramDescription, this.domainType), //
                    () -> this.collectNodesWithDomainAndWithoutContent(diagramDescription, this.annotedDomainType));
            DeleteTool deleteTool = DiagramFactory.eINSTANCE.createDeleteTool();
            deleteTool.setName("Remove " + this.domainType.getName());
            ChangeContext deleteToolChangeContext = ViewFactory.eINSTANCE.createChangeContext();
            deleteToolChangeContext
                    .setExpression(CallQuery.queryServiceOnSelf(Services.REMOVE_VALUE_FROM, this.queryBuilder.aqlString(this.noteToElementReference.getName()), Variables.SEMANTIC_EDGE_TARGET));
            deleteTool.getBody().add(deleteToolChangeContext);
            annotedElementEdge.getPalette().setDeleteTool(deleteTool);
            this.addAnnotatedElementReconnectionTools(annotedElementEdge);
            annotedElementEdge.getStyle().setTargetArrowStyle(ArrowStyle.NONE);
            annotedElementEdge.getStyle().setLineStyle(LineStyle.DASH);
            diagramDescription.getEdgeDescriptions().add(annotedElementEdge);
        }

        // Create the edge creation tool and attach it to the noteStyleDescription. This has to be done even if the Edge
        // already exists in the diagram.
        noteStyleDescription.eAdapters().add(new CallbackAdapter(() -> {
            EdgeTool creationTool = this.viewBuilder.createFeatureBasedEdgeTool("New Link",
                    this.queryBuilder.queryAddValueTo(Variables.SEMANTIC_EDGE_SOURCE, this.noteToElementReference, Variables.SEMANTIC_EDGE_TARGET), //
                    this.collectNodesWithDomain(diagramDescription, this.annotedDomainType));
            this.edgesToolSection.getEdgeTools().add(creationTool);
        }));

    }

    private void addAnnotatedElementReconnectionTools(EdgeDescription annotedElementEdge) {
        ChangeContext sourceReconnectionOperation = this.viewBuilder.createChangeContextOperation(
                new CallQuery(SEMANTIC_OTHER_END).callService(this.reconnectSourceService, Variables.SEMANTIC_RECONNECTION_SOURCE, Variables.SEMANTIC_RECONNECTION_TARGET));
        annotedElementEdge.getPalette().getEdgeReconnectionTools().add(this.viewBuilder.createSourceReconnectionTool(annotedElementEdge, //
                this.idBuilder.getSourceReconnectionToolId(annotedElementEdge), //
                List.of(sourceReconnectionOperation)));

        ChangeContext targetReconnectionOperation = this.viewBuilder.createChangeContextOperation(//
                new CallQuery(Variables.EDGE_SEMANTIC_ELEMENT).callService(this.reconnectTargetService, Variables.SEMANTIC_RECONNECTION_SOURCE, Variables.SEMANTIC_RECONNECTION_TARGET));
        annotedElementEdge.getPalette().getEdgeReconnectionTools().add(this.viewBuilder.createTargetReconnectionTool(annotedElementEdge, //
                this.idBuilder.getTargetReconnectionToolId(annotedElementEdge), //
                List.of(targetReconnectionOperation)));
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

    protected List<NodeDescription> collectNodesWithDomainAndWithoutContent(DiagramDescription description, EClass... domains) {
        return this.collectNodesWithDomain(description, domains).stream() //
                .filter(nd -> !"SHARED_DESCRIPTIONS".equals(nd.getName())) //
                .filter(nd -> !nd.getName().contains("Content"))
                .toList();
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

    private boolean isCompartmentChildren(NodeDescription n) {
        EObject parent = n.eContainer();
        if (parent instanceof NodeDescription) {
            return IdBuilder.isCompartmentNode((NodeDescription) parent);
        }
        return false;
    }

    /**
     * Returns whether the provided {@code node} matches the given {@code domains}.
     *
     * @param node
     *            the {@link NodeDescription} to check
     * @param includeCompartment
     *            {@code true} if the method should include compartment {@link NodeDescription}
     * @param includeListItem
     *            {@code true} if this method should include items from list {@link NodeDescription}
     * @param domains
     *            the domain classes to check
     * @return {@code true} if the provided {@code node} matches the given {@code domains}, {@code false} otherwise
     */
    protected boolean isValidNodeDescription(NodeDescription node, boolean includeCompartment, boolean includeListItem, EClass... domains) {
        final boolean result;
        if (node.getName().equals(AbstractRepresentationDescriptionBuilder.SHARED_DESCRIPTIONS)) {
            // Ignore SHARED_DESCRIPTIONS: we don't want to have it be the source/target of any edge.
            result = false;
        } else if (!includeCompartment && IdBuilder.isCompartmentNode(node)) {
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

}

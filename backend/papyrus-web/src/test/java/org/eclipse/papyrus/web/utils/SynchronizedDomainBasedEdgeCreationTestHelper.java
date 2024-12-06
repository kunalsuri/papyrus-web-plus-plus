/*******************************************************************************
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
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.sirius.contributions.ViewDiagramDescriptionService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;

/**
 * Test helper for domain base edge creation service.
 *
 * @author Arthur Daussy
 */
public final class SynchronizedDomainBasedEdgeCreationTestHelper {

    private IdBuilder idBuilder;

    private DiagramTestHelper representationHelper;

    private IObjectService objectService;

    private EObject source;

    private EObject target;

    private String sourceNodeDescriptionName;

    private String targetNodeDescriptionName;

    private String expectedDomainBasedEdgeDescriptionName;

    private String sourceNodeId;

    private String targetNodeId;

    private IEditingContext editingContext;

    private EClass type;

    private EReference expectedContainementRef;

    private EObject expectedOwner;

    private AbstractDiagramService diagramService;

    private EObject newElement;

    private SynchronizedDomainBasedEdgeCreationTestHelper(Builder builder) {
        this.idBuilder = builder.idBuilder;
        this.representationHelper = builder.representationHelper;
        this.objectService = builder.objectService;
        this.source = builder.source;
        this.target = builder.target;
        this.sourceNodeDescriptionName = builder.sourceNodeDescriptionName;
        this.targetNodeDescriptionName = builder.targetNodeDescriptionName;
        this.expectedDomainBasedEdgeDescriptionName = builder.expectedDomainBasedEdgeDescriptionName;
        this.sourceNodeId = builder.sourceNodeId;
        this.targetNodeId = builder.targetNodeId;
        this.editingContext = builder.editingContext;
        this.type = builder.type;
        this.expectedContainementRef = builder.expectedContainementRef;
        this.expectedOwner = builder.expectedOwner;
        this.diagramService = builder.diagramService;
    }

    private String getSourceNodeDescriptionName() {
        if (this.sourceNodeDescriptionName == null) {
            return this.idBuilder.getDomainNodeName(this.source.eClass());
        } else {
            return this.sourceNodeDescriptionName;
        }
    }

    private String getTargetNodeDescriptionName() {
        if (this.targetNodeDescriptionName == null) {
            return this.idBuilder.getDomainNodeName(this.target.eClass());
        } else {
            return this.targetNodeDescriptionName;
        }
    }

    private String getExpectedDomainBasedEdgeDescriptionName() {
        if (this.expectedDomainBasedEdgeDescriptionName == null) {
            return this.idBuilder.getDomainBaseEdgeId(this.type);
        } else {
            return this.expectedDomainBasedEdgeDescriptionName;
        }
    }

    public SynchronizedDomainBasedEdgeCreationTestHelper emulateCreationTool() {
        Node sourceNode = this.getSourceNode();
        Node targetNode = this.getTargetNode();

        Optional<Object> optSemanticSource = this.objectService.getObject(this.editingContext, sourceNode.getTargetObjectId());
        if (optSemanticSource.isEmpty()) {
            fail("Unable to find semantic target element of " + sourceNode);
        }

        EObject semanticSource = (EObject) optSemanticSource.get();

        Optional<Object> optSemanticTarget = this.objectService.getObject(this.editingContext, targetNode.getTargetObjectId());
        if (optSemanticTarget.isEmpty()) {
            fail("Unable to find semantic target element of " + targetNode);
        }

        EObject semanticTarget = (EObject) optSemanticTarget.get();

        this.newElement = this.representationHelper.modify(context -> {
            EObject aNewElement = this.diagramService//
                    .createDomainBasedEdge(semanticSource, semanticTarget, //
                            this.type.getName(), this.expectedContainementRef.getName(), //
                            sourceNode, targetNode, this.editingContext, context);

            return aNewElement;
        });

        return this;
    }

    private Node getSourceNode() {
        // Prevent calling creatIn in NodeDescription is not found first because is name is suffixed with Holder
        Node sourceNode = null;
        Optional<NodeDescription> optSourceNodeDescription;
        if (this.sourceNodeId == null) {
            optSourceNodeDescription = this.representationHelper.getOptionalNodeDescriptionByName(this.getSourceNodeDescriptionName());
            if (optSourceNodeDescription.isPresent()) {
                sourceNode = this.representationHelper.createNodeInDiagram(this.getSourceNodeDescriptionName(), this.source);

            } else {
                sourceNode = this.representationHelper.createNodeInDiagram(this.getSourceNodeDescriptionName() + "_Holder", this.source);
            }
        } else {
            sourceNode = this.representationHelper.assertUniqueNodeById(this.sourceNodeId);
        }
        this.sourceNodeId = sourceNode.getId();
        return sourceNode;
    }

    private Node getTargetNode() {
        Node targetNode = null;
        Optional<NodeDescription> optTargetNodeDescription;
        // Prevent calling creatIn in NodeDescription is not found first because is name is suffixed with Holder
        if (this.targetNodeId == null) {
            optTargetNodeDescription = this.representationHelper.getOptionalNodeDescriptionByName(this.getTargetNodeDescriptionName());
            if (optTargetNodeDescription.isPresent()) {
                targetNode = this.representationHelper.createNodeInDiagram(this.getTargetNodeDescriptionName(), this.target);
            } else {
                targetNode = this.representationHelper.createNodeInDiagram(this.getTargetNodeDescriptionName() + "_Holder", this.target);
            }
        } else {
            targetNode = this.representationHelper.assertUniqueNodeById(this.targetNodeId);
        }
        this.targetNodeId = targetNode.getId();
        return targetNode;
    }

    public Edge assertEdgeCreation() {

        // Check semantic
        assertTrue(this.type.isInstance(this.newElement));
        assertEquals(this.expectedOwner, this.newElement.eContainer());
        if (this.expectedContainementRef.isMany()) {
            assertTrue(((Collection<?>) this.expectedOwner.eGet(this.expectedContainementRef)).contains(this.newElement));
        } else {
            assertEquals(this.expectedOwner.eGet(this.expectedContainementRef), this.newElement);
        }
        Node sourceNode = this.representationHelper.assertUniqueNodeById(this.sourceNodeId);
        Node targetNode = this.representationHelper.assertUniqueNodeById(this.targetNodeId);
        Edge edge = this.representationHelper.assertGetExistDomainBasedEdge(this.getExpectedDomainBasedEdgeDescriptionName(), this.newElement, sourceNode, targetNode);

        // Check there is only one representation for this semantic element
        List<Edge> matchingEdges = this.representationHelper.getAllMatchingEdges(Optional.of(edge.getTargetObjectId()), Optional.empty(), Optional.empty(), Optional.empty());
        assertEquals(1, matchingEdges.size());

        return edge;
    }

    public static Builder builder() {
        return new Builder();
    }

    // CHECKSTYLE:OFF Builder pattern
    public static final class Builder {

        private ViewDiagramDescriptionService viewDiagramDescriptionService;

        private IdBuilder idBuilder;

        private DiagramTestHelper representationHelper;

        private IObjectService objectService;

        private EObject source;

        private EObject target;

        private String sourceNodeDescriptionName;

        private String targetNodeDescriptionName;

        private String expectedDomainBasedEdgeDescriptionName;

        private String sourceNodeId;

        private String targetNodeId;

        private IEditingContext editingContext;

        private EClass type;

        private EReference expectedContainementRef;

        private EObject expectedOwner;

        private AbstractDiagramService diagramService;

        private Builder() {
        }

        public Builder withIdBuilder(IdBuilder idBuilder) {
            this.idBuilder = idBuilder;
            return this;
        }

        public Builder withRepresentationHelper(DiagramTestHelper representationHelper) {
            this.representationHelper = representationHelper;
            return this;
        }

        public Builder withObjectService(IObjectService objectService) {
            this.objectService = objectService;
            return this;
        }

        public Builder withExpectedDomainBasedEdgeDescriptionName(String expectedDomainBasedEdgeDescriptionName) {
            this.expectedDomainBasedEdgeDescriptionName = expectedDomainBasedEdgeDescriptionName;
            return this;
        }

        public Builder withSource(EObject source) {
            this.source = source;
            return this;
        }

        public Builder withSourceNodeDescriptionName(String sourceNodeDescriptionName) {
            this.sourceNodeDescriptionName = sourceNodeDescriptionName;
            return this;
        }

        public Builder withSourceNodeId(String sourceNodeId) {
            this.sourceNodeId = sourceNodeId;
            return this;
        }

        public Builder withTarget(EObject target) {
            this.target = target;
            return this;
        }

        public Builder withTargetNodeDescriptionName(String targetNodeDescriptionName) {
            this.targetNodeDescriptionName = targetNodeDescriptionName;
            return this;
        }

        public Builder withTargetNodeId(String targetNodeId) {
            this.targetNodeId = targetNodeId;
            return this;
        }

        public Builder withEditingContext(IEditingContext editingContext) {
            this.editingContext = editingContext;
            return this;
        }

        public Builder withType(EClass type) {
            this.type = type;
            return this;
        }

        public Builder withExpectedContainementRef(EReference containementRef) {
            this.expectedContainementRef = containementRef;
            return this;
        }

        public Builder withExpectedOwner(EObject expectedOwner) {
            this.expectedOwner = expectedOwner;
            return this;
        }

        public Builder withDiagramService(AbstractDiagramService diagramService) {
            this.diagramService = diagramService;
            return this;
        }

        public SynchronizedDomainBasedEdgeCreationTestHelper build() {
            Objects.requireNonNull(this.source);
            Objects.requireNonNull(this.target);
            Objects.requireNonNull(this.representationHelper);
            Objects.requireNonNull(this.objectService);
            Objects.requireNonNull(this.editingContext);
            Objects.requireNonNull(this.expectedContainementRef);
            Objects.requireNonNull(this.expectedOwner);
            Objects.requireNonNull(this.diagramService);
            return new SynchronizedDomainBasedEdgeCreationTestHelper(this);
        }
    }
    // CHECKSTYLE:ON Builder pattern
}

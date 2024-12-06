/*******************************************************************************
 * Copyright (c) 2022, 2024 CEA LIST, Obeo, Artal Technolgies.
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.diagrams.Edge;

/**
 * Test used to check that a synchronized domain based edge is displayed on a diagram if the semantic condition are
 * matched.
 *
 * @author Arthur Daussy
 */
public final class SynchronizedDomainBasedEdgeTestHelper {

    private IdBuilder idBuilder;

    private DiagramTestHelper representationHelper;

    private IObjectService objectService;

    private EObject source;

    private EObject target;

    private EObject domainBasedEdge;

    private String sourceNodeDescriptionName;

    private String targetNodeDescriptionName;

    private String domainBasedEdgeDescriptionName;

    private String sourceNodeId;

    private String targetNodeId;

    private SynchronizedDomainBasedEdgeTestHelper(Builder builder) {
        this.idBuilder = builder.idBuilder;
        this.representationHelper = builder.representationHelper;
        this.objectService = builder.objectService;
        this.source = builder.source;
        this.target = builder.target;
        this.domainBasedEdge = builder.domainBasedEdge;
        this.sourceNodeDescriptionName = builder.sourceNodeDescriptionName;
        this.targetNodeDescriptionName = builder.targetNodeDescriptionName;
        this.domainBasedEdgeDescriptionName = builder.domainBasedEdgeDescriptionName;
        this.sourceNodeId = builder.sourceNodeId;
        this.targetNodeId = builder.targetNodeId;
    }

    private String getSourceNodeDescriptionName() {
        if (this.sourceNodeDescriptionName == null) {
            String sourceName = this.idBuilder.getDomainNodeName(this.source.eClass());
            if (this.representationHelper.getOptionalNodeDescriptionByName(sourceName).isEmpty()) {
                sourceName = this.idBuilder.getSpecializedDomainNodeName(this.source.eClass(), "Holder");
            }
            return sourceName;
        }
        return this.sourceNodeDescriptionName;
    }

    private String getTargetNodeDescriptionName() {
        if (this.targetNodeDescriptionName == null) {
            String targetName = this.idBuilder.getDomainNodeName(this.target.eClass());
            if (this.representationHelper.getOptionalNodeDescriptionByName(targetName).isEmpty()) {
                targetName = this.idBuilder.getSpecializedDomainNodeName(this.target.eClass(), "Holder");
            }
            return targetName;
        }
        return this.targetNodeDescriptionName;
    }

    private String getDomainBasedEdgeDescriptionName() {
        if (this.domainBasedEdgeDescriptionName == null) {
            return this.idBuilder.getDomainBaseEdgeId(this.domainBasedEdge.eClass());
        } else {
            return this.domainBasedEdgeDescriptionName;
        }
    }

    /**
     * Update the diagram.
     *
     * @return the synchronized domain based edge test helper
     */
    public SynchronizedDomainBasedEdgeTestHelper updateDiagram() {
        if (this.sourceNodeId == null) {
            this.sourceNodeId = this.representationHelper.createNodeInDiagram(this.getSourceNodeDescriptionName(), this.source).getId();
        }
        if (this.targetNodeId == null) {
            this.targetNodeId = this.representationHelper.createNodeInDiagram(this.getTargetNodeDescriptionName(), this.target).getId();
        }

        this.representationHelper.refresh();

        return this;
    }

    /**
     * Assert that the edge is displayed on diagram.
     *
     * @return the edge
     */
    public Edge assertDisplayedOnDiagram() {
        Objects.requireNonNull(this.sourceNodeId, "No source node found. Did you update the diagram before assert");
        Objects.requireNonNull(this.targetNodeId, "No target node found. Did you update the diagram before assert");

        Edge edge = this.representationHelper.getMatchingEdge(Optional.of(this.getDomainBasedEdgeDescriptionName()), //
                Optional.of(this.objectService.getId(this.domainBasedEdge)), Optional.of(this.sourceNodeId), Optional.of(this.targetNodeId));

        assertNotNull(edge);

        return edge;
    }

    /**
     * Builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    // CHECKSTYLE:OFF Builder pattern
    public static final class Builder {
        private IdBuilder idBuilder;

        private DiagramTestHelper representationHelper;

        private IObjectService objectService;

        private EObject source;

        private EObject target;

        private EObject domainBasedEdge;

        private String sourceNodeDescriptionName;

        private String targetNodeDescriptionName;

        private String domainBasedEdgeDescriptionName;

        private String targetNodeId;

        private String sourceNodeId;

        private Builder() {
        }

        /**
         * Builder with id builder.
         *
         * @param idBuilder
         *            the id builder
         * @return the builder
         */
        public Builder withIdBuilder(IdBuilder idBuilder) {
            this.idBuilder = idBuilder;
            return this;
        }

        /**
         * Builder with representation helper.
         *
         * @param representationHelper
         *            the representation helper
         * @return the builder
         */
        public Builder withRepresentationHelper(DiagramTestHelper representationHelper) {
            this.representationHelper = representationHelper;
            return this;
        }

        /**
         * Builder with object service.
         *
         * @param objectService
         *            the object service
         * @return the builder
         */
        public Builder withObjectService(IObjectService objectService) {
            this.objectService = objectService;
            return this;
        }

        /**
         * Semantic source
         */
        public Builder withSource(EObject source) {
            this.source = source;
            return this;
        }

        /**
         * Source node id(optional can be created on the fly)
         */
        public Builder withSourceNodeId(String sourceNodeId) {
            this.sourceNodeId = sourceNodeId;
            return this;
        }

        /**
         * Description name of source node if using the creation of the fly. For on the fly creation the description is
         * optional if name can be computed using {@link IdBuilder#getDomainNodeName(org.eclipse.emf.ecore.EClass)}
         */
        public Builder withSourceNodeDescriptionName(String sourceNodeDescriptionName) {
            this.sourceNodeDescriptionName = sourceNodeDescriptionName;
            return this;
        }

        /**
         * Semantic target
         */
        public Builder withTarget(EObject target) {
            this.target = target;
            return this;
        }

        /**
         * Target node id(optional can be created on the fly)
         */
        public Builder withTargetNodeId(String targetNodeId) {
            this.targetNodeId = targetNodeId;
            return this;
        }

        /**
         * Description name of target node if using the creation of the fly. For on the flyw creation the description is
         * optional if name can be computed using {@link IdBuilder#getDomainNodeName(org.eclipse.emf.ecore.EClass)}
         */
        public Builder withTargetNodeDescriptionName(String targetNodeDescriptionName) {
            this.targetNodeDescriptionName = targetNodeDescriptionName;
            return this;
        }

        /**
         * Builder for domain based edge.
         *
         * @param domainBasedEdge
         *            the domain based edge
         * @return the builder
         */
        public Builder withDomainBasedEdge(EObject domainBasedEdge) {
            this.domainBasedEdge = domainBasedEdge;
            return this;
        }

        /**
         * Builder for domain based edge description name.
         *
         * @param domainBasedEdgeDescriptionName
         *            the domain based edge description name
         * @return the builder
         */
        public Builder withDomainBasedEdgeDescriptionName(String domainBasedEdgeDescriptionName) {
            this.domainBasedEdgeDescriptionName = domainBasedEdgeDescriptionName;
            return this;
        }

        /**
         * Builds the test helper.
         *
         * @return the synchronized domain based edge test helper
         */
        public SynchronizedDomainBasedEdgeTestHelper build() {
            Objects.requireNonNull(this.domainBasedEdge);
            Objects.requireNonNull(this.source);
            Objects.requireNonNull(this.target);
            Objects.requireNonNull(this.representationHelper);
            Objects.requireNonNull(this.objectService);
            return new SynchronizedDomainBasedEdgeTestHelper(this);
        }
    }
    // CHECKSTYLE:ON Builder pattern

}

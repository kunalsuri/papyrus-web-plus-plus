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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.sirius.components.diagrams.Edge;

/**
 * Test used to check that a synchronized feature based edge is displayed on a diagram if the semantic condition are
 * matched.
 *
 * @author Arthur Daussy
 */
public final class SynchronizedFeatureEdgeTestHelper {

    private EObject source;

    private EObject target;

    private String edgeDescriptionId;

    private String sourceNodeDescriptionName;

    private String targetNodeDescriptionName;

    private IdBuilder idBuilder;

    private DiagramTestHelper representationHelper;

    private String sourceNodeId;

    private String targetNodeId;

    private SynchronizedFeatureEdgeTestHelper(Builder builder) {
        this.source = builder.source;
        this.target = builder.target;
        this.edgeDescriptionId = builder.edgeDescriptionId;
        this.sourceNodeDescriptionName = builder.sourceNodeDescriptionName;
        this.targetNodeDescriptionName = builder.targetNodeDescriptionName;
        this.idBuilder = builder.idBuilder;
        this.representationHelper = builder.representationHelper;
    }

    public static Builder builder() {
        return new Builder();
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

    /**
     * Update the diagram by creating source and target.
     *
     * @return the synchronized feature edge test helper
     */
    public SynchronizedFeatureEdgeTestHelper updateDiagram() {
        if (this.representationHelper.getOptionalNodeDescriptionByName(this.getSourceNodeDescriptionName()).isPresent()) {
            this.sourceNodeId = this.representationHelper.createNodeInDiagram(this.getSourceNodeDescriptionName(), this.source).getId();
        } else {
            this.sourceNodeId = this.representationHelper.createNodeInDiagram(this.getSourceNodeDescriptionName() + "_Holder", this.source).getId();
        }
        if (this.representationHelper.getOptionalNodeDescriptionByName(this.getTargetNodeDescriptionName()).isPresent()) {
            this.targetNodeId = this.representationHelper.createNodeInDiagram(this.getTargetNodeDescriptionName(), this.target).getId();
        } else {
            this.targetNodeId = this.representationHelper.createNodeInDiagram(this.getTargetNodeDescriptionName() + "_Holder", this.target).getId();
        }
        this.representationHelper.refresh();

        return this;
    }

    /**
     * Assert edge displayed on diagram.
     */
    public void assertEdgeDisplayedOnDiagram() {

        Objects.requireNonNull(this.sourceNodeId, "No source node found. Did you update the diagram before assert");
        Objects.requireNonNull(this.targetNodeId, "No target node found. Did you update the diagram before assert");

        Edge edge = this.representationHelper.getMatchingEdge(Optional.of(this.edgeDescriptionId), //
                Optional.empty(), Optional.of(this.sourceNodeId), Optional.of(this.targetNodeId));

        assertNotNull(edge);

    }

    // CHECKSTYLE:OFF Builder pattern
    public static final class Builder {
        private EObject source;

        private EObject target;

        private String edgeDescriptionId;

        private String sourceNodeDescriptionName;

        private String targetNodeDescriptionName;

        private IdBuilder idBuilder;

        private DiagramTestHelper representationHelper;

        private Builder() {
        }

        public Builder withSource(EObject source) {
            this.source = source;
            return this;
        }

        public Builder withTarget(EObject target) {
            this.target = target;
            return this;
        }

        public Builder withEdgeDescriptionId(String edgeDescriptionId) {
            this.edgeDescriptionId = edgeDescriptionId;
            return this;
        }

        public Builder withSourceNodeDescriptionName(String sourceNodeDescriptionName) {
            this.sourceNodeDescriptionName = sourceNodeDescriptionName;
            return this;
        }

        public Builder withTargetNodeDescriptionName(String targetNodeDescriptionName) {
            this.targetNodeDescriptionName = targetNodeDescriptionName;
            return this;
        }

        public Builder withIdBuilder(IdBuilder idBuilder) {
            this.idBuilder = idBuilder;
            return this;
        }

        public Builder withRepresentationHelper(DiagramTestHelper representationHelper) {
            this.representationHelper = representationHelper;
            return this;
        }

        public SynchronizedFeatureEdgeTestHelper build() {
            Objects.requireNonNull(this.source);
            Objects.requireNonNull(this.target);
            Objects.requireNonNull(this.representationHelper);
            Objects.requireNonNull(this.edgeDescriptionId);
            return new SynchronizedFeatureEdgeTestHelper(this);
        }
    }
    // CHECKSTYLE:ON Builder pattern

}

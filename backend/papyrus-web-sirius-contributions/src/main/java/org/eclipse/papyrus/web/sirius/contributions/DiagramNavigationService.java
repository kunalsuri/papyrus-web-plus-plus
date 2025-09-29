/*******************************************************************************
 * Copyright (c) 2022, 2023 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.web.sirius.contributions;

import static java.util.stream.Collectors.toList;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.papyrus.web.sirius.contributions.query.NodeMatcher;
import org.eclipse.papyrus.web.sirius.contributions.query.NodeMatcher.BorderNodeStatus;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link IDiagramNavigationService}.
 *
 * @author pcdavid
 */
@Service
public class DiagramNavigationService implements IDiagramNavigationService {

    private final IEMFNavigationService emfNavigationService;

    private final IObjectSearchService objectSearchService;

    public DiagramNavigationService(IEMFNavigationService emfNavigationService,
            IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.emfNavigationService = Objects.requireNonNull(emfNavigationService);
    }

    @Override
    public Optional<Object> getParent(Diagram diagram, Node node) {
        return Optional.ofNullable(new ParentNodeQuery(diagram).getParent(node));
    }

    @Override
    public List<Node> getAncestorNodes(Diagram diagram, Node node) {
        ParentNodeQuery querier = new ParentNodeQuery(diagram);
        querier.setUseCache(true);

        List<Node> result = new ArrayList<>();
        Node current = node;
        while (current != null) {
            if (current != node) {
                result.add(current);
            }

            Object parent = querier.getParent(current);
            if (parent instanceof Node) {
                current = (Node) parent;
            } else {
                current = null;
            }
        }
        return result;
    }

    @Override
    public List<Node> getMatchingNodes(Diagram diagram, IEditingContext editingContext, NodeMatcher matcher) {
        List<Node> result = new ArrayList<>();
        this.getMatchingNode(this.getChildren(diagram, matcher.getBorderedNodeStatus()), editingContext, matcher, result);
        return result;
    }

    @Override
    public List<Node> getMatchingNodesIn(Node parentNode, Diagram diagram, IEditingContext editingContext, NodeMatcher matcher) {
        List<Node> result = new ArrayList<>();
        this.getMatchingNode(this.getChildren(parentNode, matcher.getBorderedNodeStatus()), editingContext, matcher, result);
        return result;
    }

    public void getMatchingNode(List<Node> candidates, IEditingContext editingContext, NodeMatcher matcher, List<Node> collector) {
        for (var node : candidates) {
            if (matcher.match(node, this.buildSemanticProvider(editingContext, node))) {
                collector.add(node);
            }
            this.getMatchingNode(this.getChildren(node, matcher.getBorderedNodeStatus()), editingContext, matcher, collector);
        }

    }

    private Supplier<Object> buildSemanticProvider(IEditingContext editingContext, Node node) {
        return () -> this.objectSearchService.getObject(editingContext, node.getTargetObjectId()).orElse(null);
    }

    private List<Node> getChildren(Object parent, BorderNodeStatus borderNodeStatus) {
        final List<Node> result;
        if (parent instanceof Diagram) {
            result = ((Diagram) parent).getNodes();
        } else if (parent instanceof Node) {
            Node parentNode = (Node) parent;
            switch (borderNodeStatus) {
                case BOTH:
                    result = Stream.concat(parentNode.getChildNodes().stream(), parentNode.getBorderNodes().stream()).collect(toList());
                    break;
                case BORDERED_NODE:
                    result = parentNode.getBorderNodes();
                    break;
                case BASIC_NODE:
                    result = parentNode.getChildNodes();
                    break;
                default:
                    throw new IllegalStateException(MessageFormat.format("Unknown enum {0}", borderNodeStatus));
            }
        } else {
            result = List.of();
        }
        return result;
    }
}

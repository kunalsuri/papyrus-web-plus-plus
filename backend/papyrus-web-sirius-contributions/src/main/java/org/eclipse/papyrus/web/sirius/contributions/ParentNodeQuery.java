/*******************************************************************************
 * Copyright (c) 2022, 2025 CEA LIST, Obeo.
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
 *******************************************************************************/
package org.eclipse.papyrus.web.sirius.contributions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Query used to retrieve the parent of a Node in a {@link Diagram}.
 *
 * @author Arthur Daussy
 */
public class ParentNodeQuery {

    private final Diagram diagram;

    private final Map<String, Object> childToParentCache = new HashMap<>();

    private boolean useCache;

    public ParentNodeQuery(Diagram diagram) {
        super();
        this.diagram = diagram;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * Gets the parent of the given {@link Node}. It can either be another {@link Node} or a {@link Diagram}
     *
     * @param searchNode
     *            a non <code>null</code> node to search
     * @return a parent {@link Node}, the {@link Diagram} or <code>null</code> if the given node is not in the current
     *         diagram
     */
    public Object getParent(Node searchNode) {
        return this.getParent(searchNode.getId(), searchNode.isBorderNode());
    }

    /**
     * Gets the parent of the given {@link Node}. It can either be another {@link Node} or a {@link Diagram}
     *
     * @param searchedNodeId
     *            the node id to search
     * @param isBorderNode
     *            holds <code>true</code> if the node is a border node
     * @return a parent {@link Node}, the {@link Diagram} or <code>null</code> if the given node is not in the current
     *         diagram
     */
    public Object getParent(String searchedNodeId, boolean isBorderNode) {
        Object cacheResult = this.getCacheResult(searchedNodeId);
        if (cacheResult != null) {
            return cacheResult;
        } else {
            return this.doSearch(searchedNodeId, isBorderNode);
        }

    }

    private Object doSearch(String searchedNodeId, boolean isBorderNode) {
        for (Node node : this.diagram.getNodes()) {

            if (this.useCache) {
                this.childToParentCache.put(node.getId(), this.diagram);
            }

            final Object result;
            if (Objects.equals(node.getId(), searchedNodeId)) {
                result = this.diagram;
            } else {
                result = this.searchIn(node, searchedNodeId, isBorderNode);
            }

            if (result != null) {
                return result;
            }

        }
        return null;
    }

    private Object getCacheResult(String searchedNodeId) {
        if (this.useCache) {
            return this.childToParentCache.get(searchedNodeId);
        }
        return null;
    }

    private Node searchIn(Node current, String searchedNodeId, boolean isBorderNode) {
        if (this.getAllChildrenCandidates(current, isBorderNode).anyMatch(n -> searchedNodeId.equals(n.getId()))) {
            return current;
        } else {
            Node result = null;
            for (Node child : current.getChildNodes()) {
                this.childToParentCache.put(child.getId(), current);
                result = this.searchIn(child, searchedNodeId, isBorderNode);
                if (result != null) {
                    break;
                }
            }
            return result;
        }
    }

    private Stream<Node> getAllChildrenCandidates(Node node, boolean isBorderNode) {
        if (isBorderNode) {
            return node.getBorderNodes().stream();
        } else {
            return node.getChildNodes().stream();
        }
    }
}

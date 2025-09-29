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
package org.eclipse.papyrus.web.application.representations.aqlservices.utils;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.uml.domain.services.IViewQuerier;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Implementation of a {@link IViewQuerier} for the Web platform.
 *
 * @author Arthur Daussy
 */
public class WebRepresentationQuerier implements IViewQuerier {

    private final Diagram diagram;


    private final IEditingContext editingContext;

    private final IDiagramNavigationService diagramNavigationService;

    private final IObjectSearchService objectSearchService;

    public WebRepresentationQuerier(Diagram diagram, IObjectSearchService objectSearchService, IDiagramNavigationService diagramNavigationService, IEditingContext editingContext) {
        super();
        this.diagram = diagram;
        this.objectSearchService = objectSearchService;
        this.editingContext = editingContext;
        this.diagramNavigationService = diagramNavigationService;
    }

    @Override
    public Object getVisualParent(Object element) {

        final Object parent;
        if (element instanceof Node) {
            Node node = (Node) element;
            parent = this.diagramNavigationService.getParent(this.diagram, node).orElse(null);
        } else {
            parent = null;
        }
        return parent;
    }

    @Override
    public EObject getSemanticElement(Object view) {
        final String semanticElementID;
        if (view instanceof Node) {
            Node node = (Node) view;
            semanticElementID = node.getTargetObjectId();

        } else if (view instanceof Diagram) {
            semanticElementID = ((Diagram) view).getTargetObjectId();
        } else {
            semanticElementID = null;
        }
        if (semanticElementID != null) {
            return this.objectSearchService.getObject(this.editingContext, semanticElementID) //
                    .filter(e -> e instanceof EObject) //
                    .map(e -> (EObject) e) //
                    .orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public List<? extends Object> getVisualAncestorNodes(Object view) {

        if (view instanceof Node) {
            Node node = (Node) view;
            return this.diagramNavigationService.getAncestorNodes(this.diagram, node);
        }
        return List.of();
    }

    @Override
    public List<? extends Object> getChildrenNodes(Object view) {
        if (view instanceof Node) {
            Node node = (Node) view;
            return node.getChildNodes();
        }
        return Collections.emptyList();
    }

    @Override
    public List<? extends Object> getBorderedNodes(Object view) {
        if (view instanceof Node) {
            Node node = (Node) view;
            return node.getBorderNodes();
        }
        return Collections.emptyList();
    }

    @Override
    public Object getDiagram() {
        return this.diagram;
    }

}

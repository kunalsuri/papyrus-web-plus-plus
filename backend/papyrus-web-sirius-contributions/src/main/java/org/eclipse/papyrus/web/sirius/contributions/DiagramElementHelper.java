/*******************************************************************************
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
 *******************************************************************************/
package org.eclipse.papyrus.web.sirius.contributions;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.elements.DiagramElementProps;
import org.eclipse.sirius.components.diagrams.elements.EdgeElementProps;
import org.eclipse.sirius.components.diagrams.elements.NodeElementProps;
import org.eclipse.sirius.components.diagrams.renderer.DiagramRenderingCache;
import org.eclipse.sirius.components.representations.Element;
import org.eclipse.sirius.components.representations.IProps;

/**
 * Helper class to manipulate and navigate between {@link Element}s displayed in a Diagram.
 *
 * @author Arthur Daussy
 */
public class DiagramElementHelper {

    private final Element element;

    public DiagramElementHelper(Element element) {
        super();
        this.element = element;
    }

    public Optional<String> getId() {
        Optional<String> id;
        IProps props = this.element.getProps();
        if (props instanceof NodeElementProps nodeProps) {
            id = Optional.ofNullable(nodeProps.getId());
        } else if (props instanceof EdgeElementProps edgeProps) {
            id = Optional.ofNullable(edgeProps.getId());
        } else if (props instanceof DiagramElementProps diagProps) {
            id = Optional.ofNullable(diagProps.getId());
        } else {
            id = Optional.empty();
        }

        return id;
    }

    public Optional<String> getTargetId() {
        Optional<String> id;
        IProps props = this.element.getProps();
        if (props instanceof NodeElementProps nodeProps) {
            id = Optional.ofNullable(nodeProps.getTargetObjectId());
        } else if (props instanceof EdgeElementProps edgeProps) {
            id = Optional.ofNullable(edgeProps.getTargetObjectId());
        } else if (props instanceof DiagramElementProps diagProps) {
            id = Optional.ofNullable(diagProps.getTargetObjectId());
        } else {
            id = Optional.empty();
        }

        return id;
    }

    public Element getElement() {
        return this.element;
    }

    public List<DiagramElementHelper> getAncestors(DiagramRenderingCache cache) {
        return this.getId().map(cache::getAncestors).map(ancestors -> ancestors.stream().map(DiagramElementHelper::new).toList()).orElse(Collections.emptyList());
    }

    public Optional<Object> getElementTarget(IObjectSearchService objectSearchService, IEditingContext context) {
        return this.getTargetId().flatMap(id -> objectSearchService.getObject(context, id));
    }

    public Optional<DiagramElementHelper> getParent(DiagramRenderingCache cache) {
        return this.getId().flatMap(id -> cache.getParent(id)).map(DiagramElementHelper::new);
    }

    public Optional<DiagramElementHelper> getCommonAncestor(DiagramElementHelper other, DiagramRenderingCache cache) {
        Set<String> sourceAncestors = this.getAncestors(cache).stream()//
                .map(DiagramElementHelper::getId)//
                .filter(Optional::isPresent)//
                .map(Optional::get)//
                .collect(toSet());
        List<DiagramElementHelper> targetAncestors = other.getAncestors(cache);

        Iterator<DiagramElementHelper> targetIte = targetAncestors.iterator();
        while (targetIte.hasNext()) {
            DiagramElementHelper targetAncestor = targetIte.next();
            if (targetAncestor.getId().map(sourceAncestors::contains).orElse(false)) {
                return Optional.of(targetAncestor);
            }
        }

        return Optional.empty();
    }

}

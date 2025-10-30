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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramCreationService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

/**
 * Service used to build diagram programmatically.
 *
 * @author Arthur Daussy
 */
@Service
public class DiagramBuilderService implements IDiagramBuilderService {

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IDiagramCreationService diagramCreationService;

    public DiagramBuilderService(IRepresentationDescriptionSearchService representationDescriptionSearchService, IDiagramCreationService diagramCreationService) {
        super();
        this.representationDescriptionSearchService = representationDescriptionSearchService;
        this.diagramCreationService = diagramCreationService;
    }

    @Override
    public Optional<Diagram> createDiagram(IEditingContext editingContext, Predicate<DiagramDescription> diagramDescriptionMatcher, Object semanticTarget, String diagramName) {
        var optionalDiagramDescription = this.findDiagramDescription(editingContext, diagramDescriptionMatcher);
        if (optionalDiagramDescription.isPresent()) {
            DiagramDescription diagramDescription = optionalDiagramDescription.get();
            return Optional.of(this.diagramCreationService.create(semanticTarget, diagramDescription, editingContext));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Diagram> updateDiagram(Diagram diagram, IEditingContext editingContext, Consumer<DiagramContext> updater) {
        DiagramContext diagramContext = new DiagramContext(diagram);
        if (updater != null) {
            updater.accept(diagramContext);
        }
        return this.diagramCreationService.refresh(editingContext, diagramContext);
    }

    @Override
    public <T> Optional<Pair<Diagram, T>> updateDiagramAndGet(Diagram initialDiagramState, IEditingContext editingContext, Function<DiagramContext, T> updater) {
        DiagramContext diagramContext = new DiagramContext(initialDiagramState);
        final T result;
        if (updater != null) {
            result = updater.apply(diagramContext);
        } else {
            result = null;
        }
        return this.diagramCreationService.refresh(editingContext, diagramContext)//
                .map(d -> Pair.of(d, result));
    }

    private Optional<DiagramDescription> findDiagramDescription(IEditingContext editingContext, Predicate<DiagramDescription> diagramDescriptionMatcher) {
        return this.representationDescriptionSearchService.findAll(editingContext).values().stream()
                .filter(DiagramDescription.class::isInstance)
                .map(DiagramDescription.class::cast)
                .filter(diagramDescriptionMatcher)
                .findFirst();
    }

    @Override
    public Optional<Diagram> refreshDiagram(Diagram diagram, IEditingContext editingContext) {
        return this.updateDiagram(diagram, editingContext, null);
    }

}

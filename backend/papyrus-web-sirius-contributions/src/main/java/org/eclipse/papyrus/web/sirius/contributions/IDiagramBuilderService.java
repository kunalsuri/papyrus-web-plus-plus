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
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.springframework.data.util.Pair;

/**
 * Service used to create diagram programmatically.
 *
 * @author Arthur Daussy
 */
public interface IDiagramBuilderService {

    /**
     * Creates a diagram.
     *
     * @param editingContext
     *            the editing context
     * @param diagramDescriptionMatcher
     *            a predicate used to select the {@link DiagramDescription} used for creation among all registered
     *            description
     * @param semanticTarget
     *            the semantic target for the diagram
     * @param diagramInitialName
     *            the initial name of the diagram
     * @return an diagram if the creation terminated successfully, {@link Optional#empty()} otherwise
     */
    Optional<Diagram> createDiagram(IEditingContext editingContext, Predicate<DiagramDescription> diagramDescriptionMatcher, Object semanticTarget, String diagramInitialName);

    /**
     * Update the give diagram using a updater. <b> /!\ Be aware that the given diagram is not modified, the returned
     * value contained the updated diagram /!\</b>
     *
     * @param initialDiagramState
     *            the initial state of the diagram
     * @param editingContext
     *            the {@link IEditingContext}
     * @param updater
     *            the optional updater of the diagram
     * @return the new state of the diagram if the update terminated successfully, {@link Optional#empty()} otherwise
     */
    Optional<Diagram> updateDiagram(Diagram initialDiagramState, IEditingContext editingContext,
            Consumer<DiagramContext> updater);

    /**
     * Update the give diagram using a updater and return a value provided by a function. <b> /!\ Be aware that the
     * given diagram is not modified, the returned value contained the updated diagram /!\</b>
     *
     * @param initialDiagramState
     *            the initial state of the diagram
     * @param editingContext
     *            the {@link IEditingContext}
     * @param updater
     *            the optional updater of the diagram that returs a value
     * @return the new state of the diagram if the update terminated successfully, {@link Optional#empty()} otherwise
     */
    <T> Optional<Pair<Diagram, T>> updateDiagramAndGet(Diagram initialDiagramState, IEditingContext editingContext,
            Function<DiagramContext, T> updater);

    /**
     * Refresh the given diagram. <b> /!\ Be aware that the given diagram is not modified, the returned value contained
     * the updated diagram /!\</b>
     *
     * @param diagramInitialState
     *            the diagram initial state
     * @param editingContext
     *            the {@link IEditingContext}
     * @return the new state of the diagram if the update terminated successfully, {@link Optional#empty()} otherwise
     */
    Optional<Diagram> refreshDiagram(Diagram diagramInitialState, IEditingContext editingContext);
}

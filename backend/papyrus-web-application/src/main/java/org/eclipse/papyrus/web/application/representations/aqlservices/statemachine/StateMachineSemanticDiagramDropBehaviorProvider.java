/*****************************************************************************
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
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.aqlservices.statemachine;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.drop.diagrams.StateMachineExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.uml.domain.services.drop.diagrams.StateMachineExternalSourceToRepresentationDropChecker;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.web.application.representations.IWebExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.SemanticDropSwitch;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Provides the behavior on a semantic drop event in the "State Machine" Diagram.
 *
 * This class is copied and adapted from
 * {@link org.eclipse.papyrus.web.application.representations.aqlservices.profile.ProfileSemanticDropBehaviorProvider}.
 *
 *
 * @author Laurent Fasani
 */
public class StateMachineSemanticDiagramDropBehaviorProvider implements IWebExternalSourceToRepresentationDropBehaviorProvider {

    private final IViewHelper viewHelper;

    private final IObjectSearchService objectSearchService;

    private final DiagramNavigator diagramNavigator;

    private final IEditingContext editionContext;

    private final ECrossReferenceAdapter crossRef;

    private final IEditableChecker editableChecker;

    /**
     * Logger used to report errors and warnings to the user.
     */
    private ILogger logger;

    /**
     * Constructor.
     *
     * @param editionContext
     *            editing context used to retrieve semantic target
     * @param viewHelper
     *            the helper used to create element on a diagram
     * @param objectSearchService
     *            service used to retrieve semantic target according to node id
     * @param diagramNavigator
     *            the helper used to navigate inside a diagram and/or to its description
     * @param logger
     *            Logger used to report errors and warnings to the user
     */
    public StateMachineSemanticDiagramDropBehaviorProvider(IEditingContext editionContext, IViewHelper viewHelper,
            IObjectSearchService objectSearchService, ECrossReferenceAdapter crossRef,
            IEditableChecker editableChecker,
            DiagramNavigator diagramNavigator, ILogger logger) {
        this.diagramNavigator = Objects.requireNonNull(diagramNavigator);
        this.crossRef = Objects.requireNonNull(crossRef);
        this.editableChecker = Objects.requireNonNull(editableChecker);
        this.editionContext = Objects.requireNonNull(editionContext);
        this.viewHelper = Objects.requireNonNull(viewHelper);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.logger = logger;
    }

    /**
     * Handles a semantic drop event.
     *
     * @param droppedElement
     *            the dropped element
     * @param targetNode
     *            the target node or <code>null</code> if the drop occurred on the diagram
     */
    @Override
    public void handleSemanticDrop(EObject droppedElement, org.eclipse.sirius.components.diagrams.Node targetNode) {
        Optional<Node> optionalTargetNode = Optional.ofNullable(targetNode);
        new SemanticDropSwitch(optionalTargetNode, this.viewHelper, this.diagramNavigator, this.logger) //
                .withDropChecker(new StateMachineExternalSourceToRepresentationDropChecker()) //
                .withDropProvider(new StateMachineExternalSourceToRepresentationDropBehaviorProvider()) //
                .withCrossRef(this.crossRef) //
                .withEditableChecker(this.editableChecker) //
                .withEObjectResolver(this::getSemanticObject) //
                .doSwitch(droppedElement);
    }

    private Object getSemanticObject(String id) {
        return this.objectSearchService.getObject(this.editionContext, id).orElse(null);
    }

}

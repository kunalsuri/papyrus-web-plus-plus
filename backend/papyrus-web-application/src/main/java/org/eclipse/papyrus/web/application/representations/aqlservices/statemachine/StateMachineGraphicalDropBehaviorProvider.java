/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo, Artal Technologies.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *  Aurelien Didier (Artal Technologies) - Issue 190
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.aqlservices.statemachine;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.drop.diagrams.StateMachineInternalSourceToRepresentationDropChecker;
import org.eclipse.papyrus.uml.domain.services.drop.diagrams.StateMachineInternalSourceTorepresentationDropBehaviorProvider;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.web.application.representations.IWebInternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.GraphicalDropSwitch;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Provides the behavior on a graphical drop event in the "State Machine" Diagram.
 *
 * This class is copied and adapted from
 * {@link org.eclipse.papyrus.web.application.representations.aqlservices.profile.ProfileGraphicalDropBehaviorProvider}.
 *
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class StateMachineGraphicalDropBehaviorProvider implements IWebInternalSourceToRepresentationDropBehaviorProvider {

    private final IEditingContext editionContext;

    private final IViewHelper viewHelper;


    private final ECrossReferenceAdapter crossRef;

    private final IEditableChecker editableChecker;

    private final DiagramNavigator diagramNavigator;

    private final IObjectSearchService objectSearchService;

    /**
     * Logger used to report errors and warnings to the user.
     */
    private final ILogger logger;

    /**
     * Constructor.
     *
     * @param editionContext
     *            editing context used to retrieve semantic target
     * @param viewHelper
     *            the helper used to create element on a diagram
     * @param objectSearchService
     *            service used to retrieve semantic target according to node id
     * @param crossRef
     *            An adapter used to get inverse references
     * @param editableChecker
     *            Object that check if an element can be edited
     * @param diagramNavigator
     *            the helper used to navigate inside a diagram and/or to its description
     * @param logger
     *            Logger used to report errors and warnings to the user
     */
    public StateMachineGraphicalDropBehaviorProvider(IEditingContext editionContext, IViewHelper viewHelper,
            IObjectSearchService objectSearchService, ECrossReferenceAdapter crossRef,
            IEditableChecker editableChecker, DiagramNavigator diagramNavigator, ILogger logger) {
        this.diagramNavigator = Objects.requireNonNull(diagramNavigator);
        this.crossRef = Objects.requireNonNull(crossRef);
        this.editableChecker = Objects.requireNonNull(editableChecker);
        this.editionContext = Objects.requireNonNull(editionContext);
        this.viewHelper = Objects.requireNonNull(viewHelper);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.logger = logger;
    }

    /**
     * Handles a graphical drop event.
     *
     * @param droppedElement
     *            the semantic element to drop
     * @param targetElement
     *            the semantic target of the dropped element
     * @param droppedNode
     *            the node to drop
     * @param targetNode
     *            the target node or <code>null</code> if the drop occurred on the diagram
     */
    @Override
    public void handleGraphicalDrop(EObject droppedElement, EObject targetElement, Node droppedNode, Node targetNode) {
        Optional<Node> optionalTargetNode = Optional.ofNullable(targetNode);
        Optional<EObject> optionalOldContainer = Optional.ofNullable(droppedElement.eContainer());
        Optional<EObject> optionalNewContainer = Optional.ofNullable(targetElement);
        new GraphicalDropSwitch(optionalTargetNode, optionalOldContainer, optionalNewContainer, this.viewHelper, this.diagramNavigator, droppedNode, this.logger) //
                .withDropChecker(new StateMachineInternalSourceToRepresentationDropChecker()) //
                .withDropProvider(new StateMachineInternalSourceTorepresentationDropBehaviorProvider()) //
                .withCrossRef(this.crossRef) //
                .withEditableChecker(this.editableChecker) //
                .withEObjectResolver(this::getSemanticObject) //
                .doSwitch(droppedElement);
    }

    private Object getSemanticObject(String id) {
        return this.objectSearchService.getObject(this.editionContext, id).orElse(null);
    }

}

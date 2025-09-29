/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.application.representations.aqlservices.communication;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.papyrus.uml.domain.services.labels.ElementDefaultNameProvider;
import org.eclipse.papyrus.web.application.representations.uml.CODDiagramDescriptionBuilder;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IInputPreProcessor;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Sinks.Many;

/**
 * Service to provide actions before the input processing.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public class CommunicationInputPreProcessor implements IInputPreProcessor {

    private final IObjectSearchService objectSearchService;

    private final IIdentityService identityService;

    /**
     * The constructor.
     *
     * @param objectSearchService
     *            service used to retrieve semantic target according to node id
     *
     */
    public CommunicationInputPreProcessor(IObjectSearchService objectSearchService, IIdentityService identityService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.identityService = Objects.requireNonNull(identityService);
    }

    @Override
    public IInput preProcess(IEditingContext editingContext, IInput input, Many<ChangeDescription> changeDescriptionSink) {
        if (input instanceof CreateRepresentationInput createRepresentationInput) {
            if (CODDiagramDescriptionBuilder.COD_REP_NAME.equals(createRepresentationInput.representationName())) {
                Optional<Object> optionalTarget = this.objectSearchService.getObject(editingContext,
                        createRepresentationInput.objectId());
                if (this.shouldCreateIntermediateInteraction(optionalTarget)) {
                    Interaction newInteraction = this.createIntermediateInteraction(optionalTarget.get());
                    changeDescriptionSink.tryEmitNext(new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, editingContext.getId(), input));
                    String interactionIdExplorer = this.identityService.getId(newInteraction);
                    return new CreateRepresentationInput(createRepresentationInput.id(), createRepresentationInput.editingContextId(), createRepresentationInput.representationDescriptionId(),
                            interactionIdExplorer, createRepresentationInput.representationName());
                }
            }
        }
        return input;
    }

    /**
     * Check if an intermediate {@link Interaction} should be created before Communication diagram creation.
     *
     * @param target
     *            the context used initially to launch Communication diagram creation
     * @return {@code true} if an intermediate {@link Interaction} should be created, {@code false} otherwise.
     */
    private boolean shouldCreateIntermediateInteraction(Optional<Object> target) {
        if (target.isPresent()) {
            Object parent = target.get();
            return parent instanceof Package || (parent instanceof BehavioredClassifier && !(parent instanceof Interaction));
        }
        return false;
    }

    /**
     * Create an intermediate {@link Interaction} on the given {@code target}.
     *
     * @param target
     *            the parent of the new {@link Interaction}.
     * @return an intermediate {@link Interaction} on the given {@code target}.
     */
    private Interaction createIntermediateInteraction(Object target) {
        Interaction newInteraction = UMLFactory.eINSTANCE.createInteraction();
        ElementDefaultNameProvider elementDefaultNameProvider = new ElementDefaultNameProvider();
        if (target instanceof Package pack) {
            newInteraction.setName(elementDefaultNameProvider.getDefaultName(newInteraction, pack));
            pack.getPackagedElements().add(newInteraction);
        } else if (target instanceof BehavioredClassifier behavioredClassifier) {
            newInteraction.setName(elementDefaultNameProvider.getDefaultName(newInteraction, behavioredClassifier));
            behavioredClassifier.getOwnedBehaviors().add(newInteraction);
            behavioredClassifier.setClassifierBehavior(newInteraction);
        }
        return newInteraction;
    }

}

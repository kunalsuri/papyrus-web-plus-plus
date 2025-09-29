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
package org.eclipse.papyrus.web.application.representations.aqlservices.activity;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.papyrus.uml.domain.services.labels.ElementDefaultNameProvider;
import org.eclipse.papyrus.web.application.representations.uml.ADDiagramDescriptionBuilder;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IInputPreProcessor;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Sinks.Many;

/**
 * Service used to create intermediate semantic activities when creating Activity diagram representations.
 * <p>
 * This service allows to create a representation on an element that doesn't directly supports it. In this case this
 * class takes care of creating the intermediate semantic activity required to create the Activity diagram
 * representation.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class ActivityInputPreProcessor implements IInputPreProcessor {

    private final IObjectSearchService objectSearchService;

    private final IIdentityService identityService;

    /**
     * The constructor.
     *
     * @param objectSearchService
     *            service used to retrieve semantic target according to node id
     * @param identityService
     *            service used to get the identity of an object
     */
    public ActivityInputPreProcessor(IObjectSearchService objectSearchService, IIdentityService identityService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.identityService = Objects.requireNonNull(identityService);
    }

    @Override
    public IInput preProcess(IEditingContext editingContext, IInput input, Many<ChangeDescription> changeDescriptionSink) {
        if (input instanceof CreateRepresentationInput createRepresentationInput) {
            if (ADDiagramDescriptionBuilder.AD_REP_NAME.equals(createRepresentationInput.representationName())) {
                Optional<Object> optionalTarget = this.objectSearchService.getObject(editingContext,
                        createRepresentationInput.objectId());
                if (this.shouldCreateIntermediateActivity(optionalTarget)) {
                    Activity newActivity = this.createIntermediateActivity(optionalTarget.get());
                    changeDescriptionSink.tryEmitNext(new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, editingContext.getId(), input));
                    String activityIdExplorer = this.identityService.getId(newActivity);
                    return new CreateRepresentationInput(createRepresentationInput.id(), createRepresentationInput.editingContextId(), createRepresentationInput.representationDescriptionId(),
                            activityIdExplorer, createRepresentationInput.representationName());
                }
            }
        }
        return input;
    }

    /**
     * Check if an intermediate {@link Activity} should be created before Activity diagram creation.
     *
     * @param target
     *            the context used initially to launch Activity diagram creation
     *
     * @return {@code true} if an intermediate {@link Activity} should be created, {@code false} otherwise
     */
    private boolean shouldCreateIntermediateActivity(Optional<Object> target) {
        if (target.isPresent()) {
            Object parent = target.get();
            return parent instanceof Package || (parent instanceof BehavioredClassifier && !(parent instanceof Activity));
        }
        return false;
    }

    /**
     * Create an intermediate {@link Activity} on the given {@code target}.
     *
     * @param target
     *            the parent of the new {@link Activity}
     * @return an intermediate {@link Activity} on the given {@code target}
     */
    private Activity createIntermediateActivity(Object target) {
        Activity newActivity = UMLFactory.eINSTANCE.createActivity();
        ElementDefaultNameProvider elementDefaultNameProvider = new ElementDefaultNameProvider();
        if (target instanceof Package pack) {
            newActivity.setName(elementDefaultNameProvider.getDefaultName(newActivity, pack));
            pack.getPackagedElements().add(newActivity);
        } else if (target instanceof BehavioredClassifier behavioredClassifier) {
            newActivity.setName(elementDefaultNameProvider.getDefaultName(newActivity, behavioredClassifier));
            behavioredClassifier.getOwnedBehaviors().add(newActivity);
            behavioredClassifier.setClassifierBehavior(newActivity);
        }
        return newActivity;
    }

}

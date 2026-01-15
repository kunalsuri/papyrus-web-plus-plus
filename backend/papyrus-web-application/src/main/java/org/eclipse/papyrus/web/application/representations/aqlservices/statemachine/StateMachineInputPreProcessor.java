/*****************************************************************************
 * Copyright (c) 2025 CEA LIST, Obeo, Artal Technologies.
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
 *  Artal Technologies - Issue 232 : Enable State Machine diagram creation from Model
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.aqlservices.statemachine;

import java.util.Optional;

import org.eclipse.papyrus.uml.domain.services.labels.ElementDefaultNameProvider;
import org.eclipse.papyrus.web.application.representations.uml.SMDDiagramDescriptionBuilder;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IInputPreProcessor;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.ProtocolStateMachine;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.UMLFactory;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Sinks.Many;

/**
 * Service used to create intermediate semantic StateMachine when creating State Machine diagram representations.
 * <p>
 * This service allows to create a representation on an element that doesn't directly supports it. In this case this
 * class takes care of creating the intermediate semantic State Machine required to create the State Machine diagram
 * representation.
 * </p>
 *
 * This class is copied and adapted from
 * {@link org.eclipse.papyrus.web.application.representations.aqlservices.activity.ActivityInputPreProcessor}.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class StateMachineInputPreProcessor implements IInputPreProcessor {

    private IObjectService objectService;

    /**
     * The constructor.
     *
     * @param objectService
     *            service used to retrieve semantic target according to node id
     */
    public StateMachineInputPreProcessor(IObjectService objectService) {
        this.objectService = objectService;
    }

    @Override
    public IInput preProcess(IEditingContext editingContext, IInput input, Many<ChangeDescription> changeDescriptionSink) {
        if (input instanceof CreateRepresentationInput createRepresentationInput) {
            if (SMDDiagramDescriptionBuilder.SMD_REP_NAME.equals(createRepresentationInput.representationName())) {
                Optional<Object> optionalTarget = this.objectService.getObject(editingContext, createRepresentationInput.objectId());
                if (this.shouldCreateIntermediateStateMachine(optionalTarget)) {
                    StateMachine newSM = this.createIntermediateStateMachine(optionalTarget.get());
                    changeDescriptionSink.tryEmitNext(new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, editingContext.getId(), input));
                    String smIdExplorer = this.objectService.getId(newSM);
                    return new CreateRepresentationInput(createRepresentationInput.id(), createRepresentationInput.editingContextId(), createRepresentationInput.representationDescriptionId(),
                            smIdExplorer, createRepresentationInput.representationName());
                }
            }
        }
        return input;
    }

    /**
     * Check if an intermediate {@link StateMachine} should be created before StateMachine diagram creation.
     *
     * @param target
     *            the context used initially to launch StateMachine diagram creation
     *
     * @return {@code true} if an intermediate {@link StateMachine} should be created, {@code false} otherwise
     */
    private boolean shouldCreateIntermediateStateMachine(Optional<Object> target) {
        if (target.isPresent()) {
            Object parent = target.get();
            boolean needIntermediate = parent instanceof Package ||
                    parent instanceof BehavioredClassifier && !(parent instanceof StateMachine) || parent instanceof Interface;
            return needIntermediate;
        }
        return false;
    }

    /**
     * Create an intermediate {@link StateMachine} on the given {@code target}.
     *
     * @param target
     *            the parent of the new {@link StateMachine}
     * @return an intermediate {@link StateMachine} on the given {@code target}
     */
    private StateMachine createIntermediateStateMachine(Object target) {
        StateMachine newSM = UMLFactory.eINSTANCE.createStateMachine();
        ElementDefaultNameProvider elementDefaultNameProvider = new ElementDefaultNameProvider();
        if (target instanceof Package pack) {
            newSM.setName(elementDefaultNameProvider.getDefaultName(newSM, pack));
            pack.getPackagedElements().add(newSM);
        } else if (target instanceof BehavioredClassifier behavioredClassifier) {
            newSM.setName(elementDefaultNameProvider.getDefaultName(newSM, behavioredClassifier));
            behavioredClassifier.getOwnedBehaviors().add(newSM);
            behavioredClassifier.setClassifierBehavior(newSM);
        } else if (target instanceof Interface inter) {
            newSM = UMLFactory.eINSTANCE.createProtocolStateMachine();
            newSM.setName(elementDefaultNameProvider.getDefaultName(newSM, inter));
            inter.setProtocol((ProtocolStateMachine) newSM);
        }
        return newSM;
    }

}

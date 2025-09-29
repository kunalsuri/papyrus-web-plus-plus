/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.custom.widgets.containmentreference.handlers;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.papyrus.web.custom.widgets.containmentreference.ContainmentReferenceWidget;
import org.eclipse.papyrus.web.custom.widgets.containmentreference.dto.MoveContainmentReferenceItemHandlerParamaters;
import org.eclipse.papyrus.web.custom.widgets.containmentreference.dto.MoveContainmentReferenceItemInput;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.Monitoring;
import org.eclipse.sirius.components.collaborative.forms.api.IFormEventHandler;
import org.eclipse.sirius.components.collaborative.forms.api.IFormInput;
import org.eclipse.sirius.components.collaborative.forms.api.IFormQueryService;
import org.eclipse.sirius.components.collaborative.forms.messages.ICollaborativeFormMessageService;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.forms.Form;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;

/**
 * The handler of the move containment reference item event.
 *
 * @author Jerome Gout
 */
@Service
public class MoveContainmentReferenceItemEventHandler implements IFormEventHandler {

    private final ICollaborativeFormMessageService messageService;

    private final Counter counter;
    private final IObjectSearchService objectSearchService;

    private final IFormQueryService formQueryService;

    public MoveContainmentReferenceItemEventHandler(IFormQueryService formQueryService,
            ICollaborativeFormMessageService messageService, MeterRegistry meterRegistry,
            IObjectSearchService objectSearchService) {
        this.formQueryService = Objects.requireNonNull(formQueryService);
        this.messageService = Objects.requireNonNull(messageService);

        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    @Override
    public boolean canHandle(IFormInput formInput) {
        return formInput instanceof MoveContainmentReferenceItemInput;
    }

    @Override
    public void handle(One<IPayload> payloadSink, Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, Form form, IFormInput formInput) {
        this.counter.increment();

        String message = this.messageService.invalidInput(formInput.getClass().getSimpleName(), MoveContainmentReferenceItemInput.class.getSimpleName());
        IPayload payload = new ErrorPayload(formInput.id(), message);
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, formInput.representationId(), formInput);

        if (formInput instanceof MoveContainmentReferenceItemInput input) {
            var optionalReferenceWidget = this.formQueryService.findWidget(form, input.referenceWidgetId())
                    .filter(ContainmentReferenceWidget.class::isInstance)
                    .map(ContainmentReferenceWidget.class::cast);

            IStatus status;
            if (optionalReferenceWidget.map(ContainmentReferenceWidget::isReadOnly).filter(Boolean::booleanValue).isPresent()) {
                status = new Failure("Read-only widget can not be edited");
            } else {
                Optional<MoveContainmentReferenceItemHandlerParamaters> handlerInput = this.getHandlerInput(editingContext, input);
                status = optionalReferenceWidget.map(ContainmentReferenceWidget::getMoveHandler)
                        .map(handler -> handler.apply(handlerInput.get()))
                        .orElse(new Failure(""));
            }
            if (status instanceof Success success) {
                changeDescription = new ChangeDescription(success.getChangeKind(), formInput.representationId(), formInput, success.getParameters());
                payload = new SuccessPayload(formInput.id(), success.getMessages());
            } else if (status instanceof Failure failure) {
                payload = new ErrorPayload(formInput.id(), failure.getMessages());
            }
        }

        changeDescriptionSink.tryEmitNext(changeDescription);
        payloadSink.tryEmitValue(payload);
    }

    private Optional<MoveContainmentReferenceItemHandlerParamaters> getHandlerInput(IEditingContext editingContext, MoveContainmentReferenceItemInput input) {
        return this.objectSearchService.getObject(editingContext, input.referenceItemId()).flatMap(value -> {
            return Optional.of(new MoveContainmentReferenceItemHandlerParamaters(value, input.fromIndex(), input.toIndex()));
        });
    }
}

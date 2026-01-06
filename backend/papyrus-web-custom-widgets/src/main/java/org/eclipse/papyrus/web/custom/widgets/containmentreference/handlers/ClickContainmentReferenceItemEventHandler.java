/*****************************************************************************
 * Copyright (c) 2023, 2026 CEA LIST, Obeo.
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

import java.util.Collection;
import java.util.Objects;

import org.eclipse.papyrus.web.custom.widgets.containmentreference.ContainmentReferenceItem;
import org.eclipse.papyrus.web.custom.widgets.containmentreference.ContainmentReferenceWidget;
import org.eclipse.papyrus.web.custom.widgets.containmentreference.dto.ClickContainmentReferenceItemInput;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.Monitoring;
import org.eclipse.sirius.components.collaborative.forms.api.IFormEventHandler;
import org.eclipse.sirius.components.collaborative.forms.api.IFormInput;
import org.eclipse.sirius.components.collaborative.forms.api.IFormQueryService;
import org.eclipse.sirius.components.collaborative.forms.messages.ICollaborativeFormMessageService;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
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
 * The handler of the click containment reference item event.
 *
 * @author Jerome Gout
 */
@Service
public class ClickContainmentReferenceItemEventHandler implements IFormEventHandler {

    private final Counter counter;

    private final IFormQueryService formQueryService;

    private final ICollaborativeFormMessageService messageService;

    public ClickContainmentReferenceItemEventHandler(IFormQueryService formQueryService, ICollaborativeFormMessageService messageService, MeterRegistry meterRegistry) {
        this.formQueryService = Objects.requireNonNull(formQueryService);
        this.messageService = Objects.requireNonNull(messageService);

        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IFormInput formInput) {
        return formInput instanceof ClickContainmentReferenceItemInput;
    }

    @Override
    public void handle(One<IPayload> payloadSink, Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, Form form, IFormInput formInput) {
        this.counter.increment();

        String message = this.messageService.invalidInput(formInput.getClass().getSimpleName(), ClickContainmentReferenceItemInput.class.getSimpleName());
        IPayload payload = new ErrorPayload(formInput.id(), message);
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, formInput.representationId(), formInput);

        if (formInput instanceof ClickContainmentReferenceItemInput input) {
            var optionalReferenceWidget = this.formQueryService.findWidget(form, input.referenceWidgetId())
                    .filter(ContainmentReferenceWidget.class::isInstance)
                    .map(ContainmentReferenceWidget.class::cast);

            IStatus status;
            if (optionalReferenceWidget.map(ContainmentReferenceWidget::isReadOnly).filter(Boolean::booleanValue).isPresent()) {
                status = new Failure("Read-only widget can not be edited");
            } else {
                var optionalReferenceValue = optionalReferenceWidget
                        .stream()
                        .map(ContainmentReferenceWidget::getReferenceValues)
                        .flatMap(Collection::stream)
                        .filter(item -> item.getId().equals(input.referenceItemId()))
                        .findFirst();

                status = optionalReferenceValue.map(ContainmentReferenceItem::getClickHandler)
                        .map(handler -> handler.apply(input.clickEventKind()))
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

}

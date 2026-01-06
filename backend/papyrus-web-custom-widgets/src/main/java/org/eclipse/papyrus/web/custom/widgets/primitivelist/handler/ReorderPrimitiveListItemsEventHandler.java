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
package org.eclipse.papyrus.web.custom.widgets.primitivelist.handler;

import java.util.Objects;

import org.eclipse.papyrus.web.custom.widgets.primitivelist.PrimitiveListWidget;
import org.eclipse.papyrus.web.custom.widgets.primitivelist.dto.ReorderPrimitiveListHandlerParameters;
import org.eclipse.papyrus.web.custom.widgets.primitivelist.dto.ReorderPrimitiveListItemsInput;
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
 * The handler of the reorder list items event.
 *
 * @author Jerome Gout
 */
@Service
public class ReorderPrimitiveListItemsEventHandler implements IFormEventHandler {

    private final ICollaborativeFormMessageService messageService;

    private final Counter counter;

    private final IFormQueryService formQueryService;

    public ReorderPrimitiveListItemsEventHandler(IFormQueryService formQueryService, ICollaborativeFormMessageService messageService, MeterRegistry meterRegistry) {
        this.formQueryService = Objects.requireNonNull(formQueryService);
        this.messageService = Objects.requireNonNull(messageService);

        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IFormInput formInput) {
        return formInput instanceof ReorderPrimitiveListItemsInput;
    }

    @Override
    public void handle(One<IPayload> payloadSink, Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, Form form, IFormInput formInput) {
        this.counter.increment();

        String message = this.messageService.invalidInput(formInput.getClass().getSimpleName(), ReorderPrimitiveListItemsInput.class.getSimpleName());
        IPayload payload = new ErrorPayload(formInput.id(), message);
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, formInput.representationId(), formInput);

        if (formInput instanceof ReorderPrimitiveListItemsInput input) {

            var optionalList = this.formQueryService.findWidget(form, input.listId())
                    .filter(PrimitiveListWidget.class::isInstance)
                    .map(PrimitiveListWidget.class::cast);

            IStatus status;
            if (optionalList.map(PrimitiveListWidget::isReadOnly).filter(Boolean::booleanValue).isPresent()) {
                status = new Failure("Read-only widget can not be edited");
            } else {
                status = optionalList.map(PrimitiveListWidget::getReorderHandler).map(handler -> handler.apply(this.getHandlerInput(input))).orElse(new Failure(""));
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

    private ReorderPrimitiveListHandlerParameters getHandlerInput(ReorderPrimitiveListItemsInput input) {
        return new ReorderPrimitiveListHandlerParameters(input.itemId(), input.fromIndex(), input.toIndex());
    }
}

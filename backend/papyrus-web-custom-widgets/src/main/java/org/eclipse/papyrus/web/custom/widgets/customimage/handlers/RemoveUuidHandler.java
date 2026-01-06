/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 210
 *****************************************************************************/
package org.eclipse.papyrus.web.custom.widgets.customimage.handlers;

import java.util.Objects;

import org.eclipse.papyrus.web.custom.widgets.customimage.CustomImageWidget;
import org.eclipse.papyrus.web.custom.widgets.customimage.dto.RemoveUuidInput;
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
 * Service that handles removing the uuid in a custom image widget.
 *
 * @author tiboue
 */
@Service
public class RemoveUuidHandler implements IFormEventHandler {

    private final ICollaborativeFormMessageService messageService;

    private final IFormQueryService formQueryService;

    private final Counter counter;

    public RemoveUuidHandler(IFormQueryService formQueryService, ICollaborativeFormMessageService messageService, MeterRegistry meterRegistry) {
        this.formQueryService = Objects.requireNonNull(formQueryService);
        this.messageService = Objects.requireNonNull(messageService);

        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IFormInput formInput) {
        return formInput instanceof RemoveUuidInput;
    }

    @Override
    public void handle(One<IPayload> payloadSink, Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, Form form, IFormInput formInput) {
        this.counter.increment();
        String message = this.messageService.invalidInput(formInput.getClass().getSimpleName(), RemoveUuidInput.class.getSimpleName());
        IPayload payload = new ErrorPayload(formInput.id(), message);
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, formInput.representationId(), formInput);

        if (formInput instanceof RemoveUuidInput input) {

            var optionalUuidExpression = this.formQueryService.findWidget(form, input.customImageId())
                    .filter(CustomImageWidget.class::isInstance)
                    .map(CustomImageWidget.class::cast);

            IStatus status;
            if (optionalUuidExpression.map(CustomImageWidget::isReadOnly).filter(Boolean::booleanValue).isPresent()) {
                status = new Failure("Read-only widget can not be edited");
            } else {
                status = optionalUuidExpression.map(CustomImageWidget::getRemoveUuidHandler).map(handler -> handler.apply(input.removeUuid())).orElse(new Failure(""));

            }
            if (status instanceof Success success) {
                payload = new SuccessPayload(formInput.id(), success.getMessages());
                changeDescription = new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, formInput.representationId(), formInput);
            } else if (status instanceof Failure failure) {
                payload = new ErrorPayload(formInput.id(), failure.getMessages());
            }
        }

        changeDescriptionSink.tryEmitNext(changeDescription);
        payloadSink.tryEmitValue(payload);
    }

}

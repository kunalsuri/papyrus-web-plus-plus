/*****************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.custom.widgets.primitiveradio;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.eclipse.papyrus.web.custom.widgets.primitiveradio.dto.NewValueInput;
import org.eclipse.papyrus.web.custom.widgets.primitiveradio.handlers.NewValueHandler;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.forms.api.IFormQueryService;
import org.eclipse.sirius.components.collaborative.forms.messages.ICollaborativeFormMessageService;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.forms.AbstractWidget;
import org.eclipse.sirius.components.forms.Form;
import org.eclipse.sirius.components.forms.Group;
import org.eclipse.sirius.components.forms.Page;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import reactor.core.publisher.Sinks;

/**
 * Unit tests for the primitive radio widget handler.
 *
 * @author Jerome Gout
 */
public class NewValuePrimitiveRadioHandlerTests {

    private static final String MEMBER_END_OWNER_ID = "Member end owner id";

    private static final String ASSOCIATION = "Association";

    private static final String CLASSIFIER = "Classifier";

    private static final UUID FORM_ID = UUID.randomUUID();

    private static final String GROUP_ID = "groupId";

    private static final String GROUP_LABEL = "group label";

    private static final String PAGE_ID = "pageId";

    private static final String PAGE_LABEL = "page label";

    private static final String TARGET_OBJECT_ID = "targetObjectId";

    @Test
    public void testNewValueHandler() {

        var input = new NewValueInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), MEMBER_END_OWNER_ID, CLASSIFIER);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> newValueHandler = newValue -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        PrimitiveRadio meoWidget = PrimitiveRadio.newPrimitiveRadio(MEMBER_END_OWNER_ID)
                .label("label")
                .newValueHandler(newValueHandler)
                .candidateValue(ASSOCIATION)
                .candidateList(List.of(CLASSIFIER, ASSOCIATION))
                .diagnostics(List.of())
                .readOnly(false)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(meoWidget))
                .build();

        Page page = Page.newPage(PAGE_ID)
                .label(PAGE_LABEL)
                .groups(Collections.singletonList(group))
                .build();

        Form form = Form.newForm(FORM_ID.toString())
                .targetObjectId(TARGET_OBJECT_ID)
                .descriptionId(UUID.randomUUID().toString())
                .pages(Collections.singletonList(page))
                .build();

        IFormQueryService formQueryService = new IFormQueryService.NoOp() {
            @Override
            public Optional<AbstractWidget> findWidget(Form form, String widgetId) {
                return Optional.of(meoWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        NewValueHandler handler = new NewValueHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);

        ChangeDescription changeDescription = changeDescriptionSink.asFlux().blockFirst();
        assertThat(changeDescription.getKind()).isEqualTo(ChangeKind.SEMANTIC_CHANGE);

        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(SuccessPayload.class);

        assertThat(hasBeenExecuted.get()).isTrue();
    }

    @Test
    public void testNewValueHandlerReadOnly() {

        var input = new NewValueInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), MEMBER_END_OWNER_ID, CLASSIFIER);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> newValueHandler = newValue -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        PrimitiveRadio meoWidget = PrimitiveRadio.newPrimitiveRadio(MEMBER_END_OWNER_ID)
                .label("label")
                .newValueHandler(newValueHandler)
                .candidateValue(ASSOCIATION)
                .candidateList(List.of(CLASSIFIER, ASSOCIATION))
                .diagnostics(List.of())
                .readOnly(true)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(meoWidget))
                .build();

        Page page = Page.newPage(PAGE_ID)
                .label(PAGE_LABEL)
                .groups(Collections.singletonList(group))
                .build();

        Form form = Form.newForm(FORM_ID.toString())
                .targetObjectId(TARGET_OBJECT_ID)
                .descriptionId(UUID.randomUUID().toString())
                .pages(Collections.singletonList(page))
                .build();

        IFormQueryService formQueryService = new IFormQueryService.NoOp() {
            @Override
            public Optional<AbstractWidget> findWidget(Form form, String widgetId) {
                return Optional.of(meoWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        NewValueHandler handler = new NewValueHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);

        handler.handle(payloadSink, changeDescriptionSink, new IEditingContext.NoOp(), form, input);

        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(ErrorPayload.class);
        assertThat(((ErrorPayload) payload).message()).isEqualTo("Read-only widget can not be edited");
        assertThat(hasBeenExecuted.get()).isFalse();
    }


}

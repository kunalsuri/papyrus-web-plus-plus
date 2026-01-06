/*****************************************************************************
 * Copyright (c) 2023, 2026 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.custom.widgets.customimage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.eclipse.papyrus.web.custom.widgets.customimage.dto.NewUuidInput;
import org.eclipse.papyrus.web.custom.widgets.customimage.dto.RemoveUuidInput;
import org.eclipse.papyrus.web.custom.widgets.customimage.handlers.NewUuidHandler;
import org.eclipse.papyrus.web.custom.widgets.customimage.handlers.RemoveUuidHandler;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
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
 * Unit tests of the widget reference handlers.
 *
 * @author tiboue
 */
public class CustomImageTest {

    private static final String EDITING_READ_ONLY_WIDGET_ERROR = "Read-only widget can not be edited";

    private static final String CUSTOM_IMAGE_LABEL = "customImage label";

    private static final String GROUP_ID = "groupId";

    private static final String GROUP_LABEL = "group label";

    private static final String PAGE_ID = "pageId";

    private static final String PAGE_LABEL = "page label";

    private static final String TARGET_OBJECT_ID = "targetObjectId";

    private static final String FORM_LABEL = "form label";

    private static final String CI_WIDGET_ID = "CustomImage id";

    private static final String CI_WIDGET_UUID = "/custom/test_id";

    private static final UUID FORM_ID = UUID.randomUUID();

    @Test
    public void testHandlers() {

        var newUuidinput = new NewUuidInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), CI_WIDGET_ID, "");
        var removeUuidinput = new RemoveUuidInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), CI_WIDGET_ID, CI_WIDGET_UUID);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> newUuid = (uuid) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };
        Function<String, IStatus> removeUuid = (uuid) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        CustomImageWidget customImageWidget = CustomImageWidget.newCustomImage(CI_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(CUSTOM_IMAGE_LABEL)
                .currentUuid("")
                .newUuidHandler(newUuid)
                .removeUuidHandler(removeUuid)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(customImageWidget))
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
                return Optional.of(customImageWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        NewUuidHandler newUUidhandler = new NewUuidHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(newUUidhandler.canHandle(editingContext, newUuidinput)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        newUUidhandler.handle(payloadSink, changeDescriptionSink, editingContext, form, newUuidinput);
        changeDescriptionSink.asFlux().blockFirst();
        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(SuccessPayload.class);
        assertThat(hasBeenExecuted.get()).isTrue();

        RemoveUuidHandler removeUUidHandler = new RemoveUuidHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(removeUUidHandler.canHandle(editingContext, removeUuidinput)).isTrue();

        changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        payloadSink = Sinks.one();

        removeUUidHandler.handle(payloadSink, changeDescriptionSink, editingContext, form, removeUuidinput);
        changeDescriptionSink.asFlux().blockFirst();
        payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(SuccessPayload.class);
        assertThat(hasBeenExecuted.get()).isTrue();
    }

    @Test
    public void testHandlersReadOnly() {
        var newUuidinput = new NewUuidInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), CI_WIDGET_ID, CI_WIDGET_UUID);
        var removeUuidinput = new RemoveUuidInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), CI_WIDGET_ID, CI_WIDGET_UUID);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> newUuid = (uuid) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };
        Function<String, IStatus> removeUuid = (uuid) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        CustomImageWidget customImageWidget = CustomImageWidget.newCustomImage(CI_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(CUSTOM_IMAGE_LABEL)
                .currentUuid("")
                .newUuidHandler(newUuid)
                .removeUuidHandler(removeUuid)
                .readOnly(true)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(customImageWidget))
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
                return Optional.of(customImageWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        NewUuidHandler newUUidhandler = new NewUuidHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(newUUidhandler.canHandle(editingContext, newUuidinput)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        newUUidhandler.handle(payloadSink, changeDescriptionSink, editingContext, form, newUuidinput);
        changeDescriptionSink.asFlux().blockFirst();
        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(ErrorPayload.class);
        assertThat(((ErrorPayload) payload).message()).isEqualTo(EDITING_READ_ONLY_WIDGET_ERROR);
        assertThat(hasBeenExecuted.get()).isFalse();

        RemoveUuidHandler removeUUidHandler = new RemoveUuidHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(removeUUidHandler.canHandle(editingContext, removeUuidinput)).isTrue();

        changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        payloadSink = Sinks.one();

        removeUUidHandler.handle(payloadSink, changeDescriptionSink, editingContext, form, removeUuidinput);
        changeDescriptionSink.asFlux().blockFirst();
        payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(ErrorPayload.class);
        assertThat(((ErrorPayload) payload).message()).isEqualTo(EDITING_READ_ONLY_WIDGET_ERROR);
        assertThat(hasBeenExecuted.get()).isFalse();

    }

}

/*****************************************************************************
 * Copyright (c) 2024, 2026 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.custom.widgets.primitivelist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.papyrus.web.custom.widgets.primitivelist.dto.AddPrimitiveListItemInput;
import org.eclipse.papyrus.web.custom.widgets.primitivelist.dto.PrimitiveListItem;
import org.eclipse.papyrus.web.custom.widgets.primitivelist.handler.AddPrimitiveListItemEventHandler;
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
 * Unit tests for the add primitive list item handler.
 *
 * @author Jerome Gout
 */
public class AddPrimitiveListItemHandlerTests {

    private static final String NEW_VALUE = "newValue";

    private static final String WIDGET_ID = "Primitive List id";

    private static final String CHANGE_DESCRIPTION_PARAMETER_KEY = "change_description_parameter_key";

    private static final UUID FORM_ID = UUID.randomUUID();

    private static final String GROUP_ID = "groupId";

    private static final String GROUP_LABEL = "group label";

    private static final String PAGE_ID = "pageId";

    private static final String PAGE_LABEL = "page label";

    private static final String TARGET_OBJECT_ID = "targetObjectId";

    @Test
    public void testAddPrimitiveListItemHandler() {
        String itemId = "Item Id";
        String changeKind = ChangeKind.SEMANTIC_CHANGE;

        var input = new AddPrimitiveListItemInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID()
                .toString(), WIDGET_ID, NEW_VALUE);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(CHANGE_DESCRIPTION_PARAMETER_KEY, itemId);
        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> addHandler = (newValue) -> {
            hasBeenExecuted.set(true);
            assertEquals(NEW_VALUE, newValue);
            return new Success(changeKind, parameters);
        };
        Supplier<IStatus> deleteHandler = () -> {
            return new Success(changeKind, parameters);
        };

        var item =  PrimitiveListItem.newPrimitiveListItem(itemId)
            .label("")
            .iconURL(List.of())
            .kind("")
            .deletable(true)
            .deleteHandler(deleteHandler)
            .build();

        var widget = PrimitiveListWidget.newPrimitiveList(WIDGET_ID)
                .iconURL(List.of())
                .label("")
                .diagnostics(List.of())
                .items(List.of(item))
                .readOnly(false)
                .newValueHandler(addHandler)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(widget))
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
                return Optional.of(widget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        var handler = new AddPrimitiveListItemEventHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
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
    public void testAddPrimitiveListItemHandlerReadOnly() {
        String itemId = "Item Id";
        String changeKind = ChangeKind.SEMANTIC_CHANGE;

        var input = new AddPrimitiveListItemInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID()
                .toString(), WIDGET_ID, NEW_VALUE);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(CHANGE_DESCRIPTION_PARAMETER_KEY, itemId);
        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> addHandler = (newValue) -> {
            hasBeenExecuted.set(true);
            return new Success(changeKind, parameters);
        };
        Supplier<IStatus> deleteHandler = () -> {
            return new Success(changeKind, parameters);
        };
        var item =  PrimitiveListItem.newPrimitiveListItem(itemId)
            .label("")
            .iconURL(List.of())
            .kind("")
            .deletable(true)//
            .deleteHandler(deleteHandler)
            .build();

        var widget = PrimitiveListWidget.newPrimitiveList(WIDGET_ID)
                .iconURL(List.of())
                .label("")
                .diagnostics(List.of())
                .items(List.of(item))
                .newValueHandler(addHandler)
                .readOnly(true)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(widget))
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
                return Optional.of(widget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        var handler = new AddPrimitiveListItemEventHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);

        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(ErrorPayload.class);
        assertThat(((ErrorPayload) payload).message()).isEqualTo("Read-only widget can not be edited");
        assertThat(hasBeenExecuted.get()).isFalse();
    }


}

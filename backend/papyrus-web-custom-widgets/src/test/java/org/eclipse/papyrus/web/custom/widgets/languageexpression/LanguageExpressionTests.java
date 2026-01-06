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
package org.eclipse.papyrus.web.custom.widgets.languageexpression;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.papyrus.web.custom.widgets.languageexpression.dto.AddLanguageInput;
import org.eclipse.papyrus.web.custom.widgets.languageexpression.dto.DeleteLanguageInput;
import org.eclipse.papyrus.web.custom.widgets.languageexpression.dto.EditLanguageBodyInput;
import org.eclipse.papyrus.web.custom.widgets.languageexpression.dto.MoveLanguageDirection;
import org.eclipse.papyrus.web.custom.widgets.languageexpression.dto.MoveLanguageInput;
import org.eclipse.papyrus.web.custom.widgets.languageexpression.handlers.AddLanguageHandler;
import org.eclipse.papyrus.web.custom.widgets.languageexpression.handlers.DeleteLanguageHandler;
import org.eclipse.papyrus.web.custom.widgets.languageexpression.handlers.EditLanguageBodyHandler;
import org.eclipse.papyrus.web.custom.widgets.languageexpression.handlers.MoveLanguageHandler;
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
 *
 * Unit tests of the widget reference handlers.
 *
 * @author Jerome Gout
 */
public class LanguageExpressionTests {

    private static final String EDITING_READ_ONLY_WIDGET_ERROR = "Read-only widget can not be edited";

    private static final String LANGUAGE_EXPRESSION_LABEL = "languageExpression label";

    private static final String GROUP_ID = "groupId";

    private static final String GROUP_LABEL = "group label";

    private static final String PAGE_ID = "pageId";

    private static final String PAGE_LABEL = "page label";

    private static final String TARGET_OBJECT_ID = "targetObjectId";

    private static final String JAVA_LANGUAGE_BODY = "java body";

    private static final String JAVA_LANGUAGE = "JAVA";

    private static final UUID FORM_ID = UUID.randomUUID();

    private static final String LE_WIDGET_ID = "LanguageExpressionWidget id";

    @Test
    public void testAddLanguageHandler() {

        var input = new AddLanguageInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), LE_WIDGET_ID, JAVA_LANGUAGE);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> addLanguageHandler = (newLanguage) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        LanguageExpression languageExpressionWidget = LanguageExpression.newLanguageExpression(LE_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(LANGUAGE_EXPRESSION_LABEL)
                .languages(Collections.emptyList())
                .addLanguageHandler(addLanguageHandler)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(languageExpressionWidget))
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
                return Optional.of(languageExpressionWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        AddLanguageHandler handler = new AddLanguageHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);
        changeDescriptionSink.asFlux().blockFirst();
        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(SuccessPayload.class);
        assertThat(hasBeenExecuted.get()).isTrue();
    }

    @Test
    public void testDeleteLanguageHandler() {

        var input = new DeleteLanguageInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), LE_WIDGET_ID, JAVA_LANGUAGE);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> deleteLanguageHandler = (language) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        LanguageExpression languageExpressionWidget = LanguageExpression.newLanguageExpression(LE_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(LANGUAGE_EXPRESSION_LABEL)
                .languages(Collections.emptyList())
                .deleteLanguageHandler(deleteLanguageHandler)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(languageExpressionWidget))
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

        IEditingContext editingContext = new IEditingContext.NoOp();

        IFormQueryService formQueryService = new IFormQueryService.NoOp() {
            @Override
            public Optional<AbstractWidget> findWidget(Form form, String widgetId) {
                return Optional.of(languageExpressionWidget);
            }
        };

        DeleteLanguageHandler handler = new DeleteLanguageHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);
        changeDescriptionSink.asFlux().blockFirst();
        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(SuccessPayload.class);
        assertThat(hasBeenExecuted.get()).isTrue();
    }

    @Test
    public void testEditLanguageBodyHandler() {

        var input = new EditLanguageBodyInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), LE_WIDGET_ID, JAVA_LANGUAGE, JAVA_LANGUAGE_BODY);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        BiFunction<String, String, IStatus> editLanguageBodyHandler = (language, body) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        LanguageExpression languageExpressionWidget = LanguageExpression.newLanguageExpression(LE_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(LANGUAGE_EXPRESSION_LABEL)
                .languages(Collections.emptyList())
                .editLanguageBodyHandler(editLanguageBodyHandler)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(languageExpressionWidget))
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
                return Optional.of(languageExpressionWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        EditLanguageBodyHandler handler = new EditLanguageBodyHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);
        changeDescriptionSink.asFlux().blockFirst();
        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(SuccessPayload.class);
        assertThat(hasBeenExecuted.get()).isTrue();
    }

    @Test
    public void testMoveLanguageHandler() {

        var input = new MoveLanguageInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), LE_WIDGET_ID, JAVA_LANGUAGE, MoveLanguageDirection.FORWARD);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        BiFunction<String, MoveLanguageDirection, IStatus> moveLanguageHandler = (language, direction) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        LanguageExpression languageExpressionWidget = LanguageExpression.newLanguageExpression(LE_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(LANGUAGE_EXPRESSION_LABEL)
                .languages(Collections.emptyList())
                .moveLanguageHandler(moveLanguageHandler)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(languageExpressionWidget))
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
                return Optional.of(languageExpressionWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        MoveLanguageHandler handler = new MoveLanguageHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);
        changeDescriptionSink.asFlux().blockFirst();
        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(SuccessPayload.class);
        assertThat(hasBeenExecuted.get()).isTrue();
    }

    @Test
    public void testAddLanguageHandlerReadOnly() {

        var input = new AddLanguageInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), LE_WIDGET_ID, JAVA_LANGUAGE);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> addLanguageHandler = (newLanguage) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        LanguageExpression languageExpressionWidget = LanguageExpression.newLanguageExpression(LE_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(LANGUAGE_EXPRESSION_LABEL)
                .languages(Collections.emptyList())
                .addLanguageHandler(addLanguageHandler)
                .readOnly(true)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(languageExpressionWidget))
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
                return Optional.of(languageExpressionWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        AddLanguageHandler handler = new AddLanguageHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);

        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(ErrorPayload.class);
        assertThat(((ErrorPayload) payload).message()).isEqualTo(EDITING_READ_ONLY_WIDGET_ERROR);
        assertThat(hasBeenExecuted.get()).isFalse();
    }

    @Test
    public void testDeleteLanguageHandlerReadOnly() {

        var input = new DeleteLanguageInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), LE_WIDGET_ID, JAVA_LANGUAGE);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<String, IStatus> deleteLanguageHandler = (language) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        LanguageExpression languageExpressionWidget = LanguageExpression.newLanguageExpression(LE_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(LANGUAGE_EXPRESSION_LABEL)
                .languages(Collections.emptyList())
                .deleteLanguageHandler(deleteLanguageHandler)
                .readOnly(true)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(languageExpressionWidget))
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
                return Optional.of(languageExpressionWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        DeleteLanguageHandler handler = new DeleteLanguageHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);

        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(ErrorPayload.class);
        assertThat(((ErrorPayload) payload).message()).isEqualTo(EDITING_READ_ONLY_WIDGET_ERROR);
        assertThat(hasBeenExecuted.get()).isFalse();
    }

    @Test
    public void testEditLanguageBodyHandlerReadOnly() {

        var input = new EditLanguageBodyInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), LE_WIDGET_ID, JAVA_LANGUAGE, JAVA_LANGUAGE_BODY);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        BiFunction<String, String, IStatus> editLanguageBodyHandler = (language, body) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        LanguageExpression languageExpressionWidget = LanguageExpression.newLanguageExpression(LE_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(LANGUAGE_EXPRESSION_LABEL)
                .languages(Collections.emptyList())
                .editLanguageBodyHandler(editLanguageBodyHandler)
                .readOnly(true)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(languageExpressionWidget))
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
                return Optional.of(languageExpressionWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        EditLanguageBodyHandler handler = new EditLanguageBodyHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);

        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(ErrorPayload.class);
        assertThat(((ErrorPayload) payload).message()).isEqualTo(EDITING_READ_ONLY_WIDGET_ERROR);
        assertThat(hasBeenExecuted.get()).isFalse();
    }

    @Test
    public void testMoveLanguageHandlerReadOnly() {

        var input = new MoveLanguageInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID().toString(), LE_WIDGET_ID, JAVA_LANGUAGE, MoveLanguageDirection.FORWARD);

        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        BiFunction<String, MoveLanguageDirection, IStatus> moveLanguageHandler = (language, direction) -> {
            hasBeenExecuted.set(true);
            return new Success();
        };

        LanguageExpression languageExpressionWidget = LanguageExpression.newLanguageExpression(LE_WIDGET_ID)
                .diagnostics(Collections.emptyList())
                .label(LANGUAGE_EXPRESSION_LABEL)
                .languages(Collections.emptyList())
                .moveLanguageHandler(moveLanguageHandler)
                .readOnly(true)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(languageExpressionWidget))
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
                return Optional.of(languageExpressionWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        MoveLanguageHandler handler = new MoveLanguageHandler(formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry());
        assertThat(handler.canHandle(editingContext, input)).isTrue();

        Sinks.Many<ChangeDescription> changeDescriptionSink = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.One<IPayload> payloadSink = Sinks.one();

        handler.handle(payloadSink, changeDescriptionSink, editingContext, form, input);

        IPayload payload = payloadSink.asMono().block();
        assertThat(payload).isInstanceOf(ErrorPayload.class);
        assertThat(((ErrorPayload) payload).message()).isEqualTo(EDITING_READ_ONLY_WIDGET_ERROR);
        assertThat(hasBeenExecuted.get()).isFalse();
    }

}

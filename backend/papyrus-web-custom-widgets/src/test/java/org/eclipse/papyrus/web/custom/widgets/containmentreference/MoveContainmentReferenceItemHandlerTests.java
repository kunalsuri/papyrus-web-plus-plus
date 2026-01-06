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
package org.eclipse.papyrus.web.custom.widgets.containmentreference;

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

import org.eclipse.papyrus.web.custom.widgets.containmentreference.dto.MoveContainmentReferenceItemHandlerParamaters;
import org.eclipse.papyrus.web.custom.widgets.containmentreference.dto.MoveContainmentReferenceItemInput;
import org.eclipse.papyrus.web.custom.widgets.containmentreference.handlers.MoveContainmentReferenceItemEventHandler;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.forms.api.IFormQueryService;
import org.eclipse.sirius.components.collaborative.forms.messages.ICollaborativeFormMessageService;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
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
 * Unit tests for the move containment reference item handler.
 *
 * @author Jerome Gout
 */
public class MoveContainmentReferenceItemHandlerTests {

    private static final String WIDGET_ID = "Containement Reference id";

    private static final String CHANGE_DESCRIPTION_PARAMETER_KEY = "change_description_parameter_key";

    private static final UUID FORM_ID = UUID.randomUUID();

    private static final String GROUP_ID = "groupId";

    private static final String GROUP_LABEL = "group label";

    private static final String PAGE_ID = "pageId";

    private static final String PAGE_LABEL = "page label";

    private static final String TARGET_OBJECT_ID = "targetObjectId";

    private static final String FORM_LABEL = "form label";

    @Test
    public void testMoveContainmentReferenceItemHandler() {
        String referenceValueId = "ReferenceValue Id";
        String changeKind = ChangeKind.SEMANTIC_CHANGE;

        var input = new MoveContainmentReferenceItemInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID()
                .toString(), WIDGET_ID, referenceValueId, 0, 1);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(CHANGE_DESCRIPTION_PARAMETER_KEY, referenceValueId);
        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<MoveContainmentReferenceItemHandlerParamaters, IStatus> moveHandler = params -> {
            hasBeenExecuted.set(true);
            assertEquals(0, params.fromIndex());
            assertEquals(1, params.toIndex());
            return new Success(changeKind, parameters);
        };

        ContainmentReferenceItem referenceValue = ContainmentReferenceItem.newReferenceItem(referenceValueId)
            .label("")
            .kind("")
            .build();

        ContainmentReferenceWidget refWidget = ContainmentReferenceWidget.newContainmentReferenceWidget(WIDGET_ID)
                .iconURL(List.of())
                .diagnostics(List.of())
                .referenceValues(Collections.singletonList(referenceValue))
                .descriptionId("")
                .label("")
                .readOnly(false)
                .ownerId("")
                .ownerKind("")
                .referenceKind("")
                .many(false)
                .moveHandler(moveHandler)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(refWidget))
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
                return Optional.of(refWidget);
            }
        };

        IObjectSearchService objectSearchService = new IObjectSearchService() {
            @Override
            public Optional<Object> getObject(IEditingContext editingContext, String objectId) {
                return Optional.of(refWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        MoveContainmentReferenceItemEventHandler handler = new MoveContainmentReferenceItemEventHandler(
                formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry(),
                objectSearchService);
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
    public void testMoveContainmentReferenceItemHandlerReadOnly() {
        String referenceValueId = "ReferenceValue Id";
        String changeKind = ChangeKind.SEMANTIC_CHANGE;

        var input = new MoveContainmentReferenceItemInput(UUID.randomUUID(), FORM_ID.toString(), UUID.randomUUID()
                .toString(), WIDGET_ID, referenceValueId, 0, 1);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(CHANGE_DESCRIPTION_PARAMETER_KEY, referenceValueId);
        AtomicBoolean hasBeenExecuted = new AtomicBoolean();
        Function<MoveContainmentReferenceItemHandlerParamaters, IStatus> moveHandler = params -> {
            hasBeenExecuted.set(true);
            return new Success(changeKind, parameters);
        };

        ContainmentReferenceItem referenceValue = ContainmentReferenceItem.newReferenceItem(referenceValueId)
                .label("")
                .kind("")
                .build();

        ContainmentReferenceWidget refWidget = ContainmentReferenceWidget.newContainmentReferenceWidget(WIDGET_ID)
                .iconURL(List.of())
                .diagnostics(List.of())
                .referenceValues(Collections.singletonList(referenceValue))
                .descriptionId("")
                .label("")
                .readOnly(true)
                .ownerId("")
                .ownerKind("")
                .referenceKind("")
                .many(false)
                .moveHandler(moveHandler)
                .build();

        Group group = Group.newGroup(GROUP_ID)
                .label(GROUP_LABEL)
                .widgets(Collections.singletonList(refWidget))
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
        // @formatter:on

        IFormQueryService formQueryService = new IFormQueryService.NoOp() {
            @Override
            public Optional<AbstractWidget> findWidget(Form form, String widgetId) {
                return Optional.of(refWidget);
            }
        };

        IEditingContext editingContext = new IEditingContext.NoOp();

        MoveContainmentReferenceItemEventHandler handler = new MoveContainmentReferenceItemEventHandler(
                formQueryService, new ICollaborativeFormMessageService.NoOp(), new SimpleMeterRegistry(),
                new IObjectSearchService.NoOp());
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

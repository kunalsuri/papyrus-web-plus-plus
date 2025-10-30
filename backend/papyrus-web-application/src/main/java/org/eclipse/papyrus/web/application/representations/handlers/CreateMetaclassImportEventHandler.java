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
package org.eclipse.papyrus.web.application.representations.handlers;

import java.util.List;
import java.util.Objects;

import org.eclipse.papyrus.web.application.Monitoring;
import org.eclipse.papyrus.web.application.profile.dto.ApplyProfileInput;
import org.eclipse.papyrus.web.application.representations.aqlservices.profile.ProfileDiagramService;
import org.eclipse.papyrus.web.application.representations.dto.CreateMetaclassImportInput;
import org.eclipse.papyrus.web.application.representations.dto.CreateMetaclassImportSuccessPayload;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramEventHandler;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramInput;
import org.eclipse.sirius.components.collaborative.messages.ICollaborativeMessageService;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.uml2.uml.ElementImport;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;

/**
 * Handler that creates an {@link ElementImport} for a given UML metaclass.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class CreateMetaclassImportEventHandler implements IDiagramEventHandler {

    private final ProfileDiagramService profileDiagramService;

    private final ICollaborativeMessageService messageService;

    private final Counter counter;

    public CreateMetaclassImportEventHandler(ICollaborativeMessageService messageService, ProfileDiagramService profileDiagramService, MeterRegistry meterRegistry) {
        this.profileDiagramService = Objects.requireNonNull(profileDiagramService);
        this.messageService = Objects.requireNonNull(messageService);

        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
    }

    @Override
    public boolean canHandle(IDiagramInput input) {
        return input instanceof CreateMetaclassImportInput;
    }

    @Override
    public void handle(One<IPayload> payloadSink, Many<ChangeDescription> changeDescriptionSink,
            IEditingContext editingContext, DiagramContext diagramContext, IDiagramInput diagramInput) {
        this.counter.increment();

        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, editingContext.getId(), diagramInput);
        IPayload payload = null;

        final String message;
        if (diagramInput instanceof CreateMetaclassImportInput createMetaclassImportInput) {
            String representationId = createMetaclassImportInput.representationId();
            String diagramElementId = createMetaclassImportInput.diagramElementId();
            List<String> metaclassIds = createMetaclassImportInput.metaclassIds();

            boolean result = true;
            for (String metaclassId : metaclassIds) {
                result = result && this.profileDiagramService.createMetaclassImport(editingContext, representationId, diagramElementId, metaclassId, diagramContext);
            }
            if (result) {
                payload = new CreateMetaclassImportSuccessPayload(createMetaclassImportInput.id());
                changeDescription = new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, editingContext.getId(), createMetaclassImportInput);
                message = null;
            } else {
                changeDescription = new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, editingContext.getId(), createMetaclassImportInput);
                message = "The metaclass import creation failed";
            }
        } else {
            message = this.messageService.invalidInput(diagramInput.getClass().getSimpleName(), ApplyProfileInput.class.getSimpleName());
        }

        if (payload == null) {
            payload = new ErrorPayload(diagramInput.id(), message);
        }

        payloadSink.tryEmitValue(payload);
        changeDescriptionSink.tryEmitNext(changeDescription);
    }
}

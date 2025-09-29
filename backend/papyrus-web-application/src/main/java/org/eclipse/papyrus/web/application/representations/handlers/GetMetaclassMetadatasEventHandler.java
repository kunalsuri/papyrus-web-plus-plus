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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.web.application.Monitoring;
import org.eclipse.papyrus.web.application.representations.UMLMetaclassMetadata;
import org.eclipse.papyrus.web.application.representations.aqlservices.profile.ProfileDiagramService;
import org.eclipse.papyrus.web.application.representations.dto.GetMetaclassMetadatasInput;
import org.eclipse.papyrus.web.application.representations.dto.GetMetaclassMetadatasSuccessPayload;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.IEditingContextEventHandler;
import org.eclipse.sirius.components.collaborative.messages.ICollaborativeMessageService;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.graphql.api.URLConstants;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;

/**
 * Handler that returns the metadatas of the UML metaclasses.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class GetMetaclassMetadatasEventHandler implements IEditingContextEventHandler {

    private final ProfileDiagramService profileDiagramService;

    private final ILabelService labelService;

    private final ICollaborativeMessageService messageService;

    private final Counter counter;
    private final IIdentityService identityService;

    public GetMetaclassMetadatasEventHandler(ICollaborativeMessageService messageService, MeterRegistry meterRegistry, ProfileDiagramService profileDiagramService, ILabelService labelService,
            IIdentityService identityService) {
        this.messageService = Objects.requireNonNull(messageService);
        this.profileDiagramService = Objects.requireNonNull(profileDiagramService);
        this.labelService = Objects.requireNonNull(labelService);
        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
        this.identityService = Objects.requireNonNull(identityService);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IInput input) {
        return input instanceof GetMetaclassMetadatasInput;
    }

    @Override
    public void handle(One<IPayload> payloadSink, Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, IInput input) {
        this.counter.increment();

        String message = this.messageService.invalidInput(input.getClass().getSimpleName(), GetMetaclassMetadatasInput.class.getSimpleName());
        IPayload payload = new ErrorPayload(input.id(), message);
        if (input instanceof GetMetaclassMetadatasInput && editingContext != null) {

            List<? extends Class> metaclasses = this.profileDiagramService.getMetaclasses(editingContext);
            List<UMLMetaclassMetadata> metaclassMetadatas = metaclasses.stream() //
                    .map(metaclass -> new UMLMetaclassMetadata(this.identityService.getId(metaclass), metaclass.getName(), this.getMetaclassImagePath(metaclass))) //
                    .toList();
            payload = new GetMetaclassMetadatasSuccessPayload(input.id(), metaclassMetadatas);
        }
        payloadSink.tryEmitValue(payload);
    }

    private String getMetaclassImagePath(Class metaclass) {
        Objects.requireNonNull(metaclass);
        // Copied from org.eclipse.papyrus.uml.tools.providers.UMLMetaclassLabelProvider
        EClassifier metaclassClassifier = UMLPackage.eINSTANCE.getEClassifier(metaclass.getName());
        if (metaclassClassifier instanceof EClass metaclassEClass) {
            if (!metaclassEClass.isAbstract() && !metaclassEClass.isInterface()) {
                // Copied from org.eclipse.papyrus.uml.tools.providers.UMLEClassLabelProvider
                final EObject instance = UMLFactory.eINSTANCE.create(metaclassEClass);
                List<String> metaclassImagePaths = this.labelService.getImagePaths(instance);
                if (!metaclassImagePaths.isEmpty()) {
                    // Return the first path, we don't want to display decorators here.
                    return URLConstants.IMAGE_BASE_PATH + metaclassImagePaths.get(0);
                }
            }
        }
        return null;
    }
}

/*****************************************************************************
 * Copyright (c) 2026 Obeo, CEA LIST.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.web.application.uml.services.library;

import java.util.List;
import java.util.Objects;

import org.eclipse.papyrus.web.application.uml.services.library.api.IUMLLibraryPublisher;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContextSearchService;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.eclipse.sirius.web.application.library.dto.PublishLibrariesInput;
import org.eclipse.sirius.web.application.library.services.api.ILibraryApplicationService;
import org.eclipse.sirius.web.application.library.services.api.ILibraryPublicationHandler;
import org.eclipse.sirius.web.application.project.services.api.IProjectEditingContextService;
import org.eclipse.sirius.web.domain.boundedcontexts.project.services.api.IProjectSearchService;
import org.springframework.stereotype.Service;

/**
 * Handles the publication of UML libraries from UML projects.
 *
 * Adapted from SysON SysONLibraryPublicationHandler
 *
 * @see ILibraryApplicationService
 * @see UMLLibraryPublicationListener
 *
 * @author Vincent LORENZO
 */
@Service
public class UMLLibraryPublicationHandler implements ILibraryPublicationHandler {

    private final IEditingContextSearchService editingContextSearchService;

    private final IProjectEditingContextService projectEditingContextService;

    private final IProjectSearchService projectSearchService;

    private final IUMLLibraryPublisher umlLibraryPublisher;

    public UMLLibraryPublicationHandler(final IEditingContextSearchService editingContextSearchService,
            final IProjectEditingContextService projectEditingContextService,
            final IProjectSearchService projectSearchService,
            final IUMLLibraryPublisher umlLibraryPublisher) {
        this.editingContextSearchService = Objects.requireNonNull(editingContextSearchService);
        this.projectEditingContextService = Objects.requireNonNull(projectEditingContextService);
        this.projectSearchService = Objects.requireNonNull(projectSearchService);
        this.umlLibraryPublisher = Objects.requireNonNull(umlLibraryPublisher);
    }

    @Override
    public boolean canHandle(PublishLibrariesInput input) {
        return Objects.equals(input.publicationKind(), "publishUMLModel");
    }

    @Override
    public IPayload handle(PublishLibrariesInput input) {
        IPayload payload = null;
        var editingContextId = input.editingContextId();
        var optionalProjectId = this.projectEditingContextService.getProjectId(editingContextId);
        if (optionalProjectId.isPresent()) {
            var projectId = optionalProjectId.get();
            var optionalProject = this.projectSearchService.findById(projectId);
            var optionalEditingContext = this.editingContextSearchService.findById(editingContextId)
                    .filter(IEMFEditingContext.class::isInstance)
                    .map(IEMFEditingContext.class::cast);

            if (optionalProject.isPresent() && optionalEditingContext.isPresent()) {
                payload = this.umlLibraryPublisher.publish(
                        input,
                        optionalEditingContext.get(),
                        projectId,
                        optionalProject.get().getName(),
                        input.version(),
                        input.description());
            } else {
                payload = new ErrorPayload(input.id(), List.of(new Message("Could not find project with following editingContextId '%s'.".formatted(editingContextId), MessageLevel.ERROR)));
            }
        } else {
            payload = new ErrorPayload(input.id(), List.of(new Message("Could not find project with following editingContextId '%s'.".formatted(editingContextId), MessageLevel.ERROR)));
        }
        return payload;
    }

}
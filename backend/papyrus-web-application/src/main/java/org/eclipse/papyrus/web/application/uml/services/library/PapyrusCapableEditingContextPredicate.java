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

import java.util.Objects;

import org.eclipse.papyrus.web.application.templates.projects.PapyrusUMLNatures;
import org.eclipse.papyrus.web.application.uml.services.library.api.IPapyrusCapableEditingContextPredicate;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.domain.boundedcontexts.project.Nature;
import org.eclipse.sirius.web.domain.boundedcontexts.project.Project;
import org.eclipse.sirius.web.domain.boundedcontexts.project.services.api.IProjectSearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.ProjectSemanticData;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.services.api.IProjectSemanticDataSearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.services.api.ISemanticDataSearchService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * Used to test if an editing context is capable of supporting a Papyrus model.
 *
 * Adapted from StudioCapableEditingContextPredicate
 *
 * @author Vincent LORENZO
 *
 */
@Service
public class PapyrusCapableEditingContextPredicate implements IPapyrusCapableEditingContextPredicate {

    private final IProjectSearchService projectSearchService;

    private final IProjectSemanticDataSearchService projectSemanticDataSearchService;

    public PapyrusCapableEditingContextPredicate(IProjectSearchService projectSearchService, IProjectSemanticDataSearchService projectSemanticDataSearchService,
            ISemanticDataSearchService semanticDataSearchService) {
        this.projectSearchService = Objects.requireNonNull(projectSearchService);
        this.projectSemanticDataSearchService = Objects.requireNonNull(projectSemanticDataSearchService);
    }

    @Override
    public boolean test(String editingContextId) {
        return this.isPapyrusProject(editingContextId);
    }

    private boolean isPapyrusProject(String editingContextId) {
        return new UUIDParser().parse(editingContextId)
                .flatMap(semanticDataId -> this.projectSemanticDataSearchService.findBySemanticDataId(AggregateReference.to(semanticDataId)))
                .map(ProjectSemanticData::getProject)
                .map(AggregateReference::getId)
                .flatMap(this.projectSearchService::findById)
                .filter(this::isPapyrus)
                .isPresent();
    }

    private boolean isPapyrus(Project project) {
        return project.getNatures().stream()
                .map(Nature::name)
                .anyMatch(PapyrusUMLNatures.UML::equals);
    }
}

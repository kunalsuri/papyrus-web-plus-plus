/*******************************************************************************
 * Copyright (c) 2025, 2026 CEA LIST, Obeo.
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
 *  Dilan EESHVARAN (dilan.eeshvaran@cea.fr) - Issue 285
 *******************************************************************************/
package org.eclipse.papyrus.web.application.configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.papyrus.web.application.templates.projects.ProfileProjectTemplateProvider;
import org.eclipse.papyrus.web.application.templates.projects.UMLProjectTemplateProvider;
import org.eclipse.sirius.web.application.capability.SiriusWebCapabilities;
import org.eclipse.sirius.web.application.capability.services.api.ICapabilityEvaluator;
import org.eclipse.sirius.web.application.project.dto.ProjectTemplateContext;
import org.eclipse.sirius.web.application.project.dto.ProjectTemplateDTO;
import org.eclipse.sirius.web.application.project.services.api.IProjectTemplateApplicationService;
import org.eclipse.sirius.web.application.project.services.api.IProjectTemplateProvider;
import org.eclipse.sirius.web.application.project.services.api.ProjectTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of IProjectTemplateApplicationService for Papyrus Web.
 * This implementation provides custom ordering of project templates on the home page.
 * The blank project template is excluded from the list of templates.
 * 
 * NOTE : This class overrides Sirius Web's ProjectTemplateApplicationService.
 * When updating the Sirius Web dependency, check if IProjectTemplateApplicationService 
 * has changed as it may require updating this class accordingly to avoid breaking the build.
 * </p>
 *
 * @author sbegaudeau
 */
@Service
@Primary
public class PapyrusProjectTemplateApplicationService implements IProjectTemplateApplicationService {

    // Custom ordering for project template cards on the home page.
    // Templates not in this map will be sorted alphabetically after the ones in this map.
    private static final Map<String, Integer> TEMPLATE_ORDER = Map.of(
            UMLProjectTemplateProvider.UML_WITH_PRIMITIVES_TEMPLATE_ID, 1,
            ProfileProjectTemplateProvider.PROFILE_WITH_PRIMITIVES_AND_UML_TEMPLATE_ID, 2,
            UMLProjectTemplateProvider.EMPTY_UML_TEMPLATE, 3,
            "UMLCppProject", 4
    );

    private final List<IProjectTemplateProvider> projectTemplateProviders;

    private final ICapabilityEvaluator capabilityEvaluator;

    public PapyrusProjectTemplateApplicationService(List<IProjectTemplateProvider> projectTemplateProviders, ICapabilityEvaluator capabilityEvaluator) {
        this.projectTemplateProviders = Objects.requireNonNull(projectTemplateProviders);
        this.capabilityEvaluator = Objects.requireNonNull(capabilityEvaluator);
    }

    @Override
    public Page<ProjectTemplateDTO> findAll(Pageable pageable, String context) {
        return switch (context) {
            case ProjectTemplateContext.PROJECT_BROWSER -> this.handleProjectBrowser(pageable);
            case ProjectTemplateContext.PROJECT_TEMPLATE_MODAL -> this.handleProjectTemplateModal(pageable);
            default -> this.handleProjectTemplateModal(pageable);
        };
    }

    @Override
    public List<ProjectTemplateDTO> findAll() {
        return this.getProjectTemplatesSortedByName().stream()
                .map(this::toDTO)
                .toList();
    }

    private List<ProjectTemplate> getProjectTemplatesSortedByName() {
        return this.projectTemplateProviders.stream()
                .map(IProjectTemplateProvider::getProjectTemplates)
                .flatMap(List::stream)
                .sorted(Comparator
                        .comparingInt((ProjectTemplate pt) -> TEMPLATE_ORDER.getOrDefault(pt.id(), Integer.MAX_VALUE))
                        .thenComparing(ProjectTemplate::label))
                .toList();
    }

    // excludes the "create-project" template id to remove the "+ Blank Project" card from the menu.
    private Page<ProjectTemplateDTO> handleProjectBrowser(Pageable pageable) {
        var projectTemplates = this.getProjectTemplatesSortedByName();

        // include only "upload-project" and "browse-all-project-templates" templates
        List<ProjectTemplate> siriusWebProjectTemplate = new ArrayList<>();
        this.getUploadProject().ifPresent(siriusWebProjectTemplate::add);
        this.getBrowseAllProjectTemplates().ifPresent(siriusWebProjectTemplate::add);

        int startIndex = (int) pageable.getOffset() * pageable.getPageSize();
        int endIndex = Math.min(((int) pageable.getOffset() + 1) * pageable.getPageSize(), projectTemplates.size() + siriusWebProjectTemplate.size());
        var projectTemplateDTOs = Stream.concat(projectTemplates.subList(startIndex, endIndex - siriusWebProjectTemplate.size()).stream(), siriusWebProjectTemplate.stream())
                .map(projectTemplate -> this.toDTO(projectTemplate))
                .toList();

        return new PageImpl<>(projectTemplateDTOs, pageable, projectTemplates.size());
    }

    private Page<ProjectTemplateDTO> handleProjectTemplateModal(Pageable pageable) {
        var projectTemplates = this.getProjectTemplatesSortedByName();

        int startIndex = (int) pageable.getOffset() * pageable.getPageSize();
        int endIndex = Math.min(((int) pageable.getOffset() + 1) * pageable.getPageSize(), projectTemplates.size());

        var projectTemplateDTOs = projectTemplates.subList(startIndex, endIndex).stream()
                .map(projectTemplate -> this.toDTO(projectTemplate))
                .toList();

        return new PageImpl<>(projectTemplateDTOs, pageable, projectTemplates.size());
    }

    private ProjectTemplateDTO toDTO(ProjectTemplate projectTemplate) {
        return new ProjectTemplateDTO(projectTemplate.id(), projectTemplate.label(), projectTemplate.imageURL());
    }

    private Optional<ProjectTemplate> getUploadProject() {
        Optional<ProjectTemplate> result = Optional.empty();
        var canCreate = this.capabilityEvaluator.hasCapability(SiriusWebCapabilities.PROJECT, null, SiriusWebCapabilities.Project.UPLOAD);
        if (canCreate) {
            result = Optional.of(new ProjectTemplate("upload-project", "", "", List.of()));
        }
        return result;
    }

    private Optional<ProjectTemplate> getBrowseAllProjectTemplates() {
        Optional<ProjectTemplate> result = Optional.empty();
        var aTemplateExists = this.projectTemplateProviders.stream().mapToLong(providers -> providers.getProjectTemplates().size()).sum() > 0;
        if (aTemplateExists) {
            result = Optional.of(new ProjectTemplate("browse-all-project-templates", "", "", List.of()));
        }
        return result;
    }
}

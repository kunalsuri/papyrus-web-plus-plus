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
package org.eclipse.papyrus.web.application.templates.projects;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramBuilderService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationMetadataPersistenceService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.web.application.project.services.api.ISemanticDataInitializer;
import org.eclipse.uml2.uml.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Initializes the contents of projects created from a Profile project template.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public class ProfileSemanticDataTemplateInitializer implements ISemanticDataInitializer {

    private static final String PROFILE_MODEL_TITLE = "Profile.profile.uml";

    private final Logger logger = LoggerFactory.getLogger(ProfileSemanticDataTemplateInitializer.class);

    private final TemplateInitializer initializerHelper;

    private final IDiagramBuilderService diagramBuilderService;

    private final IRepresentationPersistenceService representationPersistenceService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IRepresentationMetadataPersistenceService representationMetadataPersistenceService;

    public ProfileSemanticDataTemplateInitializer(TemplateInitializer initializerHelper, //
                                                  IDiagramBuilderService diagramBuilderService, //
                                                  PapyrusProjectTemplateInitializerParameters papyrusProjectTemplateInitializerParameters) {
        this.initializerHelper = initializerHelper;
        this.diagramBuilderService = diagramBuilderService;
        this.representationPersistenceService = papyrusProjectTemplateInitializerParameters.representationPersistenceService();
        this.representationDescriptionSearchService = papyrusProjectTemplateInitializerParameters.representationDescriptionSearchService();
        this.representationMetadataPersistenceService = papyrusProjectTemplateInitializerParameters.representationMetadataPersistenceService();
    }

    @Override
    public boolean canHandle(String projectTemplateId) {
        return List.of(ProfileProjectTemplateProvider.PROFILE_WITH_PRIMITIVES_AND_UML_TEMPLATE_ID).contains(projectTemplateId);
    }

    @Override
    public void handle(ICause cause, IEditingContext editingContext, String projectTemplateId) {
        if (ProfileProjectTemplateProvider.PROFILE_WITH_PRIMITIVES_AND_UML_TEMPLATE_ID.equals(projectTemplateId)) {
            this.initializeProfileWithPrimitivesAndUmlProjectContents(editingContext, cause);
        }
    }

    private void initializeProfileWithPrimitivesAndUmlProjectContents(IEditingContext editingContext, ICause cause) {
        try {
            Optional<Resource> resource = this.initializerHelper.initializeResourceFromClasspathFile(editingContext, PROFILE_MODEL_TITLE, "DefaultProfileWithPrimitiveAndUml.uml", cause);
            var optionalDiagram = resource.flatMap(r -> this.createProfileDiagram(editingContext, r, cause));
            if (optionalDiagram.isPresent()) {
                var diagram = optionalDiagram.get();
                Object semanticTarget = resource.map(r -> r.getContents().get(0)).orElse(null);
                var optionalRepresentationMetadata = this.createRepresentationMetadata(editingContext, diagram, semanticTarget);
                optionalRepresentationMetadata.ifPresent(rm -> {
                    this.representationMetadataPersistenceService.save(cause, editingContext, rm, diagram.getTargetObjectId());
                    this.representationPersistenceService.save(cause, editingContext, diagram);
                });
            }
        } catch (IOException e) {
            this.logger.error("Error while creating template", e);
        }
    }

    private Optional<? extends Diagram> createProfileDiagram(IEditingContext editingContext, Resource r, ICause cause) {
        Profile profile = (Profile) r.getContents().get(0);

        return this.diagramBuilderService
                .createDiagram(editingContext, diagramDescription -> PRDDiagramDescriptionBuilder.PRD_REP_NAME.equals(diagramDescription.getLabel()), profile, "Root Profile Diagram");
    }


    private Optional<RepresentationMetadata> createRepresentationMetadata(IEditingContext editingContext, Diagram diagram, Object semanticTarget) {
        return this.representationDescriptionSearchService.findById(editingContext, diagram.getDescriptionId())
                .filter(DiagramDescription.class::isInstance)
                .map(DiagramDescription.class::cast)
                .map(diagramDescription -> {
                    var variableManager = new VariableManager();
                    variableManager.put(VariableManager.SELF, semanticTarget);
                    variableManager.put(DiagramDescription.LABEL, diagramDescription.getLabel());
                    String label = diagramDescription.getLabelProvider().apply(variableManager);
                    List<String> iconURLs = diagramDescription.getIconURLsProvider().apply(variableManager);
                    return RepresentationMetadata.newRepresentationMetadata(diagram.getId())
                            .kind(diagram.getKind())
                            .label(label)
                            .descriptionId(diagram.getDescriptionId())
                            .iconURLs(iconURLs)
                            .build();
                });
    }
}

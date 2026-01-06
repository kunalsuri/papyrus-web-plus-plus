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
package org.eclipse.papyrus.web.application.templates.projects;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.papyrus.web.application.explorer.builder.UMLDefaultTreeDescriptionBuilder;
import org.eclipse.papyrus.web.application.properties.UMLDetailViewFromBuilder;
import org.eclipse.papyrus.web.application.properties.UMLPropertiesConfigurer;
import org.eclipse.papyrus.web.application.representations.PapyrusRepresentationDescriptionRegistry;
import org.eclipse.papyrus.web.application.tables.comment.UMLCommentTableRepresentationDescriptionBuilder;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextPersistenceService;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.IDAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.table.TableDescription;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.web.application.project.services.api.ISemanticDataInitializer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides Studio-specific project templates initializers.
 *
 * @author Arthur Daussy
 */
@Service
public class PapyrusStudioSemanticDataTemplatesInitializer implements ISemanticDataInitializer {
    private static final String STUDIO_PREFIX = " Studio";

    private final PapyrusRepresentationDescriptionRegistry papyrusRepresentationRegistry;

    private final IEditingContextPersistenceService editingContextPersistenceService;

    public PapyrusStudioSemanticDataTemplatesInitializer(
            PapyrusRepresentationDescriptionRegistry papyrusRepresentationRegistry, IEditingContextPersistenceService editingContextPersistenceService) {
        this.papyrusRepresentationRegistry = papyrusRepresentationRegistry;
        this.editingContextPersistenceService = Objects.requireNonNull(editingContextPersistenceService);
    }

    @Override
    public boolean canHandle(String templateId) {
        return List.of(PapyrusStudioProjectTemplatesProvider.PAPYRUS_STUDIO_TEMPLATE_ID).contains(templateId);
    }

    @Override
    public void handle(ICause cause, IEditingContext editingContext, String projectTemplateId) {
        if (PapyrusStudioProjectTemplatesProvider.PAPYRUS_STUDIO_TEMPLATE_ID.equals(projectTemplateId)) {
            this.initializePapyrusStudioProject(editingContext, cause);
        }
    }

    private void initializePapyrusStudioProject(IEditingContext editingContext, ICause cause) {
        Optional<ResourceSet> optionalResourceSet = this.getResourceSet(editingContext);

        if (optionalResourceSet.isPresent()) {
            ResourceSet resourceSet = optionalResourceSet.get();

            String formName = UMLPropertiesConfigurer.UML_DETAIL_VIEW_NAME + STUDIO_PREFIX;
            View view = new UMLDetailViewFromBuilder(formName).build();
            this.addToResource(resourceSet, view, formName);

            for (var papyrusView : this.papyrusRepresentationRegistry.getPapyrusViews()) {
                View copiedView = (View) EcoreUtil.copy(papyrusView);
                RepresentationDescription copiedDiagram = copiedView.getDescriptions().get(0);
                String name = copiedDiagram.getName() + STUDIO_PREFIX;
                copiedDiagram.setName(name);
                this.addToResource(resourceSet, copiedView, name);
            }

            View treeDescription = new UMLDefaultTreeDescriptionBuilder().createView();
            this.addToResource(resourceSet, treeDescription, treeDescription.getDescriptions().get(0).getName() + STUDIO_PREFIX);

            View commentTableView = new UMLCommentTableRepresentationDescriptionBuilder().createView();
            if (commentTableView.getDescriptions().get(0) instanceof TableDescription tableDescription) {
                tableDescription.setName(tableDescription.getName() + STUDIO_PREFIX);
                this.addToResource(resourceSet, commentTableView, tableDescription.getName());
            }

            this.editingContextPersistenceService.persist(cause, editingContext);
        }
    }

    private Optional<ResourceSet> getResourceSet(IEditingContext editingContext) {
        return Optional.of(editingContext)
                .filter(IEMFEditingContext.class::isInstance)
                .map(IEMFEditingContext.class::cast)
                .map(IEMFEditingContext::getDomain)
                .map(AdapterFactoryEditingDomain::getResourceSet);
    }

    public void addToResource(ResourceSet resourceSet, EObject root, String name) {
        JSONResourceFactory jsonResourceFactory = new JSONResourceFactory();
        JsonResource resource = jsonResourceFactory.createResourceFromPath(UUID.randomUUID().toString());
        resource.eAdapters().add(new ResourceMetadataAdapter(name));
        resource.getContents().add(root);
        resource.getAllContents().forEachRemaining(eObject -> {
            eObject.eAdapters().add(new IDAdapter(UUID.nameUUIDFromBytes(EcoreUtil.getURI(eObject).toString().getBytes())));
        });
        resourceSet.getResources().add(resource);
    }
}

/*****************************************************************************
 * Copyright (c) 2022, 2025 CEA LIST, Obeo.
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
 *******************************************************************************/
package org.eclipse.papyrus.web.utils;

import java.util.List;
import java.util.UUID;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.papyrus.web.application.templates.projects.PapyrusUMLNatures;
import org.eclipse.papyrus.web.tests.utils.UMLTestHelper;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateProjectMutationRunner;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextSearchService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.web.domain.boundedcontexts.project.Project;
import org.eclipse.sirius.web.domain.boundedcontexts.project.services.api.IProjectCreationService;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.ProjectSemanticData;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.services.api.IProjectSemanticDataSearchService;
import org.eclipse.sirius.web.domain.services.Success;
import org.eclipse.uml2.uml.UMLPackage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/**
 * Abstract test class for testing web UML features.
 *
 * @author Arthur Daussy
 */
public class AbstractWebUMLTest extends AbstractIntegrationTest {

    protected static final UMLPackage UML = UMLPackage.eINSTANCE;

    protected AdapterFactoryEditingDomain editingDomain;

    private final UMLTestHelper umlHelper = new UMLTestHelper();

    @Autowired
    private IEditingContextSearchService editingContextSearchService;

    @Autowired
    private IProjectCreationService projectCreationService;

    @Autowired
    private IProjectSemanticDataSearchService projectSemanticDataSearchService;

    @Autowired
    private IIdentityService identityService;

    @Autowired
    private IObjectSearchService objectSearchService;

    @Autowired
    private ILabelService labelService;

    private IEditingContext editingContext;

    @Autowired
    private PapyrusCreateProjectMutationRunner projectCreator;

    @BeforeEach
    public void before() {
        var result = this.projectCreationService.createProject(new ICause.NoOp(), UUID.randomUUID().toString(), List.of(PapyrusUMLNatures.UML));
        if (result instanceof Success<Project> success && success.data() != null) {
            this.editingContext = getEditingContext(success.data().getId());
            this.editingDomain = ((IEMFEditingContext) this.editingContext).getDomain();

            this.registerClasspathURIHandler();
        }
    }

    public ILabelService getLabelService() {
        return labelService;
    }

    public IIdentityService getIdentityService() {
        return identityService;
    }

    public IObjectSearchService getObjectSearchService() {
        return objectSearchService;
    }

    public IEditingContext getEditingContext() {
        return this.editingContext;
    }

    /**
     * Plugs a special {@link URIHandler} and {@link Factory.Registry} to be able to handle "classpath://$pathToFile" URIs.
     */
    protected void registerClasspathURIHandler() {
        AdapterFactoryEditingDomain localEditingDomain = this.getEditingDomain();
        localEditingDomain.getResourceSet().getURIConverter().getURIHandlers().add(0, new ClassPathResourceURIHandler());
        localEditingDomain.getResourceSet().getResourceFactoryRegistry().getProtocolToFactoryMap().put(ClassPathResourceURIHandler.CLASSPATH,
                new ClassPathResourceFactory(localEditingDomain.getResourceSet().getResourceFactoryRegistry()));
    }

    protected @NotNull IEditingContext getEditingContext(String projectId) {
        return this.projectSemanticDataSearchService.findByProjectId(AggregateReference.to(projectId))
                .map(ProjectSemanticData::getSemanticData)
                .map(AggregateReference::getId)
                .map(UUID::toString)
                .flatMap(this.editingContextSearchService::findById)
                .orElseThrow();
    }

    public AdapterFactoryEditingDomain getEditingDomain() {
        return this.editingDomain;
    }

    public ResourceSet getResourceSet() {
        return this.editingDomain.getResourceSet();
    }

    /**
     * Creates an element with the given type in the given parent. The containment reference is automatically computed by finding the feature containment {@link EReference} that can contains the given
     * object.
     *
     * @param <T>
     *         the expected type of the given element
     * @param type
     *         the expected type of the given element
     * @param parent
     *         the container
     * @return a new element
     */
    protected <T extends EObject> T createIn(java.lang.Class<T> type, EObject parent) {
        return this.umlHelper.createIn(type, parent);
    }

    protected <T extends EObject> T createInResource(java.lang.Class<T> type, Resource resource) {
        return this.umlHelper.createInResource(type, resource);
    }

    protected <T extends EObject> T createIn(java.lang.Class<T> type, EObject parent, String containmentRefName) {
        return this.umlHelper.createIn(type, parent, containmentRefName);
    }

    protected <T extends EObject> T create(java.lang.Class<T> type) {
        return this.umlHelper.create(type);
    }

    protected <T extends EObject> EClass getEClass(java.lang.Class<T> type) {
        return (EClass) UML.getEClassifier(type.getSimpleName());
    }

    protected Resource createResource(String resourceId) {
        JsonResource resource = new JSONResourceFactory().createResourceFromPath(resourceId);
        this.editingDomain.getResourceSet().getResources().add(resource);
        return resource;
    }

    protected Resource createResource() {
        return this.createResource(UUID.randomUUID().toString());
    }

}

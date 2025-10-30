/*******************************************************************************
 * Copyright (c) 2025 CEA LIST.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.web.application.uml;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.destroy.DestroyerStatus;
import org.eclipse.papyrus.uml.domain.services.destroy.ElementDestroyer;
import org.eclipse.papyrus.uml.domain.services.destroy.IDestroyer;
import org.eclipse.papyrus.uml.domain.services.status.State;
import org.eclipse.sirius.components.core.api.ChildCreationDescription;
import org.eclipse.sirius.components.core.api.IDefaultEditService;
import org.eclipse.sirius.components.core.api.IEditService;
import org.eclipse.sirius.components.core.api.IEditServiceDelegate;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.uml2.uml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class allows to override {@link IEditService} behavior.
 *
 * @author Laurent Fasani
 */
@Service
public class UMLEditServiceDelegate implements IEditServiceDelegate {
    
    private static final String ITEM_SEP = ",";

    private static final Logger LOGGER = LoggerFactory.getLogger(UMLEditServiceDelegate.class);

    private final IEditableChecker editableChecker;

    private final IDefaultEditService defaultEditService;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public UMLEditServiceDelegate(IEditableChecker editableChecker, IDefaultEditService defaultEditService) {
        this.editableChecker = editableChecker;
        this.defaultEditService = defaultEditService;
    }

    @Override
    public boolean canHandle(Object object) {
        return object instanceof Element;
    }

    @Override
    public boolean canHandle(IEditingContext editingContext) {
        return true;
    }

    @Override
    public List<ChildCreationDescription> getRootCreationDescriptions(IEditingContext editingContext, String domainId,
            boolean suggested, String referenceKind) {
        return defaultEditService.getRootCreationDescriptions(editingContext, domainId, suggested, referenceKind);
    }

    @Override
    public List<ChildCreationDescription> getChildCreationDescriptions(IEditingContext editingContext, String containerId,
            String referenceKind) {
        return defaultEditService.getChildCreationDescriptions(editingContext, containerId, referenceKind);
    }

    @Override
    public Optional<Object> createChild(IEditingContext editingContext, Object object,
            String childCreationDescriptionId) {
        return defaultEditService.createChild(editingContext, object, childCreationDescriptionId);
    }

    @Override
    public Optional<Object> createRootObject(IEditingContext editingContext, UUID documentId, String domainId,
            String rootObjectCreationDescriptionId) {
        return defaultEditService.createRootObject(editingContext, documentId, domainId,
                rootObjectCreationDescriptionId);
    }

    @Override
    public void delete(Object semanticElement) {
        if (semanticElement instanceof EObject semanticEObject) {
            ECrossReferenceAdapter adapter = this.getECrossReferenceAdapter(semanticEObject);
            DestroyerStatus destroyerStatus = this.buildDestroyer(adapter).destroy(semanticEObject);

            if (State.FAILED.equals(destroyerStatus.getState())) {
                String elements = destroyerStatus.getElements().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(ITEM_SEP));
                String errorMessage = destroyerStatus.getMessage() + ": " + elements;
                LOGGER.warn(errorMessage);
            }
        }
    }

    private ECrossReferenceAdapter getECrossReferenceAdapter(EObject source) {
        return source.eResource().getResourceSet().eAdapters().stream()
                .filter(a -> a instanceof ECrossReferenceAdapter)
                .map(a -> (ECrossReferenceAdapter) a)
                .findFirst().orElse(null);
    }

    private IDestroyer buildDestroyer(ECrossReferenceAdapter adapter) {
        return ElementDestroyer.buildDefault(adapter, this.editableChecker);
    }
}

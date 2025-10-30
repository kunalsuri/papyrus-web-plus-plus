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
package org.eclipse.papyrus.web.custom.widgets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MonoReferenceWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MultiReferenceWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PapyrusWidgetsPackage;
import org.eclipse.sirius.components.collaborative.widget.reference.api.IReferenceWidgetCreateElementHandler;
import org.eclipse.sirius.components.core.URLParser;
import org.eclipse.sirius.components.core.api.ChildCreationDescription;
import org.eclipse.sirius.components.core.api.IEditService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.SemanticKindConstants;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.emf.services.api.IEMFKindService;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IRepresentationDescriptionIdProvider;
import org.eclipse.sirius.components.view.emf.form.api.IFormIdProvider;
import org.eclipse.sirius.components.view.emf.form.api.IViewFormDescriptionSearchService;
import org.eclipse.sirius.components.view.emf.operations.api.IAddExecutor;
import org.eclipse.sirius.components.view.emf.operations.api.IClearExecutor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Mono and multi reference implementation of {@link IReferenceWidgetCreateElementHandler}.
 *
 * @author Jerome Gout
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PapyrusReferenceCreateElementHandler implements IReferenceWidgetCreateElementHandler {

    private static final String MULTI_REF_DESCRIPTION = PapyrusWidgetsPackage.eINSTANCE.getMultiReferenceWidgetDescription().getName();

    private static final String MONO_REF_DESCRIPTION = PapyrusWidgetsPackage.eINSTANCE.getMonoReferenceWidgetDescription().getName();

    private static final String CHILD_CREATION_DESCRIPTION_ID_SEPARATOR = "::";

    private final IEMFKindService emfKindService;

    private final IObjectSearchService objectSearchService;

    private final IEditService editService;

    private final ILabelService labelService;

    private final IViewFormDescriptionSearchService viewFormSearchService;

    private final IAQLInterpreterProvider interpreterProvider;

    private final IAddExecutor addExecutor;

    private final IClearExecutor clearExecutor;

    //CHECKSTYLE:OFF Injected parameters
    public PapyrusReferenceCreateElementHandler(IEMFKindService emfKindService, IObjectSearchService objectSearchService, IEditService editService, ILabelService labelService,
            IViewFormDescriptionSearchService viewFormSearchService,
            IAQLInterpreterProvider interpreterProvider, IAddExecutor addExecutor, IClearExecutor clearExecutor) {
        //CHECKSTYLE:OFF Injected parameters
        this.emfKindService = Objects.requireNonNull(emfKindService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.editService = Objects.requireNonNull(editService);
        this.labelService = Objects.requireNonNull(labelService);
        this.viewFormSearchService = Objects.requireNonNull(viewFormSearchService);
        this.interpreterProvider = Objects.requireNonNull(interpreterProvider);
        this.addExecutor = addExecutor;
        this.clearExecutor = clearExecutor;
    }

    @Override
    public boolean canHandle(String descriptionId) {
        if (descriptionId != null && descriptionId.startsWith(IFormIdProvider.FORM_ELEMENT_DESCRIPTION_PREFIX)) {
            Map<String, List<String>> parameters = new URLParser().getParameterValues(descriptionId);
            String sourceKind = parameters.get(IRepresentationDescriptionIdProvider.SOURCE_KIND).get(0);
            String kind = parameters.get(IRepresentationDescriptionIdProvider.KIND).get(0);
            return IRepresentationDescriptionIdProvider.VIEW_SOURCE_KIND.equals(sourceKind) && (MONO_REF_DESCRIPTION.equals(kind) || MULTI_REF_DESCRIPTION.equals(kind));
        } else {
            return false;
        }
    }

    @Override
    public List<ChildCreationDescription> getRootCreationDescriptions(IEditingContext editingContext, String domainId, String referenceKind, String descriptionId) {
        return List.of();
    }

    @Override
    public List<ChildCreationDescription> getChildCreationDescriptions(IEditingContext editingContext, String ownerId, String referenceKind, String descriptionId) {
        List<EClass> childrenTypes = this.getInstanciableTypesOf(editingContext, referenceKind);
        return this.toEClassByOwnerId(editingContext, ownerId)//
                .map(eClass -> this.createChildCreationDescription(eClass, childrenTypes))//
                .orElse(List.of());

    }

    private List<ChildCreationDescription> createChildCreationDescription(EClass eClass, List<EClass> childrenTypes) {
        return eClass.getEAllReferences().stream()//
                .filter(EReference::isContainment).flatMap(ref -> this.createChildCreationDescription(ref, childrenTypes).stream()).sorted(Comparator.comparing(ChildCreationDescription::label))
                .toList();
    }

    private List<ChildCreationDescription> createChildCreationDescription(EReference ref, List<EClass> childrenTypes) {
        List<ChildCreationDescription> children = new ArrayList<>();
        for (EClass child : childrenTypes) {
            if (ref.getEReferenceType().isSuperTypeOf(child)) {
                EObject instance = child.getEPackage().getEFactoryInstance().create(child);
                ChildCreationDescription description = new ChildCreationDescription(ref.getName() + CHILD_CREATION_DESCRIPTION_ID_SEPARATOR + child.getName(),
                        child.getName() + " (in " + ref.getName() + ")", this.labelService.getImagePaths(instance));
                children.add(description);
            }
        }
        return children;
    }

    private Optional<EClass> toEClassByOwnerId(IEditingContext editingContext, String ownerId) {
        return objectSearchService.getObject(editingContext, ownerId)
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast)
                .map(EObject::eClass);

    }

    private Optional<EClass> toEClassByReferenceKind(IEditingContext editingContext, String refKind) {
        Optional<Registry> optionalRegistry = this.getPackageRegistry(editingContext);
        if (optionalRegistry.isPresent() && !refKind.isBlank() && refKind.startsWith(SemanticKindConstants.PREFIX)) {
            var ePackageRegistry = optionalRegistry.get();
            String ePackageName = this.emfKindService.getEPackageName(refKind);
            String eClassName = this.emfKindService.getEClassName(refKind);
            Optional<EPackage> optionalPackage = this.emfKindService.findEPackage(ePackageRegistry, ePackageName);

            if (optionalPackage.isPresent()) {
                var pack = optionalPackage.get();
                EClassifier classifier = pack.getEClassifier(eClassName);
                if (classifier instanceof EClass eClass) {
                    return Optional.of(eClass);
                }
            }
        }
        return Optional.empty();
    }

    private List<EClass> getInstanciableTypesOf(IEditingContext editingContext, String referenceKind) {
        return this.toEClassByReferenceKind(editingContext, referenceKind).map(this::getInstanciableTypesOf).orElse(List.of());
    }

    private List<EClass> getInstanciableTypesOf(EClass type) {
        EPackage pack = type.getEPackage();
        return pack.getEClassifiers().stream()//
                .filter(EClass.class::isInstance)//
                .map(EClass.class::cast)//
                .filter(e -> !e.isAbstract() && !e.isInterface() && type.isSuperTypeOf(e))//
                .toList();
    }

    private Optional<Registry> getPackageRegistry(IEditingContext editingContext) {
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            Registry packageRegistry = emfEditingContext.getDomain().getResourceSet().getPackageRegistry();
            return Optional.of(packageRegistry);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Object> createRootObject(IEditingContext editingContext, UUID documentId, String domainId, String rootObjectCreationDescriptionId, String descriptionId) {
        // No creation at resource level is allowed in Papyrus
        return Optional.empty();
    }

    @Override
    public Optional<Object> createChild(IEditingContext editingContext, Object parent, String childCreationDescriptionId, String descriptionId) {
        var res = Optional.empty();
        Map<String, List<String>> parameters = new URLParser().getParameterValues(descriptionId);
        String kind = parameters.get(IRepresentationDescriptionIdProvider.KIND).get(0);
        if (MONO_REF_DESCRIPTION.equals(kind)) {
            res = this.createMonoReferenceChild(editingContext, parent, childCreationDescriptionId, descriptionId);
        } else if (MULTI_REF_DESCRIPTION.equals(kind)) {
            res = this.createMultiReferenceChild(editingContext, parent, childCreationDescriptionId, descriptionId);
        }
        return res;
    }

    private Optional<Object> createMultiReferenceChild(IEditingContext editingContext, Object parent, String childCreationDescriptionId, String descriptionId) {
        var optionalWidgetDescription = this.viewFormSearchService.findFormElementDescriptionById(editingContext, descriptionId).filter(MultiReferenceWidgetDescription.class::isInstance)
                .map(MultiReferenceWidgetDescription.class::cast);
        if (optionalWidgetDescription.isPresent()) {
            var reference = optionalWidgetDescription.get();
            var optionalView = this.getViewFromWidgetDescription(reference);
            if (optionalView.isPresent() && reference.getName() != null) {
                var createOperation = reference.getCreateElementOperation();
                VariableManager variableManager = this.createVariableManagerForElementCreation(editingContext, parent, childCreationDescriptionId, reference.getName());
                AQLInterpreter interpreter = this.interpreterProvider.createInterpreter(optionalView.get(), editingContext);
                OperationInterpreterViewSwitch operationInterpreterViewSwitch = new OperationInterpreterViewSwitch(variableManager, interpreter, this.editService, this.addExecutor,
                        this.clearExecutor);
                Optional<VariableManager> optionalVariableManager = operationInterpreterViewSwitch.doSwitch(createOperation.getBody().get(0));
                if (optionalVariableManager.isPresent()) {
                    return optionalVariableManager.get().get(VariableManager.SELF, Object.class);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Object> createMonoReferenceChild(IEditingContext editingContext, Object parent, String childCreationDescriptionId, String descriptionId) {
        var optionalWidgetDescription = this.viewFormSearchService.findFormElementDescriptionById(editingContext, descriptionId).filter(MonoReferenceWidgetDescription.class::isInstance)
                .map(MonoReferenceWidgetDescription.class::cast);
        if (optionalWidgetDescription.isPresent()) {
            var reference = optionalWidgetDescription.get();
            var optionalView = this.getViewFromWidgetDescription(reference);
            if (optionalView.isPresent() && reference.getName() != null) {
                var createOperation = reference.getCreateElementOperation();
                VariableManager variableManager = this.createVariableManagerForElementCreation(editingContext, parent, childCreationDescriptionId, reference.getName());
                AQLInterpreter interpreter = this.interpreterProvider.createInterpreter(optionalView.get(), editingContext);
                OperationInterpreterViewSwitch operationInterpreterViewSwitch = new OperationInterpreterViewSwitch(variableManager, interpreter, this.editService, this.addExecutor,
                        this.clearExecutor);
                Optional<VariableManager> optionalVariableManager = operationInterpreterViewSwitch.doSwitch(createOperation.getBody().get(0));
                if (optionalVariableManager.isPresent()) {
                    return optionalVariableManager.get().get(VariableManager.SELF, Object.class);
                }
            }
        }
        return Optional.empty();
    }

    private VariableManager createVariableManagerForElementCreation(IEditingContext editingContext, Object parent, String creationDescriptionId, String name) {
        var variableManager = new VariableManager();

        String[] parts = creationDescriptionId.split(CHILD_CREATION_DESCRIPTION_ID_SEPARATOR);
        String referenceName = parts[0];
        String childType = parts[1];

        variableManager.put("parent", parent);
        variableManager.put("kind", childType);
        variableManager.put("feature", referenceName);

        return variableManager;
    }

    private Optional<View> getViewFromWidgetDescription(EObject widgetDescription) {
        EObject container = widgetDescription;
        while (!(container instanceof View) && container != null) {
            container = container.eContainer();
        }
        if (container instanceof View view) {
            return Optional.of(view);
        }
        return Optional.empty();
    }
}

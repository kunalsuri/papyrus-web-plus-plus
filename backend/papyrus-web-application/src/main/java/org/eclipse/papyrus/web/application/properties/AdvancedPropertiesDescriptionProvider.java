/*******************************************************************************
 * Copyright (c) 2019, 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *     CEA - Adaptation for Papyrus Web
 *******************************************************************************/
package org.eclipse.papyrus.web.application.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory.Descriptor;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.emf.forms.EEnumIfDescriptionProvider;
import org.eclipse.sirius.components.emf.forms.EStringIfDescriptionProvider;
import org.eclipse.sirius.components.emf.forms.NumberIfDescriptionProvider;
import org.eclipse.sirius.components.emf.forms.api.IPropertiesValidationProvider;
import org.eclipse.sirius.components.emf.forms.api.IWidgetReadOnlyProvider;
import org.eclipse.sirius.components.emf.services.api.IEMFKindService;
import org.eclipse.sirius.components.emf.services.messages.IEMFMessageService;
import org.eclipse.sirius.components.forms.description.AbstractControlDescription;
import org.eclipse.sirius.components.forms.description.ForDescription;
import org.eclipse.sirius.components.forms.description.FormDescription;
import org.eclipse.sirius.components.forms.description.GroupDescription;
import org.eclipse.sirius.components.forms.description.PageDescription;
import org.eclipse.sirius.components.representations.GetOrCreateRandomIdProvider;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.uml2.uml.Element;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of the default property view to display an advanced view for UML properties, it disable live
 * validation and extends the default data type handled with the standard UML Type such as
 * TypesPackage.eINSTANCE.getBoolean().
 *
 * @author Arthur Daussy
 * @see EBooleanIfDescriptionProvider
 */

@Service
public class AdvancedPropertiesDescriptionProvider {

    public static final String ESTRUCTURAL_FEATURE = "eStructuralFeature";

    private final List<Descriptor> composedAdapterFactoryDescriptors;

    private final IPropertiesValidationProvider propertiesValidationProvider;

    private final IEMFMessageService emfMessageService;

    private final Function<VariableManager, String> semanticTargetIdProvider;

    private final IEMFKindService emfKindService;

    private final IFeedbackMessageService feedbackMessageService;

    private final IIdentityService identityService;

    private final IWidgetReadOnlyProvider widgetReadOnlyProvider;

    private final ILabelService labelService;

    private final IObjectSearchService objectSearchService;

    // CHECKSTYLE:OFF Injected parameters
    public AdvancedPropertiesDescriptionProvider(IIdentityService identityService, List<Descriptor> composedAdapterFactoryDescriptors, IEMFMessageService emfMessageService,
            IFeedbackMessageService feedbackMessageService, IEMFKindService emfKindService, IWidgetReadOnlyProvider widgetReadOnlyProvider, ILabelService labelService,
            IObjectSearchService objectSearchService) {
        this.identityService = Objects.requireNonNull(identityService);
        // CHECKSTYLE:ON Injected parameters
        this.composedAdapterFactoryDescriptors = Objects.requireNonNull(composedAdapterFactoryDescriptors);
        this.widgetReadOnlyProvider = Objects.requireNonNull(widgetReadOnlyProvider);
        this.labelService = Objects.requireNonNull(labelService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.propertiesValidationProvider = new IPropertiesValidationProvider.NoOp(); // Unplug live validation
        // validation
        this.emfMessageService = Objects.requireNonNull(emfMessageService);
        this.semanticTargetIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class).map(identityService::getId).orElse(null);
        this.emfKindService = Objects.requireNonNull(emfKindService);
        this.feedbackMessageService = Objects.requireNonNull(feedbackMessageService);
    }

    public FormDescription getFormDescription() {
        List<GroupDescription> groupDescriptions = new ArrayList<>();
        GroupDescription groupDescription = this.getGroupDescription();

        groupDescriptions.add(groupDescription);

        List<PageDescription> pageDescriptions = new ArrayList<>();
        PageDescription firstPageDescription = this.getPageDescription(groupDescriptions);
        pageDescriptions.add(firstPageDescription);

        Function<VariableManager, String> labelProvider = variableManager -> "Properties";

        Function<VariableManager, String> targetObjectIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(this.identityService::getId)
                .orElse(null);

        return FormDescription.newFormDescription(UUID.nameUUIDFromBytes("UMLAdvancedPropertyViewForm".getBytes()).toString())
                .label("Default form description")
                .idProvider(new GetOrCreateRandomIdProvider())
                .labelProvider(labelProvider)
                .targetObjectIdProvider(targetObjectIdProvider)
                .canCreatePredicate(variableManager -> this.canCreatePage(variableManager))
                .pageDescriptions(pageDescriptions)
                .iconURLsProvider(variableManager -> List.of())
                .build();
    }

    private boolean canCreatePage(VariableManager variableManager) {
        var optionalSelf = variableManager.get(VariableManager.SELF, Object.class);
        if (optionalSelf.isPresent()) {
            return optionalSelf.get() instanceof Element;
        }
        return false;
    }

    private PageDescription getPageDescription(List<GroupDescription> groupDescriptions) {
        Function<VariableManager, String> idProvider = variableManager -> {
            var optionalSelf = variableManager.get(VariableManager.SELF, Object.class);
            if (optionalSelf.isPresent()) {
                Object self = optionalSelf.get();
                return this.identityService.getId(self);
            }
            return UUID.randomUUID().toString();
        };

        Function<VariableManager, String> labelProvider = variableManager -> "Advanced";

        return PageDescription.newPageDescription("UMLAdvancedPropertyViewPage")
                .idProvider(idProvider)
                .labelProvider(labelProvider)
                .semanticElementsProvider(variableManager -> Collections.singletonList(variableManager.getVariables().get(VariableManager.SELF)))
                .groupDescriptions(groupDescriptions)
                .canCreatePredicate(variableManager -> this.canCreatePage(variableManager))
                .build();
    }

    private GroupDescription getGroupDescription() {
        List<AbstractControlDescription> controlDescriptions = new ArrayList<>();

        Function<VariableManager, List<?>> iterableProvider = variableManager -> {
            List<Object> objects = new ArrayList<>();

            Object self = variableManager.getVariables().get(VariableManager.SELF);
            if (self instanceof EObject) {
                EObject eObject = (EObject) self;

                List<AdapterFactory> adapterFactories = this.composedAdapterFactoryDescriptors.stream()
                        .map(Descriptor::createAdapterFactory)
                        .toList();
                var composedAdapterFactory = new ComposedAdapterFactory(adapterFactories);

                List<IItemPropertyDescriptor> propertyDescriptors = Optional.ofNullable(composedAdapterFactory.adapt(eObject, IItemPropertySource.class))
                        .filter(IItemPropertySource.class::isInstance)
                        .map(IItemPropertySource.class::cast)
                        .map(iItemPropertySource -> iItemPropertySource.getPropertyDescriptors(eObject))
                        .orElse(new ArrayList<>());

                propertyDescriptors.stream() //
                        .map(propertyDescriptor -> propertyDescriptor.getFeature(eObject))
                        .filter(EStructuralFeature.class::isInstance)
                        .map(EStructuralFeature.class::cast)
                        // Prevents EReference targeting EModelElements and EObject.
                        // (https://github.com/PapyrusSirius/papyrus-web/issues/58)
                        // * It can return thousands of elements making the UI really slow
                        // * On some candidates an id cannot be computed (EPackage) causing NPE (nevertheless this case
                        // should be fixed in Sirius Component)
                        // https://github.com/eclipse-sirius/sirius-components/issues/1433
                        .filter(feature -> feature.getEType() != EcorePackage.eINSTANCE.getEObject() && feature.getEType() != EcorePackage.eINSTANCE.getEModelElement())
                        .forEach(objects::add);

                composedAdapterFactory.dispose();
            }
            return objects;
        };

        List<AbstractControlDescription> ifDescriptions = new ArrayList<>();
        ifDescriptions.addAll(new EStringIfDescriptionProvider(this.identityService, this.propertiesValidationProvider, this.widgetReadOnlyProvider).getIfDescriptions());
        ifDescriptions.addAll(new EBooleanIfDescriptionProvider(this.identityService, this.propertiesValidationProvider, this.widgetReadOnlyProvider).getIfDescriptions());
        ifDescriptions.addAll(new EEnumIfDescriptionProvider(this.identityService, this.propertiesValidationProvider, this.widgetReadOnlyProvider).getIfDescriptions());

        ifDescriptions.add(new NonDerivedNonContainmentReferenceIfDescriptionProvider(this.labelService, this.semanticTargetIdProvider, this.propertiesValidationProvider,
                this.feedbackMessageService, this.emfKindService, this.objectSearchService, this.identityService).getIfDescription());

        var numericDataTypes = List.of(
                EcorePackage.Literals.EINT,
                EcorePackage.Literals.EINTEGER_OBJECT,
                EcorePackage.Literals.EDOUBLE,
                EcorePackage.Literals.EDOUBLE_OBJECT,
                EcorePackage.Literals.EFLOAT,
                EcorePackage.Literals.EFLOAT_OBJECT,
                EcorePackage.Literals.ELONG,
                EcorePackage.Literals.ELONG_OBJECT,
                EcorePackage.Literals.ESHORT,
                EcorePackage.Literals.ESHORT_OBJECT);
        for (var dataType : numericDataTypes) {
            ifDescriptions.add(new NumberIfDescriptionProvider(dataType, this.propertiesValidationProvider, this.emfMessageService, this.semanticTargetIdProvider,
                    this.widgetReadOnlyProvider)
                            .getIfDescription());
        }

        ForDescription forDescription = ForDescription.newForDescription("forId")
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .iterator(ESTRUCTURAL_FEATURE)
                .iterableProvider(iterableProvider)
                .controlDescriptions(ifDescriptions)
                .build();

        controlDescriptions.add(forDescription);

        return GroupDescription.newGroupDescription("groupId")
                .idProvider(variableManager -> "Core Properties")
                .labelProvider(variableManager -> "Core Properties")
                .semanticElementsProvider(variableManager -> Collections.singletonList(variableManager.getVariables().get(VariableManager.SELF)))
                .controlDescriptions(controlDescriptions)
                .build();
    }

}

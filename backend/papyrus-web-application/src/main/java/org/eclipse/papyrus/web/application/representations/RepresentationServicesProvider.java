/*****************************************************************************
 * Copyright (c) 2022, 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issues 210, 218
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.papyrus.uml.domain.services.internal.helpers.UMLService;
import org.eclipse.papyrus.uml.domain.services.properties.PropertiesMultiplicityServices;
import org.eclipse.papyrus.uml.domain.services.properties.PropertiesProfileDefinitionServices;
import org.eclipse.papyrus.uml.domain.services.properties.PropertiesStereotypeApplicationServices;
import org.eclipse.papyrus.uml.domain.services.properties.PropertiesUMLServices;
import org.eclipse.papyrus.uml.domain.services.properties.PropertiesValueSpecificationServices;
import org.eclipse.papyrus.web.application.properties.UMLPropertiesConfigurer;
import org.eclipse.papyrus.web.application.representations.aqlservices.DebugService;
import org.eclipse.papyrus.web.application.representations.aqlservices.activity.ActivityDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.clazz.ClassDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.communication.CommunicationDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.component.ComponentDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.composite.CompositeStructureDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.deployment.DeploymentDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.pakage.PackageDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.profile.ProfileDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesAnnotationServices;
import org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesCrudServicesWrapper;
import org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesHelpContentServices;
import org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesImageServicesWrapper;
import org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesMemberEndServicesWrapper;
import org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesProfileServices;
import org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesReferenceTypeServices;
import org.eclipse.papyrus.web.application.representations.aqlservices.scope.ReachableElementsServices;
import org.eclipse.papyrus.web.application.representations.aqlservices.statemachine.StateMachineDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.tables.UMLCommentTableServices;
import org.eclipse.papyrus.web.application.representations.aqlservices.useCase.UseCaseDiagramService;
import org.eclipse.papyrus.web.application.representations.uml.ADDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.CDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.CODDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.CSDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.DDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.PADDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.SMDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UCDDiagramDescriptionBuilder;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.sirius.components.view.table.TableDescription;
import org.springframework.context.annotation.Configuration;

/**
 * Registers all the Representation Service classes for Papyrus Web representations.
 *
 * @author Arthur Duassy
 */
@Configuration
public class RepresentationServicesProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        return view.getDescriptions().stream()
                .flatMap(representationDescription -> this.getRepresentationServicesClass(representationDescription).stream()) //
                .collect(Collectors.toList());
    }

    private List<Class<?>> getRepresentationServicesClass(RepresentationDescription representationDescription) {
        List<Class<?>> services = new ArrayList<>();
        if (representationDescription instanceof DiagramDescription) {
            this.registerDiagramServices(representationDescription, services);
        } else if (representationDescription instanceof org.eclipse.sirius.components.view.form.FormDescription formDesc) {
            this.registerFormServices(formDesc, services);
        } else if (representationDescription instanceof TableDescription) {
            this.registerTableServices(representationDescription, services);
        }
        return services;
    }

    private void registerDiagramServices(RepresentationDescription representationDescription, List<Class<?>> services) {
        String name = representationDescription.getName();
        if (name != null) {
            // Generic services
            services.add(UMLService.class);
            services.add(DebugService.class);
            services.add(PropertiesAnnotationServices.class);
            String repName = representationDescription.getName();
            // Handle both in memory and serialized version
            if (repName != null) {
                if (repName.startsWith(CSDDiagramDescriptionBuilder.CSD_REP_NAME)) {
                    services.add(CompositeStructureDiagramService.class);
                } else if (repName.startsWith(SMDDiagramDescriptionBuilder.SMD_REP_NAME)) {
                    services.add(StateMachineDiagramService.class);
                } else if (repName.startsWith(PADDiagramDescriptionBuilder.PD_REP_NAME)) {
                    services.add(PackageDiagramService.class);
                } else if (repName.startsWith(CDDiagramDescriptionBuilder.CD_REP_NAME)) {
                    services.add(ClassDiagramService.class);
                } else if (repName.startsWith(UCDDiagramDescriptionBuilder.UCD_REP_NAME)) {
                    services.add(UseCaseDiagramService.class);
                } else if (repName.startsWith(PRDDiagramDescriptionBuilder.PRD_REP_NAME)) {
                    services.add(ProfileDiagramService.class);
                } else if (repName.startsWith(CODDiagramDescriptionBuilder.COD_REP_NAME)) {
                    services.add(CommunicationDiagramService.class);
                } else if (repName.startsWith(ADDiagramDescriptionBuilder.AD_REP_NAME)) {
                    services.add(ActivityDiagramService.class);
                } else if (repName.startsWith(CPDDiagramDescriptionBuilder.CPD_REP_NAME)) {
                    services.add(ComponentDiagramService.class);
                } else if (repName.startsWith(DDDiagramDescriptionBuilder.DD_REP_NAME)) {
                    services.add(DeploymentDiagramService.class);
                }
            }
        }
    }

    private void registerFormServices(org.eclipse.sirius.components.view.form.FormDescription formDesc, List<Class<?>> services) {
        String name = formDesc.getName();
        if (name != null && name.startsWith(UMLPropertiesConfigurer.UML_DETAIL_VIEW_NAME)) {
            services.add(PropertiesCrudServicesWrapper.class);
            services.add(PropertiesImageServicesWrapper.class);
            services.add(PropertiesMemberEndServicesWrapper.class);
            services.add(PropertiesMultiplicityServices.class);
            services.add(PropertiesProfileDefinitionServices.class);
            services.add(PropertiesUMLServices.class);
            services.add(PropertiesValueSpecificationServices.class);
            services.add(DebugService.class);
            services.add(PropertiesHelpContentServices.class);
            services.add(PropertiesProfileServices.class);
            services.add(ReachableElementsServices.class);
            services.add(PropertiesStereotypeApplicationServices.class);
            services.add(PropertiesReferenceTypeServices.class);
            services.add(PropertiesAnnotationServices.class);
        }
    }

    private void registerTableServices(RepresentationDescription representationDescription, List<Class<?>> services) {
        services.add(ReachableElementsServices.class);
        services.add(PropertiesStereotypeApplicationServices.class);
        services.add(UMLCommentTableServices.class);
    }
}

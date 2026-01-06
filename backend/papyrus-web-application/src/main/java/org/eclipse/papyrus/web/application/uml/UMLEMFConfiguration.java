/*******************************************************************************
 * Copyright (c) 2022, 2026 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.application.uml;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.provider.EcoreItemProviderAdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.papyrus.web.application.pathmap.PathmapResourceFactory;
import org.eclipse.papyrus.web.application.pathmap.services.api.IStaticPathmapResourceRegistry;
import org.eclipse.papyrus.web.domain.boundedcontext.profile.service.api.IProfileSearchService;
import org.eclipse.uml2.types.TypesPackage;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.edit.providers.UMLItemProviderAdapterFactory;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.eclipse.uml2.uml.profile.standard.StandardPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the EMF support for Papyrus Web.
 *
 * @author lfasani
 */
@Configuration
public class UMLEMFConfiguration {

    @Bean
    public ComposedAdapterFactory.Descriptor umlAdapterFactory() {
        return UMLItemProviderAdapterFactory::new;
    }

    @Bean
    public ComposedAdapterFactory.Descriptor ecoreAdapterFactory() {
        return EcoreItemProviderAdapterFactory::new;
    }

    @Bean
    public EPackage ecoreEPackage() {
        return EcorePackage.eINSTANCE;
    }

    @Bean
    public EPackage umlEPackage() {
        return UMLPackage.eINSTANCE;
    }

    @Bean
    public EPackage standardEPackage() {
        return StandardPackage.eINSTANCE;
    }

    @Bean
    public EPackage typesEPackage() {
        return TypesPackage.eINSTANCE;
    }

    @Bean
    public Resource.Factory.Registry factoryRegistry(IStaticPathmapResourceRegistry pathmapResourceRegistry, IProfileSearchService profileSearchService) {
        Registry globalFactoryRegistryInstance = Resource.Factory.Registry.INSTANCE;

        // initialize the registry from the global
        Registry factoryRegistry = new ResourceFactoryRegistryImpl();
        Map<String, Object> protocolToFactoryMap = factoryRegistry.getProtocolToFactoryMap();
        globalFactoryRegistryInstance.getProtocolToFactoryMap().forEach((key, value) -> protocolToFactoryMap.put(key, value));
        Map<String, Object> extensionToFactoryMap = factoryRegistry.getExtensionToFactoryMap();
        globalFactoryRegistryInstance.getExtensionToFactoryMap().forEach((key, value) -> extensionToFactoryMap.put(key, value));
        Map<String, Object> contentTypeToFactoryMap = factoryRegistry.getContentTypeToFactoryMap();
        globalFactoryRegistryInstance.getContentTypeToFactoryMap().forEach((key, value) -> contentTypeToFactoryMap.put(key, value));

        // Add pathmap scheme factory
        Resource.Factory pathmapFactory = new PathmapResourceFactory(pathmapResourceRegistry, globalFactoryRegistryInstance, profileSearchService);
        factoryRegistry.getProtocolToFactoryMap().put(IStaticPathmapResourceRegistry.PROTOCOL_PATHMAP, pathmapFactory);

        // Add factory associated to uml extension
        factoryRegistry.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, new UMLResourceFactoryImpl());

        return factoryRegistry;
    }
}

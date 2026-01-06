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
package org.eclipse.papyrus.web.application.representations.nodes;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.provider.PapyrusCustomNodesItemProviderAdapterFactory;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.provider.customimpl.PapyrusCustomNodesItemProviderAdapterFactoryCustomImpl;
import org.eclipse.sirius.components.emf.configuration.ChildExtenderProvider;
import org.eclipse.sirius.components.view.diagram.DiagramPackage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the customnodes DSL extension for the papyrus custom nodes.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Configuration
public class PapyrusCustomNodeEMFConfiguration {

    /**
     * Provide custom {@link EPackage}.
     *
     * @return custom {@link EPackage}
     */
    @Bean
    public EPackage papyrusCustomNodeEPackage() {
        return PapyrusCustomNodesPackage.eINSTANCE;
    }

    /**
     * Provide custom {@link AdapterFactory}.
     *
     * @return custom {@link AdapterFactory}
     */
    @Bean
    public ComposedAdapterFactory.Descriptor papyrusCustomNodeAdapterFactory() {
        return PapyrusCustomNodesItemProviderAdapterFactoryCustomImpl::new;
    }

    /**
     * Provide custom {@link ChildExtenderProvider}.
     *
     * @return custom {@link ChildExtenderProvider}
     */
    @Bean
    public ChildExtenderProvider papyrusCustomNodeChildExtenderProvider() {
        return new ChildExtenderProvider(DiagramPackage.eNS_URI, PapyrusCustomNodesItemProviderAdapterFactory.DiagramChildCreationExtender::new);
    }
}

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
package org.eclipse.papyrus.web.custom.widgets;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PapyrusWidgetsPackage;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.provider.PapyrusWidgetsItemProviderAdapterFactory;
import org.eclipse.papyrus.web.custom.widgets.provider.customimpl.PapyrusWidgetsItemProviderAdapterFactoryCustomImpl;
import org.eclipse.sirius.components.emf.configuration.ChildExtenderProvider;
import org.eclipse.sirius.components.view.form.FormPackage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the View DSL extension for the papyrus custom widgets.
 *
 * @author Arthur Daussy
 */
@Configuration
public class PapyrusWidgetsEMFConfiguration {
    @Bean
    public EPackage papyrusWidgetsEPackage() {
        return PapyrusWidgetsPackage.eINSTANCE;
    }

    @Bean
    public ComposedAdapterFactory.Descriptor papyrusWidgetsAdapterFactory() {
        return PapyrusWidgetsItemProviderAdapterFactoryCustomImpl::new;
    }

    @Bean
    public ChildExtenderProvider papyrusWidgetsChildExtenderProvider() {
        return new ChildExtenderProvider(FormPackage.eNS_URI, PapyrusWidgetsItemProviderAdapterFactory.FormChildCreationExtender::new);
    }
}

/*******************************************************************************
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
package org.eclipse.papyrus.web.application.pathmap;

import java.util.List;

import org.eclipse.papyrus.web.application.pathmap.services.StaticPathmapResourceRegistry;
import org.eclipse.papyrus.web.application.pathmap.services.api.IPathMapProvider;
import org.eclipse.papyrus.web.application.pathmap.services.api.IStaticPathmapResourceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The configuration used to provide the services related to the PathMaps.
 *
 * @author Arthur Daussy
 */
@Configuration
public class PathMapConfiguration {

    @Bean
    public IStaticPathmapResourceRegistry pathmapDescriptionService(List<IPathMapProvider> pathmapProviders) {
        StaticPathmapResourceRegistry registry = new StaticPathmapResourceRegistry();
        pathmapProviders.stream().flatMap(provider -> provider.getPathmaps().stream()).forEach(pathMap -> {
            registry.add(pathMap.getResourceURIOpaquePart(), pathMap.getLocalPath());
        });
        return registry;
    }
}

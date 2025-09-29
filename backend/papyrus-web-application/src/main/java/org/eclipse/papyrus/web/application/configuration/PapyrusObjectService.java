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
package org.eclipse.papyrus.web.application.configuration;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.papyrus.web.graphics.services.api.IImageOverrideService;
import org.eclipse.sirius.components.core.api.IDefaultLabelService;
import org.eclipse.sirius.components.core.api.ILabelServiceDelegate;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.uml2.uml.Element;
import org.springframework.stereotype.Service;

/**
 * Specialized version of {@link ILabelServiceDelegate} for Papyrus application.
 *
 * @author Arthur Daussy
 */
@Service
public class PapyrusObjectService implements ILabelServiceDelegate {

    private final IDefaultLabelService defaultLabelService;
    private final List<IImageOverrideService> imageOverriders;

    public PapyrusObjectService(IDefaultLabelService defaultLabelService, List<IImageOverrideService> imageOverriders) {
        this.defaultLabelService = Objects.requireNonNull(defaultLabelService);
        this.imageOverriders = Objects.requireNonNull(imageOverriders);
    }

    @Override
    public boolean canHandle(Object object) {
        return object instanceof Element;
    }

    @Override
    public StyledString getStyledLabel(Object object) {
        return defaultLabelService.getStyledLabel(object);
    }

    @Override
    public List<String> getImagePaths(Object object) {
        List<String> images = defaultLabelService.getImagePaths(object);

        return images.stream().map(image -> this.imageOverriders.stream().map(imgOverrider -> imgOverrider.getOverrideImage(image)) //
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(image)
        ).toList();
    }

}

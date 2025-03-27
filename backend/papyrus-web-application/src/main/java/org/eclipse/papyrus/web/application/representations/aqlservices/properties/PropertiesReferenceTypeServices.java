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
package org.eclipse.papyrus.web.application.representations.aqlservices.properties;

import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.web.application.properties.UMLRedefinedTypeService;

/**
 * Service that handles reference types services.
 *
 * @author Jerome Gout
 */
public class PropertiesReferenceTypeServices {

    private final UMLRedefinedTypeService redefinedTypeService;

    public PropertiesReferenceTypeServices(UMLRedefinedTypeService redefinedTypeService) {
        this.redefinedTypeService = Objects.requireNonNull(redefinedTypeService);
    }

    /**
     * Return the qualified name of the type of the given feature of the given element.
     *
     * @param self
     *            the current selected element owning the feature
     * @param featureName
     *            the name of the feature
     * @return the qualified name of the feature type.
     */
    public String getFeatureTypeQualifiedName(EObject self, String featureName) {
        return this.redefinedTypeService.getFeatureTypeQualifiedName(self, featureName);
    }
}

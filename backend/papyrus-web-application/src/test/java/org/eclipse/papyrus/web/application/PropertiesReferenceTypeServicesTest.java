/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.papyrus.web.application.properties.UMLRedefinedTypeService;
import org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesReferenceTypeServices;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.papyrus.web.tests.utils.UMLTestHelper;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.DurationConstraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link PropertiesReferenceTypeServices} service class.
 *
 * @author Jerome Gout
 */
class PropertiesReferenceTypeServicesTest {

    private PropertiesReferenceTypeServices propertiesService;

    /**
     * Helper to create UML elements
     */
    private final UMLTestHelper umlHelper = new UMLTestHelper();

    @BeforeEach
    public void setUp() {
        this.propertiesService = new PropertiesReferenceTypeServices(new UMLRedefinedTypeService(new MockLogger()));
    }

    /**
     * Test method for
     * {@link org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesReferenceTypeServices#getFeatureTypeQualifiedName(org.eclipse.emf.ecore.EObject, java.lang.String)}.
     */
    @Test
    void testGetFeatureTypeQualifiedName() {
        Activity activity = this.umlHelper.create(Activity.class);
        assertEquals("uml::Variable", this.propertiesService.getFeatureTypeQualifiedName(activity, "variable"));
    }

    @Test
    void testGetFeatureTypeQualifiedNameRedefined() {
        DurationConstraint durationConstraint = this.umlHelper.create(DurationConstraint.class);
        assertEquals("uml::DurationInterval", this.propertiesService.getFeatureTypeQualifiedName(durationConstraint, "specification"));
    }
}

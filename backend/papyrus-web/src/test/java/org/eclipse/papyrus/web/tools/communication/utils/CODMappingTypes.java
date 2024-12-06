/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo, Artal Technologies.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.tools.communication.utils;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * The mapping types for the Communication Diagram.
 * <p>
 * This class can be used to retrieve both node mapping types and edge mapping types.
 * </p>
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public final class CODMappingTypes {

    private static final String COD_PREFIX = "COD_";

    private CODMappingTypes() {
        // private to prevent instantiation.
    }

    public static String getMappingType(EClass eClass) {
        String result = COD_PREFIX + eClass.getName();
        if (UMLPackage.eINSTANCE.getMessage().isSuperTypeOf(eClass)) {
            result = COD_PREFIX + eClass.getName() + "_DomainEdge";
        }
        return result;
    }

}

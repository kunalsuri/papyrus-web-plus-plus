/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.tools.profile.utils;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * The mapping types for the Profile Diagram.
 * <p>
 * This class can be used to retrieve both node mapping types and edge mapping types.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public final class PRDMappingTypes {

    /**
     * The mapping name for metaclasses on the diagram.
     * <p>
     * This mapping cannot be computed by {@link #getMappingType(EClass)} because it overlaps with the {@link Class}
     * mapping.
     * </p>
     */
    public static final String PRD_METACLASS = "PRD_Metaclass";

    /**
     * The mapping name for metaclasses contained in other nodes.
     * <p>
     * This mapping cannot be computed by {@link #getMappingTypeAsSubNode(EClass)} because it overlaps with the
     * {@link Class} mapping.
     * </p>
     */
    public static final String PRD_METACLASS_SHARED = "PRD_Metaclass_SHARED";

    private static final String PRD_PREFIX = "PRD_";

    private PRDMappingTypes() {
        // private to prevent instantiation.
    }

    public static String getMappingType(EClass eClass) {
        String result = PRD_PREFIX + eClass.getName();
        if (UMLPackage.eINSTANCE.getPackage().equals(eClass) || UMLPackage.eINSTANCE.getProfile().equals(eClass)) {
            result = result.concat("_Holder");
        }
        if (UMLPackage.eINSTANCE.getRelationship().isSuperTypeOf(eClass)) {
            result = PRD_PREFIX + eClass.getName() + "_DomainEdge";
        }
        return result;
    }

    public static String getMappingTypeAsSubNode(EClass eClass) {
        String result = PRD_PREFIX + eClass.getName() + "_SHARED";
        if (UMLPackage.eINSTANCE.getPackage().equals(eClass) || UMLPackage.eINSTANCE.getProfile().equals(eClass)) {
            result = result.concat("_Holder");
        }
        if (UMLPackage.eINSTANCE.getEnumerationLiteral().isSuperTypeOf(eClass)) {
            // Enumeration literals aren't suffixed with "_SHARED" even if they are sub-nodes, because they aren't
            // shared
            result = getMappingType(eClass);
        }
        return result;
    }

}

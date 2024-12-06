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
package org.eclipse.papyrus.web.tools.usecase.utils;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * The mapping types for the Use Case Diagram.
 * <p>
 * This class can be used to retrieve both node mapping types and edge mapping types.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public final class UCDMappingTypes {

    private static final String UCD_PREFIX = "UCD_";

    private UCDMappingTypes() {
        // private to prevent instantiation.
    }

    public static String getMappingType(EClass eClass) {
        String suffix = switch (eClass.getName()) {
            case "Activity", "Class", "Component", "Interaction", "Package", "StateMachine" -> "_Holder";
            default -> "";
        };
        String result = UCD_PREFIX + eClass.getName() + suffix;
        if (UMLPackage.eINSTANCE.getRelationship().isSuperTypeOf(eClass)) {
            result = UCD_PREFIX + eClass.getName() + "_DomainEdge";
        }
        return result;
    }

    public static String getMappingTypeAsSubNode(EClass eClass) {
        String suffix = switch (eClass.getName()) {
            case "Activity", "Class", "Component", "Interaction", "Package", "StateMachine" -> "_Holder";
            default -> "";
        };
        return UCD_PREFIX + eClass.getName() + "_SHARED" + suffix;
    }

}

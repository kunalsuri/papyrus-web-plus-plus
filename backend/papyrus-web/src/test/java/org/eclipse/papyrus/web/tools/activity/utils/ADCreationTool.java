/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.tools.activity.utils;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * A creation tool for Activity Diagram tests.
 * <p>
 * Instances of this class are built from a {@code toolSection} name (see {@link ADToolSections}) and the {@code eClass}
 * of the element to create.
 *
 * @see ADToolSections
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public final class ADCreationTool extends org.eclipse.papyrus.web.tools.utils.CreationTool {

    /**
     * Build the Use Case Diagram creation tool from the provided {@code toolSection} and {@code eClass}.
     *
     * @param toolSection
     *            the name of the tool section containing the creation tool
     * @param eClass
     *            the type of the element to create
     */
    public ADCreationTool(String toolSection, EClass eClass) {
        super(toolSection, eClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String computeToolName(EClass eClass) {
        String result = super.computeToolName(eClass);
        // ExpansionNode can be created with both New Input ExpansionNode and New Output ExpansionNode tools. We choose
        // to create only input ExpansionNode to run the tests.
        if (UMLPackage.eINSTANCE.getExpansionNode().isSuperTypeOf(eClass)) {
            result = "New Input Expansion Node";
        }
        return result;
    }

}

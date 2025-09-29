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
package org.eclipse.papyrus.web.tools.checker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.IDiagramElement;

/**
 * Utility class to check that a semantic node has not been removed in the semantic model after semantic deletion.
 * <p>
 * It also validates that the semantic model contains the appropriate number of elements after the creation.
 * </p>
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class NodeSemanticDeletionSemanticChecker extends DeletionSemanticChecker {

    /**
     * Initializes the checker with the provided parameters.
     *
     * @param objectSearchService
     *            the object service used to retrieve and compute identifiers
     * @param editingContextSupplier
     *            a supplier to access and reload the editing context
     * @param oldOwnerSupplier
     *            a supplier to access and reload the previous owner of the checked element
     * @param containmentFeature
     *            the expected containment feature of the checked element
     */
    public NodeSemanticDeletionSemanticChecker(IObjectSearchService objectSearchService,
            Supplier<IEditingContext> editingContextSupplier, Supplier<EObject> oldOwnerSupplier,
            EReference containmentFeature) {
        super(objectSearchService, editingContextSupplier, oldOwnerSupplier, containmentFeature);
    }

    @Override
    public void validateRepresentationElement(IDiagramElement element) {
        assertThat(this.getContainmentFeatureValue()).noneMatch(containedElement -> Objects.equals(containedElement, element.getId()));
    }
}

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
package org.eclipse.papyrus.web.tools.checker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Utility class to check that a semantic element has been removed or not in the semantic model after semantic or
 * graphical deletion.
 * <p>
 * It also validates that the semantic model contains the appropriate number of elements after the creation.
 * </p>
 *
 * @author <a href="mailto:jessy.mallet@obeosoft.com">Jessy Mallet</a>
 */
public abstract class DeletionSemanticChecker implements Checker {

    protected Supplier<IEditingContext> editingContextSupplier;

    protected Supplier<EObject> oldOwnerSupplier;
    
    private final IObjectSearchService objectSearchService;

    private final EReference containmentFeature;

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
    public DeletionSemanticChecker(IObjectSearchService objectSearchService,
            Supplier<IEditingContext> editingContextSupplier, Supplier<EObject> oldOwnerSupplier,
            EReference containmentFeature) {
        this.objectSearchService = objectSearchService;
        this.editingContextSupplier = editingContextSupplier;
        this.oldOwnerSupplier = oldOwnerSupplier;
        this.containmentFeature = containmentFeature;
    }

    protected final Collection<?> getContainmentFeatureValue() {
        final EReference owningFeature = this.containmentFeature;
        if (!owningFeature.isMany()) {
            throw new UnsupportedOperationException("The case where the owning feature is not multi-valued is not yet implemented");
        }
        return (Collection<?>) this.oldOwnerSupplier.get().eGet(this.containmentFeature);
    }

    protected EObject getSemanticElement(IDiagramElement diagramElement) {
        String semanticElementId = null;
        if (diagramElement instanceof Node node) {
            semanticElementId = node.getTargetObjectId();
        } else {
            fail();
        }
        Optional<Object> optSemanticElement = this.objectSearchService.getObject(this.editingContextSupplier.get(),
                semanticElementId);
        assertThat(optSemanticElement).isPresent();
        assertThat(optSemanticElement.get()).isInstanceOf(EObject.class);
        EObject semanticElement = (EObject) optSemanticElement.get();
        return semanticElement;
    }

}

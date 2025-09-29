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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Utility class to check that a semantic element has been created in the semantic model.
 * <p>
 * This checker validates that the expected semantic element has been created in the expected semantic container. It
 * also validates that the semantic model contains the appropriate number of elements after the creation.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public abstract class CreationSemanticChecker implements Checker {

    protected Supplier<IEditingContext> editingContextSupplier;

    private final IObjectSearchService objectSearchService;

    private final IIdentityService identityService;

    private final EClass expectedType;

    private final Supplier<EObject> expectedOwnerSupplier;

    private final EReference containmentFeature;

    /**
     * Initializes the checker with the provided parameters.
     *
     * @param objectSearchService
     *            the object service used to retrieve objects and compute identifiers
     * @param  identityService
     *            the identity service
     * @param editingContextSupplier
     *            a supplier to access and reload the editing context
     * @param expectedType
     *            the expected type of the checked element
     * @param expectedOwnerSupplier
     *            a supplier to access and reload the expected semantic owner of the checked element
     * @param containmentFeature
     *            the expected containment feature of the checked element
     */
    public CreationSemanticChecker(IObjectSearchService objectSearchService, IIdentityService identityService,
            Supplier<IEditingContext> editingContextSupplier, EClass expectedType,
            Supplier<EObject> expectedOwnerSupplier,
            EReference containmentFeature) {
        this.objectSearchService = objectSearchService;
        this.identityService = identityService;
        this.editingContextSupplier = editingContextSupplier;
        this.expectedType = expectedType;
        this.expectedOwnerSupplier = expectedOwnerSupplier;
        this.containmentFeature = containmentFeature;
    }

    @Override
    public void validateRepresentationElement(IDiagramElement element) {
        EObject semanticElement = this.getSemanticElement(element);
        this.validateSemanticElementInstance(semanticElement);
        this.validateSemanticOwner(semanticElement);
    }

    protected void validateSemanticElementInstance(EObject element) {
        assertThat(this.expectedType.isInstance(element)).isTrue();
    }

    protected void validateSemanticOwner(final EObject semanticElement) {
        EObject expectedOwner = this.expectedOwnerSupplier.get();
        // Need to compare IDs because EObject instances are different
        assertThat(this.getContainmentFeatureValue().stream().map(this.identityService::getId)).as(
                        "The semantic owner doesn't contain the checked element")
                .anyMatch(id -> Objects.equals(id, this.identityService.getId(semanticElement)));
        assertThat(this.identityService.getId(semanticElement.eContainer())).as(
                        "The created element is not contained by the expected owner")
                .isEqualTo(this.identityService.getId(expectedOwner));
    }

    protected EObject getSemanticElement(IDiagramElement diagramElement) {
        String semanticElementId = null;
        if (diagramElement instanceof Node node) {
            semanticElementId = node.getTargetObjectId();
        } else if (diagramElement instanceof Edge edge) {
            semanticElementId = edge.getTargetObjectId();
        } else {
            fail("Unsupported type of IDiagramElement: " + diagramElement);
        }
        Optional<Object> optSemanticElement = this.objectSearchService.getObject(this.editingContextSupplier.get(),
                semanticElementId);
        assertThat(optSemanticElement).isPresent();
        assertThat(optSemanticElement.get()).isInstanceOf(EObject.class);
        EObject semanticElement = (EObject) optSemanticElement.get();
        return semanticElement;
    }

    protected final Collection<?> getContainmentFeatureValue() {
        final EReference owningFeature = this.containmentFeature;
        if (!owningFeature.isMany()) {
            throw new UnsupportedOperationException("The case where the owning feature is not multi-valued is not yet implemented");
        }
        return (Collection<?>) this.expectedOwnerSupplier.get().eGet(this.containmentFeature);
    }

}

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
package org.eclipse.papyrus.web.tools.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.representations.IRepresentation;
import org.junit.jupiter.api.BeforeEach;

/**
 * Utility class to help the definition of diagram creation tests.
 * <p>
 * Concrete diagram creation tests can extend this class and reuse
 * {@link #testDiagramCreationOnParentElement(EClass, EReference)} to test the creation of a diagram on a given element,
 * or {@link #testDiagramCreationOnParentElementWithIntermediateElementCreation(EClass, EReference, EClass, EReference)}
 * to test the creation of an intermediate element on which the diagram is created.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class DiagramCreationTest extends AbstractPapyrusWebTest {

    /**
     * Initializes the test with the provided {@code representationName} and {@code rootElementEClass}.
     *
     * @param documentName
     *            the name of the document to create
     * @param representationName
     *            the name of the representation to create
     * @param rootElementEClass
     *            the type of the root semantic element to create
     *
     * @see AbstractPapyrusWebTest#AbstractPapyrusWebTest(String, String, EClass)
     */
    public DiagramCreationTest(String documentName, String representationName, EClass rootElementEClass) {
        super(documentName, representationName, rootElementEClass);
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithoutRepresentation();
    }

    /**
     * Tests the creation of the diagram on the given {@code parentElementType}.
     * <p>
     * This method creates a {@code parentElementType} element in the root of the model (using the
     * {@code parentContainmentReference}) and creates a diagram representation on it. The test checks that no
     * intermediate element has been created, and that the created diagram representation targets the
     * {@code parentElementType} element.
     * </p>
     * <p>
     * Note that the type of diagram representation to create is configured by the {@code representationName} argument
     * of this class' constructor.
     * </p>
     *
     * @param parentElementType
     *            the type of the element to create the diagram representation on
     * @param parentContainmentReference
     *            the reference containing the parent element in the model's root
     */
    protected void testDiagramCreationOnParentElement(EClass parentElementType, EReference parentContainmentReference) {
        EObject parentElement = this.createSemanticElement(this.getRootSemanticElement(), parentContainmentReference, parentElementType, "Container");
        String parentElementId = this.getIdentityService().getId(parentElement);
        this.createRepresentation(parentElementId);
        // Reload the element from the EditingContext to make sure it is updated.
        parentElement = this.findSemanticElementById(parentElementId);
        // Check that no intermediate element has been created
        assertTrue(parentElement.eContents().isEmpty());
        this.assertThatRepresentationTargetsSemanticElement(parentElement);
    }

    /**
     * Tests the creation of the diagram on an intermediate element when targeting the given {@code parentElementType}.
     * <p>
     * This method creates a {@code parentElementType} element in the root of the model (using the
     * {@code parentContainmentReference}) and creates a diagram representation on it. The test checks that an
     * intermediate element of type {@code intermediateElementType} has been created and is contained by the
     * {@code intermediateContainmentReference} reference by the {@code parentElementType} element. The test also
     * verifies that the created diagram representation targets the {@code intermediateElementType} element.
     * </p>
     * <p>
     * Note that the type of diagram representation to create is configured by the {@code representationName} argument
     * of this class' constructor.
     * </p>
     *
     * @param parentElementType
     *            the type of the element to create the diagram representation on
     * @param parentContainmentReference
     *            the reference containing the parent element in the model's root
     * @param intermediateElementType
     *            the type of the intermediate element created with the diagram creation
     * @param intermediateContainmentReference
     *            the reference containing the intermediate element
     */
    protected void testDiagramCreationOnParentElementWithIntermediateElementCreation(EClass parentElementType, EReference parentContainmentReference, EClass intermediateElementType,
            EReference intermediateContainmentReference) {
        EObject parentElement = this.createSemanticElement(this.getRootSemanticElement(), parentContainmentReference, parentElementType, "Container");
        String parentElementId = this.getIdentityService().getId(parentElement);
        this.createRepresentation(parentElementId);
        // Reload the element from the EditingContext to make sure it is updated.
        parentElement = this.findSemanticElementById(parentElementId);
        final EObject createdIntermediateElement;
        if (intermediateContainmentReference.isMany()) {
            List<?> subElements = (List<?>) parentElement.eGet(intermediateContainmentReference);
            assertEquals(1, subElements.size());
            assertTrue(intermediateElementType.isInstance(subElements.get(0)));
            createdIntermediateElement = (EObject) subElements.get(0);
        } else {
            assertTrue(intermediateElementType.isInstance(parentElement.eGet(intermediateContainmentReference)));
            createdIntermediateElement = (EObject) parentElement.eGet(intermediateContainmentReference);
        }
        this.assertThatRepresentationTargetsSemanticElement(createdIntermediateElement);
    }

    /**
     * Checks that the current representation targets the provided {@code semanticElement}.
     * <p>
     * The current representation is the one created by the last {@link #createRepresentation(String)} call.
     * </p>
     *
     * @param semanticElement
     *            the semantic element that should be targeted by the representation
     */
    private void assertThatRepresentationTargetsSemanticElement(EObject semanticElement) {
        Optional<IRepresentation> optRepresentation = this.representationSearchService.findById(this.getEditingContext(), this.representationId, IRepresentation.class);
        assertTrue(optRepresentation.isPresent());
        assertEquals(this.getIdentityService().getId(semanticElement), optRepresentation.get().getTargetObjectId());
    }

}

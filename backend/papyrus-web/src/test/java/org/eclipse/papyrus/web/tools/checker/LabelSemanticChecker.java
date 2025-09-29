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

import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.uml2.uml.NamedElement;

/**
 * Utility class to check the label of a semantic element.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class LabelSemanticChecker implements Checker {

    private final IObjectSearchService objectSearchService;

    private Supplier<IEditingContext> editingContextSupplier;

    private String expectedLabel;

    /**
     * Initializes the checker with the provided parameters.
     *
     * @param objectSearchService
     *            the object service used to retrieve and compute identifiers
     * @param editingContextSupplier
     *            a supplier to access and reload the editing context
     * @param expectedLabel
     *            the expected label to check
     */
    public LabelSemanticChecker(IObjectSearchService objectSearchService,
            Supplier<IEditingContext> editingContextSupplier, String expectedLabel) {
        this.objectSearchService = objectSearchService;
        this.editingContextSupplier = editingContextSupplier;
        this.expectedLabel = expectedLabel;
    }

    @Override
    public void validateRepresentationElement(IDiagramElement element) {
        String semanticId = null;
        if (element instanceof Node node) {
            semanticId = node.getTargetObjectId();
        } else if (element instanceof Edge edge) {
            semanticId = edge.getTargetObjectId();
        } else {
            fail("Unknown IDiagramElement type " + element.getClass().getSimpleName());
        }
        Optional<Object> optObject = this.objectSearchService.getObject(this.editingContextSupplier.get(), semanticId);
        assertThat(optObject).as("Cannot find the semantic element with id " + semanticId).isPresent();
        assertThat(optObject.get()).as("The semantic object with id " + semanticId + " isn't a NamedElement").isInstanceOf(NamedElement.class);
        NamedElement namedElement = (NamedElement) optObject.get();
        String labelError = MessageFormat.format("Label {0} of element {1} doesn't match the expected value {2}", namedElement.getName(), namedElement, this.expectedLabel);
        assertThat(namedElement.getName()).as(labelError).isEqualTo(this.expectedLabel);
    }

}

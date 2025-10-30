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
package org.eclipse.papyrus.web.tools.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.papyrus.web.tools.checker.Checker;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.InsideLabel;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.OutsideLabel;

/**
 * Utility class to help the definition of edit label tool tests.
 * <p>
 * Concrete edit label tool tests can extend this class and reuse {@link #editLabel(String, String, Checker)} to invoke
 * the edit label tool and check the result.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class EditLabelTest extends AbstractPapyrusWebTest {

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
    public EditLabelTest(String documentName, String representationName, EClass rootElementEClass) {
        super(documentName, representationName, rootElementEClass);
    }

    /**
     * Edits the label of the provided {@code elementName} with {@code newLabel}.
     *
     * @param elementName
     *            the label of the graphical element to edit the label of
     * @param newLabel
     *            the new label to set
     * @param checker
     *            the {@link Checker} to use to validate the operation
     */
    protected void editLabel(String elementName, String newLabel, Checker checker) {
        assertThat(checker).as("checker cannot be null").isNotNull();
        IDiagramElement element = this.findGraphicalElementExcludingContentByLabel(elementName);
        String labelId = null;
        if (element instanceof Node node) {
            InsideLabel label = node.getInsideLabel();
            if (label == null) {
                labelId = node.getOutsideLabels().stream().map(OutsideLabel::id).findFirst().orElse(null);
            } else {
                labelId = label.getId();
            }
        } else if (element instanceof Edge edge) {
            labelId = edge.getCenterLabel().id();
        } else {
            fail();
        }
        this.applyEditLabelTool(labelId, newLabel);
        // Find by id to avoid finding by name, which is in conflict with the current test.
        IDiagramElement updatedElement = this.findGraphicalElementById(element.getId());
        assertThat(updatedElement).isNotNull();
        checker.validateRepresentationElement(updatedElement);
    }
}

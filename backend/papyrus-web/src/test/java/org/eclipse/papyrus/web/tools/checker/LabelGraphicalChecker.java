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

import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Utility class to check the label of a graphical element.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class LabelGraphicalChecker implements Checker {

    private static final String LABEL_ERROR_TEMPLATE = "Label {0} of element {1} doesn't match the expected value {2}";

    private String expectedLabel;

    /**
     * Initializes the checker with the provided {@code expectedLabel}.
     *
     * @param expectedLabel
     *            the expected label to check
     */
    public LabelGraphicalChecker(String expectedLabel) {
        this.expectedLabel = expectedLabel;
    }

    @Override
    public void validateRepresentationElement(IDiagramElement element) {
        if (element instanceof Node node) {
            if (node.getInsideLabel() != null) {
                String labelError = MessageFormat.format(LABEL_ERROR_TEMPLATE, node.getInsideLabel().getText(), node.getId(), this.expectedLabel);
                assertThat(node.getInsideLabel().getText()).as(labelError).isEqualTo(this.expectedLabel);
            } else if (node.getOutsideLabels() != null) {
                String labelError = MessageFormat.format(LABEL_ERROR_TEMPLATE, node.getOutsideLabels(), node.getId(), this.expectedLabel);
                assertThat(node.getOutsideLabels()).as(labelError).anyMatch(outsideLabel -> outsideLabel.text().equals(this.expectedLabel));
            } else {
                fail("No label found in element " + element.getId());
            }
        } else if (element instanceof Edge edge) {
            String labelError = MessageFormat.format(LABEL_ERROR_TEMPLATE, edge.getCenterLabel().text(), edge.getId(), this.expectedLabel);
            assertThat(edge.getCenterLabel().text()).as(labelError).isEqualTo(this.expectedLabel);
        } else {
            fail("Unknown IDiagramElement type " + element.getClass().getSimpleName());
        }
    }

}

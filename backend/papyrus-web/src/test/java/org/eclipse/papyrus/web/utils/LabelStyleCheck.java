/*******************************************************************************
 * Copyright (c) 2022, 2025 CEA LIST, Obeo.
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
 *******************************************************************************/
package org.eclipse.papyrus.web.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.InsideLabel;
import org.eclipse.sirius.components.diagrams.Label;
import org.eclipse.sirius.components.diagrams.LabelStyle;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Helper used to check the style of a label.
 *
 * @author Arthur Daussy
 */
public class LabelStyleCheck {

    private final LabelStyle labelStyle;

    public LabelStyleCheck(LabelStyle labelStyle) {
        super();
        this.labelStyle = labelStyle;
    }

    public static LabelStyleCheck build(Node n) {
        InsideLabel label = n.getInsideLabel();
        assertNotNull(label);
        LabelStyle style = label.getStyle();
        assertNotNull(style);
        return new LabelStyleCheck(style);
    }

    public static LabelStyleCheck buildCenteredLabel(Edge e) {
        Label label = e.getCenterLabel();
        assertNotNull(label);
        LabelStyle style = label.style();
        assertNotNull(style);
        return new LabelStyleCheck(style);
    }

    public LabelStyleCheck assertIsItalic() {
        assertTrue(this.labelStyle.isItalic());
        return this;
    }

    public LabelStyleCheck assertIsNotItalic() {
        assertFalse(this.labelStyle.isItalic());
        return this;
    }

    public LabelStyleCheck assertIsUnderline() {
        assertTrue(this.labelStyle.isUnderline());
        return this;
    }

    public LabelStyleCheck assertIsNotUnderline() {
        assertFalse(this.labelStyle.isUnderline());
        return this;
    }
}

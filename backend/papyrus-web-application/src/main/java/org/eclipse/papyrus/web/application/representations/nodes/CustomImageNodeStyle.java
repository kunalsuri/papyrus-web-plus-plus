/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 218
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.nodes;

import java.text.MessageFormat;
import java.util.Objects;

import org.eclipse.sirius.components.diagrams.INodeStyle;
import org.eclipse.sirius.components.diagrams.LineStyle;

/**
 * The custom image node style.
 *
 * @author tiboue
 */
public final class CustomImageNodeStyle implements INodeStyle {

    private String shape;

    private String background;

    private String borderColor;

    private int borderSize;

    private LineStyle borderStyle;

    private CustomImageNodeStyle() {
        // Prevent instantiation
    }

    public static Builder newCustomImageNodeStyle() {
        return new Builder();
    }

    public String getShape() {
        return this.shape;
    }

    public String getBackground() {
        return this.background;
    }

    public String getBorderColor() {
        return this.borderColor;
    }

    public int getBorderSize() {
        return this.borderSize;
    }

    public LineStyle getBorderStyle() {
        return this.borderStyle;
    }

    @Override
    public String toString() {
        String pattern = "{0} '{'shape: {1}, background: {2}, border: '{' background: {3}, size: {4}, style: {5}'}''}'";
        return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.getShape(), this.background, this.borderColor, this.borderSize, this.borderStyle);
    }

    /**
     * The builder used to create the custom image node style.
     *
     * @author tiboue
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public static final class Builder {

        private String shape;

        private String background;

        private String borderColor;

        private int borderSize;

        private LineStyle borderStyle;

        private Builder() {
            // Prevent instantiation
        }

        public Builder shape(String shape) {
            this.shape = Objects.requireNonNull(shape);
            return this;
        }

        public Builder background(String background) {
            this.background = Objects.requireNonNull(background);
            return this;
        }

        public Builder borderColor(String borderColor) {
            this.borderColor = Objects.requireNonNull(borderColor);
            return this;
        }

        public Builder borderSize(int borderSize) {
            this.borderSize = borderSize;
            return this;
        }

        public Builder borderStyle(LineStyle borderStyle) {
            this.borderStyle = Objects.requireNonNull(borderStyle);
            return this;
        }

        public CustomImageNodeStyle build() {
            CustomImageNodeStyle nodeStyleDescription = new CustomImageNodeStyle();
            nodeStyleDescription.shape = Objects.requireNonNull(this.shape);
            nodeStyleDescription.background = Objects.requireNonNull(this.background);
            nodeStyleDescription.borderColor = Objects.requireNonNull(this.borderColor);
            nodeStyleDescription.borderSize = this.borderSize;
            nodeStyleDescription.borderStyle = Objects.requireNonNull(this.borderStyle);
            return nodeStyleDescription;
        }
    }

}

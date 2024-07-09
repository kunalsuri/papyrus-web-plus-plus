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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.papyrus.web.custom.widgets.IAQLInterpreterProvider;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription;
import org.eclipse.sirius.components.core.api.IEditingContextSearchService;
import org.eclipse.sirius.components.diagrams.INodeStyle;
import org.eclipse.sirius.components.diagrams.LineStyle;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.interpreter.StringValueProvider;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.FixedColor;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.emf.diagram.INodeStyleProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * This class provides style information for the custom image node.
 *
 * @author tiboue
 */
@Service
public class CustomImageNodeStyleProvider implements INodeStyleProvider {

    public static final String NODE_CUSTOM_IMAGE = "customnode:customimage";

    private IEditingContextSearchService editingContextSearchService;

    private IAQLInterpreterProvider interpreterProvider;

    public CustomImageNodeStyleProvider(@Lazy IEditingContextSearchService editingContextSearchService,
            IAQLInterpreterProvider interpreterProvider) {
        this.editingContextSearchService = Objects.requireNonNull(editingContextSearchService);
        this.interpreterProvider = Objects.requireNonNull(interpreterProvider);
    }

    @Override
    public Optional<String> getNodeType(NodeStyleDescription nodeStyle) {
        if (nodeStyle instanceof CustomImageNodeStyleDescription) {
            return Optional.of(NODE_CUSTOM_IMAGE);
        }
        return Optional.empty();
    }

    @Override
    public Optional<INodeStyle> createNodeStyle(NodeStyleDescription nodeStyle, Optional<String> optionalEditingContextId) {
        Optional<INodeStyle> iNodeStyle = Optional.empty();
        Optional<String> nodeType = this.getNodeType(nodeStyle);
        if (nodeType.isPresent() && nodeStyle instanceof CustomImageNodeStyleDescription) {
            return Optional.of(CustomImageNodeStyle.newCustomImageNodeStyle()
                    .shape(Optional.ofNullable(((CustomImageNodeStyleDescription) nodeStyle).getShape())
                            .orElse(""))
                    .background(Optional.ofNullable(((CustomImageNodeStyleDescription) nodeStyle).getBackground())
                            .filter(FixedColor.class::isInstance)
                            .map(FixedColor.class::cast)
                            .map(FixedColor::getValue)
                            .orElse("transparent"))
                    .borderColor(Optional.ofNullable(nodeStyle.getBorderColor())
                            .filter(FixedColor.class::isInstance)
                            .map(FixedColor.class::cast)
                            .map(FixedColor::getValue)
                            .orElse("black"))
                    .borderSize(nodeStyle.getBorderSize())
                    .borderStyle(LineStyle.valueOf(nodeStyle.getBorderLineStyle().getLiteral()))
                    .build());
        }

        return iNodeStyle;
    }

    public Optional<INodeStyle> createNodeStyle(NodeStyleDescription nodeStyle, Optional<String> optionalEditingContextId, VariableManager variableManager, AQLInterpreter interpreter) {
        Optional<INodeStyle> iNodeStyle = Optional.empty();
        Optional<String> nodeType = this.getNodeType(nodeStyle);
        StringValueProvider svp = new StringValueProvider(interpreter, ((CustomImageNodeStyleDescription) nodeStyle).getShape());
        if (nodeType.isPresent() && nodeStyle instanceof CustomImageNodeStyleDescription) {
            return Optional.of(CustomImageNodeStyle.newCustomImageNodeStyle()
                    .shape(svp.apply(variableManager))
                    .background(Optional.ofNullable(((CustomImageNodeStyleDescription) nodeStyle).getBackground())
                            .filter(FixedColor.class::isInstance)
                            .map(FixedColor.class::cast)
                            .map(FixedColor::getValue)
                            .orElse("transparent"))
                    .borderColor(Optional.ofNullable(nodeStyle.getBorderColor())
                            .filter(FixedColor.class::isInstance)
                            .map(FixedColor.class::cast)
                            .map(FixedColor::getValue)
                            .orElse("black"))
                    .borderSize(nodeStyle.getBorderSize())
                    .borderStyle(LineStyle.valueOf(nodeStyle.getBorderLineStyle().getLiteral()))
                    .build());
        }

        return iNodeStyle;
    }

    /*
     * private AQLInterpreter createInterpreter(Optional<String> optionalEditingContextId) { Optional<IEditingContext>
     * editingContext = this.editingContextSearchService.findById(optionalEditingContextId.get()); return
     * this.interpreterProvider.createInterpreter(org.eclipse.sirius.components.view.ViewFactory.eINSTANCE.createView(),
     * editingContext.get()); }
     */

}

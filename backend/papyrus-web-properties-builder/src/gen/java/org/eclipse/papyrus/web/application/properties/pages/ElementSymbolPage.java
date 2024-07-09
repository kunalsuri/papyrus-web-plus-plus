/*****************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 210
 *****************************************************************************/
package org.eclipse.papyrus.web.application.properties.pages;

import org.eclipse.papyrus.web.application.properties.ColorRegistry;
import org.eclipse.papyrus.web.application.properties.CustomImageWidgetBuilder;
import org.eclipse.papyrus.web.application.properties.ViewElementsFactory;
import org.eclipse.sirius.components.view.form.GroupDescription;
import org.eclipse.sirius.components.view.form.GroupDisplayMode;
import org.eclipse.sirius.components.view.form.PageDescription;

/**
 * @author tiboue
 */
public class ElementSymbolPage {

    protected final ViewElementsFactory viewElementFactory;

    protected final ColorRegistry colorRegistry;

    public ElementSymbolPage(ViewElementsFactory viewElementFactory, ColorRegistry colorRegistry) {
        super();
        this.viewElementFactory = viewElementFactory;
        this.colorRegistry = colorRegistry;
    }

    public PageDescription create() {

        PageDescription page = this.createPage();

        this.createElementSymbolGroup(page);

        return page;

    }

    protected void createElementSymbolGroup(PageDescription page) {
        GroupDescription group = this.viewElementFactory.createGroupDescription("element_symbol_group", "", "var:self", GroupDisplayMode.LIST);
        page.getGroups().add(group);

        this.addSymbolImage(group);
    }

    protected void addSymbolImage(GroupDescription group) {
        var builder = new CustomImageWidgetBuilder()
                .name("symbol") //
                .label("aql:'Symbol'") //
                .help("aql:'The symbol associated to this element'")
                .uuidExpression("aql:self.getSymbolValue()")
                .addOperation("")
                .selectOperation("aql:self.createOrUpdateAnnotation(newUuid)")
                .removeOperation("aql:self.removeSymbolFromAnnotation()");
        group.getChildren().add(builder.build());
    }

    protected PageDescription createPage() {
        return this.viewElementFactory.createPageDescription("element_symbols_page", "uml::Element", "aql:'Symbol'", "aql:self", "aql:not(selection->size()>1)");
    }
}

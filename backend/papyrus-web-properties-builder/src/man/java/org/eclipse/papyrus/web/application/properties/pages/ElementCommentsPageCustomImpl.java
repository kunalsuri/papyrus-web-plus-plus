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
package org.eclipse.papyrus.web.application.properties.pages;

import org.eclipse.papyrus.web.application.properties.ColorRegistry;
import org.eclipse.papyrus.web.application.properties.MultiReferenceWidgetBuilder;
import org.eclipse.papyrus.web.application.properties.ViewElementsFactory;
import org.eclipse.sirius.components.view.form.GroupDescription;

/**
 * Customization of {@link ElementCommentsPage}.
 * @author Jerome Gout
 */
public class ElementCommentsPageCustomImpl extends ElementCommentsPage {

    public ElementCommentsPageCustomImpl(ViewElementsFactory viewElementFactory, ColorRegistry colorRegistry) {
        super(viewElementFactory, colorRegistry);
    }

    @Override
    protected void addOwnedComment(GroupDescription group) {
        super.addOwnedComment(group);

        // Add a second widget inside the comment page for applied comments
        var builder = new MultiReferenceWidgetBuilder() //
                .name("appliedComment") //
                .label("aql:'Applied comments'") //
                .help("aql:'The list of comments applied to this element'") //
                .isEnable("aql:self.eClass().getEStructuralFeature('ownedComment').changeable") //
                .owner("") //
                .type("aql:'uml::Comment'") //
                .value("aql:self.getAllAppliedComments()") //
                .searchScope("aql:self.getAllReachableRootElements()") //
                .dropdownOptions("aql:self.getAllReachableElements(uml::Comment)") //
                .createOperation("aql:parent.create('uml::Comment', feature)") //
                .addOperation("aql:newValue.addReferenceElement(Sequence{self}, 'annotatedElement')") //
                .removeOperation("aql:self.delete(item, 'annotatedElement')") //
                .reorderOperation("") // sorting those elements has no sense
                .clearOperation("aql:self.getAllAppliedComments()->forAll(comment | self.delete(comment, 'annotatedElement'))"); //
        group.getChildren().add(builder.build());
    }
}

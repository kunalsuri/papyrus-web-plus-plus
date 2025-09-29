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
package org.eclipse.papyrus.web.custom.widgets.containmentreference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.web.custom.widgets.IAQLInterpreterProvider;
import org.eclipse.sirius.components.core.api.IEditService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.emf.services.api.IEMFKindService;
import org.eclipse.sirius.components.view.emf.form.api.IViewFormDescriptionSearchService;
import org.junit.jupiter.api.Test;

/**
 * Unit tests of the containment reference create element event handler.
 *
 * @author Jerome Gout
 */
public class CreateElementHandlerTests {

    @Test
    public void testCreateElementInReference() {
        String descriptionId = "siriusComponents://formElementDescription?kind=ContainmentReferenceWidgetDescription&sourceKind=view&sourceId=2a52b45a-ba2e-36b2-8b65-1784bc54e5c0&sourceElementId=40a866b8-aa89-4460-9193-04cc19216ff3";
        ContainmentReferenceCreateElementHandler handler = new ContainmentReferenceCreateElementHandler(new IEMFKindService.NoOp(), new IEditService.NoOp(), new ILabelService.NoOp(),
                new IViewFormDescriptionSearchService.NoOp(), new IAQLInterpreterProvider.NoOp(), (EObject instance, String featureName, Object value) -> null,
                (EObject instance, String featureName) -> null);

        assertTrue(handler.canHandle(descriptionId));
    }

    /**
     * Checks that invalid descriptionId (not a view model sourceKind) is not handle.
     */
    @Test
    public void testCreateElementInReferenceInvalidSourceKind() {
        String descriptionId = "siriusComponents://formElementDescription?kind=ContainmentReferenceWidgetDescription&sourceKind=NOT_VIEW_MODEL&sourceId=2a52b45a-ba2e-36b2-8b65-1784bc54e5c0&sourceElementId=40a866b8-aa89-4460-9193-04cc19216ff3";
        ContainmentReferenceCreateElementHandler handler = new ContainmentReferenceCreateElementHandler(new IEMFKindService.NoOp(), new IEditService.NoOp(), new ILabelService.NoOp(),
                new IViewFormDescriptionSearchService.NoOp(), new IAQLInterpreterProvider.NoOp(), (EObject instance, String featureName, Object value) -> null,
                (EObject instance, String featureName) -> null);

        assertFalse(handler.canHandle(descriptionId));
    }

    /**
     * Checks that invalid descriptionId (not a view model kind) is not handle.
     */
    @Test
    public void testCreateElementInReferenceInvalidKind() {
        String descriptionId = "siriusComponents://formElementDescription?kind=UnknownWidgetDescription&sourceKind=view&sourceId=2a52b45a-ba2e-36b2-8b65-1784bc54e5c0&sourceElementId=40a866b8-aa89-4460-9193-04cc19216ff3";
        ContainmentReferenceCreateElementHandler handler = new ContainmentReferenceCreateElementHandler(new IEMFKindService.NoOp(), new IEditService.NoOp(), new ILabelService.NoOp(),
                new IViewFormDescriptionSearchService.NoOp(), new IAQLInterpreterProvider.NoOp(), (EObject instance, String featureName, Object value) -> null,
                (EObject instance, String featureName) -> null);

        assertFalse(handler.canHandle(descriptionId));
    }
}

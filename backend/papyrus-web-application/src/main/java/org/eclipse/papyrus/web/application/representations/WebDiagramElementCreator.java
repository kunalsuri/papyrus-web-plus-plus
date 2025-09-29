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
package org.eclipse.papyrus.web.application.representations;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.uml.domain.services.create.CreationStatus;
import org.eclipse.papyrus.uml.domain.services.create.ICreator;
import org.eclipse.papyrus.uml.domain.services.status.State;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.ViewHelper;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;

/**
 * Element in charge of creating an element in the context of a web Diagram.
 *
 * @author Arthur Daussy
 */
public class WebDiagramElementCreator {

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final IViewDiagramDescriptionService viewDiagramNavigationService;

    private final IDiagramOperationsService diagramOperationsService;

    private final ICreator elementCreator;

    public WebDiagramElementCreator(ICreator elementCreator, IIdentityService identityService,
            ILabelService labelService, IViewDiagramDescriptionService diagramNavigationService,
            IDiagramOperationsService diagramOperationsService) {
        super();
        this.elementCreator = Objects.requireNonNull(elementCreator);
        this.identityService = Objects.requireNonNull(identityService);
        this.labelService = Objects.requireNonNull(labelService);
        this.viewDiagramNavigationService = Objects.requireNonNull(diagramNavigationService);
        this.diagramOperationsService = Objects.requireNonNull(diagramOperationsService);
    }

    public CreationStatus handleCreation(EObject parent, String type, String referenceName, Node targetView,
            IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {

        if (parent == null || type == null || referenceName == null) {
            return CreationStatus.createFailingStatus(
                    MessageFormat.format("Invalid input for creation (parent ={0} type ={1} referenceName = {2})",
                            parent, type, referenceName));
        }

        CreationStatus status = this.elementCreator.create(parent, type, referenceName);

        if (status.getState() == State.DONE) {
            EObject semanticElement = status.getElement();
            if (semanticElement != null) {
                IViewHelper createViewHelper = ViewHelper.create(this.identityService, this.labelService,
                        this.viewDiagramNavigationService, this.diagramOperationsService, diagramContext,
                        capturedNodeDescriptions);
                if (targetView == null) {
                    createViewHelper.createRootView(semanticElement);
                } else {
                    createViewHelper.createChildView(semanticElement, targetView);
                }
            }
        }

        return status;
    }

}

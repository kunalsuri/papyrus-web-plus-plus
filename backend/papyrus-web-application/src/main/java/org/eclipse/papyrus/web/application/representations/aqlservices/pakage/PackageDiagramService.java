/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo, Artal Technologies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 190
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.aqlservices.pakage;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.web.application.representations.IWebExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.IWebInternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.ViewHelper;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.springframework.stereotype.Service;

/**
 * Service for the "Package" diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public class PackageDiagramService extends AbstractDiagramService {

    /**
     * Logger used to report errors and warnings to the user.
     */
    private final ILogger logger;

    /**
     * Constructor.
     *
     * @param identityService
     *         the service in charge of getting the identity of an object
     * @param labelService
     *         the service in charge of getting labels and images of an object
     * @param objectSearchService
     *         the service in charge of getting an object from its id
     * @param diagramNavigationService
     *            helper that must introspect the current diagram's structure and its description
     * @param diagramOperationsService
     *            helper that must modify the current diagram, most notably create or delete views for unsynchronized
     *            elements
     * @param editableChecker
     *            Object that check if an element can be edited
     * @param viewDiagramService
     *            Service used to navigate in DiagramDescription
     * @param logger
     *            Logger used to report errors and warnings to the user
     */
    //CHECKSTYLE:OFF Injected parameters
    public PackageDiagramService(IIdentityService identityService, ILabelService labelService,
            IObjectSearchService objectSearchService, IDiagramNavigationService diagramNavigationService,
            IDiagramOperationsService diagramOperationsService, IEditableChecker editableChecker,
            IViewDiagramDescriptionService viewDiagramService, ILogger logger) {
        //CHECKSTYLE:ON Injected parameters
        super(identityService, labelService, objectSearchService, diagramNavigationService, diagramOperationsService,
                editableChecker, viewDiagramService, logger);
        this.logger = logger;
    }

    @Override
    protected IWebInternalSourceToRepresentationDropBehaviorProvider buildGraphicalDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getIdentityService(), getLabelService(),
                this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext,
                capturedNodeDescriptions);
        IWebInternalSourceToRepresentationDropBehaviorProvider dropProvider = new PackageGraphicalDropBehaviorProvider(
                editionContext, createViewHelper, this.getObjectSearchService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

    @Override
    protected IWebExternalSourceToRepresentationDropBehaviorProvider buildSemanticDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getIdentityService(), getLabelService(),
                this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext,
                capturedNodeDescriptions);
        IWebExternalSourceToRepresentationDropBehaviorProvider dropProvider = new PackageSemanticDropBehaviorProvider(
                editionContext, createViewHelper, this.getObjectSearchService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

}

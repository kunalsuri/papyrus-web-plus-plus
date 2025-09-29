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
package org.eclipse.papyrus.web.services;

import org.eclipse.papyrus.web.application.representations.PapyrusRepresentationDescriptionRegistry;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramBuilderService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.papyrus.web.utils.AbstractWebUMLTest;
import org.eclipse.papyrus.web.utils.DiagramServiceTestHelper;
import org.eclipse.papyrus.web.utils.DiagramTestHelper;
import org.eclipse.sirius.components.view.emf.diagram.IDiagramIdProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class used to implement diagram integration tests.
 *
 * @author Arthur Daussy
 */
public abstract class AbstractDiagramTest extends AbstractWebUMLTest {

    private DiagramTestHelper viewTestHelper;

    private AbstractDiagramService diagramService;

    @Autowired
    private IDiagramNavigationService diagramNavigationService;

    @Autowired
    private IDiagramOperationsService diagramOperationsService;

    @Autowired
    private IDiagramBuilderService diagramBuilderService;

    @Autowired
    private PapyrusRepresentationDescriptionRegistry viewRegistry;

    @Autowired
    private IDiagramOperationsService diagramOpService;

    @Autowired
    private IDiagramNavigationService diagramNavService;

    @Autowired
    private IViewDiagramDescriptionService viewDiagramDescriptionService;

    @Autowired
    private IDiagramIdProvider idProvider;

    private DiagramServiceTestHelper diagramServiceTestHelper;

    @Override
    @BeforeEach
    public void before() {
        super.before();
        this.diagramService = this.buildService();
        this.viewTestHelper = new DiagramTestHelper(this.getEditingContext(), this.getIdentityService(),
                getLabelService(), getObjectSearchService(), this.viewRegistry, this.diagramBuilderService,
                this.diagramOpService, this.diagramNavService,
                this.viewDiagramDescriptionService, this.idProvider);
        this.diagramServiceTestHelper = new DiagramServiceTestHelper(this.viewTestHelper, this.diagramService,
                this.getEditingContext(), getIdentityService(), getObjectSearchService());
    }

    protected IViewDiagramDescriptionService getViewDiagramDescriptionService() {
        return this.viewDiagramDescriptionService;
    }

    protected abstract AbstractDiagramService buildService();

    protected DiagramServiceTestHelper getServiceTester() {
        return this.diagramServiceTestHelper;
    }

    protected IDiagramNavigationService getDiagramNavigationService() {
        return this.diagramNavigationService;
    }

    protected IDiagramOperationsService getDiagramOperationsService() {
        return this.diagramOperationsService;
    }

    protected DiagramTestHelper getDiagramHelper() {
        return this.viewTestHelper;
    }

    protected AbstractDiagramService getDiagramService() {
        return this.diagramService;
    }

}

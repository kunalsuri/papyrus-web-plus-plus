/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo, Artal Technologies.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import org.eclipse.papyrus.web.application.representations.view.aql.Services;

/**
 * Services available for the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class ComponentDiagramServices extends Services {

    /**
     * The name of the service that check creation of {@link Property}.
     */
    public static final String CAN_CREATE_PROPERTY_INTO_PARENT = "canCreatePropertyIntoParentCPD";

    /**
     * The name of the service that creates a {@link Port}.
     */
    public static final String CREATE_PORT = "createPortCPD";

    /**
     * The name of the service that creates a {@link Port}.
     */
    public static final String CREATE_PORT_IN_HOLDER = "createPortInHolderCPD";

    /**
     * The name of the service that creates a {@link Property}.
     */
    public static final String CREATE_PROPERTY = "createPropertyCPD";

    /**
     * The name of the service that retrieves {@link Port} candidates.
     */
    public static final String GET_PORT_NODE_CANDIDATES = "getPortCandidatesCPD";

    /**
     * The name of the service that retrieves {@link Property} candidates.
     */
    public static final String GET_PROPERTY_NODE_CANDIDATES = "getPropertyCandidatesCPD";

    /**
     * The name of the service that check if a {@link Connector} should be displayed.
     */
    public static final String SHOULD_DISPLAY_CONNECTOR = "shouldDisplayConnector";

}

/*****************************************************************************
 * Copyright (c) 2022, 2024 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.application.representations.view.aql;

/**
 * List of service names.
 *
 * @author Arthur Daussy
 */
public class Services {

    public static final String RENDER_LABEL_ONE_LINE = "renderSimpleOneLineLabel";

    public static final String ADD_VALUE_TO_SERVICE = "addValueTo";

    public static final String CREATE_DOMAIN_BASED_EDGE_SERVICE = "createDomainBasedEdge";

    public static final String RECONNECT_SOURCE_ON_DOMAIN_BASED_EDGE = "reconnectSourceOnDomainBasedEdge";

    public static final String RECONNECT_TARGET_ON_DOMAIN_BASED_EDGE = "reconnectTargetOnDomainBasedEdge";

    public static final String CONSUME_DIRECT_EDIT_VALUE_SERVICE = "consumeDirectEditValue";

    public static final String GET_DIRECT_EDIT_INPUT_VALUE_SERVICE = "getDirectEditInputValue";

    public static final String RESET_DEFAULT_NAME_SERVICE = "resetDefaultName";

    public static final String CREATE_SIBLING_SERVICE = "createSibling";

    public static final String CREATE_IN_COMPARTMENT_SERVICE = "createInCompartment";

    public static final String DOMAIN_BASED_EDGE_SOURCE_LABEL_SERVICE = "getDomainBasedEdgeSourceLabel";

    public static final String DOMAIN_BASED_EDGE_TARGET_LABEL_SERVICE = "getDomainBasedEdgeTargetLabel";

    public static final String RECONNECT_COMMENT_ANNOTATED_ELEMENT_EDGE_TARGET_SERVICE = "reconnectCommentAnnotatedElementEdgeTarget";

    public static final String RECONNECT_COMMENT_ANNOTATED_ELEMENT_EDGE_SOURCE_SERVICE = "reconnectCommentAnnotatedElementEdgeSource";

    public static final String RECONNECT_CONSTRAINT_CONSTRAINED_ELEMENT_EDGE_TARGET_SERVICE = "reconnectConstraintConstrainedElementEdgeTarget";

    public static final String RECONNECT_CONSTRAINT_CONSTRAINED_ELEMENT_EDGE_SOURCE_SERVICE = "reconnectConstraintConstrainedElementEdgeSource";

    public static final String RENDER_LABEL_SERVICE = "renderLabel";

    public static final String MOVE_IN = "moveIn";

    public static final String GET_SOURCE_SERVICE = "getSource";

    public static final String GET_TARGETS_SERVICE = "getTargets";

    public static final String GET_ALL_REACHABLE_SERVICE = "getAllReachable";

    public static final String DESTROY_SERVICE = "destroy";

    public static final String SEMANTIC_DROP_SERVICE = "semanticDrop";

    public static final String GRAPHICAL_DROP_SERVICE = "graphicalDrop";

    public static final String CREATE_SERVICE = "create";

    public static final String IS_NOT_VISUAL_DESCENDANT = "isNotVisualDescendant";

    public static final String REMOVE_VALUE_FROM = "removeValueFrom";

    public static final String GET_SYMBOL = "getAllSymbol";

    public static final String GET_ALL_NON_SYMBOL = "getAllNonSymbol";

    /**
     * The name of the service that check is the root model is not a Profile model.
     */
    public static final String IS_NOT_PROFILE_MODEL = "isNotProfileModel";

    public static final String CREATE_ANNOTATION = "createAnnotation";

}

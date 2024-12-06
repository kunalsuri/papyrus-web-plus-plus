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
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.view.aql;

import static org.eclipse.papyrus.web.application.representations.view.aql.CallQuery.queryServiceOnSelf;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.CONVERTED_NODES;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DELETION_POLICY;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DIAGRAM;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DIAGRAM_CONTEXT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DROPPED_ELEMENT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.DROPPED_NODE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDGE_SEMANTIC_ELEMENT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDGE_SOURCE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDGE_TARGET;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.EDITING_CONTEXT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.RECONNECTION_SOURCE_VIEW;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.RECONNECTION_TARGET_VIEW;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SELECTED_EDGE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SELECTED_NODE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SELF;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_EDGE_SOURCE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_EDGE_TARGET;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_RECONNECTION_SOURCE;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.SEMANTIC_RECONNECTION_TARGET;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.TARGET_ELEMENT;
import static org.eclipse.papyrus.web.application.representations.view.aql.Variables.TARGET_NODE;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.papyrus.web.application.representations.view.IDomainHelper;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;

/**
 * Builder of AQL query used to builder a DiagramDescription.
 *
 * @author Arthur Daussy
 */
public class QueryHelper {

    public static final String AQL_PREFIX = "aql:";

    private static final String S_SEP = "'";

    private final IDomainHelper metamodelHelper;

    public QueryHelper(IDomainHelper metamodelHelper) {
        super();
        this.metamodelHelper = metamodelHelper;
    }

    static String trimAqlPrefix(String expression) {
        if (expression.startsWith(AQL_PREFIX)) {
            return expression.substring(AQL_PREFIX.length());
        }
        return expression;
    }

    /**
     * Converts the given String into a AQL String literal.
     *
     * @param string
     *            a string value
     * @return a AQL String literal
     */
    public String aqlString(String string) {
        return S_SEP + string + S_SEP;
    }

    public String aqlDomainBaseGetSourceQuery() {
        return CallQuery.queryServiceOnSelf(Services.GET_SOURCE_SERVICE);
    }

    public String aqlDomainBaseGetTargetsQuery() {
        return CallQuery.queryServiceOnSelf(Services.GET_TARGETS_SERVICE);
    }

    /**
     * Query to get self element.
     */
    public String querySelf() {
        return AQL_PREFIX + SELF;
    }

    /**
     * Query to retrieve all element of a given type in the current ResourceSet.
     *
     * @param type
     *            the searched type
     * @return the query
     */
    public String queryAllReachable(EClass type) {
        return CallQuery.queryServiceOnSelf(Services.GET_ALL_REACHABLE_SERVICE, this.aqlString(type.getName()));
    }

    /**
     * Query to retrieve all elements with the exact expected type current ResourceSet (the subtypes are not returned).
     *
     * @param type
     *            the searched type
     * @return the query
     */
    public String queryAllReachableExactType(EClass type) {
        return CallQuery.queryServiceOnSelf(Services.GET_ALL_REACHABLE_SERVICE, this.aqlString(type.getName()), "false");
    }

    public String createNodeQuery(String domainType, String seflExpression, EReference containementRef) {
        String self;
        if (seflExpression.startsWith(AQL_PREFIX)) {
            self = seflExpression.substring(AQL_PREFIX.length());
        } else {
            self = seflExpression;
        }
        return new CallQuery(self).callService(Services.CREATE_SERVICE, this.aqlString(domainType), this.aqlString(containementRef.getName()), SELECTED_NODE, DIAGRAM_CONTEXT,
                CONVERTED_NODES);
    }

    /**
     * Query to create an element in the Holder (parent) insteand of the content. Typically used for BorderedNode.
     *
     * @param domainType
     *            the type of the element to create
     * @param seflExpression
     *            the self expression
     * @param containementRef
     *            the containment reference which will contain the created element
     * @return the query
     */

    public String createNodeInHolderQuery(String domainType, String seflExpression, EReference containementRef) {
        String self;
        if (seflExpression.startsWith(AQL_PREFIX)) {
            self = seflExpression.substring(AQL_PREFIX.length());
        } else {
            self = seflExpression;
        }
        return new CallQuery(self).callService(Services.CREATE_IN_HOLDER_SERVICE, this.aqlString(domainType), this.aqlString(containementRef.getName()), SELECTED_NODE,
                DIAGRAM_CONTEXT,
                CONVERTED_NODES);
    }

    /**
     * Query to create an element in compartment of the given parent.
     *
     * @param domainType
     *            the type of the element to create
     * @param compartmentName
     *            the name of the compartment where element should be created
     * @param containementRef
     *            the containment reference which will contain the created element
     * @return the query
     */
    public String createInCompartmentNodeQuery(String domainType, String compartmentName, EReference containementRef) {
        return new CallQuery(Variables.SELF).callService(Services.CREATE_IN_COMPARTMENT_SERVICE, this.aqlString(domainType), this.aqlString(compartmentName), this.aqlString(containementRef.getName()),
                SELECTED_NODE, DIAGRAM_CONTEXT, CONVERTED_NODES);
    }

    @Deprecated
    public String createSiblingNodeQuery(String domainType, String seflExpression, EReference containementRef) {
        String self;
        if (seflExpression.startsWith(AQL_PREFIX)) {
            self = seflExpression.substring(AQL_PREFIX.length());
        } else {
            self = seflExpression;
        }

        return new CallQuery(trimAqlPrefix(self)).callService(Services.CREATE_SIBLING_SERVICE, this.aqlString(domainType), this.aqlString(containementRef.getName()), SELECTED_NODE, DIAGRAM_CONTEXT,
                CONVERTED_NODES);
    }

    public String queryRenderLabel() {
        return CallQuery.queryServiceOnSelf(Services.RENDER_LABEL_SERVICE);
    }

    public String queryCreateDomainBaseEdge(EdgeDescription description, EReference containmentReference) {
        return new CallQuery(SEMANTIC_EDGE_SOURCE)//
                .callService(Services.CREATE_DOMAIN_BASED_EDGE_SERVICE, //
                        SEMANTIC_EDGE_TARGET, //
                        S_SEP + this.metamodelHelper.toEClass(description.getDomainType()).getName() + S_SEP, //
                        S_SEP + containmentReference.getName() + S_SEP, //
                        EDGE_SOURCE, //
                        EDGE_TARGET, //
                        EDITING_CONTEXT, //
                        DIAGRAM_CONTEXT);
    }

    public String queryAddValueTo(String variable, EStructuralFeature feature, String value) {
        if (!feature.isMany()) {
            throw new IllegalStateException("Expected a many valuated feature");
        }
        return new CallQuery(variable).callService(Services.ADD_VALUE_TO_SERVICE, S_SEP + feature.getName() + S_SEP, value);
    }

    public String querySemanticDrop() {
        return CallQuery.queryServiceOnSelf(Services.SEMANTIC_DROP_SERVICE, SELECTED_NODE, EDITING_CONTEXT, DIAGRAM_CONTEXT, CONVERTED_NODES);
    }

    public String queryGraphicalDrop() {
        return new CallQuery(DROPPED_ELEMENT).callService(Services.GRAPHICAL_DROP_SERVICE, TARGET_ELEMENT, DROPPED_NODE, TARGET_NODE, EDITING_CONTEXT, DIAGRAM_CONTEXT, CONVERTED_NODES);
    }

    /**
     * Builds a query that destroys an element represented by a node.
     *
     * @return a query
     */
    public String queryDestroyNode() {
        return queryServiceOnSelf(Services.DESTROY_SERVICE, DIAGRAM_CONTEXT, SELECTED_NODE, DELETION_POLICY);
    }

    /**
     * Builds a query that destroys an element represented by an edge.
     *
     * @return a query
     */
    public String queryDestroyEdge() {
        return queryServiceOnSelf(Services.DESTROY_SERVICE, DIAGRAM_CONTEXT, SELECTED_EDGE, DELETION_POLICY);
    }

    public String emptyString() {
        return AQL_PREFIX + "''";
    }

    /**
     * Builds the query used for domain based reconnection tool of the source.
     *
     * @return a query
     */
    public String queryDomainBasedSourceReconnection() {
        return new CallQuery(EDGE_SEMANTIC_ELEMENT).callService(Services.RECONNECT_SOURCE_ON_DOMAIN_BASED_EDGE, //
                SEMANTIC_RECONNECTION_SOURCE, SEMANTIC_RECONNECTION_TARGET, RECONNECTION_TARGET_VIEW, RECONNECTION_SOURCE_VIEW, EDITING_CONTEXT, DIAGRAM);
    }

    /**
     * Builds the query used for domain based reconnection tool of the target.
     *
     * @return a query
     */
    public String queryDomainBasedTargetReconnection() {
        return new CallQuery(EDGE_SEMANTIC_ELEMENT).callService(Services.RECONNECT_TARGET_ON_DOMAIN_BASED_EDGE, //
                SEMANTIC_RECONNECTION_SOURCE, SEMANTIC_RECONNECTION_TARGET, RECONNECTION_TARGET_VIEW, RECONNECTION_SOURCE_VIEW, EDITING_CONTEXT, DIAGRAM);
    }

    /**
     * Builds the query used to compute the source label of a domain based edge.
     *
     * @return a query
     */
    public String createDomainBaseEdgeSourceLabelExpression() {
        return CallQuery.queryServiceOnSelf(Services.DOMAIN_BASED_EDGE_SOURCE_LABEL_SERVICE, //
                SEMANTIC_EDGE_SOURCE);
    }

    /**
     * Builds the query used to compute the target label of a domain based edge.
     *
     * @return a query
     */
    public String createDomainBaseEdgeTargetLabelExpression() {
        return CallQuery.queryServiceOnSelf(Services.DOMAIN_BASED_EDGE_TARGET_LABEL_SERVICE, //
                SEMANTIC_EDGE_TARGET);
    }

}

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
package org.eclipse.papyrus.web.utils.mutations;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import org.eclipse.sirius.components.collaborative.diagrams.dto.DropNodeInput;
import org.springframework.stereotype.Service;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

/**
 * Service used to drop a graphical element on the diagram or on graphical containers.
 * <p>
 * This class instantiates and runs the {@code dropNode} mutation. Note that this mutation can drop elements on the diagram itself, or on containers already displayed on the diagram.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class PapyrusDropNodeMutationRunner {

    private static String query = """
            mutation dropNode($input: DropNodeInput!) {
               dropNode(input: $input) {
                 __typename
                 ... on ErrorPayload {
                   messages {
                     body
                     level
                     __typename
                   }
                   __typename
                 }
                 ... on SuccessPayload {
                   messages {
                     body
                     level
                     __typename
                   }
                   __typename
                 }
               }
             }
            """;

    private GraphQL graphQL;

    private ObjectMapper objectMapper;

    /**
     * Initializes the runner with the provided {@code graphQL} and {@code objectMapper}.
     *
     * @param graphQL
     *         the GraphQL execution engine
     * @param objectMapper
     *         the object mapper
     */
    public PapyrusDropNodeMutationRunner(GraphQL graphQL, ObjectMapper objectMapper) {
        this.graphQL = graphQL;
        this.objectMapper = objectMapper;
    }

    /**
     * Drops the {@code droppedElementId} element on the {@code targetElementId}.
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the element to drop
     * @param representationId
     *         the representation where the element is dropped
     * @param droppedElementId
     *         the graphical identifier of the element to drop
     * @param targetElementId
     *         the graphical identifier of the target container element
     */
    public void dropNode(String editingContextId, String representationId, String droppedElementId, String targetElementId) {
        DropNodeInput dropNodeInput = new DropNodeInput(UUID.randomUUID(), editingContextId, representationId, droppedElementId, targetElementId, 0, 0);
        ExecutionInput executionInput = ExecutionInput.newExecutionInput(query) //
                .variables(Map.of("input", this.objectMapper.convertValue(dropNodeInput, new TypeReference<Map<String, Object>>() {
                })))
                .build();
        ExecutionResult executionResult = this.graphQL.execute(executionInput);
    }
}

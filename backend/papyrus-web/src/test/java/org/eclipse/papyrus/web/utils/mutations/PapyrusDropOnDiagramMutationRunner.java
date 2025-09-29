/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo.
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.sirius.components.collaborative.diagrams.dto.DropOnDiagramInput;
import org.springframework.stereotype.Service;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

/**
 * Service used to drop a semantic element on the diagram.
 * <p>
 * This class instantiates and runs the {@code dropOnDiagram} mutation. Note that this mutation can drop elements on the diagram itself, or on containers already displayed on the diagram.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class PapyrusDropOnDiagramMutationRunner {

    private static String query = """
                                    mutation dropOnDiagram($input: DropOnDiagramInput!) {
              dropOnDiagram(input: $input) {
                __typename
                ... on DropOnDiagramSuccessPayload {
                  diagram {
                    id
                    __typename
                  }
                  __typename
                }
                ... on ErrorPayload {
                  message
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
    public PapyrusDropOnDiagramMutationRunner(GraphQL graphQL, ObjectMapper objectMapper) {
        this.graphQL = graphQL;
        this.objectMapper = objectMapper;
    }

    /**
     * Drops the {@code droppedObjectIds} elements on the {@code targetElementId}.
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the elements to drop
     * @param representationId
     *         the representation where the elements are dropped
     * @param targetElementId
     *         the graphical identifier of the target container element
     * @param droppedObjectIds
     *         the semantic identifiers of the elements to drop
     */
    public void dropOnDiagram(String editingContextId, String representationId, String targetElementId, List<String> droppedObjectIds) {
        // Starting position isn't relevant when invoking the tool manually, so we set it to 0
        DropOnDiagramInput dropOnDiagramInput = new DropOnDiagramInput(UUID.randomUUID(), editingContextId, representationId, targetElementId, droppedObjectIds, 0, 0);
        ExecutionInput executionInput = ExecutionInput.newExecutionInput(query)//
                .variables(Map.of("input", this.objectMapper.convertValue(dropOnDiagramInput, new TypeReference<Map<String, Object>>() {
                    /**/
                }))) //
                .build();
        ExecutionResult executionResult = this.graphQL.execute(executionInput);
    }
}

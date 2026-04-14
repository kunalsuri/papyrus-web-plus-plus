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
package org.eclipse.papyrus.web.utils.mutations;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.sirius.components.collaborative.diagrams.dto.DeleteFromDiagramInput;
import org.springframework.stereotype.Service;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import tools.jackson.databind.ObjectMapper;

/**
 * Service used to invoke a semantic deletion tool on one element.
 * <p>
 * This class instantiates and runs the {@code deleteFromDiagram}.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public class PapyrusDeleteFromDiagramMutationRunner {

    private static final String DELETE_FROM_DIAGRAM_QUERY = """
            mutation deleteFromDiagram($input: DeleteFromDiagramInput!) {
              deleteFromDiagram(input: $input) {
                __typename
                ... on DeleteFromDiagramSuccessPayload {
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
    public PapyrusDeleteFromDiagramMutationRunner(GraphQL graphQL, ObjectMapper objectMapper) {
        this.graphQL = graphQL;
        this.objectMapper = objectMapper;
    }

    /**
     * Invokes the semantic deletion tool on the {@code diagramElementId} node.
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the element on which the tool is invoked
     * @param representationId
     *         the representation containing the element
     * @param diagramElementId
     *         the graphical identifier of the element to delete
     */
    public void semanticDeleteNodeFromDiagram(String editingContextId, String representationId, String diagramElementId) {
        this.deleteFromDiagram(editingContextId, representationId, diagramElementId, List.of(diagramElementId), List.of());
    }

    /**
     * Invokes the semantic deletion tool on the {@code diagramElementId} edge.
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the element on which the tool is invoked
     * @param representationId
     *         the representation containing the element
     * @param diagramElementId
     *         the graphical identifier of the element to delete
     */
    public void semanticDeleteEdgeFromDiagram(String editingContextId, String representationId, String diagramElementId) {
        this.deleteFromDiagram(editingContextId, representationId, diagramElementId, List.of(), List.of(diagramElementId));
    }


    private void deleteFromDiagram(String editingContextId, String representationId, String diagramElementId, List<String> nodeIds, List<String> edgeIds) {
        DeleteFromDiagramInput deleteFromDiagramInput = new DeleteFromDiagramInput(UUID.randomUUID(), editingContextId, representationId, nodeIds, edgeIds);

        Map<String, Object> variables = Map.of("input", this.objectMapper.convertValue(deleteFromDiagramInput, new tools.jackson.core.type.TypeReference<Map<String, Object>>() { }));
        ExecutionInput executionInput = ExecutionInput.newExecutionInput(DELETE_FROM_DIAGRAM_QUERY)
                .variables(variables)
                .build();

        ExecutionResult executionResult = this.graphQL.execute(executionInput);
        assertThat(executionResult.getErrors()).isEmpty();
    }

}

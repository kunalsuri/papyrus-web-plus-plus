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

import com.jayway.jsonpath.JsonPath;

import java.util.UUID;

import org.eclipse.sirius.components.collaborative.diagrams.dto.ReconnectEdgeInput;
import org.eclipse.sirius.components.diagrams.events.ReconnectEdgeKind;
import org.eclipse.sirius.components.diagrams.tests.graphql.ReconnectEdgeMutationRunner;
import org.eclipse.sirius.components.graphql.tests.api.GraphQLResult;
import org.springframework.stereotype.Service;

/**
 * Service used to reconnect the source or target of an edge.
 * <p>
 * This class instantiates and runs the {@code reconnectEdge} mutation. Node that this mutation performs both graphical and semantic operations.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class PapyrusReconnectEdgeMutationRunner {

    private ReconnectEdgeMutationRunner runner;

    /**
     * Initializes the runner with the provided {@code graphQL} and {@code objectMapper}.
     */
    public PapyrusReconnectEdgeMutationRunner(ReconnectEdgeMutationRunner runner) {
        this.runner = runner;
    }

    /**
     * Reconnects the source of the {@code edgeId} edge to {@code newEdgeEndId}.
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the elements
     * @param representationId
     *         the representation containing the elements
     * @param edgeId
     *         the graphical identifier of the edge to reconnect the source from
     * @param newEdgeEndId
     *         the graphical identifier of the new source of the edge
     */
    public void reconnectEdgeSource(String editingContextId, String representationId, String edgeId, String newEdgeEndId) {
        this.reconnectEdge(editingContextId, representationId, edgeId, newEdgeEndId, ReconnectEdgeKind.SOURCE);
    }

    /**
     * Reconnects the target of the {@code edgeId} edge to {@code newEdgeEndId}.
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the elements
     * @param representationId
     *         the representation containing the elements
     * @param edgeId
     *         the graphical identifier of the edge to reconnect the target from
     * @param newEdgeEndId
     *         the graphical identifier of the new target of the edge
     */
    public void reconnectEdgeTarget(String editingContextId, String representationId, String edgeId, String newEdgeEndId) {
        this.reconnectEdge(editingContextId, representationId, edgeId, newEdgeEndId, ReconnectEdgeKind.TARGET);
    }

    public void reconnectEdge(String editingContext, String representationId, String edgeId, String newEdgeEndId, ReconnectEdgeKind reconnectEdgeKind) {
        ReconnectEdgeInput reconnectEdgeInput = new ReconnectEdgeInput(UUID.randomUUID(), editingContext.toString(), representationId.toString(), edgeId.toString(), newEdgeEndId.toString(),
                reconnectEdgeKind, 0, 0);
        GraphQLResult result = this.runner.run(reconnectEdgeInput);

        String responseTypeName = JsonPath.read(result.data(), "$.data.reconnectEdge.__typename");
        assertThat(responseTypeName).isEqualTo("SuccessPayload");
    }

}

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

import java.util.List;
import java.util.UUID;

import org.eclipse.sirius.components.collaborative.diagrams.dto.InvokeSingleClickOnDiagramElementToolInput;
import org.eclipse.sirius.components.diagrams.tests.graphql.InvokeSingleClickOnDiagramElementToolMutationRunner;
import org.springframework.stereotype.Service;

/**
 * Service used to invoke a tool on one element.
 * <p>
 * This class instantiates and runs the {@code invokeSingleClickOnDiagramElementTool}. The invoked tool can be retrieved via
 * {@link PapyrusPaletteToolQueryRunner#getTool(UUID, UUID, UUID, String, String)}.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class PapyrusInvokeSingleClickOnDiagramElementToolRunner {

    private InvokeSingleClickOnDiagramElementToolMutationRunner runner;

    /**
     * Initializes the runner with the provided {@code graphQL} and {@code objectMapper}.
     */
    public PapyrusInvokeSingleClickOnDiagramElementToolRunner(InvokeSingleClickOnDiagramElementToolMutationRunner runner) {
        this.runner = runner;
    }

    /**
     * Invokes the {@code toolId} tool on the {@code diagramElementId} element.
     * <p>
     * This method invokes the given tool on a single element, see {@link #invokeTool(UUID, UUID, UUID, List<UUID>, UUID)} to invoke a tool on two elements.
     * </p>
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the element on which the tool is invoked
     * @param representationId
     *         the representation containing the element
     * @param diagramElementIds
     *         the graphical identifiers of the elements on which the tool is invoked
     * @param toolId
     *         the identifier of the tool to invoke
     */
    public void invokeTool(String editingContextId, String representationId, List<String> diagramElementIds, String toolId) {
        // Starting position and selected objects aren't relevant when invoking the tool manually, so we set them to 0
        // and null, respectively
        InvokeSingleClickOnDiagramElementToolInput invokeSingleClickOnDiagramElementToolInput = new InvokeSingleClickOnDiagramElementToolInput(UUID.randomUUID(), editingContextId, representationId,
                diagramElementIds, toolId, 0, 0, List.of());

        String jsonResult = this.runner.run(invokeSingleClickOnDiagramElementToolInput);

        String responseTypeName = JsonPath.read(jsonResult, "$.data.invokeSingleClickOnDiagramElementTool.__typename");
        assertThat(responseTypeName).isEqualTo("InvokeSingleClickOnDiagramElementToolSuccessPayload");
    }

}

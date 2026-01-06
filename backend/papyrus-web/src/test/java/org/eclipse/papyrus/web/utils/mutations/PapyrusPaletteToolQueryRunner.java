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
import java.util.Map;
import java.util.Optional;

import org.eclipse.sirius.components.diagrams.tests.graphql.PaletteQueryRunner;
import org.springframework.stereotype.Service;

import net.minidev.json.JSONArray;

/**
 * Service used to retrieve a palette tool on a given element.
 * <p>
 * This class instantiates and runs the {@code getPalette} query. The content of the palette is then processed to retrieve the desired tool.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class PapyrusPaletteToolQueryRunner {

    private final PaletteQueryRunner runner;

    /**
     * Initializes the runner with the provided {@code graphQL} and {@code objectMapper}.
     */
    public PapyrusPaletteToolQueryRunner(PaletteQueryRunner runner) {
        this.runner = runner;
    }

    /**
     * Returns the raw JSON of the palette associated to the {@code diagramElementId} element.
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the element to retrieve the palette from
     * @param representationId
     *         the representation containing the element
     * @param diagramElementIds
     *         the graphical identifier of the element to retrieve the palette from
     * @return the raw JSON of the palette
     */
    private String getPalette(String editingContextId, String representationId, List<String> diagramElementIds) {
        Map<String, Object> parameters = Map.of("editingContextId", editingContextId, "representationId", representationId, "diagramElementIds", diagramElementIds);
        return this.runner.run(parameters);
    }

    /**
     * Returns the identifier of the {@code toolName} tool in the {@code diagramElementId} element's palette.
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the element to retrieve the palette tool from
     * @param representationId
     *         the representation containing the element
     * @param diagramElementId
     *         the graphical identifier of the element to retrieve the palette tool from
     * @param toolSectionName
     *         the name of the tool section containing the tool
     * @param toolName
     *         the name of the tool
     * @return the identifier of the tool if it exists, or an {@code empty} {@link Optional}
     */
    public Optional<String> getTool(String editingContextId, String representationId, List<String> diagramElementIds, String toolSectionName, String toolName) {
        String rawPalette = this.getPalette(editingContextId, representationId, diagramElementIds);
        Object palette = JsonPath.read(rawPalette,
                "$.data.viewer.editingContext.representation.description.palette.paletteEntries[?(@.label=='" + toolSectionName + "')].tools[?(@.label=='" + toolName + "')]");
        assertThat(palette).isInstanceOf(JSONArray.class);
        Optional<String> result = Optional.empty();
        JSONArray array = (JSONArray) palette;
        if (!array.isEmpty()) {
            assertThat(array).hasSize(1);
            Object toolObject = array.get(0);
            assertThat(toolObject).isInstanceOf(Map.class);
            Map<String, Object> toolMap = (Map<String, Object>) toolObject;
            Object toolIdObject = toolMap.get("id");
            assertThat(toolIdObject).isInstanceOf(String.class);
            result = Optional.of((String) toolIdObject);
        }
        return result;
    }

    /**
     * Returns the identifier of the quick Access tool named {@code toolName} on the {@code diagramElementId} element's palette.
     * <p>
     * This method produces a test failure if the underlying GraphQL query returns an error.
     * </p>
     *
     * @param editingContextId
     *         the project containing the element to retrieve the palette tool from
     * @param representationId
     *         the representation containing the element
     * @param diagramElementId
     *         the graphical identifier of the element to retrieve the palette tool from
     * @param toolName
     *         the name of the tool
     * @return the identifier of the tool if it exists, or an {@code empty} {@link Optional}
     */
    public Optional<String> getQuickAccessTool(String editingContextId, String representationId, List<String> diagramElementIds, String toolName) {
        String rawPalette = this.getPalette(editingContextId, representationId, diagramElementIds);
        Object palette = JsonPath.read(rawPalette,
                "$.data.viewer.editingContext.representation.description.palette.quickAccessTools[?(@.label=='" + toolName + "')]");
        assertThat(palette).isInstanceOf(JSONArray.class);
        Optional<String> result = Optional.empty();
        JSONArray array = (JSONArray) palette;
        if (!array.isEmpty()) {
            assertThat(array).hasSize(1);
            Object toolObject = array.get(0);
            assertThat(toolObject).isInstanceOf(Map.class);
            Map<String, Object> toolMap = (Map<String, Object>) toolObject;
            Object toolIdObject = toolMap.get("id");
            assertThat(toolIdObject).isInstanceOf(String.class);
            result = Optional.of((String) toolIdObject);
        }
        return result;
    }
}

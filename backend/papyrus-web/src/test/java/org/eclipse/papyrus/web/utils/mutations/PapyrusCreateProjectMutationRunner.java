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

import org.eclipse.sirius.web.application.project.dto.CreateProjectInput;
import org.eclipse.sirius.web.tests.graphql.CreateProjectMutationRunner;
import org.springframework.stereotype.Service;

/**
 * Service used to create a project.
 *
 * @author Arthur Daussy
 */
@Service
public class PapyrusCreateProjectMutationRunner {

    private final CreateProjectMutationRunner runner;

    public PapyrusCreateProjectMutationRunner(CreateProjectMutationRunner runner) {
        super();
        this.runner = runner;
    }

    public String createProject(String projectName, String templateId) {

        var input = new CreateProjectInput(UUID.randomUUID(), projectName, templateId, List.of());

        String editingContextId = null;
        var jsonResult = this.runner.run(input);
        String responseTypeName = JsonPath.read(jsonResult, "$.data.createProject.__typename");
        assertThat(responseTypeName).isEqualTo("CreateProjectSuccessPayload");

        editingContextId = JsonPath.read(jsonResult, "$.data.createProject.project.id");
        return editingContextId;
    }

}

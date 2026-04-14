/*******************************************************************************
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
 *******************************************************************************/
package org.eclipse.papyrus.web.application.profile.controllers;

import tools.jackson.databind.ObjectMapper;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.papyrus.web.application.profile.services.api.IUMLProfileService;
import org.eclipse.sirius.components.annotations.spring.graphql.MutationDataFetcher;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;

import graphql.schema.DataFetchingEnvironment;

/**
 * Mutation that help delete publish profile from the database.
 *
 * @author Arthur Daussy
 */
@MutationDataFetcher(type = "Mutation", field = MutationDeletePublishedDynamicProfileDataFetcher.DELETE_PUBLISHED_PROFILE_PROFILE_FIELD)
public class MutationDeletePublishedDynamicProfileDataFetcher implements IDataFetcherWithFieldCoordinates<CompletableFuture<IPayload>> {

    public static final String DELETE_PUBLISHED_PROFILE_PROFILE_FIELD = "deletePublishedDynamicProfileByName";

    private final ObjectMapper objectMapper;

    private final IUMLProfileService umlProfileService;

    public MutationDeletePublishedDynamicProfileDataFetcher(ObjectMapper objectMapper, IUMLProfileService umlProfileService) {
        this.umlProfileService = Objects.requireNonNull(umlProfileService);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public CompletableFuture<IPayload> get(DataFetchingEnvironment environment) throws Exception {
        Object argument = environment.getArgument("input");
        var input = this.objectMapper.convertValue(argument, String.class);

        return CompletableFuture.supplyAsync(() -> this.umlProfileService.deletePublishedDynamicProfileByName(input));
    }

}

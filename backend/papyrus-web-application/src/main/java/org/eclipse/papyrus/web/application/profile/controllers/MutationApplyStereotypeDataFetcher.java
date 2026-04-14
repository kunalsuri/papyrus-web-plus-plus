/*******************************************************************************
 * Copyright (c) 2022, 2026 CEA LIST, Obeo.
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

import org.eclipse.papyrus.web.application.profile.dto.ApplyStereotypeInput;
import org.eclipse.sirius.components.annotations.spring.graphql.MutationDataFetcher;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;
import org.eclipse.sirius.components.graphql.api.IEditingContextDispatcher;

import graphql.schema.DataFetchingEnvironment;
import reactor.core.publisher.Mono;

/**
 * The data fetcher used to apply an UML Stereotype on a UML Element.
 * <p>
 * It will be used to handle the following GraphQL field:
 * </p>
 *
 * <pre>
 * type Mutation {
 *   applyStereotype(input: ApplyStereotypeInput!): ApplyStereotypePayload!
 * }
 * </pre>
 *
 * @author lfasani
 */
@MutationDataFetcher(type = "Mutation", field = MutationApplyStereotypeDataFetcher.APPLY_STEREOTYPE_FIELD)
public class MutationApplyStereotypeDataFetcher implements IDataFetcherWithFieldCoordinates<CompletableFuture<IPayload>> {

    public static final String APPLY_STEREOTYPE_FIELD = "applyStereotype";

    private final ObjectMapper objectMapper;

    private IEditingContextDispatcher editingContextDispatcher;

    public MutationApplyStereotypeDataFetcher(ObjectMapper objectMapper, IEditingContextDispatcher editingContextDispatcher) {
        this.editingContextDispatcher = Objects.requireNonNull(editingContextDispatcher);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public CompletableFuture<IPayload> get(DataFetchingEnvironment environment) throws Exception {
        Object argument = environment.getArgument("input");
        var input = this.objectMapper.convertValue(argument, ApplyStereotypeInput.class);

        Mono<IPayload> defaultIfEmpty = this.editingContextDispatcher.dispatchMutation(input.editingContextId(), input)
                .defaultIfEmpty(new ErrorPayload(input.id(), "Unexpected error"));
        return defaultIfEmpty.log().toFuture();
    }

}

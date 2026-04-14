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
package org.eclipse.papyrus.web.custom.widgets.primitivelist.datafetchers;

import tools.jackson.databind.ObjectMapper;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.papyrus.web.custom.widgets.primitivelist.dto.ActionPrimitiveListItemInput;
import org.eclipse.sirius.components.annotations.spring.graphql.MutationDataFetcher;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;
import org.eclipse.sirius.components.graphql.api.IEditingContextDispatcher;
import org.eclipse.sirius.components.graphql.api.IExceptionWrapper;

import graphql.schema.DataFetchingEnvironment;

/**
 * The data fetcher used for action on a list item.
 * <p>
 * It will be used to handle the following GraphQL field:
 * </p>
 *
 * <pre>
 * type Mutation {
 *   actionPrimitiveListItem(input: ActionListItemInput!): ActionListItemPayload!
 * }
 * </pre>
 *
 * @author Jerome Gout
 */
@MutationDataFetcher(type = "Mutation", field = "actionPrimitiveListItem")
public class MutationActionPrimitiveListItemDataFetcher implements IDataFetcherWithFieldCoordinates<CompletableFuture<IPayload>> {

    private static final String INPUT_ARGUMENT = "input";

    private final ObjectMapper objectMapper;

    private final IExceptionWrapper exceptionWrapper;

    private final IEditingContextDispatcher editingContextDispatcher;

    public MutationActionPrimitiveListItemDataFetcher(ObjectMapper objectMapper, IExceptionWrapper exceptionWrapper, IEditingContextDispatcher editingContextDispatcher) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.exceptionWrapper = Objects.requireNonNull(exceptionWrapper);
        this.editingContextDispatcher = Objects.requireNonNull(editingContextDispatcher);
    }

    @Override
    public CompletableFuture<IPayload> get(DataFetchingEnvironment environment) throws Exception {
        Object argument = environment.getArgument(INPUT_ARGUMENT);

        ActionPrimitiveListItemInput input = this.objectMapper.convertValue(argument, ActionPrimitiveListItemInput.class);

        return this.exceptionWrapper.wrapMono(() -> this.editingContextDispatcher.dispatchMutation(input.editingContextId(), input), input).toFuture();

    }

}

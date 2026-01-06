/*****************************************************************************
 * Copyright (c) 2026 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.application.explorer;

import org.eclipse.papyrus.web.application.explorer.builder.UMLDefaultTreeDescriptionBuilder;
import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.api.IRepresentationMetadataProvider;
import org.eclipse.sirius.components.core.api.IURLParser;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.web.application.views.explorer.ExplorerEventProcessorFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides the metadata for the Papyrus UML Explorer.
 *
 * @author ntinsalhi
 */
@Service
public class UMLExplorerTreeRepresentationMetadataProvider implements IRepresentationMetadataProvider {

    private final IURLParser urlParser;

    private final UMLDefaultTreeExplorerInstaller umlDefaultTreeExplorerInstaller;

    public UMLExplorerTreeRepresentationMetadataProvider(IURLParser urlParser, UMLDefaultTreeExplorerInstaller umlDefaultTreeExplorerInstaller) {
        this.urlParser = Objects.requireNonNull(urlParser);
        this.umlDefaultTreeExplorerInstaller = Objects.requireNonNull(umlDefaultTreeExplorerInstaller);
    }

    @Override
    public Optional<RepresentationMetadata> getMetadata(String representationId) {
        var viewTreeDescriptionId = this.umlDefaultTreeExplorerInstaller.getDescriptionId();
        if (this.getTreeDescriptionIdFromRepresentationId(representationId).equals(viewTreeDescriptionId)) {
            var representationMetadata = RepresentationMetadata.newRepresentationMetadata(representationId)
                    .kind(Tree.KIND)
                    .label(UMLDefaultTreeDescriptionBuilder.UML_EXPLORER)
                    .descriptionId(viewTreeDescriptionId)
                    .iconURLs(List.of("/explorer/explorer.svg"))
                    .build();

            return Optional.of(representationMetadata);
        }
        return Optional.empty();
    }

    private String getTreeDescriptionIdFromRepresentationId(String representationId) {
        if (representationId.indexOf(ExplorerEventProcessorFactory.TREE_DESCRIPTION_ID_PARAMETER) > 0) {
            Map<String, List<String>> parameters = this.urlParser.getParameterValues(representationId);
            return parameters.get(ExplorerEventProcessorFactory.TREE_DESCRIPTION_ID_PARAMETER).get(0);
        }
        return "";
    }
}

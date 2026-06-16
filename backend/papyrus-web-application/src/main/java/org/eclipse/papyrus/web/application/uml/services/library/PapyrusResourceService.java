/*****************************************************************************
 * Copyright (c) 2026 Obeo, CEA LIST.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.web.application.uml.services.library;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.web.application.uml.services.library.api.IPapyrusResourceService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.library.services.LibraryMetadataAdapter;
import org.eclipse.sirius.web.domain.boundedcontexts.library.Library;
import org.eclipse.sirius.web.domain.boundedcontexts.library.services.api.ILibrarySearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.SemanticData;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * The default Papyrus implementation for {@link IPapyrusResourceService}.
 *
 * Adapted from SysONResourceService
 *
 * @author Vincent LORENZO
 */
@Service
public class PapyrusResourceService implements IPapyrusResourceService {

    private final ILibrarySearchService librarySearchService;

    public PapyrusResourceService(ILibrarySearchService librarySearchService) {
        this.librarySearchService = Objects.requireNonNull(librarySearchService);
    }

    @Override
    public boolean isUML(Resource resource) {
        final boolean isUMLResource = resource.eAdapters()
                .stream()
                .filter(ResourceMetadataAdapter.class::isInstance)
                .map(ResourceMetadataAdapter.class::cast)
                .findFirst()
                .map(ResourceMetadataAdapter::getName)
                .filter(name -> name.toLowerCase().endsWith(".uml"))
                .isPresent();
        return isUMLResource;
    }

    @Override
    public boolean isImported(IEditingContext editingContext, Resource resource) {
        Objects.requireNonNull(resource);
        boolean res = this.isUMLStandardLibrary(resource) || this.isFromReferencedLibrary(editingContext, resource);
        return res;
    }

    @Override
    public boolean isFromReferencedLibrary(IEditingContext editingContext, Resource resource) {
        Objects.requireNonNull(editingContext);
        Objects.requireNonNull(resource);
        boolean result = false;

        Optional<Library> optionalEditingContextLibrary = new UUIDParser().parse(editingContext.getId())
                .map(AggregateReference::<SemanticData, UUID>to)
                .flatMap(this.librarySearchService::findBySemanticData);
        if (optionalEditingContextLibrary.isPresent()) {
            // The library is not from a referenced library if it is the library defined in the editing context.
            // This happens when visualizing a library: the content of the library is considered as a library by Sirius
            // Web but it isn't imported from anywhere, it is defined in the current editing context.
            Library editingContextLibrary = optionalEditingContextLibrary.get();
            result = resource.eAdapters().stream()
                    .filter(LibraryMetadataAdapter.class::isInstance)
                    .map(LibraryMetadataAdapter.class::cast)
                    .findFirst()
                    .map(libraryAdapter -> !this.isLibrary(libraryAdapter, editingContextLibrary))
                    .orElse(false);
        } else {
            result = resource.eAdapters().stream()
                    .anyMatch(LibraryMetadataAdapter.class::isInstance);
        }

        return result;
    }

    private boolean isLibrary(LibraryMetadataAdapter libraryMetadataAdapter, Library library) {
        return Objects.equals(libraryMetadataAdapter.getNamespace(), library.getNamespace())
                && Objects.equals(libraryMetadataAdapter.getName(), library.getName())
                && Objects.equals(libraryMetadataAdapter.getVersion(), library.getVersion());
    }

    public boolean isUMLStandardLibrary(Resource resource) {
        final boolean isSysMLResource = resource.eAdapters()
                .stream()
                .filter(ResourceMetadataAdapter.class::isInstance)
                .map(ResourceMetadataAdapter.class::cast)
                .findFirst()
                .map(ResourceMetadataAdapter::getName)
                .filter(name -> name.toLowerCase().endsWith("library.uml"))
                .isPresent();
        return isSysMLResource;
    }
}

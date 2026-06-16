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
import java.util.UUID;

import org.eclipse.sirius.components.events.ICause;

/**
 * Used to indicate that the creation of a UML library has been requested.
 *
 * Adapted from SysONPublishedLibrarySemanticDataCreationRequested.
 *
 * @author Vincent LORENZO
 */
public record UMLLibrarySemanticDataCreationRequested(
        UUID id,
        ICause causedBy,
        String libraryNamespace,
        String libraryName,
        String libraryVersion,
        String libraryDescription) implements ICause {

    public UMLLibrarySemanticDataCreationRequested(final ICause cause, final String libraryNamespace, final String libraryName, final String libraryVersion,
            final String libraryDescription) {
        this(UUID.randomUUID(), cause, libraryNamespace, libraryName, libraryVersion, libraryDescription);
    }

    public UMLLibrarySemanticDataCreationRequested {
        Objects.requireNonNull(id);
        Objects.requireNonNull(causedBy);
        Objects.requireNonNull(libraryNamespace);
        Objects.requireNonNull(libraryName);
        Objects.requireNonNull(libraryVersion);
        Objects.requireNonNull(libraryDescription);
    }

}
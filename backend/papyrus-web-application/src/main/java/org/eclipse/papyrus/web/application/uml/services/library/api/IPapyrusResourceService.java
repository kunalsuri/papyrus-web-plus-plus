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
package org.eclipse.papyrus.web.application.uml.services.library.api;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.core.api.IEditingContext;

/**
 * Papyrus services related to EMF {@link Resource}.
 *
 * Adapted from ISysONResourceService
 *
 * @author Vincent LORENZO
 */
public interface IPapyrusResourceService {

    boolean isUML(Resource resource);

    boolean isImported(IEditingContext editingContext, Resource resource);

    boolean isFromReferencedLibrary(IEditingContext editingContext, Resource resource);
}

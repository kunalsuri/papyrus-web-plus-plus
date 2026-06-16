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
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.web.application.studio.services.library.api.DependencyGraph;

/**
 * Collects the dependencies between UML libraries.
 *
 * Adapted from ISysONLibraryDependencyCollector
 *
 * @author Vincent LORENZO
 */
public interface IPapyrusLibraryDependencyCollector {

    DependencyGraph<Resource> collectDependencies(ResourceSet resourceSet);

}

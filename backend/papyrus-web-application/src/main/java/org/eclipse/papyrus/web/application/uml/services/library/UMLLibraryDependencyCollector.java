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

import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.papyrus.web.application.uml.services.library.api.IPapyrusLibraryDependencyCollector;
import org.eclipse.sirius.web.application.library.services.LibraryMetadataAdapter;
import org.eclipse.sirius.web.application.studio.services.library.api.DependencyGraph;
import org.springframework.stereotype.Service;

/**
 * Collects the dependencies between UML libraries.
 *
 * Adapted from SysONLibraryDependencyCollector
 *
 * @author Vincent LORENZO
 */
@Service
public class UMLLibraryDependencyCollector implements IPapyrusLibraryDependencyCollector {

    @Override
    public DependencyGraph<Resource> collectDependencies(ResourceSet resourceSet) {
        DependencyGraph<Resource> dependencyGraph = new DependencyGraph<>();
        for (Resource resource : resourceSet.getResources()) {
            if (this.isLibrary(resource)) {
                resource.getAllContents().forEachRemaining(eObject -> {
                    ECrossReferenceAdapter.getCrossReferenceAdapter(eObject).getInverseReferences(eObject)
                            .forEach(setting -> {
                                if (setting.getEStructuralFeature() instanceof EReference reference
                                        && !reference.isContainment()) {
                                    Resource dependentResource = setting.getEObject().eResource();
                                    if (dependentResource != null && resource != dependentResource) {
                                        // implicit have null resource
                                        dependencyGraph.addEdge(dependentResource, resource);
                                    }
                                }
                            });
                });
            }
        }
        return dependencyGraph;
    }

    private boolean isLibrary(Resource resource) {
        return resource.eAdapters().stream()
                .anyMatch(LibraryMetadataAdapter.class::isInstance);
    }
}

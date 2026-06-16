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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.papyrus.web.application.uml.services.library.api.IPapyrusCapableEditingContextPredicate;
import org.eclipse.sirius.components.collaborative.omnibox.api.IWorkbenchOmniboxCommandProvider;
import org.eclipse.sirius.components.collaborative.omnibox.dto.OmniboxCommand;
import org.springframework.stereotype.Service;

/**
 *
 * This command provider allows to publish a Library.
 *
 * Adapted from StudioPublicationCommandProvider
 *
 * @author Vincent LORENZO
 *
 */
@Service
public class UMLLibraryPublicationCommandProvider implements IWorkbenchOmniboxCommandProvider {

    public static final String PUBLISH_UML_MODEL_COMMAND_ID = "publishUMLModel";

    private final IPapyrusCapableEditingContextPredicate papyrusPredicate;

    public UMLLibraryPublicationCommandProvider(final IPapyrusCapableEditingContextPredicate predicate) {
        this.papyrusPredicate = predicate;
    }

    @Override
    public List<OmniboxCommand> getCommands(String editingContextId, List<String> selectedObjectIds, String query) {
        List<OmniboxCommand> result = new ArrayList<>();
        if (this.papyrusPredicate.test(editingContextId)) {
            result.add(new OmniboxCommand(PUBLISH_UML_MODEL_COMMAND_ID, "Publish UML Model", List.of("/omnibox/publish.svg"), "Publish the UML Model/Package as Library"));
        }
        return result;
    }

}

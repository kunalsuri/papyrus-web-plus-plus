/*****************************************************************************
 * Copyright (c) 2024, 2026 CEA LIST, Obeo.
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.sirius.components.collaborative.trees.api.ITreeFilterProvider;
import org.eclipse.sirius.components.collaborative.trees.api.TreeFilter;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.springframework.stereotype.Service;

/**
 * Provides custom tree filter for Papyrus Web.
 *
 * @author Arthur Daussy
 */
@Service
public class PapyrusTreeFilterProvider implements ITreeFilterProvider {

    public static final String HIDE_PATHMAP_URI_TREE_ITEM_FILTER_ID = UUID.nameUUIDFromBytes("Papyrus.HideResourceReadonlyResources".getBytes()).toString();

    @Override
    public List<TreeFilter> get(String editingContextId, TreeDescription treeDescription) {
        List<TreeFilter> filters = new ArrayList<>();
        filters.add(new TreeFilter(HIDE_PATHMAP_URI_TREE_ITEM_FILTER_ID, "Hide read only resources", true));
        return filters;
    }
}

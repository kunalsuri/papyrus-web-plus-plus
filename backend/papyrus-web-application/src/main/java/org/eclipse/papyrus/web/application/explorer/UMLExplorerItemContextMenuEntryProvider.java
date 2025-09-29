/*******************************************************************************
 * Copyright (c) 2025 CEA LIST.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.web.application.explorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.components.collaborative.trees.api.ITreeItemContextMenuEntryProvider;
import org.eclipse.sirius.components.collaborative.trees.dto.ITreeItemContextMenuEntry;
import org.eclipse.sirius.components.collaborative.trees.dto.SingleClickTreeItemContextMenuEntry;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.web.application.views.explorer.services.ExplorerDescriptionProvider;
import org.springframework.stereotype.Service;

/**
 * {@link ITreeItemContextMenuEntryProvider} in charge of contributing contextual menu in the UML default tree explorer.
 *
 * @author Arthur Daussy
 */
@Service
public class UMLExplorerItemContextMenuEntryProvider implements ITreeItemContextMenuEntryProvider {

    private final UMLDefaultTreeExplorerInstaller umlDefaultTreeInstaller;

    public UMLExplorerItemContextMenuEntryProvider(UMLDefaultTreeExplorerInstaller umlDefaultTreeInstaller) {
        this.umlDefaultTreeInstaller = Objects.requireNonNull(umlDefaultTreeInstaller);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, TreeDescription treeDescription, Tree tree,
            TreeItem treeItem) {
        return tree.getId().startsWith(ExplorerDescriptionProvider.PREFIX) && Objects.equals(tree.getDescriptionId(),
                umlDefaultTreeInstaller.getDescriptionId());
    }

    @Override
    public List<ITreeItemContextMenuEntry> getTreeItemContextMenuEntries(IEditingContext editingContext,
            TreeDescription treeDescription, Tree tree, TreeItem treeItem) {
        List<ITreeItemContextMenuEntry> result = new ArrayList<>();
        if (treeItem.isHasChildren()) {
            result.add(new SingleClickTreeItemContextMenuEntry("expandAll", "", List.of(), false));
        }
        return result;
    }
}

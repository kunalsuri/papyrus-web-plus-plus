/*****************************************************************************
 * Copyright (c) 2024 Obeo an others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *  CEA List - Customization Papyrus Web
 *****************************************************************************/
package org.eclipse.papyrus.web.application.explorer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.papyrus.web.application.explorer.builder.UMLDefaultTreeDescriptionBuilder;
import org.eclipse.sirius.components.collaborative.trees.api.IExpandAllTreePathProvider;
import org.eclipse.sirius.components.collaborative.trees.dto.ExpandAllTreePathInput;
import org.eclipse.sirius.components.collaborative.trees.dto.ExpandAllTreePathSuccessPayload;
import org.eclipse.sirius.components.collaborative.trees.dto.TreePath;
import org.eclipse.sirius.components.collaborative.trees.services.api.ITreeNavigationService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.representations.IRepresentationRenderVariableCustomizer;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.components.trees.renderer.TreeRenderer;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.PackageImport;
import org.springframework.stereotype.Service;

/**
 * Custom {@link IExpandAllTreePathProvider} that handle {@link UMLDefaultTreeDescriptionBuilder#UML_EXPLORER} to avoid
 * expanding {@link PackageImport}, {@link ElementImport} and {@link ImportedElementTreeItem}.
 *
 * @author Obeo
 */
// Copy of org.eclipse.sirius.components.collaborative.trees.handlers.DefaultExpandAllTreePathHandler with the
// customization that prevent expanding PackageImport
@Service
public class UMLDefaultExplorerExpandAllPathProvider implements IExpandAllTreePathProvider {

    private static final int MAX_EXPAND_DEPTH_INCREASE = 100;

    private final ITreeNavigationService treeNavigationService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private UMLDefaultTreeExplorerInstaller umlDefaultExplorerInstaller;

    private final List<IRepresentationRenderVariableCustomizer> renderVariableCustomizers;

    public UMLDefaultExplorerExpandAllPathProvider(ITreeNavigationService treeNavigationService,
            IRepresentationDescriptionSearchService representationDescriptionSearchService, UMLDefaultTreeExplorerInstaller umlDefaultExplorerInstaller,
            List<IRepresentationRenderVariableCustomizer> renderVariableCustomizers) {
        this.treeNavigationService = treeNavigationService;
        this.representationDescriptionSearchService = representationDescriptionSearchService;
        this.umlDefaultExplorerInstaller = umlDefaultExplorerInstaller;
        this.renderVariableCustomizers = Objects.requireNonNull(renderVariableCustomizers);
    }

    @Override
    public boolean canHandle(Tree tree) {
        return this.umlDefaultExplorerInstaller.getDescriptionId().equals(tree.getDescriptionId());
    }

    @Override
    public IPayload handle(IEditingContext editingContext, Tree tree, ExpandAllTreePathInput input) {
        int maxDepth = 0;
        String treeItemId = input.treeItemId();

        Set<String> treeItemIdsToExpand = new LinkedHashSet<>();
        // We need to get the current depth of the tree item
        var itemAncestors = this.treeNavigationService.getAncestors(editingContext, tree, treeItemId);
        maxDepth = itemAncestors.size();
        var optionalTreeDescription = this.getTreeDescription(editingContext, tree.getDescriptionId());
        if (optionalTreeDescription.isPresent()) {
            int index = this.computeIndexOf(treeItemId, tree.getChildren());
            var variableManager = new VariableManager();
            variableManager.put(TreeRenderer.INDEX, index);
            variableManager.put(TreeRenderer.ANCESTOR_IDS, itemAncestors);
            variableManager.put(IEditingContext.EDITING_CONTEXT, editingContext);
            variableManager.put(TreeDescription.ID, treeItemId);
            for (var renderVariableCustomizer : this.renderVariableCustomizers) {
                variableManager = renderVariableCustomizer.customize(optionalTreeDescription.get(), variableManager);
            }
            maxDepth = this.addAllContents(optionalTreeDescription.get(), treeItemId, maxDepth, treeItemIdsToExpand, maxDepth, variableManager);
        }
        return new ExpandAllTreePathSuccessPayload(input.id(), new TreePath(treeItemIdsToExpand.stream().toList(), maxDepth));
    }

    private int computeIndexOf(String treeItemId, List<TreeItem> children) {
        int index = -1;
        for (int currentIndex = 0; currentIndex < children.size() && index < 0; currentIndex++) {
            TreeItem currentItem = children.get(currentIndex);
            if (treeItemId.equals(currentItem.getId())) {
                index = currentIndex;
            } else {
                index = this.computeIndexOf(treeItemId, currentItem.getChildren());
            }
        }
        return index;
    }

    private int addAllContents(TreeDescription treeDescription, String treeItemId, int depth, Set<String> treeItemIdsToExpand, int startingDepth, VariableManager variableManager) {
        var depthConsidered = depth;
        if (depthConsidered - startingDepth < MAX_EXPAND_DEPTH_INCREASE) {

            var optionalObject = this.getTreeItemObject(treeDescription, variableManager);
            treeItemIdsToExpand.add(treeItemId);
            variableManager.put(TreeRenderer.EXPANDED, treeItemIdsToExpand.stream().toList());
            if (optionalObject.isPresent() && !this.isElementImport(optionalObject.get())) { // <-- Customization here
                Object object = optionalObject.get();
                variableManager.put(VariableManager.SELF, object);
                if (this.hasChildren(treeDescription, variableManager)) {

                    List<?> children = this.getChildren(treeDescription, variableManager);
                    int index = 0;
                    for (var child : children) {
                        VariableManager childVariableManager = variableManager.createChild();
                        childVariableManager.put(TreeRenderer.INDEX, index++);
                        List<String> ancestors = new ArrayList<String>(childVariableManager.get(TreeRenderer.ANCESTOR_IDS, List.class).orElse(List.of()));
                        ancestors.add(treeItemId);
                        childVariableManager.put(TreeRenderer.ANCESTOR_IDS, ancestors);
                        var optionalChildId = this.getTreeItemId(treeDescription, child);
                        if (optionalChildId.isPresent()) {
                            childVariableManager.put(TreeDescription.ID, optionalChildId.get());
                            var childTreePathMaxDepth = depth + 1;
                            childTreePathMaxDepth = this.addAllContents(treeDescription, optionalChildId.get(), childTreePathMaxDepth, treeItemIdsToExpand, startingDepth, childVariableManager);
                            depthConsidered = Math.max(depthConsidered, childTreePathMaxDepth);
                        }
                    }
                } else {
                    depthConsidered = Math.max(depthConsidered, depth + 1);
                }
            } else {
                depthConsidered = Math.max(depthConsidered, depth + 1);
            }
        }
        return depthConsidered;
    }

    private boolean isElementImport(Object object) {
        return (object instanceof PackageImport) || (object instanceof ElementImport)
                || object instanceof ImportedElementTreeItem;
    }

    private boolean hasChildren(TreeDescription treeDescription, VariableManager variableManager) {
        return treeDescription.getHasChildrenProvider().apply(variableManager);
    }

    private List<?> getChildren(TreeDescription treeDescription, VariableManager variableManager) {
        return treeDescription.getChildrenProvider().apply(variableManager);
    }

    private Optional<String> getTreeItemId(TreeDescription treeDescription, Object object) {
        var variableManager = new VariableManager();
        variableManager.put(VariableManager.SELF, object);
        return Optional.of(treeDescription.getTreeItemIdProvider().apply(variableManager));
    }

    private Optional<Object> getTreeItemObject(TreeDescription treeDescription, VariableManager variableManager) {
        return Optional.ofNullable(treeDescription.getTreeItemObjectProvider().apply(variableManager));
    }

    private Optional<TreeDescription> getTreeDescription(IEditingContext editingContext, String descriptionId) {
        return this.representationDescriptionSearchService.findById(editingContext, descriptionId)
                .filter(TreeDescription.class::isInstance)
                .map(TreeDescription.class::cast);
    }

}

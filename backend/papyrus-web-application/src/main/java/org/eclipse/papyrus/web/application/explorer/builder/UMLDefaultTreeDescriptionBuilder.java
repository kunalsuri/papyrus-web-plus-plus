/*****************************************************************************
 * Copyright (c) 2024 CEA LIST.
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
 *****************************************************************************/
package org.eclipse.papyrus.web.application.explorer.builder;

import java.util.UUID;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.IDAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.trees.renderer.TreeRenderer;
import org.eclipse.sirius.components.view.TextStyleDescription;
import org.eclipse.sirius.components.view.TextStylePalette;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.tree.TreeBuilders;
import org.eclipse.sirius.components.view.builder.generated.tree.TreeDescriptionBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.tree.TreeDescription;
import org.eclipse.sirius.components.view.tree.TreeItemLabelDescription;
import org.eclipse.sirius.components.view.tree.TreeItemLabelFragmentDescription;
import org.eclipse.sirius.emfjson.resource.JsonResource;

/**
 * Builder of the {@link TreeDescription} to be used in a UML editing context.
 *
 * @author Arthur Daussy
 */
public class UMLDefaultTreeDescriptionBuilder {

    /**
     * Name of the Tree.
     */
    public static final String UML_EXPLORER = "UML Default Explorer";

    private TextStyleDescription stereotypeApplicationLabelFragmentDescription;

    private TextStyleDescription elementBaseLabelFragmentDescription;

    public View createView() {

        var domainTextStylePalette = this.createTextStylePalette();

        var umlDefaultTreeDescription = this.build();

        var umlDefaultTreeView = new ViewBuilders()
                .newView()
                .descriptions(umlDefaultTreeDescription)
                .textStylePalettes(domainTextStylePalette)
                .build();

        // Generate stable ids
        umlDefaultTreeView.eAllContents().forEachRemaining(eObject -> {
            var id = UUID.nameUUIDFromBytes(EcoreUtil.getURI(eObject).toString().getBytes());
            eObject.eAdapters().add(new IDAdapter(id));
        });

        UUID resourceId = UUID.nameUUIDFromBytes(UMLDefaultTreeDescriptionBuilder.UML_EXPLORER.getBytes());
        String resourcePath = resourceId.toString();
        JsonResource resource = new JSONResourceFactory().createResourceFromPath(resourcePath);
        resource.eAdapters().add(new ResourceMetadataAdapter(UMLDefaultTreeDescriptionBuilder.UML_EXPLORER));
        resource.getContents().add(umlDefaultTreeView);

        return umlDefaultTreeView;
    }

    private TextStylePalette createTextStylePalette() {
        return new ViewBuilders()
                .newTextStylePalette()
                .name("UML Text Style Palette")
                .styles(this.createStereotypeApplicationLabelStyle(), this.createMainElementLabelStyle())
                .build();
    }

    private TreeDescription build() {

        return new TreeDescriptionBuilder()
                .name(UML_EXPLORER)
                .childrenExpression("aql:self.getChildrenItems(editingContext,expanded, " + TreeRenderer.ANCESTOR_IDS + "," + TreeRenderer.INDEX + ", existingRepresentations)")
                .deletableExpression("aql:self.canBeDeleted()")
                .editableExpression("aql:self.canBeRenamed()")
                .elementsExpression("aql:editingContext.getRootElements(activeFilterIds)")
                .hasChildrenExpression("aql:self.hasChildren(editingContext," + TreeRenderer.ANCESTOR_IDS + "," + TreeRenderer.INDEX + ", existingRepresentations)")
                .treeItemIconExpression("aql:self.getIconURLs()")
                .kindExpression("aql:self.getItemKind()")
                .parentExpression("aql:self.getParentItem(id,editingContext)")
                .preconditionExpression("aql:false")
                .selectableExpression("aql:true")
                .titleExpression(UML_EXPLORER)
                .treeItemIdExpression("aql:self.getItemId()")
                .treeItemObjectExpression("aql:id.toObject(editingContext)")
                .treeItemLabelDescriptions(this.createElementLabelDescription(), this.createImportedElementLabelDescription(), this.createDefaultStyle())
                .build();
    }

    private TextStyleDescription createStereotypeApplicationLabelStyle() {
        this.stereotypeApplicationLabelFragmentDescription = new ViewBuilders().newTextStyleDescription()
                .name("Stereotype application style description")
                .foregroundColorExpression("aql:self.getStereotypeApplicationLabelColor()")
                .build();
        return this.stereotypeApplicationLabelFragmentDescription;
    }

    private TextStyleDescription createMainElementLabelStyle() {
        this.elementBaseLabelFragmentDescription = new ViewBuilders().newTextStyleDescription()
                .name("Element style description")
                .isUnderlineExpression("aql:self.isStatic()")
                .isItalicExpression("aql:self.isAbstract()")
                .foregroundColorExpression("aql:self.getMainLabelColor()")
                .build();
        return this.elementBaseLabelFragmentDescription;
    }

    private TreeItemLabelDescription createElementLabelDescription() {
        return new TreeBuilders()
                .newTreeItemLabelDescription()
                .name("Element Label description")
                .preconditionExpression("aql:self.oclIsKindOf(uml::Element)")
                .children(this.createStereotypeApplicationFragmentLabelDescription(), this.createElementMainLabelFragmentDescription())
                .build();
    }

    private TreeItemLabelDescription createImportedElementLabelDescription() {
        return new TreeBuilders()
                .newTreeItemLabelDescription()
                .name("Imported elements label description")
                .preconditionExpression("aql:self.isImportedElementItem()")
                .children(this.createStereotypeApplicationFragmentLabelDescription(), this.createElementMainLabelFragmentDescription())
                .build();
    }

    private TreeItemLabelDescription createDefaultStyle() {
        return new TreeBuilders()
                .newTreeItemLabelDescription()
                .name("Default style")
                .preconditionExpression("aql:true")
                .children(this.getDefaultLabelFragmentDescription())
                .build();
    }

    private TreeItemLabelFragmentDescription createElementMainLabelFragmentDescription() {
        return new TreeBuilders().newTreeItemLabelFragmentDescription()
                .labelExpression("aql:self.getItemLabel()")
                .style(this.elementBaseLabelFragmentDescription)
                .build();
    }

    private TreeItemLabelFragmentDescription getDefaultLabelFragmentDescription() {
        return new TreeBuilders().newTreeItemLabelFragmentDescription()
                .labelExpression("aql:self.getItemLabel()")
                .build();
    }

    private TreeItemLabelFragmentDescription createStereotypeApplicationFragmentLabelDescription() {
        return new TreeBuilders().newTreeItemLabelFragmentDescription()
                .labelExpression("aql:self.getAppliedStereotypesLabel()")
                .style(this.stereotypeApplicationLabelFragmentDescription)
                .build();
    }

}

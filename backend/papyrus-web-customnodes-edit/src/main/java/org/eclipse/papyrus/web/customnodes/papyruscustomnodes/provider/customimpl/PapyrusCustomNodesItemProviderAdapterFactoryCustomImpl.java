/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.customnodes.papyruscustomnodes.provider.customimpl;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.provider.PapyrusCustomNodesItemProviderAdapterFactory;

/**
 * Custom implementation of {@link PapyrusCustomNodesItemProviderAdapterFactory}.
 *
 * @author Jerome Gout
 */
public class PapyrusCustomNodesItemProviderAdapterFactoryCustomImpl extends PapyrusCustomNodesItemProviderAdapterFactory {

    @Override
    public Adapter createCustomImageNodeStyleDescriptionAdapter() {
        if (this.customImageNodeStyleDescriptionItemProvider == null) {
            this.customImageNodeStyleDescriptionItemProvider = new CustomImageNodeStyleDescriptionItemProviderCustomImpl(this);
        }
        return this.customImageNodeStyleDescriptionItemProvider;
    }

    @Override
    public Adapter createCuboidNodeStyleDescriptionAdapter() {
        if (this.cuboidNodeStyleDescriptionItemProvider == null) {
            this.cuboidNodeStyleDescriptionItemProvider = new CuboidNodeStyleDescriptionItemProviderCustomImpl(this);
        }
        return this.cuboidNodeStyleDescriptionItemProvider;
    }

    @Override
    public Adapter createNoteNodeStyleDescriptionAdapter() {
        if (this.noteNodeStyleDescriptionItemProvider == null) {
            this.noteNodeStyleDescriptionItemProvider = new NoteNodeStyleDescriptionItemProviderCustomImpl(this);
        }

        return this.noteNodeStyleDescriptionItemProvider;
    }

    @Override
    public Adapter createPackageNodeStyleDescriptionAdapter() {
        if (this.packageNodeStyleDescriptionItemProvider == null) {
            this.packageNodeStyleDescriptionItemProvider = new PackageNodeStyleDescriptionItemProviderCustomImpl(this);
        }

        return this.packageNodeStyleDescriptionItemProvider;
    }

    @Override
    public Adapter createRectangleWithExternalLabelNodeStyleDescriptionAdapter() {
        if (this.rectangleWithExternalLabelNodeStyleDescriptionItemProvider == null) {
            this.rectangleWithExternalLabelNodeStyleDescriptionItemProvider = new RectangleWithExternalLabelNodeStyleDescriptionItemProviderCustomImpl(this);
        }

        return this.rectangleWithExternalLabelNodeStyleDescriptionItemProvider;
    }

    @Override
    public Adapter createInnerFlagNodeStyleDescriptionAdapter() {
        if (this.innerFlagNodeStyleDescriptionItemProvider == null) {
            this.innerFlagNodeStyleDescriptionItemProvider = new InnerFlagNodeStyleDescriptionItemProviderCustomImpl(this);
        }
        return this.innerFlagNodeStyleDescriptionItemProvider;
    }

    @Override
    public Adapter createOuterFlagNodeStyleDescriptionAdapter() {
        if (this.outerFlagNodeStyleDescriptionItemProvider == null) {
            this.outerFlagNodeStyleDescriptionItemProvider = new OuterFlagNodeStyleDescriptionItemProviderCustomImpl(this);
        }
        return this.outerFlagNodeStyleDescriptionItemProvider;
    }
}

/**
 * Copyright (c) 2023, 2024 CEA LIST, Obeo
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Obeo - Initial API and implementation
 */
package org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.InnerFlagNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.OuterFlagNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesFactory;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 *
 * @generated
 */
public class PapyrusCustomNodesFactoryImpl extends EFactoryImpl implements PapyrusCustomNodesFactory {
    /**
     * Creates the default factory implementation. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static PapyrusCustomNodesFactory init() {
        try {
            PapyrusCustomNodesFactory thePapyrusCustomNodesFactory = (PapyrusCustomNodesFactory) EPackage.Registry.INSTANCE.getEFactory(PapyrusCustomNodesPackage.eNS_URI);
            if (thePapyrusCustomNodesFactory != null) {
                return thePapyrusCustomNodesFactory;
            }
        } catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new PapyrusCustomNodesFactoryImpl();
    }

    /**
     * Creates an instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public PapyrusCustomNodesFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case PapyrusCustomNodesPackage.PACKAGE_NODE_STYLE_DESCRIPTION:
                return this.createPackageNodeStyleDescription();
            case PapyrusCustomNodesPackage.RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION:
                return this.createRectangleWithExternalLabelNodeStyleDescription();
            case PapyrusCustomNodesPackage.NOTE_NODE_STYLE_DESCRIPTION:
                return this.createNoteNodeStyleDescription();
            case PapyrusCustomNodesPackage.INNER_FLAG_NODE_STYLE_DESCRIPTION:
                return this.createInnerFlagNodeStyleDescription();
            case PapyrusCustomNodesPackage.OUTER_FLAG_NODE_STYLE_DESCRIPTION:
                return this.createOuterFlagNodeStyleDescription();
            case PapyrusCustomNodesPackage.CUBOID_NODE_STYLE_DESCRIPTION:
                return this.createCuboidNodeStyleDescription();
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION:
                return this.createCustomImageNodeStyleDescription();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public PackageNodeStyleDescription createPackageNodeStyleDescription() {
        PackageNodeStyleDescriptionImpl packageNodeStyleDescription = new PackageNodeStyleDescriptionImpl();
        return packageNodeStyleDescription;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public RectangleWithExternalLabelNodeStyleDescription createRectangleWithExternalLabelNodeStyleDescription() {
        RectangleWithExternalLabelNodeStyleDescriptionImpl rectangleWithExternalLabelNodeStyleDescription = new RectangleWithExternalLabelNodeStyleDescriptionImpl();
        return rectangleWithExternalLabelNodeStyleDescription;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public NoteNodeStyleDescription createNoteNodeStyleDescription() {
        NoteNodeStyleDescriptionImpl noteNodeStyleDescription = new NoteNodeStyleDescriptionImpl();
        return noteNodeStyleDescription;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public InnerFlagNodeStyleDescription createInnerFlagNodeStyleDescription() {
        InnerFlagNodeStyleDescriptionImpl innerFlagNodeStyleDescription = new InnerFlagNodeStyleDescriptionImpl();
        return innerFlagNodeStyleDescription;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public OuterFlagNodeStyleDescription createOuterFlagNodeStyleDescription() {
        OuterFlagNodeStyleDescriptionImpl outerFlagNodeStyleDescription = new OuterFlagNodeStyleDescriptionImpl();
        return outerFlagNodeStyleDescription;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public CuboidNodeStyleDescription createCuboidNodeStyleDescription() {
        CuboidNodeStyleDescriptionImpl cuboidNodeStyleDescription = new CuboidNodeStyleDescriptionImpl();
        return cuboidNodeStyleDescription;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public CustomImageNodeStyleDescription createCustomImageNodeStyleDescription() {
        CustomImageNodeStyleDescriptionImpl customImageNodeStyleDescription = new CustomImageNodeStyleDescriptionImpl();
        return customImageNodeStyleDescription;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public PapyrusCustomNodesPackage getPapyrusCustomNodesPackage() {
        return (PapyrusCustomNodesPackage) this.getEPackage();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @deprecated
     * @generated
     */
    @Deprecated
    public static PapyrusCustomNodesPackage getPackage() {
        return PapyrusCustomNodesPackage.eINSTANCE;
    }

} // PapyrusCustomNodesFactoryImpl

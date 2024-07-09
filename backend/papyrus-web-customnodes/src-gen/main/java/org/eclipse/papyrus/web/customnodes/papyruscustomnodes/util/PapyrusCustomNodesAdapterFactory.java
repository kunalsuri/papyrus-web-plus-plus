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
package org.eclipse.papyrus.web.customnodes.papyruscustomnodes.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.InnerFlagNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.OuterFlagNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.BorderStyle;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;

/**
 * <!-- begin-user-doc --> The <b>Adapter Factory</b> for the model. It provides an adapter <code>createXXX</code>
 * method for each class of the model. <!-- end-user-doc -->
 *
 * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage
 * @generated
 */
public class PapyrusCustomNodesAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected static PapyrusCustomNodesPackage modelPackage;

    /**
     * Creates an instance of the adapter factory. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public PapyrusCustomNodesAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = PapyrusCustomNodesPackage.eINSTANCE;
        }
    }

    /**
     * Returns whether this factory is applicable for the type of the object. <!-- begin-user-doc --> This
     * implementation returns <code>true</code> if the object is either the model's package or is an instance object of
     * the model. <!-- end-user-doc -->
     *
     * @return whether this factory is applicable for the type of the object.
     * @generated
     */
    @Override
    public boolean isFactoryForType(Object object) {
        if (object == modelPackage) {
            return true;
        }
        if (object instanceof EObject) {
            return ((EObject) object).eClass().getEPackage() == modelPackage;
        }
        return false;
    }

    /**
     * The switch that delegates to the <code>createXXX</code> methods. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected PapyrusCustomNodesSwitch<Adapter> modelSwitch = new PapyrusCustomNodesSwitch<>() {
        @Override
        public Adapter casePackageNodeStyleDescription(PackageNodeStyleDescription object) {
            return PapyrusCustomNodesAdapterFactory.this.createPackageNodeStyleDescriptionAdapter();
        }

        @Override
        public Adapter caseRectangleWithExternalLabelNodeStyleDescription(RectangleWithExternalLabelNodeStyleDescription object) {
            return PapyrusCustomNodesAdapterFactory.this.createRectangleWithExternalLabelNodeStyleDescriptionAdapter();
        }

        @Override
        public Adapter caseNoteNodeStyleDescription(NoteNodeStyleDescription object) {
            return PapyrusCustomNodesAdapterFactory.this.createNoteNodeStyleDescriptionAdapter();
        }

        @Override
        public Adapter caseInnerFlagNodeStyleDescription(InnerFlagNodeStyleDescription object) {
            return PapyrusCustomNodesAdapterFactory.this.createInnerFlagNodeStyleDescriptionAdapter();
        }

        @Override
        public Adapter caseOuterFlagNodeStyleDescription(OuterFlagNodeStyleDescription object) {
            return PapyrusCustomNodesAdapterFactory.this.createOuterFlagNodeStyleDescriptionAdapter();
        }

        @Override
        public Adapter caseCuboidNodeStyleDescription(CuboidNodeStyleDescription object) {
            return PapyrusCustomNodesAdapterFactory.this.createCuboidNodeStyleDescriptionAdapter();
        }

        @Override
        public Adapter caseCustomImageNodeStyleDescription(CustomImageNodeStyleDescription object) {
            return PapyrusCustomNodesAdapterFactory.this.createCustomImageNodeStyleDescriptionAdapter();
        }

        @Override
        public Adapter caseBorderStyle(BorderStyle object) {
            return PapyrusCustomNodesAdapterFactory.this.createBorderStyleAdapter();
        }

        @Override
        public Adapter caseNodeStyleDescription(NodeStyleDescription object) {
            return PapyrusCustomNodesAdapterFactory.this.createNodeStyleDescriptionAdapter();
        }

        @Override
        public Adapter defaultCase(EObject object) {
            return PapyrusCustomNodesAdapterFactory.this.createEObjectAdapter();
        }
    };

    /**
     * Creates an adapter for the <code>target</code>. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param target
     *            the object to adapt.
     * @return the adapter for the <code>target</code>.
     * @generated
     */
    @Override
    public Adapter createAdapter(Notifier target) {
        return this.modelSwitch.doSwitch((EObject) target);
    }

    /**
     * Creates a new adapter for an object of class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription <em>Package Node Style
     * Description</em>}'. <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc -->
     *
     * @return the new adapter.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription
     * @generated
     */
    public Adapter createPackageNodeStyleDescriptionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription
     * <em>Rectangle With External Label Node Style Description</em>}'. <!-- begin-user-doc --> This default
     * implementation returns null so that we can easily ignore cases; it's useful to ignore a case when inheritance
     * will catch all the cases anyway. <!-- end-user-doc -->
     *
     * @return the new adapter.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription
     * @generated
     */
    public Adapter createRectangleWithExternalLabelNodeStyleDescriptionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription <em>Note Node Style
     * Description</em>}'. <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc -->
     *
     * @return the new adapter.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription
     * @generated
     */
    public Adapter createNoteNodeStyleDescriptionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.InnerFlagNodeStyleDescription <em>Inner Flag Node
     * Style Description</em>}'. <!-- begin-user-doc --> This default implementation returns null so that we can easily
     * ignore cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc
     * -->
     *
     * @return the new adapter.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.InnerFlagNodeStyleDescription
     * @generated
     */
    public Adapter createInnerFlagNodeStyleDescriptionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.OuterFlagNodeStyleDescription <em>Outer Flag Node
     * Style Description</em>}'. <!-- begin-user-doc --> This default implementation returns null so that we can easily
     * ignore cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc
     * -->
     *
     * @return the new adapter.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.OuterFlagNodeStyleDescription
     * @generated
     */
    public Adapter createOuterFlagNodeStyleDescriptionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription <em>Cuboid Node Style
     * Description</em>}'. <!-- begin-user-doc --> This default implementation returns null so that we can easily ignore
     * cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc -->
     *
     * @return the new adapter.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription
     * @generated
     */
    public Adapter createCuboidNodeStyleDescriptionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription <em>Custom Image
     * Node Style Description</em>}'. <!-- begin-user-doc --> This default implementation returns null so that we can
     * easily ignore cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!--
     * end-user-doc -->
     *
     * @return the new adapter.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription
     * @generated
     */
    public Adapter createCustomImageNodeStyleDescriptionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.eclipse.sirius.components.view.diagram.BorderStyle
     * <em>Border Style</em>}'. <!-- begin-user-doc --> This default implementation returns null so that we can easily
     * ignore cases; it's useful to ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc
     * -->
     *
     * @return the new adapter.
     * @see org.eclipse.sirius.components.view.diagram.BorderStyle
     * @generated
     */
    public Adapter createBorderStyleAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class
     * '{@link org.eclipse.sirius.components.view.diagram.NodeStyleDescription <em>Node Style Description</em>}'. <!--
     * begin-user-doc --> This default implementation returns null so that we can easily ignore cases; it's useful to
     * ignore a case when inheritance will catch all the cases anyway. <!-- end-user-doc -->
     *
     * @return the new adapter.
     * @see org.eclipse.sirius.components.view.diagram.NodeStyleDescription
     * @generated
     */
    public Adapter createNodeStyleDescriptionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for the default case. <!-- begin-user-doc --> This default implementation returns null.
     * <!-- end-user-doc -->
     *
     * @return the new adapter.
     * @generated
     */
    public Adapter createEObjectAdapter() {
        return null;
    }

} // PapyrusCustomNodesAdapterFactory

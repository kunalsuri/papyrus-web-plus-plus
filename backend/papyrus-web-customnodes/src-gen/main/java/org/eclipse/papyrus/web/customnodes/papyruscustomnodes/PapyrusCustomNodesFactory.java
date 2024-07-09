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
package org.eclipse.papyrus.web.customnodes.papyruscustomnodes;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a create method for each non-abstract class of
 * the model. <!-- end-user-doc -->
 *
 * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage
 * @generated
 */
public interface PapyrusCustomNodesFactory extends EFactory {
    /**
     * The singleton instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    PapyrusCustomNodesFactory eINSTANCE = org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesFactoryImpl.init();

    /**
     * Returns a new object of class '<em>Package Node Style Description</em>'. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @return a new object of class '<em>Package Node Style Description</em>'.
     * @generated
     */
    PackageNodeStyleDescription createPackageNodeStyleDescription();

    /**
     * Returns a new object of class '<em>Rectangle With External Label Node Style Description</em>'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Rectangle With External Label Node Style Description</em>'.
     * @generated
     */
    RectangleWithExternalLabelNodeStyleDescription createRectangleWithExternalLabelNodeStyleDescription();

    /**
     * Returns a new object of class '<em>Note Node Style Description</em>'. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @return a new object of class '<em>Note Node Style Description</em>'.
     * @generated
     */
    NoteNodeStyleDescription createNoteNodeStyleDescription();

    /**
     * Returns a new object of class '<em>Inner Flag Node Style Description</em>'. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @return a new object of class '<em>Inner Flag Node Style Description</em>'.
     * @generated
     */
    InnerFlagNodeStyleDescription createInnerFlagNodeStyleDescription();

    /**
     * Returns a new object of class '<em>Outer Flag Node Style Description</em>'. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @return a new object of class '<em>Outer Flag Node Style Description</em>'.
     * @generated
     */
    OuterFlagNodeStyleDescription createOuterFlagNodeStyleDescription();

    /**
     * Returns a new object of class '<em>Cuboid Node Style Description</em>'. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @return a new object of class '<em>Cuboid Node Style Description</em>'.
     * @generated
     */
    CuboidNodeStyleDescription createCuboidNodeStyleDescription();

    /**
     * Returns a new object of class '<em>Custom Image Node Style Description</em>'. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @return a new object of class '<em>Custom Image Node Style Description</em>'.
     * @generated
     */
    CustomImageNodeStyleDescription createCustomImageNodeStyleDescription();

    /**
     * Returns the package supported by this factory. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the package supported by this factory.
     * @generated
     */
    PapyrusCustomNodesPackage getPapyrusCustomNodesPackage();

} // PapyrusCustomNodesFactory

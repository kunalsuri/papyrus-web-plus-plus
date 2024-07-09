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

import org.eclipse.sirius.components.view.UserColor;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Custom Image Node Style Description</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription#getBackground
 * <em>Background</em>}</li>
 * <li>{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription#getShape
 * <em>Shape</em>}</li>
 * </ul>
 *
 * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage#getCustomImageNodeStyleDescription()
 * @model
 * @generated
 */
public interface CustomImageNodeStyleDescription extends NodeStyleDescription {
    /**
     * Returns the value of the '<em><b>Background</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the value of the '<em>Background</em>' reference.
     * @see #setBackground(UserColor)
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage#getCustomImageNodeStyleDescription_Background()
     * @model
     * @generated
     */
    UserColor getBackground();

    /**
     * Sets the value of the
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription#getBackground
     * <em>Background</em>}' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value
     *            the new value of the '<em>Background</em>' reference.
     * @see #getBackground()
     * @generated
     */
    void setBackground(UserColor value);

    /**
     * Returns the value of the '<em><b>Shape</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the value of the '<em>Shape</em>' attribute.
     * @see #setShape(String)
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage#getCustomImageNodeStyleDescription_Shape()
     * @model dataType="org.eclipse.sirius.components.view.InterpretedExpression"
     * @generated
     */
    String getShape();

    /**
     * Sets the value of the
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription#getShape
     * <em>Shape</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value
     *            the new value of the '<em>Shape</em>' attribute.
     * @see #getShape()
     * @generated
     */
    void setShape(String value);

} // CustomImageNodeStyleDescription

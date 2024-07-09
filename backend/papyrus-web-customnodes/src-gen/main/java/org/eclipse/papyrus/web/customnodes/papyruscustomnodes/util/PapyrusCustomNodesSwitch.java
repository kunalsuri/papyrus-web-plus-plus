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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;
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
 * <!-- begin-user-doc --> The <b>Switch</b> for the model's inheritance hierarchy. It supports the call
 * {@link #doSwitch(EObject) doSwitch(object)} to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object and proceeding up the inheritance hierarchy until a non-null result is
 * returned, which is the result of the switch. <!-- end-user-doc -->
 *
 * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage
 * @generated
 */
public class PapyrusCustomNodesSwitch<T> extends Switch<T> {
    /**
     * The cached model package <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected static PapyrusCustomNodesPackage modelPackage;

    /**
     * Creates an instance of the switch. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public PapyrusCustomNodesSwitch() {
        if (modelPackage == null) {
            modelPackage = PapyrusCustomNodesPackage.eINSTANCE;
        }
    }

    /**
     * Checks whether this is a switch for the given package. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param ePackage
     *            the package in question.
     * @return whether this is a switch for the given package.
     * @generated
     */
    @Override
    protected boolean isSwitchFor(EPackage ePackage) {
        return ePackage == modelPackage;
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that
     * result. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    @Override
    protected T doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case PapyrusCustomNodesPackage.PACKAGE_NODE_STYLE_DESCRIPTION: {
                PackageNodeStyleDescription packageNodeStyleDescription = (PackageNodeStyleDescription) theEObject;
                T result = this.casePackageNodeStyleDescription(packageNodeStyleDescription);
                if (result == null)
                    result = this.caseNodeStyleDescription(packageNodeStyleDescription);
                if (result == null)
                    result = this.caseBorderStyle(packageNodeStyleDescription);
                if (result == null)
                    result = this.defaultCase(theEObject);
                return result;
            }
            case PapyrusCustomNodesPackage.RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION: {
                RectangleWithExternalLabelNodeStyleDescription rectangleWithExternalLabelNodeStyleDescription = (RectangleWithExternalLabelNodeStyleDescription) theEObject;
                T result = this.caseRectangleWithExternalLabelNodeStyleDescription(rectangleWithExternalLabelNodeStyleDescription);
                if (result == null)
                    result = this.caseNodeStyleDescription(rectangleWithExternalLabelNodeStyleDescription);
                if (result == null)
                    result = this.caseBorderStyle(rectangleWithExternalLabelNodeStyleDescription);
                if (result == null)
                    result = this.defaultCase(theEObject);
                return result;
            }
            case PapyrusCustomNodesPackage.NOTE_NODE_STYLE_DESCRIPTION: {
                NoteNodeStyleDescription noteNodeStyleDescription = (NoteNodeStyleDescription) theEObject;
                T result = this.caseNoteNodeStyleDescription(noteNodeStyleDescription);
                if (result == null)
                    result = this.caseNodeStyleDescription(noteNodeStyleDescription);
                if (result == null)
                    result = this.caseBorderStyle(noteNodeStyleDescription);
                if (result == null)
                    result = this.defaultCase(theEObject);
                return result;
            }
            case PapyrusCustomNodesPackage.INNER_FLAG_NODE_STYLE_DESCRIPTION: {
                InnerFlagNodeStyleDescription innerFlagNodeStyleDescription = (InnerFlagNodeStyleDescription) theEObject;
                T result = this.caseInnerFlagNodeStyleDescription(innerFlagNodeStyleDescription);
                if (result == null)
                    result = this.caseNodeStyleDescription(innerFlagNodeStyleDescription);
                if (result == null)
                    result = this.caseBorderStyle(innerFlagNodeStyleDescription);
                if (result == null)
                    result = this.defaultCase(theEObject);
                return result;
            }
            case PapyrusCustomNodesPackage.OUTER_FLAG_NODE_STYLE_DESCRIPTION: {
                OuterFlagNodeStyleDescription outerFlagNodeStyleDescription = (OuterFlagNodeStyleDescription) theEObject;
                T result = this.caseOuterFlagNodeStyleDescription(outerFlagNodeStyleDescription);
                if (result == null)
                    result = this.caseNodeStyleDescription(outerFlagNodeStyleDescription);
                if (result == null)
                    result = this.caseBorderStyle(outerFlagNodeStyleDescription);
                if (result == null)
                    result = this.defaultCase(theEObject);
                return result;
            }
            case PapyrusCustomNodesPackage.CUBOID_NODE_STYLE_DESCRIPTION: {
                CuboidNodeStyleDescription cuboidNodeStyleDescription = (CuboidNodeStyleDescription) theEObject;
                T result = this.caseCuboidNodeStyleDescription(cuboidNodeStyleDescription);
                if (result == null)
                    result = this.caseNodeStyleDescription(cuboidNodeStyleDescription);
                if (result == null)
                    result = this.caseBorderStyle(cuboidNodeStyleDescription);
                if (result == null)
                    result = this.defaultCase(theEObject);
                return result;
            }
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION: {
                CustomImageNodeStyleDescription customImageNodeStyleDescription = (CustomImageNodeStyleDescription) theEObject;
                T result = this.caseCustomImageNodeStyleDescription(customImageNodeStyleDescription);
                if (result == null)
                    result = this.caseNodeStyleDescription(customImageNodeStyleDescription);
                if (result == null)
                    result = this.caseBorderStyle(customImageNodeStyleDescription);
                if (result == null)
                    result = this.defaultCase(theEObject);
                return result;
            }
            default:
                return this.defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Package Node Style Description</em>'. <!--
     * begin-user-doc --> This implementation returns null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Package Node Style Description</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T casePackageNodeStyleDescription(PackageNodeStyleDescription object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Rectangle With External Label Node Style
     * Description</em>'. <!-- begin-user-doc --> This implementation returns null; returning a non-null result will
     * terminate the switch. <!-- end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Rectangle With External Label Node Style
     *         Description</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseRectangleWithExternalLabelNodeStyleDescription(RectangleWithExternalLabelNodeStyleDescription object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Note Node Style Description</em>'. <!--
     * begin-user-doc --> This implementation returns null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Note Node Style Description</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseNoteNodeStyleDescription(NoteNodeStyleDescription object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Inner Flag Node Style Description</em>'.
     * <!-- begin-user-doc --> This implementation returns null; returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Inner Flag Node Style Description</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseInnerFlagNodeStyleDescription(InnerFlagNodeStyleDescription object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Outer Flag Node Style Description</em>'.
     * <!-- begin-user-doc --> This implementation returns null; returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Outer Flag Node Style Description</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseOuterFlagNodeStyleDescription(OuterFlagNodeStyleDescription object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Cuboid Node Style Description</em>'. <!--
     * begin-user-doc --> This implementation returns null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Cuboid Node Style Description</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseCuboidNodeStyleDescription(CuboidNodeStyleDescription object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Custom Image Node Style Description</em>'.
     * <!-- begin-user-doc --> This implementation returns null; returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Custom Image Node Style Description</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseCustomImageNodeStyleDescription(CustomImageNodeStyleDescription object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Border Style</em>'. <!-- begin-user-doc -->
     * This implementation returns null; returning a non-null result will terminate the switch. <!-- end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Border Style</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseBorderStyle(BorderStyle object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Node Style Description</em>'. <!--
     * begin-user-doc --> This implementation returns null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Node Style Description</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseNodeStyleDescription(NodeStyleDescription object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>EObject</em>'. <!-- begin-user-doc --> This
     * implementation returns null; returning a non-null result will terminate the switch, but this is the last case
     * anyway. <!-- end-user-doc -->
     *
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
    @Override
    public T defaultCase(EObject object) {
        return null;
    }

} // PapyrusCustomNodesSwitch

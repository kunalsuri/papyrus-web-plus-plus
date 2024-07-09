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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage;
import org.eclipse.sirius.components.view.UserColor;
import org.eclipse.sirius.components.view.diagram.LineStyle;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Custom Image Node Style Description</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl#getBorderColor
 * <em>Border Color</em>}</li>
 * <li>{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl#getBorderRadius
 * <em>Border Radius</em>}</li>
 * <li>{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl#getBorderSize
 * <em>Border Size</em>}</li>
 * <li>{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl#getBorderLineStyle
 * <em>Border Line Style</em>}</li>
 * <li>{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl#getBackground
 * <em>Background</em>}</li>
 * <li>{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl#getShape
 * <em>Shape</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CustomImageNodeStyleDescriptionImpl extends MinimalEObjectImpl.Container implements CustomImageNodeStyleDescription {
    /**
     * The cached value of the '{@link #getBorderColor() <em>Border Color</em>}' reference. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #getBorderColor()
     * @generated
     * @ordered
     */
    protected UserColor borderColor;

    /**
     * The default value of the '{@link #getBorderRadius() <em>Border Radius</em>}' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @see #getBorderRadius()
     * @generated
     * @ordered
     */
    protected static final int BORDER_RADIUS_EDEFAULT = 3;

    /**
     * The cached value of the '{@link #getBorderRadius() <em>Border Radius</em>}' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @see #getBorderRadius()
     * @generated
     * @ordered
     */
    protected int borderRadius = BORDER_RADIUS_EDEFAULT;

    /**
     * The default value of the '{@link #getBorderSize() <em>Border Size</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #getBorderSize()
     * @generated
     * @ordered
     */
    protected static final int BORDER_SIZE_EDEFAULT = 1;

    /**
     * The cached value of the '{@link #getBorderSize() <em>Border Size</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #getBorderSize()
     * @generated
     * @ordered
     */
    protected int borderSize = BORDER_SIZE_EDEFAULT;

    /**
     * The default value of the '{@link #getBorderLineStyle() <em>Border Line Style</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @see #getBorderLineStyle()
     * @generated
     * @ordered
     */
    protected static final LineStyle BORDER_LINE_STYLE_EDEFAULT = LineStyle.SOLID;

    /**
     * The cached value of the '{@link #getBorderLineStyle() <em>Border Line Style</em>}' attribute. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @see #getBorderLineStyle()
     * @generated
     * @ordered
     */
    protected LineStyle borderLineStyle = BORDER_LINE_STYLE_EDEFAULT;

    /**
     * The cached value of the '{@link #getBackground() <em>Background</em>}' reference. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #getBackground()
     * @generated
     * @ordered
     */
    protected UserColor background;

    /**
     * The default value of the '{@link #getShape() <em>Shape</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @see #getShape()
     * @generated
     * @ordered
     */
    protected static final String SHAPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getShape() <em>Shape</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @see #getShape()
     * @generated
     * @ordered
     */
    protected String shape = SHAPE_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected CustomImageNodeStyleDescriptionImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return PapyrusCustomNodesPackage.Literals.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public UserColor getBorderColor() {
        if (this.borderColor != null && this.borderColor.eIsProxy()) {
            InternalEObject oldBorderColor = (InternalEObject) this.borderColor;
            this.borderColor = (UserColor) this.eResolveProxy(oldBorderColor);
            if (this.borderColor != oldBorderColor) {
                if (this.eNotificationRequired())
                    this.eNotify(new ENotificationImpl(this, Notification.RESOLVE, PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_COLOR, oldBorderColor, this.borderColor));
            }
        }
        return this.borderColor;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public UserColor basicGetBorderColor() {
        return this.borderColor;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setBorderColor(UserColor newBorderColor) {
        UserColor oldBorderColor = this.borderColor;
        this.borderColor = newBorderColor;
        if (this.eNotificationRequired())
            this.eNotify(new ENotificationImpl(this, Notification.SET, PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_COLOR, oldBorderColor, this.borderColor));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public int getBorderRadius() {
        return this.borderRadius;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setBorderRadius(int newBorderRadius) {
        int oldBorderRadius = this.borderRadius;
        this.borderRadius = newBorderRadius;
        if (this.eNotificationRequired())
            this.eNotify(new ENotificationImpl(this, Notification.SET, PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_RADIUS, oldBorderRadius, this.borderRadius));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public int getBorderSize() {
        return this.borderSize;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setBorderSize(int newBorderSize) {
        int oldBorderSize = this.borderSize;
        this.borderSize = newBorderSize;
        if (this.eNotificationRequired())
            this.eNotify(new ENotificationImpl(this, Notification.SET, PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_SIZE, oldBorderSize, this.borderSize));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public LineStyle getBorderLineStyle() {
        return this.borderLineStyle;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setBorderLineStyle(LineStyle newBorderLineStyle) {
        LineStyle oldBorderLineStyle = this.borderLineStyle;
        this.borderLineStyle = newBorderLineStyle == null ? BORDER_LINE_STYLE_EDEFAULT : newBorderLineStyle;
        if (this.eNotificationRequired())
            this.eNotify(new ENotificationImpl(this, Notification.SET, PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE, oldBorderLineStyle, this.borderLineStyle));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public UserColor getBackground() {
        if (this.background != null && this.background.eIsProxy()) {
            InternalEObject oldBackground = (InternalEObject) this.background;
            this.background = (UserColor) this.eResolveProxy(oldBackground);
            if (this.background != oldBackground) {
                if (this.eNotificationRequired())
                    this.eNotify(new ENotificationImpl(this, Notification.RESOLVE, PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BACKGROUND, oldBackground, this.background));
            }
        }
        return this.background;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public UserColor basicGetBackground() {
        return this.background;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setBackground(UserColor newBackground) {
        UserColor oldBackground = this.background;
        this.background = newBackground;
        if (this.eNotificationRequired())
            this.eNotify(new ENotificationImpl(this, Notification.SET, PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BACKGROUND, oldBackground, this.background));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getShape() {
        return this.shape;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setShape(String newShape) {
        String oldShape = this.shape;
        this.shape = newShape;
        if (this.eNotificationRequired())
            this.eNotify(new ENotificationImpl(this, Notification.SET, PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__SHAPE, oldShape, this.shape));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_COLOR:
                if (resolve)
                    return this.getBorderColor();
                return this.basicGetBorderColor();
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_RADIUS:
                return this.getBorderRadius();
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_SIZE:
                return this.getBorderSize();
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE:
                return this.getBorderLineStyle();
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BACKGROUND:
                if (resolve)
                    return this.getBackground();
                return this.basicGetBackground();
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__SHAPE:
                return this.getShape();
            default:
                return super.eGet(featureID, resolve, coreType);
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_COLOR:
                this.setBorderColor((UserColor) newValue);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_RADIUS:
                this.setBorderRadius((Integer) newValue);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_SIZE:
                this.setBorderSize((Integer) newValue);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE:
                this.setBorderLineStyle((LineStyle) newValue);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BACKGROUND:
                this.setBackground((UserColor) newValue);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__SHAPE:
                this.setShape((String) newValue);
                return;
            default:
                super.eSet(featureID, newValue);
                return;
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_COLOR:
                this.setBorderColor((UserColor) null);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_RADIUS:
                this.setBorderRadius(BORDER_RADIUS_EDEFAULT);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_SIZE:
                this.setBorderSize(BORDER_SIZE_EDEFAULT);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE:
                this.setBorderLineStyle(BORDER_LINE_STYLE_EDEFAULT);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BACKGROUND:
                this.setBackground((UserColor) null);
                return;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__SHAPE:
                this.setShape(SHAPE_EDEFAULT);
                return;
            default:
                super.eUnset(featureID);
                return;
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_COLOR:
                return this.borderColor != null;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_RADIUS:
                return this.borderRadius != BORDER_RADIUS_EDEFAULT;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_SIZE:
                return this.borderSize != BORDER_SIZE_EDEFAULT;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE:
                return this.borderLineStyle != BORDER_LINE_STYLE_EDEFAULT;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BACKGROUND:
                return this.background != null;
            case PapyrusCustomNodesPackage.CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__SHAPE:
                return SHAPE_EDEFAULT == null ? this.shape != null : !SHAPE_EDEFAULT.equals(this.shape);
            default:
                return super.eIsSet(featureID);
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String toString() {
        if (this.eIsProxy())
            return super.toString();

        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (borderRadius: ");
        result.append(this.borderRadius);
        result.append(", borderSize: ");
        result.append(this.borderSize);
        result.append(", borderLineStyle: ");
        result.append(this.borderLineStyle);
        result.append(", shape: ");
        result.append(this.shape);
        result.append(')');
        return result.toString();
    }

} // CustomImageNodeStyleDescriptionImpl

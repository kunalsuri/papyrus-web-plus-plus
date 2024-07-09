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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.view.diagram.DiagramPackage;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each operation of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 *
 * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesFactory
 * @model kind="package"
 * @generated
 */
public interface PapyrusCustomNodesPackage extends EPackage {
    /**
     * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    String eNAME = "papyruscustomnodes";

    /**
     * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    String eNS_URI = "http://www.eclipse.org/papyrus-web/customnodes";

    /**
     * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    String eNS_PREFIX = "papyruscustomnodes";

    /**
     * The singleton instance of the package. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    PapyrusCustomNodesPackage eINSTANCE = org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl.init();

    /**
     * The meta object id for the
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PackageNodeStyleDescriptionImpl <em>Package
     * Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PackageNodeStyleDescriptionImpl
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getPackageNodeStyleDescription()
     * @generated
     */
    int PACKAGE_NODE_STYLE_DESCRIPTION = 0;

    /**
     * The feature id for the '<em><b>Border Color</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int PACKAGE_NODE_STYLE_DESCRIPTION__BORDER_COLOR = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_COLOR;

    /**
     * The feature id for the '<em><b>Border Radius</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int PACKAGE_NODE_STYLE_DESCRIPTION__BORDER_RADIUS = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_RADIUS;

    /**
     * The feature id for the '<em><b>Border Size</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int PACKAGE_NODE_STYLE_DESCRIPTION__BORDER_SIZE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_SIZE;

    /**
     * The feature id for the '<em><b>Border Line Style</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @generated
     * @ordered
     */
    int PACKAGE_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE;

    /**
     * The feature id for the '<em><b>Background</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int PACKAGE_NODE_STYLE_DESCRIPTION__BACKGROUND = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Package Node Style Description</em>' class. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int PACKAGE_NODE_STYLE_DESCRIPTION_FEATURE_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 1;

    /**
     * The number of operations of the '<em>Package Node Style Description</em>' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int PACKAGE_NODE_STYLE_DESCRIPTION_OPERATION_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_OPERATION_COUNT + 0;

    /**
     * The meta object id for the
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.RectangleWithExternalLabelNodeStyleDescriptionImpl
     * <em>Rectangle With External Label Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.RectangleWithExternalLabelNodeStyleDescriptionImpl
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getRectangleWithExternalLabelNodeStyleDescription()
     * @generated
     */
    int RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION = 1;

    /**
     * The feature id for the '<em><b>Border Color</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION__BORDER_COLOR = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_COLOR;

    /**
     * The feature id for the '<em><b>Border Radius</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION__BORDER_RADIUS = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_RADIUS;

    /**
     * The feature id for the '<em><b>Border Size</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION__BORDER_SIZE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_SIZE;

    /**
     * The feature id for the '<em><b>Border Line Style</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @generated
     * @ordered
     */
    int RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE;

    /**
     * The feature id for the '<em><b>Background</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION__BACKGROUND = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Rectangle With External Label Node Style Description</em>' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION_FEATURE_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 1;

    /**
     * The number of operations of the '<em>Rectangle With External Label Node Style Description</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION_OPERATION_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_OPERATION_COUNT + 0;

    /**
     * The meta object id for the
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.NoteNodeStyleDescriptionImpl <em>Note Node
     * Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.NoteNodeStyleDescriptionImpl
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getNoteNodeStyleDescription()
     * @generated
     */
    int NOTE_NODE_STYLE_DESCRIPTION = 2;

    /**
     * The feature id for the '<em><b>Border Color</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int NOTE_NODE_STYLE_DESCRIPTION__BORDER_COLOR = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_COLOR;

    /**
     * The feature id for the '<em><b>Border Radius</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int NOTE_NODE_STYLE_DESCRIPTION__BORDER_RADIUS = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_RADIUS;

    /**
     * The feature id for the '<em><b>Border Size</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int NOTE_NODE_STYLE_DESCRIPTION__BORDER_SIZE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_SIZE;

    /**
     * The feature id for the '<em><b>Border Line Style</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @generated
     * @ordered
     */
    int NOTE_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE;

    /**
     * The feature id for the '<em><b>Background</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int NOTE_NODE_STYLE_DESCRIPTION__BACKGROUND = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Note Node Style Description</em>' class. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int NOTE_NODE_STYLE_DESCRIPTION_FEATURE_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 1;

    /**
     * The number of operations of the '<em>Note Node Style Description</em>' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int NOTE_NODE_STYLE_DESCRIPTION_OPERATION_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_OPERATION_COUNT + 0;

    /**
     * The meta object id for the
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.InnerFlagNodeStyleDescriptionImpl <em>Inner
     * Flag Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.InnerFlagNodeStyleDescriptionImpl
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getInnerFlagNodeStyleDescription()
     * @generated
     */
    int INNER_FLAG_NODE_STYLE_DESCRIPTION = 3;

    /**
     * The feature id for the '<em><b>Border Color</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int INNER_FLAG_NODE_STYLE_DESCRIPTION__BORDER_COLOR = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_COLOR;

    /**
     * The feature id for the '<em><b>Border Radius</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int INNER_FLAG_NODE_STYLE_DESCRIPTION__BORDER_RADIUS = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_RADIUS;

    /**
     * The feature id for the '<em><b>Border Size</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int INNER_FLAG_NODE_STYLE_DESCRIPTION__BORDER_SIZE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_SIZE;

    /**
     * The feature id for the '<em><b>Border Line Style</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @generated
     * @ordered
     */
    int INNER_FLAG_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE;

    /**
     * The feature id for the '<em><b>Background</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int INNER_FLAG_NODE_STYLE_DESCRIPTION__BACKGROUND = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Inner Flag Node Style Description</em>' class. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int INNER_FLAG_NODE_STYLE_DESCRIPTION_FEATURE_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 1;

    /**
     * The number of operations of the '<em>Inner Flag Node Style Description</em>' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int INNER_FLAG_NODE_STYLE_DESCRIPTION_OPERATION_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_OPERATION_COUNT + 0;

    /**
     * The meta object id for the
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.OuterFlagNodeStyleDescriptionImpl <em>Outer
     * Flag Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.OuterFlagNodeStyleDescriptionImpl
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getOuterFlagNodeStyleDescription()
     * @generated
     */
    int OUTER_FLAG_NODE_STYLE_DESCRIPTION = 4;

    /**
     * The feature id for the '<em><b>Border Color</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int OUTER_FLAG_NODE_STYLE_DESCRIPTION__BORDER_COLOR = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_COLOR;

    /**
     * The feature id for the '<em><b>Border Radius</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int OUTER_FLAG_NODE_STYLE_DESCRIPTION__BORDER_RADIUS = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_RADIUS;

    /**
     * The feature id for the '<em><b>Border Size</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int OUTER_FLAG_NODE_STYLE_DESCRIPTION__BORDER_SIZE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_SIZE;

    /**
     * The feature id for the '<em><b>Border Line Style</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @generated
     * @ordered
     */
    int OUTER_FLAG_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE;

    /**
     * The feature id for the '<em><b>Background</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int OUTER_FLAG_NODE_STYLE_DESCRIPTION__BACKGROUND = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Outer Flag Node Style Description</em>' class. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int OUTER_FLAG_NODE_STYLE_DESCRIPTION_FEATURE_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 1;

    /**
     * The number of operations of the '<em>Outer Flag Node Style Description</em>' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int OUTER_FLAG_NODE_STYLE_DESCRIPTION_OPERATION_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_OPERATION_COUNT + 0;

    /**
     * The meta object id for the
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CuboidNodeStyleDescriptionImpl <em>Cuboid
     * Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CuboidNodeStyleDescriptionImpl
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getCuboidNodeStyleDescription()
     * @generated
     */
    int CUBOID_NODE_STYLE_DESCRIPTION = 5;

    /**
     * The feature id for the '<em><b>Border Color</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUBOID_NODE_STYLE_DESCRIPTION__BORDER_COLOR = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_COLOR;

    /**
     * The feature id for the '<em><b>Border Radius</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUBOID_NODE_STYLE_DESCRIPTION__BORDER_RADIUS = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_RADIUS;

    /**
     * The feature id for the '<em><b>Border Size</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUBOID_NODE_STYLE_DESCRIPTION__BORDER_SIZE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_SIZE;

    /**
     * The feature id for the '<em><b>Border Line Style</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @generated
     * @ordered
     */
    int CUBOID_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE;

    /**
     * The feature id for the '<em><b>Background</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUBOID_NODE_STYLE_DESCRIPTION__BACKGROUND = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Cuboid Node Style Description</em>' class. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUBOID_NODE_STYLE_DESCRIPTION_FEATURE_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 1;

    /**
     * The number of operations of the '<em>Cuboid Node Style Description</em>' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUBOID_NODE_STYLE_DESCRIPTION_OPERATION_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_OPERATION_COUNT + 0;

    /**
     * The meta object id for the
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl
     * <em>Custom Image Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getCustomImageNodeStyleDescription()
     * @generated
     */
    int CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION = 6;

    /**
     * The feature id for the '<em><b>Border Color</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_COLOR = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_COLOR;

    /**
     * The feature id for the '<em><b>Border Radius</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_RADIUS = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_RADIUS;

    /**
     * The feature id for the '<em><b>Border Size</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_SIZE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_SIZE;

    /**
     * The feature id for the '<em><b>Border Line Style</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @generated
     * @ordered
     */
    int CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE = DiagramPackage.NODE_STYLE_DESCRIPTION__BORDER_LINE_STYLE;

    /**
     * The feature id for the '<em><b>Background</b></em>' reference. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BACKGROUND = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Shape</b></em>' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__SHAPE = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 1;

    /**
     * The number of structural features of the '<em>Custom Image Node Style Description</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION_FEATURE_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_FEATURE_COUNT + 2;

    /**
     * The number of operations of the '<em>Custom Image Node Style Description</em>' class. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION_OPERATION_COUNT = DiagramPackage.NODE_STYLE_DESCRIPTION_OPERATION_COUNT + 0;

    /**
     * Returns the meta object for class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription <em>Package Node Style
     * Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Package Node Style Description</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription
     * @generated
     */
    EClass getPackageNodeStyleDescription();

    /**
     * Returns the meta object for the reference
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription#getBackground
     * <em>Background</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the reference '<em>Background</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription#getBackground()
     * @see #getPackageNodeStyleDescription()
     * @generated
     */
    EReference getPackageNodeStyleDescription_Background();

    /**
     * Returns the meta object for class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription
     * <em>Rectangle With External Label Node Style Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Rectangle With External Label Node Style Description</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription
     * @generated
     */
    EClass getRectangleWithExternalLabelNodeStyleDescription();

    /**
     * Returns the meta object for the reference
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription#getBackground
     * <em>Background</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the reference '<em>Background</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription#getBackground()
     * @see #getRectangleWithExternalLabelNodeStyleDescription()
     * @generated
     */
    EReference getRectangleWithExternalLabelNodeStyleDescription_Background();

    /**
     * Returns the meta object for class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription <em>Note Node Style
     * Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Note Node Style Description</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription
     * @generated
     */
    EClass getNoteNodeStyleDescription();

    /**
     * Returns the meta object for the reference
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription#getBackground
     * <em>Background</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the reference '<em>Background</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription#getBackground()
     * @see #getNoteNodeStyleDescription()
     * @generated
     */
    EReference getNoteNodeStyleDescription_Background();

    /**
     * Returns the meta object for class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.InnerFlagNodeStyleDescription <em>Inner Flag Node
     * Style Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Inner Flag Node Style Description</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.InnerFlagNodeStyleDescription
     * @generated
     */
    EClass getInnerFlagNodeStyleDescription();

    /**
     * Returns the meta object for the reference
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.InnerFlagNodeStyleDescription#getBackground
     * <em>Background</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the reference '<em>Background</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.InnerFlagNodeStyleDescription#getBackground()
     * @see #getInnerFlagNodeStyleDescription()
     * @generated
     */
    EReference getInnerFlagNodeStyleDescription_Background();

    /**
     * Returns the meta object for class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.OuterFlagNodeStyleDescription <em>Outer Flag Node
     * Style Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Outer Flag Node Style Description</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.OuterFlagNodeStyleDescription
     * @generated
     */
    EClass getOuterFlagNodeStyleDescription();

    /**
     * Returns the meta object for the reference
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.OuterFlagNodeStyleDescription#getBackground
     * <em>Background</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the reference '<em>Background</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.OuterFlagNodeStyleDescription#getBackground()
     * @see #getOuterFlagNodeStyleDescription()
     * @generated
     */
    EReference getOuterFlagNodeStyleDescription_Background();

    /**
     * Returns the meta object for class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription <em>Cuboid Node Style
     * Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Cuboid Node Style Description</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription
     * @generated
     */
    EClass getCuboidNodeStyleDescription();

    /**
     * Returns the meta object for the reference
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription#getBackground
     * <em>Background</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the reference '<em>Background</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription#getBackground()
     * @see #getCuboidNodeStyleDescription()
     * @generated
     */
    EReference getCuboidNodeStyleDescription_Background();

    /**
     * Returns the meta object for class
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription <em>Custom Image
     * Node Style Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Custom Image Node Style Description</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription
     * @generated
     */
    EClass getCustomImageNodeStyleDescription();

    /**
     * Returns the meta object for the reference
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription#getBackground
     * <em>Background</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the reference '<em>Background</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription#getBackground()
     * @see #getCustomImageNodeStyleDescription()
     * @generated
     */
    EReference getCustomImageNodeStyleDescription_Background();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription#getShape
     * <em>Shape</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Shape</em>'.
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription#getShape()
     * @see #getCustomImageNodeStyleDescription()
     * @generated
     */
    EAttribute getCustomImageNodeStyleDescription_Shape();

    /**
     * Returns the factory that creates the instances of the model. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the factory that creates the instances of the model.
     * @generated
     */
    PapyrusCustomNodesFactory getPapyrusCustomNodesFactory();

    /**
     * <!-- begin-user-doc --> Defines literals for the meta objects that represent
     * <ul>
     * <li>each class,</li>
     * <li>each feature of each class,</li>
     * <li>each operation of each class,</li>
     * <li>each enum,</li>
     * <li>and each data type</li>
     * </ul>
     * <!-- end-user-doc -->
     *
     * @generated
     */
    interface Literals {
        /**
         * The meta object literal for the
         * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PackageNodeStyleDescriptionImpl
         * <em>Package Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PackageNodeStyleDescriptionImpl
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getPackageNodeStyleDescription()
         * @generated
         */
        EClass PACKAGE_NODE_STYLE_DESCRIPTION = eINSTANCE.getPackageNodeStyleDescription();

        /**
         * The meta object literal for the '<em><b>Background</b></em>' reference feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @generated
         */
        EReference PACKAGE_NODE_STYLE_DESCRIPTION__BACKGROUND = eINSTANCE.getPackageNodeStyleDescription_Background();

        /**
         * The meta object literal for the
         * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.RectangleWithExternalLabelNodeStyleDescriptionImpl
         * <em>Rectangle With External Label Node Style Description</em>}' class. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.RectangleWithExternalLabelNodeStyleDescriptionImpl
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getRectangleWithExternalLabelNodeStyleDescription()
         * @generated
         */
        EClass RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION = eINSTANCE.getRectangleWithExternalLabelNodeStyleDescription();

        /**
         * The meta object literal for the '<em><b>Background</b></em>' reference feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @generated
         */
        EReference RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION__BACKGROUND = eINSTANCE.getRectangleWithExternalLabelNodeStyleDescription_Background();

        /**
         * The meta object literal for the
         * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.NoteNodeStyleDescriptionImpl <em>Note
         * Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.NoteNodeStyleDescriptionImpl
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getNoteNodeStyleDescription()
         * @generated
         */
        EClass NOTE_NODE_STYLE_DESCRIPTION = eINSTANCE.getNoteNodeStyleDescription();

        /**
         * The meta object literal for the '<em><b>Background</b></em>' reference feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @generated
         */
        EReference NOTE_NODE_STYLE_DESCRIPTION__BACKGROUND = eINSTANCE.getNoteNodeStyleDescription_Background();

        /**
         * The meta object literal for the
         * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.InnerFlagNodeStyleDescriptionImpl
         * <em>Inner Flag Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.InnerFlagNodeStyleDescriptionImpl
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getInnerFlagNodeStyleDescription()
         * @generated
         */
        EClass INNER_FLAG_NODE_STYLE_DESCRIPTION = eINSTANCE.getInnerFlagNodeStyleDescription();

        /**
         * The meta object literal for the '<em><b>Background</b></em>' reference feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @generated
         */
        EReference INNER_FLAG_NODE_STYLE_DESCRIPTION__BACKGROUND = eINSTANCE.getInnerFlagNodeStyleDescription_Background();

        /**
         * The meta object literal for the
         * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.OuterFlagNodeStyleDescriptionImpl
         * <em>Outer Flag Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.OuterFlagNodeStyleDescriptionImpl
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getOuterFlagNodeStyleDescription()
         * @generated
         */
        EClass OUTER_FLAG_NODE_STYLE_DESCRIPTION = eINSTANCE.getOuterFlagNodeStyleDescription();

        /**
         * The meta object literal for the '<em><b>Background</b></em>' reference feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @generated
         */
        EReference OUTER_FLAG_NODE_STYLE_DESCRIPTION__BACKGROUND = eINSTANCE.getOuterFlagNodeStyleDescription_Background();

        /**
         * The meta object literal for the
         * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CuboidNodeStyleDescriptionImpl <em>Cuboid
         * Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CuboidNodeStyleDescriptionImpl
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getCuboidNodeStyleDescription()
         * @generated
         */
        EClass CUBOID_NODE_STYLE_DESCRIPTION = eINSTANCE.getCuboidNodeStyleDescription();

        /**
         * The meta object literal for the '<em><b>Background</b></em>' reference feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @generated
         */
        EReference CUBOID_NODE_STYLE_DESCRIPTION__BACKGROUND = eINSTANCE.getCuboidNodeStyleDescription_Background();

        /**
         * The meta object literal for the
         * '{@link org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl
         * <em>Custom Image Node Style Description</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.CustomImageNodeStyleDescriptionImpl
         * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.impl.PapyrusCustomNodesPackageImpl#getCustomImageNodeStyleDescription()
         * @generated
         */
        EClass CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION = eINSTANCE.getCustomImageNodeStyleDescription();

        /**
         * The meta object literal for the '<em><b>Background</b></em>' reference feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @generated
         */
        EReference CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BACKGROUND = eINSTANCE.getCustomImageNodeStyleDescription_Background();

        /**
         * The meta object literal for the '<em><b>Shape</b></em>' attribute feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @generated
         */
        EAttribute CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__SHAPE = eINSTANCE.getCustomImageNodeStyleDescription_Shape();

    }

} // PapyrusCustomNodesPackage

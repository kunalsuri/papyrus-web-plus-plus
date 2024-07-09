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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CuboidNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.CustomImageNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.InnerFlagNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.NoteNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.OuterFlagNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PackageNodeStyleDescription;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesFactory;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage;
import org.eclipse.papyrus.web.customnodes.papyruscustomnodes.RectangleWithExternalLabelNodeStyleDescription;
import org.eclipse.sirius.components.view.ViewPackage;
import org.eclipse.sirius.components.view.diagram.DiagramPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!-- end-user-doc -->
 *
 * @generated
 */
public class PapyrusCustomNodesPackageImpl extends EPackageImpl implements PapyrusCustomNodesPackage {
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass packageNodeStyleDescriptionEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass rectangleWithExternalLabelNodeStyleDescriptionEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass noteNodeStyleDescriptionEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass innerFlagNodeStyleDescriptionEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass outerFlagNodeStyleDescriptionEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass cuboidNodeStyleDescriptionEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass customImageNodeStyleDescriptionEClass = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with {@link org.eclipse.emf.ecore.EPackage.Registry
     * EPackage.Registry} by the package package URI value.
     * <p>
     * Note: the correct way to create the package is via the static factory method {@link #init init()}, which also
     * performs initialization of the package, or returns the registered package, if one already exists. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see org.eclipse.papyrus.web.customnodes.papyruscustomnodes.PapyrusCustomNodesPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private PapyrusCustomNodesPackageImpl() {
        super(eNS_URI, PapyrusCustomNodesFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
     *
     * <p>
     * This method is used to initialize {@link PapyrusCustomNodesPackage#eINSTANCE} when that field is accessed.
     * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static PapyrusCustomNodesPackage init() {
        if (isInited)
            return (PapyrusCustomNodesPackage) EPackage.Registry.INSTANCE.getEPackage(PapyrusCustomNodesPackage.eNS_URI);

        // Obtain or create and register package
        Object registeredPapyrusCustomNodesPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
        PapyrusCustomNodesPackageImpl thePapyrusCustomNodesPackage = registeredPapyrusCustomNodesPackage instanceof PapyrusCustomNodesPackageImpl
                ? (PapyrusCustomNodesPackageImpl) registeredPapyrusCustomNodesPackage
                : new PapyrusCustomNodesPackageImpl();

        isInited = true;

        // Initialize simple dependencies
        DiagramPackage.eINSTANCE.eClass();
        ViewPackage.eINSTANCE.eClass();

        // Create package meta-data objects
        thePapyrusCustomNodesPackage.createPackageContents();

        // Initialize created meta-data
        thePapyrusCustomNodesPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        thePapyrusCustomNodesPackage.freeze();

        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(PapyrusCustomNodesPackage.eNS_URI, thePapyrusCustomNodesPackage);
        return thePapyrusCustomNodesPackage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getPackageNodeStyleDescription() {
        return this.packageNodeStyleDescriptionEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getPackageNodeStyleDescription_Background() {
        return (EReference) this.packageNodeStyleDescriptionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getRectangleWithExternalLabelNodeStyleDescription() {
        return this.rectangleWithExternalLabelNodeStyleDescriptionEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getRectangleWithExternalLabelNodeStyleDescription_Background() {
        return (EReference) this.rectangleWithExternalLabelNodeStyleDescriptionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getNoteNodeStyleDescription() {
        return this.noteNodeStyleDescriptionEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getNoteNodeStyleDescription_Background() {
        return (EReference) this.noteNodeStyleDescriptionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getInnerFlagNodeStyleDescription() {
        return this.innerFlagNodeStyleDescriptionEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getInnerFlagNodeStyleDescription_Background() {
        return (EReference) this.innerFlagNodeStyleDescriptionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getOuterFlagNodeStyleDescription() {
        return this.outerFlagNodeStyleDescriptionEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getOuterFlagNodeStyleDescription_Background() {
        return (EReference) this.outerFlagNodeStyleDescriptionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getCuboidNodeStyleDescription() {
        return this.cuboidNodeStyleDescriptionEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getCuboidNodeStyleDescription_Background() {
        return (EReference) this.cuboidNodeStyleDescriptionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getCustomImageNodeStyleDescription() {
        return this.customImageNodeStyleDescriptionEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getCustomImageNodeStyleDescription_Background() {
        return (EReference) this.customImageNodeStyleDescriptionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getCustomImageNodeStyleDescription_Shape() {
        return (EAttribute) this.customImageNodeStyleDescriptionEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public PapyrusCustomNodesFactory getPapyrusCustomNodesFactory() {
        return (PapyrusCustomNodesFactory) this.getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package. This method is guarded to have no affect on any invocation but
     * its first. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public void createPackageContents() {
        if (this.isCreated)
            return;
        this.isCreated = true;

        // Create classes and their features
        this.packageNodeStyleDescriptionEClass = this.createEClass(PACKAGE_NODE_STYLE_DESCRIPTION);
        this.createEReference(this.packageNodeStyleDescriptionEClass, PACKAGE_NODE_STYLE_DESCRIPTION__BACKGROUND);

        this.rectangleWithExternalLabelNodeStyleDescriptionEClass = this.createEClass(RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION);
        this.createEReference(this.rectangleWithExternalLabelNodeStyleDescriptionEClass, RECTANGLE_WITH_EXTERNAL_LABEL_NODE_STYLE_DESCRIPTION__BACKGROUND);

        this.noteNodeStyleDescriptionEClass = this.createEClass(NOTE_NODE_STYLE_DESCRIPTION);
        this.createEReference(this.noteNodeStyleDescriptionEClass, NOTE_NODE_STYLE_DESCRIPTION__BACKGROUND);

        this.innerFlagNodeStyleDescriptionEClass = this.createEClass(INNER_FLAG_NODE_STYLE_DESCRIPTION);
        this.createEReference(this.innerFlagNodeStyleDescriptionEClass, INNER_FLAG_NODE_STYLE_DESCRIPTION__BACKGROUND);

        this.outerFlagNodeStyleDescriptionEClass = this.createEClass(OUTER_FLAG_NODE_STYLE_DESCRIPTION);
        this.createEReference(this.outerFlagNodeStyleDescriptionEClass, OUTER_FLAG_NODE_STYLE_DESCRIPTION__BACKGROUND);

        this.cuboidNodeStyleDescriptionEClass = this.createEClass(CUBOID_NODE_STYLE_DESCRIPTION);
        this.createEReference(this.cuboidNodeStyleDescriptionEClass, CUBOID_NODE_STYLE_DESCRIPTION__BACKGROUND);

        this.customImageNodeStyleDescriptionEClass = this.createEClass(CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION);
        this.createEReference(this.customImageNodeStyleDescriptionEClass, CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__BACKGROUND);
        this.createEAttribute(this.customImageNodeStyleDescriptionEClass, CUSTOM_IMAGE_NODE_STYLE_DESCRIPTION__SHAPE);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model. This method is guarded to have no affect on any
     * invocation but its first. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public void initializePackageContents() {
        if (this.isInitialized)
            return;
        this.isInitialized = true;

        // Initialize package
        this.setName(eNAME);
        this.setNsPrefix(eNS_PREFIX);
        this.setNsURI(eNS_URI);

        // Obtain other dependent packages
        DiagramPackage theDiagramPackage = (DiagramPackage) EPackage.Registry.INSTANCE.getEPackage(DiagramPackage.eNS_URI);
        ViewPackage theViewPackage = (ViewPackage) EPackage.Registry.INSTANCE.getEPackage(ViewPackage.eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes
        this.packageNodeStyleDescriptionEClass.getESuperTypes().add(theDiagramPackage.getNodeStyleDescription());
        this.rectangleWithExternalLabelNodeStyleDescriptionEClass.getESuperTypes().add(theDiagramPackage.getNodeStyleDescription());
        this.noteNodeStyleDescriptionEClass.getESuperTypes().add(theDiagramPackage.getNodeStyleDescription());
        this.innerFlagNodeStyleDescriptionEClass.getESuperTypes().add(theDiagramPackage.getNodeStyleDescription());
        this.outerFlagNodeStyleDescriptionEClass.getESuperTypes().add(theDiagramPackage.getNodeStyleDescription());
        this.cuboidNodeStyleDescriptionEClass.getESuperTypes().add(theDiagramPackage.getNodeStyleDescription());
        this.customImageNodeStyleDescriptionEClass.getESuperTypes().add(theDiagramPackage.getNodeStyleDescription());

        // Initialize classes, features, and operations; add parameters
        this.initEClass(this.packageNodeStyleDescriptionEClass, PackageNodeStyleDescription.class, "PackageNodeStyleDescription", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        this.initEReference(this.getPackageNodeStyleDescription_Background(), theViewPackage.getUserColor(), null, "background", null, 0, 1, PackageNodeStyleDescription.class, !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        this.initEClass(this.rectangleWithExternalLabelNodeStyleDescriptionEClass, RectangleWithExternalLabelNodeStyleDescription.class, "RectangleWithExternalLabelNodeStyleDescription", !IS_ABSTRACT,
                !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        this.initEReference(this.getRectangleWithExternalLabelNodeStyleDescription_Background(), theViewPackage.getUserColor(), null, "background", null, 0, 1,
                RectangleWithExternalLabelNodeStyleDescription.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);

        this.initEClass(this.noteNodeStyleDescriptionEClass, NoteNodeStyleDescription.class, "NoteNodeStyleDescription", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        this.initEReference(this.getNoteNodeStyleDescription_Background(), theViewPackage.getUserColor(), null, "background", null, 0, 1, NoteNodeStyleDescription.class, !IS_TRANSIENT, !IS_VOLATILE,
                IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        this.initEClass(this.innerFlagNodeStyleDescriptionEClass, InnerFlagNodeStyleDescription.class, "InnerFlagNodeStyleDescription", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        this.initEReference(this.getInnerFlagNodeStyleDescription_Background(), theViewPackage.getUserColor(), null, "background", null, 0, 1, InnerFlagNodeStyleDescription.class, !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        this.initEClass(this.outerFlagNodeStyleDescriptionEClass, OuterFlagNodeStyleDescription.class, "OuterFlagNodeStyleDescription", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        this.initEReference(this.getOuterFlagNodeStyleDescription_Background(), theViewPackage.getUserColor(), null, "background", null, 0, 1, OuterFlagNodeStyleDescription.class, !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        this.initEClass(this.cuboidNodeStyleDescriptionEClass, CuboidNodeStyleDescription.class, "CuboidNodeStyleDescription", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        this.initEReference(this.getCuboidNodeStyleDescription_Background(), theViewPackage.getUserColor(), null, "background", null, 0, 1, CuboidNodeStyleDescription.class, !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        this.initEClass(this.customImageNodeStyleDescriptionEClass, CustomImageNodeStyleDescription.class, "CustomImageNodeStyleDescription", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        this.initEReference(this.getCustomImageNodeStyleDescription_Background(), theViewPackage.getUserColor(), null, "background", null, 0, 1, CustomImageNodeStyleDescription.class, !IS_TRANSIENT,
                !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        this.initEAttribute(this.getCustomImageNodeStyleDescription_Shape(), theViewPackage.getInterpretedExpression(), "shape", null, 0, 1, CustomImageNodeStyleDescription.class, !IS_TRANSIENT,
                !IS_VOLATILE,
                IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Create resource
        this.createResource(eNS_URI);
    }

} // PapyrusCustomNodesPackageImpl

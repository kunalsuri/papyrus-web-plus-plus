/*****************************************************************************
 * Copyright (c) 2022, 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 190, 229
 *****************************************************************************/
package org.eclipse.papyrus.web.services.clazz;

import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.CONTENT_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.HOLDER_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.SHARED_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.uml.domain.services.EMFUtils;
import org.eclipse.papyrus.uml.domain.services.internal.helpers.ClassifierUtils;
import org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters;
import org.eclipse.papyrus.web.application.representations.aqlservices.clazz.ClassDiagramService;
import org.eclipse.papyrus.web.application.representations.uml.CDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UMLMetamodelHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.services.AbstractDiagramTest;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.papyrus.web.utils.AssociationTestCaseBuilder;
import org.eclipse.papyrus.web.utils.ElementMatcher;
import org.eclipse.papyrus.web.utils.LabelStyleCheck;
import org.eclipse.papyrus.web.utils.SynchronizedFeatureEdgeTestHelper;
import org.eclipse.sirius.components.diagrams.ArrowStyle;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.PackageMerge;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.Usage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class gathering integration test regarding creation in the Class Diagram.
 *
 * @author Arthur Daussy
 */
@SpringBootTest
@WebAppConfiguration
public class ClassDiagramTests extends AbstractDiagramTest {
    private static final String SHARED_HOLDER_SUFFIX = SHARED_SUFFIX + UNDERSCORE + HOLDER_SUFFIX;

    private static final String SHARED_CONTENT_SUFFIX = SHARED_SUFFIX + UNDERSCORE + CONTENT_SUFFIX;

    private static final IdBuilder ID_BUILDER = new IdBuilder(CDDiagramDescriptionBuilder.CD_PREFIX, new UMLMetamodelHelper());

    private static final String CD_COMMENT = ID_BUILDER.getDomainNodeName(UML.getComment());

    private static final String CD_COMMENT_SHARED = ID_BUILDER.getSpecializedDomainNodeName(UML.getComment(), SHARED_SUFFIX);

    private static final String CD_PACKAGE_TOP_HOLDER = ID_BUILDER.getSpecializedDomainNodeName(UML.getPackage(), HOLDER_SUFFIX);

    private static final String CD_PACKAGE_TOP_CONTENT = ID_BUILDER.getSpecializedDomainNodeName(UML.getPackage(), CONTENT_SUFFIX);

    private static final String CD_MODEL_TOP_HOLDER = ID_BUILDER.getSpecializedDomainNodeName(UML.getModel(), HOLDER_SUFFIX);

    private static final String CD_MODEL_TOP_CONTENT = ID_BUILDER.getSpecializedDomainNodeName(UML.getModel(), CONTENT_SUFFIX);

    private static final String CD_PACKAGE_SHARED_HOLDER = ID_BUILDER.getSpecializedDomainNodeName(UML.getPackage(), SHARED_HOLDER_SUFFIX);

    private static final String CD_PACKAGE_SHARED_CONTENT = ID_BUILDER.getSpecializedDomainNodeName(UML.getPackage(), SHARED_CONTENT_SUFFIX);

    private static final String CD_MODEL_SHARED_HOLDER = ID_BUILDER.getSpecializedDomainNodeName(UML.getModel(), SHARED_HOLDER_SUFFIX);

    private static final String CD_MODEL_SHARED_CONTENT = ID_BUILDER.getSpecializedDomainNodeName(UML.getModel(), SHARED_CONTENT_SUFFIX);

    private static final String CD_CLASS = ID_BUILDER.getDomainNodeName(UML.getClass_());

    private static final String CD_ENUMERRATION = ID_BUILDER.getDomainNodeName(UML.getEnumeration());

    private static final String CD_INTERFACE = ID_BUILDER.getDomainNodeName(UML.getInterface());

    private static final String CD_PRIMITIVE_TYPE = ID_BUILDER.getDomainNodeName(UML.getPrimitiveType());

    private static final String CD_DATA_TYPE = ID_BUILDER.getDomainNodeName(UML.getDataType());

    private static final String CD_CLASS_CHILD = ID_BUILDER.getSpecializedDomainNodeName(UML.getClass_(), SHARED_SUFFIX);

    private static final String CD_INTERFACE_CHILD = ID_BUILDER.getSpecializedDomainNodeName(UML.getInterface(), SHARED_SUFFIX);

    private static final String CD_PRIMITIVE_TYPE_CHILD = ID_BUILDER.getSpecializedDomainNodeName(UML.getPrimitiveType(), SHARED_SUFFIX);

    private static final String CD_DATA_TYPE_CHILD = ID_BUILDER.getSpecializedDomainNodeName(UML.getDataType(), SHARED_SUFFIX);

    private static final String CD_ENUMERATION_TYPE_CHILD = ID_BUILDER.getSpecializedDomainNodeName(UML.getEnumeration(), SHARED_SUFFIX);

    private static final String CD_ASSOCIATION = ID_BUILDER.getDomainBaseEdgeId(UML.getAssociation());

    private static final String CD_USAGE = ID_BUILDER.getDomainBaseEdgeId(UML.getUsage());

    private static String getLabelNodeId(EClass type, EClass parentType) {
        return ID_BUILDER.getListItemDomainNodeName(type, parentType);
    }

    private static String getAttributeCompartmentId(EClass owner) {
        return ID_BUILDER.getSpecializedCompartmentDomainNodeName(owner, CDDiagramDescriptionBuilder.ATTRIBUTES_COMPARTMENT_SUFFIX, SHARED_SUFFIX);
    }

    private static String getLiteralCompartmentId(EClass owner) {
        return ID_BUILDER.getSpecializedCompartmentDomainNodeName(owner, CDDiagramDescriptionBuilder.LITERAL_COMPARTMENT_SUFFIX, SHARED_SUFFIX);
    }

    private static String getOperationCompartmentId(EClass owner) {
        return ID_BUILDER.getSpecializedCompartmentDomainNodeName(owner, CDDiagramDescriptionBuilder.OPERATIONS_COMPARTMENT_SUFFIX, SHARED_SUFFIX);
    }

    private static String getNestedClassifierCompartmentId(EClass owner) {
        return ID_BUILDER.getSpecializedCompartmentDomainNodeName(owner, CDDiagramDescriptionBuilder.NESTED_CLASSIFIERS_COMPARTMENT_SUFFIX, SHARED_SUFFIX);
    }

    @Test
    public void checkRootPackageCreation() {
        this.init();
        Pair<EObject, Node> semanticAndNode = this.getServiceTester().assertRootCreation(UML.getPackage(), UML.getPackage_PackagedElement(), CD_PACKAGE_TOP_HOLDER);
        EObject semantic = semanticAndNode.getFirst();

        // Assert a content is created
        this.getDiagramHelper().assertGetUniqueMatchingNode(CD_PACKAGE_TOP_CONTENT, semantic);
    }

    @Test
    public void checkRootModelCreation() {
        this.init();
        Pair<EObject, Node> semanticAndNode = this.getServiceTester().assertRootCreation(UML.getModel(), UML.getPackage_PackagedElement(), CD_MODEL_TOP_HOLDER);
        EObject semantic = semanticAndNode.getFirst();

        // Assert a content is created
        this.getDiagramHelper().assertGetUniqueMatchingNode(CD_MODEL_TOP_CONTENT, semantic);

    }

    @Test
    public void checkConditionalLabelStyleOnClass() {
        Package pack = this.init();

        Class class1 = this.createIn(Class.class, pack);
        Node nodeClass = this.getDiagramHelper().createNodeInDiagram(CD_CLASS, class1);

        LabelStyleCheck.build(nodeClass).assertIsNotItalic().assertIsNotUnderline();

        // Test abstract on a class
        class1.setIsAbstract(true);
        this.getDiagramHelper().refresh();

        nodeClass = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_CLASS, class1);
        LabelStyleCheck.build(nodeClass).assertIsItalic().assertIsNotUnderline();
    }

    @Test
    public void checkConditionalLabelStyleOnOperation() {
        Package pack = this.init();

        Class class1 = this.createIn(Class.class, pack);
        this.getDiagramHelper().createNodeInDiagram(CD_CLASS, class1);

        // Test static and abstract on operation
        Operation op1 = this.createIn(Operation.class, class1);
        Node operationCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getClass_()), class1);
        Node op1Node = this.getDiagramHelper().createNodeInParent(getLabelNodeId(UML.getOperation(), UML.getClass_()), op1, operationCmp);

        LabelStyleCheck.build(op1Node).assertIsNotItalic().assertIsNotUnderline();

        op1.setIsStatic(true);
        this.getDiagramHelper().refresh();
        op1Node = this.getDiagramHelper().assertGetUniqueMatchingNode(getLabelNodeId(UML.getOperation(), UML.getClass_()), op1);

        LabelStyleCheck.build(op1Node).assertIsNotItalic().assertIsUnderline();

        op1.setIsAbstract(true);
        this.getDiagramHelper().refresh();
        op1Node = this.getDiagramHelper().assertGetUniqueMatchingNode(getLabelNodeId(UML.getOperation(), UML.getClass_()), op1);

        LabelStyleCheck.build(op1Node).assertIsItalic().assertIsUnderline();
    }

    @Test
    public void checkConditionalLabelStyleOnAttribute() {
        Package pack = this.init();

        Class class1 = this.createIn(Class.class, pack);
        this.getDiagramHelper().createNodeInDiagram(CD_CLASS, class1);

        // Test static on property
        Node attrCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getClass_()), class1);
        Property prop1 = this.createIn(Property.class, class1);
        Node prop1Node = this.getDiagramHelper().createNodeInParent(getLabelNodeId(UML.getProperty(), UML.getClass_()), prop1, attrCmp);
        LabelStyleCheck.build(prop1Node).assertIsNotItalic().assertIsNotUnderline();

        prop1.setIsStatic(true);
        this.getDiagramHelper().refresh();

        prop1Node = this.getDiagramHelper().assertGetUniqueMatchingNode(getLabelNodeId(UML.getProperty(), UML.getClass_()), prop1);
        LabelStyleCheck.build(prop1Node).assertIsNotItalic().assertIsUnderline();

    }

    @Test
    public void checkConditionalLabelStyleOnNestedClassifier() {
        Package pack = this.init();

        Class class1 = this.createIn(Class.class, pack);
        this.getDiagramHelper().createNodeInDiagram(CD_CLASS, class1);

        // Test abstract on nested class
        Class nestedClass = this.createIn(Class.class, class1);
        Node nestedClassifierCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getNestedClassifierCompartmentId(UML.getClass_()), class1);
        Node nestedNode = this.getDiagramHelper().createNodeInParent(getLabelNodeId(UML.getClassifier(), UML.getClass_()), nestedClass, nestedClassifierCmp);

        LabelStyleCheck.build(nestedNode).assertIsNotItalic().assertIsNotUnderline();

        nestedClass.setIsAbstract(true);
        this.getDiagramHelper().refresh();
        nestedNode = this.getDiagramHelper().assertGetUniqueMatchingNode(getLabelNodeId(UML.getClassifier(), UML.getClass_()), nestedClass);

        LabelStyleCheck.build(nestedNode).assertIsItalic().assertIsNotUnderline();
    }

    @Test
    public void checkRootCommentCreation() {
        this.init();
        this.getServiceTester().assertRootCreation(UML.getComment(), UML.getElement_OwnedComment(), CD_COMMENT);
    }

    @Test
    public void checkRootClassCreation() {
        this.init();
        Pair<EObject, Node> semanticAndNode = this.getServiceTester().assertRootCreation(UML.getClass_(), UML.getPackage_PackagedElement(), CD_CLASS);

        EObject semantic = semanticAndNode.getFirst();

        // Assert there is 3 compartments
        this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getClass_()), semantic);
        this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getClass_()), semantic);
        this.getDiagramHelper().assertGetUniqueMatchingNode(getNestedClassifierCompartmentId(UML.getClass_()), semantic);
    }

    @Test
    public void checkRootEnumerationCreation() {
        this.init();
        Pair<EObject, Node> semanticAndNode = this.getServiceTester().assertRootCreation(UML.getEnumeration(), UML.getPackage_PackagedElement(), CD_ENUMERRATION);

        EObject semantic = semanticAndNode.getFirst();

        // Assert there is 1 compartments
        this.getDiagramHelper().assertGetUniqueMatchingNode(getLiteralCompartmentId(UML.getEnumeration()), semantic);
    }

    @Test
    public void checkRootInterfaceCreation() {
        this.init();
        Pair<EObject, Node> semanticAndNode = this.getServiceTester().assertRootCreation(UML.getInterface(), UML.getPackage_PackagedElement(), CD_INTERFACE);

        EObject semantic = semanticAndNode.getFirst();

        // Assert there is 3 compartments
        this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getInterface()), semantic);
        this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getInterface()), semantic);
        this.getDiagramHelper().assertGetUniqueMatchingNode(getNestedClassifierCompartmentId(UML.getInterface()), semantic);
    }

    @Test
    public void checkRootPrimitiveTypeCreation() {
        this.init();
        Pair<EObject, Node> semanticAndNode = this.getServiceTester().assertRootCreation(UML.getPrimitiveType(), UML.getPackage_PackagedElement(), CD_PRIMITIVE_TYPE);

        EObject semantic = semanticAndNode.getFirst();

        // Assert there is 2 compartments
        this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getPrimitiveType()), semantic);
        this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getPrimitiveType()), semantic);
    }

    @Test
    public void checkRootDataTypeCreation() {
        this.init();
        Pair<EObject, Node> semanticAndNode = this.getServiceTester().assertRootCreation(UML.getDataType(), UML.getPackage_PackagedElement(), CD_DATA_TYPE);

        EObject semantic = semanticAndNode.getFirst();

        // Assert there is 2 compartments
        this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getDataType()), semantic);
        this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getDataType()), semantic);
    }

    @Test
    public void checkClassChildren() {
        Package pack = this.init();

        Class clazz = this.createIn(Class.class, pack);

        this.getServiceTester().assertSemanticDrop(clazz, null, CD_CLASS);
        this.getServiceTester().assertSemanticDrop(clazz, null, CD_CLASS);

        this.getDiagramHelper().refresh();

        // Test for attributes
        Node attributeCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getClass_()), clazz);
        this.getServiceTester().assertChildAndSiblingCreation(attributeCmp, UML.getProperty(), UML.getStructuredClassifier_OwnedAttribute(), getLabelNodeId(UML.getProperty(), UML.getClass_()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Property.class, clazz), attributeCmp, getLabelNodeId(UML.getProperty(), UML.getClass_()));

        // Test for operations
        Node operationCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getClass_()), clazz);
        this.getServiceTester().assertChildAndSiblingCreation(operationCmp, UML.getOperation(), UML.getClass_OwnedOperation(), getLabelNodeId(UML.getOperation(), UML.getClass_()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Operation.class, clazz), operationCmp, getLabelNodeId(UML.getOperation(), UML.getClass_()));

        // Test for nested classifier
        Node nestedClassifierCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getNestedClassifierCompartmentId(UML.getClass_()), clazz);
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getClass_(), UML.getClass_NestedClassifier(), getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Class.class, clazz), nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getPrimitiveType(), UML.getClass_NestedClassifier(), getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertSemanticDrop(this.createIn(PrimitiveType.class, clazz), nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getDataType(), UML.getClass_NestedClassifier(), getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getEnumeration(), UML.getClass_NestedClassifier(), getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Enumeration.class, clazz), nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getInterface(), UML.getClass_NestedClassifier(), getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Interface.class, clazz), nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getClass_()));

    }

    @Test
    public void checkEnumerationChildren() {
        Package pack = this.init();

        Enumeration enumeration = this.createIn(Enumeration.class, pack);

        this.getServiceTester().assertSemanticDrop(enumeration, null, CD_ENUMERRATION);

        this.getDiagramHelper().refresh();

        // Test for attributes
        Node literalCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getLiteralCompartmentId(UML.getEnumeration()), enumeration);
        this.getServiceTester().assertChildAndSiblingCreation(literalCmp, UML.getEnumerationLiteral(), UML.getEnumeration_OwnedLiteral(),
                getLabelNodeId(UML.getEnumerationLiteral(), UML.getEnumeration()));
        this.getServiceTester().assertSemanticDrop(this.createIn(EnumerationLiteral.class, enumeration), literalCmp, getLabelNodeId(UML.getEnumerationLiteral(), UML.getEnumeration()));

    }

    @Test
    public void checkChildrenCreationOnInterface() {
        Package pack = this.init();

        Interface aInterface = this.createIn(Interface.class, pack);

        this.getServiceTester().assertSemanticDrop(aInterface, null, CD_INTERFACE);

        this.getDiagramHelper().refresh();

        // Test for attributes
        Node attributeCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getInterface()), aInterface);
        this.getServiceTester().assertChildAndSiblingCreation(attributeCmp, UML.getProperty(), UML.getInterface_OwnedAttribute(), getLabelNodeId(UML.getProperty(), UML.getInterface()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Property.class, aInterface), attributeCmp, getLabelNodeId(UML.getProperty(), UML.getInterface()));

        // Test for operations
        Node operationCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getInterface()), aInterface);
        this.getServiceTester().assertChildAndSiblingCreation(operationCmp, UML.getOperation(), UML.getInterface_OwnedOperation(), getLabelNodeId(UML.getOperation(), UML.getInterface()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Operation.class, aInterface), operationCmp, getLabelNodeId(UML.getOperation(), UML.getInterface()));

        // Test for nested classifier
        Node nestedClassifierCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getNestedClassifierCompartmentId(UML.getInterface()), aInterface);
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getClass_(), UML.getInterface_NestedClassifier(), getLabelNodeId(UML.getClassifier(), UML.getInterface()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Class.class, aInterface), nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getInterface()));
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getPrimitiveType(), UML.getInterface_NestedClassifier(),
                getLabelNodeId(UML.getClassifier(), UML.getInterface()));
        this.getServiceTester().assertSemanticDrop(this.createIn(PrimitiveType.class, aInterface), nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getInterface()));
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getDataType(), UML.getInterface_NestedClassifier(), getLabelNodeId(UML.getClassifier(), UML.getInterface()));
        this.getServiceTester().assertSemanticDrop(this.createIn(DataType.class, aInterface), nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getInterface()));
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getEnumeration(), UML.getInterface_NestedClassifier(), getLabelNodeId(UML.getClassifier(), UML.getInterface()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Enumeration.class, aInterface), nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getInterface()));
        this.getServiceTester().assertChildAndSiblingCreation(nestedClassifierCmp, UML.getInterface(), UML.getInterface_NestedClassifier(), getLabelNodeId(UML.getClassifier(), UML.getInterface()));
        this.getServiceTester().assertSemanticDrop(this.createIn(Interface.class, aInterface), nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getInterface()));

    }

    /**
     * Checks that the item in the compartment of a class are displayed on one line even if they own stereotypes.
     */
    @Test
    public void checkListItemLabels() {
        Package pack = this.init();

        Class clazz = this.createIn(Class.class, pack);

        Interface nestedInterface = this.createIn(Interface.class, clazz);
        nestedInterface.setName("Interface1");

        Profile standardProfile = (Profile) this.getResourceSet().getEObject(URI.createURI("pathmap://UML_PROFILES/Standard.profile.uml#_0"), true);
        pack.applyProfile(standardProfile);
        assertEquals(1, pack.getAllAppliedProfiles().size());

        this.getServiceTester().assertSemanticDrop(clazz, null, CD_CLASS);

        this.getDiagramHelper().refresh();

        Node nestedClassifierCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getNestedClassifierCompartmentId(UML.getClass_()), clazz);
        Node interfaceNode = this.getServiceTester().assertSemanticDrop(nestedInterface, nestedClassifierCmp, getLabelNodeId(UML.getClassifier(), UML.getClass_()));

        // Checks that there is no keyword and everything is on one line
        assertEquals("Interface1", interfaceNode.getInsideLabel().getText());

        Stereotype stereotype = EMFUtils.allContainedObjectOfType(standardProfile, Stereotype.class)//
                .filter(s -> "Realization".equals(s.getName()))//
                .findFirst().get();

        nestedInterface.applyStereotype(stereotype);

        assertEquals(1, nestedInterface.getAppliedStereotypes().size());

        this.getDiagramHelper().refresh();

        interfaceNode = this.getDiagramHelper().assertGetUniqueMatchingNode(getLabelNodeId(UML.getClassifier(), UML.getClass_()), nestedInterface);
        assertEquals(UMLCharacters.ST_LEFT + "Realization" + UMLCharacters.ST_RIGHT + " Interface1", interfaceNode.getInsideLabel().getText());

    }

    @Test
    public void checkChildrenDnDOnClass() {
        Package pack = this.init();

        Class clazz = this.createIn(Class.class, pack);

        this.getServiceTester().assertSemanticDrop(clazz, null, CD_CLASS);

        this.getDiagramHelper().refresh();

        // Test for attributes
        Property attr1 = this.createIn(Property.class, clazz);
        Property attr2 = this.createIn(Property.class, clazz);
        Node attributeCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getClass_()), clazz);
        this.getServiceTester().assertChildAndSiblingSemanticDrop(attributeCmp, attr1, attr2, getLabelNodeId(UML.getProperty(), UML.getClass_()));

        // Test for operations
        Operation op1 = this.createIn(Operation.class, clazz);
        Operation op2 = this.createIn(Operation.class, clazz);
        Node operationCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getClass_()), clazz);
        this.getServiceTester().assertChildAndSiblingSemanticDrop(operationCmp, op1, op2, getLabelNodeId(UML.getOperation(), UML.getClass_()));
        //
        // Test for nested classifier
        Class nestedClass = this.createIn(Class.class, clazz);
        PrimitiveType nestedPrimitiveType = this.createIn(PrimitiveType.class, clazz);
        Enumeration nestedEnumeration = this.createIn(Enumeration.class, clazz);
        DataType nestedDataType = this.createIn(DataType.class, clazz);
        Interface nestedInterface = this.createIn(Interface.class, clazz);

        Class nestedClass2 = this.createIn(Class.class, clazz);
        PrimitiveType nestedPrimitiveType2 = this.createIn(PrimitiveType.class, clazz);
        Enumeration nestedEnumeration2 = this.createIn(Enumeration.class, clazz);
        DataType nestedDataType2 = this.createIn(DataType.class, clazz);
        Interface nestedInterface2 = this.createIn(Interface.class, clazz);

        Node nestedClassifierCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getNestedClassifierCompartmentId(UML.getClass_()), clazz);
        this.getServiceTester().assertChildAndSiblingSemanticDrop(nestedClassifierCmp, nestedClass, nestedClass2, getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertChildAndSiblingSemanticDrop(nestedClassifierCmp, nestedPrimitiveType, nestedPrimitiveType2, getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertChildAndSiblingSemanticDrop(nestedClassifierCmp, nestedDataType, nestedDataType2, getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertChildAndSiblingSemanticDrop(nestedClassifierCmp, nestedEnumeration, nestedEnumeration2, getLabelNodeId(UML.getClassifier(), UML.getClass_()));
        this.getServiceTester().assertChildAndSiblingSemanticDrop(nestedClassifierCmp, nestedInterface, nestedInterface2, getLabelNodeId(UML.getClassifier(), UML.getClass_()));

    }

    @Test
    public void checkChildrenDnDOnPrimitiveType() {
        Package pack = this.init();

        PrimitiveType primitiveType = this.createIn(PrimitiveType.class, pack);

        this.getServiceTester().assertSemanticDrop(primitiveType, null, CD_PRIMITIVE_TYPE);

        this.getDiagramHelper().refresh();

        // Test for attributes
        Property attr1 = this.createIn(Property.class, primitiveType);
        Property attr2 = this.createIn(Property.class, primitiveType);
        Node attributeCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getPrimitiveType()), primitiveType);
        this.getServiceTester().assertChildAndSiblingSemanticDrop(attributeCmp, attr1, attr2, getLabelNodeId(UML.getProperty(), UML.getPrimitiveType()));

        // Test for operations
        Operation op1 = this.createIn(Operation.class, primitiveType);
        Operation op2 = this.createIn(Operation.class, primitiveType);
        Node operationCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getPrimitiveType()), primitiveType);
        this.getServiceTester().assertChildAndSiblingSemanticDrop(operationCmp, op1, op2, getLabelNodeId(UML.getOperation(), UML.getPrimitiveType()));

    }

    @Test
    public void checkChildrenDnDOnDataType() {
        Package pack = this.init();

        DataType dataType = this.createIn(DataType.class, pack);

        this.getServiceTester().assertSemanticDrop(dataType, null, CD_DATA_TYPE);

        this.getDiagramHelper().refresh();

        // Test for attributes
        Property attr1 = this.createIn(Property.class, dataType);
        Property attr2 = this.createIn(Property.class, dataType);
        Node attributeCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getAttributeCompartmentId(UML.getDataType()), dataType);
        this.getServiceTester().assertChildAndSiblingSemanticDrop(attributeCmp, attr1, attr2, getLabelNodeId(UML.getProperty(), UML.getDataType()));

        // Test for operations
        Operation op1 = this.createIn(Operation.class, dataType);
        Operation op2 = this.createIn(Operation.class, dataType);
        Node operationCmp = this.getDiagramHelper().assertGetUniqueMatchingNode(getOperationCompartmentId(UML.getDataType()), dataType);
        this.getServiceTester().assertChildAndSiblingSemanticDrop(operationCmp, op1, op2, getLabelNodeId(UML.getOperation(), UML.getDataType()));

    }

    @Test
    public void checkModelChildren() {
        Package pack = this.init();

        Model model = this.createIn(Model.class, pack);
        this.getDiagramHelper().createNodeInDiagram(CD_MODEL_TOP_HOLDER, model);
        Node content = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_MODEL_TOP_CONTENT, model);

        this.checkPackageChildren(content, true);
    }

    private void checkPackageChildren(Node parentNode, boolean recurse) {
        // Model
        Node droppedModelNode = this.getServiceTester().assertChildCreationAndDrop(parentNode, Model.class, UML.getPackage_PackagedElement(), CD_MODEL_SHARED_HOLDER);
        if (recurse) {
            EObject model = this.getDiagramHelper().getSemanticElement(droppedModelNode);
            this.getDiagramHelper().assertGetUniqueMatchingNode(CD_MODEL_SHARED_HOLDER, model);
            Node contentNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_MODEL_SHARED_CONTENT, model);
            this.checkPackageChildren(contentNode, false);
        }

        // Package
        Node droppedPackageNodeHolder = this.getServiceTester().assertChildCreationAndDrop(parentNode, Package.class, UML.getPackage_PackagedElement(), CD_PACKAGE_SHARED_HOLDER);
        EObject pack = this.getDiagramHelper().getSemanticElement(droppedPackageNodeHolder);
        Node droppedPackageNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_PACKAGE_SHARED_CONTENT, pack);

        if (recurse) {
            EObject subpack = this.getDiagramHelper().getSemanticElement(droppedPackageNodeContent);
            this.getDiagramHelper().assertGetUniqueMatchingNode(CD_PACKAGE_SHARED_HOLDER, subpack);
            Node contentNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_PACKAGE_SHARED_CONTENT, subpack);
            this.checkPackageChildren(contentNode, false);
        }

        // Comment
        this.getServiceTester().assertChildCreationAndDrop(droppedPackageNodeContent, Comment.class, UML.getElement_OwnedComment(), CD_COMMENT_SHARED);

        // Class in Package
        this.getServiceTester().assertChildCreationAndDrop(droppedPackageNodeContent, Class.class, UML.getPackage_PackagedElement(), CD_CLASS_CHILD);

        // Interface
        this.getServiceTester().assertChildCreationAndDrop(droppedPackageNodeContent, Interface.class, UML.getPackage_PackagedElement(), CD_INTERFACE_CHILD);

        // PrimitiveType
        this.getServiceTester().assertChildCreationAndDrop(droppedPackageNodeContent, PrimitiveType.class, UML.getPackage_PackagedElement(), CD_PRIMITIVE_TYPE_CHILD);

        // DataType
        this.getServiceTester().assertChildCreationAndDrop(droppedPackageNodeContent, DataType.class, UML.getPackage_PackagedElement(), CD_DATA_TYPE_CHILD);

        // Enumeration
        this.getServiceTester().assertChildCreationAndDrop(droppedPackageNodeContent, Enumeration.class, UML.getPackage_PackagedElement(), CD_ENUMERATION_TYPE_CHILD);
    }

    @Test
    public void checkPackageChildren() {
        Package pack = this.init();

        this.createIn(Package.class, pack);
        this.getDiagramHelper().createNodeInDiagram(CD_PACKAGE_TOP_HOLDER, pack);
        Node content = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_PACKAGE_TOP_CONTENT, pack);

        this.checkPackageChildren(content, true);

    }

    @Test
    public void checkGeneralizationEdge() {
        Package pack = this.init();

        // From Class
        this.checkGeneralizationEdge(this.createIn(Class.class, pack), this.createIn(Class.class, pack));
        this.checkGeneralizationEdge(this.createIn(Class.class, pack), this.createIn(Interface.class, pack));
        this.checkGeneralizationEdge(this.createIn(Class.class, pack), this.createIn(DataType.class, pack));
        this.checkGeneralizationEdge(this.createIn(Class.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkGeneralizationEdge(this.createIn(Class.class, pack), this.createIn(Enumeration.class, pack));

        // From Interface
        this.checkGeneralizationEdge(this.createIn(Interface.class, pack), this.createIn(Class.class, pack));
        this.checkGeneralizationEdge(this.createIn(Interface.class, pack), this.createIn(Interface.class, pack));
        this.checkGeneralizationEdge(this.createIn(Interface.class, pack), this.createIn(DataType.class, pack));
        this.checkGeneralizationEdge(this.createIn(Interface.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkGeneralizationEdge(this.createIn(Interface.class, pack), this.createIn(Enumeration.class, pack));

        // From DataType
        this.checkGeneralizationEdge(this.createIn(DataType.class, pack), this.createIn(Class.class, pack));
        this.checkGeneralizationEdge(this.createIn(DataType.class, pack), this.createIn(Interface.class, pack));
        this.checkGeneralizationEdge(this.createIn(DataType.class, pack), this.createIn(DataType.class, pack));
        this.checkGeneralizationEdge(this.createIn(DataType.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkGeneralizationEdge(this.createIn(DataType.class, pack), this.createIn(Enumeration.class, pack));

        // From PrimitiveType
        this.checkGeneralizationEdge(this.createIn(PrimitiveType.class, pack), this.createIn(Class.class, pack));
        this.checkGeneralizationEdge(this.createIn(PrimitiveType.class, pack), this.createIn(Interface.class, pack));
        this.checkGeneralizationEdge(this.createIn(PrimitiveType.class, pack), this.createIn(DataType.class, pack));
        this.checkGeneralizationEdge(this.createIn(PrimitiveType.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkGeneralizationEdge(this.createIn(PrimitiveType.class, pack), this.createIn(Enumeration.class, pack));

        // From Enumeration
        this.checkGeneralizationEdge(this.createIn(Enumeration.class, pack), this.createIn(Class.class, pack));
        this.checkGeneralizationEdge(this.createIn(Enumeration.class, pack), this.createIn(Interface.class, pack));
        this.checkGeneralizationEdge(this.createIn(Enumeration.class, pack), this.createIn(DataType.class, pack));
        this.checkGeneralizationEdge(this.createIn(Enumeration.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkGeneralizationEdge(this.createIn(Enumeration.class, pack), this.createIn(Enumeration.class, pack));

        // Check with some children classifier mappings
        Interface targetInterface = this.createIn(Interface.class, pack);

        // Check with class inside a package
        Package subPack = this.createIn(Package.class, pack);
        this.getServiceTester().assertSemanticDrop(subPack, null, CD_PACKAGE_TOP_HOLDER);
        Node subPackageContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_PACKAGE_TOP_CONTENT, subPack);

        Class nestedClass = this.createIn(Class.class, subPack);
        Node nestedClassNode = this.getServiceTester().assertSemanticDrop(nestedClass, subPackageContent, CD_CLASS_CHILD);

        Node interfaceNode = this.getServiceTester().assertSemanticDrop(targetInterface, null, CD_INTERFACE);
        this.getServiceTester().buildDomainBasedEdgeTestHelper(ID_BUILDER)//
                .withSource(nestedClass)//
                .withTarget(targetInterface)//
                .withDomainBasedEdge(this.createGeneralization(nestedClass, targetInterface))//
                .withSourceNodeId(nestedClassNode.getId())//
                .withTargetNodeId(interfaceNode.getId()).build()//
                .updateDiagram()//
                .assertDisplayedOnDiagram();

        // Check with interface inside a package
        Class rootClass = this.createIn(Class.class, pack);
        Node rootClassNode = this.getServiceTester().assertSemanticDrop(rootClass, null, CD_CLASS);

        Interface nestedInsterface = this.createIn(Interface.class, subPack);
        Node nestedInterfaceNode = this.getServiceTester().assertSemanticDrop(nestedInsterface, subPackageContent, CD_INTERFACE_CHILD);

        this.getServiceTester().buildDomainBasedEdgeTestHelper(ID_BUILDER)//
                .withSource(nestedClass)//
                .withTarget(targetInterface)//
                .withDomainBasedEdge(this.createGeneralization(rootClass, nestedInsterface))//
                .withSourceNodeId(rootClassNode.getId())//
                .withTargetNodeId(nestedInterfaceNode.getId()).build()//
                .updateDiagram()//
                .assertDisplayedOnDiagram();

        // Check the creation
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(nestedClass)//
                .withSourceNodeId(nestedClassNode.getId())//
                .withTarget(targetInterface)//
                .withTargetNodeId(interfaceNode.getId())//
                .withExpectedContainementRef(UML.getClassifier_Generalization())//
                .withExpectedOwner(nestedClass)//
                .withType(UML.getGeneralization())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();

        // Check with both the interface and the class inside a package
        this.getServiceTester().buildDomainBasedEdgeTestHelper(ID_BUILDER)//
                .withSource(nestedClass)//
                .withTarget(targetInterface)//
                .withDomainBasedEdge(this.createGeneralization(nestedClass, nestedInsterface))//
                .withSourceNodeId(nestedClassNode.getId())//
                .withTargetNodeId(nestedInterfaceNode.getId()).build()//
                .updateDiagram()//
                .assertDisplayedOnDiagram();

    }

    @Test
    public void checkInterfaceRealizationEdge() {
        Package pack = this.init();

        // From Package
        Interface targetInterface = this.createIn(Interface.class, pack);
        this.checkInterfaceRealizationEdge(this.createIn(Class.class, pack), targetInterface);

        // Check with class inside a package
        Package subPack = this.createIn(Package.class, pack);

        this.getServiceTester().assertSemanticDrop(subPack, null, CD_PACKAGE_TOP_HOLDER);
        Node subPackageNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_PACKAGE_TOP_CONTENT, subPack);
        Class nestedClass = this.createIn(Class.class, subPack);
        Node nestedClassNode = this.getServiceTester().assertSemanticDrop(nestedClass, subPackageNodeContent, CD_CLASS_CHILD);

        Node interfaceNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_INTERFACE, targetInterface);
        this.getServiceTester().buildDomainBasedEdgeTestHelper(ID_BUILDER)//
                .withSource(nestedClass)//
                .withTarget(targetInterface)//
                .withDomainBasedEdge(this.createInterfaceRealization(nestedClass, targetInterface))//
                .withSourceNodeId(nestedClassNode.getId())//
                .withTargetNodeId(interfaceNode.getId()).build()//
                .updateDiagram()//
                .assertDisplayedOnDiagram();

        // Check with interface inside a package
        Class rootClass = this.createIn(Class.class, pack);
        Node rootClassNode = this.getServiceTester().assertSemanticDrop(rootClass, null, CD_CLASS);

        Interface nestedInsterface = this.createIn(Interface.class, subPack);
        Node nestedInterfaceNode = this.getServiceTester().assertSemanticDrop(nestedInsterface, subPackageNodeContent, CD_INTERFACE_CHILD);

        this.getServiceTester().buildDomainBasedEdgeTestHelper(ID_BUILDER)//
                .withSource(nestedClass)//
                .withTarget(targetInterface)//
                .withDomainBasedEdge(this.createInterfaceRealization(rootClass, nestedInsterface))//
                .withSourceNodeId(rootClassNode.getId())//
                .withTargetNodeId(nestedInterfaceNode.getId()).build()//
                .updateDiagram()//
                .assertDisplayedOnDiagram();

        // Check with both the interface and the class inside a package
        this.getServiceTester().buildDomainBasedEdgeTestHelper(ID_BUILDER)//
                .withSource(nestedClass)//
                .withTarget(targetInterface)//
                .withDomainBasedEdge(this.createInterfaceRealization(nestedClass, nestedInsterface))//
                .withSourceNodeId(nestedClassNode.getId())//
                .withTargetNodeId(nestedInterfaceNode.getId()).build()//
                .updateDiagram()//
                .assertDisplayedOnDiagram();

    }

    @Test
    public void checkDependencyEdge() {
        Package pack = this.init();

        // From Package
        this.checkDependencyEdge(this.createIn(Package.class, pack), this.createIn(Package.class, pack));
        this.checkDependencyEdge(this.createIn(Package.class, pack), this.createIn(Model.class, pack));
        this.checkDependencyEdge(this.createIn(Package.class, pack), this.createIn(Class.class, pack));
        this.checkDependencyEdge(this.createIn(Package.class, pack), this.createIn(Interface.class, pack));
        this.checkDependencyEdge(this.createIn(Package.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkDependencyEdge(this.createIn(Package.class, pack), this.createIn(Enumeration.class, pack));

        // From Model
        this.checkDependencyEdge(this.createIn(Model.class, pack), this.createIn(Package.class, pack));
        this.checkDependencyEdge(this.createIn(Model.class, pack), this.createIn(Model.class, pack));
        this.checkDependencyEdge(this.createIn(Model.class, pack), this.createIn(Class.class, pack));
        this.checkDependencyEdge(this.createIn(Model.class, pack), this.createIn(Interface.class, pack));
        this.checkDependencyEdge(this.createIn(Model.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkDependencyEdge(this.createIn(Model.class, pack), this.createIn(DataType.class, pack));
        this.checkDependencyEdge(this.createIn(Model.class, pack), this.createIn(Enumeration.class, pack));

        // From Class
        this.checkDependencyEdge(pack, this.createIn(Class.class, pack), this.createIn(Package.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Class.class, pack), this.createIn(Model.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Class.class, pack), this.createIn(Class.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Class.class, pack), this.createIn(Interface.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Class.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Class.class, pack), this.createIn(DataType.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Class.class, pack), this.createIn(Enumeration.class, pack));

        // From Primitive Type
        this.checkDependencyEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Package.class, pack));
        this.checkDependencyEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Model.class, pack));
        this.checkDependencyEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Class.class, pack));
        this.checkDependencyEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Interface.class, pack));
        this.checkDependencyEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkDependencyEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(DataType.class, pack));
        this.checkDependencyEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Enumeration.class, pack));

        // From Data Type
        this.checkDependencyEdge(pack, this.createIn(DataType.class, pack), this.createIn(Package.class, pack));
        this.checkDependencyEdge(pack, this.createIn(DataType.class, pack), this.createIn(Model.class, pack));
        this.checkDependencyEdge(pack, this.createIn(DataType.class, pack), this.createIn(Class.class, pack));
        this.checkDependencyEdge(pack, this.createIn(DataType.class, pack), this.createIn(Interface.class, pack));
        this.checkDependencyEdge(pack, this.createIn(DataType.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkDependencyEdge(pack, this.createIn(DataType.class, pack), this.createIn(DataType.class, pack));
        this.checkDependencyEdge(pack, this.createIn(DataType.class, pack), this.createIn(Enumeration.class, pack));

        // From Enumeration
        this.checkDependencyEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Package.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Model.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Class.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Interface.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(DataType.class, pack));
        this.checkDependencyEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Enumeration.class, pack));
    }

    @Test
    public void checkUsageReconnectionEdge() {
        Package pack = this.init();

        Node sourcePackageNode = this.getServiceTester().assertSemanticDrop(pack, null, CD_PACKAGE_TOP_HOLDER);
        Class targetClass = this.createIn(Class.class, pack);
        Usage usage = this.createUsage(pack, pack, targetClass);
        Interface anInterface = this.createIn(Interface.class, pack);

        Node targetClassNode = this.getServiceTester().assertSemanticDrop(targetClass, null, CD_CLASS);
        this.getServiceTester().assertSemanticDrop(anInterface, null, CD_INTERFACE);

        this.getDiagramHelper().assertGetExistDomainBasedEdge(CD_USAGE, usage, sourcePackageNode, targetClassNode);

        // Check source reconnection
        this.getServiceTester().assertSourceReconnection(//
                new ElementMatcher(usage, CD_USAGE), //
                new ElementMatcher(pack, CD_PACKAGE_TOP_HOLDER), //
                new ElementMatcher(anInterface, CD_INTERFACE), //
                new ElementMatcher(targetClass, CD_CLASS));

        // Check target reconnection
        this.getServiceTester().assertTargetReconnection(//
                new ElementMatcher(usage, CD_USAGE), //
                new ElementMatcher(targetClass, CD_CLASS), //
                new ElementMatcher(pack, CD_PACKAGE_TOP_HOLDER), //
                new ElementMatcher(anInterface, CD_INTERFACE));

    }

    @Test
    public void checkUsageEdge() {
        Package pack = this.init();

        // From Package
        this.checkUsageEdge(this.createIn(Package.class, pack), this.createIn(Package.class, pack));
        this.checkUsageEdge(this.createIn(Package.class, pack), this.createIn(Model.class, pack));
        this.checkUsageEdge(this.createIn(Package.class, pack), this.createIn(Class.class, pack));
        this.checkUsageEdge(this.createIn(Package.class, pack), this.createIn(Interface.class, pack));
        this.checkUsageEdge(this.createIn(Package.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkUsageEdge(this.createIn(Package.class, pack), this.createIn(Enumeration.class, pack));

        // From Model
        this.checkUsageEdge(this.createIn(Model.class, pack), this.createIn(Package.class, pack));
        this.checkUsageEdge(this.createIn(Model.class, pack), this.createIn(Model.class, pack));
        this.checkUsageEdge(this.createIn(Model.class, pack), this.createIn(Class.class, pack));
        this.checkUsageEdge(this.createIn(Model.class, pack), this.createIn(Interface.class, pack));
        this.checkUsageEdge(this.createIn(Model.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkUsageEdge(this.createIn(Model.class, pack), this.createIn(DataType.class, pack));
        this.checkUsageEdge(this.createIn(Model.class, pack), this.createIn(Enumeration.class, pack));

        // From Class
        this.checkUsageEdge(pack, this.createIn(Class.class, pack), this.createIn(Package.class, pack));
        this.checkUsageEdge(pack, this.createIn(Class.class, pack), this.createIn(Model.class, pack));
        this.checkUsageEdge(pack, this.createIn(Class.class, pack), this.createIn(Class.class, pack));
        this.checkUsageEdge(pack, this.createIn(Class.class, pack), this.createIn(Interface.class, pack));
        this.checkUsageEdge(pack, this.createIn(Class.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkUsageEdge(pack, this.createIn(Class.class, pack), this.createIn(DataType.class, pack));
        this.checkUsageEdge(pack, this.createIn(Class.class, pack), this.createIn(Enumeration.class, pack));

        // From Primitive Type
        this.checkUsageEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Package.class, pack));
        this.checkUsageEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Model.class, pack));
        this.checkUsageEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Class.class, pack));
        this.checkUsageEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Interface.class, pack));
        this.checkUsageEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkUsageEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(DataType.class, pack));
        this.checkUsageEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Enumeration.class, pack));

        // From Data Type
        this.checkUsageEdge(pack, this.createIn(DataType.class, pack), this.createIn(Package.class, pack));
        this.checkUsageEdge(pack, this.createIn(DataType.class, pack), this.createIn(Model.class, pack));
        this.checkUsageEdge(pack, this.createIn(DataType.class, pack), this.createIn(Class.class, pack));
        this.checkUsageEdge(pack, this.createIn(DataType.class, pack), this.createIn(Interface.class, pack));
        this.checkUsageEdge(pack, this.createIn(DataType.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkUsageEdge(pack, this.createIn(DataType.class, pack), this.createIn(DataType.class, pack));
        this.checkUsageEdge(pack, this.createIn(DataType.class, pack), this.createIn(Enumeration.class, pack));

        // From Enumeration
        this.checkUsageEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Package.class, pack));
        this.checkUsageEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Model.class, pack));
        this.checkUsageEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Class.class, pack));
        this.checkUsageEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Interface.class, pack));
        this.checkUsageEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkUsageEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(DataType.class, pack));
        this.checkUsageEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Enumeration.class, pack));
    }

    private void checkInterfaceRealizationEdge(BehavioredClassifier owner, Interface target) {
        InterfaceRealization interfaceRealization = this.createInterfaceRealization(owner, target);

        this.getServiceTester().checkDisplayedDomainBasedEdge(owner, target, interfaceRealization, ID_BUILDER);
    }

    private void checkGeneralizationEdge(Classifier source, Classifier target) {
        Generalization generalization = this.createGeneralization(source, target);

        this.getServiceTester().checkDisplayedDomainBasedEdge(source, target, generalization, ID_BUILDER);

        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(source)//
                .withTarget(target)//
                .withExpectedContainementRef(UML.getClassifier_Generalization())//
                .withExpectedOwner(source)//
                .withType(UML.getGeneralization())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();
    }

    private Generalization createGeneralization(Classifier source, Classifier target) {
        Generalization generalization = this.create(Generalization.class);
        source.getGeneralizations().add(generalization);
        generalization.setGeneral(target);
        return generalization;
    }

    private InterfaceRealization createInterfaceRealization(BehavioredClassifier owner, Interface target) {
        InterfaceRealization interfaceRealization = this.createIn(InterfaceRealization.class, owner);
        interfaceRealization.setContract(target);
        interfaceRealization.setImplementingClassifier(owner);
        return interfaceRealization;
    }

    private void checkDependencyEdge(EObject owner, NamedElement source, NamedElement target) {
        Dependency dependency = this.createDependency(owner, source, target);

        this.getServiceTester().checkDisplayedDomainBasedEdge(source, target, dependency, ID_BUILDER);
    }

    private void checkUsageEdge(EObject owner, NamedElement source, NamedElement target) {
        Usage usage = this.createUsage(owner, source, target);

        this.getServiceTester().checkDisplayedDomainBasedEdge(source, target, usage, ID_BUILDER);
    }

    private Edge checkAssociationEdge(EObject owner, Classifier source, Classifier target) {
        Association association = this.createAssociation(owner, source, target);
        return this.getServiceTester().checkDisplayedDomainBasedEdge(source, target, association, ID_BUILDER);
    }

    private Dependency createDependency(EObject owner, NamedElement source, NamedElement target) {
        Dependency dependency = this.createIn(Dependency.class, owner);
        dependency.getClients().add(source);
        dependency.getSuppliers().add(target);
        return dependency;
    }

    private Usage createUsage(EObject owner, NamedElement source, NamedElement target) {
        Usage usage = this.createIn(Usage.class, owner);
        usage.getClients().add(source);
        usage.getSuppliers().add(target);
        return usage;
    }

    private Association createAssociation(EObject owner, Classifier sourceClassifier, Classifier targetClassifier) {
        Association association = this.createIn(Association.class, owner);
        // create source property
        Property sourceProp = UMLFactory.eINSTANCE.createProperty();
        sourceProp.setType(targetClassifier);
        association.getMemberEnds().add(sourceProp);

        // create target property
        Property targetProp = UMLFactory.eINSTANCE.createProperty();
        targetProp.setType(sourceClassifier);
        association.getMemberEnds().add(targetProp);

        boolean added = ClassifierUtils.addOwnedAttribute(sourceClassifier, sourceProp);
        if (!added) {
            association.getOwnedEnds().add(sourceProp);
        }

        // Add target {@link Property} in the correct container
        added = ClassifierUtils.addOwnedAttribute(targetClassifier, targetProp);
        if (!added) {
            association.getOwnedEnds().add(targetProp);
        }

        return association;
    }

    private void checkDependencyEdge(NamedElement source, NamedElement target) {
        this.checkDependencyEdge(source, source, target);

        final EObject expectedOwner;
        if (source instanceof Package) {
            expectedOwner = source;
        } else {
            expectedOwner = source.eContainer();
        }
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(source)//
                .withTarget(target)//
                .withExpectedContainementRef(UML.getPackage_PackagedElement())//
                .withExpectedOwner(expectedOwner)//
                .withType(UML.getDependency())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();

    }

    private void checkUsageEdge(NamedElement source, NamedElement target) {
        this.checkUsageEdge(source, source, target);

        final EObject expectedOwner;
        if (source instanceof Package) {
            expectedOwner = source;
        } else {
            expectedOwner = source.eContainer();
        }
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(source)//
                .withTarget(target)//
                .withExpectedContainementRef(UML.getPackage_PackagedElement())//
                .withExpectedOwner(expectedOwner)//
                .withType(UML.getDependency())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();

    }

    @Test
    public void checkCommentAnnotedElementLinkEdge() {
        Package pack = this.init();

        var source = this.createIn(Comment.class, pack);

        // From package
        this.checkCommentAnnotedElement(source, this.createIn(Package.class, pack));

        // From model
        this.checkCommentAnnotedElement(source, this.createIn(Model.class, pack));

        // From Class
        this.checkCommentAnnotedElement(source, this.createIn(Class.class, pack));

        // From Interface
        this.checkCommentAnnotedElement(source, this.createIn(Interface.class, pack));

        // From Primitive Type
        this.checkCommentAnnotedElement(source, this.createIn(PrimitiveType.class, pack));

        // From DataType Type
        this.checkCommentAnnotedElement(source, this.createIn(DataType.class, pack));

    }

    private void checkCommentAnnotedElement(Comment source, Element target) {
        source.getAnnotatedElements().add(target);
        SynchronizedFeatureEdgeTestHelper.builder()//
                .withEdgeDescriptionId(ID_BUILDER.getFeatureBaseEdgeId(UML.getComment_AnnotatedElement()))//
                .withIdBuilder(ID_BUILDER)//
                .withSource(source)//
                .withTarget(target)//
                .withRepresentationHelper(this.getDiagramHelper())//
                .build()//
                .updateDiagram()//
                .assertEdgeDisplayedOnDiagram();
    }

    @Test
    public void checkAbstractionEdge() {
        Package pack = this.init();

        // From Package
        this.checkAbstractionEdge(this.createIn(Package.class, pack), this.createIn(Package.class, pack));
        this.checkAbstractionEdge(this.createIn(Package.class, pack), this.createIn(Model.class, pack));
        this.checkAbstractionEdge(this.createIn(Package.class, pack), this.createIn(Class.class, pack));
        this.checkAbstractionEdge(this.createIn(Package.class, pack), this.createIn(Interface.class, pack));
        this.checkAbstractionEdge(this.createIn(Package.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkAbstractionEdge(this.createIn(Package.class, pack), this.createIn(Enumeration.class, pack));

        // From Model
        this.checkAbstractionEdge(this.createIn(Model.class, pack), this.createIn(Package.class, pack));
        this.checkAbstractionEdge(this.createIn(Model.class, pack), this.createIn(Model.class, pack));
        this.checkAbstractionEdge(this.createIn(Model.class, pack), this.createIn(Class.class, pack));
        this.checkAbstractionEdge(this.createIn(Model.class, pack), this.createIn(Interface.class, pack));
        this.checkAbstractionEdge(this.createIn(Model.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkAbstractionEdge(this.createIn(Model.class, pack), this.createIn(DataType.class, pack));
        this.checkAbstractionEdge(this.createIn(Model.class, pack), this.createIn(Enumeration.class, pack));

        // From Class
        this.checkAbstractionEdge(pack, this.createIn(Class.class, pack), this.createIn(Package.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Class.class, pack), this.createIn(Model.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Class.class, pack), this.createIn(Class.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Class.class, pack), this.createIn(Interface.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Class.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Class.class, pack), this.createIn(DataType.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Class.class, pack), this.createIn(Enumeration.class, pack));

        // From Primitive Type
        this.checkAbstractionEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Package.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Model.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Class.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Interface.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(DataType.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(PrimitiveType.class, pack), this.createIn(Enumeration.class, pack));

        // From Data Type
        this.checkAbstractionEdge(pack, this.createIn(DataType.class, pack), this.createIn(Package.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(DataType.class, pack), this.createIn(Model.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(DataType.class, pack), this.createIn(Class.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(DataType.class, pack), this.createIn(Interface.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(DataType.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(DataType.class, pack), this.createIn(DataType.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(DataType.class, pack), this.createIn(Enumeration.class, pack));

        // From Data Type
        this.checkAbstractionEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Package.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Model.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Class.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Interface.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(PrimitiveType.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(DataType.class, pack));
        this.checkAbstractionEdge(pack, this.createIn(Enumeration.class, pack), this.createIn(Enumeration.class, pack));
    }

    private void checkAbstractionEdge(EObject owner, NamedElement source, NamedElement target) {
        var edge = this.createIn(Abstraction.class, owner);
        edge.getClients().add(source);
        edge.getSuppliers().add(target);

        this.getServiceTester().checkDisplayedDomainBasedEdge(source, target, edge, ID_BUILDER);

        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(source)//
                .withTarget(target)//
                .withExpectedContainementRef(UML.getPackage_PackagedElement())//
                .withExpectedOwner(owner)//
                .withType(UML.getAbstraction())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();
    }

    private void checkAbstractionEdge(NamedElement source, NamedElement target) {
        this.checkAbstractionEdge(source, source, target);
    }

    @Test
    public void checkPackageMergeEdge() {
        Package pack = this.init();

        // From package
        this.checkPackageMergeEdge(this.createIn(Package.class, pack), this.createIn(Package.class, pack));
        this.checkPackageMergeEdge(this.createIn(Package.class, pack), this.createIn(Model.class, pack));

        // From model
        this.checkPackageMergeEdge(this.createIn(Model.class, pack), this.createIn(Package.class, pack));
        this.checkPackageMergeEdge(this.createIn(Model.class, pack), this.createIn(Model.class, pack));

    }

    private void checkPackageMergeEdge(Package source, Package target) {
        var edge = this.createIn(PackageMerge.class, source);
        edge.setReceivingPackage(source);
        edge.setMergedPackage(target);

        this.getServiceTester().checkDisplayedDomainBasedEdge(source, target, edge, ID_BUILDER);

        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(source)//
                .withTarget(target)//
                .withExpectedContainementRef(UML.getPackage_PackageMerge())//
                .withExpectedOwner(source)//
                .withType(UML.getPackageMerge())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();
    }

    @Test
    public void checkPackageImportEdge() {
        Package pack = this.init();

        // From package
        this.checkPackageImportEdge(this.createIn(Package.class, pack), this.createIn(Package.class, pack));
        this.checkPackageImportEdge(this.createIn(Package.class, pack), this.createIn(Model.class, pack));

        // From model
        this.checkPackageImportEdge(this.createIn(Model.class, pack), this.createIn(Package.class, pack));
        this.checkPackageImportEdge(this.createIn(Model.class, pack), this.createIn(Model.class, pack));

    }

    private void checkPackageImportEdge(Namespace source, Package target) {

        var edge = this.createIn(PackageImport.class, source);
        edge.setImportingNamespace(source);
        edge.setImportedPackage(target);
        this.getServiceTester().checkDisplayedDomainBasedEdge(source, target, edge, ID_BUILDER);

        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(source)//
                .withTarget(target)//
                .withExpectedContainementRef(UML.getNamespace_PackageImport())//
                .withExpectedOwner(source)//
                .withType(UML.getPackageImport())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();
    }

    private Package init() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);

        this.getDiagramHelper().init(pack, CDDiagramDescriptionBuilder.CD_REP_NAME);

        return pack;
    }

    /**
     * Check that a ContainmentLink is always created between directly contained Packages.
     */
    @Test
    public void checkClassifierContainmentLinkSynchronization() {
        Package pack = this.init();

        Class parentClass = this.createIn(Class.class, pack);
        Class childClass1 = this.createIn(Class.class, parentClass);
        Class futureChildClass = this.createIn(Class.class, pack);

        Node parentNode = this.getDiagramHelper().createNodeInDiagram(CD_CLASS, parentClass);
        Node childNode = this.getDiagramHelper().createNodeInDiagram(CD_CLASS, childClass1);
        Node futureChildNode = this.getDiagramHelper().createNodeInDiagram(CD_CLASS, futureChildClass);

        this.getDiagramHelper().refresh();

        this.getDiagramHelper().assertGetUniqueFeatureBasedEdge(CDDiagramDescriptionBuilder.CLASSIFIER_CONTAINMENT_LINK_EDGE_ID, parentNode, childNode);
        this.getDiagramHelper().assertNoFeatureEdgeStartingFrom(CDDiagramDescriptionBuilder.CLASSIFIER_CONTAINMENT_LINK_EDGE_ID, futureChildNode);

        this.getDiagramService().moveIn(futureChildClass, parentClass, UML.getClass_NestedClassifier().getName());

        this.getDiagramHelper().refresh();

        this.getDiagramHelper().assertGetUniqueFeatureBasedEdge(CDDiagramDescriptionBuilder.CLASSIFIER_CONTAINMENT_LINK_EDGE_ID, parentNode, childNode);
        this.getDiagramHelper().assertGetUniqueFeatureBasedEdge(CDDiagramDescriptionBuilder.CLASSIFIER_CONTAINMENT_LINK_EDGE_ID, parentNode, futureChildNode);

    }

    /**
     * Check that a ContainmentLink is always created between directly contained PackageableElements.
     */
    @Test
    public void checkPackageContainmentLinkSynchronization() {
        Package pack = this.init();

        Package parentPackage = this.createIn(Package.class, pack);
        Class childClass1 = this.createIn(Class.class, parentPackage);
        Package childPackage1 = this.createIn(Package.class, parentPackage);
        Class futureChildClass = this.createIn(Class.class, pack);

        Node parentNode = this.getDiagramHelper().createNodeInDiagram(CD_PACKAGE_TOP_HOLDER, parentPackage);
        Node childPackageNode = this.getDiagramHelper().createNodeInDiagram(CD_PACKAGE_TOP_HOLDER, childPackage1);
        Node childNode = this.getDiagramHelper().createNodeInDiagram(CD_CLASS, childClass1);
        Node futureChildNode = this.getDiagramHelper().createNodeInDiagram(CD_CLASS, futureChildClass);

        this.getDiagramHelper().refresh();

        this.getDiagramHelper().assertGetUniqueFeatureBasedEdge(CDDiagramDescriptionBuilder.PACKAGE_CONTAINMENT_LINK_EDGE_ID, parentNode, childNode);
        this.getDiagramHelper().assertGetUniqueFeatureBasedEdge(CDDiagramDescriptionBuilder.PACKAGE_CONTAINMENT_LINK_EDGE_ID, parentNode, childPackageNode);
        this.getDiagramHelper().assertNoFeatureEdgeStartingFrom(CDDiagramDescriptionBuilder.PACKAGE_CONTAINMENT_LINK_EDGE_ID, futureChildNode);

        this.getDiagramService().moveIn(futureChildClass, parentPackage, UML.getPackage_PackagedElement().getName());

        this.getDiagramHelper().refresh();

        this.getDiagramHelper().assertGetUniqueFeatureBasedEdge(CDDiagramDescriptionBuilder.PACKAGE_CONTAINMENT_LINK_EDGE_ID, parentNode, childNode);
        this.getDiagramHelper().assertGetUniqueFeatureBasedEdge(CDDiagramDescriptionBuilder.PACKAGE_CONTAINMENT_LINK_EDGE_ID, parentNode, futureChildNode);
        this.getDiagramHelper().assertGetUniqueFeatureBasedEdge(CDDiagramDescriptionBuilder.PACKAGE_CONTAINMENT_LINK_EDGE_ID, parentNode, childPackageNode);

    }

    @Test
    public void checkAssociationReconnection() {
        Package pack = this.init();

        Class sourceClass = this.createIn(Class.class, pack);
        Class targetClass = this.createIn(Class.class, pack);
        Association association = this.createAssociation(pack, sourceClass, targetClass);
        Interface anInterface = this.createIn(Interface.class, pack);

        Node sourceClassNode = this.getServiceTester().assertSemanticDrop(sourceClass, null, CD_CLASS);
        Node targetClassNode = this.getServiceTester().assertSemanticDrop(targetClass, null, CD_CLASS);
        this.getServiceTester().assertSemanticDrop(anInterface, null, CD_INTERFACE);

        this.getDiagramHelper().assertGetExistDomainBasedEdge(CD_ASSOCIATION, association, sourceClassNode, targetClassNode);

        // Check source reconnection
        this.getServiceTester().assertSourceReconnection(//
                new ElementMatcher(association, CD_ASSOCIATION), //
                new ElementMatcher(sourceClass, CD_CLASS), //
                new ElementMatcher(anInterface, CD_INTERFACE), //
                new ElementMatcher(targetClass, CD_CLASS));

        // Check target reconnection
        this.getServiceTester().assertTargetReconnection(//
                new ElementMatcher(association, CD_ASSOCIATION), //
                new ElementMatcher(targetClass, CD_CLASS), //
                new ElementMatcher(sourceClass, CD_CLASS), //
                new ElementMatcher(anInterface, CD_INTERFACE));
    }

    /**
     * Creates the
     * {@link ClassDiagramService#createCompositeAssociation(EObject, EObject, Node, Node, org.eclipse.sirius.components.core.api.IEditingContext, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext)}
     * service.
     */
    @Test
    public void checkCompositeAssociationEdge() {
        Package pack = this.init();

        Class c1 = this.createIn(Class.class, pack);
        Interface i1 = this.createIn(Interface.class, pack);

        Node classNode = this.getServiceTester().assertSemanticDrop(c1, null, CD_CLASS);
        Node interfaceNode = this.getServiceTester().assertSemanticDrop(i1, null, CD_INTERFACE);

        this.checkCompositeAssociationCreation(c1, i1, classNode, interfaceNode);
    }

    private void checkCompositeAssociationCreation(Class c1, Interface i1, Node source, Node target) {
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createCompositeAssociation(c1, i1, source, target, this.getEditingContext(), context);

            return aNewElement;
        });

        assertTrue(newElement instanceof Association);
        Property sourceEnd = ((Association) newElement).getMemberEnds().get(0);
        assertEquals(AggregationKind.COMPOSITE_LITERAL, sourceEnd.getAggregation());
        assertEquals(c1, sourceEnd.eContainer());
        assertEquals(0, sourceEnd.getLower());
        assertEquals(1, sourceEnd.getUpper());
        this.getDiagramHelper().assertGetExistDomainBasedEdge(CD_ASSOCIATION, newElement, source, target);

    }

    /**
     * Creates the
     * {@link ClassDiagramService#createSharedAssociation(EObject, EObject, Node, Node, org.eclipse.sirius.components.core.api.IEditingContext, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext)}
     * service.
     */
    @Test
    public void checkSharedAssociationEdge() {
        Package pack = this.init();

        Class c1 = this.createIn(Class.class, pack);
        Interface i1 = this.createIn(Interface.class, pack);

        Node classNode = this.getServiceTester().assertSemanticDrop(c1, null, CD_CLASS);
        Node interfaceNode = this.getServiceTester().assertSemanticDrop(i1, null, CD_INTERFACE);

        this.checkSharedAssociationCreation(c1, i1, classNode, interfaceNode);
    }

    private void checkSharedAssociationCreation(Class c1, Interface i1, Node source, Node target) {
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createSharedAssociation(c1, i1, source, target, this.getEditingContext(), context);

            return aNewElement;
        });

        assertTrue(newElement instanceof Association);
        Property sourceEnd = ((Association) newElement).getMemberEnds().get(0);
        assertEquals(AggregationKind.SHARED_LITERAL, sourceEnd.getAggregation());
        assertEquals(c1, sourceEnd.eContainer());
        assertEquals(0, sourceEnd.getLower());
        assertEquals(1, sourceEnd.getUpper());
        this.getDiagramHelper().assertGetExistDomainBasedEdge(CD_ASSOCIATION, newElement, source, target);
    }

    @Test
    public void checkAssociationEdge() {
        Package pack = this.init();

        // From Class
        Class c1 = this.createIn(Class.class, pack);
        this.checkAssociationEdge(pack, c1, this.createIn(Class.class, pack));
        Interface i1 = this.createIn(Interface.class, pack);
        Edge c1i1Edge = this.checkAssociationEdge(pack, c1, i1);

        PrimitiveType p1 = this.createIn(PrimitiveType.class, pack);
        Edge c1p1Edge = this.checkAssociationEdge(pack, c1, p1);

        DataType d1 = this.createIn(DataType.class, pack);
        Edge c1d1Edge = this.checkAssociationEdge(pack, c1, d1);

        Enumeration e1 = this.createIn(Enumeration.class, pack);
        Edge c1e1Edge = this.checkAssociationEdge(pack, c1, e1);

        // From Primitive Type
        PrimitiveType prim1 = this.createIn(PrimitiveType.class, pack);
        this.checkAssociationEdge(pack, prim1, this.createIn(Class.class, pack));
        this.checkAssociationEdge(pack, prim1, this.createIn(Interface.class, pack));
        this.checkAssociationEdge(pack, prim1, this.createIn(PrimitiveType.class, pack));
        this.checkAssociationEdge(pack, prim1, this.createIn(DataType.class, pack));
        this.checkAssociationEdge(pack, prim1, this.createIn(Enumeration.class, pack));

        // From Data Type
        DataType data1 = this.createIn(DataType.class, pack);
        this.checkAssociationEdge(pack, data1, this.createIn(Class.class, pack));
        this.checkAssociationEdge(pack, data1, this.createIn(Interface.class, pack));
        this.checkAssociationEdge(pack, data1, this.createIn(PrimitiveType.class, pack));
        this.checkAssociationEdge(pack, data1, this.createIn(DataType.class, pack));
        this.checkAssociationEdge(pack, data1, this.createIn(Enumeration.class, pack));

        // From Enumeration
        Enumeration enum1 = this.createIn(Enumeration.class, pack);
        this.checkAssociationEdge(pack, enum1, this.createIn(Class.class, pack));
        this.checkAssociationEdge(pack, enum1, this.createIn(Interface.class, pack));
        this.checkAssociationEdge(pack, enum1, this.createIn(PrimitiveType.class, pack));
        this.checkAssociationEdge(pack, enum1, this.createIn(DataType.class, pack));
        this.checkAssociationEdge(pack, enum1, this.createIn(Enumeration.class, pack));

        // Check on children node

        this.getServiceTester().assertSemanticDrop(pack, null, CD_PACKAGE_TOP_HOLDER);
        Node parentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CD_PACKAGE_TOP_CONTENT, pack);
        Node c1Node = this.getServiceTester().assertSemanticDrop(c1, parentNodeContent, CD_CLASS_CHILD);
        Node p1Node = this.getServiceTester().assertSemanticDrop(p1, parentNodeContent, CD_PRIMITIVE_TYPE_CHILD);
        Node i1Node = this.getServiceTester().assertSemanticDrop(i1, parentNodeContent, CD_INTERFACE_CHILD);
        Node d1Node = this.getServiceTester().assertSemanticDrop(d1, parentNodeContent, CD_DATA_TYPE_CHILD);
        Node e1Node = this.getServiceTester().assertSemanticDrop(e1, parentNodeContent, CD_ENUMERATION_TYPE_CHILD);

        this.getDiagramHelper().assertGetExistDomainBasedEdge(CD_ASSOCIATION, this.getDiagramHelper().getSemanticElement(c1i1Edge), //
                c1Node, i1Node);
        this.getDiagramHelper().assertGetExistDomainBasedEdge(CD_ASSOCIATION, this.getDiagramHelper().getSemanticElement(c1p1Edge), //
                c1Node, p1Node);
        this.getDiagramHelper().assertGetExistDomainBasedEdge(CD_ASSOCIATION, this.getDiagramHelper().getSemanticElement(c1d1Edge), //
                c1Node, d1Node);
        this.getDiagramHelper().assertGetExistDomainBasedEdge(CD_ASSOCIATION, this.getDiagramHelper().getSemanticElement(c1e1Edge), //
                c1Node, e1Node);
    }

    @Test
    public void checkAssociationStyle() {
        Resource resource = this.createResource();
        Model pack = this.createInResource(Model.class, resource);

        this.getDiagramHelper().init(pack, CDDiagramDescriptionBuilder.CD_REP_NAME);

        resource.getContents().add(pack);
        new AssociationTestCaseBuilder(pack).createAssociations();

        // Drop all elements
        EMFUtils.allContainedObjectOfType(pack, Class.class).forEach(c -> this.getServiceTester().assertSemanticDrop(c, null, CD_CLASS));

        // Check all edges
        List<Edge> edges = this.getDiagramHelper().getMatchingEdges(Optional.of(CD_ASSOCIATION), Optional.empty(), Optional.empty(), Optional.empty());
        assertEquals(36, edges.size());

        for (Edge edge : edges) {
            // Check arrow style
            Association association = (Association) this.getObjectService().getObject(this.getEditingContext(), edge.getTargetObjectId()).get();
            assertEquals(this.getExpectedArrowStyle(association, false), edge.getStyle().getSourceArrow(), "Invalid begin style for association " + association.getName());
            assertEquals(this.getExpectedArrowStyle(association, true), edge.getStyle().getTargetArrow(), "Invalid end style for association " + association.getName());

            // Check labels are displayed (the content of each label is tested in UML domain services)
            assertFalse(edge.getCenterLabel().getText().isBlank(), "No center label for association " + association.getName());
            assertFalse(edge.getBeginLabel().getText().isBlank(), "No begin label for association " + association.getName());
            assertFalse(edge.getEndLabel().getText().isBlank(), "No end label for association " + association.getName());
        }
    }

    private ArrowStyle getExpectedArrowStyle(Association association, boolean isTarget) {
        final int navigableMemberIndex;
        final int aggrMemberIndex;
        if (isTarget) {
            navigableMemberIndex = 0;
            aggrMemberIndex = 1;
        } else {
            navigableMemberIndex = 1;
            aggrMemberIndex = 0;
        }
        Property navProperty = association.getMemberEnds().get(navigableMemberIndex);
        Property aggProperty = association.getMemberEnds().get(aggrMemberIndex);
        boolean isNavigable = association.getNavigableOwnedEnds().contains(navProperty);
        AggregationKind aggType = aggProperty.getAggregation();
        ArrowStyle style = ArrowStyle.None;
        if (isNavigable && aggType == AggregationKind.NONE_LITERAL) {
            style = ArrowStyle.InputArrow;
        } else if (isNavigable && aggType == AggregationKind.SHARED_LITERAL) {
            style = ArrowStyle.InputArrowWithDiamond;
        } else if (isNavigable && aggType == AggregationKind.COMPOSITE_LITERAL) {
            style = ArrowStyle.InputArrowWithFillDiamond;
        } else if (!isNavigable && aggType == AggregationKind.NONE_LITERAL) {
            style = ArrowStyle.None;
        } else if (!isNavigable && aggType == AggregationKind.SHARED_LITERAL) {
            style = ArrowStyle.Diamond;
        } else if (!isNavigable && aggType == AggregationKind.COMPOSITE_LITERAL) {
            style = ArrowStyle.FillDiamond;
        }

        return style;
    }

    @Override
    protected ClassDiagramService buildService() {
        return new ClassDiagramService(this.getObjectService(), this.getDiagramNavigationService(), this.getDiagramOperationsService(), e -> true, this.getViewDiagramDescriptionService(),
                new MockLogger());
    }

    @Override
    protected ClassDiagramService getDiagramService() {
        return (ClassDiagramService) super.getDiagramService();
    }
}

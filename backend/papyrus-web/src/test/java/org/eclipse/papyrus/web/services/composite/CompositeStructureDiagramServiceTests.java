/*****************************************************************************
 * Copyright (c) 2022, 2024 CEA LIST, Obeo, Artal Technologies
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
 *  Aurelien Didier (Artal Technologies) - Issue 190
 *****************************************************************************/
package org.eclipse.papyrus.web.services.composite;

import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.CONTENT_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.HOLDER_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.SHARED_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.composite.CompositeStructureDiagramService;
import org.eclipse.papyrus.web.application.representations.uml.CSDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UMLMetamodelHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.services.AbstractDiagramTest;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.papyrus.web.utils.LabelStyleCheck;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class gathering integration test regarding creation in the Composite Structure Diagram.
 *
 * @author Arthur Daussy
 */
@SpringBootTest
@WebAppConfiguration
public class CompositeStructureDiagramServiceTests extends AbstractDiagramTest {

    private static final String U_HOLDER_SUFFIX = UNDERSCORE + HOLDER_SUFFIX;

    private static final String U_CONTENT_SUFFIX = UNDERSCORE + CONTENT_SUFFIX;

    private static final IdBuilder ID_BUILDER = new IdBuilder(CSDDiagramDescriptionBuilder.CSD_PREFIX, new UMLMetamodelHelper());

    private static final String CSD_COMMENT_SHARED = ID_BUILDER.getSpecializedDomainNodeName(UML.getComment(), SHARED_SUFFIX);

    private static final String CSD_CLASSIFIER_SHARED = ID_BUILDER.getSpecializedDomainNodeName(UML.getClass_(), SHARED_SUFFIX);

    private static final String CSD_CLASSIFIER = ID_BUILDER.getDomainNodeName(UML.getClass_());

    private static final String CSD_CONNECTOR = ID_BUILDER.getDomainBaseEdgeId(UML.getConnector());

    private static final String CSD_PROPERTY_ON_PROPERTY = ID_BUILDER.getSpecializedDomainNodeName(UML.getProperty(), CSDDiagramDescriptionBuilder.IN_PROPERTY + UNDERSCORE + SHARED_SUFFIX);

    private static final String CSD_PROPERTY_ON_CLASSIFIER = ID_BUILDER.getSpecializedDomainNodeName(UML.getProperty(), CSDDiagramDescriptionBuilder.IN_CLASSIFIER + UNDERSCORE + SHARED_SUFFIX);

    private static final String CSD_PORT_ON_PROPERTY = ID_BUILDER.getSpecializedDomainNodeName(UML.getPort(), CSDDiagramDescriptionBuilder.IN_PROPERTY + UNDERSCORE + SHARED_SUFFIX);

    private static final String CSD_PORT_ON_CLASSIFIER = ID_BUILDER.getSpecializedDomainNodeName(UML.getPort(), CSDDiagramDescriptionBuilder.IN_CLASSIFIER + UNDERSCORE + SHARED_SUFFIX);

    @Override
    protected AbstractDiagramService buildService() {
        return new CompositeStructureDiagramService(this.getObjectService(), this.getDiagramNavigationService(), this.getDiagramOperationsService(), e -> true, this.getViewDiagramDescriptionService(),
                new MockLogger());
    }

    @Test
    public void checkConditionalLabelStyleOnClass() {
        Package pack = this.init();

        Class clazz = this.createIn(Class.class, pack);
        Node classNode = this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, clazz);

        LabelStyleCheck.build(classNode).assertIsNotItalic().assertIsNotUnderline();

        clazz.setIsAbstract(true);

        this.getDiagramHelper().refresh();
        classNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_CLASSIFIER + U_HOLDER_SUFFIX, clazz);

        LabelStyleCheck.build(classNode).assertIsItalic().assertIsNotUnderline();
    }

    @Test
    public void checkConditionalLabelStyleOnProperty() {
        Package pack = this.init();

        Class clazz = this.createIn(Class.class, pack);
        this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, clazz);
        Node classNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_CLASSIFIER + U_CONTENT_SUFFIX, clazz);
        Property property = this.createIn(Property.class, clazz);

        Node propHolder = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, property, classNode);
        Node propertyNode = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_PROPERTY_ON_CLASSIFIER + U_CONTENT_SUFFIX, propHolder, property);

        LabelStyleCheck.build(propHolder).assertIsNotItalic().assertIsNotUnderline();

        property.setIsStatic(true);

        this.getDiagramHelper().refresh();
        propertyNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, property);

        LabelStyleCheck.build(propertyNode).assertIsNotItalic().assertIsUnderline();
    }

    @Test
    public void checkChildrenCreationInTypedProperty() {
        Package pack = this.init();

        Class clazz = this.createIn(Class.class, pack);

        Property property = this.createIn(Property.class, clazz);
        Class propertyType = this.createIn(Class.class, pack);
        property.setType(propertyType);

        this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, clazz);
        Node classNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_CLASSIFIER + U_CONTENT_SUFFIX, clazz);

        Node propertyHolderNode = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, property, classNode);
        Node propertyNode = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_PROPERTY_ON_CLASSIFIER + U_CONTENT_SUFFIX, propertyHolderNode, property);

        // Port
        this.getServiceTester().assertChildCreation(propertyHolderNode, propertyType, UML.getPort(), UML.getStructuredClassifier_OwnedAttribute(), CSD_PORT_ON_PROPERTY, propertyType);

        // Property
        this.getServiceTester().assertChildCreation(propertyNode, propertyType, UML.getProperty(), UML.getStructuredClassifier_OwnedAttribute(), CSD_PROPERTY_ON_PROPERTY + U_HOLDER_SUFFIX,
                propertyType);

        // Comment
        this.getServiceTester().assertChildCreation(propertyNode, UML.getComment(), UML.getElement_OwnedComment(), CSD_COMMENT_SHARED, property);

    }

    /**
     * Checks that a port on property can only be done if the type the property can own ports.
     */
    @Test
    public void checkPortCreationOnUntypedProperty() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);

        Class clazz = this.createIn(Class.class, pack);

        Property property = this.createIn(Property.class, clazz);

        this.getDiagramHelper().init(clazz, CSDDiagramDescriptionBuilder.CSD_REP_NAME);
        this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, clazz);
        Node classNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_CLASSIFIER + U_CONTENT_SUFFIX, clazz);

        Node propertyHolderNode = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, property, classNode);
        Node propertyNode = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_PROPERTY_ON_CLASSIFIER + U_CONTENT_SUFFIX, propertyHolderNode, property);

        this.getDiagramHelper().modify(diagramContext -> {
            EObject creation = this.getDiagramService().create(property, UML.getPort().getName(), UML.getStructuredClassifier_OwnedAttribute().getName(), propertyNode, diagramContext,
                    this.getDiagramHelper().getConvertedNodes());
            assertEquals(CompositeStructureDiagramService.FAILURE_OBJECT, creation);
        });

    }

    @Test
    public void checkChildrenCreationInRootClass() {
        Package pack = this.init();

        Class parent = this.createIn(Class.class, pack);
        Node parentNodeHolder = this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, parent);
        Node parentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_CLASSIFIER + U_CONTENT_SUFFIX, parent);

        // Property in Class
        this.getServiceTester().assertChildCreation(parentNodeContent, UML.getProperty(), UML.getStructuredClassifier_OwnedAttribute(), CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, parent);

        // Port in Class
        this.getServiceTester().assertChildCreation(parentNodeHolder, UML.getPort(), UML.getStructuredClassifier_OwnedAttribute(), CSD_PORT_ON_CLASSIFIER, parent);

        // Comment in Package
        this.getServiceTester().assertChildCreation(parentNodeContent, UML.getComment(), UML.getElement_OwnedComment(), CSD_COMMENT_SHARED, parent);

        // Class in Class
        Class nested = this.createIn(Class.class, parent);
        Node nestedClassNodeHolder = this.getDiagramHelper().createNodeInParent(CSD_CLASSIFIER_SHARED + U_HOLDER_SUFFIX, nested, parentNodeContent);
        Node nestedClassNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_CLASSIFIER_SHARED + U_CONTENT_SUFFIX, nestedClassNodeHolder, nested);

        // Test Class in Class recursion
        this.getServiceTester().assertChildCreation(nestedClassNodeContent, UML.getClass_(), UML.getClass_NestedClassifier(), CSD_CLASSIFIER_SHARED + U_HOLDER_SUFFIX,
                (EObject) this.getObjectService().getObject(this.getEditingContext(), nestedClassNodeContent.getTargetObjectId()).get());
    }

    /**
     * Check the root creation of a Class.
     */
    @Test
    public void checkRootClassCreation() {
        this.init();
        this.getServiceTester().assertRootCreation(UML.getClass_(), UML.getPackage_PackagedElement(), CSD_CLASSIFIER + U_HOLDER_SUFFIX);
    }

    /**
     * Check the root creation of a Component.
     */
    @Test
    public void checkRootComponentCreation() {
        this.init();
        this.getServiceTester().assertRootCreation(UML.getComponent(), UML.getPackage_PackagedElement(), CSD_CLASSIFIER + U_HOLDER_SUFFIX);

    }

    @Test
    public void checkConnectorEdgeCreationBetweenProperties() {

        Package pack = this.init();

        Class rootClass = this.createIn(Class.class, pack);

        Property sourceProp = this.createIn(Property.class, rootClass);
        Class type1 = this.createIn(Class.class, pack);
        sourceProp.setType(type1);
        Class type2 = this.createIn(Class.class, pack);
        Property targetProp = this.createIn(Property.class, rootClass);
        targetProp.setType(type2);

        this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, rootClass);
        Node rootClassContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_CLASSIFIER + U_CONTENT_SUFFIX, rootClass);
        Node sourcePropNode = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, sourceProp, rootClassContent);
        Node targetPropNode = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, targetProp, rootClassContent);

        this.checkConnectorEdge(sourceProp, sourcePropNode.getId(), targetProp, targetPropNode.getId(), rootClass);

        // Also check that a new connector can be created between the two properties
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(sourceProp)//
                .withTarget(targetProp)//
                .withSourceNodeId(sourcePropNode.getId())//
                .withTargetNodeId(targetPropNode.getId())//
                .withExpectedContainementRef(UML.getStructuredClassifier_OwnedConnector())//
                .withExpectedOwner(rootClass)//
                .withType(UML.getConnector())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();
    }

    @Test
    public void checkConditionalLabelStyleOnConnector() {
        Package pack = this.init();

        Class rootClass = this.createIn(Class.class, pack);

        Property sourceProp = this.createIn(Property.class, rootClass);
        Class type1 = this.createIn(Class.class, pack);
        sourceProp.setType(type1);
        Class type2 = this.createIn(Class.class, pack);
        Property targetProp = this.createIn(Property.class, rootClass);
        targetProp.setType(type2);

        Node rootClassNode = this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, rootClass);
        Node rootClassContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_CLASSIFIER + U_CONTENT_SUFFIX, rootClass);
        Node sourceNode = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, sourceProp, rootClassContent);
        Node targetNode = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, targetProp, rootClassContent);

        Connector connector = this.createIn(Connector.class, rootClass);
        this.createIn(ConnectorEnd.class, connector).setRole(sourceProp);
        this.createIn(ConnectorEnd.class, connector).setRole(targetProp);

        this.getDiagramHelper().refresh();

        Edge connectorNode = this.getDiagramHelper().assertGetExistDomainBasedEdge(CSD_CONNECTOR, connector, sourceNode, targetNode);

        LabelStyleCheck.buildCenteredLabel(connectorNode).assertIsNotItalic().assertIsNotUnderline();

        connector.setIsStatic(true);
        this.getDiagramHelper().refresh();

        connectorNode = this.getDiagramHelper().assertGetExistDomainBasedEdge(CSD_CONNECTOR, connector, sourceNode, targetNode);
        LabelStyleCheck.buildCenteredLabel(connectorNode).assertIsNotItalic().assertIsUnderline();
    }

    @Test
    public void checkConnectorEdgeCreationBetweenPortOnProperties() {

        Package pack = this.init();

        Class rootClass = this.createIn(Class.class, pack);

        Property sourceProp = this.createIn(Property.class, rootClass);
        Class type1 = this.createIn(Class.class, pack);
        sourceProp.setType(type1);
        Port sourcePort = this.createIn(Port.class, type1);

        Class type2 = this.createIn(Class.class, pack);
        Property targetProp = this.createIn(Property.class, rootClass);
        targetProp.setType(type2);
        Port targetPort = this.createIn(Port.class, type2);

        this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, rootClass);
        Node rootClassNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_CLASSIFIER + U_CONTENT_SUFFIX, rootClass);

        Node holder1 = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, sourceProp, rootClassNode);
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_PROPERTY_ON_CLASSIFIER + U_CONTENT_SUFFIX, holder1, sourceProp);
        Node sourceNode = this.getDiagramHelper().createNodeInParent(CSD_PORT_ON_PROPERTY, sourcePort, holder1);

        Node prop2Holder = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, targetProp, rootClassNode);
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_PROPERTY_ON_CLASSIFIER + U_CONTENT_SUFFIX, prop2Holder, targetProp);
        Node targetNode = this.getDiagramHelper().createNodeInParent(CSD_PORT_ON_PROPERTY, targetPort, prop2Holder);

        this.checkConnectorEdge(sourcePort, sourceProp, sourceNode.getId(), targetPort, targetProp, targetNode.getId(), rootClass);

        // Also check that a new connector can be created between the two ports
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(sourcePort)//
                .withTarget(targetPort)//
                .withSourceNodeId(sourceNode.getId())//
                .withTargetNodeId(targetNode.getId())//
                .withExpectedContainementRef(UML.getStructuredClassifier_OwnedConnector())//
                .withExpectedOwner(rootClass)//
                .withType(UML.getConnector())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();

    }

    /**
     * Check a complex case of Connectors connecting the same port but displayed on different Properties.
     * <p>
     * The use case is
     * <ul>
     * <li>A Car class that contains 4 Properties of type <i>Wheel</i>: Front_Left, Front_Right, Back_Left,
     * Back_Right.</li>
     * <li>The Type Wheel has one Port: Port1</li>
     * <li>The Front wheels are connected using FrontConnector</li>
     * <li>The back wheels are connected using BackConnector</li>
     * </ul>
     * The complexity here is that all the sources and targets of all Connectors are connected to the same instance of
     * Port (Port1). Only the feature {@link ConnectorEnd#getPartWithPort()} distinguishes the source and target of each
     * edges.
     * </p>
     */
    @Test
    public void checkConnectorOnTestUseCase() {

        // Set up the model
        var pack = this.init();
        var car = this.createIn(Class.class, pack);

        var carNodeHolder = this.getServiceTester().assertSemanticDrop(car, null, CSD_CLASSIFIER + U_HOLDER_SUFFIX);
        var carNode = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_CLASSIFIER + U_CONTENT_SUFFIX, carNodeHolder, car);

        var wheel = this.createIn(Class.class, pack);
        var wheelNode = this.getServiceTester().assertSemanticDrop(wheel, null, CSD_CLASSIFIER + U_HOLDER_SUFFIX);

        var port1 = this.createIn(Port.class, wheel);
        // Needs to display this port so we can check there is no edge starting or ending on this node
        this.getServiceTester().assertSemanticDrop(port1, wheelNode, CSD_PORT_ON_CLASSIFIER);

        var frontLeft = this.createIn(Property.class, car);
        frontLeft.setType(wheel);
        var frontLeftNode = this.getServiceTester().assertSemanticDrop(frontLeft, carNode, CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX);
        var portOnFrontLeft = this.getServiceTester().assertSemanticDrop(port1, frontLeftNode, CSD_PORT_ON_PROPERTY);

        var frontRight = this.createIn(Property.class, car);
        frontRight.setType(wheel);
        var frontRightNode = this.getServiceTester().assertSemanticDrop(frontRight, carNode, CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX);
        var portOnFrontRight = this.getServiceTester().assertSemanticDrop(port1, frontRightNode, CSD_PORT_ON_PROPERTY);

        var backLeft = this.createIn(Property.class, car);
        backLeft.setType(wheel);
        var backLeftNode = this.getServiceTester().assertSemanticDrop(backLeft, carNode, CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX);
        var portOnBackLeft = this.getServiceTester().assertSemanticDrop(port1, backLeftNode, CSD_PORT_ON_PROPERTY);

        var backRight = this.createIn(Property.class, car);
        backRight.setType(wheel);
        var backRightNode = this.getServiceTester().assertSemanticDrop(backRight, carNode, CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX);
        var portOnBackRight = this.getServiceTester().assertSemanticDrop(port1, backRightNode, CSD_PORT_ON_PROPERTY);

        // Create the Front and Back Connector
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(frontLeft)//
                .withTarget(frontRight)//
                .withSourceNodeId(portOnFrontLeft.getId())//
                .withTargetNodeId(portOnFrontRight.getId())//
                .withExpectedContainementRef(UML.getStructuredClassifier_OwnedConnector())//
                .withExpectedOwner(car)//
                .withType(UML.getConnector())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();

        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(backLeft)//
                .withTarget(backRight)//
                .withSourceNodeId(portOnBackLeft.getId())//
                .withTargetNodeId(portOnBackRight.getId())//
                .withExpectedContainementRef(UML.getStructuredClassifier_OwnedConnector())//
                .withExpectedOwner(car)//
                .withType(UML.getConnector())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();

        List<Edge> edges = this.getDiagramHelper().getDiagram().getEdges();
        assertEquals(2, edges.size());

        var edge1 = edges.get(0);
        assertEquals(portOnFrontLeft.getId(), edge1.getSourceId());
        assertEquals(portOnFrontRight.getId(), edge1.getTargetId());
        var edge2 = edges.get(1);
        assertEquals(portOnBackLeft.getId(), edge2.getSourceId());
        assertEquals(portOnBackRight.getId(), edge2.getTargetId());

    }

    @Test
    public void checkConnectorEdgeCreationBetweenPortOnClassifier() {

        Package pack = this.init();

        Class rootClass = this.createIn(Class.class, pack);

        Class type1 = this.createIn(Class.class, rootClass);
        Port portSource = this.createIn(Port.class, type1);

        Class type2 = this.createIn(Class.class, rootClass);
        Port portTarget = this.createIn(Port.class, type2);

        Node rootClassNode = this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, rootClass);
        Node rootClassContent = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_CLASSIFIER + U_CONTENT_SUFFIX, rootClassNode, rootClass);

        Node type1NodeHolder = this.getDiagramHelper().createNodeInParent(CSD_CLASSIFIER_SHARED + U_HOLDER_SUFFIX, type1, rootClassContent);
        Node type1Node = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_CLASSIFIER_SHARED + U_CONTENT_SUFFIX, type1NodeHolder, type1);

        Node type2Node = this.getDiagramHelper().createNodeInParent(CSD_CLASSIFIER_SHARED + U_HOLDER_SUFFIX, type2, rootClassContent);

        Node portSourceNode = this.getDiagramHelper().createNodeInParent(CSD_PORT_ON_CLASSIFIER, portSource, type1NodeHolder);
        Node portTargetNode = this.getDiagramHelper().createNodeInParent(CSD_PORT_ON_CLASSIFIER, portTarget, type2Node);

        this.checkConnectorEdge(portSource, portSourceNode.getId(), portTarget, portTargetNode.getId(), rootClass);

        // Also check that a new connector can be created between the two ports
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(portSource)//
                .withTarget(portTarget)//
                .withSourceNodeId(portSourceNode.getId())//
                .withTargetNodeId(portTargetNode.getId())//
                .withExpectedContainementRef(UML.getStructuredClassifier_OwnedConnector())//
                .withExpectedOwner(rootClass)//
                .withType(UML.getConnector())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();

    }

    private void checkConnectorEdge(ConnectableElement source, Property sourceProperty, String sourceNodeId, ConnectableElement target, Property targetProperty, String targetNodeId,
            StructuredClassifier parent) {
        var edge = this.createIn(Connector.class, parent);

        ConnectorEnd sourceConnectorEnd = this.createIn(ConnectorEnd.class, edge);
        sourceConnectorEnd.setRole(source);
        sourceConnectorEnd.setPartWithPort(sourceProperty);
        ConnectorEnd targetConnectorEnd = this.createIn(ConnectorEnd.class, edge);
        targetConnectorEnd.setPartWithPort(targetProperty);
        targetConnectorEnd.setRole(target);

        this.getServiceTester().buildDomainBasedEdgeTestHelper(ID_BUILDER)//
                .withSource(source)//
                .withTarget(target)//
                .withSourceNodeId(sourceNodeId)//
                .withTargetNodeId(targetNodeId)//
                .withDomainBasedEdge(edge)//
                .build()//
                .updateDiagram()//
                .assertDisplayedOnDiagram();
    }

    private Edge checkConnectorEdge(ConnectableElement source, String sourceNodeId, ConnectableElement target, String targetNodeId, StructuredClassifier parent) {
        var edge = this.createIn(Connector.class, parent);

        ConnectorEnd sourceConnectorEnd = this.createIn(ConnectorEnd.class, edge);
        sourceConnectorEnd.setRole(source);
        ConnectorEnd targetConnectorEnd = this.createIn(ConnectorEnd.class, edge);
        targetConnectorEnd.setRole(target);

        return this.getServiceTester().buildDomainBasedEdgeTestHelper(ID_BUILDER)//
                .withSource(source)//
                .withTarget(target)//
                .withSourceNodeId(sourceNodeId)//
                .withTargetNodeId(targetNodeId)//
                .withDomainBasedEdge(edge)//
                .build()//
                .updateDiagram()//
                .assertDisplayedOnDiagram();
    }

    /**
     * Checks all the possible owner of comments.
     */
    @Test
    public void checkCommentParent() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);

        Class clazz = this.createIn(Class.class, pack);

        // In Class
        this.getDiagramHelper().init(clazz, CSDDiagramDescriptionBuilder.CSD_REP_NAME);
        this.getDiagramHelper().createNodeInDiagram(CSD_CLASSIFIER + U_HOLDER_SUFFIX, clazz);
        Node classNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CSD_CLASSIFIER + U_CONTENT_SUFFIX, clazz);

        this.getServiceTester().assertChildCreation(classNode, UML.getComment(), UML.getElement_OwnedComment(), CSD_COMMENT_SHARED);

        // In Property
        Property property = this.createIn(Property.class, clazz);
        Node propertyHolderNode = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_CLASSIFIER + U_HOLDER_SUFFIX, property, classNode);
        Node propertyNode = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_PROPERTY_ON_CLASSIFIER + U_CONTENT_SUFFIX, propertyHolderNode, property);
        this.getServiceTester().assertChildCreation(propertyNode, UML.getComment(), UML.getElement_OwnedComment(), CSD_COMMENT_SHARED);

        // In nested class
        Class nestedClazz = this.createIn(Class.class, clazz);
        Node nestedClassNodeHolder = this.getDiagramHelper().createNodeInParent(CSD_CLASSIFIER_SHARED + U_HOLDER_SUFFIX, nestedClazz, classNode);
        Node nestedClassNode = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_CLASSIFIER_SHARED + U_CONTENT_SUFFIX, nestedClassNodeHolder,
                nestedClazz);
        this.getServiceTester().assertChildCreation(nestedClassNode, UML.getComment(), UML.getElement_OwnedComment(), CSD_COMMENT_SHARED);

        // Propery of Property
        Class type = this.createIn(Class.class, pack);
        property.setType(type);
        Property property2 = this.createIn(Property.class, type);
        Node propertyOnPropertyH = this.getDiagramHelper().createNodeInParent(CSD_PROPERTY_ON_PROPERTY + U_HOLDER_SUFFIX, property2, propertyNode);
        Node propertyOnProperty = this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CSD_PROPERTY_ON_PROPERTY + U_CONTENT_SUFFIX, propertyOnPropertyH,
                property2);

        this.getServiceTester().assertChildCreation(propertyOnProperty, UML.getComment(), UML.getElement_OwnedComment(), CSD_COMMENT_SHARED);

    }

    private Package init() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);

        this.getDiagramHelper().init(pack, CSDDiagramDescriptionBuilder.CSD_REP_NAME);

        return pack;
    }

}

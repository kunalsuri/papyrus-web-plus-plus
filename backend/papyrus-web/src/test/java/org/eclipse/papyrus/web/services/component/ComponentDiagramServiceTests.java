/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo, Artal Technologies.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.services.component;

import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.CONTENT_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.HOLDER_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.component.ComponentDiagramService;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UMLMetamodelHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.services.AbstractDiagramTest;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StructuredClassifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class gathering integration test regarding creation in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@SpringBootTest
@WebAppConfiguration
public class ComponentDiagramServiceTests extends AbstractDiagramTest {

    private static final String TOP_HOLDER_SUFFIX = UNDERSCORE + HOLDER_SUFFIX;

    private static final String TOP_CONTENT_SUFFIX = UNDERSCORE + CONTENT_SUFFIX;

    private static final IdBuilder ID_BUILDER = new IdBuilder(CPDDiagramDescriptionBuilder.CPD_PREFIX, new UMLMetamodelHelper());

    private static final String CPD_COMMENT_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getComment());

    private static final String CPD_COMPONENT_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getComponent());

    private static final String CPD_COMPONENT_SHARED_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getComponent(), CPDDiagramDescriptionBuilder.SHARED_SUFFIX);

    private static final String CPD_PROPERTY_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getProperty(), CPDDiagramDescriptionBuilder.SHARED_SUFFIX);

    private static final String CPD_PORT_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getPort(), CPDDiagramDescriptionBuilder.SHARED_SUFFIX);

    /**
     * Initialize UML Model and diagram.
     *
     * @return the root of the UML Model.
     */
    private Package init() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);

        this.getDiagramHelper().init(pack, CPDDiagramDescriptionBuilder.CPD_REP_NAME);
        this.getDiagramHelper().refresh();
        return pack;
    }

    /**
     * Test {@link ComponentDiagramService#canCreatePropertyIntoParentCPD(EObject)} inside a null parent.
     */
    @Test
    public void testCanCreatePropertyInNullParent() {
        assertFalse(this.getDiagramService().canCreatePropertyIntoParentCPD(null));
    }

    /**
     * Test {@link ComponentDiagramService#canCreatePropertyIntoParentCPD(EObject)} inside a {@link Property} typed by a
     * {@link StructuredClassifier}.
     */
    @Test
    public void testCanCreatePropertyInPropertyTypedByStructuredClassifier() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Property typedProperty = this.create(Property.class);
        component.getOwnedAttributes().add(typedProperty);
        Component componentType = this.createIn(Component.class, rootPack);
        typedProperty.setType(componentType);
        assertTrue(this.getDiagramService().canCreatePropertyIntoParentCPD(typedProperty));
    }

    /**
     * Test {@link ComponentDiagramService#canCreatePropertyIntoParentCPD(EObject)} inside a {@link Property} typed by a
     * non-{@link StructuredClassifier}.
     */
    @Test
    public void testCanCreatePropertyInPropertyTypedByNonStructuredClassifier() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Property typedProperty = this.create(Property.class);
        component.getOwnedAttributes().add(typedProperty);
        Actor actor = this.createIn(Actor.class, rootPack);
        typedProperty.setType(actor);
        assertFalse(this.getDiagramService().canCreatePropertyIntoParentCPD(typedProperty));
    }

    /**
     * Test {@link ComponentDiagramService#canCreatePropertyIntoParentCPD(EObject)} inside a not typed {@link Property}.
     */
    @Test
    public void testCanCreatePropertyInNotTypedProperty() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Property property = this.create(Property.class);
        component.getOwnedAttributes().add(property);
        assertFalse(this.getDiagramService().canCreatePropertyIntoParentCPD(property));
    }

    /**
     * Test {@link ComponentDiagramService#canCreatePropertyIntoParentCPD(EObject)} inside a {@link Component}.
     */
    @Test
    public void testCanCreatePropertyInComponent() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        assertTrue(this.getDiagramService().canCreatePropertyIntoParentCPD(component));
    }

    /**
     * Test {@link ComponentDiagramService#canCreatePropertyIntoParentCPD(EObject)} inside a {@link Comment}.
     */
    @Test
    public void testCanCreatePropertyInComment() {
        Package rootPack = this.init();
        Comment comment = this.createIn(Comment.class, rootPack);
        assertFalse(this.getDiagramService().canCreatePropertyIntoParentCPD(comment));
    }

    /**
     * Test {@link ComponentDiagramService#canCreatePropertyIntoParentCPD(EObject)} inside a {@link Port}.
     */
    @Test
    public void testCanCreatePropertyInPort() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Port port = this.create(Port.class);
        component.getOwnedAttributes().add(port);
        assertFalse(this.getDiagramService().canCreatePropertyIntoParentCPD(port));
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPortCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Component}.
     */
    @Test
    public void testCreatePortInComponent() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);

        org.eclipse.sirius.components.diagrams.Node componentNode = this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, component);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPortCPD(component, componentNode, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check Port creation
        assertTrue(newElement instanceof Port);
        Port port = (Port) newElement;
        assertEquals(component, port.eContainer());
        assertTrue(component.getOwnedAttributes().contains(port));

        // check border node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CPD_PORT_NODE_NAME, componentNode, newElement);
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPortCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Property} type by a {@link StructuredClassifier}.
     */
    @Test
    public void testCreatePortInPropertyTypedByStructuredClassifier() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Property typedProperty = this.create(Property.class);
        component.getOwnedAttributes().add(typedProperty);
        Component componentType = this.createIn(Component.class, rootPack);
        typedProperty.setType(componentType);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node componentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node typedPropertyNode = this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, typedProperty, componentNodeContent);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPortCPD(typedProperty, typedPropertyNode, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check Port creation
        assertTrue(newElement instanceof Port);
        Port port = (Port) newElement;
        assertEquals(componentType, port.eContainer());
        assertTrue(componentType.getOwnedAttributes().contains(port));

        // check border node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CPD_PORT_NODE_NAME, typedPropertyNode, newElement);
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPortCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Property} typed by a non-{@link StructuredClassifier}.
     */
    @Test
    public void testCreatePortInPropertyTypedByNonStructuredClassifier() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Property typedProperty = this.create(Property.class);
        component.getOwnedAttributes().add(typedProperty);
        Actor actorType = this.createIn(Actor.class, rootPack);
        typedProperty.setType(actorType);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node componentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node typedPropertyNode = this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, typedProperty, componentNodeContent);

        this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPortCPD(typedProperty, typedPropertyNode, context, this.getDiagramHelper().getConvertedNodes());
            assertEquals(AbstractDiagramService.FAILURE_OBJECT, aNewElement);
        });
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPortCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Comment}.
     */
    @Test
    public void testCreatePortInComment() {
        Package rootPack = this.init();
        Comment comment = this.createIn(Comment.class, rootPack);

        org.eclipse.sirius.components.diagrams.Node commentNode = this.getDiagramHelper().createNodeInDiagram(CPD_COMMENT_NODE_NAME, comment);

        this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPortCPD(comment, commentNode, context, this.getDiagramHelper().getConvertedNodes());
            assertEquals(AbstractDiagramService.FAILURE_OBJECT, aNewElement);
        });
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPortCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside an untyped {@link Property}. The creation of {@link Port} fails because there is no type.
     */
    @Test
    public void testCreatePortOnUntypedProperty() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Property untypedProperty = this.create(Property.class);
        component.getOwnedAttributes().add(untypedProperty);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node componentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node typedPropertyNode = this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, untypedProperty, componentNodeContent);

        this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPortCPD(untypedProperty, typedPropertyNode, context, this.getDiagramHelper().getConvertedNodes());
            assertEquals(AbstractDiagramService.FAILURE_OBJECT, aNewElement);
        });
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPropertyCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Component}.
     */
    @Test
    public void testCreatePropertyInComponent() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node componentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, component);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPropertyCPD(component, componentNodeContent, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check Property creation
        assertTrue(newElement instanceof Property);
        Property property = (Property) newElement;
        assertEquals(component, property.eContainer());
        assertTrue(component.getOwnedAttributes().contains(property));

        // check node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, componentNodeContent, newElement);
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPropertyCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Property} typed by a {@link StructuredClassifier}.
     */
    @Test
    public void testCreatePropertyInPropertyTypedByStructuredClassifier() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Property typedProperty = this.create(Property.class);
        component.getOwnedAttributes().add(typedProperty);
        Component componentType = this.createIn(Component.class, rootPack);
        typedProperty.setType(componentType);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node componentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, component);
        this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, typedProperty, componentNodeContent);
        org.eclipse.sirius.components.diagrams.Node typedPropertyNode = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_PROPERTY_NODE_NAME + TOP_CONTENT_SUFFIX,
                typedProperty);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPropertyCPD(typedProperty, typedPropertyNode, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check Property creation
        assertTrue(newElement instanceof Property);
        Property property = (Property) newElement;
        assertEquals(componentType, property.eContainer());
        assertTrue(componentType.getOwnedAttributes().contains(property));

        // check node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, typedPropertyNode, newElement);
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPropertyCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Property} typed by a non-{@link StructuredClassifier}.
     */
    @Test
    public void testCreatePropertyInPropertyTypedByNonStructuredClassifier() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Property typedProperty = this.create(Property.class);
        component.getOwnedAttributes().add(typedProperty);
        Actor actorType = this.createIn(Actor.class, rootPack);
        typedProperty.setType(actorType);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node componentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node typedPropertyNode = this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, typedProperty, componentNodeContent);

        this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPropertyCPD(typedProperty, typedPropertyNode, context, this.getDiagramHelper().getConvertedNodes());
            assertEquals(AbstractDiagramService.FAILURE_OBJECT, aNewElement);
        });
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPropertyCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside an untyped {@link Property}. The creation of {@link Property} fails because there is no type.
     */
    @Test
    public void testCreatePropertyOnUntypedProperty() {
        Package rootPack = this.init();
        Component component = this.createIn(Component.class, rootPack);
        Property untypedProperty = this.create(Property.class);
        component.getOwnedAttributes().add(untypedProperty);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node componentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, component);
        org.eclipse.sirius.components.diagrams.Node typedPropertyNode = this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, untypedProperty, componentNodeContent);

        this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPropertyCPD(untypedProperty, typedPropertyNode, context, this.getDiagramHelper().getConvertedNodes());
            assertEquals(AbstractDiagramService.FAILURE_OBJECT, aNewElement);
        });
    }

    /**
     * Test
     * {@link ComponentDiagramService#createPropertyCPD(EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Comment}.
     */
    @Test
    public void testCreatePropertyInComment() {
        Package rootPack = this.init();
        Comment comment = this.createIn(Comment.class, rootPack);

        org.eclipse.sirius.components.diagrams.Node commentNode = this.getDiagramHelper().createNodeInDiagram(CPD_COMMENT_NODE_NAME, comment);

        this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createPropertyCPD(comment, commentNode, context, this.getDiagramHelper().getConvertedNodes());
            assertEquals(AbstractDiagramService.FAILURE_OBJECT, aNewElement);
        });
    }

    /**
     * Test {@link ComponentDiagramService#getPortCandidatesCPD(EObject)} from {@link Component} with no {@link Port}.
     */
    @Test
    public void testGetEmptyPortCandidates() {
        Component component = this.create(Component.class);
        assertTrue(this.getDiagramService().getPortCandidatesCPD(component).isEmpty());
    }

    /**
     * Test {@link ComponentDiagramService#getPortCandidatesCPD(EObject)} from null Object.
     */
    @Test
    public void testGetPortCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getPortCandidatesCPD(null).isEmpty());
    }

    /**
     * Test {@link ComponentDiagramService#getPortCandidatesCPD(EObject)} from {@link Component} with {@link Port}.
     */
    @Test
    public void testGetPortCandidates() {
        Component component = this.create(Component.class);
        Port port = this.create(Port.class);
        component.getOwnedAttributes().add(port);

        // check one Port among list of one Port from Component
        List<? extends Property> portCandidatesCPD = this.getDiagramService().getPortCandidatesCPD(component);
        assertFalse(portCandidatesCPD.isEmpty());
        assertEquals(1, portCandidatesCPD.size());
        assertTrue(portCandidatesCPD.contains(port));

        // check one Port among list of one Port and one Property from Component
        Property property = this.create(Property.class);
        component.getOwnedAttributes().add(property);
        portCandidatesCPD = this.getDiagramService().getPortCandidatesCPD(component);
        assertFalse(portCandidatesCPD.isEmpty());
        assertEquals(1, portCandidatesCPD.size());
        assertTrue(portCandidatesCPD.contains(port));

        // check Property inherited from an other Component
        Component subComponent = this.create(Component.class);
        Generalization generalization = this.create(Generalization.class);
        generalization.setSpecific(subComponent);
        generalization.setGeneral(component);
        subComponent.getGeneralizations().add(generalization);
        portCandidatesCPD = this.getDiagramService().getPortCandidatesCPD(component);
        assertFalse(portCandidatesCPD.isEmpty());
        assertEquals(1, portCandidatesCPD.size());
        assertTrue(portCandidatesCPD.contains(port));

        // check Property on Property typed by StructuredClassifier with Port
        Property typedProperty = this.create(Property.class);
        typedProperty.setType(component);
        portCandidatesCPD = this.getDiagramService().getPortCandidatesCPD(typedProperty);
        assertFalse(portCandidatesCPD.isEmpty());
        assertEquals(1, portCandidatesCPD.size());
        assertTrue(portCandidatesCPD.contains(port));

        // check one Port among list of one Port from Class
        Class c1 = this.create(Class.class);
        Port p1 = this.create(Port.class);
        c1.getOwnedAttributes().add(p1);
        portCandidatesCPD = this.getDiagramService().getPortCandidatesCPD(c1);
        assertFalse(portCandidatesCPD.isEmpty());
        assertEquals(1, portCandidatesCPD.size());
        assertTrue(portCandidatesCPD.contains(p1));

        // check Property on Property typed by non-StructuredClassifier
        Actor actor = this.create(Actor.class);
        typedProperty.setType(actor);
        portCandidatesCPD = this.getDiagramService().getPortCandidatesCPD(typedProperty);
        assertTrue(portCandidatesCPD.isEmpty());
    }

    /**
     * Test {@link ComponentDiagramService#getPropertyCandidatesCPD(EObject)} from {@link Component} with no
     * {@link Property}.
     */
    @Test
    public void testGetEmptyPropertiesCandidates() {
        Component component = this.create(Component.class);
        assertTrue(this.getDiagramService().getPropertyCandidatesCPD(component).isEmpty());
    }

    /**
     * Test {@link ComponentDiagramService#getPropertyCandidatesCPD(EObject)} from null Object.
     */
    @Test
    public void testGetPropertyCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getPropertyCandidatesCPD(null).isEmpty());
    }

    /**
     * Test {@link ComponentDiagramService#getPropertyCandidatesCPD(EObject)} from {@link Component} with
     * {@link Property}.
     */
    @Test
    public void testGetPropertiesCandidates() {
        Component component = this.create(Component.class);
        Property property = this.create(Property.class);
        component.getOwnedAttributes().add(property);

        // check one Property among list of one Property from Component
        List<? extends Property> propertyCandidatesCPD = this.getDiagramService().getPropertyCandidatesCPD(component);
        assertFalse(propertyCandidatesCPD.isEmpty());
        assertEquals(1, propertyCandidatesCPD.size());
        assertTrue(propertyCandidatesCPD.contains(property));

        // check one Property among list of one Property and one Port from Component
        Port port = this.create(Port.class);
        component.getOwnedAttributes().add(port);
        propertyCandidatesCPD = this.getDiagramService().getPropertyCandidatesCPD(component);
        assertFalse(propertyCandidatesCPD.isEmpty());
        assertEquals(1, propertyCandidatesCPD.size());
        assertTrue(propertyCandidatesCPD.contains(property));

        // check Property inherited from an other Component
        Component subComponent = this.create(Component.class);
        Generalization generalization = this.create(Generalization.class);
        generalization.setSpecific(subComponent);
        generalization.setGeneral(component);
        subComponent.getGeneralizations().add(generalization);
        propertyCandidatesCPD = this.getDiagramService().getPropertyCandidatesCPD(component);
        assertFalse(propertyCandidatesCPD.isEmpty());
        assertEquals(1, propertyCandidatesCPD.size());
        assertTrue(propertyCandidatesCPD.contains(property));

        // check Property on Property typed by StructuredClassifier with Port
        Property typedProperty = this.create(Property.class);
        typedProperty.setType(component);
        propertyCandidatesCPD = this.getDiagramService().getPropertyCandidatesCPD(typedProperty);
        assertFalse(propertyCandidatesCPD.isEmpty());
        assertEquals(1, propertyCandidatesCPD.size());
        assertTrue(propertyCandidatesCPD.contains(property));

        // check one Property among list of one Property from Class
        Class c1 = this.create(Class.class);
        Property p1 = this.create(Property.class);
        c1.getOwnedAttributes().add(p1);
        propertyCandidatesCPD = this.getDiagramService().getPropertyCandidatesCPD(c1);
        assertFalse(propertyCandidatesCPD.isEmpty());
        assertEquals(1, propertyCandidatesCPD.size());
        assertTrue(propertyCandidatesCPD.contains(p1));

        // check Property on Property typed by non-StructuredClassifier
        Actor actor = this.create(Actor.class);
        typedProperty.setType(actor);
        propertyCandidatesCPD = this.getDiagramService().getPropertyCandidatesCPD(typedProperty);
        assertTrue(propertyCandidatesCPD.isEmpty());
    }

    /**
     * Test creation of {@link Connector} between {@link Port} of {@link Component}.
     */
    @Test
    public void testConnectorEdgeCreationBetweenPortOnComponent() {
        Package pack = this.init();
        Component rootComponent = this.createIn(Component.class, pack);

        Component type1 = this.create(Component.class);
        rootComponent.getPackagedElements().add(type1);
        Port portSource = this.createIn(Port.class, type1);

        Component type2 = this.create(Component.class);
        rootComponent.getPackagedElements().add(type2);
        Port portTarget = this.createIn(Port.class, type2);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, rootComponent);
        Node rootComponentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, rootComponent);

        Node type1NodeHolder = this.getDiagramHelper().createNodeInParent(CPD_COMPONENT_SHARED_NODE_NAME + TOP_HOLDER_SUFFIX, type1, rootComponentNodeContent);
        Node type2NodeHolder = this.getDiagramHelper().createNodeInParent(CPD_COMPONENT_SHARED_NODE_NAME + TOP_HOLDER_SUFFIX, type2, rootComponentNodeContent);

        Node portSourceNode = this.getDiagramHelper().createNodeInParent(CPD_PORT_NODE_NAME, portSource, type1NodeHolder);
        Node portTargetNode = this.getDiagramHelper().createNodeInParent(CPD_PORT_NODE_NAME, portTarget, type2NodeHolder);

        this.checkConnectorEdge(portSource, portSourceNode.getId(), portTarget, portTargetNode.getId(), rootComponent);

        // Also check that a new connector can be created between the two ports
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(portSource)//
                .withTarget(portTarget)//
                .withSourceNodeId(portSourceNode.getId())//
                .withTargetNodeId(portTargetNode.getId())//
                .withExpectedContainementRef(UML.getStructuredClassifier_OwnedConnector())//
                .withExpectedOwner(rootComponent)//
                .withType(UML.getConnector())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();

    }

    /**
     * Test creation of {@link Connector} between {@link Port} of {@link Property}.
     */
    @Test
    public void testConnectorEdgeCreationBetweenPortOnProperties() {
        Package pack = this.init();

        Component rootComponent = this.createIn(Component.class, pack);

        Property sourceProp = this.createIn(Property.class, rootComponent);
        Component type1 = this.create(Component.class);
        rootComponent.getPackagedElements().add(type1);
        sourceProp.setType(type1);
        Port sourcePort = this.createIn(Port.class, type1);

        Component type2 = this.create(Component.class);
        rootComponent.getPackagedElements().add(type2);
        Property targetProp = this.createIn(Property.class, rootComponent);
        targetProp.setType(type2);
        Port targetPort = this.createIn(Port.class, type2);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, rootComponent);
        Node rootComponentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, rootComponent);

        Node prop1Node = this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, sourceProp, rootComponentNodeContent);
        Node sourceNode = this.getDiagramHelper().createNodeInParent(CPD_PORT_NODE_NAME, sourcePort, prop1Node);

        Node prop2Node = this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, targetProp, rootComponentNodeContent);
        Node targetNode = this.getDiagramHelper().createNodeInParent(CPD_PORT_NODE_NAME, targetPort, prop2Node);

        this.checkConnectorEdge(sourcePort, sourceProp, sourceNode.getId(), targetPort, targetProp, targetNode.getId(), rootComponent);

        // Also check that a new connector can be created between the two ports
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(sourcePort)//
                .withTarget(targetPort)//
                .withSourceNodeId(sourceNode.getId())//
                .withTargetNodeId(targetNode.getId())//
                .withExpectedContainementRef(UML.getStructuredClassifier_OwnedConnector())//
                .withExpectedOwner(rootComponent)//
                .withType(UML.getConnector())//
                .build()//
                .emulateCreationTool()//
                .assertEdgeCreation();

    }

    /**
     * Test creation of {@link Connector} between {@link Property}.
     */
    @Test
    public void testConnectorEdgeCreationBetweenProperties() {
        Package pack = this.init();

        Component rootComponent = this.createIn(Component.class, pack);

        Property sourceProp = this.createIn(Property.class, rootComponent);
        Class type1 = this.createIn(Class.class, pack);
        sourceProp.setType(type1);
        Class type2 = this.createIn(Class.class, pack);
        Property targetProp = this.createIn(Property.class, rootComponent);
        targetProp.setType(type2);

        this.getDiagramHelper().createNodeInDiagram(CPD_COMPONENT_NODE_NAME + TOP_HOLDER_SUFFIX, rootComponent);
        Node rootComponentNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(CPD_COMPONENT_NODE_NAME + TOP_CONTENT_SUFFIX, rootComponent);

        Node sourcePropNode = this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, sourceProp, rootComponentNodeContent);
        Node targetPropNode = this.getDiagramHelper().createNodeInParent(CPD_PROPERTY_NODE_NAME + TOP_HOLDER_SUFFIX, targetProp, rootComponentNodeContent);

        this.checkConnectorEdge(sourceProp, sourcePropNode.getId(), targetProp, targetPropNode.getId(), rootComponent);

        // Also check that a new connector can be created between the two properties
        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER)//
                .withSource(sourceProp)//
                .withTarget(targetProp)//
                .withSourceNodeId(sourcePropNode.getId())//
                .withTargetNodeId(targetPropNode.getId())//
                .withExpectedContainementRef(UML.getStructuredClassifier_OwnedConnector())//
                .withExpectedOwner(rootComponent)//
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

    @Override
    protected ComponentDiagramService buildService() {
        return new ComponentDiagramService(this.getIdentityService(), this.getLabelService(),
                this.getObjectSearchService(), this.getDiagramNavigationService(), this.getDiagramOperationsService(),
                e -> true, this.getViewDiagramDescriptionService(),
                new MockLogger());
    }

    @Override
    protected ComponentDiagramService getDiagramService() {
        return (ComponentDiagramService) super.getDiagramService();
    }
}

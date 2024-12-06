/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.services.deployment;

import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.CONTENT_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.HOLDER_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.web.application.representations.aqlservices.deployment.DeploymentDiagramService;
import org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.DDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UMLMetamodelHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.services.AbstractDiagramTest;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.uml2.uml.Artifact;
import org.eclipse.uml2.uml.CommunicationPath;
import org.eclipse.uml2.uml.DeploymentSpecification;
import org.eclipse.uml2.uml.Device;
import org.eclipse.uml2.uml.ExecutionEnvironment;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.Manifestation;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Node;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Pin;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class gathering integration test regarding creation in the Deployment Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@SpringBootTest
@WebAppConfiguration
public class DeploymentDiagramServiceTests extends AbstractDiagramTest {

    private static final String TOP_HOLDER_SUFFIX = UNDERSCORE + HOLDER_SUFFIX;

    private static final String TOP_CONTENT_SUFFIX = UNDERSCORE + CONTENT_SUFFIX;

    private static final String ARTIFACT = UML.getArtifact().getName();

    private static final String NODE = UML.getNode().getName();

    private static final IdBuilder ID_BUILDER = new IdBuilder(DDDiagramDescriptionBuilder.DD_PREFIX, new UMLMetamodelHelper());

    private static final String DD_ARTIFACT_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getArtifact());

    private static final String DD_ARTIFACT_SHARED_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getArtifact(), AbstractRepresentationDescriptionBuilder.SHARED_SUFFIX);

    private static final String DD_DEVICE_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getDevice());

    private static final String DD_MANIFESTATION_EDGE_NAME = ID_BUILDER.getDomainBaseEdgeId(UML.getManifestation());

    private static final String DD_NODE_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getNode());

    private static final String DD_NODE_SHARED_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getNode(), AbstractRepresentationDescriptionBuilder.SHARED_SUFFIX);

    private static final String DD_PACKAGE_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getPackage());

    /**
     * Test {@link DeploymentDiagramService#getArtifactCandidatesDD(org.eclipse.uml2.uml.Element)} from {@link Package}
     * with no {@link Artifact}.
     */
    @Test
    public void testGetEmptyArtifactCandidates() {
        Package pack = this.create(Package.class);
        assertTrue(this.getDiagramService().getArtifactCandidatesDD(pack).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getArtifactCandidatesDD(org.eclipse.uml2.uml.Element)} from a {@code null}
     * element.
     */
    @Test
    public void testGetArtifactCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getArtifactCandidatesDD(null).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getArtifactCandidatesDD(org.eclipse.uml2.uml.Element)} from container with
     * {@link Artifact}.
     */
    @Test
    public void testGetArtifactCandidates() {
        Model model = this.create(Model.class);
        Artifact rootArtifact = this.create(Artifact.class);
        Node rootNode = this.create(Node.class);
        Artifact artifactInModel = this.create(Artifact.class);
        DeploymentSpecification deploymentSpecificationInModel = this.create(DeploymentSpecification.class);
        model.getPackagedElements().addAll(List.of(artifactInModel, deploymentSpecificationInModel));

        // We check Artifact within a Package type (a Model here)
        List<? extends Artifact> artifactCandidates = this.getDiagramService().getArtifactCandidatesDD(model);
        assertEquals(1, artifactCandidates.size());
        assertTrue(artifactCandidates.contains(artifactInModel));

        Artifact artifactInArtifact = this.create(Artifact.class);
        DeploymentSpecification deploymentSpecificationInArtifact = this.create(DeploymentSpecification.class);
        rootArtifact.getNestedArtifacts().addAll(List.of(artifactInArtifact, deploymentSpecificationInArtifact));

        // We check Artifact within a Artifact type
        artifactCandidates = this.getDiagramService().getArtifactCandidatesDD(rootArtifact);
        assertEquals(1, artifactCandidates.size());
        assertTrue(artifactCandidates.contains(artifactInArtifact));

        Artifact artifactInNode = this.create(Artifact.class);
        DeploymentSpecification deploymentSpecificationInNode = this.create(DeploymentSpecification.class);
        rootNode.getNestedClassifiers().addAll(List.of(artifactInNode, deploymentSpecificationInNode));

        // We check Artifact within a Node type
        artifactCandidates = this.getDiagramService().getArtifactCandidatesDD(rootNode);
        assertEquals(1, artifactCandidates.size());
        assertTrue(artifactCandidates.contains(artifactInNode));

        // Check a type that is not supported
        Pin pin = this.create(InputPin.class);
        artifactCandidates = this.getDiagramService().getArtifactCandidatesDD(pin);
        assertEquals(0, artifactCandidates.size());
    }

    /**
     * Test {@link DeploymentDiagramService#getCommunicationPathCandidatesDD(org.eclipse.emf.ecore.EObject)} from
     * {@link Package} with no {@link CommunicationPath}.
     */
    @Test
    public void testGetEmptyCommunicationPathCandidates() {
        Package pack = this.create(Package.class);
        assertTrue(this.getDiagramService().getCommunicationPathCandidatesDD(pack).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getCommunicationPathCandidatesDD(org.eclipse.emf.ecore.EObject)} from a
     * {@code null} element.
     */
    @Test
    public void testGetCommunicationPathCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getCommunicationPathCandidatesDD(null).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getCommunicationPathCandidatesDD(org.eclipse.emf.ecore.EObject)} from
     * {@link Package} with {@link CommunicationPath}.
     */
    @Test
    public void testGetCommunicationPathCandidates() {
        Package pack = this.create(Package.class);
        CommunicationPath communicationPath = this.create(CommunicationPath.class);
        pack.getPackagedElements().add(communicationPath);
        Collection<CommunicationPath> communicationPathCandidates = this.getDiagramService().getCommunicationPathCandidatesDD(pack);
        assertFalse(communicationPathCandidates.isEmpty());
        assertEquals(1, communicationPathCandidates.size());
        assertTrue(communicationPathCandidates.contains(communicationPath));

        // test multiple {@link CommunicationPath} at different level
        Package pack2 = this.createIn(Package.class, pack);
        CommunicationPath communicationPath2 = this.create(CommunicationPath.class);
        pack2.getPackagedElements().add(communicationPath2);
        communicationPathCandidates = this.getDiagramService().getCommunicationPathCandidatesDD(pack);
        assertFalse(communicationPathCandidates.isEmpty());
        assertEquals(2, communicationPathCandidates.size());
        assertTrue(communicationPathCandidates.contains(communicationPath));
        assertTrue(communicationPathCandidates.contains(communicationPath2));
    }

    /**
     * Test {@link DeploymentDiagramService#getDeploymentSpecificationCandidatesDD(org.eclipse.uml2.uml.Element)} from
     * {@link Package} with no {@link DeploymentSpecification}.
     */
    @Test
    public void testGetEmptyDeploymentSpecificationCandidates() {
        Package pack = this.create(Package.class);
        assertTrue(this.getDiagramService().getDeploymentSpecificationCandidatesDD(pack).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getDeploymentSpecificationCandidatesDD(org.eclipse.uml2.uml.Element)} from a
     * {@code null} element.
     */
    @Test
    public void testGetDeploymentSpecificationCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getDeploymentSpecificationCandidatesDD(null).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getDeploymentSpecificationCandidatesDD(org.eclipse.uml2.uml.Element)} from
     * container with {@link DeploymentSpecification}.
     */
    @Test
    public void testGetDeploymentSpecificationCandidates() {
        Model model = this.create(Model.class);
        Node rootNode = this.create(Node.class);
        Artifact artifactInModel = this.create(Artifact.class);
        DeploymentSpecification deploymentSpecificationInModel = this.create(DeploymentSpecification.class);
        model.getPackagedElements().addAll(List.of(artifactInModel, deploymentSpecificationInModel));

        // We check DeploymentSpecification within a Package type (a Model here)
        List<? extends Artifact> deploymentSpecificationCandidates = this.getDiagramService().getDeploymentSpecificationCandidatesDD(model);
        assertEquals(1, deploymentSpecificationCandidates.size());
        assertTrue(deploymentSpecificationCandidates.contains(deploymentSpecificationInModel));

        Artifact artifactInArtifact = this.create(Artifact.class);
        DeploymentSpecification deploymentSpecificationInArtifact = this.create(DeploymentSpecification.class);
        artifactInModel.getNestedArtifacts().addAll(List.of(artifactInArtifact, deploymentSpecificationInArtifact));

        // We check DeploymentSpecification within a Artifact type
        deploymentSpecificationCandidates = this.getDiagramService().getDeploymentSpecificationCandidatesDD(artifactInModel);
        assertEquals(1, deploymentSpecificationCandidates.size());
        assertTrue(deploymentSpecificationCandidates.contains(deploymentSpecificationInArtifact));

        Artifact artifactInNode = this.create(Artifact.class);
        DeploymentSpecification deploymentSpecificationInNode = this.create(DeploymentSpecification.class);
        rootNode.getNestedClassifiers().addAll(List.of(artifactInNode, deploymentSpecificationInNode));

        // We check Artifact within a Node type
        deploymentSpecificationCandidates = this.getDiagramService().getDeploymentSpecificationCandidatesDD(rootNode);
        assertEquals(1, deploymentSpecificationCandidates.size());
        assertTrue(deploymentSpecificationCandidates.contains(deploymentSpecificationInNode));

        // Check a type that is not supported
        Pin pin = this.create(InputPin.class);
        deploymentSpecificationCandidates = this.getDiagramService().getDeploymentSpecificationCandidatesDD(pin);
        assertEquals(0, deploymentSpecificationCandidates.size());
    }

    /**
     * Test {@link DeploymentDiagramService#getDeviceCandidates(org.eclipse.uml2.uml.Element)} from {@link Package} with
     * no {@link Device}.
     */
    @Test
    public void testGetEmptyDeviceCandidates() {
        Package pack = this.create(Package.class);
        assertTrue(this.getDiagramService().getDeviceCandidatesDD(pack).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getDeviceCandidates(org.eclipse.uml2.uml.Element)} from a {@code null}
     * element.
     */
    @Test
    public void testGetDeviceCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getDeviceCandidatesDD(null).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getDeviceCandidates(org.eclipse.uml2.uml.Element)} from container with
     * {@link Device}.
     */
    @Test
    public void testGetDeviceCandidates() {
        Model model = this.create(Model.class);
        Artifact rootArtifact = this.create(Artifact.class);
        Device rootDevice = this.create(Device.class);
        Device deviceInModel = this.create(Device.class);
        Device device2InModel = this.create(Device.class);
        Node nodeInModel = this.create(Node.class);
        model.getPackagedElements().addAll(List.of(deviceInModel, device2InModel, nodeInModel));

        // Check Device in Package
        List<? extends Node> deviceCandidates = this.getDiagramService().getDeviceCandidatesDD(model);
        assertEquals(2, deviceCandidates.size());
        assertTrue(List.of(deviceInModel, device2InModel).containsAll(deviceCandidates));

        Device deviceInDevice = this.create(Device.class);
        Device device2InDevice = this.create(Device.class);
        rootDevice.getNestedNodes().addAll(List.of(deviceInDevice, device2InDevice));

        // Check Device in Device
        deviceCandidates = this.getDiagramService().getDeviceCandidatesDD(rootDevice);
        assertEquals(2, deviceCandidates.size());
        assertTrue(List.of(deviceInDevice, device2InDevice).containsAll(deviceCandidates));

        // Check a type that is not supported
        deviceCandidates = this.getDiagramService().getDeviceCandidatesDD(rootArtifact);
        assertEquals(0, deviceCandidates.size());
    }

    /**
     * Test {@link DeploymentDiagramService#getNodeCandidatesDD(org.eclipse.uml2.uml.Element)} from {@link Package} with
     * no {@link Node}.
     */
    @Test
    public void testGetEmptyNodeCandidates() {
        Package pack = this.create(Package.class);
        assertTrue(this.getDiagramService().getNodeCandidatesDD(pack).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getNodeCandidatesDD(org.eclipse.uml2.uml.Element)} from a {@code null}
     * element.
     */
    @Test
    public void testGetNodeCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getNodeCandidatesDD(null).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getNodeCandidatesDD(org.eclipse.uml2.uml.Element)} from container with
     * {@link Node}.
     */
    @Test
    public void testGetNodeCandidates() {
        Model model = this.create(Model.class);
        Artifact rootArtifact = this.create(Artifact.class);
        Node rootNode = this.create(Node.class);
        Node nodeInModel = this.create(Node.class);
        Node node2InModel = this.create(Node.class);
        Device deviceInModel = this.create(Device.class);
        model.getPackagedElements().addAll(List.of(nodeInModel, node2InModel, deviceInModel));

        // Check Node in Package
        List<? extends Node> nodeCandidates = this.getDiagramService().getNodeCandidatesDD(model);
        assertEquals(2, nodeCandidates.size());
        assertTrue(List.of(nodeInModel, node2InModel).containsAll(nodeCandidates));

        Node nodeInNode = this.create(Node.class);
        Node node2InNode = this.create(Node.class);
        rootNode.getNestedNodes().addAll(List.of(nodeInNode, node2InNode));

        // Check Node in Node
        nodeCandidates = this.getDiagramService().getNodeCandidatesDD(rootNode);
        assertEquals(2, nodeCandidates.size());
        assertTrue(List.of(nodeInNode, node2InNode).containsAll(nodeCandidates));

        // Check a type that is not supported
        nodeCandidates = this.getDiagramService().getNodeCandidatesDD(rootArtifact);
        assertEquals(0, nodeCandidates.size());
    }

    /**
     * Test {@link DeploymentDiagramService#getExecutionEnvironmentCandidatesDD(org.eclipse.uml2.uml.Element)} from
     * {@link Package} with no {@link ExecutionEnvironment}.
     */
    @Test
    public void testGetEmptyExecutionEnvironmentCandidates() {
        Package pack = this.create(Package.class);
        assertTrue(this.getDiagramService().getExecutionEnvironmentCandidatesDD(pack).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getExecutionEnvironmentCandidatesDD(org.eclipse.uml2.uml.Element)} from a
     * {@code null} element.
     */
    @Test
    public void testGeExecutionEnvironmentCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getExecutionEnvironmentCandidatesDD(null).isEmpty());
    }

    /**
     * Test {@link DeploymentDiagramService#getExecutionEnvironmentCandidatesDD(org.eclipse.uml2.uml.Element)} from
     * container with {@link ExecutionEnvironment}.
     */
    @Test
    public void testExecutionEnvironmentNodeCandidates() {
        Model model = this.create(Model.class);
        Artifact rootArtifact = this.create(Artifact.class);
        ExecutionEnvironment executionEnvironmentInModel = this.create(ExecutionEnvironment.class);
        ExecutionEnvironment executionEnvironment2InModel = this.create(ExecutionEnvironment.class);
        Device deviceInModel = this.create(Device.class);
        model.getPackagedElements().addAll(List.of(executionEnvironmentInModel, executionEnvironment2InModel, deviceInModel));

        // Check ExecutionEnvironment in Package
        List<? extends Node> executionEnvironmentCandidates = this.getDiagramService().getExecutionEnvironmentCandidatesDD(model);
        assertEquals(2, executionEnvironmentCandidates.size());
        assertTrue(List.of(executionEnvironmentInModel, executionEnvironment2InModel).containsAll(executionEnvironmentCandidates));

        ExecutionEnvironment executionEnvironmentInDevice = this.create(ExecutionEnvironment.class);
        ExecutionEnvironment executionEnvironment2InDevice = this.create(ExecutionEnvironment.class);
        deviceInModel.getNestedNodes().addAll(List.of(executionEnvironmentInDevice, executionEnvironment2InDevice));

        // Check ExecutionEnvironment in Device
        executionEnvironmentCandidates = this.getDiagramService().getExecutionEnvironmentCandidatesDD(deviceInModel);
        assertEquals(2, executionEnvironmentCandidates.size());
        assertTrue(List.of(executionEnvironmentInDevice, executionEnvironment2InDevice).containsAll(executionEnvironmentCandidates));

        // Check a type that is not supported
        executionEnvironmentCandidates = this.getDiagramService().getExecutionEnvironmentCandidatesDD(rootArtifact);
        assertEquals(0, executionEnvironmentCandidates.size());
    }

    /**
     * Test
     * {@link DeploymentDiagramService#createManifestationDD(EObject, EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.core.api.IEditingContext, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext)}
     * between two {@link Device}.The new {@link Manifestation} is contained by the closest {@link Package} parent.
     */
    @Test
    public void testCreateManifestationWithPackagedElementContainmentRef() {

        Package pack = this.init();
        Device sourceDevice = this.createIn(Device.class, pack);
        Device targetDevice = this.createIn(Device.class, pack);

        org.eclipse.sirius.components.diagrams.Node sourceDeviceNode = this.getDiagramHelper().createNodeInDiagram(DD_DEVICE_NODE_NAME + TOP_HOLDER_SUFFIX, sourceDevice);
        org.eclipse.sirius.components.diagrams.Node targetDeviceNode = this.getDiagramHelper().createNodeInDiagram(DD_DEVICE_NODE_NAME + TOP_HOLDER_SUFFIX, targetDevice);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createManifestationDD(sourceDevice, targetDevice, sourceDeviceNode, targetDeviceNode, this.getEditingContext(), context);

            return aNewElement;
        });

        // check Manifestation creation and its initialization
        assertTrue(newElement instanceof Manifestation);
        Manifestation manifestation = (Manifestation) newElement;
        assertEquals(pack, manifestation.eContainer());
        assertTrue(manifestation.getSuppliers().contains(targetDevice));
        assertTrue(manifestation.getClients().contains(sourceDevice));

        // check edge creation
        this.getDiagramHelper().assertGetExistDomainBasedEdge(DD_MANIFESTATION_EDGE_NAME, newElement, sourceDeviceNode, targetDeviceNode);
    }

    /**
     * Test
     * {@link DeploymentDiagramService#createManifestationDD(EObject, EObject, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.core.api.IEditingContext, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext)}
     * between two {@link Artifact}. The new {@link Manifestation} is contained by the source {@link Artifact}.
     */
    @Test
    public void testCreateManifestationWithArtifactManifestationContainmentRef() {

        Package pack = this.init();
        Artifact sourceArtifact = this.createIn(Artifact.class, pack);
        Artifact targetArtifact = this.createIn(Artifact.class, pack);

        org.eclipse.sirius.components.diagrams.Node sourceArtifactNode = this.getDiagramHelper().createNodeInDiagram(DD_ARTIFACT_NODE_NAME + TOP_HOLDER_SUFFIX, sourceArtifact);
        org.eclipse.sirius.components.diagrams.Node targetArtifactNode = this.getDiagramHelper().createNodeInDiagram(DD_ARTIFACT_NODE_NAME + TOP_HOLDER_SUFFIX, targetArtifact);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createManifestationDD(sourceArtifact, targetArtifact, sourceArtifactNode, targetArtifactNode, this.getEditingContext(), context);

            return aNewElement;
        });

        // check Manifestation creation and its initialization
        assertTrue(newElement instanceof Manifestation);
        Manifestation manifestation = (Manifestation) newElement;
        assertEquals(sourceArtifact, manifestation.eContainer());
        assertTrue(sourceArtifact.getManifestations().contains(manifestation));
        assertTrue(manifestation.getSuppliers().contains(targetArtifact));
        assertTrue(manifestation.getClients().contains(sourceArtifact));

        // check edge creation
        this.getDiagramHelper().assertGetExistDomainBasedEdge(DD_MANIFESTATION_EDGE_NAME, newElement, sourceArtifactNode, targetArtifactNode);
    }

    /**
     * Test
     * {@link DeploymentDiagramService#createArtifactDD(EObject, String, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Package}. The containment feature used is UMLPackage.eINSTANCE.getPackage_PackagedElement().
     */
    @Test
    public void testCreateArtifactInPackage() {

        Package rootPack = this.init();
        Package childPack = this.createIn(Package.class, rootPack);
        this.getDiagramHelper().createNodeInDiagram(DD_PACKAGE_NODE_NAME + TOP_HOLDER_SUFFIX, childPack);
        org.eclipse.sirius.components.diagrams.Node childPackNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(DD_PACKAGE_NODE_NAME + TOP_CONTENT_SUFFIX, childPack);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createArtifactDD(childPack, ARTIFACT, childPackNodeContent, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check Artifact creation
        assertTrue(newElement instanceof Artifact);
        Artifact artifact = (Artifact) newElement;
        assertEquals(childPack, artifact.eContainer());
        assertTrue(childPack.getPackagedElements().contains(artifact));

        // check node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(DD_ARTIFACT_SHARED_NODE_NAME + TOP_HOLDER_SUFFIX, childPackNodeContent, newElement);
    }

    /**
     * Test
     * {@link DeploymentDiagramService#createArtifactDD(EObject, String, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Artifact}. The containment feature used is UMLPackage.eINSTANCE.getArtifact_NestedArtifact().
     */
    @Test
    public void testCreateArtifactInArtifact() {

        Package rootPack = this.init();
        Artifact childArtifact = this.createIn(Artifact.class, rootPack);
        org.eclipse.sirius.components.diagrams.Node childArtifactNodeHolder = this.getDiagramHelper().createNodeInDiagram(DD_ARTIFACT_NODE_NAME + TOP_HOLDER_SUFFIX, childArtifact);
        org.eclipse.sirius.components.diagrams.Node childArtifactNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(DD_ARTIFACT_NODE_NAME + TOP_CONTENT_SUFFIX, childArtifact);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createArtifactDD(childArtifact, ARTIFACT, childArtifactNodeContent, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check Artifact creation
        assertTrue(newElement instanceof Artifact);
        Artifact artifact = (Artifact) newElement;
        assertEquals(childArtifact, artifact.eContainer());
        assertTrue(childArtifact.getNestedArtifacts().contains(artifact));

        // check node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(DD_ARTIFACT_SHARED_NODE_NAME + TOP_HOLDER_SUFFIX, childArtifactNodeContent, newElement);
    }

    /**
     * Test
     * {@link DeploymentDiagramService#createArtifactDD(EObject, String, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Node}. The containment feature used is UMLPackage.eINSTANCE.getClass_NestedClassifier().
     */
    @Test
    public void testCreateArtifactInNode() {

        Package rootPack = this.init();
        Node childNode = this.createIn(Node.class, rootPack);
        this.getDiagramHelper().createNodeInDiagram(DD_NODE_NODE_NAME + TOP_HOLDER_SUFFIX, childNode);
        org.eclipse.sirius.components.diagrams.Node childNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(DD_NODE_NODE_NAME + TOP_CONTENT_SUFFIX, childNode);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createArtifactDD(childNode, ARTIFACT, childNodeContent, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check Artifact creation
        assertTrue(newElement instanceof Artifact);
        Artifact artifact = (Artifact) newElement;
        assertEquals(childNode, artifact.eContainer());
        assertTrue(childNode.getNestedClassifiers().contains(artifact));

        // check node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(DD_ARTIFACT_SHARED_NODE_NAME + TOP_HOLDER_SUFFIX, childNodeContent, newElement);
    }

    /**
     * Test
     * {@link DeploymentDiagramService#createNodeDD(org.eclipse.uml2.uml.Element, String, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Node}. The containment feature used is UMLPackage.eINSTANCE.getPackage_PackagedElement().
     */
    @Test
    public void testCreateNodeInPackage() {

        Package rootPack = this.init();
        Package childPack = this.createIn(Package.class, rootPack);
        this.getDiagramHelper().createNodeInDiagram(DD_PACKAGE_NODE_NAME + TOP_HOLDER_SUFFIX, childPack);
        org.eclipse.sirius.components.diagrams.Node childPackNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(DD_PACKAGE_NODE_NAME + TOP_CONTENT_SUFFIX, childPack);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createNodeDD(childPack, NODE, childPackNodeContent, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check Node creation
        assertTrue(newElement instanceof Node);
        Node node = (Node) newElement;
        assertEquals(childPack, node.eContainer());
        assertTrue(childPack.getPackagedElements().contains(node));

        // check node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(DD_NODE_SHARED_NODE_NAME + TOP_HOLDER_SUFFIX, childPackNodeContent, newElement);
    }

    /**
     * Test
     * {@link DeploymentDiagramService#createNodeDD(org.eclipse.uml2.uml.Element, String, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Device}. The containment feature used is UMLPackage.eINSTANCE.getNode_NestedNode().
     */
    @Test
    public void testCreateNodeInDevice() {

        Package rootPack = this.init();
        Device childDevice = this.createIn(Device.class, rootPack);
        this.getDiagramHelper().createNodeInDiagram(DD_DEVICE_NODE_NAME + TOP_HOLDER_SUFFIX, childDevice);
        org.eclipse.sirius.components.diagrams.Node childDeviceNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(DD_DEVICE_NODE_NAME + TOP_CONTENT_SUFFIX, childDevice);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createNodeDD(childDevice, NODE, childDeviceNodeContent, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check Node creation
        assertTrue(newElement instanceof Node);
        Node node = (Node) newElement;
        assertEquals(childDevice, node.eContainer());
        assertTrue(childDevice.getNestedNodes().contains(node));

        // check node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(DD_NODE_SHARED_NODE_NAME + TOP_HOLDER_SUFFIX, childDeviceNodeContent, newElement);
    }

    /**
     * Initialize UML Model and diagram.
     *
     * @return the root of the UML Model.
     */
    private Package init() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);

        this.getDiagramHelper().init(pack, DDDiagramDescriptionBuilder.DD_REP_NAME);
        this.getDiagramHelper().refresh();
        return pack;
    }

    @Override
    protected DeploymentDiagramService buildService() {
        return new DeploymentDiagramService(this.getObjectService(), this.getDiagramNavigationService(), this.getDiagramOperationsService(), e -> true, this.getViewDiagramDescriptionService(),
                new MockLogger());
    }

    @Override
    protected DeploymentDiagramService getDiagramService() {
        return (DeploymentDiagramService) super.getDiagramService();
    }
}

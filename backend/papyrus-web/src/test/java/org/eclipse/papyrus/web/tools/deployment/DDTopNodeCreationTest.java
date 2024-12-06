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
package org.eclipse.papyrus.web.tools.deployment;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.DDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.deployment.utils.DDMappingTypes;
import org.eclipse.papyrus.web.tools.test.NodeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node creation tools at the root of the diagram in the Deployment Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class DDTopNodeCreationTest extends NodeCreationTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    public DDTopNodeCreationTest() {
        super(DEFAULT_DOCUMENT, DDDiagramDescriptionBuilder.DD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()), UML.getDeploymentSpecification(), PACKAGED_ELEMENT) //
        );
    }

    private static Stream<Arguments> parameterHolderProvider() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getArtifact()), UML.getArtifact(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDevice()), UML.getDevice(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()), UML.getExecutionEnvironment(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getModel()), UML.getModel(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getNode()), UML.getNode(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT)//
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} on diagram")
    @MethodSource("parameterProvider")
    public void testCreateNode(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = DDMappingTypes.getMappingType(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType, this::getRootSemanticElement,
                expectedContainmentReference);
        this.createTopNode(nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @ParameterizedTest(name = "[{index}] Create Node {1} on diagram")
    @MethodSource("parameterHolderProvider")
    public void testCreateHolderNode(CreationTool nodeCreationTool, EClass expectedType, EReference expectedContainmentReference) {
        String mappingType = DDMappingTypes.getMappingType(expectedType);
        NodeCreationGraphicalChecker graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, expectedType, this::getRootSemanticElement,
                expectedContainmentReference);
        this.createTopNode(nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }

}

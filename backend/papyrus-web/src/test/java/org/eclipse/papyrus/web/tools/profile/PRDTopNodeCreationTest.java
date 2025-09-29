/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.tools.profile;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDClassifierCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDEnumerationCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.profile.utils.PRDMappingTypes;
import org.eclipse.papyrus.web.tools.test.NodeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests node creation tools at the root of the diagram in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDTopNodeCreationTest extends NodeCreationTest {

    private static final EReference PACKAGED_ELEMENT = UML.getPackage_PackagedElement();

    public PRDTopNodeCreationTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getClass_()), UML.getClass_(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getComment()), UML.getComment(), UML.getElement_OwnedComment()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getConstraint()), UML.getConstraint(), UML.getNamespace_OwnedRule()), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getDataType()), UML.getDataType(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getEnumeration()), UML.getEnumeration(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPackage()), UML.getPackage(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getPrimitiveType()), UML.getPrimitiveType(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getProfile()), UML.getProfile(), PACKAGED_ELEMENT), //
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getStereotype()), UML.getStereotype(), PACKAGED_ELEMENT)//
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
        String mappingType = PRDMappingTypes.getMappingType(expectedType);
        NodeCreationGraphicalChecker graphicalChecker;

        if (UML.getEnumeration().isSuperTypeOf(expectedType)) {
            graphicalChecker = new PRDEnumerationCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        } else if (UML.getClassifier().isSuperTypeOf(expectedType) && !UML.getPrimitiveType().isSuperTypeOf(expectedType)) {
            graphicalChecker = new PRDClassifierCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        } else if (UML.getPackage().isSuperTypeOf(expectedType)) {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        } else {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, null, mappingType, this.getCapturedNodes());
        }
        NodeCreationSemanticChecker semanticChecker = new NodeCreationSemanticChecker(this.getObjectSearchService(),
                this.getIdentityService(), this::getEditingContext, expectedType, this::getRootSemanticElement,
                expectedContainmentReference);
        this.createTopNode(nodeCreationTool, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

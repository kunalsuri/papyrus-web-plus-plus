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

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.HolderDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeGraphicalDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDClassifierDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDEnumerationDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.NodeDeletionTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests graphical deletion node tool at the root of the diagram in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDTopNodeGraphicalDeletionTest extends NodeDeletionTest {

    private static final String CLASS1 = "Class1";

    private static final String CONSTRAINT1 = "Constraint1";

    private static final String DATA_TYPE1 = "DataType1";

    private static final String ENUMERATION1 = "Enumeration1";

    private static final String PACKAGE1 = "Package1";

    private static final String PRIMITIVE_TYPE1 = "PrimitiveType1";

    private static final String PROFILE1 = "Profile1";

    private static final String STEREOTYPE1 = "Stereotype1";

    public PRDTopNodeGraphicalDeletionTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(CLASS1, UML.getPackage_PackagedElement()), //
                Arguments.of(CONSTRAINT1, UML.getNamespace_OwnedRule()), //
                Arguments.of(DATA_TYPE1, UML.getPackage_PackagedElement()), //
                Arguments.of(ENUMERATION1, UML.getPackage_PackagedElement()), //
                Arguments.of(PACKAGE1, UML.getPackage_PackagedElement()), //
                Arguments.of(PRIMITIVE_TYPE1, UML.getPackage_PackagedElement()), //
                Arguments.of(PROFILE1, UML.getPackage_PackagedElement()), //
                Arguments.of(STEREOTYPE1, UML.getPackage_PackagedElement()) //
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getClass_()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getConstraint()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getDataType()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getEnumeration()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getPrimitiveType()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getProfile()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getStereotype()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testDeleteGraphicalNode(String elementName, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker;
        if (ENUMERATION1.equals(elementName)) {
            graphicalChecker = new PRDEnumerationDeletionGraphicalChecker(this::getDiagram, null);
        } else if (List.of(CLASS1, DATA_TYPE1, STEREOTYPE1).contains(elementName)) {
            graphicalChecker = new PRDClassifierDeletionGraphicalChecker(this::getDiagram, null);
        } else if (List.of(PACKAGE1, PROFILE1).contains(elementName)) {
            graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, null);
        } else {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, null);
        }
        NodeGraphicalDeletionSemanticChecker semanticChecker = new NodeGraphicalDeletionSemanticChecker(
                this.getObjectSearchService(), this.getIdentityService(), this::getEditingContext,
                this::getRootSemanticElement,
                containmentReference);
        this.deleteGraphicalNode(elementName, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

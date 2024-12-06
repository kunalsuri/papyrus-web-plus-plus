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
package org.eclipse.papyrus.web.tools.component;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.DeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.HolderDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeSemanticDeletionSemanticChecker;
import org.eclipse.papyrus.web.tools.component.checker.CPDInterfaceDeletionGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.NodeDeletionTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests semantic deletion node tool at the root of the diagram in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDTopNodeSemanticDeletionTest extends NodeDeletionTest {

    private static final String COMPONENT1 = "Component1";

    private static final String CONSTRAINT1 = "Constraint1";

    private static final String INTERFACE1 = "Interface1";

    private static final String MODEL1 = "Model1";

    private static final String PACKAGE1 = "Package1";

    public CPDTopNodeSemanticDeletionTest() {
        super(DEFAULT_DOCUMENT, CPDDiagramDescriptionBuilder.CPD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(COMPONENT1, UML.getPackage_PackagedElement()), //
                Arguments.of(CONSTRAINT1, UML.getNamespace_OwnedRule()), //
                Arguments.of(INTERFACE1, UML.getPackage_PackagedElement()), //
                Arguments.of(MODEL1, UML.getPackage_PackagedElement()), //
                Arguments.of(PACKAGE1, UML.getPackage_PackagedElement()) //
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getConstraint()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getInterface()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getModel()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testDeleteSemanticNode(String elementName, EReference containmentReference) {
        DeletionGraphicalChecker graphicalChecker;
        if (INTERFACE1.equals(elementName)) {
            graphicalChecker = new CPDInterfaceDeletionGraphicalChecker(this::getDiagram, null);
        } else if (CONSTRAINT1.equals(elementName)) {
            graphicalChecker = new DeletionGraphicalChecker(this::getDiagram, null);
        } else {
            graphicalChecker = new HolderDeletionGraphicalChecker(this::getDiagram, null);
        }
        NodeSemanticDeletionSemanticChecker semanticChecker = new NodeSemanticDeletionSemanticChecker(this.getObjectService(), this::getEditingContext, this::getRootSemanticElement,
                containmentReference);
        this.deleteSemanticNode(elementName, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

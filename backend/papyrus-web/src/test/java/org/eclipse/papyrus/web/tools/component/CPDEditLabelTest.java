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

import org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters;
import org.eclipse.papyrus.web.application.representations.uml.CPDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.LabelGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.LabelSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EditLabelTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edit label tools in the Component Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CPDEditLabelTest extends EditLabelTest {

    private static final String COMPONENT = "Component1";

    private static final String COMPONENT_LABEL_PREFIX = UMLCharacters.ST_LEFT + "component" + UMLCharacters.ST_RIGHT + System.lineSeparator();

    private static final String CONSTRAINT = "Constraint1";

    private static final String CONSTRAINT_LABEL_SUFFIX = System.lineSeparator() + "{{OCL} true}";

    private static final String INTERFACE = "Interface1";

    private static final String INTERFACE_LABEL_PREFIX = UMLCharacters.ST_LEFT + "interface" + UMLCharacters.ST_RIGHT + System.lineSeparator();

    private static final String MODEL = "Model1";

    private static final String OPERATION = "Operation1";

    private static final String OPERATION_LABEL_PREFIX = "+ ";

    private static final String PACKAGE = "Package1";

    private static final String PORT = "Port1";

    private static final String PROPERTY_LABEL_PREFIX = "+ ";

    private static final String PROPERTY_LABEL_SUFFIX = ": <Undefined> [1]";

    private static final String PROPERTY = "Property1";

    private static final String RECEPTION = "Reception1";

    private static final String RECEPTION_LABEL_PREFIX = UMLCharacters.ST_LEFT + "signal" + UMLCharacters.ST_RIGHT + " + ";

    private static final String NEW_LABEL = "New Label";

    private static final String OPERATION_LABEL_SUFFIX = "()";

    private String componentNodeId;

    private String interfaceNodeId;

    public CPDEditLabelTest() {
        super(DEFAULT_DOCUMENT, CPDDiagramDescriptionBuilder.CPD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(COMPONENT), //
                Arguments.of(CONSTRAINT), //
                Arguments.of(INTERFACE), //
                Arguments.of(MODEL),
                Arguments.of(OPERATION), //
                Arguments.of(PACKAGE), //
                Arguments.of(PORT), //
                Arguments.of(RECEPTION)); //
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getComponent()));
        Node componentNode = (Node) this.findGraphicalElementContentByLabel(COMPONENT);
        this.componentNodeId = componentNode.getId();
        this.applyNodeCreationTool(this.componentNodeId, new CreationTool(ToolSections.NODES, UML.getPort()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getConstraint()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getInterface()));
        Node interfaceNode = (Node) this.findGraphicalElementExcludingContentByLabel(INTERFACE);
        this.interfaceNodeId = interfaceNode.getId();
        this.applyNodeCreationTool(this.interfaceNodeId, new CreationTool(ToolSections.NODES, UML.getOperation()));
        this.applyNodeCreationTool(this.interfaceNodeId, new CreationTool(ToolSections.NODES, UML.getReception()));
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
    public void testEditLabel(String elementName) {
        String expectedGraphicalLabel = switch (elementName) {
            case COMPONENT -> COMPONENT_LABEL_PREFIX + NEW_LABEL;
            case CONSTRAINT -> NEW_LABEL + CONSTRAINT_LABEL_SUFFIX;
            case INTERFACE -> INTERFACE_LABEL_PREFIX + NEW_LABEL;
            case OPERATION -> OPERATION_LABEL_PREFIX + NEW_LABEL + OPERATION_LABEL_SUFFIX;
            case PORT -> PROPERTY_LABEL_PREFIX + NEW_LABEL + PROPERTY_LABEL_SUFFIX;
            case RECEPTION -> RECEPTION_LABEL_PREFIX + NEW_LABEL + OPERATION_LABEL_SUFFIX;
            default -> NEW_LABEL;
        };
        LabelGraphicalChecker graphicalChecker = new LabelGraphicalChecker(expectedGraphicalLabel);
        LabelSemanticChecker semanticChecker = new LabelSemanticChecker(this.getObjectService(), this::getEditingContext, NEW_LABEL);
        this.editLabel(elementName, NEW_LABEL, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @Test
    public void testEditLabelPropertyInComponent() {
        this.applyNodeCreationTool(this.componentNodeId, new CreationTool(ToolSections.NODES, UML.getProperty()));
        LabelGraphicalChecker graphicalChecker = new LabelGraphicalChecker(PROPERTY_LABEL_PREFIX + NEW_LABEL + PROPERTY_LABEL_SUFFIX);
        LabelSemanticChecker semanticChecker = new LabelSemanticChecker(this.getObjectService(), this::getEditingContext, NEW_LABEL);
        this.editLabel(PROPERTY, NEW_LABEL, new CombinedChecker(graphicalChecker, semanticChecker));
    }

    @Test
    public void testEditLabelPropertyInInterface() {
        this.applyNodeCreationTool(this.interfaceNodeId, new CreationTool(ToolSections.NODES, UML.getProperty()));
        LabelGraphicalChecker graphicalChecker = new LabelGraphicalChecker(PROPERTY_LABEL_PREFIX + NEW_LABEL + PROPERTY_LABEL_SUFFIX);
        LabelSemanticChecker semanticChecker = new LabelSemanticChecker(this.getObjectService(), this::getEditingContext, NEW_LABEL);
        this.editLabel(PROPERTY, NEW_LABEL, new CombinedChecker(graphicalChecker, semanticChecker));
    }

}

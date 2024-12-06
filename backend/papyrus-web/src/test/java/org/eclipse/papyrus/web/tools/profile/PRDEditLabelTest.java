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

import org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.LabelGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.LabelSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EditLabelTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edit label tools in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDEditLabelTest extends EditLabelTest {

    private static final String CLASS = "Class1";

    private static final String CONSTRAINT = "Constraint1";

    private static final String DATA_TYPE = "DataType1";

    private static final String ENUMERATION = "Enumeration1";

    private static final String ENUMERATION_LITERAL = "EnumerationLiteral1";

    private static final String OPERATION = "Operation1";

    private static final String PACKAGE = "Package1";

    private static final String PRIMITIVE_TYPE = "PrimitiveType1";

    private static final String PROFILE = "Profile1";

    private static final String PROPERTY = "Property1";

    private static final String STEREOTYPE = "Stereotype1";

    private static final String NEW_LABEL = "New Label";

    private static final String CONSTRAINT_LABEL_SUFFIX = System.lineSeparator() + "{{OCL} true}";

    private static final String DATA_TYPE_LABEL_PREFIX = UMLCharacters.ST_LEFT + "dataType" + UMLCharacters.ST_RIGHT + System.lineSeparator();

    private static final String ENUMERATION_LABEL_PREFIX = UMLCharacters.ST_LEFT + "enumeration" + UMLCharacters.ST_RIGHT + System.lineSeparator();

    private static final String OPERATION_LABEL_PREFIX = "+ ";

    private static final String OPERATION_LABEL_SUFFIX = "()";

    private static final String PRIMITIVE_TYPE_LABEL_PREFIX = UMLCharacters.ST_LEFT + "primitive" + UMLCharacters.ST_RIGHT + System.lineSeparator();

    private static final String PROFILE_LABEL_PREFIX = UMLCharacters.ST_LEFT + "profile" + UMLCharacters.ST_RIGHT + System.lineSeparator();

    private static final String PROPERTY_LABEL_PREFIX = "+ ";

    private static final String PROPERTY_LABEL_SUFFIX = ": <Undefined> [1]";

    private static final String STEREOTYPE_LABEL_PREFIX = UMLCharacters.ST_LEFT + "stereotype" + UMLCharacters.ST_RIGHT + System.lineSeparator();

    public PRDEditLabelTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(CLASS), //
                Arguments.of(CONSTRAINT), //
                Arguments.of(DATA_TYPE), //
                Arguments.of(ENUMERATION), //
                Arguments.of(ENUMERATION_LITERAL), //
                Arguments.of(OPERATION), //
                Arguments.of(PACKAGE), //
                Arguments.of(PRIMITIVE_TYPE), //
                Arguments.of(PROFILE), //
                Arguments.of(PROPERTY), //
                Arguments.of(STEREOTYPE)//
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getClass_()));
        Node classNode = (Node) this.findGraphicalElementExcludingContentByLabel(CLASS);
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getConstraint()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getDataType()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getEnumeration()));
        Node enumerationNode = (Node) this.findGraphicalElementExcludingContentByLabel(ENUMERATION);
        this.applyNodeCreationTool(enumerationNode.getId(), new CreationTool(ToolSections.NODES, UML.getEnumerationLiteral()));
        this.applyNodeCreationTool(classNode.getId(), new CreationTool(ToolSections.NODES, UML.getOperation()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getPrimitiveType()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getProfile()));
        this.applyNodeCreationTool(classNode.getId(), new CreationTool(ToolSections.NODES, UML.getProperty()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getStereotype()));
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
            case CONSTRAINT -> NEW_LABEL + CONSTRAINT_LABEL_SUFFIX;
            case DATA_TYPE -> DATA_TYPE_LABEL_PREFIX + NEW_LABEL;
            case ENUMERATION -> ENUMERATION_LABEL_PREFIX + NEW_LABEL;
            case OPERATION -> OPERATION_LABEL_PREFIX + NEW_LABEL + OPERATION_LABEL_SUFFIX;
            case PRIMITIVE_TYPE -> PRIMITIVE_TYPE_LABEL_PREFIX + NEW_LABEL;
            case PROFILE -> PROFILE_LABEL_PREFIX + NEW_LABEL;
            case PROPERTY -> PROPERTY_LABEL_PREFIX + NEW_LABEL + PROPERTY_LABEL_SUFFIX;
            case STEREOTYPE -> STEREOTYPE_LABEL_PREFIX + NEW_LABEL;
            default -> NEW_LABEL;
        };
        LabelGraphicalChecker graphicalChecker = new LabelGraphicalChecker(expectedGraphicalLabel);
        LabelSemanticChecker semanticChecker = new LabelSemanticChecker(this.getObjectService(), this::getEditingContext, NEW_LABEL);
        this.editLabel(elementName, NEW_LABEL, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

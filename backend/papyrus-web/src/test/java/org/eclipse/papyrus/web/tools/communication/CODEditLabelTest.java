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
package org.eclipse.papyrus.web.tools.communication;

import static org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters.ST_LEFT;
import static org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters.ST_RIGHT;

import java.util.stream.Stream;

import org.eclipse.papyrus.web.application.representations.uml.CODDiagramDescriptionBuilder;
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
 * Tests edit label tools in the Communication Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CODEditLabelTest extends EditLabelTest {

    private static final String ROOT_INTERACTION = "rootInteraction";

    private static final String LIFELINE = "Lifeline1";

    private static final String CONSTRAINT = "Constraint1";

    private static final String DURATION_OBSERVATION = "DurationObservation1";

    private static final String TIME_OBSERVATION = "TimeObservation1";

    private static final String NEW_LABEL = "New Label";

    private static final String CONSTRAINT_LABEL_SUFFIX = System.lineSeparator() + "{{OCL} true}";

    private static final String INTERACTION_LABEL_PREFIX = ST_LEFT + "interaction" + ST_RIGHT + System.lineSeparator();

    public CODEditLabelTest() {
        super(DEFAULT_DOCUMENT, CODDiagramDescriptionBuilder.COD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(ROOT_INTERACTION), //
                Arguments.of(LIFELINE), //
                Arguments.of(CONSTRAINT), //
                Arguments.of(DURATION_OBSERVATION), //
                Arguments.of(TIME_OBSERVATION));
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_INTERACTION, UML.getInteraction());
        Node rootContent = (Node) this.findGraphicalElementContentByLabel(ROOT_INTERACTION);
        String interactionId = rootContent.getId();
        this.applyNodeCreationTool(interactionId, new CreationTool(ToolSections.NODES, UML.getLifeline()));
        this.applyNodeCreationTool(interactionId, new CreationTool(ToolSections.NODES, UML.getConstraint()));
        this.applyNodeCreationTool(interactionId, new CreationTool(ToolSections.NODES, UML.getDurationObservation()));
        this.applyNodeCreationTool(interactionId, new CreationTool(ToolSections.NODES, UML.getTimeObservation()));
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
            case ROOT_INTERACTION -> INTERACTION_LABEL_PREFIX + NEW_LABEL;
            default -> NEW_LABEL;
        };
        LabelGraphicalChecker graphicalChecker = new LabelGraphicalChecker(expectedGraphicalLabel);
        LabelSemanticChecker semanticChecker = new LabelSemanticChecker(this.getObjectService(), this::getEditingContext, NEW_LABEL);
        this.editLabel(elementName, NEW_LABEL, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo.
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
 *****************************************************************************/
package org.eclipse.papyrus.web.tools.usecase;

import java.util.stream.Stream;

import org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters;
import org.eclipse.papyrus.web.application.representations.uml.UCDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.LabelGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.LabelSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EditLabelTest;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDCreationTool;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edit label tools in the Use Case Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class UCDEditLabelTest extends EditLabelTest {

    private static final String ACTIVITY = "Activity1";

    private static final String ACTOR = "Actor1";

    private static final String CLASS = "Class1";

    private static final String COMPONENT = "Component1";

    private static final String INTERACTION = "Interaction1";

    private static final String PACKAGE = "Package1";

    private static final String STATE_MACHINE = "StateMachine1";

    private static final String USE_CASE = "UseCase1";

    private static final String NEW_LABEL = "New Label";

    private static final String ACTIVITY_LABEL_PREFIX = UMLCharacters.ST_LEFT + "activity" + UMLCharacters.ST_RIGHT + UMLCharacters.EOL;

    private static final String COMPONENT_LABEL_PREFIX = UMLCharacters.ST_LEFT + "component" + UMLCharacters.ST_RIGHT + UMLCharacters.EOL;

    private static final String INTERACTION_LABEL_PREFIX = UMLCharacters.ST_LEFT + "interaction" + UMLCharacters.ST_RIGHT + UMLCharacters.EOL;

    private static final String STATE_MACHINE_LABEL_PREFIX = UMLCharacters.ST_LEFT + "stateMachine" + UMLCharacters.ST_RIGHT + UMLCharacters.EOL;

    public UCDEditLabelTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(ACTIVITY), //
                Arguments.of(ACTOR), //
                Arguments.of(CLASS), //
                Arguments.of(COMPONENT), //
                Arguments.of(INTERACTION), //
                Arguments.of(PACKAGE), //
                Arguments.of(STATE_MACHINE), //
                Arguments.of(USE_CASE)//
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getActor()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()));
        this.applyNodeCreationTool(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testEditLabel(String elementName) {
        String expectedGraphicalLabel = NEW_LABEL;
        if (ACTIVITY.equals(elementName)) {
            expectedGraphicalLabel = ACTIVITY_LABEL_PREFIX + NEW_LABEL;
        } else if (COMPONENT.equals(elementName)) {
            expectedGraphicalLabel = COMPONENT_LABEL_PREFIX + NEW_LABEL;
        } else if (INTERACTION.equals(elementName)) {
            expectedGraphicalLabel = INTERACTION_LABEL_PREFIX + NEW_LABEL;
        } else if (STATE_MACHINE.equals(elementName)) {
            expectedGraphicalLabel = STATE_MACHINE_LABEL_PREFIX + NEW_LABEL;
        }
        LabelGraphicalChecker graphicalChecker = new LabelGraphicalChecker(expectedGraphicalLabel);
        LabelSemanticChecker semanticChecker = new LabelSemanticChecker(this.getObjectSearchService(),
                this::getEditingContext, NEW_LABEL);
        this.editLabel(elementName, NEW_LABEL, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

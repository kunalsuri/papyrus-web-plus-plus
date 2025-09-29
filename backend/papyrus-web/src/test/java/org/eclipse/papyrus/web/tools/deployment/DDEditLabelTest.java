/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.tools.deployment;

import static org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters.ST_LEFT;
import static org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters.ST_RIGHT;

import java.util.stream.Stream;

import org.eclipse.papyrus.web.application.representations.uml.DDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.LabelGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.LabelSemanticChecker;
import org.eclipse.papyrus.web.tools.test.EditLabelTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edit label tools in the Deployment Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class DDEditLabelTest extends EditLabelTest {

    private static final String NEW_LABEL = "New Label";

    private static final String ARTIFACT = "Artifact1";

    private static final String CONSTRAINT = "Constraint1";

    private static final String DEPLOYMENT_SPECIFICATION = "DeploymentSpecification1";

    private static final String DEVICE = "Device1";

    private static final String EXECUTION_ENVIRONMENT = "ExecutionEnvironment1";

    private static final String MODEL = "Model1";

    private static final String NODE = "Node1";

    private static final String PACKAGE = "Package1";

    private static final String ARTIFACT_LABEL_PREFIX = ST_LEFT + "artifact" + ST_RIGHT + System.lineSeparator();

    private static final String CONSTRAINT_LABEL_SUFFIX = System.lineSeparator() + "{{OCL} true}";

    private static final String DEPLOYMENT_SPECIFICATION_LABEL_PREFIX = ST_LEFT + "deployment spec" + ST_RIGHT + System.lineSeparator();

    private static final String DEVICE_LABEL_PREFIX = ST_LEFT + "device" + ST_RIGHT + System.lineSeparator();

    private static final String EXECUTION_ENVIRONMENT_LABEL_PREFIX = ST_LEFT + "executionEnvironment" + ST_RIGHT + System.lineSeparator();

    public DDEditLabelTest() {
        super(DEFAULT_DOCUMENT, DDDiagramDescriptionBuilder.DD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> parameterProvider() {
        return Stream.of(//
                Arguments.of(ARTIFACT), //
                Arguments.of(CONSTRAINT), //
                Arguments.of(DEPLOYMENT_SPECIFICATION), //
                Arguments.of(DEVICE), //
                Arguments.of(EXECUTION_ENVIRONMENT), //
                Arguments.of(MODEL), //
                Arguments.of(NODE), //
                Arguments.of(PACKAGE) //
        );
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getArtifact()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getConstraint()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getDeploymentSpecification()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getDevice()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getExecutionEnvironment()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getModel()));
        this.applyNodeCreationTool(this.representationId, new CreationTool(ToolSections.NODES, UML.getNode()));
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
            case ARTIFACT -> ARTIFACT_LABEL_PREFIX + NEW_LABEL;
            case CONSTRAINT -> NEW_LABEL + CONSTRAINT_LABEL_SUFFIX;
            case DEPLOYMENT_SPECIFICATION -> DEPLOYMENT_SPECIFICATION_LABEL_PREFIX + NEW_LABEL;
            case DEVICE -> DEVICE_LABEL_PREFIX + NEW_LABEL;
            case EXECUTION_ENVIRONMENT -> EXECUTION_ENVIRONMENT_LABEL_PREFIX + NEW_LABEL;
            default -> NEW_LABEL;
        };
        LabelGraphicalChecker graphicalChecker = new LabelGraphicalChecker(expectedGraphicalLabel);
        LabelSemanticChecker semanticChecker = new LabelSemanticChecker(this.getObjectSearchService(),
                this::getEditingContext, NEW_LABEL);
        this.editLabel(elementName, NEW_LABEL, new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

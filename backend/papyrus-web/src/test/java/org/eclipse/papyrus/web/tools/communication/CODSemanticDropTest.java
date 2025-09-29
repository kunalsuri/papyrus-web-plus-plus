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

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.CODDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.communication.utils.CODMappingTypes;
import org.eclipse.papyrus.web.tools.test.SemanticDropTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests semantic drop tools in the Communication Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
public class CODSemanticDropTest extends SemanticDropTest {

    private static final String ROOT_INTERACTION = "rootInteraction";

    public CODSemanticDropTest() {
        super(DEFAULT_DOCUMENT, CODDiagramDescriptionBuilder.COD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> dropOnDiagramInteractionParameters() {
        return Stream.of(//
                Arguments.of(UML.getInteraction_Lifeline(), UML.getLifeline()), //
                Arguments.of(UML.getElement_OwnedComment(), UML.getComment()), //
                Arguments.of(UML.getNamespace_OwnedRule(), UML.getConstraint()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getDurationObservation()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getTimeObservation()));
    }

    private static Stream<Arguments> dropMessageParameters() {
        return Stream.of(//
                Arguments.of(new CreationTool(ToolSections.NODES, UML.getLifeline()), new CreationTool(ToolSections.NODES, UML.getLifeline())));
    }

    @Override
    @BeforeEach
    public void setUp() {
        this.setUpWithIntermediateRoot(ROOT_INTERACTION, UML.getInteraction());
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("dropOnDiagramInteractionParameters")
    public void testSemanticDropOnDiagram(EReference containmentReference, EClass elementType) {
        EObject elementToDrop;
        if (UML.getDurationObservation().isSuperTypeOf(elementType) || UML.getTimeObservation().isSuperTypeOf(elementType)) {
            elementToDrop = this.createSemanticElement(this.getRootSemanticElement(), containmentReference, elementType, elementType.getName());
        } else {
            elementToDrop = this.createSemanticElement(this.findSemanticElementByName(ROOT_INTERACTION), containmentReference, elementType, elementType.getName());
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(ROOT_INTERACTION),
                CODMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        this.semanticDropOnContent(ROOT_INTERACTION, this.getIdentityService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropMessageParameters")
    public void testSemanticDropMessage(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnContainers(sourceCreationTool, ROOT_INTERACTION, targetCreationTool, ROOT_INTERACTION, new CreationTool(ToolSections.EDGES, UML.getMessage()), ROOT_INTERACTION,
                CODMappingTypes.getMappingType(UML.getMessage()));
    }

}

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
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.CombinedChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.EdgeCreationSemanticChecker;
import org.eclipse.papyrus.web.tools.profile.utils.PRDMappingTypes;
import org.eclipse.papyrus.web.tools.test.EdgeCreationTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.diagrams.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests edge creation tools in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDSubNodeEdgeCreationTest extends EdgeCreationTest {

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final String CLASS_SOURCE = "ClassSource";

    private static final String CLASS_TARGET = "ClassTarget";

    private static final String DATA_TYPE_SOURCE = "DataTypeSource";

    private static final String DATA_TYPE_TARGET = "DataTypeTarget";

    private static final String ENUMERATION_SOURCE = "EnumerationSource";

    private static final String ENUMERATION_TARGET = "EnumerationTarget";

    private static final String PRIMITIVE_TYPE_SOURCE = "PrimitiveTypeSource";

    private static final String PRIMITIVE_TYPE_TARGET = "PrimitiveTypeTarget";

    private static final String STEREOTYPE_SOURCE = "StereotypeSource";

    private static final String STEREOTYPE_TARGET = "StereotypeTarget";

    private String packageContainerId;

    public PRDSubNodeEdgeCreationTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> associationAndGeneralizationParameters() {
        List<String> sources = List.of(CLASS_SOURCE, DATA_TYPE_SOURCE, ENUMERATION_SOURCE, PRIMITIVE_TYPE_SOURCE, STEREOTYPE_SOURCE);
        List<String> targets = List.of(CLASS_TARGET, DATA_TYPE_TARGET, ENUMERATION_TARGET, PRIMITIVE_TYPE_TARGET, STEREOTYPE_TARGET);
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> extensionParameters() {
        List<String> sources = List.of(STEREOTYPE_SOURCE);
        List<String> targets = List.of(CLASS_TARGET, STEREOTYPE_TARGET);
        return cartesianProduct(sources, targets);
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
        Node packageContainerNode = (Node) this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER);
        this.packageContainerId = packageContainerNode.getId();
        // No need to create constraint, package, and profile: these elements aren't sources or targets of PRD edges.
        this.createSourceAndTargetNodes(this.packageContainerId, new CreationTool(ToolSections.NODES, UML.getClass_()));
        this.createSourceAndTargetNodes(this.packageContainerId, new CreationTool(ToolSections.NODES, UML.getDataType()));
        this.createSourceAndTargetNodes(this.packageContainerId, new CreationTool(ToolSections.NODES, UML.getEnumeration()));
        this.createSourceAndTargetNodes(this.packageContainerId, new CreationTool(ToolSections.NODES, UML.getPrimitiveType()));
        this.createSourceAndTargetNodes(this.packageContainerId, new CreationTool(ToolSections.NODES, UML.getStereotype()));
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testCreateAssociation(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getAssociation());
    }

    @ParameterizedTest
    @MethodSource("associationAndGeneralizationParameters")
    public void testCreateGeneralization(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getGeneralization(), sourceElementLabel, UML.getClassifier_Generalization());
    }

    @ParameterizedTest
    @MethodSource("extensionParameters")
    public void testCreateExtension(String sourceElementLabel, String targetElementLabel) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, UML.getExtension(), PACKAGE_CONTAINER, UML.getPackage_PackagedElement());
    }

    private void testCreateEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType) {
        this.testCreateEdge(sourceElementLabel, targetElementLabel, edgeType, null, UML.getPackage_PackagedElement());
    }

    private void testCreateEdge(String sourceElementLabel, String targetElementLabel, EClass edgeType, String expectedSemanticOwnerName, EReference expectedContainmentReference) {
        Supplier<EObject> expectedSemanticOwnerSupplier;
        if (expectedSemanticOwnerName == null) {
            expectedSemanticOwnerSupplier = () -> this.getRootSemanticElement();
        } else {
            expectedSemanticOwnerSupplier = () -> this.findSemanticElementByName(expectedSemanticOwnerName);
        }
        EdgeCreationGraphicalChecker graphicalChecker = new EdgeCreationGraphicalChecker(this::getDiagram, null, PRDMappingTypes.getMappingType(edgeType), this.getCapturedEdges());
        EdgeCreationSemanticChecker semanticChecker = new EdgeCreationSemanticChecker(this.getObjectService(), this::getEditingContext, edgeType, expectedSemanticOwnerSupplier,
                expectedContainmentReference);
        this.createEdge(sourceElementLabel, targetElementLabel, new CreationTool(ToolSections.EDGES, edgeType), new CombinedChecker(graphicalChecker, semanticChecker));
    }
}

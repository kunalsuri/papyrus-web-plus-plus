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
package org.eclipse.papyrus.web.tools.usecase;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.papyrus.web.application.representations.uml.UCDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.test.SemanticDropTest;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDCreationTool;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDMappingTypes;
import org.eclipse.papyrus.web.tools.usecase.utils.UCDToolSections;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests semantic drop tools in the Use Case Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class UCDSemanticDropTest extends SemanticDropTest {

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final String CLASS_CONTAINER = "ClassContainer";

    private static final String DROP_SUFFIX = "Drop";

    public UCDSemanticDropTest() {
        super(DEFAULT_DOCUMENT, UCDDiagramDescriptionBuilder.UCD_REP_NAME, UML.getModel());
    }

    private static Stream<Arguments> dropOnDiagramAndPackageParameters() {
        return Stream.of(//
                Arguments.of(UML.getPackage_PackagedElement(), UML.getActor()), //
                Arguments.of(UML.getElement_OwnedComment(), UML.getComment()), //
                Arguments.of(UML.getNamespace_OwnedRule(), UML.getConstraint()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getPackage()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getActivity()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getClass_()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getComponent()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getInteraction()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getStateMachine()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getUseCase())//
        );
    }

    private static Stream<Arguments> dropOnClassParameters() {
        return Stream.of(//
                Arguments.of(UML.getElement_OwnedComment(), UML.getComment()), //
                Arguments.of(UML.getNamespace_OwnedRule(), UML.getConstraint()), //
                Arguments.of(UML.getClassifier_OwnedUseCase(), UML.getUseCase())//
        );
    }

    private static Stream<Arguments> dropAbstractionAndDependencyAndRealizationAndUsageParameters() {
        List<CreationTool> sources = List.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()));
        List<CreationTool> targets = List.of(
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()),
                new UCDCreationTool(UCDToolSections.NODES, UML.getActor()),
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()),
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()),
                new UCDCreationTool(UCDToolSections.NODES, UML.getConstraint()),
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()),
                new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()),
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()),
                new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()));
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> dropAssociationAndGeneralizationParameters() {
        List<CreationTool> sources = List.of(new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()));
        List<CreationTool> targets = List.of(
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getActivity()),
                new UCDCreationTool(UCDToolSections.NODES, UML.getActor()),
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()),
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getComponent()),
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getInteraction()),
                new UCDCreationTool(UCDToolSections.SUBJECT, UML.getStateMachine()),
                new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()));
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> dropExtendAndIncludeParameters() {
        return Stream.of(Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase()), new UCDCreationTool(UCDToolSections.NODES, UML.getUseCase())));
    }

    private static Stream<Arguments> dropPackageImportAndPackageMergeParameters() {
        return Stream.of(Arguments.of(new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()), new UCDCreationTool(UCDToolSections.NODES, UML.getPackage())));
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

    @ParameterizedTest
    @MethodSource("dropOnDiagramAndPackageParameters")
    public void testSemanticDropOnDiagram(EReference containmentReference, EClass elementType) {
        EObject elementToDrop = this.createSemanticElement(this.getRootSemanticElement(), containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker = switch (elementType.getName()) {
            case "Activity", "Class", "Component", "Interaction", "Package", "StateMachine" -> new HolderCreationGraphicalChecker(this::getDiagram, null, UCDMappingTypes.getMappingType(elementType),
                    this.getCapturedNodes());
            default -> new NodeCreationGraphicalChecker(this::getDiagram, null, UCDMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        };

        this.semanticDropOnDiagram(this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnDiagramAndPackageParameters")
    public void testSemanticDropOnPackage(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);
        EObject parentElement = this.findSemanticElementByName(PACKAGE_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker = switch (elementType.getName()) {
            case "Activity", "Class", "Component", "Interaction", "Package", "StateMachine" -> new HolderCreationGraphicalChecker(this::getDiagram,
                    () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                    UCDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
            default -> new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                    UCDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        };
        this.semanticDropOnContent(PACKAGE_CONTAINER, this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnClassParameters")
    public void testSemanticDropOnClass(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new UCDCreationTool(UCDToolSections.SUBJECT, UML.getClass_()), CLASS_CONTAINER);
        EObject parentElement = this.findSemanticElementByName(CLASS_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(CLASS_CONTAINER),
                UCDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        this.semanticDropOnContent(CLASS_CONTAINER, this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropAbstractionAndDependencyAndRealizationAndUsageParameters")
    public void testSemanticDropAbstraction(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getAbstraction()), UCDMappingTypes.getMappingType(UML.getAbstraction()));
    }

    @ParameterizedTest
    @MethodSource("dropAssociationAndGeneralizationParameters")
    public void testSemanticDropAssociation(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getAssociation()), UCDMappingTypes.getMappingType(UML.getAssociation()));
    }

    @ParameterizedTest
    @MethodSource("dropAbstractionAndDependencyAndRealizationAndUsageParameters")
    public void testSemanticDropDependency(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getDependency()), UCDMappingTypes.getMappingType(UML.getDependency()));
    }

    @ParameterizedTest
    @MethodSource("dropExtendAndIncludeParameters")
    public void testSemanticDropExtend(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getExtend()), UCDMappingTypes.getMappingType(UML.getExtend()));
    }

    @ParameterizedTest
    @MethodSource("dropAssociationAndGeneralizationParameters")
    public void testSemanticDropGeneralization(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getGeneralization()), UCDMappingTypes.getMappingType(UML.getGeneralization()));
    }

    @ParameterizedTest
    @MethodSource("dropExtendAndIncludeParameters")
    public void testSemanticDropInclude(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getInclude()), UCDMappingTypes.getMappingType(UML.getInclude()));
    }

    @ParameterizedTest
    @MethodSource("dropPackageImportAndPackageMergeParameters")
    public void testSemanticDropPackageImport(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getPackageImport()), UCDMappingTypes.getMappingType(UML.getPackageImport()));
    }

    @ParameterizedTest
    @MethodSource("dropPackageImportAndPackageMergeParameters")
    public void testSemanticDropPackageMerge(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getPackageMerge()), UCDMappingTypes.getMappingType(UML.getPackageMerge()));
    }

    @ParameterizedTest
    @MethodSource("dropAbstractionAndDependencyAndRealizationAndUsageParameters")
    public void testSemanticDropRealization(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getRealization()), UCDMappingTypes.getMappingType(UML.getRealization()));
    }

    @ParameterizedTest
    @MethodSource("dropAbstractionAndDependencyAndRealizationAndUsageParameters")
    public void testSemanticDropUsage(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getUsage()), UCDMappingTypes.getMappingType(UML.getUsage()));
    }

}

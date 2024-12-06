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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.web.application.representations.uml.PRDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.tools.checker.HolderCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.checker.NodeCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDClassifierCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.profile.checker.PRDEnumerationCreationGraphicalChecker;
import org.eclipse.papyrus.web.tools.profile.utils.PRDMappingTypes;
import org.eclipse.papyrus.web.tools.test.SemanticDropTest;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.tools.utils.ToolSections;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests semantic drop tools in the Profile Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public class PRDSemanticDropTest extends SemanticDropTest {

    private static final String PACKAGE_CONTAINER = "PackageContainer";

    private static final String PROFILE_CONTAINER = "ProfileContainer";

    private static final String CLASS_CONTAINER = "ClassContainer";

    private static final String CLASS_ATTRIBUTE_COMPARTMENT = "PRD_Class_Attributes_SHARED_CompartmentNode";

    private static final String CLASS_OPERATION_COMPARTMENT = "PRD_Class_Operations_SHARED_CompartmentNode";

    private static final String DATA_TYPE_CONTAINER = "DataTypeContainer";

    private static final String DATA_TYPE_ATTRIBUTE_COMPARTMENT = "PRD_DataType_Attributes_SHARED_CompartmentNode";

    private static final String DATA_TYPE_OPERATION_COMPARTMENT = "PRD_DataType_Operations_SHARED_CompartmentNode";

    private static final String ENUMERATION_CONTAINER = "EnumerationContainer";

    private static final String ENUMERATION_LITERAL_COMPARTMENT = "PRD_Enumeration_Literals_SHARED_CompartmentNode";

    private static final String STEREOTYPE_CONTAINER = "StereotypeContainer";

    private static final String STEREOTYPE_ATTRIBUTE_COMPARTMENT = "PRD_Stereotype_Attributes_SHARED_CompartmentNode";

    private static final String STEREOTYPE_OPERATION_COMPARTMENT = "PRD_Stereotype_Operations_SHARED_CompartmentNode";

    private static final String DROP_SUFFIX = "Drop";

    public PRDSemanticDropTest() {
        super("test.profile.uml", PRDDiagramDescriptionBuilder.PRD_REP_NAME, UML.getProfile());
    }

    private static Stream<Arguments> dropOnDiagramAndPackageAndProfileParameters() {
        return Stream.of(//
                Arguments.of(UML.getPackage_PackagedElement(), UML.getClass_()), //
                Arguments.of(UML.getElement_OwnedComment(), UML.getComment()), //
                Arguments.of(UML.getNamespace_OwnedRule(), UML.getConstraint()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getDataType()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getEnumeration()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getPackage()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getPrimitiveType()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getProfile()), //
                Arguments.of(UML.getPackage_PackagedElement(), UML.getStereotype())//
        );
    }

    private static Stream<Arguments> dropOnClassAndStereotypeParameters() {
        return Stream.of(//
                Arguments.of(UML.getClass_OwnedOperation(), UML.getOperation()), //
                Arguments.of(UML.getStructuredClassifier_OwnedAttribute(), UML.getProperty()) //
        );
    }

    private static Stream<Arguments> dropOnDataTypeParameters() {
        return Stream.of(//
                Arguments.of(UML.getDataType_OwnedOperation(), UML.getOperation()), //
                Arguments.of(UML.getDataType_OwnedAttribute(), UML.getProperty())//
        );
    }

    private static Stream<Arguments> dropOnEnumerationParameters() {
        return Stream.of(//
                Arguments.of(UML.getEnumeration_OwnedLiteral(), UML.getEnumerationLiteral())//
        );
    }

    private static Stream<Arguments> dropAssociationAndGeneralizationParameters() {
        List<CreationTool> sources = List.of(new CreationTool(ToolSections.NODES, UML.getClass_()));
        List<CreationTool> targets = List.of(
                new CreationTool(ToolSections.NODES, UML.getClass_()),
                new CreationTool(ToolSections.NODES, UML.getDataType()),
                new CreationTool(ToolSections.NODES, UML.getEnumeration()),
                new CreationTool(ToolSections.NODES, UML.getPrimitiveType()),
                new CreationTool(ToolSections.NODES, UML.getStereotype()));
        return cartesianProduct(sources, targets);
    }

    private static Stream<Arguments> dropExtensionParameters() {
        List<CreationTool> sources = List.of(new CreationTool(ToolSections.NODES, UML.getStereotype()));
        List<CreationTool> targets = List.of(
                new CreationTool(ToolSections.NODES, UML.getClass_()),
                new CreationTool(ToolSections.NODES, UML.getStereotype()));
        return cartesianProduct(sources, targets);
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
    @MethodSource("dropOnDiagramAndPackageAndProfileParameters")
    public void testSemanticDropOnDiagram(EReference containmentReference, EClass elementType) {
        EObject elementToDrop = this.createSemanticElement(this.getRootSemanticElement(), containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker;
        if (UML.getEnumeration().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDEnumerationCreationGraphicalChecker(this::getDiagram, null, PRDMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        } else if (UML.getClassifier().isSuperTypeOf(elementType) && !UML.getPrimitiveType().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDClassifierCreationGraphicalChecker(this::getDiagram, null, PRDMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        } else if (UML.getPackage().isSuperTypeOf(elementType) || UML.getProfile().isSuperTypeOf(elementType)) {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, null, PRDMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        } else {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, null, PRDMappingTypes.getMappingType(elementType), this.getCapturedNodes());
        }
        this.semanticDropOnDiagram(this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @Test
    public void testSemanticDropMetaclassOnDiagram() {
        ElementImport elementToDrop = (ElementImport) this.createSemanticElement(this.getRootSemanticElement(), UML.getNamespace_ElementImport(), UML.getElementImport(), "");
        String elementToDropId = this.getObjectService().getId(elementToDrop);
        IEMFEditingContext editingContext = (IEMFEditingContext) this.getEditingContext();
        // Reload the elementToDrop from the current editing context.
        Optional<Object> optObject = this.getObjectService().getObject(editingContext, elementToDropId);
        assertThat(optObject).isPresent();
        assertThat(optObject.get()).isInstanceOf(EObject.class);
        elementToDrop = (ElementImport) optObject.get();
        Class testMetaclass = this.getTestMetaclass(editingContext);
        elementToDrop.setImportedElement(testMetaclass);
        this.persistenceService.persist(new ICause.NoOp(), editingContext);

        // Dispose the editing context event processor to force a reload of the persisted editing context.
        // This implies to re-create the event subscription runner, which has been deleted when disposing the editing
        // context event processor.
        this.editingContextEventProcessorRegistry.disposeEditingContextEventProcessor(editingContext.getId());
        this.diagramEventSubscriptionRunner.createSubscription(this.editingContextId, this.representationId);

        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, null, PRDMappingTypes.PRD_METACLASS, this.getCapturedNodes());
        this.semanticDropOnDiagram(this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnDiagramAndPackageAndProfileParameters")
    public void testSemanticDropOnPackage(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getPackage()), PACKAGE_CONTAINER);

        EObject parentElement = this.findSemanticElementByName(PACKAGE_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker;
        if (UML.getEnumeration().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDEnumerationCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                    PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else if (UML.getClassifier().isSuperTypeOf(elementType) && !UML.getPrimitiveType().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDClassifierCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                    PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else if (UML.getPackage().isSuperTypeOf(elementType) || UML.getProfile().isSuperTypeOf(elementType)) {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                    PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PACKAGE_CONTAINER),
                    PRDMappingTypes.getMappingTypeAsSubNode(elementType),
                    this.getCapturedNodes());
        }
        this.semanticDropOnContent(PACKAGE_CONTAINER, this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnDiagramAndPackageAndProfileParameters")
    public void testSemanticDropOnProfile(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getProfile()), PROFILE_CONTAINER);

        EObject parentElement = this.findSemanticElementByName(PROFILE_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        NodeCreationGraphicalChecker graphicalChecker;
        if (UML.getEnumeration().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDEnumerationCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER),
                    PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else if (UML.getClassifier().isSuperTypeOf(elementType) && !UML.getPrimitiveType().isSuperTypeOf(elementType)) {
            graphicalChecker = new PRDClassifierCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER),
                    PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else if (UML.getPackage().isSuperTypeOf(elementType) || UML.getProfile().isSuperTypeOf(elementType)) {
            graphicalChecker = new HolderCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER),
                    PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        } else {
            graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER),
                    PRDMappingTypes.getMappingTypeAsSubNode(elementType),
                    this.getCapturedNodes());
        }
        this.semanticDropOnContent(PROFILE_CONTAINER, this.getObjectService().getId(elementToDrop), graphicalChecker);

    }

    @Test
    public void testSemanticDropMetaclassOnProfile() {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getProfile()), PROFILE_CONTAINER);
        ElementImport elementToDrop = (ElementImport) this.createSemanticElement(this.findSemanticElementByName(PROFILE_CONTAINER), UML.getNamespace_ElementImport(), UML.getElementImport(), "");
        String elementToDropId = this.getObjectService().getId(elementToDrop);
        EditingContext editingContext = (EditingContext) this.getEditingContext();
        // Reload the elementToDrop from the current editing context.
        Optional<Object> optObject = this.getObjectService().getObject(editingContext, elementToDropId);
        assertThat(optObject).isPresent();
        assertThat(optObject.get()).isInstanceOf(EObject.class);
        elementToDrop = (ElementImport) optObject.get();
        Class testMetaclass = this.getTestMetaclass(editingContext);
        elementToDrop.setImportedElement(testMetaclass);
        this.persistenceService.persist(new ICause.NoOp(), editingContext);

        // Dispose the editing context event processor to force a reload of the persisted editing context.
        // This implies to re-create the event subscription runner, which has been deleted when disposing the editing
        // context event processor.
        this.editingContextEventProcessorRegistry.disposeEditingContextEventProcessor(editingContext.getId());
        this.diagramEventSubscriptionRunner.createSubscription(this.editingContextId, this.representationId);

        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.findGraphicalElementContentByLabel(PROFILE_CONTAINER),
                PRDMappingTypes.PRD_METACLASS_SHARED, this.getCapturedNodes());
        this.semanticDropOnContent(PROFILE_CONTAINER, this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnClassAndStereotypeParameters")
    public void testSemanticDropOnClass(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getClass_()), CLASS_CONTAINER);
        EObject parentElement = this.findSemanticElementByName(CLASS_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        String compartmentName;
        if (UML.getOperation().isSuperTypeOf(elementType)) {
            compartmentName = CLASS_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(elementType)) {
            compartmentName = CLASS_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentName = null;
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(CLASS_CONTAINER, compartmentName),
                PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        this.semanticDropOnContentCompartment(CLASS_CONTAINER, compartmentName, this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnClassAndStereotypeParameters")
    public void testSemanticDropOnStereotype(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getStereotype()), STEREOTYPE_CONTAINER);
        EObject parentElement = this.findSemanticElementByName(STEREOTYPE_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        String compartmentName;
        if (UML.getOperation().isSuperTypeOf(elementType)) {
            compartmentName = STEREOTYPE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(elementType)) {
            compartmentName = STEREOTYPE_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentName = null;
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(STEREOTYPE_CONTAINER, compartmentName),
                PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        this.semanticDropOnContentCompartment(STEREOTYPE_CONTAINER, compartmentName, this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnDataTypeParameters")
    public void testSemanticDropOnDataType(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getDataType()), DATA_TYPE_CONTAINER);
        EObject parentElement = this.findSemanticElementByName(DATA_TYPE_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        String compartmentName;
        if (UML.getOperation().isSuperTypeOf(elementType)) {
            compartmentName = DATA_TYPE_OPERATION_COMPARTMENT;
        } else if (UML.getProperty().isSuperTypeOf(elementType)) {
            compartmentName = DATA_TYPE_ATTRIBUTE_COMPARTMENT;
        } else {
            compartmentName = null;
        }
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(DATA_TYPE_CONTAINER, compartmentName),
                PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        this.semanticDropOnContentCompartment(DATA_TYPE_CONTAINER, compartmentName, this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropOnEnumerationParameters")
    public void testSemanticDropOnEnumeration(EReference containmentReference, EClass elementType) {
        this.createNodeWithLabel(this.representationId, new CreationTool(ToolSections.NODES, UML.getEnumeration()), ENUMERATION_CONTAINER);
        EObject parentElement = this.findSemanticElementByName(ENUMERATION_CONTAINER);
        EObject elementToDrop = this.createSemanticElement(parentElement, containmentReference, elementType, elementType.getName() + DROP_SUFFIX);
        String compartmentName = ENUMERATION_LITERAL_COMPARTMENT;
        NodeCreationGraphicalChecker graphicalChecker = new NodeCreationGraphicalChecker(this::getDiagram, () -> this.getSubNode(ENUMERATION_CONTAINER, compartmentName),
                PRDMappingTypes.getMappingTypeAsSubNode(elementType), this.getCapturedNodes());
        this.semanticDropOnContentCompartment(ENUMERATION_CONTAINER, compartmentName, this.getObjectService().getId(elementToDrop), graphicalChecker);
    }

    @ParameterizedTest
    @MethodSource("dropAssociationAndGeneralizationParameters")
    public void testSemanticDropAssociation(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getAssociation()), PRDMappingTypes.getMappingType(UML.getAssociation()));
    }

    @ParameterizedTest
    @MethodSource("dropAssociationAndGeneralizationParameters")
    public void testSemanticDropGeneralization(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getGeneralization()), PRDMappingTypes.getMappingType(UML.getGeneralization()));
    }

    @ParameterizedTest
    @MethodSource("dropExtensionParameters")
    public void testSemanticDropExtension(CreationTool sourceCreationTool, CreationTool targetCreationTool) {
        this.edgeSemanticDropOnDiagram(sourceCreationTool, targetCreationTool, new CreationTool(ToolSections.EDGES, UML.getExtension()), PRDMappingTypes.getMappingType(UML.getExtension()));
    }

    private Class getTestMetaclass(IEMFEditingContext editingContext) {
        Resource umlMetamodelResource = editingContext.getDomain().getResourceSet().getResource(URI.createURI(UMLResource.UML_METAMODEL_URI), true);
        Package umlPackage = (Package) EcoreUtil.getObjectByType(umlMetamodelResource.getContents(), UMLPackage.eINSTANCE.getPackage());
        Class activityMetaclass = umlPackage.getOwnedTypes().stream() //
                .filter(Class.class::isInstance) //
                .map(Class.class::cast) //
                .filter(metaClass -> Objects.equals(metaClass.getName(), "Activity")) //
                .findFirst().get();
        return activityMetaclass;
    }
}

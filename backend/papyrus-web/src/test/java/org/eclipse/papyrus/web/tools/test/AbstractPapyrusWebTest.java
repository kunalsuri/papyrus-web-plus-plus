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
package org.eclipse.papyrus.web.tools.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.papyrus.web.application.representations.PapyrusRepresentationDescriptionRegistry;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.application.templates.documents.UMLStereotypeProvider;
import org.eclipse.papyrus.web.application.templates.projects.PapyrusUMLNatures;
import org.eclipse.papyrus.web.tools.configuration.ProjectInitializerInput;
import org.eclipse.papyrus.web.tools.utils.CreationTool;
import org.eclipse.papyrus.web.utils.AbstractWebUMLTest;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateChildMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateDocumentMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateProjectMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateRepresentationMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateRootObjectCreateMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusDeleteFromDiagramMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusDiagramEventSubscriptionRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusDropNodeMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusDropOnDiagramMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusEditLabelMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusInvokeSingleClickOnDiagramElementToolRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusInvokeSingleClickOnTwoDiagramElementsToolRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusPaletteToolQueryRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusReconnectEdgeMutationRunner;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.collaborative.editingcontext.EditingContextEventProcessorRegistry;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextPersistenceService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.components.representations.IRepresentation;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.web.domain.boundedcontexts.project.repositories.IProjectRepository;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.services.api.IProjectSemanticDataSearchService;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Abstract class to define integration tests of tools in Papyrus Web.
 * <p>
 * This class defined high-level utility methods that can be reused in concrete Papyrus Web tests to apply tools and
 * search graphical/semantic elements. It also performs a minimal environment initialization (creation of a project,
 * root element, initialization of a diagram).
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractPapyrusWebTest extends AbstractWebUMLTest {

    protected static final String DEFAULT_DOCUMENT = "test.uml";

    protected static final UMLPackage UML = UMLPackage.eINSTANCE;

    protected String editingContextId;

    protected String rootObjectId;

    protected String intermediateRootObjectId;

    protected String representationId;

    protected String documentId;

    protected String representationName;

    protected EClass rootElementEClass;

    protected String documentName;

    @Autowired
    protected EditingContextEventProcessorRegistry editingContextEventProcessorRegistry;

    @Autowired
    protected PapyrusDiagramEventSubscriptionRunner diagramEventSubscriptionRunner;

    @Autowired
    protected IEditingContextPersistenceService persistenceService;

    @Autowired
    protected PapyrusCreateRepresentationMutationRunner representationCreator;

    @Autowired
    protected IRepresentationSearchService representationSearchService;

    @Autowired
    private PapyrusCreateProjectMutationRunner projectCreator;

    @Autowired
    private PapyrusCreateDocumentMutationRunner documentCreator;

    @Autowired
    private PapyrusCreateRootObjectCreateMutationRunner rootElementCreator;

    @Autowired
    private IProjectRepository projectRepository;

    @Autowired
    private PapyrusPaletteToolQueryRunner getPaletteQueryRunner;

    @Autowired
    private PapyrusInvokeSingleClickOnDiagramElementToolRunner invokeToolOnOneElementRunner;

    @Autowired
    private PapyrusInvokeSingleClickOnTwoDiagramElementsToolRunner invokeToolOnTwoElementsRunner;

    @Autowired
    private PapyrusEditLabelMutationRunner editLabelRunner;

    @Autowired
    private PapyrusReconnectEdgeMutationRunner reconnectEdgeRunner;

    @Autowired
    private PapyrusDropOnDiagramMutationRunner dropOnDiagramRunner;

    @Autowired
    private PapyrusDropNodeMutationRunner dropNodeRunner;

    @Autowired
    private PapyrusDeleteFromDiagramMutationRunner deleteFromDiagramMutationRunner;

    @Autowired
    private PapyrusCreateChildMutationRunner childCreationRunner;

    @Autowired
    private PapyrusRepresentationDescriptionRegistry papyrusRepresentationDescriptionRegistry;

    @Autowired
    private IProjectSemanticDataSearchService projectSemanticDataSearchService;

    private Map<String, IEditingContext> editingContextCache;

    /**
     * Initializes the test with the provided {@code documentName}, {@code representationName}, and
     * {@code rootElementEClass}.
     * <p>
     * The provided {@documentName}, {@code representationName}, and {@code rootElementEClass} are used by the
     * {@link #setUp()} method to initialize the test environment.
     *
     * @param documentName
     *            the name of the document to create
     * @param representationName
     *            the name of the representation to create
     * @param rootElementEClass
     *            the type of the root semantic element to create
     * @see #setUp()
     */
    public AbstractPapyrusWebTest(String documentName, String representationName, EClass rootElementEClass) {
        this.documentName = documentName;
        this.representationName = representationName;
        this.rootElementEClass = rootElementEClass;
    }

    /**
     * Computes the cartesian product of {@code list1} and {@code list2}.
     *
     * @param list1
     *            the first list to compute the cartesian product of
     * @param list2
     *            the second list to compute the cartesian product of
     * @return the cartesian product of {@code list1} and {@code list2}
     */
    protected static Stream<Arguments> cartesianProduct(List<?> list1, List<?> list2) {
        List<Arguments> arguments = new ArrayList<>();
        for (Object source : list1) {
            for (Object target : list2) {
                arguments.add(Arguments.of(source, target));
            }
        }
        return arguments.stream();
    }

    /**
     * Initializes the test environment for the provided {@code documentName}, {@code representationName}, and
     * {@code rootElementEClass}.
     * <p>
     * This method creates a new project, document, and root semantic element. The representation matching
     * {@code representationName} is created on the root semantic element. This method also opens the
     * <i>subscription</i> for the created diagram, allowing to invoke tools performing graphical operations.
     * </p>
     * <p>
     * The {@code documentName}, {@code representationName}, and {@code rootElementEClass} are provided in this class'
     * constructor.
     * </p>
     *
     * @see #AbstractPapyrusWebTest(String, String, EClass)
     */
    @BeforeEach
    public void setUp() {
        this.setUpWithoutRepresentation();
        this.representationId = this.representationCreator.createRepresentation(this.editingContextId, this.rootObjectId, this.representationName, this.representationName);
        this.diagramEventSubscriptionRunner.createSubscription(this.editingContextId, this.representationId);
    }

    @Override
    public void before() {
        // Ignore the super set up since we create the project and we cache the editing domain in this class
    }

    @Override
    public AdapterFactoryEditingDomain getEditingDomain() {
        return ((IEMFEditingContext) this.getEditingContext()).getDomain();
    }

    @Override
    public ResourceSet getResourceSet() {
        return this.getEditingDomain().getResourceSet();
    }

    /**
     * Initializes the test environment and creates an {@code intermediateRootType} element with the given
     * {@code intermediateRootName} label.
     * <p>
     * This method creates an {@code intermediateRootType} element and adds it in the root element of the model.
     * </p>
     * <p>
     * This method is typically used to test setup test environment of diagrams that can't be created on the root
     * element of their semantic model.
     * </p>
     *
     * @param intermediateRootName
     *            the label of the intermediate element
     * @param intermediateRootType
     *            the type of the intermediate element to create
     */
    public void setUpWithIntermediateRoot(String intermediateRootName, EClass intermediateRootType) {
        String projectId = this.projectCreator.createProject("Instance", List.of(PapyrusUMLNatures.UML));

        this.editingContextId = this.getEditingContext(projectId).getId();

        ProjectInitializerInput input = new ProjectInitializerInput(UUID.randomUUID());
        this.editingContextEventProcessorRegistry.dispatchEvent(this.editingContextId, input);
        this.editingContextCache = input.getEditingContextCache();
        this.registerClasspathURIHandler();
        this.documentId = this.documentCreator.createDocument(this.editingContextId, this.documentName, UMLStereotypeProvider.EMPTY_UML);
        this.rootObjectId = this.rootElementCreator.createRootObject(UMLPackage.eNS_URI, this.rootElementEClass.getName(), this.documentId, this.editingContextId.toString());
        EObject intermediateRoot = this.createSemanticElement(this.getRootSemanticElement(), UML.getPackage_PackagedElement(), intermediateRootType, intermediateRootType.getName());
        this.intermediateRootObjectId = this.getObjectService().getId(intermediateRoot);
        this.representationId = this.representationCreator.createRepresentation(this.editingContextId, this.intermediateRootObjectId, this.representationName, this.representationName);
        this.diagramEventSubscriptionRunner.createSubscription(this.editingContextId, this.representationId);
        this.applyEditLabelTool(this.getDiagram().getNodes().get(0).getInsideLabel().getId(), intermediateRootName);
    }

    /**
     * Initializes the test environment without a representation.
     * <p>
     * This method is typically used to test diagram creations (see {@link DiagramCreationTest}) where the creation of
     * the representation is the critical part of the test.
     * </p>
     */
    public void setUpWithoutRepresentation() {
        String projectId = this.projectCreator.createProject("Instance", List.of(PapyrusUMLNatures.UML));
        this.editingContextId = this.getEditingContext(projectId).getId();

        ProjectInitializerInput input = new ProjectInitializerInput(UUID.randomUUID());
        this.editingContextEventProcessorRegistry.dispatchEvent(this.editingContextId, input);
        this.editingContextCache = input.getEditingContextCache();
        this.registerClasspathURIHandler();
        this.documentId = this.documentCreator.createDocument(this.editingContextId, this.documentName, UMLStereotypeProvider.EMPTY_UML);
        this.rootObjectId = this.rootElementCreator.createRootObject(UMLPackage.eNS_URI, this.rootElementEClass.getName(), this.documentId, this.editingContextId.toString());
    }

    /**
     * Deletes the current project and all its content.
     */
    @AfterEach
    public void tearDown() {
        this.editingContextEventProcessorRegistry.dispose();
        this.projectRepository.deleteAll();
    }

    /**
     * Creates a {@code representationName} representation on the given {@code semanticElementId}.
     * <p>
     * The {@code representationName} to create is configured by the {@code representationName} argument of this class'
     * constructor. This method invokes the {@code createRepresentation} GraphQL mutation to perform the representation
     * creation.
     * </p>
     *
     * @param semanticElementId
     *            the semantic element on which the representation is created
     * @see PapyrusCreateRepresentationMutationRunner
     */
    protected void createRepresentation(String semanticElementId) {
        this.representationId = this.representationCreator.createRepresentation(this.editingContextId, semanticElementId, this.representationName, this.representationName);
        this.diagramEventSubscriptionRunner.createSubscription(this.editingContextId, this.representationId);
    }

    /**
     * Creates a node in {@code parentDiagramElementId}.
     * <p>
     * This method invokes the {@code invokeSingleClickOnDiagramElementTool} GraphQL mutation to perform the node
     * creation.
     *
     * @param parentDiagramElementId
     *            the graphical identifier of the parent element of the node to create
     * @param nodeCreationTool
     *            the {@link CreationTool} specifying the tool section and name in the palette
     * @see CreationTool
     * @see PapyrusInvokeSingleClickOnDiagramElementToolRunner
     */
    protected void applyNodeCreationTool(String parentDiagramElementId, CreationTool nodeCreationTool) {
        Optional<String> toolId = this.getPaletteQueryRunner.getTool(this.editingContextId, this.representationId, parentDiagramElementId, nodeCreationTool.getToolSection(),
                nodeCreationTool.getToolName());
        assertThat(toolId).as(MessageFormat.format("The tool {0} | {1} doesn't exist", nodeCreationTool.getToolSection(), nodeCreationTool.getToolName())).isPresent();
        this.invokeToolOnOneElementRunner.invokeTool(this.editingContextId, this.representationId, parentDiagramElementId, toolId.get());
    }

    /**
     * Creates an edge between {@code sourceDiagramElementId} and {@code targetDiagramElementId}.
     * <p>
     * This method invokes the {@code invokeSingleClickOnTwoDiagramElementsTool} GraphQL mutation to perform the edge
     * creation.
     * </p>
     *
     * @param sourceDiagramElementId
     *            the graphical identifier of the source element of the edge
     * @param targetDiagramElementId
     *            the graphical identifier of the target element of the edge
     * @param edgeCreationTool
     *            the {@link CreationTool} specifying the tool section and name in the palette
     * @see CreationTool
     * @see PapyrusInvokeSingleClickOnTwoDiagramElementsToolRunner
     */
    protected void applyEdgeCreationTool(String sourceDiagramElementId, String targetDiagramElementId, CreationTool edgeCreationTool) {
        Optional<String> toolId = this.getPaletteQueryRunner.getTool(this.editingContextId, this.representationId, sourceDiagramElementId, edgeCreationTool.getToolSection(),
                edgeCreationTool.getToolName());
        assertThat(toolId).isPresent();
        this.invokeToolOnTwoElementsRunner.invokeTool(this.editingContextId, this.representationId, sourceDiagramElementId, targetDiagramElementId, toolId.get());
    }

    /**
     * Delete graphically a node matching with {@code diagramElementToDeleteId} identifier.
     * <p>
     * This method invokes the {@code deleteFromDiagram} GraphQL mutation to perform the node deletion.
     *
     * @param diagramElementToDeleteId
     *            the graphical identifier of the element to delete
     * @see GraphicalDeleteNodeFromDiagramMutationRunner
     */
    protected void applyNodeGraphicalDeletionTool(String diagramElementToDeleteId) {
        this.deleteFromDiagramMutationRunner.graphicalDeleteNodeFromDiagram(this.editingContextId, this.representationId, diagramElementToDeleteId);
    }

    /**
     * Delete semantically a node matching with {@code diagramElementToDeleteId} identifier.
     * <p>
     * This method invokes the {@code deleteFromDiagram} GraphQL mutation to perform the node deletion.
     *
     * @param diagramElementToDeleteId
     *            the graphical identifier of the element to delete
     * @see PapyrusDeleteFromDiagramMutationRunner
     */
    protected void applyNodeSemanticDeletionTool(String diagramElementToDeleteId) {
        this.deleteFromDiagramMutationRunner.semanticDeleteNodeFromDiagram(this.editingContextId, this.representationId, diagramElementToDeleteId);
    }

    /**
     * Delete semantically an edge matching with {@code diagramElementToDeleteId} identifier.
     * <p>
     * This method invokes the {@code deleteFromDiagram} GraphQL mutation to perform the node deletion.
     *
     * @param diagramElementToDeleteId
     *            the graphical identifier of the element to delete
     * @see PapyrusDeleteFromDiagramMutationRunner
     */
    protected void applyEdgeSemanticDeletionTool(String diagramElementToDeleteId) {
        this.deleteFromDiagramMutationRunner.semanticDeleteEdgeFromDiagram(this.editingContextId, this.representationId, diagramElementToDeleteId);
    }

    /**
     * Edits the label of the provided {@code labelId}.
     * <p>
     * This method invokes the {@code editLabel} GraphQL mutation to perform the label edition.
     * </p>
     *
     * @param labelId
     *            the identifier of the label to edit
     * @param newLabel
     *            the new value to set for the edited label
     * @see Node#getLabel()
     * @see Edge#getCenterLabel()
     * @see PapyrusEditLabelMutationRunner
     */
    protected void applyEditLabelTool(String labelId, String newLabel) {
        this.editLabelRunner.editLabel(this.editingContextId, this.representationId, labelId, newLabel);
    }

    /**
     * Reconnects the source of the provided {@code edgeId}.
     * <p>
     * This method invokes the {@code reconnectEdge} GraphQL mutation to perform the reconnection.
     * </p>
     *
     * @param edgeId
     *            the graphical identifier of the edge to reconnect the source from
     * @param newSourceElementId
     *            the graphical identifier of the new source of the edge
     * @see #applyReconnectEdgeTargetTool(String, String) to reconnect the target of an edge
     * @see PapyrusReconnectEdgeMutationRunner
     */
    protected void applyReconnectEdgeSourceTool(String edgeId, String newSourceElementId) {
        this.reconnectEdgeRunner.reconnectEdgeSource(this.editingContextId, this.representationId, edgeId, newSourceElementId);
    }

    /**
     * Reconnects the target of the provided {@code edgeId}.
     * <p>
     * This method invokes the {@code reconnectEdge} GraphQL mutation to perform the reconnection.
     * </p>
     *
     * @param edgeId
     *            the graphical identifier of the edge to reconnect the target from
     * @param newTargetElementId
     *            the graphical identifier of the new target of the edge
     * @see #applyReconnectEdgeSourceTool(String, String) to reconnect the source of an edge
     * @see PapyrusReconnectEdgeMutationRunner
     */
    protected void applyReconnectEdgeTargetTool(String edgeId, String newTargetElementId) {
        this.reconnectEdgeRunner.reconnectEdgeTarget(this.editingContextId, this.representationId, edgeId, newTargetElementId);
    }

    /**
     * Drop the provided {@code droppedElementIds} on {@code targetElementId}.
     * <p>
     * This method invokes the {@code dropOnDiagram} GraphQL mutation to perform the drop.
     * </p>
     *
     * @param targetElementId
     *            the graphical identifier of the target container element
     * @param droppedElementIds
     *            the semantic identifiers of the elements to drop
     * @see PapyrusDropOnDiagramMutationRunner
     */
    protected void applyDropOnDiagramTool(String targetElementId, List<String> droppedElementIds) {
        this.dropOnDiagramRunner.dropOnDiagram(this.editingContextId, this.representationId, targetElementId, droppedElementIds);
    }

    /**
     * Drop the provided {@code droppedElementId} on {@code targetElementId}.
     * <p>
     * This method invokes the {@code dropNode} GraphQL mutation to perform the drop.
     * </p>
     *
     * @param droppedElementId
     *            the graphical identifier of the element to drop
     * @param targetElementId
     *            the graphical identifier of the target container element
     */
    protected void applyDropNodeTool(String droppedElementId, String targetElementId) {
        this.dropNodeRunner.dropNode(this.editingContextId, this.representationId, droppedElementId, targetElementId);
    }

    /**
     * Creates a semantic child in {@code parentElementId}.
     * <p>
     * This method invokes the {@code createChild} GraphQL mutation to perform the child creation.
     * </p>
     *
     * @param parentElementId
     *            the identifier of the semantic parent of the element to create
     * @param containmentReference
     *            the containment reference of the element to create
     * @param childType
     *            the type of the element to create
     * @see PapyrusCreateChildMutationRunner
     */
    protected void applyCreateChildTool(String parentElementId, EReference containmentReference, EClass childType) {
        this.childCreationRunner.createChild(this.editingContextId, parentElementId, containmentReference, childType);
    }

    /**
     * Returns the current {@link Diagram}.
     * <p>
     * This method reloads the {@link Diagram} from the backend to ensure that the latest persisted changes are
     * accessible to callers. Note that this method cannot search for a particular {@link Diagram}, it reloads the
     * representation built by the {@link #setUp()} method.
     *
     * @return the current {@link Diagram}
     */
    protected Diagram getDiagram() {
        Optional<IRepresentation> representation = this.representationSearchService.findById(this.getEditingContext(), this.representationId.toString(), IRepresentation.class);
        assertThat(representation).isPresent();
        return (Diagram) representation.get();
    }

    /**
     * Returns the current {@link IEditingContext}.
     * <p>
     * This method reloads the {@link IEditingContext} from the backend to ensure that the latest persisted changes are
     * accessible to callers.
     * </p>
     *
     * @return the current {@link IEditingContext}
     */
    @Override
    public IEditingContext getEditingContext() {
        return this.editingContextCache.get(this.editingContextId.toString());
    }

    /**
     * Returns the diagram-to-description mappings for the nodes of the current diagram.
     *
     * @return the diagram-to-description mappings for the nodes of the current diagram
     * @see PapyrusRepresentationDescriptionRegistry
     */
    public Map<org.eclipse.sirius.components.view.diagram.NodeDescription, org.eclipse.sirius.components.diagrams.description.NodeDescription> getCapturedNodes() {
        return this.papyrusRepresentationDescriptionRegistry.getConvertedNode(this.representationName);
    }

    /**
     * Returns the diagram-to-description mappings for the edges of the current diagram.
     *
     * @return the diagram-to-description mappings for the edges of the current diagram
     * @see PapyrusRepresentationDescriptionRegistry
     */
    public Map<org.eclipse.sirius.components.view.diagram.EdgeDescription, org.eclipse.sirius.components.diagrams.description.EdgeDescription> getCapturedEdges() {
        return this.papyrusRepresentationDescriptionRegistry.getConvertedEdges(this.representationName);
    }

    /**
     * Returns the {@link NamedElement} in the semantic model with the provided {@code semanticElementName}.
     * <p>
     * This method searches for {@link NamedElement} in the semantic model. It cannot find UML elements that aren't
     * subclasses of {@link NamedElement} (e.g. Comment).
     * </p>
     * <p>
     * This method produces a test failure if the semantic model contains multiple element with the provided
     * {@code semanticElementName}.
     * </p>
     *
     * @param semanticElementName
     *            the name of the element to search for
     * @return the {@link NamedElement} with the provided name
     */
    public NamedElement findSemanticElementByName(String semanticElementName) {
        IEditingContext editingContext = this.getEditingContext();
        Optional<Object> optObject = this.getObjectService().getObject(editingContext, this.rootObjectId.toString());
        assertThat(optObject).isPresent();
        assertThat(optObject.get()).isInstanceOf(EObject.class);
        EObject rootEObject = (EObject) optObject.get();
        List<NamedElement> result = new ArrayList<>();
        if (rootEObject instanceof NamedElement rootNamedElement) {
            if (Objects.equals(rootNamedElement.getName(), semanticElementName)) {
                result.add(rootNamedElement);
            }
        }
        Iterable<EObject> allContents = () -> rootEObject.eAllContents();
        for (EObject content : allContents) {
            if (content instanceof NamedElement namedElement) {
                if (Objects.equals(namedElement.getName(), semanticElementName)) {
                    result.add(namedElement);
                }
            }
        }
        assertThat(result).as("The semantic model doesn't contain an element named " + semanticElementName).isNotEmpty();
        assertThat(result).as("The semantic model contains multiple elements named " + semanticElementName).hasSize(1);
        return result.get(0);
    }

    /**
     * Returns the {@link EObject} in the semantic model with the provided {@code semanticId}.
     * <p>
     * This method produces a test failure if the semantic model doesn't contain an element with the provided
     * {@code semanticId}.
     * </p>
     *
     * @param semanticId
     *            the semantic identifier to search for
     * @return the {@link EObject} with the provided {@code semanticId}
     */
    public EObject findSemanticElementById(String semanticId) {
        IEditingContext editingContext = this.getEditingContext();
        Optional<Object> optObject = this.getObjectService().getObject(editingContext, semanticId);
        assertThat(optObject).as("The semantic model doesn't contain an element with id " + semanticId).isPresent();
        return (EObject) optObject.get();
    }

    /**
     * Returns the {@link IDiagramElement} in the graphical model with the provided {@code graphicalElementLabel}.
     * <p>
     * This method produces a test failure if the graphical model contains multiple elements with the provided
     * {@code graphicalElementLabel}.
     *
     * @param graphicalElementLabel
     *            the label of the graphical element to search for
     * @return the {@link IDiagramElement} with the provided label
     */
    public IDiagramElement findGraphicalElementExcludingContentByLabel(String graphicalElementLabel) {
        Diagram diagram = this.getDiagram();
        List<IDiagramElement> result = new ArrayList<>();
        for (Node node : diagram.getNodes()) {
            result.addAll(this.findGraphicalElementExcludingContentByLabel(node, graphicalElementLabel));
        }
        for (Edge edge : diagram.getEdges()) {
            if (Objects.equals(edge.getTargetObjectLabel(), graphicalElementLabel)) {
                result.add(edge);
            }
        }
        assertThat(result).as("The graphical model doesn't contain an element with the label  " + graphicalElementLabel).isNotEmpty();
        assertThat(result).as("The graphical model contains multiple elements with the label " + graphicalElementLabel).hasSize(1);
        return result.get(0);
    }

    /**
     * Returns the {@link IDiagramElement} in the graphical model with the provided {@code graphicalElementLabel}.
     * <p>
     * This method produces a test failure if the graphical model contains multiple elements with the provided
     * {@code graphicalElementLabel}.
     *
     * @param graphicalElementLabel
     *            the label of the graphical element to search for
     * @return the {@link IDiagramElement} with the provided label
     */
    public IDiagramElement findGraphicalElementContentByLabel(String graphicalElementLabel) {
        Diagram diagram = this.getDiagram();
        List<IDiagramElement> result = new ArrayList<>();
        for (Node node : diagram.getNodes()) {
            result.addAll(this.findGraphicalElemenContentByLabel(node, graphicalElementLabel));
        }
        for (Edge edge : diagram.getEdges()) {
            if (Objects.equals(edge.getTargetObjectLabel(), graphicalElementLabel)) {
                result.add(edge);
            }
        }
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    /**
     * Returns the {@link IDiagramElement} in the graphical model with the provided {@code graphicalElementLabel}.
     * <p>
     * This method produces a test failure if the graphical model contains multiple elements with the provided
     * {@code graphicalElementLabel}.
     *
     * @param graphicalElementLabel
     *            the label of the graphical element to search for
     * @return the {@link IDiagramElement} with the provided label
     */
    public IDiagramElement findGraphicalContentIfExistByLabel(String graphicalElementLabel) {
        Diagram diagram = this.getDiagram();
        List<IDiagramElement> result = new ArrayList<>();
        for (Node node : diagram.getNodes()) {
            result.addAll(this.findGraphicalElemenContentByLabel(node, graphicalElementLabel));
        }
        for (Edge edge : diagram.getEdges()) {
            if (Objects.equals(edge.getTargetObjectLabel(), graphicalElementLabel)) {
                result.add(edge);
            }
        }
        if (result.size() > 0) {
            return result.get(0);
        }
        return this.findGraphicalElementExcludingContentByLabel(graphicalElementLabel);
    }

    /**
     * Returns the list of {@link IDiagramElement} contained in {@code node} with the provided {@code label}.
     * <p>
     * This method searches in all the sub-tree of elements below {@code node}. Note that the root of the sub-tree (the
     * provided {@code node}) is part of the list if its label matches the provided {@code label}.
     * </p>
     * <p>
     * This method ignores compartment nodes, but look into the compartment children to find matching elements.
     * </p>
     *
     * @param node
     *            the root of the sub-tree of elements to search into
     * @param label
     *            the label of the graphical element to search for
     * @return the list of {@link IDiagramElement} contained in {@code node} with the provided {@code label}
     */
    private List<IDiagramElement> findGraphicalElementExcludingContentByLabel(Node node, String label) {
        List<IDiagramElement> result = new ArrayList<>();
        // Ignore compartments
        if (Objects.equals(node.getTargetObjectLabel(), label) && !IdBuilder.isCompartmentNode(this.getNodeDescription(node)) && !IdBuilder.isContentNode(this.getNodeDescription(node))) {
            result.add(node);
        }
        for (Node childNode : node.getChildNodes()) {
            result.addAll(this.findGraphicalElementExcludingContentByLabel(childNode, label));
        }
        for (Node borderNode : node.getBorderNodes()) {
            result.addAll(this.findGraphicalElementExcludingContentByLabel(borderNode, label));
        }
        return result;

    }

    /**
     * Returns the list of {@link IDiagramElement} contained in {@code node} with the provided {@code label}.
     * <p>
     * This method searches in all the sub-tree of elements below {@code node}. Note that the root of the sub-tree (the
     * provided {@code node}) is part of the list if its label matches the provided {@code label}.
     * </p>
     * <p>
     * This method ignores compartment nodes, but look into the compartment children to find matching elements.
     * </p>
     *
     * @param node
     *            the root of the sub-tree of elements to search into
     * @param label
     *            the label of the graphical element to search for
     * @return the list of {@link IDiagramElement} contained in {@code node} with the provided {@code label}
     */
    private List<IDiagramElement> findGraphicalElemenContentByLabel(Node node, String label) {
        List<IDiagramElement> result = new ArrayList<>();
        // Ignore compartments
        if (Objects.equals(node.getTargetObjectLabel(), label) && !IdBuilder.isCompartmentNode(this.getNodeDescription(node)) && !IdBuilder.isHolderNode(this.getNodeDescription(node))) {
            result.add(node);
        }
        for (Node childNode : node.getChildNodes()) {
            result.addAll(this.findGraphicalElemenContentByLabel(childNode, label));
        }
        for (Node borderNode : node.getBorderNodes()) {
            result.addAll(this.findGraphicalElemenContentByLabel(borderNode, label));
        }
        return result;

    }

    /**
     * Returns the {@link NodeDescription} associated to the provided {@code node}.
     * <p>
     * This method relies on {@link #getCapturedNodes()}.
     * </p>
     *
     * @param node
     *            the {@link Node} to retrieve the description from
     * @return the description of the {@link Node}
     */
    protected NodeDescription getNodeDescription(Node node) {
        return this.getCapturedNodes().entrySet().stream()//
                .filter(entry -> entry.getValue().getId().equals(node.getDescriptionId()))//
                .findFirst()//
                .get()//
                .getKey();
    }

    /**
     * Returns the sub-node with the given {@code mapping} in the {@link Node} with the provided
     * {@code parentNodeLabel}.
     * <p>
     * This method checks the {@link NodeDescription} of the children nodes to find the one with the appropriate
     * mapping.
     * </p>
     *
     * @param parentNodeLabel
     *            the label of the parent {@link Node} to retrieve the sub-node from
     * @param mapping
     *            the mapping of the sub-node to retrieve
     * @return the sub-node if it exists, or {@code null} otherwise
     */
    protected Node getSubNode(String parentNodeLabel, String mapping) {
        IDiagramElement parent = this.findGraphicalContentIfExistByLabel(parentNodeLabel);
        assertThat(parent).as("Parent should be a Node").isInstanceOf(Node.class);
        return this.getSubNode((Node) parent, mapping);
    }

    /**
     * Returns the sub-node in the provided {@code parentNode} with the given {@code mapping}.
     * <p>
     * This method checks the {@link NodeDescription} of the {@code parentNode}'s children to find the one with the
     * appropriate mapping.
     * </p>
     *
     * @param parentNode
     *            the parent node to retrieve the sub-node from
     * @param mapping
     *            the mapping of the sub-node to retrieve
     * @return the sub-node if it exists, or {@code null} otherwise
     */
    protected Node getSubNode(Node parentNode, String mapping) {
        for (Node child : parentNode.getChildNodes()) {
            NodeDescription childDescription = this.getNodeDescription(child);
            if (childDescription.getName().equals(mapping)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Returns the {@link IDiagramElement} in the graphical model with the provided {@code graphicalId}.
     * <p>
     * This method produces a test failure if the graphical model doesn't contain an element with the provided
     * {@code graphicalId}.
     * </p>
     *
     * @param graphicalId
     *            the graphical identifier to search for
     * @return the {@link IDiagramElement} with the provided graphical {@code graphicalId}
     */
    public IDiagramElement findGraphicalElementById(String graphicalId) {
        Diagram diagram = this.getDiagram();
        List<IDiagramElement> result = new ArrayList<>();
        for (Node node : diagram.getNodes()) {
            result.addAll(this.findGraphicalElementById(node, graphicalId));
        }
        for (Edge edge : diagram.getEdges()) {
            if (Objects.equals(edge.getId(), graphicalId)) {
                result.add(edge);
            }
        }
        assertThat(result).as("The graphical model contains multiple elements with the id " + graphicalId).hasSize(1);
        return result.get(0);
    }

    /**
     * Returns the list of {@link IDiagramElement} contained in {@code node} with the provided {@code graphicalId}.
     * <p>
     * This method searches in all the sub-tree of elements below {@code node}. Note that the root of the sub-tree (the
     * provided {@code node}) is part of the list if its id matches the provided {@code graphicalId}.
     * </p>
     *
     * @param node
     *            the root of the sub-tree of elements to search into
     * @param graphicalId
     *            the graphical identifier to search for
     * @return the list of {@link IDiagramElement} contained in {@code node} with the provided {@code graphicalId}
     */
    private List<IDiagramElement> findGraphicalElementById(Node node, String graphicalId) {
        List<IDiagramElement> result = new ArrayList<>();
        if (Objects.equals(node.getId(), graphicalId)) {
            result.add(node);
        }
        for (Node childNode : node.getChildNodes()) {
            result.addAll(this.findGraphicalElementById(childNode, graphicalId));
        }
        for (Node borderNode : node.getBorderNodes()) {
            result.addAll(this.findGraphicalElementById(borderNode, graphicalId));
        }
        return result;
    }

    /**
     * Returns the root semantic element of the current {@link IEditingContext}.
     * <p>
     * This method reloads the {@link IEditingContext} to ensure that the latest version of the semantic model is
     * processed.
     * </p>
     *
     * @return the root semantic element of the current {@link IEditingContext}
     * @see #getEditingContext()
     */
    public EObject getRootSemanticElement() {
        IEditingContext editingContext = this.getEditingContext();
        Optional<Object> optObject = this.getObjectService().getObject(editingContext, this.rootObjectId.toString());
        assertThat(optObject).isPresent();
        assertThat(optObject.get()).isInstanceOf(EObject.class);
        return (EObject) optObject.get();
    }

    /**
     * Creates a source and target node with the provided {@code creationTool} in the diagram.
     * <p>
     * This method creates two nodes and sets their label with {@code elementType + "Source"} for the source one, and
     * {@code elementType + "Target"} for the target one, where {@code elementType} is the type of the created element.
     * </p>
     * <p>
     * This method is typically used in edge-based tests to initialize the diagram with source and target nodes.
     * </p>
     *
     * @param creationTool
     *            the tool to use to create the source and target nodes
     * @see #createSourceAndTargetNodes(String, CreationTool) to create a source and target node in a given parent
     */
    protected void createSourceAndTargetTopNodes(CreationTool creationTool) {
        this.createSourceAndTargetNodes(this.representationId, creationTool);
    }

    /**
     * Creates a source and target node with the provided {@code creationTool} in the provided {@code parentElementId}.
     * <p>
     * This method creates two nodes and sets their label with {@code elementType + "Source"} for the source one, and
     * {@code elementType + "Target"} for the target one, where {@code elementType} is the type of the created element.
     * </p>
     * <p>
     * This method is typically used in edge-based tests to initialize the diagram with source and target nodes.
     * </p>
     *
     * @param parentElementId
     *            the identifier of the graphical parent to create the nodes into
     * @param creationTool
     *            the tool to use to create the source and target nodes
     * @see #createSourceAndTargetTopNodes(CreationTool) to create a source and target node in the diagram
     */
    protected void createSourceAndTargetNodes(String parentElementId, CreationTool creationTool) {
        EClass creationToolEClass = creationTool.getToolEClass();
        this.createNodeWithLabel(parentElementId, creationTool, creationToolEClass.getName() + "Source");
        this.createNodeWithLabel(parentElementId, creationTool, creationToolEClass.getName() + "Target");
    }

    /**
     * Creates a node with the provided {@code creationTool} in the given {@code parentElementId}.
     * <p>
     * This method doesn't set the label of the created element, which will have its default label. See
     * {@link #createNodeWithLabel(String, CreationTool, String)} to create a node and set its label.
     * </p>
     *
     * @param parentElementId
     *            the identifier of the graphical parent to create the node into
     * @param creationTool
     *            the tool to use to create the node
     * @return the created node
     */
    protected Node createNode(String parentElementId, CreationTool creationTool) {
        this.applyNodeCreationTool(parentElementId, creationTool);
        return (Node) this.findGraphicalElementExcludingContentByLabel(creationTool.getToolEClass().getName() + "1");
    }

    /**
     * Creates a node with the provided {@code creationTool} in the given {@code parentElementId}.
     * <p>
     * The label of the created node is set to {@code label}. This method assumes that the diagram doesn't already
     * contain an element named {@code elementType + "1"} (the default name for elements), where {@code elementType} is
     * the type of the created element. This method produces a test failure if such element exists.
     * </p>
     *
     * @param parentElementId
     *            the identifier of the graphical parent to create the node into
     * @param creationTool
     *            the tool to use to create the node
     * @param label
     *            the label to set in the created node
     * @return the created node
     */
    protected Node createNodeWithLabel(String parentElementId, CreationTool creationTool, String label) {
        Node createdNode = this.createNode(parentElementId, creationTool);
        if (createdNode.getInsideLabel() != null) {
            this.applyEditLabelTool(createdNode.getInsideLabel().getId(), label);
        } else if (createdNode.getOutsideLabels() != null && !createdNode.getOutsideLabels().isEmpty()) {
            this.applyEditLabelTool(createdNode.getOutsideLabels().get(0).id(), label);
        } else {
            fail("No label found for element " + createdNode.getId());
        }

        // Reload the node to ensure that the new label is present
        return (Node) this.findGraphicalElementById(createdNode.getId());
    }

    /**
     * Creates an edge with the provided {@code edgeCreationTool} between {@code sourceLabel} and {@code targetLabel}.
     *
     * @param sourceLabel
     *            the label of the source graphical element
     * @param targetLabel
     *            the label of the target graphical element
     * @param edgeCreationTool
     *            the creation tool to use to create the edge
     * @return the created edge
     */
    protected String createEdge(String sourceLabel, String targetLabel, CreationTool edgeCreationTool) {
        int diagramEdgeCount = this.getDiagram().getEdges().size();
        Node sourceNode = (Node) this.findGraphicalElementExcludingContentByLabel(sourceLabel);
        Node targetNode = (Node) this.findGraphicalElementExcludingContentByLabel(targetLabel);
        this.applyEdgeCreationTool(sourceNode.getId(), targetNode.getId(), edgeCreationTool);
        assertThat(this.getDiagram().getEdges()).as("Diagram doesn't contain the created edge").hasSize(diagramEdgeCount + 1);
        return this.getDiagram().getEdges().get(diagramEdgeCount).getId();
    }

    /**
     * Creates a semantic element of the given {@code type} in the given {@code parentElement}, with the given
     * {@code name}.
     * <p>
     * This method operates at the semantic level. It doesn't create a graphical element. The created element is
     * contained in {@code parentElement} through the {@code containmentReference} reference.
     * </p>
     * <p>
     * <b>Note:</b> this method doesn't set the name of the element if {@code type} isn't a sub-type of
     * {@link NamedElement}.
     * </p>
     * <p>
     * This method is typically used to create semantic elements to drop.
     * </p>
     *
     * @param parentElement
     *            the semantic element containing the created element
     * @param containmentReference
     *            the reference containing the created element
     * @param type
     *            the type of the created element
     * @param name
     *            the name of the created element
     * @return the created element
     */
    protected EObject createSemanticElement(EObject parentElement, EReference containmentReference, EClass type, String name) {
        String parentElementId = this.getObjectService().getId(parentElement);
        int numberOfChildren = ((List<?>) parentElement.eGet(containmentReference)).size();
        this.applyCreateChildTool(parentElementId, containmentReference, type);
        IEditingContext editingContext = this.getEditingContext();
        EObject updatedParentElement = (EObject) this.getObjectService().getObject(editingContext, parentElementId).get();
        EObject createdObject = (EObject) ((List<?>) updatedParentElement.eGet(containmentReference)).get(numberOfChildren);
        if (createdObject instanceof NamedElement namedElement) {
            namedElement.setName(name);
            this.persistenceService.persist(new ICause.NoOp(), editingContext);
        }
        return createdObject;
    }
}

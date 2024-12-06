/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.aqlservices.activity;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters;
import org.eclipse.papyrus.uml.domain.services.modify.ElementFeatureModifier;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger.ILogLevel;
import org.eclipse.papyrus.uml.domain.services.status.CheckStatus;
import org.eclipse.papyrus.web.application.representations.IWebExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.IWebInternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.ViewHelper;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.papyrus.web.sirius.contributions.query.NodeMatcher;
import org.eclipse.papyrus.web.sirius.contributions.query.NodeMatcher.BorderNodeStatus;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.ActionInputPin;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityGroup;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityPartition;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.ExpansionNode;
import org.eclipse.uml2.uml.ExpansionRegion;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.InterruptibleActivityRegion;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Pin;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.TimeEvent;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValuePin;
import org.eclipse.uml2.uml.internal.impl.InputPinImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Services used in the Activity Diagram.
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
@Service
public class ActivityDiagramService extends AbstractDiagramService {

    /**
     * The width/height to use for Hourglass nodes used to represent AcceptEventAction.
     */
    private static final String HOURGLASS_NODE_SIZE = "80";

    /**
     * The keyword displayed in {@link DecisionNode}'s notes.
     */
    private static final String DECISION_INPUT = "decisionInput";

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityDiagramService.class);

    /**
     * Logger used to report errors and warnings to the user.
     */
    private ILogger logger;

    /**
     * Initializes the service with the provided parameters.
     *
     * @param objectService
     *            the service used to manipulate objects
     * @param diagramNavigationService
     *            the service used to navigate in the diagram
     * @param diagramOperationsService
     *            the service used to handle operations on the diagram
     * @param editableChecker
     *            the checker used to verify if an element can be edited
     * @param viewDiagramService
     *            the service used to handle views in the diagram
     * @param logger
     *            Logger used to report errors and warnings to the user
     */
    public ActivityDiagramService(IObjectService objectService, IDiagramNavigationService diagramNavigationService, IDiagramOperationsService diagramOperationsService,
            IEditableChecker editableChecker, IViewDiagramDescriptionService viewDiagramService, ILogger logger) {
        super(objectService, diagramNavigationService, diagramOperationsService, editableChecker, viewDiagramService, logger);
        this.logger = logger;
    }

    @Override
    protected IWebExternalSourceToRepresentationDropBehaviorProvider buildSemanticDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getObjectService(), this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext, capturedNodeDescriptions);
        IWebExternalSourceToRepresentationDropBehaviorProvider dropProvider = new ActivitySemanticDropBehaviorProvider(editionContext, createViewHelper, this.getObjectService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

    @Override
    protected IWebInternalSourceToRepresentationDropBehaviorProvider buildGraphicalDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getObjectService(), this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext, capturedNodeDescriptions);
        IWebInternalSourceToRepresentationDropBehaviorProvider dropProvider = new ActivityGraphicalDropBehaviorProvider(editionContext, createViewHelper, this.getObjectService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.getDiagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

    /**
     * Compute width expression of node representing a given {@link AcceptEventAction}.
     *
     * @param acceptEventAction
     *            the {@link AcceptEventAction} represented on the diagram
     * @return the width expression of node representing a given {@link AcceptEventAction}
     */
    public String computeAcceptEventActionWidthExpressionAD(AcceptEventAction acceptEventAction) {
        String widthComputationExpression = "170";
        EList<Trigger> triggers = acceptEventAction.getTriggers();
        if (triggers != null && triggers.size() == 1) {
            Event event = triggers.get(0).getEvent();
            if (event instanceof TimeEvent) {
                // case hourglass
                widthComputationExpression = HOURGLASS_NODE_SIZE;
            }
        }
        return widthComputationExpression;
    }

    /**
     * Compute height expression of node representing a given {@link AcceptEventAction}.
     *
     * @param acceptEventAction
     *            the {@link AcceptEventAction} represented on the diagram
     * @return the height expression of node representing a given {@link AcceptEventAction}
     */
    public String computeAcceptEventActionHeightExpressionAD(AcceptEventAction acceptEventAction) {
        String heightComputationExpression = "70";
        EList<Trigger> triggers = acceptEventAction.getTriggers();
        if (triggers != null && triggers.size() == 1) {
            Event event = triggers.get(0).getEvent();
            if (event instanceof TimeEvent) {
                // case hourglass
                heightComputationExpression = HOURGLASS_NODE_SIZE;
            }
        }
        return heightComputationExpression;
    }

    /**
     * Used to retrieve {@link ActivityNode}s to display according to the semantic context.
     * <ul>
     * <li>For an {@link Activity}, {@link ActivityNode}s that are not already displayed in an
     * {@link ActivityPartition}.</li>
     * <li>For an {@link ActivityPartition}, {@link ActivityNode}s from the {@link ActivityPartition#getNodes()}
     * feature.</li>
     * </ul>
     *
     * @param semanticContext
     *            the context in which ActivityNodes should be displayed.
     * @return all {@link ActivityNode}s that can be displayed in the specified context
     */
    public Collection<ActivityNode> getActivityNodeCandidatesAD(final EObject semanticContext) {
        // Copied from Papyrus Desktop, see
        // org.eclipse.papyrus.sirius.uml.diagram.activity.services.ActivityDiagramServices
        Collection<ActivityNode> activityNodes = Collections.emptyList();
        if (semanticContext instanceof Activity) {
            Activity activity = (Activity) semanticContext;
            activityNodes = activity.getNodes().stream().filter(node -> node.getInGroups().isEmpty() && node.getInStructuredNode() == null).collect(Collectors.toList());
        } else if (semanticContext instanceof ActivityPartition) {
            ActivityPartition activityPartition = (ActivityPartition) semanticContext;
            activityNodes = activityPartition.getNodes().stream().filter(node -> node.getInStructuredNode() == null).collect(Collectors.toList());
        } else if (semanticContext instanceof InterruptibleActivityRegion) {
            InterruptibleActivityRegion interruptibleActivityRegion = (InterruptibleActivityRegion) semanticContext;
            activityNodes = interruptibleActivityRegion.getNodes().stream().filter(node -> node.getInStructuredNode() == null).collect(Collectors.toList());
        } else if (semanticContext instanceof StructuredActivityNode) {
            StructuredActivityNode structuredActivityNode = (StructuredActivityNode) semanticContext;
            activityNodes = structuredActivityNode.getNodes();
        }
        return activityNodes;
    }

    /**
     * Used to retrieve {@link ActivityPartition}s to display according to the semantic context.
     * <ul>
     * <li>For an {@link Activity}, {@link ActivityPartition}s filtered from the {@link Activity#getOwnedGroups()}
     * feature.</li>
     * <li>For an {@link ActivityPartition}, {@link ActivityPartition}s from the
     * {@link ActivityPartition#getSubpartitions()} feature.</li>
     * </ul>
     *
     * @param semanticContext
     *            the context in which ActivityPartitions should be displayed
     * @return all {@link ActivityPartition}s that can be displayed in the specified context
     */
    public Collection<ActivityPartition> getActivityPartitionCandidatesAD(final EObject semanticContext) {
        // Copied from Papyrus Desktop, see
        // org.eclipse.papyrus.sirius.uml.diagram.activity.services.ActivityDiagramServices
        Collection<ActivityPartition> activityPartitions = Collections.emptyList();
        if (semanticContext instanceof Activity activity) {
            activityPartitions = activity.getPartitions();
        } else if (semanticContext instanceof ActivityPartition activityPartition) {
            activityPartitions = activityPartition.getSubpartitions();
        }
        return activityPartitions;
    }

    /**
     * Provides the input or output {@link ExpansionNode} from the given {@link ExpansionRegion}.
     *
     * @param expansionRegion
     *            the parent {@link ExpansionRegion}
     * @return the list of {@link ExpansionNode}
     */
    public Collection<ExpansionNode> getExpansionNodesCandidatesAD(final ExpansionRegion expansionRegion) {
        // Copied from Papyrus Desktop, see
        // org.eclipse.papyrus.sirius.uml.diagram.activity.services.ActivityDiagramServices
        Collection<ExpansionNode> expansionNodes = new HashSet<>();
        expansionNodes.addAll(expansionRegion.getInputElements());
        expansionNodes.addAll(expansionRegion.getOutputElements());
        return expansionNodes;
    }

    /**
     * Used to retrieve {@link InterruptibleActivityRegion} to display according to the semantic context.
     * <ul>
     * <li>For an {@link Activity}, {@link InterruptibleActivityRegion}s filtered from the
     * {@link Activity#getOwnedGroups()} feature.</li>
     * </ul>
     *
     * @param semanticContext
     *            the context in which InterruptibleActivityRegions should be displayed
     * @return all {@link InterruptibleActivityRegion}s that can be displayed in the specified context
     */
    public Collection<InterruptibleActivityRegion> getInterruptibleActivityRegionCandidatesAD(final EObject semanticContext) {
        // Copied from Papyrus Desktop, see
        // org.eclipse.papyrus.sirius.uml.diagram.activity.services.ActivityDiagramServices
        Collection<InterruptibleActivityRegion> interruptibleActivityRegions = Collections.emptyList();
        if (semanticContext instanceof Activity) {
            Activity activity = (Activity) semanticContext;
            interruptibleActivityRegions = activity.getOwnedGroups().stream().filter(InterruptibleActivityRegion.class::isInstance).map(InterruptibleActivityRegion.class::cast)
                    .collect(Collectors.toList());
        }
        return interruptibleActivityRegions;
    }

    /**
     * Service to create a new {@link ActivityNode} kind in an {@link Activity}, {@link ActivityPartition},
     * {@link InterruptibleActivityRegion} or in {@link StructuredActivityNode} kind (including
     * {@link ExpansionRegion}).
     *
     * @param parent
     *            the semantic parent element on which the tool is applied
     * @param type
     *            the type of element to create
     * @param referenceName
     *            the containment reference to use to create the element
     * @param targetView
     *            the view on which the tool is applied
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescription
     *            the {@link NodeDescription}s
     */
    public EObject createActivityNodeAD(EObject parent, String type, String referenceName, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        // Copied from Papyrus Desktop, see
        // org.eclipse.papyrus.sirius.uml.diagram.activity.services.ActivityDiagramServices
        final EObject result;
        if (parent instanceof Activity) {
            result = this.create(parent, type, referenceName, targetView, diagramContext, capturedNodeDescriptions);
        } else if (parent instanceof ActivityGroup && !(parent instanceof StructuredActivityNode)) {
            result = this.createActivityNodeInActivityGroup(parent, type, referenceName, targetView, diagramContext, capturedNodeDescriptions);
        } else if (parent instanceof StructuredActivityNode) {
            result = this.create(parent, type, UMLPackage.eINSTANCE.getStructuredActivityNode_Node().getName(), targetView, diagramContext, capturedNodeDescriptions);
        } else {
            String message = MessageFormat.format("Cannot create {0} in {1}", type, parent.eClass());
            LOGGER.warn(message);
            this.logger.log(message, ILogLevel.WARNING);
            result = null;
        }
        return result;
    }

    /**
     * Creates a new {@link ExpansionNode} in the given parent {@link ExpansionRegion}.
     *
     * @param parentExpansionRegion
     *            the parent {@link ExpansionRegion}.
     * @param targetView
     *            the view on which the tool is applied
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescriptions
     *            the {@link NodeDescription}s
     * @param isInput
     *            {@code true} if the created {@link ExpansionNode} should be added as input, {@code false} as output
     */
    public EObject createExpansionNodeAD(EObject parentExpansionRegion, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions, boolean isInput) {
        // Copied from Papyrus Desktop, see
        // org.eclipse.papyrus.sirius.uml.diagram.activity.services.ActivityDiagramServices
        EObject createdElement = this.createInHolder(parentExpansionRegion, "uml::ExpansionNode", UMLPackage.eINSTANCE.getStructuredActivityNode_Node().getName(), targetView, diagramContext,
                capturedNodeDescriptions);
        if (createdElement instanceof ExpansionNode) {
            EStructuralFeature expansionNodeFeature;
            EStructuralFeature expansionRegionFeature;
            if (isInput) {
                expansionNodeFeature = UMLPackage.eINSTANCE.getExpansionNode_RegionAsInput();
                expansionRegionFeature = UMLPackage.eINSTANCE.getExpansionRegion_InputElement();
            } else {
                expansionNodeFeature = UMLPackage.eINSTANCE.getExpansionNode_RegionAsOutput();
                expansionRegionFeature = UMLPackage.eINSTANCE.getExpansionRegion_OutputElement();
            }
            ElementFeatureModifier featureModifier = new ElementFeatureModifier(this.getECrossReferenceAdapter(parentExpansionRegion), this.getEditableChecker());
            featureModifier.addValue(createdElement, expansionNodeFeature.getName(), parentExpansionRegion);
            featureModifier.addValue(parentExpansionRegion, expansionRegionFeature.getName(), createdElement);
        }
        return createdElement;
    }

    private EObject createActivityNodeInActivityGroup(EObject parent, String type, String referenceName, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        // Copied from Papyrus Desktop, see
        // org.eclipse.papyrus.sirius.uml.diagram.activity.services.ActivityDiagramServices
        final Activity parentActivity = this.findActivity(parent);
        EObject createdElement = null;
        if (parentActivity != null) {
            createdElement = this.create(parentActivity, type, referenceName, targetView, diagramContext, capturedNodeDescriptions);
            if (createdElement instanceof ActivityNode) {
                ElementFeatureModifier featureModifier = new ElementFeatureModifier(this.getECrossReferenceAdapter(parent), this.getEditableChecker());
                String featureName = null;
                if (parent instanceof ActivityPartition) {
                    featureName = UMLPackage.eINSTANCE.getActivityPartition_Node().getName();
                } else if (parent instanceof InterruptibleActivityRegion) {
                    featureName = UMLPackage.eINSTANCE.getInterruptibleActivityRegion_Node().getName();
                }
                if (featureName != null) {
                    featureModifier.addValue(parent, featureName, createdElement);
                }
            }
        }
        return createdElement;
    }

    /**
     * Find parent {@link Activity}.
     *
     * @param editElement
     *            {@link ActivityPartition} element
     * @return {@code null} if {@link Activity} not found
     */
    private Activity findActivity(EObject element) {
        // Copied from Papyrus Desktop, see
        // org.eclipse.papyrus.sirius.uml.diagram.activity.services.ActivityDiagramServices
        final Activity container;
        if (element instanceof ActivityGroup activityGroup) {
            if (activityGroup.eContainer() instanceof Activity activity) {
                container = activity;
            } else {
                container = this.findActivity(activityGroup.eContainer());
            }
        } else {
            container = null;
        }
        return container;
    }

    /**
     * Service to create a new {@link ActionInputPin}.
     *
     * @param parent
     *            the semantic parent element on which the tool is applied
     * @param targetView
     *            the view on which the tool is applied
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescriptions
     *            the {@link NodeDescription}s
     * @return the new created {@link ActionInputPin}
     */
    public EObject createActionInputPinAD(Element parent, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        return this.createPin(parent, UMLPackage.eINSTANCE.getActionInputPin().getName(), targetView, diagramContext, capturedNodeDescriptions);
    }

    /**
     * Service to create a new {@link InputPin}.
     *
     * @param parent
     *            the semantic parent element on which the tool is applied
     * @param targetView
     *            the view on which the tool is applied
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescriptions
     *            the {@link NodeDescription}s
     * @return the new created {@link InputPin}
     */
    public EObject createInputPinAD(Element parent, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        return this.createPin(parent, UMLPackage.eINSTANCE.getInputPin().getName(), targetView, diagramContext, capturedNodeDescriptions);
    }

    /**
     * Service to create a new {@link OutputPin}.
     *
     * @param parent
     *            the semantic parent element on which the tool is applied
     * @param targetView
     *            the view on which the tool is applied
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescriptions
     *            the {@link NodeDescription}s
     * @return the new created {@link OutputPin}
     */
    public EObject createOutputPinAD(Element parent, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        return this.createPin(parent, UMLPackage.eINSTANCE.getOutputPin().getName(), targetView, diagramContext, capturedNodeDescriptions);
    }

    /**
     * Service to create a new {@link ValuePin}.
     *
     * @param parent
     *            the semantic parent element on which the tool is applied
     * @param targetView
     *            the view on which the tool is applied
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescriptions
     *            the {@link NodeDescription}s
     * @return the new created {@link ValuePin}
     */
    public EObject createValuePinAD(Element parent, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        return this.createPin(parent, UMLPackage.eINSTANCE.getValuePin().getName(), targetView, diagramContext, capturedNodeDescriptions);
    }

    /**
     * Create a {@link Pin} and add it to the right feature of the semantic parent.
     *
     * @param parent
     *            the semantic parent element on which the tool is applied
     * @param typeToCreate
     *            the name of the EClass Pin to create
     * @param targetView
     *            the view on which the tool is applied
     * @param diagramContext
     *            the diagram context
     * @param capturedNodeDescriptions
     *            the {@link NodeDescription}s
     * @return the new created {@link Pin}
     */
    private EObject createPin(Element parent, String typeToCreate, Node targetView, IDiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        EObject createdObject = null;
        EStructuralFeature feature = new ActivityFeatureProvider().getActivityFeature(parent, typeToCreate);
        if (feature != null) {
            createdObject = this.create(parent, typeToCreate, feature.getName(), targetView, diagramContext, capturedNodeDescriptions);
        }
        return createdObject;
    }

    /**
     * Used to retrieve {@link ActionInputPin} to display according to the semantic context.
     *
     * @param semanticContext
     *            the context in which {@link ActionInputPin} should be displayed
     * @return all {@link ActionInputPin} that can be displayed in the specified context
     */
    public Collection<ActionInputPin> getActionInputPinCandidatesAD(final EObject semanticContext) {
        Collection<ActionInputPin> actionInputPinCandidates = Collections.emptyList();
        if (semanticContext != null) {
            actionInputPinCandidates = semanticContext.eContents().stream().filter(ActionInputPin.class::isInstance).map(ActionInputPin.class::cast).toList();
        }
        return actionInputPinCandidates;
    }

    /**
     * Used to retrieve {@link InputPin} to display according to the semantic context.
     *
     * @param semanticContext
     *            the context in which {@link InputPin} should be displayed
     * @return all {@link InputPin} that can be displayed in the specified context
     */
    public Collection<InputPin> getInputPinCandidatesAD(final EObject semanticContext) {
        Collection<InputPin> inputPinCandidates = Collections.emptyList();
        if (semanticContext != null) {
            inputPinCandidates = semanticContext.eContents().stream().filter(InputPin.class::isInstance).filter(c -> InputPinImpl.class.getSimpleName().equals(c.getClass().getSimpleName()))
                    .map(InputPin.class::cast).toList();
        }
        return inputPinCandidates;
    }

    /**
     * Used to retrieve {@link OutputPin} to display according to the semantic context.
     *
     * @param semanticContext
     *            the context in which {@link OutputPin} should be displayed
     * @return all {@link InputPin} that can be displayed in the specified context
     */
    public Collection<OutputPin> getOutputPinCandidatesAD(final EObject semanticContext) {
        Collection<OutputPin> outputPinCandidates = Collections.emptyList();
        if (semanticContext != null) {
            outputPinCandidates = semanticContext.eContents().stream().filter(OutputPin.class::isInstance).map(OutputPin.class::cast).toList();
        }
        return outputPinCandidates;
    }

    /**
     * Used to retrieve {@link ValuePin} to display according to the semantic context.
     *
     * @param semanticContext
     *            the context in which {@link ValuePin} should be displayed
     * @return all {@link ValuePin} that can be displayed in the specified context
     */
    public Collection<ValuePin> getValuePinCandidatesAD(final EObject semanticContext) {
        Collection<ValuePin> valuePinCandidates = Collections.emptyList();
        if (semanticContext != null) {
            valuePinCandidates = semanticContext.eContents().stream().filter(ValuePin.class::isInstance).map(ValuePin.class::cast).toList();
        }
        return valuePinCandidates;
    }

    /**
     * Checks if an Activity diagram can be created from the given {@code context}.
     *
     * @param context
     *            the target element used to create the Activity description
     * @return {@code true} if the Activity diagram can be created, {@code false} otherwise
     */
    public boolean canCreateDiagramAD(EObject context) {
        return !this.isContainedInProfileResource(context) && (context instanceof Package || context instanceof BehavioredClassifier);
    }

    /**
     * Service used to check if an object can be created under the specified container.
     *
     * @param container
     *            the container that should contains the new object to create
     * @param objectToCreate
     *            the EClass defining the type of the object to create
     * @return {@code true} if the object can be created, {@code false} otherwise
     */
    public boolean canCreateIntoParentAD(EObject container, String eClassToCreate) {
        EStructuralFeature feature = new ActivityFeatureProvider().getActivityFeature(container, eClassToCreate);
        final CheckStatus canCreateStatus;
        if (feature != null) {
            canCreateStatus = this.buildElementCreationChecker().canCreate(container, eClassToCreate, feature.getName());
        } else {
            canCreateStatus = CheckStatus.no(MessageFormat.format("No containment feature for {0} found in {1}", eClassToCreate, container));
        }
        return canCreateStatus.isValid();
    }

    /**
     * Returns whether the {@link DecisionNode}'s note should be displayed.
     * <p>
     * This method ensures that the {@link DecisionNode}'s note is never displayed if the {@link DecisionNode} is not
     * visible, and if its decision input is not set. This is done by inspecting the {@code diagramContext}'s
     * creation/deletion requests, as well as the {@code previousDiagram} to check if the {@link DecisionNode} was
     * previously displayed.
     * </p>
     *
     * @param decisionNode
     *            the {@link DecisionNode} to check
     * @param diagramContext
     *            the context of the diagram
     * @param previousDiagram
     *            the previous state of the diagram
     * @param editingContext
     *            the editing context
     * @return {@code true} if the {@link DecisionNode}'s note should be displayed, {@code false} otherwise
     */
    public boolean showDecisionNodeNote(DecisionNode decisionNode, DiagramContext diagramContext, Diagram previousDiagram, IEditingContext editingContext) {
        List<Node> previousDecisionNode = this.getDiagramNavigationService().getMatchingNodes(previousDiagram, editingContext,
                NodeMatcher.buildSemanticAndNodeMatcher(BorderNodeStatus.BOTH,
                        semanticObject -> Objects.equals(decisionNode, semanticObject),
                        // Filter out Note node to make sure the actual DecisionNode is visible
                        node -> !(Objects.equals(node.getType(), "customnode:note"))));
        boolean isDeletingDecisionNode = false;
        boolean isCreatingDecisionNode = false;
        if (!previousDecisionNode.isEmpty()) {
            isDeletingDecisionNode = diagramContext.getViewDeletionRequests().stream() //
                    .anyMatch(viewDeletionRequest -> Objects.equals(viewDeletionRequest.getElementId(), previousDecisionNode.get(0).getId()));
        }
        isCreatingDecisionNode = diagramContext.getViewCreationRequests().stream() //
                .anyMatch(viewCreationRequest -> Objects.equals(viewCreationRequest.getTargetObjectId(), this.getObjectService().getId(decisionNode)));
        boolean showDecisionNodeNote = false;
        if (!previousDecisionNode.isEmpty() && !isDeletingDecisionNode) {
            // The DecisionNode was already displayed on the diagram, and we aren't currently deleting it. In this case
            // we want to show the DecisionNode note if its decision input is set. This ensures that a note that was
            // previously visible stays visible.
            showDecisionNodeNote = decisionNode != null && decisionNode.getDecisionInput() != null;
        } else if (isCreatingDecisionNode && (previousDecisionNode.isEmpty() || isDeletingDecisionNode)) {
            // We are currently creating the DecisionNode, either via a simple creation request, or via a graphical drag
            // & drop (which is decomposed into a deletion request and a creation request). In this case we want to show
            // the DecisionNode note if its decision input is set. This ensures that the note is visible directly after
            // the node creation, or after a graphical drag & drop.
            showDecisionNodeNote = decisionNode != null && decisionNode.getDecisionInput() != null;
        }
        return showDecisionNodeNote;
    }

    /**
     * Provides the label to display on the note attached to a {@link DecisionNode} if the
     * {@link DecisionNode#getDecisionInput()} feature is set.
     *
     * @param decisionNode
     *            the {@link DecisionNode} from which retrieving the note label.
     * @return the label or empty string if it cannot be computed.
     */
    public String getDecisionInputNoteLabel(DecisionNode decisionNode) {
        return Optional.ofNullable(decisionNode).map(DecisionNode::getDecisionInput).map(this::computeDecisionInputLabel).orElse(UMLCharacters.EMPTY);
    }

    /**
     * Gets the label of an {@link AcceptEventAction}. Depending of the trigger type, the label must be placed inside or
     * outside the shape.
     *
     * @param acceptEventAction
     *            self
     * @param isInside
     *            <code>true</code> if computation for the inside label, false otherwise
     * @return a label.
     */
    public String getAcceptEventActionLabel(AcceptEventAction acceptEventAction, boolean isInside) {
        final String label;
        boolean eventTriggeredAcceptEventAction = this.isEventTriggeredAcceptEventAction(acceptEventAction);
        if (isInside) {
            if (eventTriggeredAcceptEventAction) {
                label = "";
            } else {
                label = this.renderLabel(acceptEventAction);
            }
        } else {
            if (eventTriggeredAcceptEventAction) {
                label = this.renderLabel(acceptEventAction);
            } else {
                label = "";
            }
        }

        return label;
    }

    /**
     * Checks if the given {@link AcceptEventAction} is triggered by a TimeEvent.
     *
     * @param action
     *            self
     * @return a boolean
     */
    public boolean isEventTriggeredAcceptEventAction(AcceptEventAction action) {
        EList<Trigger> triggers = action.getTriggers();
        return !triggers.isEmpty() && triggers.get(0).getEvent() instanceof TimeEvent;
    }

    private String computeDecisionInputLabel(Behavior behavior) {
        return UMLCharacters.ST_LEFT + DECISION_INPUT + UMLCharacters.ST_RIGHT + UMLCharacters.SPACE + behavior.getName();
    }
}

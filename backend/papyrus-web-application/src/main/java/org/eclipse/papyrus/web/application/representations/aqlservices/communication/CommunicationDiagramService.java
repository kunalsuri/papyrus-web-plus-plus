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
package org.eclipse.papyrus.web.application.representations.aqlservices.communication;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.uml.domain.services.IEditableChecker;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.web.application.representations.IWebExternalSourceToRepresentationDropBehaviorProvider;
import org.eclipse.papyrus.web.application.representations.aqlservices.AbstractDiagramService;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.IViewHelper;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.ViewHelper;
import org.eclipse.papyrus.web.sirius.contributions.DiagramNavigator;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramNavigationService;
import org.eclipse.papyrus.web.sirius.contributions.IDiagramOperationsService;
import org.eclipse.papyrus.web.sirius.contributions.IViewDiagramDescriptionService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.DurationObservation;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.TimeObservation;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.springframework.stereotype.Service;

/**
 * Gather all services to be used in the Communication Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@Service
public class CommunicationDiagramService extends AbstractDiagramService {

    /**
     * Logger used to report errors and warnings to the user.
     */
    private final ILogger logger;

    /**
     * Constructor.
     *
     * @param identityService
     *         the service in charge of getting the identity of an object
     * @param labelService
     *         the service in charge of getting labels and images of an object
     * @param objectSearchService
     *         the service in charge of getting an object from its id
     * @param diagramNavigationService
     *            helper that must introspect the current diagram's structure and its description
     * @param diagramOperationsService
     *            helper that must modify the current diagram, most notably create or deleteviews for unsynchronized
     *            elements
     * @param editableChecker
     *            Object that check if an element can be edited
     * @param viewDiagramService
     *            Service used to navigate in DiagramDescription
     * @param logger
     *            Logger used to report errors and warnings to the user
     */
    //CHECKSTYLE:OFF Injected parameters
    public CommunicationDiagramService(IIdentityService identityService, ILabelService labelService,
            IObjectSearchService objectSearchService, IDiagramNavigationService diagramNavigationService,
            IDiagramOperationsService diagramOperationsService,
            IEditableChecker editableChecker, IViewDiagramDescriptionService viewDiagramService, ILogger logger) {
        //CHECKSTYLE:ON Injected parameters
        super(identityService, labelService, objectSearchService, diagramNavigationService, diagramOperationsService,
                editableChecker, viewDiagramService, logger);
        this.logger = logger;
    }

    @Override
    protected IWebExternalSourceToRepresentationDropBehaviorProvider buildSemanticDropBehaviorProvider(EObject semanticDroppedElement, IEditingContext editionContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> capturedNodeDescriptions) {
        IViewHelper createViewHelper = ViewHelper.create(this.getIdentityService(), getLabelService(),
                this.getViewDiagramService(), this.getDiagramOperationsService(), diagramContext,
                capturedNodeDescriptions);
        IWebExternalSourceToRepresentationDropBehaviorProvider dropProvider = new CommunicationSemanticDropBehaviorProvider(
                editionContext, createViewHelper, this.getObjectSearchService(),
                this.getECrossReferenceAdapter(semanticDroppedElement), this.getEditableChecker(),
                new DiagramNavigator(this.getDiagramNavigationService(), diagramContext.diagram(), capturedNodeDescriptions), this.logger);
        return dropProvider;
    }

    /**
     * Check if a Communication diagram can be created from the given {@code context}.
     *
     * @param context
     *            the target element used to create Communication description
     * @return {@code true} if the Communication diagram can be created, {@code false} otherwise.
     */
    public boolean canCreateDiagramCOD(EObject context) {
        return !this.isContainedInProfileResource(context) && (context instanceof Package || context instanceof BehavioredClassifier);
    }

    /**
     * Used to retrieve {@link DurationObservation} to display according to the semantic context.
     * <ul>
     * <li>For a non-{@link Package}, DurationObservations that are contained by its parent Package.</li>
     * <li>For a {@link Package}, DurationObservations from the Package#packagedElements feature.</li>
     * </ul>
     *
     * @see org.eclipse.papyrus.uml.service.types.helper.ObservationEditHelper
     *
     * @param semanticContext
     *            the context in which DurationObservations should be displayed.
     * @return all DurationObservations that can be displayed in the specified context
     */
    public Collection<DurationObservation> getDurationObservationCandidatesCOD(final EObject semanticContext) {
        Collection<DurationObservation> durationObservations = Collections.emptyList();
        if (semanticContext == null) {
            return durationObservations;
        }
        if (semanticContext instanceof Package) {
            EList<PackageableElement> packageElements = ((Package) semanticContext).getPackagedElements();
            durationObservations = packageElements.stream() //
                    .filter(DurationObservation.class::isInstance) //
                    .map(DurationObservation.class::cast) //
                    .collect(Collectors.toList()); //
        } else {
            durationObservations = this.getDurationObservationCandidatesCOD(this.getPackageContainerCOD(semanticContext));
        }
        return durationObservations;
    }

    /**
     * Used to retrieve {@link TimeObservation} to display according to the semantic context.
     * <ul>
     * <li>For a non-{@link Package}, TimeObservations that are contained by its parent Package.</li>
     * <li>For a {@link Package}, TimeObservations from the Package#packagedElements feature.</li>
     * </ul>
     *
     * @see org.eclipse.papyrus.uml.service.types.helper.ObservationEditHelper
     *
     * @param semanticContext
     *            the context in which TimeObservations should be displayed.
     * @return all TimeObservations that can be displayed in the specified context
     */
    public Collection<TimeObservation> getTimeObservationCandidatesCOD(final EObject semanticContext) {
        Collection<TimeObservation> timeObservations = Collections.emptyList();
        if (semanticContext == null) {
            return timeObservations;
        }
        if (semanticContext instanceof Package) {
            EList<PackageableElement> packageElements = ((Package) semanticContext).getPackagedElements();
            timeObservations = packageElements.stream() //
                    .filter(TimeObservation.class::isInstance) //
                    .map(TimeObservation.class::cast) //
                    .collect(Collectors.toList()); //
        } else {
            timeObservations = this.getTimeObservationCandidatesCOD(this.getPackageContainerCOD(semanticContext));
        }
        return timeObservations;
    }

    /**
     * Return the first element in the container hierarchy of the given element which is a {@link Package}.
     *
     * @param element
     *            the starting point
     * @return the first containing element which is a {@link Package}, or <code>null</code> if none
     */
    public Package getPackageContainerCOD(final EObject element) {
        Package pack = null;
        if (element instanceof Package) {
            pack = (Package) element;
        } else if (element != null) {
            EObject parent = element.eContainer();
            if (parent instanceof Package) {
                return (Package) parent;
            } else {
                pack = this.getPackageContainerCOD(parent);
            }
        }
        return pack;
    }

    /**
     * Create a {@link Message}.
     *
     * @param source
     *            the semantic source
     * @param target
     *            the semantic target
     * @param sourceNode
     *            the source node
     * @param targetNode
     *            the target node
     * @param editingContext
     *            the current {@link IEditingContext}
     * @param diagramContext
     *            the current {@link DiagramContext}
     * @return a new Message
     */
    public EObject createMessageCOD(EObject source, EObject target, Node sourceNode, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext) {
        EObject newEdge = this.createDomainBasedEdge(source, target, "uml::Message", UMLPackage.eINSTANCE.getInteraction_Message().getName(), sourceNode, targetNode, editingContext, diagramContext);
        if (newEdge instanceof Message) {
            this.initializeMessage((Message) newEdge, source, target);
        }
        return newEdge;
    }

    /**
     * Initialize new Message between two {@link Lifeline} by creating send/receiveEvent.
     *
     * @param message
     *            the message to initialize,
     * @param source
     *            the {@link Lifeline} source of the message,
     * @param target
     *            the {@link Lifeline} target of the message,
     */
    private void initializeMessage(Message message, EObject source, EObject target) {
        message.setMessageSort(MessageSort.ASYNCH_CALL_LITERAL);
        // set Source
        if (source instanceof Lifeline || source instanceof ExecutionSpecification) {
            MessageOccurrenceSpecification sendEvent = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
            message.getInteraction().getFragments().add(sendEvent);
            sendEvent.setName(message.getName() + "SendEvent");
            sendEvent.setMessage(message);
            message.setSendEvent(sendEvent);
            if (source instanceof Lifeline) {
                sendEvent.setCovered((Lifeline) source);
            } else if (source instanceof ExecutionSpecification) {
                sendEvent.setCovered(((ExecutionSpecification) source).getCovereds().get(0));
            }
        }
        // set Target
        if (target instanceof Lifeline || target instanceof ExecutionSpecification) {
            MessageOccurrenceSpecification receiveEvent = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
            message.getInteraction().getFragments().add(receiveEvent);
            receiveEvent.setName(message.getName() + "ReceiveEvent");
            receiveEvent.setMessage(message);
            message.setReceiveEvent(receiveEvent);
            if (target instanceof Lifeline) {
                receiveEvent.setCovered((Lifeline) target);
            } else if (target instanceof ExecutionSpecification) {
                receiveEvent.setCovered(((ExecutionSpecification) target).getCovereds().get(0));
            }
        }
    }

}

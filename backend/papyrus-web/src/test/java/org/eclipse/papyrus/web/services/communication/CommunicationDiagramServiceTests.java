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
package org.eclipse.papyrus.web.services.communication;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.web.application.representations.aqlservices.communication.CommunicationDiagramService;
import org.eclipse.papyrus.web.application.representations.uml.CODDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UMLMetamodelHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.services.AbstractDiagramTest;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.DurationObservation;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.TimeObservation;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class gathering integration test regarding creation in the Communication Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@SpringBootTest
@WebAppConfiguration
public class CommunicationDiagramServiceTests extends AbstractDiagramTest {
    /**
     *
     */
    private static final String HOLDER_SUFFIX = "_Holder";

    private static final String CONTENT_SUFFIX = "_Content";

    private static final IdBuilder ID_BUILDER = new IdBuilder(CODDiagramDescriptionBuilder.COD_PREFIX, new UMLMetamodelHelper());

    private static final String COD_INTERACTION_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getInteraction());

    private static final String COD_LIFELINE = ID_BUILDER.getDomainNodeName(UML.getLifeline());

    private static final String COD_MESSAGE = ID_BUILDER.getDomainBaseEdgeId(UML.getMessage());

    private Interaction interaction;

    /**
     * Test {@link CommunicationDiagramService#getTimeObservationCandidatesCOD(org.eclipse.emf.ecore.EObject)} from
     * Package with no {@link TimeObservation}.
     */
    @Test
    public void testGetEmptyTimeObservationCandidates() {
        Package pack = this.create(Package.class);
        Collection<TimeObservation> timeObservationCandidates = this.getDiagramService().getTimeObservationCandidatesCOD(pack);
        assertTrue(timeObservationCandidates.isEmpty());
    }

    /**
     * Test {@link CommunicationDiagramService#getTimeObservationCandidatesCOD(org.eclipse.emf.ecore.EObject)} from a
     * {@code null} element.
     */
    @Test
    public void testGetTimeObservationCandidatesFromNullObject() {
        Collection<TimeObservation> timeObservationCandidates = this.getDiagramService().getTimeObservationCandidatesCOD(null);
        assertTrue(timeObservationCandidates.isEmpty());
    }

    /**
     * Test {@link CommunicationDiagramService#getTimeObservationCandidatesCOD(org.eclipse.emf.ecore.EObject)} from
     * Package with {@link TimeObservation}.
     */
    @Test
    public void testGetTimeObservationCandidatesFromPackageWithObservation() {
        Package pack = this.create(Package.class);
        TimeObservation timeObservation = this.create(TimeObservation.class);
        pack.getPackagedElements().add(timeObservation);
        Collection<TimeObservation> timeObservationCandidates = this.getDiagramService().getTimeObservationCandidatesCOD(pack);
        assertFalse(timeObservationCandidates.isEmpty());
        assertEquals(1, timeObservationCandidates.size());
        assertEquals(timeObservation, timeObservationCandidates.stream().findFirst().get());
    }

    /**
     * Test {@link CommunicationDiagramService#getTimeObservationCandidatesCOD(org.eclipse.emf.ecore.EObject)} from
     * Interface in Package with {@link TimeObservation}.
     */
    @Test
    public void testGetTimeObservationCandidatesFromInterfaceInPackageWithObservation() {
        Package pack = this.create(Package.class);
        Interface interfaceUml = this.create(Interface.class);
        TimeObservation timeObservation = this.create(TimeObservation.class);
        pack.getPackagedElements().add(timeObservation);
        pack.getPackagedElements().add(interfaceUml);
        Collection<TimeObservation> timeObservationCandidates = this.getDiagramService().getTimeObservationCandidatesCOD(interfaceUml);
        assertFalse(timeObservationCandidates.isEmpty());
        assertEquals(1, timeObservationCandidates.size());
        assertEquals(timeObservation, timeObservationCandidates.stream().findFirst().get());
    }

    /**
     * Test {@link CommunicationDiagramService#getDurationObservationCandidatesCOD(org.eclipse.emf.ecore.EObject)} from
     * Package with no {@link DurationObservation}.
     */
    @Test
    public void testGetEmptyDurationObservationCandidates() {
        Package pack = this.create(Package.class);
        Collection<DurationObservation> durationObservationCandidates = this.getDiagramService().getDurationObservationCandidatesCOD(pack);
        assertTrue(durationObservationCandidates.isEmpty());
    }

    /**
     * Test {@link CommunicationDiagramService#getDurationObservationCandidatesCOD(org.eclipse.emf.ecore.EObject)} from
     * a {@code null} element.
     */
    @Test
    public void testGetDurationObservationCandidatesFromNullObject() {
        Collection<DurationObservation> durationObservationCandidates = this.getDiagramService().getDurationObservationCandidatesCOD(null);
        assertTrue(durationObservationCandidates.isEmpty());
    }

    /**
     * Test {@link CommunicationDiagramService#getDurationObservationCandidatesCOD(org.eclipse.emf.ecore.EObject)} from
     * Package with {@link DurationObservation}.
     */
    @Test
    public void testGetDurationObservationCandidatesFromPackageWithObservation() {
        Package pack = this.create(Package.class);
        DurationObservation durationObservation = this.create(DurationObservation.class);
        pack.getPackagedElements().add(durationObservation);
        Collection<DurationObservation> durationObservationCandidates = this.getDiagramService().getDurationObservationCandidatesCOD(pack);
        assertFalse(durationObservationCandidates.isEmpty());
        assertEquals(1, durationObservationCandidates.size());
        assertEquals(durationObservation, durationObservationCandidates.stream().findFirst().get());
    }

    /**
     * Test {@link CommunicationDiagramService#getDurationObservationCandidatesCOD(org.eclipse.emf.ecore.EObject)} from
     * Interface in Package with {@link DurationObservation}.
     */
    @Test
    public void testGetDurationObservationCandidatesFromInterfaceInPackageWithObservation() {
        Package pack = this.create(Package.class);
        Interface interfaceUml = this.create(Interface.class);
        DurationObservation durationObservation = this.create(DurationObservation.class);
        pack.getPackagedElements().add(durationObservation);
        pack.getPackagedElements().add(interfaceUml);
        Collection<DurationObservation> durationObservationCandidates = this.getDiagramService().getDurationObservationCandidatesCOD(interfaceUml);
        assertFalse(durationObservationCandidates.isEmpty());
        assertEquals(1, durationObservationCandidates.size());
        assertEquals(durationObservation, durationObservationCandidates.stream().findFirst().get());
    }

    /**
     * Test {@link CommunicationDiagramService#getPackageContainerCOD(org.eclipse.emf.ecore.EObject)} from a
     * {@code null} element.
     */
    @Test
    public void testGetPackageContainerFromNullObject() {
        assertNull(this.getDiagramService().getPackageContainerCOD(null));
    }

    /**
     * Test {@link CommunicationDiagramService#getPackageContainerCOD(org.eclipse.emf.ecore.EObject)} from
     * {@link Package}.
     */
    @Test
    public void testGetPackageContainerFromPackage() {
        Package pack = this.create(Package.class);
        assertEquals(pack, this.getDiagramService().getPackageContainerCOD(pack));
    }

    /**
     * Test {@link CommunicationDiagramService#getPackageContainerCOD(org.eclipse.emf.ecore.EObject)} from
     * {@link Actor}.
     */
    @Test
    public void testGetPackageContainerFromActor() {
        Actor actor = this.create(Actor.class);
        assertNull(this.getDiagramService().getPackageContainerCOD(actor));
    }

    /**
     * Test {@link CommunicationDiagramService#getPackageContainerCOD(org.eclipse.emf.ecore.EObject)} from {@link Actor}
     * in {@link Package}.
     */
    @Test
    public void testGetPackageContainerFromActorInPackage() {
        Package pack = this.create(Package.class);
        Actor actor = this.createIn(Actor.class, pack);
        assertEquals(pack, this.getDiagramService().getPackageContainerCOD(actor));
    }

    /**
     * Test creation of {@link Interaction} Node when Communication diagram is initialized.
     */
    @Test
    public void checkRootInteraction() {
        this.init();

        // check that the Interaction node is created
        Node interactionNode = this.getDiagramHelper().assertGetUniqueMatchingNode(COD_INTERACTION_NODE_NAME + HOLDER_SUFFIX, this.interaction);
        assertNotNull("The Interaction node should be created at the diagram refresh because it is synchronized", interactionNode);

    }

    /**
     * Test
     * {@link CommunicationDiagramService#createMessageCOD(EObject, EObject, Node, Node, org.eclipse.sirius.components.core.api.IEditingContext, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext)}
     * between two {@link Lifeline}. Check initialization of the resulting Message and creation of its edge on the
     * diagram.
     */
    @Test
    public void testCreateMessage() {
        this.init();
        this.getDiagramHelper().assertGetUniqueMatchingNode(COD_INTERACTION_NODE_NAME + HOLDER_SUFFIX, this.interaction);
        Node rootInteractionNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(COD_INTERACTION_NODE_NAME + CONTENT_SUFFIX, this.interaction);

        Lifeline sourceLifeline = this.createIn(Lifeline.class, this.interaction);
        Lifeline targetLifeline = this.createIn(Lifeline.class, this.interaction);
        Node sourceLifelineNode = this.getDiagramHelper().createNodeInParent(COD_LIFELINE, sourceLifeline, rootInteractionNodeContent);
        Node targetLifelineNode = this.getDiagramHelper().createNodeInParent(COD_LIFELINE, targetLifeline, rootInteractionNodeContent);

        this.getServiceTester().buildSynchronizedDomainBasedEdgeCreationTestHelper(ID_BUILDER);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService().createMessageCOD(sourceLifeline, targetLifeline, sourceLifelineNode, targetLifelineNode, this.getEditingContext(), context);

            return aNewElement;
        });

        // check message creation and its initialization
        assertTrue(newElement instanceof Message);
        Message message = (Message) newElement;
        assertEquals(MessageSort.ASYNCH_CALL_LITERAL, message.getMessageSort());
        EList<InteractionFragment> fragments = this.interaction.getFragments();
        assertEquals(2, fragments.size());
        assertTrue(fragments.get(0) instanceof MessageOccurrenceSpecification);
        assertTrue(fragments.get(1) instanceof MessageOccurrenceSpecification);

        MessageOccurrenceSpecification sendEvent = this.getEvent(fragments, "SendEvent");
        assertNotNull(sendEvent);
        MessageOccurrenceSpecification receiveEvent = this.getEvent(fragments, "ReceiveEvent");
        assertNotNull(receiveEvent);

        assertEquals(sourceLifeline, sendEvent.getCovered());
        assertEquals(message, sendEvent.getMessage());
        assertEquals(targetLifeline, receiveEvent.getCovered());
        assertEquals(message, receiveEvent.getMessage());
        assertEquals(sendEvent, message.getSendEvent());
        assertEquals(receiveEvent, message.getReceiveEvent());

        // check edge creation
        this.getDiagramHelper().assertGetExistDomainBasedEdge(COD_MESSAGE, newElement, sourceLifelineNode, targetLifelineNode);
    }

    /**
     * Get Event with given role from list of fragment.
     *
     * @param fragments
     *            list of fragments which contained Events,
     * @param role
     *            role of the Event to extract,
     * @return Event with given role from list of fragment.
     */
    private MessageOccurrenceSpecification getEvent(EList<InteractionFragment> fragments, String role) {
        MessageOccurrenceSpecification sendEvent = null;
        if (((MessageOccurrenceSpecification) fragments.get(0)).getName().endsWith(role)) {
            sendEvent = (MessageOccurrenceSpecification) fragments.get(0);
        }
        if (((MessageOccurrenceSpecification) fragments.get(1)).getName().endsWith(role)) {
            sendEvent = (MessageOccurrenceSpecification) fragments.get(1);
        }
        return sendEvent;
    }

    /**
     * Initialize UML Model and diagram.
     *
     * @return the root of the UML Model.
     */
    private Package init() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);
        this.interaction = this.createIn(Interaction.class, pack);

        this.getDiagramHelper().init(this.interaction, CODDiagramDescriptionBuilder.COD_REP_NAME);
        this.getDiagramHelper().refresh();
        return pack;
    }

    @Override
    protected CommunicationDiagramService buildService() {
        return new CommunicationDiagramService(this.getObjectService(), this.getDiagramNavigationService(), this.getDiagramOperationsService(), e -> true, this.getViewDiagramDescriptionService(),
                new MockLogger());
    }

    @Override
    protected CommunicationDiagramService getDiagramService() {
        return (CommunicationDiagramService) super.getDiagramService();
    }
}

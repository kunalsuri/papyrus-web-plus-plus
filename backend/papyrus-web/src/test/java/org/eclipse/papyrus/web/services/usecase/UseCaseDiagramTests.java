/*****************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.services.usecase;

import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.CONTENT_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.HOLDER_SUFFIX;
import static org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder.UNDERSCORE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.web.application.representations.aqlservices.useCase.UseCaseDiagramService;
import org.eclipse.papyrus.web.application.representations.uml.AbstractRepresentationDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UCDDiagramDescriptionBuilder;
import org.eclipse.papyrus.web.application.representations.uml.UMLMetamodelHelper;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.services.AbstractDiagramTest;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Pin;
import org.eclipse.uml2.uml.UseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class gathering integration test regarding creation in the UseCase Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@SpringBootTest
@WebAppConfiguration
public class UseCaseDiagramTests extends AbstractDiagramTest {

    private static final String TOP_HOLDER_SUFFIX = UNDERSCORE + HOLDER_SUFFIX;

    private static final String TOP_CONTENT_SUFFIX = UNDERSCORE + CONTENT_SUFFIX;

    private static final IdBuilder ID_BUILDER = new IdBuilder(UCDDiagramDescriptionBuilder.UCD_PREFIX, new UMLMetamodelHelper());

    private static final String UCD_INTERACTION_NAME = ID_BUILDER.getDomainNodeName(UML.getInteraction());

    private static final String UCD_PACKAGE_NODE_NAME = ID_BUILDER.getDomainNodeName(UML.getPackage());

    private static final String UCD_USECASE_SHARED_NODE_NAME = ID_BUILDER.getSpecializedDomainNodeName(UML.getUseCase(), AbstractRepresentationDescriptionBuilder.SHARED_SUFFIX);

    /**
     * Test {@link UseCaseDiagramService#getUseCaseCandidatesUCD(org.eclipse.uml2.uml.Element)} from {@link Package}
     * with no {@link UseCase}.
     */
    @Test
    public void testGetEmptyUseCaseCandidates() {
        Package rootPack = this.init();
        assertTrue(this.getDiagramService().getUseCaseCandidatesUCD(rootPack).isEmpty());
    }

    /**
     * Test {@link UseCaseDiagramService#getUseCaseCandidatesUCD(org.eclipse.uml2.uml.Element)} from a {@code null}
     * element.
     */
    @Test
    public void testGetUseCaseCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getUseCaseCandidatesUCD(null).isEmpty());
    }

    /**
     * Test {@link UseCaseDiagramService#getUseCaseCandidatesUCD(org.eclipse.uml2.uml.Element)} from container with
     * {@link UseCase}.
     */
    @Test
    public void testGetUseCaseCandidates() {
        Package rootPack = this.init();
        UseCase useCaseInPackage = this.create(UseCase.class);
        rootPack.getPackagedElements().add(useCaseInPackage);

        // We check UseCase within a Package type
        List<? extends UseCase> useCaseCandidates = this.getDiagramService().getUseCaseCandidatesUCD(rootPack);
        assertEquals(1, useCaseCandidates.size());
        assertTrue(useCaseCandidates.contains(useCaseInPackage));

        Interaction interactionAsSubject = this.create(Interaction.class);
        rootPack.getPackagedElements().add(interactionAsSubject);
        UseCase useCaseInInteraction = this.create(UseCase.class);
        interactionAsSubject.getOwnedUseCases().add(useCaseInInteraction);

        // We check UseCase within a Classifier type
        useCaseCandidates = this.getDiagramService().getUseCaseCandidatesUCD(interactionAsSubject);
        assertEquals(1, useCaseCandidates.size());
        assertTrue(useCaseCandidates.contains(useCaseInInteraction));

        // Check a type that is not supported
        Pin pin = this.create(InputPin.class);
        useCaseCandidates = this.getDiagramService().getUseCaseCandidatesUCD(pin);
        assertEquals(0, useCaseCandidates.size());
    }

    /**
     * Test
     * {@link UseCaseDiagramService#createUseCaseUCD(org.eclipse.uml2.uml.Element, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside a {@link Package}. The containment feature used is UMLPackage.eINSTANCE.getPackage_PackagedElement().
     */
    @Test
    public void testCreateUseCaseInPackage() {
        Package rootPack = this.init();
        Package childPack = this.createIn(Package.class, rootPack);
        this.getDiagramHelper().createNodeInDiagram(UCD_PACKAGE_NODE_NAME + TOP_HOLDER_SUFFIX, childPack);
        org.eclipse.sirius.components.diagrams.Node childPackNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(UCD_PACKAGE_NODE_NAME + TOP_CONTENT_SUFFIX, childPack);

        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createUseCaseUCD(childPack, childPackNodeContent, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check UseCase creation
        assertTrue(newElement instanceof UseCase);
        UseCase useCase = (UseCase) newElement;
        assertEquals(childPack, useCase.eContainer());
        assertTrue(childPack.getPackagedElements().contains(useCase));

        // check node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(UCD_USECASE_SHARED_NODE_NAME, childPackNodeContent, newElement);
    }

    /**
     * Test
     * {@link UseCaseDiagramService#createUseCaseUCD(org.eclipse.uml2.uml.Element, org.eclipse.sirius.components.diagrams.Node, org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext, java.util.Map)}
     * inside an {@link Interaction}. The containment feature used is UMLPackage.eINSTANCE.getClassifier_OwnedUseCase().
     */
    @Test
    public void testCreateUseCaseInInteraction() {

        Package rootPack = this.init();
        Interaction interaction = this.createIn(Interaction.class, rootPack);
        this.getDiagramHelper().createNodeInDiagram(UCD_INTERACTION_NAME + TOP_HOLDER_SUFFIX, interaction);
        org.eclipse.sirius.components.diagrams.Node interactionNodeContent = this.getDiagramHelper().assertGetUniqueMatchingNode(UCD_INTERACTION_NAME + TOP_CONTENT_SUFFIX, interaction);
        EObject newElement = this.getDiagramHelper().modify(context -> {
            EObject aNewElement = this.getDiagramService()//
                    .createUseCaseUCD(interaction, interactionNodeContent, context, this.getDiagramHelper().getConvertedNodes());
            return aNewElement;
        });

        // check UseCase creation
        assertTrue(newElement instanceof UseCase);
        UseCase useCase = (UseCase) newElement;
        assertEquals(interaction, useCase.eContainer());
        assertTrue(interaction.getOwnedUseCases().contains(useCase));

        // check node creation
        this.getDiagramHelper().assertGetUniqueMatchingNodeIn(UCD_USECASE_SHARED_NODE_NAME, interactionNodeContent, newElement);
    }

    /**
     * Initialize UML Model and diagram.
     *
     * @return the root of the UML Model.
     */
    private Package init() {
        Resource resource = this.createResource();
        Package pack = this.createInResource(Package.class, resource);

        this.getDiagramHelper().init(pack, UCDDiagramDescriptionBuilder.UCD_REP_NAME);
        this.getDiagramHelper().refresh();
        return pack;
    }

    @Override
    protected UseCaseDiagramService buildService() {
        return new UseCaseDiagramService(this.getObjectService(), this.getDiagramNavigationService(), this.getDiagramOperationsService(), e -> true, this.getViewDiagramDescriptionService(),
                new MockLogger());
    }

    @Override
    protected UseCaseDiagramService getDiagramService() {
        return (UseCaseDiagramService) super.getDiagramService();
    }
}

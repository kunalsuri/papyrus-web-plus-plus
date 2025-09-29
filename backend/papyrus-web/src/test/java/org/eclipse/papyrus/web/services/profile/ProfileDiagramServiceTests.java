/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.services.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.papyrus.uml.domain.services.EMFUtils;
import org.eclipse.papyrus.web.application.representations.aqlservices.profile.ProfileDiagramService;
import org.eclipse.papyrus.web.services.AbstractDiagramTest;
import org.eclipse.papyrus.web.tests.utils.MockLogger;
import org.eclipse.uml2.uml.Artifact;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ElementImport;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageImport;
import org.eclipse.uml2.uml.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test class gathering integration test regarding creation in the Profile Diagram.
 *
 * @author <a href="mailto:jessy.mallet@obeo.fr">Jessy Mallet</a>
 */
@SpringBootTest
@WebAppConfiguration
public class ProfileDiagramServiceTests extends AbstractDiagramTest {

    /**
     * Test {@link ProfileDiagramService#getMetaclassPRD(org.eclipse.emf.ecore.EObject)} from {@link Profile} with no
     * {@link ElementImport}.
     */
    @Test
    public void testGetEmptyMetaclassCandidates() {
        Profile profile = this.create(Profile.class);
        assertTrue(this.getDiagramService().getMetaclassPRD(profile).isEmpty());
    }

    /**
     * Test {@link ProfileDiagramService#getMetaclassPRD(org.eclipse.emf.ecore.EObject)} from a {@code null} element.
     */
    @Test
    public void testGetMetaclassCandidatesFromNullObject() {
        assertTrue(this.getDiagramService().getMetaclassPRD(null).isEmpty());
    }

    /**
     * Test {@link ProfileDiagramService#getMetaclassPRD(org.eclipse.emf.ecore.EObject)} from container with
     * {@link Artifact}.
     */
    @Test
    public void testGetMetaclassCandidates() {
        // initialize root profile
        Profile profile = this.create(Profile.class);
        PackageImport packageImport = this.create(PackageImport.class);
        Model umlModel = (Model) this.getResourceSet().getEObject(URI.createURI("pathmap://UML_METAMODELS/UML.metamodel.uml#_0"), true);
        packageImport.setImportedPackage(umlModel);

        // create elementImport with Metaclass
        ElementImport elementImport = this.create(ElementImport.class);
        Class metaClass = EMFUtils.allContainedObjectOfType(umlModel, Class.class)//
                .filter(s -> "Actor".equals(s.getName()))//
                .findFirst().get();
        elementImport.setImportedElement(metaClass);
        profile.getElementImports().add(elementImport);

        // check metaclass candidate
        List<? extends Class> metaclassCandidates = this.getDiagramService().getMetaclassPRD(profile);
        assertEquals(1, metaclassCandidates.size());
        assertTrue(metaclassCandidates.contains(metaClass));
    }

    @Override
    protected ProfileDiagramService buildService() {
        return new ProfileDiagramService(this.getIdentityService(), getLabelService(), getObjectSearchService(),
                this.getDiagramNavigationService(), this.getDiagramOperationsService(), e -> true,
                this.getViewDiagramDescriptionService(), null,
                null, null, new MockLogger());
    }

    @Override
    protected ProfileDiagramService getDiagramService() {
        return (ProfileDiagramService) super.getDiagramService();
    }
}

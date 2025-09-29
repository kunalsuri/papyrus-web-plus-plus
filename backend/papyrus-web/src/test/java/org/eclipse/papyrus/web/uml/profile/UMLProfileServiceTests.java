/*******************************************************************************
 * Copyright (c) 2022, 2025 CEA LIST, Obeo.
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
 *******************************************************************************/
package org.eclipse.papyrus.web.uml.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.web.application.profile.PublishProfileSuccessPayload;
import org.eclipse.papyrus.web.application.profile.dto.ApplyProfileInput;
import org.eclipse.papyrus.web.application.profile.dto.ApplyProfileSuccessPayload;
import org.eclipse.papyrus.web.application.profile.dto.PublishProfileInput;
import org.eclipse.papyrus.web.application.profile.dto.UMLProfileVersion;
import org.eclipse.papyrus.web.application.profile.services.api.IUMLProfileService;
import org.eclipse.papyrus.web.domain.boundedcontext.profile.ProfileResourceEntity;
import org.eclipse.papyrus.web.domain.boundedcontext.profile.repositories.IProfileRepository;
import org.eclipse.papyrus.web.utils.AbstractWebUMLTest;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.ProfileApplication;
import org.eclipse.uml2.uml.Stereotype;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Tests the services in charge of using UML Profiles.
 *
 * @author Arthur Daussy
 */
@SpringBootTest
@WebAppConfiguration
public class UMLProfileServiceTests extends AbstractWebUMLTest {

    @Autowired
    private IUMLProfileService profileService;

    @Autowired
    private IProfileRepository profileRepository;

    @AfterEach
    public void removePublishedProfile() {
        this.profileRepository.deleteAll();
    }

    /**
     * Test the application of a static package profile (<i>Standard Profile</i>).
     */
    @Test
    public void testApplyStaticProfile() {
        Model model = this.create(Model.class);

        Class aClass = this.create(Class.class);
        model.getPackagedElements().add(aClass);

        Resource umlResource = this.createResource("r1");
        umlResource.getContents().add(model);

        this.editingDomain.getResourceSet().getResource(URI.createURI("pathmap://UML_PROFILES/Standard.profile.uml"), true);
        IPayload payload = this.profileService.applyProfile(this.getEditingContext(),
                new ApplyProfileInput(UUID.randomUUID(), this.getEditingContext().getId(),
                        this.getIdentityService().getId(model),
                        URI.createURI("pathmap://UML_PROFILES/Standard.profile.uml#_0").toString()));
        assertTrue(payload instanceof ApplyProfileSuccessPayload);

        EList<ProfileApplication> appliedProfiles = model.getProfileApplications();
        assertEquals(1, appliedProfiles.size());

        ProfileApplication appliedProfile = appliedProfiles.get(0);
        assertEquals("StandardProfile", appliedProfile.getAppliedProfile().getName());
    }

    /**
     * Test loading a model with a Profile and Stereotypes from a static profile.
     *
     * @throws IOException
     */
    @Test
    public void testLoadModelWithStaticProfile() throws IOException {

        Resource modelWithProfileResource = this.createResource("modelWithStandardProfile");

        try (var input = new ClassPathResource("profile/UMLModelWithStandardProfile.json").getInputStream()) {
            modelWithProfileResource.load(input, Collections.emptyMap());
        }

        Model model = (Model) modelWithProfileResource.getContents().get(0);

        EList<Profile> appliedProfiles = model.getAllAppliedProfiles();
        assertEquals(1, appliedProfiles.size());

        PackageableElement classOneStereotype = model.getPackagedElement("ClassOneStereotype");
        EList<Stereotype> c1Stereotypes = classOneStereotype.getAppliedStereotypes();
        assertEquals(1, c1Stereotypes.size());
        assertEquals("Utility", c1Stereotypes.get(0).getName());

        PackageableElement classTwoStereotypes = model.getPackagedElement("ClassTwoStereotypes");
        EList<Stereotype> c2Stereotypes = classTwoStereotypes.getAppliedStereotypes();
        assertEquals(2, c2Stereotypes.size());
        assertEquals("Auxiliary", c2Stereotypes.get(0).getName());
        assertEquals("Focus", c2Stereotypes.get(1).getName());

    }

    /**
     * Check the behavior while giving an invalid profile URI resource. => Expected Profile application fails
     */
    @Test
    public void invalidProfileResourceContentInvalidURI() {
        Model model = this.create(Model.class);

        Resource umlResource = this.createResource("r2");
        umlResource.getContents().add(model);

        IPayload payload = this.profileService.applyProfile(this.getEditingContext(),
                new ApplyProfileInput(UUID.randomUUID(), this.getEditingContext().getId(),
                        this.getIdentityService().getId(model), URI.createURI("fake:/tot").toString()));
        assertTrue(payload instanceof ErrorPayload);

        payload = this.profileService.applyProfile(this.getEditingContext(),
                new ApplyProfileInput(UUID.randomUUID(), this.getEditingContext().getId(),
                        this.getIdentityService().getId(model), URI.createURI("fake/test").toString()));
        assertTrue(payload instanceof ErrorPayload);
    }

    /**
     * Test profile publication.
     *
     * @throws IOException
     */
    @Test
    public void testProfilePublication() throws IOException {
        Resource modelWithProfileResource = this.createResource("profileResource");

        try (var input = new ClassPathResource("profile/profile.json").getInputStream()) {
            modelWithProfileResource.load(input, Collections.emptyMap());
        }

        String profileId = "7420affe-b576-4013-b5d2-02cb0f3c48b1";
        String version1 = "1.0.1";
        String version2 = "2.0.1";
        String comment = "comment";
        String copyright = "copyright";
        String date = "date";
        String author = "author";

        IPayload payload = this.profileService.publishProfile(this.getEditingContext(),
                new PublishProfileInput(UUID.randomUUID(), this.getEditingContext().getId(), profileId, version1, comment, copyright, date, author, true));

        assertTrue(payload instanceof PublishProfileSuccessPayload);

        Optional<ProfileResourceEntity> profileResourceEntity = this.profileRepository.findById(UUID.nameUUIDFromBytes(modelWithProfileResource.getURI().lastSegment().getBytes()));
        assertTrue(profileResourceEntity.isPresent());
        profileResourceEntity.get().getContent().contains(profileId);

        Optional<UMLProfileVersion> profileLastVersion = this.profileService.getProfileLastVersion(this.getEditingContext(), profileId);
        assertEquals(new UMLProfileVersion(1, 0, 1), profileLastVersion.get());

        // publish a second time
        payload = this.profileService.publishProfile(this.getEditingContext(),
                new PublishProfileInput(UUID.randomUUID(), this.getEditingContext().getId(), profileId, version2, comment, copyright, date, author, true));

        assertTrue(payload instanceof PublishProfileSuccessPayload);

        profileResourceEntity = this.profileRepository.findById(UUID.nameUUIDFromBytes(modelWithProfileResource.getURI().lastSegment().getBytes()));
        assertTrue(profileResourceEntity.isPresent());
        profileResourceEntity.get().getContent().contains(profileId);

        profileLastVersion = this.profileService.getProfileLastVersion(this.getEditingContext(), profileId);
        assertEquals(new UMLProfileVersion(2, 0, 1), profileLastVersion.get());

        // Then try to apply that profile

        Model model = this.create(Model.class);

        // Get generated nsUri for the profile
        String profileDefinitionNsURI = this.extractNsURI(profileResourceEntity.get().getContent());

        if (profileDefinitionNsURI == null) {
            fail("Unable to extract nsURI of the profile definition");
        }
        Resource umlResource = this.createResource("r2");
        umlResource.getContents().add(model);

        IPayload applyPayload = this.profileService.applyProfile(this.getEditingContext(), new ApplyProfileInput(UUID.randomUUID(), this.getEditingContext().getId(),
                this.getIdentityService().getId(model), URI.createURI(
                        "pathmap://WEB_DYNAMIC_PROFILE/" + profileResourceEntity.get().getId().toString() + "#" + profileId)
                        .toString()));
        assertTrue(applyPayload instanceof ApplyProfileSuccessPayload);

        // Check that the NS URI has been added to the Package Registry
        assertNotNull(this.getEditingDomain().getResourceSet().getPackageRegistry().getEPackage(profileDefinitionNsURI));

    }

    private String extractNsURI(String content) {
        Pattern p = Pattern.compile("http:///schemas/profileNotGenerated/([\\w/_-]*)\"");
        Matcher matcher = p.matcher(content);
        if (matcher.find()) {
            String group = matcher.group(1);
            return "http:///schemas/profileNotGenerated/" + group;
        }
        return null;
    }
}

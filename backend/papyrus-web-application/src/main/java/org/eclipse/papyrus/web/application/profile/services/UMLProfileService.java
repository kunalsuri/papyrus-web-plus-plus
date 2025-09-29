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
package org.eclipse.papyrus.web.application.profile.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.eclipse.papyrus.uml.domain.services.EMFUtils;
import org.eclipse.papyrus.uml.domain.services.profile.DynamicProfileConverter;
import org.eclipse.papyrus.uml.domain.services.profile.ProfileDefinition;
import org.eclipse.papyrus.uml.domain.services.profile.ProfileVersion;
import org.eclipse.papyrus.web.application.profile.PublishProfileSuccessPayload;
import org.eclipse.papyrus.web.application.profile.UMLProfileMetadataRegistry;
import org.eclipse.papyrus.web.application.profile.dto.ApplyProfileInput;
import org.eclipse.papyrus.web.application.profile.dto.ApplyProfileSuccessPayload;
import org.eclipse.papyrus.web.application.profile.dto.DeleteProfileSuccessPayload;
import org.eclipse.papyrus.web.application.profile.dto.PublishProfileInput;
import org.eclipse.papyrus.web.application.profile.dto.UMLProfileMetadata;
import org.eclipse.papyrus.web.application.profile.dto.UMLProfileVersion;
import org.eclipse.papyrus.web.application.profile.services.api.IUMLProfileService;
import org.eclipse.papyrus.web.domain.boundedcontext.profile.ProfileResourceEntity;
import org.eclipse.papyrus.web.domain.boundedcontext.profile.service.api.IProfileCreationService;
import org.eclipse.papyrus.web.domain.boundedcontext.profile.service.api.IProfileDeletionService;
import org.eclipse.papyrus.web.domain.boundedcontext.profile.service.api.IProfileSearchService;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.domain.services.Failure;
import org.eclipse.sirius.web.domain.services.IResult;
import org.eclipse.sirius.web.domain.services.Success;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service used to query the available UML profile metadata.
 *
 * @author lfasani
 */
public class UMLProfileService implements IUMLProfileService {

    /**
     * This prefix is used to reference the dynamic profile.
     */
    public static final String WEB_DYNAMIC_PROFILE_RESOURCE_PREFIX = "pathmap://WEB_DYNAMIC_PROFILE/";

    private static final Logger LOGGER = LoggerFactory.getLogger(UMLProfileService.class);

    private final UMLProfileMetadataRegistry umlRegistry;

    private final IProfileSearchService profileSearchService;

    private final IProfileDeletionService profileDeletionService;

    private final IProfileCreationService profileCreationService;

    private final IIdentityService identityService;

    private Registry factoryRegistry;

    private final IObjectSearchService objectSearchService;

    public UMLProfileService(UMLProfileMetadataRegistry registry, IIdentityService identityService, IProfileSearchService profileSearchService, IProfileDeletionService profileDeletionService,
            IProfileCreationService profileCreationService, Registry factoryRegistry, IObjectSearchService objectSearchService) {
        this.profileDeletionService = Objects.requireNonNull(profileDeletionService);
        this.profileCreationService = Objects.requireNonNull(profileCreationService);
        this.umlRegistry = Objects.requireNonNull(registry);
        this.identityService = Objects.requireNonNull(identityService);
        this.profileSearchService = Objects.requireNonNull(profileSearchService);
        this.factoryRegistry = factoryRegistry;
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UMLProfileMetadata> getAllUMLProfiles() {
        List<UMLProfileMetadata> dynamicProfiles = StreamSupport.stream(this.profileSearchService.findAll().spliterator(), false)
                .flatMap(profileResourceEntity -> this.getDynamicProfileMetadata(profileResourceEntity).stream()).collect(Collectors.toList());

        List<UMLProfileMetadata> profiles = new ArrayList<>(dynamicProfiles);
        profiles.addAll(this.umlRegistry.getUMLProfileDescriptions());
        return profiles;
    }

    private List<UMLProfileMetadata> getDynamicProfileMetadata(ProfileResourceEntity profileResourceEntity) {

        // No need to resoveProxies
        Resource resource = this.createResource(profileResourceEntity.getId().toString());
        try (var inputStream = new ByteArrayInputStream(profileResourceEntity.getContent().getBytes())) {
            resource.load(inputStream, null);
            return EMFUtils.allContainedObjectOfType(resource, Profile.class).map(profile -> {
                String profileId = profile.eResource().getURIFragment(profile);
                EPackage definition = profile.getDefinition();
                EAnnotation eAnnotation = definition.getEAnnotation("PapyrusVersion");
                String version = "";
                if (eAnnotation != null) {
                    version = eAnnotation.getDetails().get("Version");
                }
                return new UMLProfileMetadata(profile.getName(), resource.getURI() + "#" + profileId, version);
            }).toList();
        } catch (IOException exception) {
            LOGGER.warn(exception.getMessage(), exception);
            return Collections.emptyList();
        }

    }

    private void copyAllKeepingId(Collection<EObject> eObjects, Resource resource) {
        if (resource instanceof XMLResourceImpl) {
            XMLResourceImpl xmlResource = (XMLResourceImpl) resource;
            Copier copier = new Copier();
            Collection<EObject> copiedObjects = copier.copyAll(eObjects);
            copier.copyReferences();

            resource.getContents().addAll(copiedObjects);
            for (EObject sourceObject : copier.keySet()) {
                String id = this.identityService.getId(sourceObject);
                xmlResource.setID(copier.get(sourceObject), id);
            }
        }
    }

    private Resource createResource(String resourceId) {
        URI resourceUri = URI.createURI(UMLProfileService.WEB_DYNAMIC_PROFILE_RESOURCE_PREFIX + resourceId);
        Resource.Factory factory = (Factory) this.factoryRegistry.getExtensionToFactoryMap().get("uml");
        return factory.createResource(resourceUri);
    }

    @Override
    public IPayload applyProfile(IEditingContext editingContext, ApplyProfileInput input) {
        String packageUMLId = input.modelId();
        String profileURI = input.profileUriPath();
        IPayload payload = null;
        Optional<Package> umlPackageOptional = this.objectSearchService.getObject(editingContext, packageUMLId)//
                .filter(Package.class::isInstance)//
                .map(Package.class::cast);

        String errorMessage = "The profile application failed";
        if (umlPackageOptional.isPresent()) {
            Package pack = umlPackageOptional.get();

            if (editingContext instanceof IEMFEditingContext) {
                // This call will load the resource in the resourceSet
                try {
                    ResourceSet resourceSet = ((IEMFEditingContext) editingContext).getDomain().getResourceSet();
                    Optional<Profile> umlProfileOptional = Optional.of(resourceSet.getEObject(URI.createURI(profileURI), true)).filter(Profile.class::isInstance)//
                            .map(Profile.class::cast);

                    if (umlProfileOptional.isPresent()) {
                        Profile profile = umlProfileOptional.get();
                        pack.applyProfile(profile);
                        this.registerInPackageRegistry(resourceSet, profile);
                        payload = new ApplyProfileSuccessPayload(input.id());

                        return payload;
                    } else {
                        errorMessage = MessageFormat.format("No profile found with id {0}", profileURI);
                    }
                    // CHECKSTYLE:OFF
                } catch (RuntimeException e) {
                    errorMessage = MessageFormat.format("No profile found with id {0} : {1}", profileURI, e.getMessage());
                }
            }
        } else {
            errorMessage = MessageFormat.format("No Package found with id {0}", packageUMLId);
        }

        LOGGER.error(errorMessage);
        return new ErrorPayload(input.id(), errorMessage);
    }

    private void registerInPackageRegistry(ResourceSet resourceSet, Profile profile) {
        // For non static profile we need to add the definition package to the registry in order to be able to load and
        // create element from the dynamic metamodel
        EPackage definition = profile.getDefinition();
        String nsURI = definition.getNsURI();
        org.eclipse.emf.ecore.EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
        if (packageRegistry.getEPackage(nsURI) == null) {
            packageRegistry.put(nsURI, definition);
        }
    }

    @Override
    public Optional<UMLProfileVersion> getProfileLastVersion(IEditingContext editingContext, String profileId) {
        Optional<UMLProfileVersion> versionOpt = Optional.empty();

        Optional<Profile> profileOpt = this.objectSearchService.getObject(editingContext, profileId)//
                .filter(Profile.class::isInstance)//
                .map(Profile.class::cast);
        //
        if (profileOpt.isPresent()) {
            Optional<ProfileResourceEntity> profileResourceEntityOpt = profileOpt.map(EObject::eResource)//
                    .map(Resource::getURI)//
                    .map(URI::lastSegment)//
                    .map(segment -> UUID.nameUUIDFromBytes(segment.getBytes()))//
                    .flatMap(this.profileSearchService::findById);
            if (profileResourceEntityOpt.isPresent()) {
                versionOpt = profileResourceEntityOpt.flatMap(profileResourceEntity -> {
                    Resource resource = this.createResource(profileResourceEntity.getId().toString());

                    try (var inputStream = new ByteArrayInputStream(profileResourceEntity.getContent().getBytes())) {
                        resource.load(inputStream, null);
                        return this.getLastProfileVersion(resource, profileId);
                    } catch (IOException exception) {
                        LOGGER.warn(exception.getMessage(), exception);
                    }
                    return null;
                });
            } else {
                versionOpt = Optional.of(new UMLProfileVersion(0, 0, 0));
            }
        }

        return versionOpt;
    }

    private Optional<UMLProfileVersion> getLastProfileVersion(Resource resourceOfPublishedProfile, String profileId) {
        return EMFUtils.allContainedObjectOfType(resourceOfPublishedProfile, Profile.class)//
                .filter(profile -> this.matchId(resourceOfPublishedProfile, profileId, profile))//
                .findFirst() //
                .flatMap(this::getVersionFromProfile);
    }

    private boolean matchId(Resource resourceOfPublishedProfile, String profileId, Profile profile) {
        return profileId.equals(resourceOfPublishedProfile.getURIFragment(profile));
    }

    private Optional<UMLProfileVersion> getVersionFromProfile(Profile profile) {
        Optional<UMLProfileVersion> versionOpt = Optional.empty();
        EAnnotation eAnnotationMain = profile.getEAnnotation("http://www.eclipse.org/uml2/2.0.0/UML");
        Optional<EPackage> ePackageOpt = eAnnotationMain.getContents().stream()//
                .filter(EPackage.class::isInstance)//
                .map(EPackage.class::cast)//
                .findFirst();
        if (ePackageOpt.isPresent()) {
            versionOpt = ePackageOpt.map(ePackage -> ePackage.getEAnnotation("PapyrusVersion"))//
                    .map(eAnnotation -> eAnnotation.getDetails().get("Version")) //
                    .map(strVersion -> {
                        UMLProfileVersion profileLastVersion = null;
                        String[] versions = strVersion.split("\\.");
                        if (versions.length == 3) {
                            try {
                                profileLastVersion = new UMLProfileVersion(Integer.parseInt(versions[0]), Integer.parseInt(versions[1]), Integer.parseInt(versions[2]));
                            } catch (NumberFormatException e) {
                                LOGGER.error(
                                        MessageFormat.format("Invalid version format of profile {0} in profile resource with id {0}", profile.getName(), profile.eResource().getURI().lastSegment()));
                            }
                        }

                        return profileLastVersion;
                    });
        } else {
            versionOpt = Optional.of(new UMLProfileVersion(0, 0, 0));
        }
        return versionOpt;
    }

    @Override
    public IPayload publishProfile(IEditingContext editingContext, PublishProfileInput publishProfileInput) {
        Optional<Profile> profileOpt = this.objectSearchService.getObject(editingContext, publishProfileInput.objectId())//
                .filter(Profile.class::isInstance)//
                .map(Profile.class::cast);

        IPayload payload = null;
        if (profileOpt.isPresent()) {
            payload = this.doPublishProfile(editingContext, publishProfileInput, profileOpt.get());
        } else {
            payload = this.buildErrorPublishProfilePayload(publishProfileInput.id(), ". No profile with id " + publishProfileInput.objectId());
        }

        return payload;
    }

    @Override
    public IPayload deletePublishedDynamicProfileByName(String name) {
        StreamSupport.stream(this.profileSearchService.findAll().spliterator(), false).filter(pr -> this.hasName(pr, name)).forEach(pr -> {
            this.profileDeletionService.deleteProfile(pr.getId());
        });
        return new DeleteProfileSuccessPayload(UUID.randomUUID());
    }

    private boolean hasName(ProfileResourceEntity profileResourceEntity, String profileName) {

        // No need to resoveProxies
        Resource resource = this.createResource(profileResourceEntity.getId().toString());
        try (var inputStream = new ByteArrayInputStream(profileResourceEntity.getContent().getBytes())) {
            resource.load(inputStream, null);
            return EMFUtils.allContainedObjectOfType(resource, Profile.class).anyMatch(p -> profileName.equals(p.getName()));
        } catch (IOException exception) {
            LOGGER.warn(exception.getMessage(), exception);
            return false;
        }

    }

    private IPayload doPublishProfile(IEditingContext editingContext, PublishProfileInput publishProfileInput, Profile profile) {
        IPayload payload;
        if (this.isRootProfile(profile)) {
            Boolean isProfileUpdated = this.updateProfileWithEPackage(profile, publishProfileInput);
            if (isProfileUpdated) {
                IResult<ProfileResourceEntity> publicationResult = this.publishProfile(profile, editingContext, publishProfileInput);
                if (publicationResult instanceof Success) {
                    payload = new PublishProfileSuccessPayload(publishProfileInput.id());
                } else {
                    payload = this.buildErrorPublishProfilePayload(publishProfileInput.id(), ". Failed to save the profile.");
                }
            } else {
                payload = this.buildErrorPublishProfilePayload(publishProfileInput.id(), ". Failed to generate the ecore EPackage meta-model.");
            }
        } else {
            payload = this.buildErrorPublishProfilePayload(publishProfileInput.id(), ". The profile is not a root profile.");
        }
        return payload;
    }

    private ErrorPayload buildErrorPublishProfilePayload(UUID inputId, String message) {
        String baseMsg = MessageFormat.format("Failed to publish the dynamic profile of id {0}", inputId);
        return new ErrorPayload(inputId, baseMsg + message);
    }

    private boolean isRootProfile(Profile profile) {
        return profile.eContainer() == null;
    }

    private Boolean updateProfileWithEPackage(Profile profile, PublishProfileInput publishProfileInput) {
        ProfileDefinition profileDefinition = new ProfileDefinition(new ProfileVersion(publishProfileInput.version()), publishProfileInput.comment(), publishProfileInput.copyright(),
                publishProfileInput.date(), publishProfileInput.author());
        if (new DynamicProfileConverter().generateEPackageInProfile(profile, publishProfileInput.saveOCLConstraint(), profileDefinition)) {
            return true;
        }
        return false;
    }

    private IResult<ProfileResourceEntity> publishProfile(Profile profile, IEditingContext editingContext, ICause cause) {
        Resource resourceJson = profile.eResource();
        String resourceId = profile.eResource().getURI().lastSegment();

        Resource outputResource = this.createResource(resourceId);
        this.copyAllKeepingId(resourceJson.getContents(), outputResource);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
            // No need to give options because the UMLResourceFactory properly set the options
            outputResource.save(outputStream, null);
            Optional<byte[]> optionalBytes = Optional.of(outputStream.toByteArray());
            if (optionalBytes.isPresent()) {
                byte[] bytes = optionalBytes.get();
                String content = new String(bytes);
                return this.profileCreationService.createProfile(cause, UUID.nameUUIDFromBytes(resourceId.getBytes()), content);
            }
        } catch (IOException exception) {
            LOGGER.warn(exception.getMessage(), exception);
        }

        return new Failure<>("Unable to publish profile");
    }

}

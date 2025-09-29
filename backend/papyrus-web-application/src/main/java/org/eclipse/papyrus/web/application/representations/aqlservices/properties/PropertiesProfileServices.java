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
package org.eclipse.papyrus.web.application.representations.aqlservices.properties;

import static java.util.stream.Collectors.toSet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.uml.domain.services.profile.ProfileUtil;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger.ILogLevel;
import org.eclipse.papyrus.web.application.profile.dto.ApplyProfileInput;
import org.eclipse.papyrus.web.application.profile.dto.UMLProfileMetadata;
import org.eclipse.papyrus.web.application.profile.services.api.IUMLProfileService;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.ProfileApplication;
import org.eclipse.uml2.uml.Stereotype;

/**
 * Properties Profile services.
 *
 * @author Jerome Gout
 */
public class PropertiesProfileServices {

    private static final String PROFILE_PARENT_ANNOTATION_SOURCE = "http://www.eclipse.org/uml2/2.0.0/UML";

    private final IUMLProfileService profileService;

    private final IIdentityService identityService;

    private final ILogger logger;

    public PropertiesProfileServices(IUMLProfileService profileService, IIdentityService identityService,
            ILogger logger) {
        super();
        this.profileService = profileService;
        this.identityService = identityService;
        this.logger = logger;
    }

    /**
     * Check whether the given package instance is an UML profile definition or not.
     *
     * @param pack
     *            an {@code EPackage} instance
     * @return {@code true} if the given package is an UML profile definition element and {@code false} otherwise.
     */
    public boolean isPackageDefiningProfile(EPackage pack) {
        if (pack.eContainer() instanceof EAnnotation parent) {
            return PROFILE_PARENT_ANNOTATION_SOURCE.equals(parent.getSource());
        }
        return false;
    }

    /**
     * Gets a label for the {@link Stereotype}.
     *
     * @param candidate
     *            a non <code>null</code> {@link Stereotype}
     * @return a label
     */
    public String getStereotypeLabel(Stereotype candidate) {
        return MessageFormat.format("{0} (from {1})", candidate.getName(), candidate.getProfile().getName());
    }

    /**
     * Gets a label for a {@link Stereotype} defined by its qualified name.
     *
     * @param qualifiedName
     *            the qualified name of the stereotype
     * @return a label
     */
    public String getStereotypeLabel(String qualifiedName) {
        String[] parts = qualifiedName.split("::");
        if (parts.length >= 2) {
            int indexProfile = parts.length - 2;
            int indexName = parts.length - 1;
            return MessageFormat.format("{0} (from {1})", parts[indexName], parts[indexProfile]);
        } else {
            return "";
        }
    }

    /**
     * Gets a label for a profile applied on a given package.
     *
     * @param self
     *            the package on which the profile is applied.
     * @param profile
     *            the profile
     * @return a label
     */
    public String getProfileLabel(Package self, Profile profile) {

        String version = this.getAppliedProfileVersion(self, profile);

        String uri = EcoreUtil.getURI(profile).toString();
        String name = this.profileService.getAllUMLProfiles().stream().filter(meta -> meta.getUriPath() != null && uri.equals(meta.getUriPath())).map(meta -> meta.getLabel()).findFirst()
                .orElse(profile.getName());
        if (version != null) {
            name = name + " (" + version + ")";
        }

        return name;
    }

    /**
     * Check if the profile applied on the given package need to be updated.
     *
     * @param self
     *            the Package on which the profile is already applied
     * @param profile
     *            the profile tested
     * @return <code>true</code> if there is a newer version available
     */
    public boolean isProfileNotUpToDate(Package self, Profile profile) {
        return ProfileUtil.isDirty(self, profile);
    }

    /**
     * Gets a label for a profile to be applied on the given package.
     *
     * @param self
     *            the package on which the profile is applied.
     * @param uriPath
     *            the URI path
     * @return a label
     */
    // self is not used here but we need to keep it to match the same signature
    // org.eclipse.papyrus.web.application.representations.aqlservices.properties.PropertiesProfileServices.getAppliedProfileLabel(Package,
    // Profile)
    public String getProfileLabel(Package self, String uriPath) {
        if (uriPath == null || uriPath.isBlank()) {
            return "";
        }
        return this.profileService.getAllUMLProfiles().stream().filter(meta -> uriPath.equals(meta.getUriPath())).map(meta -> this.getProfileLabel(meta)).findFirst().orElse("");
    }

    private String getProfileLabel(UMLProfileMetadata meta) {
        String label = meta.getLabel();
        if (meta.getVersion() != null && !meta.getVersion().isBlank()) {
            label += " (" + meta.getVersion() + ")";
        }
        return label;
    }

    private String getAppliedProfileVersion(Package self, Profile profile) {
        String version = null;
        ProfileApplication profileApplication = self.getProfileApplication(profile);
        if (profileApplication != null) {
            EPackage definition = profileApplication.getAppliedDefinition();
            if (definition != null) {
                EAnnotation eAnnotation = definition.getEAnnotation("PapyrusVersion");
                if (eAnnotation != null) {
                    version = eAnnotation.getDetails().get("Version");
                }
            }
        }
        return version;
    }

    /**
     * Re-apply a the profile on a given package if a newer version is available.
     *
     * @param self
     *            the package
     * @param profile
     *            the profile to re-apply
     * @return the package
     */
    public EObject reapplyProfile(Package self, IEditingContext editingContext, Profile profile) {
        if (ProfileUtil.isDirty(self, profile)) {
            this.applyProfile(self, editingContext, EcoreUtil.getURI(profile).toString());
        } else {
            this.logger.log(MessageFormat.format("No update available on profile {0}", profile.getName()), ILogLevel.INFO);
        }
        return self;
    }

    /**
     * Apply a profile on the given {@link EPackage}.
     *
     * @param self
     *            the package on which the profile needs to be applied
     * @param editingContext
     *            the editing context
     * @param uriPath
     *            the path to profile to apply
     * @return the package
     */
    public EObject applyProfile(org.eclipse.uml2.uml.Package self, IEditingContext editingContext, String uriPath) {
        var input = new ApplyProfileInput(UUID.randomUUID(), editingContext.getId(), this.identityService.getId(self),
                uriPath);
        IPayload payload = this.profileService.applyProfile(editingContext, input);
        if (payload instanceof ErrorPayload error) {
            this.logger.log(error.message(), ILogLevel.ERROR);
        }
        return self;
    }

    /**
     * Returns the list of profile URIs that are not yet applied.
     *
     * @param pack
     *            the package on which the profile will be applied
     * @return a list of URI
     */
    public List<String> getNonAppliedProfilePaths(org.eclipse.uml2.uml.Package pack) {
        Set<String> appliedProfileUris = pack.getAllAppliedProfiles().stream().map(EcoreUtil::getURI).map(Object::toString).collect(toSet());
        List<UMLProfileMetadata> availableProfiles = this.profileService.getAllUMLProfiles();

        return availableProfiles.stream().filter(meta -> !this.isAppliedProfile(appliedProfileUris, meta)).map(UMLProfileMetadata::getUriPath).toList();
    }

    private boolean isAppliedProfile(Set<String> appliedProfileUris, UMLProfileMetadata meta) {
        return appliedProfileUris.contains(URI.createURI(meta.getUriPath()).toString());
    }

    /**
     * Apply a stereotype on a {@link Element} given its qualified name.
     *
     * @param element
     *            an UML element
     * @param qualifiedName
     *            the qualified name of the stereotype
     * @return self
     */
    public EObject applyStereotype(Element element, String qualifiedName) {
        Stereotype stereotype = element.getApplicableStereotype(qualifiedName);
        if (stereotype != null) {
            return element.applyStereotype(stereotype);
        }

        return element;
    }

    /**
     * Gets all applicable stereotypes that are not already applied.
     *
     * @param element
     *            an Element
     * @return a list of stereotypes
     */
    public List<Stereotype> getNonAppliedApplicableStereotypes(Element element) {
        var applicableStereotypes = new ArrayList<>(element.getApplicableStereotypes());
        applicableStereotypes.removeAll(element.getAppliedStereotypes());
        return applicableStereotypes;
    }

    /**
     * Reorder the stereotype application of an element.
     *
     * @param element
     *            the Element targets by the stereotype application
     * @param oldIndex
     *            the old index of the stereotype application
     * @param newIndex
     *            the new index of the stereotype application
     * @return self
     */
    // Copied from org.eclipse.papyrus.uml.tools.commands.ReorderStereotypeApplicationsCommand.prepare()
    public EObject reorderStereotypes(Element element, int oldIndex, int newIndex) {

        EList<Stereotype> stereotypeOrdering = new BasicEList.FastCompare<>(element.getAppliedStereotypes());
        EList<EObject> oldOrdering = new BasicEList.FastCompare<>(element.getStereotypeApplications());

        oldOrdering = new BasicEList.FastCompare<>(element.getStereotypeApplications());
        if (stereotypeOrdering.size() == oldOrdering.size() && oldOrdering.stream().map(EObject::eResource).distinct().count() == 1) {
            EList<EObject> resourceContents = oldOrdering.get(0).eResource().getContents();
            EList<EObject> newOrdering = stereotypeOrdering.stream().map(element::getStereotypeApplication).filter(Objects::nonNull).collect(Collectors.toCollection(BasicEList.FastCompare::new));
            newOrdering.move(newIndex, oldIndex);
            if (newOrdering.size() == oldOrdering.size()) {
                int[] positions = oldOrdering.stream().mapToInt(resourceContents::indexOf).filter(index -> index >= 0).sorted().toArray();
                this.reorderStereotypes(oldOrdering, newOrdering, resourceContents, positions, element);
            }
        }

        return element;
    }

    // Copied from org.eclipse.papyrus.uml.tools.commands.ReorderStereotypeApplicationsCommand.execute()
    private void reorderStereotypes(EList<EObject> oldOrdering, EList<EObject> newOrdering, EList<EObject> resourceContents, int[] positions, Element element) {
        // First, replace all of the stereotype applications with placeholders because when
        // we re-insert them in their new positions, we cannot repeat objects in the list
        EObject[] dummies = IntStream.range(0, oldOrdering.size()).mapToObj(__ -> EcoreFactory.eINSTANCE.createEObject()).toArray(EObject[]::new);
        for (int i = 0; i < dummies.length; i++) {
            resourceContents.set(positions[i], dummies[i]);
        }

        // Remove the stereotype applications from the inverse reference map because otherwise
        // they will be rediscovered in the original order via that map
        oldOrdering.forEach(sa -> org.eclipse.uml2.uml.util.UMLUtil.setBaseElement(sa, null));

        // Then, replace the dummies with the original stereotype applications in their new ordering
        for (int i = 0; i < positions.length; i++) {
            resourceContents.set(positions[i], newOrdering.get(i));

            // Restore the base element reference
            org.eclipse.uml2.uml.util.UMLUtil.setBaseElement(newOrdering.get(i), element);
        }
    }
}

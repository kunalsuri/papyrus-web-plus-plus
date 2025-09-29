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
package org.eclipse.papyrus.web.application.uml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.papyrus.uml.domain.services.EMFUtils;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.web.application.document.services.api.IDocumentExporter;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * Special document exporter than uses {@link UMLResource}.
 *
 * @author Arthur Daussy
 */
@Service
public class UMLDocumentExporter implements IDocumentExporter {

    private final Logger logger = LoggerFactory.getLogger(UMLDocumentExporter.class);

    private final IIdentityService identityService;

    public UMLDocumentExporter(IIdentityService identityService) {
        super();
        this.identityService = Objects.requireNonNull(identityService);
    }

    @Override
    public boolean canHandle(Resource resource, String mediaType) {
        return MediaType.APPLICATION_XML.toString().equals(mediaType)
                && !resource.getContents().isEmpty()
                && resource.getContents().stream()
                        .anyMatch(e -> e instanceof Element);
    }

    @Override
    public Optional<byte[]> getBytes(Resource resource, String mediaType) {
        Optional<byte[]> optionalBytes = Optional.empty();

        XMLResource outputResource = (XMLResource) UMLResourceFactoryImpl.INSTANCE.createResource(resource.getURI());
        outputResource.getContents().addAll(resource.getContents());
        EMFUtils.eAllContentSteamWithSelf(outputResource)
                .forEach(notifier -> this.forceStableId(outputResource, notifier));

        Map<String, Object> options = new HashMap<>();
        options.put(XMIResource.OPTION_ENCODING, JsonResource.ENCODING_UTF_8);
        options.put(XMIResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        options.put(XMIResource.OPTION_USE_XMI_TYPE, Boolean.TRUE);
        options.put(XMIResource.OPTION_EXTENDED_META_DATA, new BasicExtendedMetaData());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
            outputResource.save(outputStream, options);
            optionalBytes = Optional.of(outputStream.toByteArray());
        } catch (IOException exception) {
            this.logger.warn(exception.getMessage(), exception);
        }

        return optionalBytes;
    }

    private void forceStableId(XMLResource outputResource, Notifier notifier) {
        if (notifier instanceof EObject eObject) {
            String id = this.identityService.getId(eObject);
            if (id != null && !id.isEmpty()) {
                outputResource.setID(eObject, id);
            }
        }
    }

}

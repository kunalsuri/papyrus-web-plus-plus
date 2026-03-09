/*******************************************************************************
 * Copyright (c) 2024, 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.web.application.uml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.web.application.document.services.LoadingReport;
import org.eclipse.sirius.web.application.document.services.api.ExternalResourceLoadingResult;
import org.eclipse.sirius.web.application.document.services.api.IExternalResourceLoaderService;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * {@link IExternalResourceLoaderService} in charge of loading UML resources using legacy mechanism (UMLResource and
 * BasicExtendedMetaData) then store the result in a JSonResource.
 *
 * @see https://github.com/eclipse-sirius/sirius-web/issues/3806
 *
 * @author Arthur Daussy
 */
@Service
public class UMLExternalResourceLoaderService implements IExternalResourceLoaderService {

    private final Logger logger = LoggerFactory.getLogger(UMLExternalResourceLoaderService.class);

    public UMLExternalResourceLoaderService() {
    }

    @Override
    public boolean canHandle(InputStream inputStream, URI resourceURI, ResourceSet resourceSet) {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        bufferedInputStream.mark(Integer.MAX_VALUE);
        try (var reader = new BufferedReader(new InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8))) {
            char[] read = new char[500];
            int nb = reader.read(read);
            bufferedInputStream.reset();
            if (nb > 0) {
                char[] content = Arrays.copyOf(read, nb);
                String stringContent = new String(content).trim();
                if (stringContent.startsWith("<?xml") && stringContent.contains("http://www.eclipse.org/uml2/5.0.0/UML")) {
                    return true;
                }
            }
        } catch (IOException exception) {
            this.logger.warn(exception.getMessage(), exception);
        }
        return false;
    }

    @Override
    public Optional<ExternalResourceLoadingResult> getResource(InputStream inputStream, URI resourceURI, ResourceSet resourceSet, boolean applyMigration) {
        Resource jsonResource = null;
        try {
            // Read inside a UMLResource
            Resource xmiResource = UMLResourceFactoryImpl.INSTANCE.createResource(resourceURI.appendSegment("uml"));
            resourceSet.getResources().add(xmiResource);
            Map<Object, Object> loadOption = new HashMap<>(resourceSet.getLoadOptions());
            // Need for the UML ascending compatibility described in
            // https://github.com/eclipse-sirius/sirius-web/issues/3806
            loadOption.put(XMIResource.OPTION_EXTENDED_META_DATA, new BasicExtendedMetaData());
            xmiResource.load(inputStream, loadOption);

            jsonResource = new JSONResourceFactory().createResource(resourceURI);
            resourceSet.getResources().add(jsonResource);
            jsonResource.getContents().addAll(xmiResource.getContents());
            resourceSet.getResources().remove(xmiResource);

        } catch (IOException exception) {
            this.logger.warn(exception.getMessage(), exception);
        }

        if (jsonResource == null) {
            return Optional.empty();
        }
        return Optional.of(new ExternalResourceLoadingResult(jsonResource, new LoadingReport(List.of())));
    }
}

/*****************************************************************************
 * Copyright (c) 2025 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.application.representations.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sirius.components.collaborative.diagrams.api.ICustomNodeStyleDeserializer;
import org.eclipse.sirius.components.diagrams.INodeStyle;
import org.springframework.stereotype.Service;

/**
 * Use to correctly deserialize custom node style.
 *
 * @author ntinsalhi
 */
@Service
public class CustomImageNodeStyleDeserializer implements ICustomNodeStyleDeserializer {

    @Override
    public boolean canHandle(String type) {
        return type.equals(CustomImageNodeStyleProvider.NODE_CUSTOM_IMAGE);
    }

    @Override
    public INodeStyle handle(ObjectMapper mapper, String root) throws JsonProcessingException {
        return mapper.readValue(root, CustomImageNodeStyle.class);
    }
}

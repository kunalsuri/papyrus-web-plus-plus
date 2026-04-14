/*****************************************************************************
 * Copyright (c) 2025, 2026 CEA LIST, Obeo.
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

import org.eclipse.sirius.components.collaborative.diagrams.api.ICustomNodeStyleDeserializer;
import org.eclipse.sirius.components.diagrams.INodeStyle;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.node.ObjectNode;

/**
 * Use to correctly deserialize custom node style.
 *
 * @author ntinsalhi
 */
@Service
public class CuboidNodeStyleDeserializer implements ICustomNodeStyleDeserializer {

    @Override
    public boolean canHandle(String type) {
        return type.equals(CuboidNodeStyleProvider.NODE_CUBOID);
    }

    @Override
    public INodeStyle handle(ObjectNode root, JsonParser jsonParser, DeserializationContext context) throws JacksonException {
        return context.readTreeAsValue(root, CuboidNodeStyle.class);
    }
}

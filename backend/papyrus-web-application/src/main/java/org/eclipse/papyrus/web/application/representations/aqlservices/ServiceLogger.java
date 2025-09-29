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
package org.eclipse.papyrus.web.application.representations.aqlservices;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.springframework.stereotype.Service;

/**
 * Web implementation of {@link ILogger}.
 *
 * @author Arthur Daussy
 */
@Service
public class ServiceLogger implements ILogger {

    private final IFeedbackMessageService feedbackService;

    private final ILabelService labelService;

    public ServiceLogger(IFeedbackMessageService feedbackService, ILabelService labelService) {
        super();
        this.feedbackService = feedbackService;
        this.labelService = labelService;
    }

    @Override
    public void log(String message, ILogLevel level) {

        this.feedbackService.addFeedbackMessage(
                new Message(this.toMessage(message, level), this.toFeedbackLevel(level)));
    }

    private String toMessage(String msg, ILogLevel level) {
        if (level == ILogLevel.DEBUG) {
            return "[DEBUG] " + msg;
        } else {
            return msg;
        }
    }

    private MessageLevel toFeedbackLevel(ILogLevel level) {
        return switch (level) {
            case INFO -> MessageLevel.INFO;
            case DEBUG -> MessageLevel.INFO;
            case WARNING -> MessageLevel.WARNING;
            case ERROR -> MessageLevel.ERROR;
            default -> throw new IllegalArgumentException("Unexpected value: " + level);
        };
    }

    @Override
    public String getLabelForLog(EObject object) {
        StyledString styledLabel = this.labelService.getStyledLabel(object);
        if (styledLabel != null) {
            return styledLabel.toString();
        }
        return "";
    }

}

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
package org.eclipse.papyrus.web.custom.widgets;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.ContainmentReferenceWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.CustomImageWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.LanguageExpressionWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MonoReferenceWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MultiReferenceWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveListWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveRadioWidgetDescription;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.emf.services.api.IEMFKindService;
import org.eclipse.sirius.components.forms.description.AbstractWidgetDescription;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.view.emf.form.api.IFormIdProvider;
import org.eclipse.sirius.components.view.emf.form.converters.widgets.api.IWidgetDescriptionConverter;
import org.eclipse.sirius.components.view.emf.operations.api.IOperationExecutor;
import org.eclipse.sirius.components.view.form.WidgetDescription;
import org.springframework.stereotype.Service;

/**
 * Provides the widget converter needed for the language expression widget.
 *
 * @author Jerome Gout
 */
@Service
public class PapyrusWidgetsConverterProvider implements IWidgetDescriptionConverter {

    private final IObjectService objectService;

    private final IOperationExecutor operationExecutor;

    private final IFeedbackMessageService feedbackMessageService;

    private final IFormIdProvider formIdProvider;

    private final IEMFKindService emfKindService;

    public PapyrusWidgetsConverterProvider(IObjectService objectService, IOperationExecutor operationExecutor, IFeedbackMessageService feedbackMessageService, IFormIdProvider formIdProvider,
            IEMFKindService emfKindService) {
        this.objectService = Objects.requireNonNull(objectService);
        this.operationExecutor = Objects.requireNonNull(operationExecutor);
        this.feedbackMessageService = Objects.requireNonNull(feedbackMessageService);
        this.formIdProvider = Objects.requireNonNull(formIdProvider);
        this.emfKindService = Objects.requireNonNull(emfKindService);
    }

    @Override
    public boolean canConvert(WidgetDescription viewWidgetDescription) {
        boolean canConvert = viewWidgetDescription instanceof ContainmentReferenceWidgetDescription
                || viewWidgetDescription instanceof MonoReferenceWidgetDescription
                || viewWidgetDescription instanceof MultiReferenceWidgetDescription;
        canConvert = canConvert || viewWidgetDescription instanceof LanguageExpressionWidgetDescription
                || viewWidgetDescription instanceof PrimitiveListWidgetDescription
                || viewWidgetDescription instanceof PrimitiveRadioWidgetDescription;
        canConvert = canConvert || viewWidgetDescription instanceof CustomImageWidgetDescription;
        return canConvert;
    }

    @Override
    public Optional<AbstractWidgetDescription> convert(WidgetDescription viewWidgetDescription, AQLInterpreter interpreter) {
        PapyrusWidgetsConverterSwitch papyrusWidgetsConverterSwitch = new PapyrusWidgetsConverterSwitch(interpreter, this.objectService, this.operationExecutor, this.feedbackMessageService,
                this.formIdProvider, this.emfKindService);

        Optional<AbstractWidgetDescription> result = Optional.empty();
        if (viewWidgetDescription instanceof ContainmentReferenceWidgetDescription containmentReferenceWidgetDescription) {
            result = papyrusWidgetsConverterSwitch.caseContainmentReferenceWidgetDescription(containmentReferenceWidgetDescription);
        } else if (viewWidgetDescription instanceof MonoReferenceWidgetDescription monoReferenceWidgetDescription) {
            result = papyrusWidgetsConverterSwitch.caseMonoReferenceWidgetDescription(monoReferenceWidgetDescription);
        } else if (viewWidgetDescription instanceof MultiReferenceWidgetDescription multiReferenceWidgetDescription) {
            result = papyrusWidgetsConverterSwitch.caseMultiReferenceWidgetDescription(multiReferenceWidgetDescription);
        } else if (viewWidgetDescription instanceof LanguageExpressionWidgetDescription languageExpressionWidgetDescription) {
            result = papyrusWidgetsConverterSwitch.caseLanguageExpressionWidgetDescription(languageExpressionWidgetDescription);
        } else if (viewWidgetDescription instanceof PrimitiveListWidgetDescription primitiveListWidgetDescription) {
            result = papyrusWidgetsConverterSwitch.casePrimitiveListWidgetDescription(primitiveListWidgetDescription);
        } else if (viewWidgetDescription instanceof PrimitiveRadioWidgetDescription primitiveRadioWidgetDescription) {
            result = papyrusWidgetsConverterSwitch.casePrimitiveRadioWidgetDescription(primitiveRadioWidgetDescription);
        } else if (viewWidgetDescription instanceof CustomImageWidgetDescription customImageWidgetDescription) {
            result = papyrusWidgetsConverterSwitch.caseCustomImageWidgetDescription(customImageWidgetDescription);
        }
        return result;
    }
}

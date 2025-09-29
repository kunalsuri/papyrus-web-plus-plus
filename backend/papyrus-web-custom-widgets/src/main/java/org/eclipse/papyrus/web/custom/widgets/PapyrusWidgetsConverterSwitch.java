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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 210
 *****************************************************************************/
package org.eclipse.papyrus.web.custom.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.papyrus.web.custom.widgets.languageexpression.LanguageExpressionDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.ClearReferenceOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.ClickReferenceValueOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.ContainmentReferenceWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.CustomImageWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.LanguageExpressionWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MonoReferenceSetOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MonoReferenceUnsetOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MonoReferenceWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MultiReferenceAddOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MultiReferenceRemoveOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MultiReferenceReorderOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.MultiReferenceWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveListAddOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveListDeleteOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveListItemActionOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveListReorderOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveRadioWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.RemoveImageOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.SelectImageOperation;
import org.eclipse.papyrus.web.custom.widgets.papyruswidgets.util.PapyrusWidgetsSwitch;
import org.eclipse.papyrus.web.custom.widgets.primitivelist.PrimitiveListCandidate;
import org.eclipse.papyrus.web.custom.widgets.primitivelist.PrimitiveListWidgetComponent;
import org.eclipse.papyrus.web.custom.widgets.primitivelist.PrimitiveListWidgetDescription;
import org.eclipse.papyrus.web.custom.widgets.primitiveradio.PrimitiveRadioDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.emf.services.api.IEMFKindService;
import org.eclipse.sirius.components.forms.ListStyle;
import org.eclipse.sirius.components.forms.WidgetIdProvider;
import org.eclipse.sirius.components.forms.description.AbstractWidgetDescription;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.interpreter.BooleanValueProvider;
import org.eclipse.sirius.components.interpreter.Result;
import org.eclipse.sirius.components.interpreter.StringValueProvider;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.Operation;
import org.eclipse.sirius.components.view.emf.form.ListStyleProvider;
import org.eclipse.sirius.components.view.emf.form.ViewFormDescriptionConverter;
import org.eclipse.sirius.components.view.emf.form.api.IFormIdProvider;
import org.eclipse.sirius.components.view.emf.operations.api.IOperationExecutor;
import org.eclipse.sirius.components.view.emf.operations.api.OperationExecutionStatus;
import org.eclipse.sirius.components.view.emf.widget.reference.ReferenceWidgetStyleProvider;
import org.eclipse.sirius.components.view.form.ListDescriptionStyle;
import org.eclipse.sirius.components.view.widget.reference.ConditionalReferenceWidgetDescriptionStyle;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescriptionStyle;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetComponent;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetDescription;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetStyle;

/**
 * Converts all view-based Papyrus widget description into its API equivalent.<br>
 * Each custom widget has it own method caseXXX
 *
 * @author Jerome Gout
 */
public class PapyrusWidgetsConverterSwitch extends PapyrusWidgetsSwitch<Optional<AbstractWidgetDescription>> {

    private static final String DELETION_ERROR_MSG = "Something went wrong while handling item deletion.";

    private static final String ITEM_ACTION_ERROR_MSG = "Something went wrong while handling item action.";

    private static final String CLICKING_ERROR_MSG = "Something went wrong while clicking item.";

    private final AQLInterpreter interpreter;

    private final ILabelService labelService;

    private final IIdentityService identityService;

    private final IFeedbackMessageService feedbackMessageService;

    private final Function<VariableManager, String> semanticTargetIdProvider;

    private final IOperationExecutor operationExecutor;

    private final IFormIdProvider widgetIdProvider;

    private final IEMFKindService emfKindService;

    public PapyrusWidgetsConverterSwitch(AQLInterpreter interpreter, ILabelService labelService,
            IOperationExecutor operationExecutor, IFeedbackMessageService feedbackMessageService,
            IFormIdProvider widgetIdProvider, IEMFKindService emfKindService, IIdentityService identityService) {
        this.interpreter = Objects.requireNonNull(interpreter);
        this.labelService = Objects.requireNonNull(labelService);
        this.identityService = Objects.requireNonNull(identityService);
        this.semanticTargetIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(identityService::getId).orElse(null);
        this.operationExecutor = Objects.requireNonNull(operationExecutor);
        this.feedbackMessageService = Objects.requireNonNull(feedbackMessageService);
        this.widgetIdProvider = Objects.requireNonNull(widgetIdProvider);
        this.emfKindService = Objects.requireNonNull(emfKindService);

    }

    @Override
    public Optional<AbstractWidgetDescription> caseCustomImageWidgetDescription(
            CustomImageWidgetDescription customImageDescription) {
        String descriptionId = this.getDescriptionId(customImageDescription);

        var builder = org.eclipse.papyrus.web.custom.widgets.customimage.CustomImageDescription.newCustomImageDescription(
                descriptionId) //
                .idProvider(new WidgetIdProvider()) //
                .labelProvider(variableManager -> this.getCustomImageLabel(customImageDescription, variableManager))//
                .iconURLProvider(variableManager -> List.of()) //
                .currentUuidProvider(this.getStringValueProvider(customImageDescription.getUuidExpression()))
                .newUuidHandler(this.handleOperation(customImageDescription.getSelectImageOperation(),
                        SelectImageOperation::getBody, "Something went wrong while updating the image's uuid."))
                .removeUuidHandler(this.handleOperation(customImageDescription.getRemoveImageOperation(),
                        RemoveImageOperation::getBody, "Something went wrong while removing the image's uuid."))
                .targetObjectIdProvider(this.semanticTargetIdProvider) //
                .isReadOnlyProvider(this.getReadOnlyValueProvider("true"));
        // .isReadOnlyProvider(this.getReadOnlyValueProvider(customImageDescription.getIsEnabledExpression()));

        if (customImageDescription.getHelpExpression() != null && !customImageDescription.getHelpExpression()
                .isBlank()) {
            builder.helpTextProvider(this.getStringValueProvider(customImageDescription.getHelpExpression()));
        }

        return Optional.of(builder.build());
    }

    private String getCustomImageLabel(CustomImageWidgetDescription customImageDescription,
            VariableManager variableManager) {
        return new StringValueProvider(this.interpreter, customImageDescription.getLabelExpression()).apply(
                variableManager);
    }

    @Override
    public Optional<AbstractWidgetDescription> caseLanguageExpressionWidgetDescription(
            LanguageExpressionWidgetDescription languageExpressionDescription) {
        String descriptionId = this.getDescriptionId(languageExpressionDescription);

        var builder = LanguageExpressionDescription.newLanguageExpressionDescription(descriptionId) //
                .idProvider(new WidgetIdProvider()) //
                .labelProvider(variableManager -> this.getLanguageExpressionLabel(languageExpressionDescription,
                        variableManager))//
                .iconURLProvider(variableManager -> List.of()) //
                .targetObjectIdProvider(this.semanticTargetIdProvider) //
                .isReadOnlyProvider(
                        this.getReadOnlyValueProvider(languageExpressionDescription.getIsEnabledExpression()));

        if (languageExpressionDescription.getHelpExpression() != null
                && !languageExpressionDescription.getHelpExpression().isBlank()) {
            builder.helpTextProvider(this.getStringValueProvider(languageExpressionDescription.getHelpExpression()));
        }

        return Optional.of(builder.build());
    }

    private String getDescriptionId(EObject description) {
        return this.widgetIdProvider.getFormElementDescriptionId(description);
    }

    private String getLanguageExpressionLabel(LanguageExpressionWidgetDescription languageExpressionDescription,
            VariableManager variableManager) {
        return new StringValueProvider(this.interpreter, languageExpressionDescription.getLabelExpression()).apply(
                variableManager);
    }

    @Override
    public Optional<AbstractWidgetDescription> casePrimitiveRadioWidgetDescription(
            PrimitiveRadioWidgetDescription primitiveRadioDescription) {
        String descriptionId = this.getDescriptionId(primitiveRadioDescription);

        var builder = PrimitiveRadioDescription.newPrimitiveRadioDescription(descriptionId)//
                .idProvider(new WidgetIdProvider()) //
                .targetObjectIdProvider(this.semanticTargetIdProvider) //
                .labelProvider(
                        variableManager -> this.getPrimitiveRadioLabel(primitiveRadioDescription, variableManager)) //
                .iconURLProvider(variableManager -> List.of()) //
                .isReadOnlyProvider(
                        this.getReadOnlyValueProvider(primitiveRadioDescription.getIsEnabledExpression())) //
                .candidateValueProvider(this.getStringValueProvider(primitiveRadioDescription.getValueExpression())) //
                .candidateListProvider(this.getOptionsProvider(primitiveRadioDescription.getCandidatesExpression())) //
                .newValueHandler(this.getOperationsHandler(primitiveRadioDescription.getBody()));

        if (primitiveRadioDescription.getHelpExpression() != null && !primitiveRadioDescription.getHelpExpression()
                .isBlank()) {
            builder.helpTextProvider(this.getStringValueProvider(primitiveRadioDescription.getHelpExpression()));
        }

        return Optional.of(builder.build());
    }

    private String getPrimitiveListItemId(VariableManager variableManager) {
        String indexKey = variableManager.get(PrimitiveListWidgetComponent.CANDIDATE_INDEX_VARIABLE, Integer.class)//
                .map(Object::toString).orElse("");
        String valueKey = variableManager.get(PrimitiveListWidgetComponent.CANDIDATE_VARIABLE, Object.class)//
                .map(Object::toString).orElse("");
        return UUID.nameUUIDFromBytes((indexKey + "-" + valueKey).getBytes()).toString();
    }

    @Override
    public Optional<AbstractWidgetDescription> casePrimitiveListWidgetDescription(
            org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveListWidgetDescription viewListDescription) {
        String descriptionId = this.getDescriptionId(viewListDescription);
        StringValueProvider labelProvider = this.getStringValueProvider(viewListDescription.getLabelExpression());
        Function<VariableManager, Boolean> isReadOnlyProvider = this.getReadOnlyValueProvider(
                viewListDescription.getIsEnabledExpression());
        Function<VariableManager, List<?>> valueProvider = this.getValuesProvider(
                viewListDescription.getValueExpression());
        Function<VariableManager, String> displayProvider = this.getItemLabelProvider(
                viewListDescription.getDisplayExpression());
        Function<VariableManager, Boolean> isDeletableProvider = variableManager -> viewListDescription.getDeleteOperation() != null;
        Function<VariableManager, String> itemIdProvider = this::getPrimitiveListItemId;
        Function<VariableManager, String> itemKindProvider = variableManger -> "unknown";
        Function<VariableManager, IStatus> itemDeleteHandlerProvider = this.handleOperation(
                viewListDescription.getDeleteOperation(), PrimitiveListDeleteOperation::getBody, DELETION_ERROR_MSG);
        Function<VariableManager, IStatus> itemActionHandlerProvider = this.handleOperation(
                viewListDescription.getItemActionOperation(), PrimitiveListItemActionOperation::getBody,
                ITEM_ACTION_ERROR_MSG);
        BiFunction<VariableManager, String, IStatus> newValueHandlerProvider = this.getNewValueHandler(
                viewListDescription.getAddOperation());

        Function<VariableManager, ListStyle> styleProvider = variableManager -> {
            var effectiveStyle = viewListDescription.getConditionalStyles().stream()//
                    .filter(style -> this.matches(style.getCondition(), variableManager))//
                    .map(ListDescriptionStyle.class::cast).findFirst().orElseGet(viewListDescription::getStyle);
            if (effectiveStyle == null) {
                return null;
            }
            return new ListStyleProvider(effectiveStyle).apply(variableManager);
        };

        PrimitiveListWidgetDescription.Builder builder = PrimitiveListWidgetDescription.newPrimitiveListDescription(
                descriptionId)//
                .idProvider(new WidgetIdProvider())//
                .labelProvider(labelProvider)//
                .iconURLProvider(variableManager -> List.of())//
                .isReadOnlyProvider(isReadOnlyProvider)//
                .itemsProvider(valueProvider)//
                .itemKindProvider(itemKindProvider)//
                .itemDeletableProvider(isDeletableProvider)//
                .itemDeleteHandlerProvider(itemDeleteHandlerProvider)//
                .itemIdProvider(itemIdProvider)//
                .itemLabelProvider(displayProvider)//
                .targetObjectIdProvider(this.semanticTargetIdProvider) //
                .styleProvider(styleProvider)//
                .diagnosticsProvider(variableManager -> List.of())//
                .kindProvider(object -> "")//
                .messageProvider(object -> "");

        if (viewListDescription.getHelpExpression() != null && !viewListDescription.getHelpExpression().isBlank()) {
            builder.helpTextProvider(this.getStringValueProvider(viewListDescription.getHelpExpression()));
        }
        if (viewListDescription.getReorderOperation() != null) {
            builder.reorderHandlerProvider(this.handleOperation(viewListDescription.getReorderOperation(),
                    PrimitiveListReorderOperation::getBody,
                    "Something went wrong while handling list items reordering."));
        }
        if (newValueHandlerProvider != null) {
            builder.newValueHandler(newValueHandlerProvider);
        }
        if (viewListDescription.getCandidatesExpression() != null) {
            builder.candidatesProvider(this.getCandidatesProvider(viewListDescription));
        }
        this.configureItemAction(viewListDescription, itemActionHandlerProvider, builder);

        return Optional.of(builder.build());
    }

    private void configureItemAction(
            org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveListWidgetDescription viewListDescription,
            Function<VariableManager, IStatus> itemActionHandlerProvider,
            PrimitiveListWidgetDescription.Builder builder) {
        PrimitiveListItemActionOperation itemActionOperation = viewListDescription.getItemActionOperation();
        if (itemActionOperation != null) {
            builder.itemActionHandlerProvider(itemActionHandlerProvider);
            builder.itemActionIconURLProvider(this.getStringValueProvider(itemActionOperation.getIconURLExpression()));
            if (itemActionOperation.getPreconditionExpression() != null) {
                builder.itemActionPreconditionHandler(
                        this.getBooleanValueProvider(itemActionOperation.getPreconditionExpression()));
            }
        }
    }

    private Function<VariableManager, List<PrimitiveListCandidate>> getCandidatesProvider(
            org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveListWidgetDescription viewListDescription) {
        return (variableManager) -> {

            final List<Object> candidates = Optional.ofNullable(viewListDescription.getCandidatesExpression())//
                    .map(safeExpression -> this.interpreter.evaluateExpression(variableManager.getVariables(),
                            safeExpression).asObjects()//
                            .orElse(List.of()))
                    .orElse(List.of());

            return candidates.stream()
                    .map(candidate -> this.toPrimitiveCandate(viewListDescription, variableManager, candidate))
                    .toList();
        };
    }

    private PrimitiveListCandidate toPrimitiveCandate(
            org.eclipse.papyrus.web.custom.widgets.papyruswidgets.PrimitiveListWidgetDescription viewListDescription,
            VariableManager variableManager, Object candidate) {
        if (viewListDescription.getDisplayExpression() != null) {
            VariableManager child = variableManager.createChild();
            child.put(PrimitiveListWidgetComponent.CANDIDATE_VARIABLE, candidate);
            String label = this.getItemLabelProvider(viewListDescription.getDisplayExpression()).apply(child);
            return new PrimitiveListCandidate(candidate, label);
        } else {
            return new PrimitiveListCandidate(candidate, candidate.toString());
        }
    }

    private <T extends EObject> Function<VariableManager, IStatus> handleOperation(T operationOwner,
            Function<T, EList<Operation>> bodyProvider, String errorMessage) {
        if (operationOwner == null || bodyProvider.apply(operationOwner).isEmpty()) {
            return variableManager -> new Success();
        }
        return this.handleOperation(bodyProvider.apply(operationOwner), errorMessage);
    }

    private boolean matches(String condition, VariableManager variableManager) {
        return this.interpreter.evaluateExpression(variableManager.getVariables(), condition).asBoolean()
                .orElse(Boolean.FALSE);
    }

    private <T> BiFunction<VariableManager, T, IStatus> getNewValueHandler(PrimitiveListAddOperation addOperation) {
        if (addOperation == null || addOperation.getBody().isEmpty()) {
            return null;
        }
        return (variableManager, newValue) -> {
            VariableManager childVariableManager = variableManager.createChild();
            childVariableManager.put(ViewFormDescriptionConverter.NEW_VALUE, newValue);

            var result = this.operationExecutor.execute(this.interpreter, childVariableManager, addOperation.getBody());
            if (result.status() == OperationExecutionStatus.FAILURE) {
                return this.buildFailureWithFeedbackMessages(
                        "Something went wrong while handling the widget new value.");
            } else {
                return this.buildSuccessWithSemanticChangeAndFeedbackMessages();
            }
        };
    }

    private Success buildSuccessWithSemanticChangeAndFeedbackMessages() {
        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of(), this.feedbackMessageService.getFeedbackMessages());
    }

    private Failure buildFailureWithFeedbackMessages(String technicalMessage) {
        List<Message> errorMessages = new ArrayList<>();
        errorMessages.add(new Message(technicalMessage, MessageLevel.ERROR));
        errorMessages.addAll(this.feedbackMessageService.getFeedbackMessages());
        return new Failure(errorMessages);
    }

    private Function<VariableManager, IStatus> handleOperation(List<Operation> operations, String errorMessage) {
        return (variableManager) -> {
            VariableManager childVariableManager = variableManager.createChild();
            var result = this.operationExecutor.execute(this.interpreter, childVariableManager, operations);
            if (result.status() == OperationExecutionStatus.FAILURE) {
                return this.buildFailureWithFeedbackMessages(errorMessage);
            } else {
                return this.buildSuccessWithSemanticChangeAndFeedbackMessages();
            }
        };
    }

    private Function<VariableManager, String> getItemLabelProvider(String valueExpression) {
        if (valueExpression != null) {
            return this.getStringValueProvider(valueExpression);
        } else {
            return variableManager -> {
                return variableManager.get(PrimitiveListWidgetComponent.CANDIDATE_VARIABLE, Object.class)
                        .map(Object::toString).orElse("");
            };
        }
    }

    private String getPrimitiveRadioLabel(PrimitiveRadioWidgetDescription primitiveRadioDescription,
            VariableManager variableManager) {
        return new StringValueProvider(this.interpreter, primitiveRadioDescription.getLabelExpression()).apply(
                variableManager);
    }

    private Function<VariableManager, List<?>> getOptionsProvider(String expression) {
        String safeExpression = Optional.ofNullable(expression).orElse("");
        return variableManager -> {
            if (safeExpression.isBlank()) {
                return List.of();
            } else {
                return this.interpreter.evaluateExpression(variableManager.getVariables(), safeExpression).asObjects()
                        .orElse(List.of());
            }
        };
    }

    private Function<VariableManager, List<?>> getValuesProvider(String expression) {
        String safeExpression = Optional.ofNullable(expression).orElse("");
        return variableManager -> {
            if (safeExpression.isBlank()) {
                return List.of();
            } else {
                return this.interpreter.evaluateExpression(variableManager.getVariables(), safeExpression).asObjects()
                        .orElse(List.of());
            }
        };
    }

    private Function<VariableManager, IStatus> getOperationsHandler(List<Operation> operations) {
        return variableManager -> {
            var result = this.operationExecutor.execute(this.interpreter, variableManager, operations);
            if (result.status() == OperationExecutionStatus.FAILURE) {
                List<Message> errorMessages = new ArrayList<>();
                errorMessages.add(new Message("Something went wrong while handling the widget operations execution.",
                        MessageLevel.ERROR));
                errorMessages.addAll(this.feedbackMessageService.getFeedbackMessages());
                return new Failure(errorMessages);
            } else {
                return new Success(this.feedbackMessageService.getFeedbackMessages());
            }
        };
    }

    private Function<VariableManager, Boolean> getReadOnlyValueProvider(String expression) {
        return variableManager -> {
            if (expression != null && !expression.isBlank()) {
                Result result = this.interpreter.evaluateExpression(variableManager.getVariables(), expression);
                return result.asBoolean().map(value -> !value).orElse(Boolean.FALSE);
            }
            return Boolean.FALSE;
        };
    }

    private StringValueProvider getStringValueProvider(String valueExpression) {
        String safeValueExpression = Optional.ofNullable(valueExpression).orElse("");
        return new StringValueProvider(this.interpreter, safeValueExpression);
    }

    private BooleanValueProvider getBooleanValueProvider(String valueExpression) {
        String safeValueExpression = Optional.ofNullable(valueExpression).orElse("");
        return new BooleanValueProvider(this.interpreter, safeValueExpression);
    }

    @Override
    public Optional<AbstractWidgetDescription> caseMonoReferenceWidgetDescription(
            MonoReferenceWidgetDescription referenceDescription) {
        String descriptionId = this.getDescriptionId(referenceDescription);

        var builder = ReferenceWidgetDescription.newReferenceWidgetDescription(descriptionId) //
                .targetObjectIdProvider(this.semanticTargetIdProvider) //
                .isReadOnlyProvider(this.getReadOnlyValueProvider(referenceDescription.getIsEnabledExpression()))//
                .idProvider(new WidgetIdProvider()) //
                .labelProvider(variableManager -> new StringValueProvider(this.interpreter,
                        referenceDescription.getLabelExpression()).apply(variableManager)) //
                .optionsProvider(this.getOptionsProvider(referenceDescription.getDropdownOptionsExpression())) //
                .iconURLProvider(variableManager -> List.of()) //
                .itemsProvider(this.getValuesProvider(referenceDescription.getValueExpression())) //
                .itemIdProvider(this::getItemId) //
                .itemKindProvider(this::getItemKind) //
                .itemLabelProvider(this::getItemLabel) //
                .itemIconURLProvider(this::getItemIconURL) //
                .ownerKindProvider(this.getOwnerKindProvider(referenceDescription.getOwnerExpression())) //
                .referenceKindProvider(this.getReferenceKindProvider(referenceDescription.getType())) //
                .isContainmentProvider(variableManager -> false) // containment reference are not handled by this widget
                .isManyProvider(variableManager -> false) // Mono!
                .styleProvider(variableManager -> this.getStyleProvider(variableManager,
                        referenceDescription.getConditionalStyles(), referenceDescription.getStyle())) //
                .ownerIdProvider(variableManager -> this.getOwnerId(variableManager,
                        referenceDescription.getOwnerExpression())) //
                .diagnosticsProvider(variableManager -> List.of()) //
                .kindProvider(object -> "") //
                .messageProvider(object -> "") //
                .clearHandlerProvider(
                        this.handleOperation(referenceDescription.getClearOperation(), ClearReferenceOperation::getBody,
                                "Something went wrong while clearing the reference.")) //
                .itemRemoveHandlerProvider(this.handleOperation(referenceDescription.getUnsetOperation(),
                        MonoReferenceUnsetOperation::getBody,
                        "Something went wrong while unsetting reference value.")) //
                .setHandlerProvider(
                        this.handleOperation(referenceDescription.getSetOperation(), MonoReferenceSetOperation::getBody,
                                "Something went wrong while setting the reference value.")) //
                .addHandlerProvider(variableManager -> null) // not available in mono
                .moveHandlerProvider(variableManager -> null); // not available in mono;

        if (referenceDescription.getHelpExpression() != null && !referenceDescription.getHelpExpression().isBlank()) {
            builder.helpTextProvider(this.getStringValueProvider(referenceDescription.getHelpExpression()));
        }
        return Optional.of(builder.build());
    }

    @Override
    public Optional<AbstractWidgetDescription> caseMultiReferenceWidgetDescription(
            MultiReferenceWidgetDescription referenceDescription) {
        String descriptionId = this.getDescriptionId(referenceDescription);

        var builder = ReferenceWidgetDescription.newReferenceWidgetDescription(descriptionId) //
                .targetObjectIdProvider(this.semanticTargetIdProvider) //
                .idProvider(new WidgetIdProvider()) //
                .isReadOnlyProvider(this.getReadOnlyValueProvider(referenceDescription.getIsEnabledExpression()))//
                .labelProvider(variableManager -> new StringValueProvider(this.interpreter,
                        referenceDescription.getLabelExpression()).apply(variableManager)) //
                .optionsProvider(this.getOptionsProvider(referenceDescription.getDropdownOptionsExpression())) //
                .iconURLProvider(variableManager -> List.of()) //
                .itemsProvider(this.getValuesProvider(referenceDescription.getValueExpression())) //
                .itemIdProvider(this::getItemId) //
                .itemKindProvider(this::getItemKind) //
                .itemLabelProvider(this::getItemLabel) //
                .itemIconURLProvider(this::getItemIconURL) //
                .ownerKindProvider(this.getOwnerKindProvider(referenceDescription.getOwnerExpression())) //
                .referenceKindProvider(this.getReferenceKindProvider(referenceDescription.getType())) //
                .isContainmentProvider(variableManager -> false) // containment reference are not handled by this widget
                .isManyProvider(variableManager -> true) // Multi!
                .styleProvider(variableManager -> this.getStyleProvider(variableManager,
                        referenceDescription.getConditionalStyles(), referenceDescription.getStyle())) //
                .ownerIdProvider(variableManager -> this.getOwnerId(variableManager,
                        referenceDescription.getOwnerExpression())) //
                .diagnosticsProvider(variableManager -> List.of()) //
                .kindProvider(object -> "") //
                .messageProvider(object -> "") //
                .clearHandlerProvider(
                        this.handleOperation(referenceDescription.getClearOperation(), ClearReferenceOperation::getBody,
                                "Something went wrong while clearing the reference.")) //
                .itemRemoveHandlerProvider(this.handleOperation(referenceDescription.getRemoveOperation(),
                        MultiReferenceRemoveOperation::getBody, DELETION_ERROR_MSG)) //
                .setHandlerProvider(variableManager -> null) // not available in multi
                .addHandlerProvider(this.handleOperation(referenceDescription.getAddOperation(),
                        MultiReferenceAddOperation::getBody, "Something went wrong while handling item addition."))//
                .moveHandlerProvider(this.handleOperation(referenceDescription.getReorderOperation(),
                        MultiReferenceReorderOperation::getBody,
                        "Something went wrong while handling items reordering."));

        if (referenceDescription.getHelpExpression() != null && !referenceDescription.getHelpExpression().isBlank()) {
            builder.helpTextProvider(this.getStringValueProvider(referenceDescription.getHelpExpression()));
        }
        return Optional.of(builder.build());
    }

    private ReferenceWidgetStyle getStyleProvider(VariableManager variableManager,
            EList<ConditionalReferenceWidgetDescriptionStyle> conditionalStyles,
            ReferenceWidgetDescriptionStyle style) {
        var effectiveStyle = conditionalStyles.stream()
                .filter(condStyle -> this.interpreter.evaluateExpression(variableManager.getVariables(),
                        condStyle.getCondition()).asBoolean().orElse(Boolean.FALSE))
                .map(ReferenceWidgetDescriptionStyle.class::cast).findFirst().orElse(style);
        if (effectiveStyle == null) {
            return null;
        }
        return new ReferenceWidgetStyleProvider(effectiveStyle).apply(variableManager);
    }

    private Optional<Object> getItem(VariableManager variableManager) {
        return variableManager.get(ReferenceWidgetComponent.ITEM_VARIABLE, Object.class);
    }

    private String getItemLabel(VariableManager variableManager) {
        return this.getItem(variableManager).map(this::getLabel).orElse("");
    }

    private String getLabel(Object input) {
        StyledString styledString = this.labelService.getStyledLabel(input);
        if (styledString != null) {
            return styledString.toString();
        }
        return "";
    }

    private List<String> getItemIconURL(VariableManager variableManager) {
        return this.getItem(variableManager).map(this.labelService::getImagePaths).orElse(List.of());
    }

    private String getItemKind(VariableManager variableManager) {
        return this.getItem(variableManager).map(this.identityService::getKind).orElse("");
    }

    private String getItemId(VariableManager variableManager) {
        return this.getItem(variableManager).map(this.identityService::getId).orElse("");
    }

    private EObject getReferenceOwner(VariableManager variableManager, String referenceOwnerExpression) {
        String safeValueExpression = Optional.ofNullable(referenceOwnerExpression).orElse("");
        EObject referenceOwner = variableManager.get(VariableManager.SELF, EObject.class).orElse(null);
        if (!safeValueExpression.isBlank()) {
            Result result = this.interpreter.evaluateExpression(variableManager.getVariables(), safeValueExpression);
            referenceOwner = result.asObject().filter(EObject.class::isInstance).map(EObject.class::cast)
                    .orElse(referenceOwner);
        }
        return referenceOwner;
    }

    private String getOwnerId(VariableManager variableManager, String ownerExpression) {
        EObject owner = this.getReferenceOwner(variableManager, ownerExpression);
        return this.identityService.getId(owner);
    }

    private Function<VariableManager, String> getOwnerKindProvider(String ownerExpression) {
        return variableManager -> {
            return this.emfKindService.getKind(this.getReferenceOwner(variableManager, ownerExpression).eClass());
        };
    }

    private Function<VariableManager, String> getReferenceKindProvider(String domainTypeExpression) {
        return variableManager -> {
            var editingDomain = variableManager.get(IEditingContext.EDITING_CONTEXT, IEMFEditingContext.class)
                    .map(IEMFEditingContext::getDomain);
            String type = this.interpreter.evaluateExpression(variableManager.getVariables(), domainTypeExpression)
                    .asString().orElse("");
            return editingDomain.flatMap(ed -> this.resolveType(ed, type))
                    .flatMap(eclass -> Optional.of(this.emfKindService.getKind(eclass))).orElse("");
        };
    }

    private Optional<EClass> resolveType(EditingDomain editingDomain, String domainType) {
        String[] parts = domainType.split("(::?|\\.)");
        if (parts.length == 2) {
            return editingDomain.getResourceSet().getPackageRegistry().values().stream() //
                    .filter(EPackage.class::isInstance) //
                    .map(EPackage.class::cast) //
                    .filter(ePackage -> Objects.equals(ePackage.getName(), parts[0])) //
                    .map(ePackage -> ePackage.getEClassifier(parts[1])) //
                    .filter(EClass.class::isInstance) //
                    .map(EClass.class::cast) //
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AbstractWidgetDescription> caseContainmentReferenceWidgetDescription(
            ContainmentReferenceWidgetDescription description) {
        String descriptionId = this.getDescriptionId(description);

        var builder = org.eclipse.papyrus.web.custom.widgets.containmentreference.ContainmentReferenceWidgetDescription.newContainmentReferenceWidgetDescription(
                descriptionId) //
                .targetObjectIdProvider(this.semanticTargetIdProvider) //
                .idProvider(new WidgetIdProvider()) //
                .labelProvider(variableManager -> new StringValueProvider(this.interpreter,
                        description.getLabelExpression()).apply(variableManager)) //
                .iconURLProvider(variableManager -> List.of()) //
                .itemsProvider(this.getValuesProvider(description.getValueExpression())) //
                .itemIdProvider(this::getItemId) //
                .itemKindProvider(this::getItemKind) //
                .itemLabelProvider(this::getItemLabel) //
                .itemIconURLProvider(this::getItemIconURL) //
                .ownerKindProvider(this.getOwnerKindProvider(description.getOwnerExpression())) //
                .referenceKindProvider(this.getReferenceKindProvider(description.getType())) //
                .isManyProvider(variableManager -> description.isMany()).styleProvider(
                        variableManager -> this.getStyleProvider(variableManager, description.getConditionalStyles(),
                                description.getStyle())) //
                .ownerIdProvider(
                        variableManager -> this.getOwnerId(variableManager, description.getOwnerExpression())) //
                .diagnosticsProvider(variableManager -> List.of()) //
                .kindProvider(object -> "") //
                .messageProvider(object -> "") //
                .itemClickHandlerProvider(
                        this.handleOperation(description.getClickOperation(), ClickReferenceValueOperation::getBody,
                                CLICKING_ERROR_MSG)) //
                .itemRemoveHandlerProvider(
                        this.handleOperation(description.getRemoveOperation(), MultiReferenceRemoveOperation::getBody,
                                DELETION_ERROR_MSG));
        if (description.getReorderOperation() != null) {
            builder.moveHandlerProvider(
                    this.handleOperation(description.getReorderOperation(), MultiReferenceReorderOperation::getBody,
                            "Something went wrong while handling items reordering."));
        }

        if (description.getHelpExpression() != null && !description.getHelpExpression().isBlank()) {
            builder.helpTextProvider(this.getStringValueProvider(description.getHelpExpression()));
        }
        return Optional.of(builder.build());
    }
}

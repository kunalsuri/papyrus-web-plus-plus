/*****************************************************************************
 * Copyright (c) 2023, 2026 CEA LIST, Obeo.
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
package org.eclipse.papyrus.web.properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Predicate;

import org.eclipse.papyrus.web.application.templates.documents.UMLStereotypeProvider;
import org.eclipse.papyrus.web.application.templates.projects.UMLProjectTemplateProvider;
import org.eclipse.papyrus.web.custom.widgets.primitivelist.PrimitiveListWidget;
import org.eclipse.papyrus.web.utils.AbstractWebUMLTest;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateDocumentMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateProjectMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateRepresentationMutationRunner;
import org.eclipse.papyrus.web.utils.mutations.PapyrusCreateRootObjectCreateMutationRunner;
import org.eclipse.sirius.components.collaborative.editingcontext.EditingContextEventProcessorRegistry;
import org.eclipse.sirius.components.collaborative.forms.dto.FormEventInput;
import org.eclipse.sirius.components.collaborative.forms.dto.FormRefreshedEventPayload;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.forms.AbstractWidget;
import org.eclipse.sirius.components.graphql.api.IEventProcessorSubscriptionProvider;
import org.eclipse.sirius.web.domain.boundedcontexts.project.repositories.IProjectRepository;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

/**
 * Integration tests for the Primitive List custom widget. Depends on {@link PrimitiveListViewRepresentationDescriptionProvider} to register the "Primitive Widget Test" form description.
 *
 * @author Arthur Daussy
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PrimitiveListWidgetIntegrationTests extends AbstractWebUMLTest {

    @Autowired
    private PapyrusCreateRootObjectCreateMutationRunner rootElementCreator;

    @Autowired
    private PapyrusCreateProjectMutationRunner projectCreator;

    @Autowired
    private PapyrusCreateDocumentMutationRunner documentCreator;

    @Autowired
    private IProjectRepository projectRepository;

    @Autowired
    private EditingContextEventProcessorRegistry editingContextEventProcessorRegistry;

    @Autowired
    private PapyrusCreateRepresentationMutationRunner representationCreator;

    @Autowired
    private IEventProcessorSubscriptionProvider eventProcessorSubscriptionProvider;

    private String editingContextId;

    private String rootObjectId;

    private String representationId;

    private String documentId;

    @BeforeEach
    public void setup() {
        String projectId = this.projectCreator.createProject("Instance", UMLProjectTemplateProvider.EMPTY_UML_TEMPLATE);
        this.editingContextId = getEditingContext(projectId).getId();
        this.documentId = this.documentCreator.createDocument(this.editingContextId, "test.uml", UMLStereotypeProvider.EMPTY_UML);
        this.rootObjectId = this.rootElementCreator.createRootObject(UMLPackage.eNS_URI, "Model", this.documentId,
                this.editingContextId.toString());
        this.representationId = this.representationCreator.createRepresentation(this.editingContextId, this.rootObjectId,
                "PrimitiveListe.view", "Form");
    }

    @AfterEach
    public void teardown() {
        this.editingContextEventProcessorRegistry.dispose();
        this.projectRepository.deleteAll();
    }

    @Test
    @DisplayName("Can instantiate a View-based FormDescription which uses the Primitive List custom widget")
    public void simpleFormWithPrimitiveList() {
        var input = new FormEventInput(UUID.randomUUID(), this.editingContextId, this.representationId);

        var payloadFlux = this.eventProcessorSubscriptionProvider.getSubscription(this.editingContextId, this.representationId.toString(), input);
        Predicate<IPayload> isFormWithPrimitiveListRefreshedEventPayload = payload -> {
            if (payload instanceof FormRefreshedEventPayload formRefreshedEventPayload) {
                return this.checkEvent(formRefreshedEventPayload);
            }
            return false;
        };

        StepVerifier.create(payloadFlux)
                .thenConsumeWhile(payload -> !(payload instanceof FormRefreshedEventPayload))
                .expectNextMatches(isFormWithPrimitiveListRefreshedEventPayload)
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

    private boolean checkEvent(FormRefreshedEventPayload formRefreshedEventPayload) {
        var form = formRefreshedEventPayload.form();
        var group = form.getPages().get(0).getGroups().get(0);

        AbstractWidget list1 = group.getWidgets().get(0);
        if (list1 instanceof PrimitiveListWidget primList) {
            this.assertNotEmpty(primList.getId(), "Invalid widget id " + primList.getId());
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param id
     */
    private void assertNotEmpty(String id, String message) {
        assertNotNull(id, message);
        assertFalse(id.isBlank(), message);
    }
}

/*****************************************************************************
 * Copyright (c) 2024, 2026 CEA LIST.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.web.explorer;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.jayway.jsonpath.JsonPath;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.papyrus.uml.domain.services.labels.UMLCharacters;
import org.eclipse.papyrus.web.application.explorer.PapyrusTreeFilterProvider;
import org.eclipse.papyrus.web.application.explorer.builder.UMLDefaultTreeDescriptionBuilder;
import org.eclipse.papyrus.web.application.explorer.builder.aqlservices.UMLDefaultTreeServices;
import org.eclipse.papyrus.web.data.SimpleUMLProjectIdentifiers;
import org.eclipse.papyrus.web.utils.AbstractIntegrationTest;
import org.eclipse.sirius.components.collaborative.trees.dto.TreeRefreshedEventPayload;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.sirius.components.core.api.labels.StyledStringFragment;
import org.eclipse.sirius.components.core.api.labels.StyledStringFragmentStyle;
import org.eclipse.sirius.components.core.api.labels.UnderLineStyle;
import org.eclipse.sirius.components.graphql.tests.api.GraphQLResult;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.components.view.emf.tree.ITreeIdProvider;
import org.eclipse.sirius.components.view.tree.TreeDescription;
import org.eclipse.sirius.web.application.views.explorer.ExplorerEventInput;
import org.eclipse.sirius.web.application.views.explorer.services.ExplorerDescriptionProvider;
import org.eclipse.sirius.web.tests.graphql.ExplorerDescriptionsQueryRunner;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.sirius.web.tests.services.explorer.ExplorerEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.services.representation.RepresentationIdBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import reactor.test.StepVerifier;

/**
 * Test class for the default UML explorer.
 *
 * @author Arthur Daussy
 */
@Transactional
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UMLDefaultExplorerTests extends AbstractIntegrationTest {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private ExplorerEventSubscriptionRunner treeEventSubscriptionRunner;

    @Autowired
    private RepresentationIdBuilder representationIdBuilder;

    @Autowired
    private ExplorerDescriptionsQueryRunner explorerDescriptionsQueryRunner;

    @Autowired
    private ITreeIdProvider treeIdProvider;

    private String treeDescriptionId;

    @BeforeEach
    public void beforeEach() {
        this.treeDescriptionId = this.treeIdProvider.getId((TreeDescription) new UMLDefaultTreeDescriptionBuilder().createView().getDescriptions().get(0));
        this.givenInitialServerState.initialize();
    }

    @Test
    @DisplayName("Check the number of available explorer description for a UML project")
    @Sql(scripts = { "/scripts/uml-model-import-and-profile.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = { "/scripts/cleanup.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void checkAvailableExplorerOnUMLProject() {

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Map<String, Object> explorerVariables = Map.of(
                "editingContextId", SimpleUMLProjectIdentifiers.UML_DEFAULT_EDITING_CONTEXT_ID.toString());
        GraphQLResult explorerResult = this.explorerDescriptionsQueryRunner.run(explorerVariables);
        List<String> explorerIds = JsonPath.read(explorerResult.data(), "$.data.viewer.editingContext.explorerDescriptions[*].id");
        assertThat(explorerIds).hasSize(2);
        assertThat(explorerIds.get(0)).isEqualTo(this.treeDescriptionId);
        assertThat(explorerIds.get(1)).isEqualTo(ExplorerDescriptionProvider.DESCRIPTION_ID);

    }

    @Test
    @DisplayName("Check default UML explorer with read only filter disable")
    @Sql(scripts = { "/scripts/uml-model-import-and-profile.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = { "/scripts/cleanup.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void checkDefaultUMLRulesFilterDisable() {

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        var explorerRepresentationId = this.representationIdBuilder.buildExplorerRepresentationId(this.treeDescriptionId,
                List.of(SimpleUMLProjectIdentifiers.MODEL_DOCUMENT_ID.toString()), List.of());

        var input = new ExplorerEventInput(UUID.randomUUID(), SimpleUMLProjectIdentifiers.UML_DEFAULT_EDITING_CONTEXT_ID.toString(), explorerRepresentationId);
        var flux = this.treeEventSubscriptionRunner.run(input).flux();

        var initialTreeContentConsumer = this.getTreeSubscriptionConsumer(tree -> {
            assertThat(tree).isNotNull();
            assertThat(tree.getChildren()).hasSize(7);
            this.hasLabelWithDefaultStyle(tree.getChildren().get(0).getLabel(), "Ecore.metamodel.uml");
            this.hasLabelWithDefaultStyle(tree.getChildren().get(1).getLabel(), "Ecore.profile.uml");
            this.hasLabelWithDefaultStyle(tree.getChildren().get(2).getLabel(), "EcorePrimitiveTypes.library.uml");
            this.hasLabelWithDefaultStyle(tree.getChildren().get(3).getLabel(), "Model.uml");
            this.hasLabelWithDefaultStyle(tree.getChildren().get(4).getLabel(), "Standard.profile.uml");
            this.hasLabelWithDefaultStyle(tree.getChildren().get(5).getLabel(), "UML.metamodel.uml");
            this.hasLabelWithDefaultStyle(tree.getChildren().get(6).getLabel(), "UMLPrimitiveTypes.library.uml");

        });

        StepVerifier.create(flux)
                .consumeNextWith(initialTreeContentConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(10));

    }

    private void hasLabelWithDefaultStyle(StyledString value, String expectedLabel) {
        assertThat(value.styledStringFragments().stream().map(StyledStringFragment::text).collect(joining())).isEqualTo(expectedLabel);
        value.styledStringFragments().stream().map(StyledStringFragment::styledStringFragmentStyle)
                .allMatch(style -> style.equals(StyledStringFragmentStyle.newDefaultStyledStringFragmentStyle().build()));
    }

    private void hasReadOnlyLabel(StyledString value, String expectedLabel) {
        assertThat(value.styledStringFragments().stream().map(StyledStringFragment::text).collect(joining())).isEqualTo(expectedLabel);
        value.styledStringFragments().stream().map(StyledStringFragment::styledStringFragmentStyle)
                .allMatch(style -> style.getForegroundColor().equals(UMLDefaultTreeServices.READ_ONLY_COLOR));
    }

    @Test
    @DisplayName("Check default explorer with read only filter enable")
    @Sql(scripts = { "/scripts/uml-model-import-and-profile.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = { "/scripts/cleanup.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void checkDefaultUMLRulesFilterEnable() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        var explorerRepresentationId = this.representationIdBuilder.buildExplorerRepresentationId(this.treeDescriptionId,
                // Expanded elements
                List.of(SimpleUMLProjectIdentifiers.MODEL_DOCUMENT_ID.toString(),
                        SimpleUMLProjectIdentifiers.CLASS_ELEMENT_ID.toString(),
                        SimpleUMLProjectIdentifiers.MODEL_ELEMENT_ID.toString(),
                        SimpleUMLProjectIdentifiers.IMPORTED_PRIMITIVETYPES_PACKAGE_ELEMENT_ID,
                        SimpleUMLProjectIdentifiers.PACKAGE_IMPORT_ELEMENT_ID.toString()),
                List.of(PapyrusTreeFilterProvider.HIDE_PATHMAP_URI_TREE_ITEM_FILTER_ID));

        var input = new ExplorerEventInput(UUID.randomUUID(), SimpleUMLProjectIdentifiers.UML_DEFAULT_EDITING_CONTEXT_ID.toString(), explorerRepresentationId);
        var flux = this.treeEventSubscriptionRunner.run(input).flux();

        var initialTreeContentConsumer = this.getTreeSubscriptionConsumer(tree -> {
            assertThat(tree).isNotNull();
            assertThat(tree.getChildren()).hasSize(1);
            TreeItem modelResourceTreeItem = tree.getChildren().get(0);
            // Check also there is no stereotype application
            this.hasLabelWithDefaultStyle(modelResourceTreeItem.getLabel(), "Model.uml");

            assertThat(modelResourceTreeItem.getChildren()).hasSize(1);
            TreeItem modelTreeItem = modelResourceTreeItem.getChildren().get(0);
            this.hasLabelWithDefaultStyle(modelTreeItem.getLabel(), "Model");

            // Expect children
            // 1 - diagram
            // 2 - Package import
            // 3 - One Class with the "Type" stereotype"
            // 4 - Profile application of standard profile
            assertThat(modelTreeItem.getChildren()).hasSize(4);

            // Diagram
            TreeItem diagramTreeItem = modelTreeItem.getChildren().get(0);
            assertThat(diagramTreeItem.getKind()).isEqualTo("siriusComponents://representation?type=Diagram");

            // Package Import
            TreeItem packageImportTreeItem = modelTreeItem.getChildren().get(1);
            this.hasLabelWithDefaultStyle(packageImportTreeItem.getLabel(), "<Package Import> PrimitiveTypes");
            assertThat(packageImportTreeItem.getChildren()).hasSize(1);

            // Imported package
            TreeItem importedPackageTreeItem = packageImportTreeItem.getChildren().get(0);
            assertThat(importedPackageTreeItem.getId()).isEqualTo(SimpleUMLProjectIdentifiers.IMPORTED_PRIMITIVETYPES_PACKAGE_ELEMENT_ID);
            this.hasReadOnlyLabel(importedPackageTreeItem.getLabel(), UMLCharacters.ST_LEFT + "EPackage, ModelLibrary" + UMLCharacters.ST_RIGHT + " PrimitiveTypes");

            // Imported package content
            assertThat(importedPackageTreeItem.getChildren()).hasSize(7);

            // Stereotyped class
            TreeItem classTreeItem = modelTreeItem.getChildren().get(2);
            StyledString classLabel = classTreeItem.getLabel();

            StyledStringFragment stereoptypeFragment = classLabel.styledStringFragments().get(0);
            assertThat(stereoptypeFragment.text()).isEqualTo(UMLCharacters.ST_LEFT + "Type" + UMLCharacters.ST_RIGHT + " ");
            assertThat(stereoptypeFragment.styledStringFragmentStyle().getForegroundColor()).isEqualTo(UMLDefaultTreeServices.STEREOTYPE_APPLICATION_COLOR);

            StyledStringFragment classFragment = classLabel.styledStringFragments().get(1);
            assertThat(classFragment.text()).isEqualTo("Class1");
            assertThat(classFragment.styledStringFragmentStyle().isItalic()).isTrue();

            // Check static prop
            assertThat(classTreeItem.getChildren()).hasSize(1);

            TreeItem propTreeItem = classTreeItem.getChildren().getFirst();
            StyledStringFragment propFragment = propTreeItem.getLabel().styledStringFragments().getFirst();
            assertThat(propFragment.text()).isEqualTo("prop1");
            assertThat(propFragment.styledStringFragmentStyle().getUnderlineStyle()).isEqualTo(UnderLineStyle.SOLID);

            // Profile application
            TreeItem profileApplicationTreeItem = modelTreeItem.getChildren().get(3);
            this.hasLabelWithDefaultStyle(profileApplicationTreeItem.getLabel(), "<Profile Application> StandardProfile");

        });

        StepVerifier.create(flux)
                .consumeNextWith(initialTreeContentConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(10));

    }

    private Consumer<Object> getTreeSubscriptionConsumer(Consumer<Tree> treeConsumer) {
        return object -> Optional.of(object)
                .filter(TreeRefreshedEventPayload.class::isInstance)
                .map(TreeRefreshedEventPayload.class::cast)
                .map(TreeRefreshedEventPayload::tree)
                .ifPresentOrElse(treeConsumer, () -> fail("Missing tree"));
    }
}

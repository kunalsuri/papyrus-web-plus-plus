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
import { v4 as uuid } from 'uuid';
const url = Cypress.env('baseAPIUrl') + '/api/graphql';
/**
 * Rename a project with a new name
 *
 * @param projectId id of the project to rename
 * @param newProjectName the new name of the project
 */
Cypress.Commands.add('renameProject', (projectId, newProjectName) => {
  const query = `
  mutation renameProject($input: RenameProjectInput!) {
    renameProject(input: $input) {
      __typename
    }
  }
  `;
  const variables = {
    input: {
      id: crypto.randomUUID(),
      projectId: projectId,
      newName: newProjectName,
    },
  };

  const body = {
    query,
    variables,
  };
  return cy.request({
    method: 'POST',
    url,
    body,
  });
});

/**
 * Creates a project from a template and create a document from a stereotype.
 */
Cypress.Commands.add('createProjectFromStereotype', (projectName, templateName, stereotypeName, resourceName, libraryIds = []) => {
  cy.createProject(projectName, templateName, libraryIds).then((res) => {
    const payload = res.body.data?.createProject;
     if (!payload || !payload.project?.id) {
       throw new Error(
        `createProject failed:\n${JSON.stringify(res.body, null, 2)}`
       );
     }

    const projectId = payload.project.id;

    cy.renameProject(projectId, projectName).then((res) => {
      cy.wrap(projectId).as('projectId');
      cy.visit(`/projects/${projectId}/edit`).then(() => {
        cy.getByTestId('new-model').click();
        cy.getByTestId('name-input').type(resourceName);
        cy.getByTestId('stereotype').click();
        cy.get('li').contains(stereotypeName).click();
        cy.getByTestId('create-document').click();
        cy.expandAll(resourceName);
      });
    });
  });
});

/*
 * Create a project from a template.
 */
Cypress.Commands.add('createProjectWithName', (context, projectName, templateName, libraryIds) => {
    cy.createProject(projectName, templateName, libraryIds).then((res) => {
    context.projectId = res.body.data.createProject.project.id;
    cy.renameProject(context.projectId, projectName).then((res) => {
      return cy.visit(`/projects/${context.projectId}/edit`);
    });
  });
});

/**
 * Create a new project and upload the model4test.uml. Then it may select a element and select a tab.
 * Note that the new project id is added to the context in the field "projectId".
 *
 * @param context a context object use to inject the project id
 * @param projectName the name of the project
 * @param elementToSelect an optional element to select
 * @param tabToSelect an optional tab to select
 */
Cypress.Commands.add('createTestProject', (context, projectName, elementToSelect, tabToSelect) => {
  context.projectId = cy.createProjectWithName(context, projectName, 'EmptyUMLTemplate').then((res) => {
    cy.visit(`/projects/${context.projectId}/edit`).then((res) => {
      cy.getByTestId('upload-document-icon').click();
      cy.fixture('model4test.uml', { mimeType: 'text/xml' }).as('model4test');
      return cy
        .getByTestId('file')
        .selectFile(
          {
            contents: '@model4test',
            fileName: 'model4test.uml', // workaround for selectFile issue https://github.com/cypress-io/cypress/issues/21936
          },
          { force: true }
        )
        .then(() => {
          cy.getByTestId('upload-document-split-button').should('be.visible').should('not.be.disabled').click();
          cy.getByTestId('upload-document-close').should('be.visible').should('not.be.disabled').click();
          cy.expandAll('model4test.uml');

          if (elementToSelect) {
            cy.getByTestId(elementToSelect).should('exist').click();
            if (tabToSelect) {
              cy.activateDetailsTab(tabToSelect);
            }
          }
        });
    });
  });
});

/**
 * Create a new project and upload DynamicProfileTypeTests.profile.uml. Then it may publish the given profile.
 * Note that the new project id is added to the context in the field "profileProjectId".
 *
 * @param context a context object use to inject the project id
 * @param projectName the name of the project
 * @param elementToSelect an optional element to select
 * @param tabToSelect an optional tab to select
 */
Cypress.Commands.add('createTestProfileProject', (context, projectName, profileName, publishProfile = true) => {
  // first load and publish the profile
  cy.createProjectWithName(context, projectName, 'EmptyUMLTemplate').then((res) => {
    context.profileProjectId = context.projectId;
    cy.visit(`/projects/${context.profileProjectId}/edit`).then((res) => {
      cy.getByTestId('upload-document-icon').click();
      cy.fixture('DynamicProfileTypeTests.profile.uml', { mimeType: 'text/xml' }).as('DynamicProfileTypeTests');
      cy.getByTestId('file')
        .selectFile(
          {
            contents: '@DynamicProfileTypeTests',
            fileName: 'DynamicProfileTypeTests.profile.uml', // workaround for selectFile issue https://github.com/cypress-io/cypress/issues/21936
          },
          { force: true }
        )
        .then(() => {
          cy.getByTestId('upload-document-split-button').should('be.visible').should('not.be.disabled').click();
          cy.getByTestId('upload-document-close').should('be.visible').should('not.be.disabled').click();

          cy.getByTestId('DynamicProfileTypeTests.profile.uml-more').should('exist').click();
          cy.getByTestId('rename-tree-item').should('be.visible').click();
          cy.getByTestId('DynamicProfileTypeTests.profile.uml')
            .get('input')
            .should('exist')
            .clear()
            .type(profileName + '.profile.uml{enter}');

          cy.expandAll(profileName + '.profile.uml');

          cy.getByTestId('DynamicProfileTypeTests').should('be.visible').first().click();
          cy.getByTestId('input-Name').should('be.visible').clear().type(profileName);

          // Lose focus
          cy.getByTestId('page-tab-UML').should('be.visible').click();

          cy.getByTestId(`${profileName}-more`).first().click();

          if (publishProfile) {
            cy.getByTestId('publish-profile').should('be.visible').click();
            cy.getByTestId('publish-profile-dialog')
              .findByTestId('publish-profile-author')
              .find('input')
              .type('Jerome');
            cy.getByTestId('publish-profile-publish')
              .should('not.have.class', 'Mui-disabled')
              .should('be.visible')
              .click();
          }
        });
    });
  });
});

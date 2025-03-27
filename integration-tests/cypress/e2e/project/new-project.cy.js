/*******************************************************************************
 * Copyright (c) 2021, 2024 Obeo.
 * This program and the accompanying materials
 * are made available under the erms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/

const projectName = 'Cypress Project - new-project';

describe('/new/project', () => {
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    cy.visit('/new/project');
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  it('contains a proper project creation form', () => {
    cy.getByTestId('name').should('have.attr', 'type', 'text');
    cy.getByTestId('name').should('have.attr', 'name', 'name');
    cy.getByTestId('name').should('have.attr', 'placeholder', 'Enter the project name');
  });

  it('focuses the name textfield automatically', () => {
    cy.focused().should('have.attr', 'data-testid', 'name');
  });

  it('requires a name', () => {
    cy.getByTestId('create-project').should('be.disabled');
  });

  it('requires a valid name', () => {
    cy.get('h2').contains('Create a new project').should('exist');
    cy.getByTestId('name').type('Cy');
    cy.getByTestId('create-project').should('be.disabled');

    cy.getByTestId('name').type(projectName);
    cy.getByTestId('create-project').should('be.enabled');
  });

  it('navigates to the edit project view on successful project creation with enter', () => {
    cy.get('h2').contains('Create a new project').should('exist');
    cy.getByTestId('name').type(`${projectName}{enter}`);

    cy.url().should('match', new RegExp(Cypress.config().baseUrl + '/projects/[a-z0-9-]*/edit'));
  });

  it('navigates to the edit project view on successful project creation by clicking on the create button', () => {
    cy.get('h2').contains('Create a new project').should('exist');
    cy.getByTestId('name').type(projectName);
    cy.getByTestId('create-project').click();

    cy.url().should('match', new RegExp(Cypress.config().baseUrl + '/projects/[a-z0-9-]*/edit'));
  });
});

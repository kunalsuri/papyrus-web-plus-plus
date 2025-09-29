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
const projectName = `Cypress Project - projects`;

describe('/projects', () => {
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    cy.visit('/projects');
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  it('contains a link to the new project page', () => {
    cy.getByTestId('create').should('be.visible');
  });

  it('contains a link to the upload project page', () => {
    cy.getByTestId('upload').should('be.visible');
  });

  it('contains the list of projects', () => {
    cy.createProject(projectName).then(() => cy.reload());
    cy.get('table').find('tr').contains(projectName).should('be.visible');
  });
});

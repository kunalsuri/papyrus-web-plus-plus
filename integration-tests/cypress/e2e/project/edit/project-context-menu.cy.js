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
import { Project } from '../../../pages/Project';

const projectName = 'Cypress Project - project-context-menu';

describe('/projects/:projectId/edit - Project Context Menu', () => {
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    cy.createProject(projectName).then((res) => {
      const projectId = res.body.data.createProject.project.id;
      new Project().visit(projectId);
    });
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  it('shows the project context menu and hides it by clicking outside', () => {
    let projectNavBar  = new Project().getProjectNavigationBar(projectName);
    projectNavBar.getRenameButton().should('be.visible');
    cy.get('body').click();
    projectNavBar.getRenameButton().should('not.exist');
  });

  it('shows the project context menu and hides it by typing esc', () => {
    let projectNavBar  = new Project().getProjectNavigationBar(projectName);
    projectNavBar.getRenameButton().should('be.visible');
    cy.get('body').type('{esc}');
    projectNavBar.getRenameButton().should('not.exist');
  });

  it('contains a download link', () => {
    cy.getByTestId('more').click();
    cy.getByTestId('download-link').should('have.attr', 'href');
  });

  it('can open the delete project modal', () => {
    let projectNavBar  = new Project().getProjectNavigationBar(projectName);
    projectNavBar.getDeleteButton().click();

    cy.get('.MuiDialog-container').should('be.visible');
  });

  it('can delete a project', () => {
    let projectNavBar  = new Project().getProjectNavigationBar(projectName);
    projectNavBar.getDeleteButton().click();

    cy.url().should('match', new RegExp(Cypress.config().baseUrl + '/projects'));
  });
});

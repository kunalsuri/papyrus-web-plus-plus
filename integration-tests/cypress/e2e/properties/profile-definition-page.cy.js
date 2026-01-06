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

const projectName = 'Profile';

describe('Profile definition page test', () => {
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    // create a new project with profile UML template
    const templateId = 'DefaultProfileWithPrimitiveAndUml';
    cy.createProject(projectName, templateId)
      .then((res) => {
        const projectId = res.body.data.createProject.project.id;
        cy.visit(`/projects/${projectId}/edit`);
      })
      .then(() => {
        cy.expandAll('Profile.profile.uml');
      });
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  it('published profile is properly displayed in Definition page', () => {
    // publish the profile

    cy.getByTestId('Profile-more').should('be.visible').click();
    cy.getByTestId('publish-profile').should('be.visible').click();
    cy.getByTestId('publish-profile-dialog').findByTestId('publish-profile-author').find('input').type('John Doe');
    cy.getByTestId('publish-profile-dialog')
      .findByTestId('publish-profile-comment')
      .find('textarea')
      .first()
      .type('comment content');
    cy.getByTestId('publish-profile-dialog')
      .findByTestId('publish-profile-copyright')
      .find('textarea')
      .first()
      .type('copyright content');
    cy.getByTestId('publish-profile-dialog').findByTestId('publish-profile-publish').should('be.visible').click();

    // check that the Definition page contains the entered data
    cy.activateDetailsTabAndWaitForElement('Definition', 'Author')
      .find('input')
      .should('be.visible')
      .and('be.disabled')
      .and('have.value', 'John Doe');
    cy.getByTestId('Copyright')
      .find('input')
      .should('be.visible')
      .and('be.disabled')
      .and('have.value', 'copyright content');
    cy.getByTestId('Comment')
      .find('textarea')
      .first()
      .should('be.visible')
      .and('be.disabled')
      .and('have.value', 'comment content');
  });

  it('published profile can be removed', () => {
    // publish the profile twice
    cy.getByTestId('Profile-more').should('be.visible').click();
    cy.getByTestId('publish-profile').should('be.visible').click();
    cy.getByTestId('publish-profile-dialog').findByTestId('publish-profile-publish').should('be.visible').click();

    cy.getByTestId('Profile-more').should('be.visible').click();
    cy.getByTestId('publish-profile').should('be.visible').click();
    cy.getByTestId('publish-profile-dialog').findByTestId('publish-profile-publish').should('be.visible').click();

    // check that there are two profile definition
    cy.activateDetailsTabAndWaitForElement('Definition', 'form').as('definition');
    cy.get('@definition').children().eq(1).children().should('have.length', 2);

    // remove 0.0.1
    cy.get('@definition').children().eq(1).children().eq(1).find('button').click();

    // check that there are two profile definitions
    cy.get('@definition').children().eq(1).children().should('have.length', 1);
  });
});

/*******************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/

const projectName = 'Cypress Project - applied-comments';
const context = {};

describe('Applied Comments tests', () => {
  /**
   * For each test, we start with a fresh new project containing all concepts gathered in one single model
   */
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    cy.createTestProject(context, projectName);
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  it('From Comment.annotatedElement', () => {
    // set that Comment is annotating Class
    cy.getByTestId('Comment').should('be.visible').click();
    cy.getByTestId('Annotated element').find('.MuiAutocomplete-endAdornment').find('button').click();
    cy.get('.MuiAutocomplete-popper')
      .find('ul')
      .find('li')
      .filter((index, li) => {
        return Cypress.$(li).text().trim() === 'Class'; // Strictly matching the text 'Class'
      })
      .scrollIntoView()
      .should('be.visible')
      .click();
    // check that class as the correct comment in appliedComment list
    cy.getByTestId('Class').should('be.visible').click();
    // wait the class is properly loaded
    cy.getByTestId('view-Details').findByTestId('Name').should('be.visible');
    cy.getByTestId(`page-tab-Comments`).click();
    cy.getByTestId(`page-tab-Comments`).should('have.class', 'Mui-selected');
    cy.getByTestId('view-Details').find('*[data-testid^="group-"]').as('comments');
    cy.getByTestId('Applied comments').should('be.visible').find('.MuiChip-root').should('have.length', 1);
    cy.get('@comments').findByTestId('reference-value-Comment').should('be.visible');
    // remove Comment from Applied Comments
    cy.get('@comments').findByTestId('reference-value-Comment').find('svg').should('be.visible').click();
    // check that Comment no longer references Class in annotatedElement
    cy.getByTestId('Comment').should('be.visible').click();
    cy.getByTestId('view-Details')
      .findByTestId('Annotated element')
      .should('be.visible')
      .find('.MuiChip-root')
      .should('have.length', 0);
  });

  it('From Class.appliedComments', () => {
    cy.getByTestId('Class').should('be.visible').click();
    // wait the class is properly loaded
    cy.getByTestId('view-Details').findByTestId('Name').should('be.visible');
    cy.getByTestId(`page-tab-Comments`).click();
    cy.getByTestId(`page-tab-Comments`).should('have.class', 'Mui-selected');
    cy.getByTestId('view-Details')
      .find('*[data-testid^="group-"]')
      .findByTestId('Applied comments')
      .should('be.visible')
      .type('Comment{downArrow}{enter}');
    // check comment.annotatedElement is containing Class
    cy.getByTestId('Comment').should('be.visible').click();
    cy.getByTestId('view-Details').as('uml');
    cy.get('@uml').findByTestId('Annotated element').find('.MuiChip-root').should('have.length', 1);
    cy.get('@uml').findByTestId('Annotated element').findByTestId('reference-value-Class').should('be.visible');
    // remove Class element from annotatedElement
    cy.get('@uml').findByTestId('Annotated element-clear').scrollIntoView().should('be.visible').click();
    // check that class has no longer Comment in appliedComments
    cy.getByTestId('Class').should('be.visible').click();
    // wait the class is properly loaded
    cy.getByTestId('view-Details').findByTestId('Name').should('be.visible');
    cy.getByTestId(`page-tab-Comments`).click();
    cy.getByTestId(`page-tab-Comments`).should('have.class', 'Mui-selected');
    cy.getByTestId('view-Details')
      .find('*[data-testid^="group-"]')
      .findByTestId('Applied comments')
      .find('.MuiChip-root')
      .should('have.length', 0);
  });

  it('Create new comment from applied comments', () => {
    cy.getByTestId('Class').should('be.visible').click();
    cy.getByTestId(`page-tab-Comments`).click();
    cy.getByTestId(`page-tab-Comments`).should('have.class', 'Mui-selected');
    cy.getByTestId('view-Details').find('*[data-testid^="group-"]').findByTestId('Applied comments-add').click();
    cy.getByTestId('create-modal').should('be.visible').as('dialog');
    cy.get('@dialog').findByTestId('model4test.uml-toggle').click();
    cy.get('@dialog').findByTestId('model4test-toggle').click();
    cy.get('@dialog').findByTestId('Activity').click();
    cy.wait(500);
    cy.get('@dialog')
      .findByTestId('childCreationDescription')
      .children('[role="combobox"]')
      .contains('Comment (in ownedComment)')
      .click();
    cy.wait(500);
    cy.get('[data-value="ownedComment::Comment"]').should('be.visible').click();
    cy.get('@dialog').findByTestId('create-object').should('be.visible').should('not.be.disabled').click();
    // check that empty comment has been created
    cy.getByTestId('Applied comments').should('be.visible').find('.MuiChip-root').should('have.length', 1);
    cy.getByTestId('Applied comments').findByTestId('reference-value-Comment').click();

    // click on new Comment
    cy.getByTestId('reference-value-Comment').click();
    // Set a body to the comment
    cy.getByTestId('input-Body').type('Comment{downArrow}{enter}');
    // check that Activity has a new child
    cy.checkChildren('Activity', ['Comment']);
    // check that comment is annotating Class
    cy.getByTestId('view-Details').as('uml');
    cy.get('@uml').findByTestId('Annotated element').find('.MuiChip-root').should('have.length', 1);
    cy.get('@uml').findByTestId('Annotated element').findByTestId('reference-value-Class').should('be.visible');
  });
});

/*****************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo, Artal Technologies.
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
const projectName = 'Cypress Project - custom image';
const imageName = 'test-custom-image-widget';
const imageFullName = imageName + '.svg';
const imagePath = 'cypress/assets/' + imageFullName;

describe('Custom Image tests', () => {
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

  const testNoImage = () => {
    cy.getByTestId(`custom-image-widget-no-image`).should('be.visible');
  };

  it('Custom Image custom widget tests', () => {
    // Switch to regular explorer so that the annotation may be visible
    cy.getByTestId('tree-descriptions-menu-icon').should('be.visible').click();
    cy.get('ul')
      .get('li')
      .eq(2) /*.contains('Explorer')*/
      .click({ force: true });
    cy.getByTestId('model4test.uml-more').click();
    cy.getByTestId('expand-all').click();
    // Select Symbol page
    cy.getByTestId('FunctionBehavior').should('be.visible').click();
    cy.getByTestId(`page-tab-Symbol`).click();
    cy.getByTestId(`page-tab-Symbol`).should('have.class', 'Mui-selected');
    // Upload the image
    testNoImage();
    cy.getByTestId('custom-image-widget-add').should('be.visible').click();
    cy.log('imageFullName: ', imageFullName);
    cy.getByTestId('file').selectFile(imagePath, { force: true });
    cy.getByTestId('file').its('0.files').should('have.length', 1).its('0.name').should('eq', imageFullName);
    cy.getByTestId('upload-image').should('be.visible').click();
    // Select the image
    cy.getByTestId('custom-image-widget-select').should('be.visible').click();
    cy.getByTestId('select-image-select').should('be.visible').click();
    cy.getByTestId(`select-image-select-${imageFullName}`).click();
    cy.getByTestId('select-image-confirm').should('be.visible').click();
    cy.getByTestId('custom-image-Symbol').should('be.visible').find('img').should('have.attr', 'src');
    // Check the created EAnnotation in the project explorer
    cy.expandAll('FunctionBehavior');
    cy.getByTestId('Symbol').should('be.visible').click();
    cy.getByTestId('input-Value').should('be.visible').should('contain', '/api/images/');
    // Select Symbol page
    cy.getByTestId('FunctionBehavior').should('be.visible').click();
    cy.wait(250); // Make sure that the pages updates properly following the previous click
    cy.getByTestId(`page-tab-Symbol`).click();
    cy.getByTestId(`page-tab-Symbol`).should('have.class', 'Mui-selected');
    // Remove the image
    cy.getByTestId('custom-image-widget-remove').should('be.visible').click();
    testNoImage();
    cy.getByTestId('org.eclipse.papyrus').should('not.exist');
  });
});

/*****************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo.
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

const projectName = 'Cypress Project - multi-reference-widget';
const context = {};

describe('Multi-valued reference widget tests', () => {
  /**
   * For each test, we start with a fresh new project containing all concepts gathered in one single model
   */
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    cy.createTestProject(context, projectName).then(() => {
      cy.getByTestId('FunctionBehavior').should('be.visible').click();
    });
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  it('add reference values with dropdown and remove one value', () => {
    cy.activateDetailsTabAndWaitForElement('UML', 'Use case');
    cy.checkDropdownContent('Use case', ['UseCase1', 'UseCase2']);
    // add UseCase1
    cy.selectDropdownValue('Use case', 'UseCase1');
    // check that UseCase1 is not longer in the dropdown
    cy.checkDropdownContent('Use case', ['UseCase2']);
    // add UseCase2
    cy.selectDropdownValue('Use case', 'UseCase2');
    // check UseCase1 and UseCase2 are in the reference value list
    cy.getByTestId('reference-value-UseCase1').should('be.visible');
    cy.getByTestId('reference-value-UseCase2').should('be.visible');
    // remove UseCase1
    cy.getByTestId('reference-value-UseCase1').should('be.visible').find('svg').click();
    // check that it is no longer there
    cy.getByTestId('reference-value-UseCase1').should('not.exist');
    // but UseCase2 is still there
    cy.getByTestId('reference-value-UseCase2').should('be.visible');
  });

  it('manage reference values with dialog', () => {
    // open ... dialog
    cy.activateDetailsTabAndWaitForElement('UML', 'Use case-more').click();
    cy.get('[role="dialog"]').as('dialog');
    // two roots Primitive types and model4test, expand model4test
    cy.get('@dialog').findByTestId('tree-root-elements').find('li').should('have.length', 2);
    cy.get('@dialog').findByTestId('model4test-toggle').should('be.visible').click();
    // select UseCase1
    cy.get('@dialog').findByTestId('UseCase1').click();
    // add UseCase1
    cy.get('@dialog').findByTestId('move-right').click();
    // check UseCase1 has been transferred to the right panel
    cy.get('@dialog').findByTestId('selected-items-list').children().should('have.length', 1);
    cy.get('@dialog').findByTestId('selected-items-list').children().first().contains('UseCase1');
    // and check the content of the reference
    cy.getByTestId('reference-value-UseCase1').should('be.visible');
    // use drag and drop to add the UseCase2
    cy.dragByTestId('UseCase2', 'selected-items-list', { inside: '[role="dialog"]' });
    // check 2 elements in the right panel
    cy.get('@dialog').findByTestId('selected-items-list').children().should('have.length', 2);
    // check bot are added to the reference
    cy.getByTestId('reference-value-UseCase1').should('be.visible');
    cy.getByTestId('reference-value-UseCase2').should('be.visible');
    // remove UseCase1
    cy.get('@dialog').findByTestId('selected-items-list').findByTestId('UseCase1').click();
    cy.get('@dialog').findByTestId('move-left').click();
    // check right panel contains only one element
    cy.get('@dialog').findByTestId('selected-items-list').children().should('have.length', 1);
    // WARNING: the following part is temporary skipped to due a Cypress error raised during execution.
    //
    // remove UseCase2 using drag and drop (from right to left)
    // cy.drag(
    //   '[data-testid="selected-items-list"] [data-testid="UseCase2"]',
    //   '[role="dialog"] [data-testid="tree-root-elements"]'
    // );
    // // check that the right panel is empty
    // cy.get('@dialog').findByTestId('selected-items-list').children().should('have.length', 0);
    // // Close transfer dialog
    // cy.get('@dialog').findByTestId('close-transfer-modal').should('be.visible').click();
  });

  it('create new value element and clear reference content', () => {
    cy.activateDetailsTabAndWaitForElement('UML', 'Use case-add').click();
    cy.get('[role="dialog"]').as('dialog');
    // check only model4test model as root of the tree
    cy.get('@dialog').findByTestId('tree-root-elements').find('li').should('have.length', 1);
    // create a Reception under Activity node
    cy.get('@dialog').findByTestId('model4test.uml-toggle').should('be.visible').click();
    cy.get('@dialog').findByTestId('model4test-toggle').should('be.visible').click();
    cy.get('@dialog').findByTestId('Activity').should('be.visible').click();
    cy.get('@dialog')
      .findByTestId('childCreationDescription')
      .children('[role="combobox"]')
      .contains('UseCase (in nestedClassifier)')
      .click();
    cy.get('[data-value="ownedUseCase::UseCase"]').should('be.visible').click();
    cy.getByTestId('create-object').click();
    // check reference value added
    cy.getByTestId('reference-value-UseCase1').should('be.visible');
    // check instance is properly created
    cy.checkChildren('Activity', ['UseCase1']);
    // clear reference content
    cy.getByTestId('Use case-clear').scrollIntoView().should('be.visible').click();
    // check reference no longer contains previous content
    cy.getByTestId('reference-value-UseCase1').should('not.exist');
    // check instance has not been removed
    cy.checkChildren('Activity', ['UseCase1']);
  });
});

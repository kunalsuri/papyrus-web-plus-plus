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

const projectName = 'Cypress Project - mono-reference-widget';
const context = {};

describe('Mono-valued reference widget tests', () => {
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

  it('set reference value with dropdown and remove value', () => {
    cy.activateDetailsTabAndWaitForElement('UML', 'Specification');
    // check initial content
    cy.checkDropdownContent('Specification', ['Operation1', 'Operation2']);
    // select Operation1
    cy.selectDropdownValue('Specification', 'Operation1');
    // check that Operation1 is not longer in the dropdown
    cy.checkDropdownContent('Specification', ['Operation2']);
    // check Operation1 is set and remove it
    cy.getByTestId('reference-value-Operation1').should('be.visible').find('svg').click();
    // check that it is no longer there
    cy.getByTestId('reference-value-Operation1').should('not.exist');
    // check Operation1 and Operation2 are now available in dropdown
    cy.checkDropdownContent('Specification', ['Operation1', 'Operation2']);
  });

  it('set reference value with dialog', () => {
    // open ... dialog
    cy.activateDetailsTabAndWaitForElement('UML', 'Specification-more').click();
    cy.get('[role="dialog"]').as('dialog');
    // two roots Primitive types and model4test, expand model4test
    cy.get('@dialog').findByTestId('tree-root-elements').find('li').should('have.length', 2);
    cy.get('@dialog').findByTestId('model4test-toggle').should('be.visible').click();
    cy.get('@dialog').findByTestId('Class-toggle').should('be.visible').click();
    // select Operation2
    cy.get('@dialog')
      .findByTestId('tree-root-elements')
      .find('ul')
      .find('ul')
      .children()
      .should('have.length', 2)
      .findByTestId('Operation2')
      .click();
    // close the dialog
    cy.get('@dialog').findByTestId('select-value').click();
    // check the reference value content
    cy.getByTestId('reference-value-Operation1').should('not.exist');
    cy.getByTestId('reference-value-Operation2').should('be.visible');
  });

  it('create new value element and clear reference content', () => {
    cy.activateDetailsTabAndWaitForElement('UML', 'Specification-add').click();
    cy.get('[role="dialog"]').as('dialog');
    // check only model4test model as root of the tree
    cy.get('@dialog').findByTestId('tree-root-elements').find('li').should('have.length', 1);
    // create a Reception under Activity node
    cy.get('@dialog').findByTestId('model4test.uml-toggle').should('be.visible').click();
    cy.get('@dialog').findByTestId('model4test-toggle').should('be.visible').click();
    cy.wait(500);
    cy.get('@dialog').findByTestId('Activity').should('be.visible').click();
    cy.get('@dialog')
      .findByTestId('childCreationDescription')
      .children('[role="combobox"]')
      .contains('Operation (in ownedOperation)')
      .click();
    cy.get('[data-value="ownedReception::Reception"]').should('be.visible').click();
    cy.getByTestId('create-object').click();
    // check reference value added
    cy.getByTestId('reference-value-Reception1').should('be.visible');
    // check instance is properly created
    cy.checkChildren('Activity', ['Reception1']);
    // clear reference content
    cy.getByTestId('Specification-clear').scrollIntoView().should('be.visible').click();
    // check reference no longer contains previous content
    cy.getByTestId('reference-value-').should('not.exist');
  });
});

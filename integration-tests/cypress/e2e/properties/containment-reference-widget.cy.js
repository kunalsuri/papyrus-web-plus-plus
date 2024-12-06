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

const projectName = 'Cypress Project - containment-reference-widget';
const context = {};

describe('Containment reference widget tests', () => {
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

  it('handle many values without type choice', () => {
    cy.getByTestId('Class').should('be.visible').click();
    // check that there are two operations under Class ()
    cy.checkChildren('Class', ['Operation1', 'Operation2'], false);
    cy.activateDetailsTabAndWaitForElement('UML', 'containment-reference-Owned operation').as('reference');
    // check that reference values are there
    cy.getByTestId('containment-reference-Owned operation');
    cy.get('@reference')
      .children()
      .then((chips) => {
        // fist child is the field label, first reference value is at index 1
        expect(chips[1]).to.have.attr('data-testid', 'Operation1');
        expect(chips[2]).to.have.attr('data-testid', 'Operation2');
      });
    // add a new Operation (Operation3) child
    cy.get('@reference').findByTestId('containment-reference-create-child').scrollIntoView().should('be.visible').click();
    // check that Class has now three Operation children
    cy.checkChildren('Class', ['Operation1', 'Operation2', 'Operation3'], false);
    cy.get('@reference').findByTestId('Operation3').should('be.visible');
    // reorder values
    cy.get('@reference').findByTestId('containment-reference-reorder-children').should('be.visible').click();
    // check dialog is open
    cy.getByTestId('containment-reference-reorder-items-dialog').should('be.visible');
    // check there are all three operations is there
    cy.getByTestId('containment-reference-items-list').as('reorder-list');
    cy.get('@reorder-list')
      .children()
      .should('have.length', 3)
      .then((operations) => {
        expect(operations[0]).to.have.attr('data-testid', 'Operation1');
        expect(operations[1]).to.have.attr('data-testid', 'Operation2');
        expect(operations[2]).to.have.attr('data-testid', 'Operation3');
      });
    // move third element to the first place
    cy.get('@reorder-list').findByTestId('Operation3').trigger('dragstart').trigger('dragleave');
    cy.get('@reorder-list')
      .findByTestId('Operation1')
      .trigger('dragenter')
      .trigger('dragover')
      .trigger('drop')
      .trigger('dragend');
    cy.wait(500); // this little pause is mandatory
    // check Operation3 is the first element of the list
    cy.get('@reorder-list')
      .children()
      .then((operations) => {
        expect(operations[0]).to.have.attr('data-testid', 'Operation3');
      });
    // close reorder dialog
    cy.getByTestId('close-containment-reference-reorder-items-dialog').should('be.visible').click();
    //check Operation3 is the first value in reference
    cy.get('@reference')
      .children()
      .then((chips) => {
        // fist child is the field label, first reference value is at index 1
        expect(chips[1]).to.have.attr('data-testid', 'Operation3');
        expect(chips[2]).to.have.attr('data-testid', 'Operation1');
        expect(chips[3]).to.have.attr('data-testid', 'Operation2');
      });
    // remove Operation3
    cy.getByTestId('Operation3').find('svg').click();
    //check Operation3 no longer exists
    cy.get('@reference').findByTestId('Operation3').should('not.exist');
  });

  it('handle only one value with type choice', () => {
    // create a Central Buffer node under Activity node
    cy.getByTestId('Activity-more').should('be.visible').click();
    cy.getByTestId('treeitem-contextmenu').findByTestId('new-object').click();
    cy.getByTestId('childCreationDescription').children('[role="combobox"]').invoke('text').should('have.length.gt', 1);
    cy.getByTestId('childCreationDescription').click();
    cy.get('span:contains("Node | Central Buffer Node")').should('exist').click();
    cy.getByTestId('create-object').click();
    // Wait for the detail view of the CentralBufferNode to be displayed
    cy.getByTestId('Is control type').should('be.visible');
    // Set a name
    cy.getByTestId('input-Name').click().type('CentralBufferNode{downArrow}{enter}');
    // check central buffer node is there
    cy.checkChildren('Activity', ['CentralBufferNode']);
    //check that reference has no value set at the moment
    cy.getByTestId('containment-reference-Upper bound').as('reference').children().eq(1).contains('None');
    // check that only Add action is present
    cy.get('@reference').findByTestId('containment-reference-toolbar').children().should('have.length', 1);
    // create upper bound element
    cy.get('@reference').findByTestId('containment-reference-create-child').click();
    // check dialog is open
    cy.getByTestId('containment-reference-new-child-dialog').should('be.visible');
    // choose to create a new StringExpression
    cy.getByTestId('containment-reference-new-child-dialog-types-list').findByTestId('StringExpression').click();
    // click to create value
    cy.getByTestId('close-containment-reference-new-child-dialog').should('be.visible').click();
    // check a new StringExpression has been created under Central buffer node
    cy.getByTestId('CentralBufferNode-toggle').click();
    cy.getByTestId('explorer://').findByTestId('StringExpression1').should('be.visible');
    cy.checkChildren('CentralBufferNode', ['StringExpression1']);
    // check reference value is properly set
    cy.get('@reference').children().eq(1).contains('StringExpression1');
    // check add new child is disabled
    cy.getByTestId('containment-reference-create-child').should('attr', 'disabled');
    // remove value
    cy.getByTestId('containment-reference-Upper bound')
      .findByTestId('StringExpression1')
      .find('svg')
      .should('be.visible')
      .click();
    // check reference has no value
    cy.get('@reference').children().eq(1).contains('None');
    // check that CentralBufferNode in explore has no longer a child
    cy.checkNoChildren('CentralBufferNode');
  });
});

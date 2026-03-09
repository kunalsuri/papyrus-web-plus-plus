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

const projectName = 'Cypress Project - basicWidgets';
const context = {};

describe('Basic widgets tests', () => {
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

  /**
   * Test validating the Text widget
   */
  it('Test Text description', () => {
    cy.getByTestId('Package').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'Name').find('input').as('nameField');
    // Verify the name of the Package
    cy.get('@nameField').should('have.value', 'Package');
    // change the Package name
    cy.get('@nameField').clear().type('myPackage{enter}', { delay: 50 });
    // click somewhere else (advanced tab)
    cy.activateDetailsTabAndWaitForElement('Advanced', 'group-Core Properties');
    // return to check new value has been properly persisted
    cy.activateDetailsTabAndWaitForElement('UML', 'Name').find('input').should('have.value', 'myPackage');
  });

  /**
   * Test validating the Text area widget
   */
  it('Test Textarea description', () => {
    cy.getByTestId('Comment').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'Body').find('textarea').eq(0).as('textarea');
    // Verify comment content
    cy.get('@textarea').should('have.value', 'Comment');
    // update the comment
    cy.get('@textarea').type('_updated{enter}');
    // Verify the comment content is shown in the explorer tree
    cy.getByTestId('explorer://').findByTestId('Comment_updated').should('be.visible');
  });

  /**
   * Test validating the Checkbox widget
   */
  it('Test Checkbox description', () => {
    cy.getByTestId('Activity').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'Is abstract').find('input').as('checkbox');
    // Verify default value is not checked
    cy.get('@checkbox').should('be.not.checked');
    // Check the checkbox
    cy.get('@checkbox').check().should('be.checked');
    // click somewhere else (advanced tab)
    cy.activateDetailsTabAndWaitForElement('Advanced', 'group-Core Properties');
    // return to check new value has been properly persisted
    cy.activateDetailsTabAndWaitForElement('UML', 'Is abstract').find('input').should('be.checked');
  });

  /**
   * Test validating the Select widget
   */
  it('Test Select description', () => {
    cy.getByTestId('Class').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'Visibility').as('select');
    // Verify that default visibility is public
    cy.get('@select').should(($select) => {
      expect($select).to.contain('public');
    });
    // open select
    cy.get('@select').click();
    // Verify the content of the Select menu
    cy.get('.MuiPopover-root[id=menu-]')
      .should('be.visible')
      .find('ul>li')
      .should(($li) => {
        expect($li).to.have.length(5);
        const values = $li.map((i, el) => Cypress.$(el).attr('data-value'));
        // call classes.get() to make this a plain array
        expect(values.get()).to.deep.eq([
          '', // None
          'http://www.eclipse.org/uml2/5.0.0/UML#//VisibilityKind/public',
          'http://www.eclipse.org/uml2/5.0.0/UML#//VisibilityKind/private',
          'http://www.eclipse.org/uml2/5.0.0/UML#//VisibilityKind/protected',
          'http://www.eclipse.org/uml2/5.0.0/UML#//VisibilityKind/package',
        ]);
      })
      // Choose package
      .eq(4)
      .click();
    // Give focus to other widget to validate Select change
    cy.getByTestId('Name').click().focused();
    // Verify Visibility is actually package
    cy.get('@select').should(($select) => {
      expect($select).to.contain('package');
    });
  });

  /**
   * Test validating the LiteralBoolean custom widget
   */
  it('Test LiteralBoolean concept', () => {
    const retrieveValuesElement = () => {
      cy.getByTestId('flexbox-Value').children().eq(1).children().eq(0).find('input').as('true');
      cy.getByTestId('flexbox-Value').children().eq(1).children().eq(1).find('input').as('false');
    };

    // Select LiteralBoolean in Explorer
    cy.getByTestId('LiteralBoolean').eq(0).click();
    cy.activateDetailsTabAndWaitForElement('UML', 'flexbox-Value');
    retrieveValuesElement();
    // Verify that True is not checked and False is checked
    cy.get('@true').should(($c) => {
      expect($c).to.have.attr('name').equals('True');
      expect($c).to.have.attr('type').equals('checkbox');
      expect($c).not.checked;
    });
    cy.get('@false').should(($c) => {
      expect($c).to.have.attr('name').equals('False');
      expect($c).to.have.attr('type').equals('checkbox');
      expect($c).checked;
    });
    // check TRUE
    cy.get('@true').check().should('be.checked');
    // Verify that in Advanced tab, the value has been changed
    cy.activateDetailsTabAndWaitForElement('Advanced', 'Value').should('be.visible').find('input').should('be.checked');
    // check value in Advanced tab, the value is now true
    cy.getByTestId('Value').find('input').check().should('be.checked');
    // back to UML tab
    cy.activateDetailsTabAndWaitForElement('UML', 'flexbox-Value');
    retrieveValuesElement();
    // Verify that True is checked and False is not checked
    cy.get('@true').should('be.checked');
    cy.get('@false').should('be.not.checked');
  });

  /**
   * Test validating the MemberEnd container
   */
  it('Test Member end group', () => {
    cy.getByTestId('Association').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'group-')
      // Verify that there are 3 groups in UML page (1 for properties and 2 memberEnds)
      .should('have.length', 3)
      // Retrieve the first MemberEnd
      .eq(1)
      .as('memberEnd');
    // Verify that there is a Name section inside MemberEnd group
    cy.get('@memberEnd').findByTestId('Name').should('be.visible').find('input').should('have.value', 'from');
    // Verify that there is a Navigable section inside MemberEnd group
    cy.get('@memberEnd').findByTestId('Navigable').should('be.visible');
    // Verify that there is a Aggregation section inside MemberEnd group
    cy.get('@memberEnd').findByTestId('Aggregation').should('be.visible');
    // Verify that there is a Multiplicity section inside MemberEnd group
    cy.get('@memberEnd').findByTestId('Multiplicity').should('be.visible');
    // Verify that there is a Owner section inside MemberEnd group
    cy.get('@memberEnd').findByTestId('primitive-radio-widget').should('be.visible');
  });

  /**
   * Test validating the Multiplicity control (with validation)
   */
  it('Test Multiplicity widget', () => {
    cy.getByTestId('Association').click();
    // Retrieve the first MemberEnd
    cy.activateDetailsTabAndWaitForElement('UML', 'flexbox-Member End').eq(0).as('memberEnd');
    cy.get('@memberEnd').findByTestId('Multiplicity').should('be.visible').find('input').as('multiplicityInput');
    // Change multiplicity value with error
    cy.get('@multiplicityInput').should('have.value', '1').clear().type('WRONG MULTIPLICITY{enter}');
    // Verify error pops up if wrong entry
    cy.get('[role="alert"]').should('be.visible').contains("'WRONG MULTIPLICITY' is not a valid multiplicity value");
    // Close error popup
    cy.get('[role="alert"]').find('button').should('be.visible').click();
    // Change multiplicity value with valid one
    cy.get('@multiplicityInput').clear().type('0..1{enter}');
    // Verify the entered multiplicity
    cy.getByTestId('from').should('be.visible').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'Multiplicity')
      .eq(0)
      .should('be.visible')
      .find('input[name="Multiplicity"]')
      .should('have.value', '0..1');
    // cy.get('[role="alert"]').should('be.not.visible');
  });
});

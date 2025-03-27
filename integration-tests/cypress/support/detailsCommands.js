/*****************************************************************************
 * Copyright (c) 2023 CEA LIST, Obeo.
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

/**
 * Activate a tab of the Details view according to its name
 *
 * @param {string} tabName the label of the tab
 */
Cypress.Commands.add('activateDetailsTab', (tabName) => {
  cy.getByTestId(`page-tab-${tabName}`).should('exist').click();
  return cy.get('[data-testid="view-Details"]');
});

/**
 * Activate a tab of the Details view according to its name and wait until the given element is visible
 *
 * @param {string} tabName the label of the tab
 * @param {string} element the data-testid of an element to guarantee the tab is correctly open
 * @returns the searched element
 */
Cypress.Commands.add('activateDetailsTabAndWaitForElement', (tabName, element) => {
  cy.getByTestId(`page-tab-${tabName}`)
    .should('exist')
    .then(($tab) => {
      if (!$tab.hasClass('Mui-selected')) {
        cy.wrap($tab).click();
      }
      return cy.wrap($tab);
    })
    .should('have.class', 'Mui-selected');
  return cy.getByTestId('view-Details').findByTestId(element).should('be.exist');
});

/**
 * Return the root element of Details panel
 */
Cypress.Commands.add('inDetailsCurrentTab', () => {
  return cy.get('[data-testid="view-Details"]');
});
/**
 * Adds a item in a primitive list widget in a strict mode
 * @param primitiveListName name of the primitive list
 * @param itemValue the value to add (available in the autocomplete component)
 */
Cypress.Commands.add('addItemInPrimitiveListStrictMode', (primitiveListName, itemValue) => {
  cy.getByTestId(`primitive-list-autocomplete-textfield-${primitiveListName}`).should('be.visible').click();
  cy.get(`.MuiAutocomplete-popper`)
    .should('be.visible')
    .find(`ul > li`)
    .contains(itemValue)
    .should('be.visible')
    .click();
  cy.getByTestId(`primitive-list-add-${primitiveListName}`).should('not.have.class', 'Mui-disabled').click();
  cy.getByTestId(`primitive-list-table-${primitiveListName}`)
    .findByTestId(`primitive-list-item-${itemValue}`)
    .should('exist');
});

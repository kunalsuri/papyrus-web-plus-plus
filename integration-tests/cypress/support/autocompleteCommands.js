/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo.
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
 * Check the given autocomplete dropdown list content.
 *
 * @param autocomplete the testid of autocomplete widget to handle
 * @param content the array of entries fully (partially if strict = <code>false</code>) expected to be found in the dropdown list
 * @param strict If <code>true</code> the content of the dropdown should be exactly as the given content.
 * If <code>false</code> only check that given content is actually present in the dropdown list.
 * <code>true</code> by default.
 */
Cypress.Commands.add('checkDropdownContent', (autocomplete, content, strict = true) => {
  cy.getByTestId(autocomplete).find('.MuiAutocomplete-endAdornment').find('button').should('exist').click();
  cy.get('.MuiAutocomplete-popper')
    .should('exist')
    .find('ul')
    .should('exist')
    .children()
    .should(($lis) => {
      const optionTexts = $lis.toArray().map((el) => el.innerText);
      if (strict) {
        expect(optionTexts).to.have.lengthOf(content.length);
        expect(optionTexts).to.deep.eq(content);
      } else {
        content.forEach((item) => {
          expect(optionTexts).to.contain(item);
        });
      }
    });
  // close the dropdown
  cy.getByTestId(autocomplete).find('.MuiAutocomplete-endAdornment').find('button').should('exist').click();
});

/**
 * Select the given value inside the given autocomplete dropdown list.
 *
 * @param autocomplete the autocomplete widget to handle
 * @param value the value to select among dropdown list entries
 */
Cypress.Commands.add('selectDropdownValue', (autocomplete, value) => {
  cy.getByTestId(autocomplete).find('.MuiAutocomplete-endAdornment').find('button').should('exist').click();
  cy.get('.MuiAutocomplete-popper').find('ul').should('exist').children().contains(value).should('exist').click();
});

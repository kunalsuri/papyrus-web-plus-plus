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

const projectName = 'Cypress Project - details-panel';
const context = {};

describe('Details panel tests', () => {
  /**
   * For each test, we start with a fresh new project containing several concepts useful to exercise the properties view
   */
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    cy.createTestProject(context, projectName);
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  /**
   * Test validating that Detail panel has 4 pages
   */
  it('Check the Details panel pages when Package is selected', () => {
    cy.getByTestId('Package').should('be.visible').click();
    cy.getByTestId('view-Details')
      .should('be.visible')
      .get('div[role="tablist"] > button')
      .should(($buttons) => {
        const texts = $buttons.toArray().map((el) => el.innerText);
        expect(texts).to.have.lengthOf(5);
        expect(texts).to.deep.eq(['UML', 'Symbol', 'Comments', 'Profile', 'Advanced']);
      });
  });
});

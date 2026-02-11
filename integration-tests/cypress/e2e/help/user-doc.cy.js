/*******************************************************************************
 * Copyright (c) 2023, 2024, 2026 CEA LIST, Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *     Vincent LORENZO (CEA LIST) - vincent.lorenzo@cea.fr - Issue 298
 *******************************************************************************/
describe('User doc test', () => {
  it('Check that the documentation is accessible from the ? button', () => {
    cy.visit('/projects');
    cy.get('header').find('button').last().click();
    cy.get('[role="menu"]').find('[role="menuitem"]').last().invoke('removeAttr', 'target').click();
    cy.get('h1').should('have.text', 'Papyrus-Web Documentation');
  });

  it('Check that the Welcome page is accessible', () => {
    cy.visit('/doc/welcome/welcome.html');
    cy.get('h1').should('have.text', 'Welcome');
  });

  it('Check that the user documentation is accessible', () => {
    cy.visit('/doc/user/userdoc.html');
    cy.get('h1').should('have.text', 'User Documentation');
  });

  it('Check that the FAQ page is accessible', () => {
    cy.visit('/doc/faq/faq.html');
    cy.get('h1').should('have.text', 'Frequently Asked Questions');
  });

  it('Check that the HowToDeploy page is accessible', () => {
    cy.visit('/doc/deploy/HowToDeploy.html');
    cy.get('h1').should('have.text', 'How to deploy Papyrus-Web locally');
  });

  it('Check that the dev documentation is accessible', () => {
    cy.visit('/doc/dev/dev-doc-index.html');
    cy.get('h1').should('have.text', 'Documentation for developers');
  });

  it('Check that the specifications are accessible', () => {
    cy.visit('/doc/spec/diagrams-spec-index.html');
    cy.get('h1').should('have.text', 'Papyrus-Web specifications');
  });
});

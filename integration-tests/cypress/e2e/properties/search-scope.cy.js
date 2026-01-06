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

const projectName = 'Cypress Project - search-scope';
const context = {};

describe('Containment reference widget tests', () => {
  /**
   * For each test, we start with a fresh new project containing all concepts gathered in one single model
   */
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    cy.createProjectWithName(context, projectName, 'EmptyUMLTemplate').then((res) => {
      cy.wrap(context.projectId).as('projectId');
      cy.visit(`/projects/${context.projectId}/edit`).then((res) => {
        cy.getByTestId('upload-document-icon').click();
        cy.fixture('search-scope.uml', { mimeType: 'text/xml' }).as('model');
        cy.getByTestId('file')
          .selectFile(
            {
              contents: '@model',
              fileName: 'search-scope.uml', // workaround for selectFile issue https://github.com/cypress-io/cypress/issues/21936
            },
            { force: true }
          )
          .then(() => {
            cy.getByTestId('upload-document-split-button').click();
            cy.getByTestId('upload-document-close').click();
            cy.expandAll('search-scope.uml');
          });
      });
    });
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  it('search scope service test', () => {
    cy.getByTestId('Property1').should('be.visible').click();
    // open dropdown of Type
    cy.activateDetailsTabAndWaitForElement('UML', 'Type');
    cy.checkDropdownContent('Type', ['Integer', 'Class2'], false);
    // check within the select dialog (...)
    cy.getByTestId('Type-more').click();
    cy.getByTestId('browse-modal').should('be.visible').as('select-dialog');
    cy.get('@select-dialog').findByTestId('Model-toggle').click();
    cy.get('@select-dialog').findByTestId('PrimitiveTypes-toggle').click();
    cy.get('@select-dialog')
      .findByTestId('tree-root-elements')
      .children()
      .should((roots) => {
        expect(roots).to.have.lengthOf(2);
      });
    // expand tree and check content
    cy.get('@select-dialog').findByTestId('Class1').should('exist');
    cy.get('@select-dialog').findByTestId('Class2').should('exist').click();
    cy.get('@select-dialog').findByTestId('Boolean').should('exist');
    cy.get('@select-dialog').findByTestId('Integer').should('exist');
    cy.get('@select-dialog').findByTestId('Real').should('exist');
    cy.get('@select-dialog').findByTestId('String').should('exist');
    cy.get('@select-dialog').findByTestId('UnlimitedNatural').should('exist');
    cy.get('@select-dialog').findByTestId('select-value').should('exist').click();
    // remove reference value set
    cy.getByTestId('reference-value-Class2').find('svg').click();

    // remove the package import node
    cy.getByTestId('<Package Import> PrimitiveTypes-more').should('be.visible').click();
    cy.getByTestId('treeitem-contextmenu').findByTestId('delete').click();
    cy.getByTestId('confirmation-dialog-button-ok').click();
    cy.getByTestId('<Package Import> PrimitiveTypes').should('not.exist');
    // check that no primitive type could be found now
    cy.getByTestId('Property1').should('be.visible').click();
    // open dropdown of Type
    cy.activateDetailsTabAndWaitForElement('UML', 'Type').click();
    cy.get('.MuiAutocomplete-popper').find('ul').as('dropdown');
    // only local model types but not primitive type
    cy.get('@dropdown').findByTestId('option-Class1').should('be.visible');
    cy.get('@dropdown').findByTestId('option-Class2').should('be.visible');
    cy.get('@dropdown').findByTestId('option-Integer').should('not.exist');
    // check within the select dialog (...)
    cy.getByTestId('Type-more').click();
    cy.getByTestId('browse-modal').should('be.visible').as('select-dialog');
    cy.get('@select-dialog').findByTestId('Model-toggle').click();
    cy.get('@select-dialog').findByTestId('PrimitiveTypes').should('not.exist');
    cy.get('@select-dialog').findByTestId('tree-root-elements').children().should('have.length', 1);
  });
});

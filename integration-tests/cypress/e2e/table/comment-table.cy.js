/*******************************************************************************
 * Copyright (c) 2025, 2026 CEA LIST.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/

const projectName = 'Cypress Project - comment table';
const context = {};

describe('Comment table tests', () => {
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    createTestProjectForCommentTable(context, projectName);
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  const createTestProjectForCommentTable = () => {
    cy.createProjectWithName(context, projectName, 'EmptyUMLTemplate').then((res) => {
      cy.visit(`/projects/${context.projectId}/edit`).then((res) => {
        cy.getByTestId('upload-document-icon').click();
        cy.fixture('CommentTableTestModel.uml', { mimeType: 'text/xml' }).as('CommentTableTestModel');
        return cy
          .getByTestId('file')
          .selectFile(
            {
              contents: '@CommentTableTestModel',
              fileName: 'CommentTableTestModel.uml', // workaround for selectFile issue https://github.com/cypress-io/cypress/issues/21936
            },
            { force: true }
          )
          .then(() => {
            cy.getByTestId('upload-document-split-button').should('be.visible').should('not.be.disabled').click();
            cy.getByTestId('upload-document-close').should('be.visible').should('not.be.disabled').click();
            cy.expandAll('CommentTableTestModel.uml');
          });
      });
    });
  };

  const checkNumberOfRows = (num) => {
    cy.getByTestId('table-representation').find('tbody').children().should('have.length', num);
  };

  const getColumnHeader = (columnLabel) => {
    return cy.getByTestId('table-representation').find('thead tr').contains('th', columnLabel);
  };

  const getCellAtPosition = (rowIndex, columnIndex) => {
    return (
      cy
        .getByTestId('table-representation')
        .find('tbody')
        .find('tr')
        // index starts with 0
        .eq(rowIndex)
        .find('td')
        .eq(columnIndex)
    );
  };

  const setNumberRowPerPage = (num) => {
    cy.getByTestId('table-representation')
      .contains('Rows per page:')
      .parent()
      .find('[aria-label="Rows per page:"]')
      .click();
    cy.get('.MuiPopover-root[id=menu-]').should('be.visible').find(`li[data-value="${num}"]`).click();
  };

  const getPreviousPageButton = () => {
    return cy
      .getByTestId('table-representation')
      .contains('Rows per page:')
      .parent()
      .parent()
      .children()
      .eq(1)
      .find('button')
      .eq(0);
  };

  const getNextPageButton = () => {
    return cy
      .getByTestId('table-representation')
      .contains('Rows per page:')
      .parent()
      .parent()
      .children()
      .eq(1)
      .find('button')
      .eq(1);
  };

  it('create table comment', () => {
    // create the comment table
    cy.getByTestId('Model').should('be.visible').click();
    cy.getByTestId('onboard-area')
      .findByTestId(
        'siriusComponents://representationDescription?kind=tableDescription&sourceKind=view&sourceId=957292ad-614d-3f43-931f-0206fd5da1e5&sourceElementId=93c27c9e-1140-387d-9d8c-ac757de69619'
      )
      .should('exist')
      .click();
    cy.getByTestId('representation-area').findByTestId('representation-tab-UML Comment Table').should('be.visible');
    cy.getByTestId('representation-area').findByTestId('table-representation').should('be.visible');
    // check that there are 7 rows in the table
    checkNumberOfRows(7);

    // apply a global filter => only 2 rows are visible now
    cy.getByTestId('table-representation').find('input[placeholder="Search"]').type('COMMENT{enter}');
    checkNumberOfRows(2);

    // clear global filter => all rows are visible
    cy.getByTestId('table-representation').find('button[aria-label="Clear search"]').click();
    checkNumberOfRows(7);

    // apply a column filter on Annotated elements column => only 2 rows should be visible
    cy.getByTestId('table-representation').find('button[aria-label="Show/Hide filters"]').click();
    getColumnHeader('Annotated Elements')
      .find('input')
      .should('have.attr', 'title', 'Filter by Annotated Elements')
      .type('MODEL{enter}');
    checkNumberOfRows(2);

    // clear column filter
    getColumnHeader('Annotated Elements').find('button[aria-label="Clear filter"]').parent().click();
    checkNumberOfRows(7);

    // select the comment with body containing 4
    cy.getByTestId('tree-root-elements').findByTestId('4-fullrow').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'input-Body').as('body');

    // change the body of the comment => should be updated in the table
    cy.get('@body').click();
    cy.get('@body').type('th row{enter}');
    getCellAtPosition(2, 2).contains('4th row');
    getCellAtPosition(3, 1).contains('4th row');

    // clear the name of an annotated element => Class should be visible in the table cell
    getCellAtPosition(0, 0).contains('1').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'reference-value-ClassA').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'input-Name').clear().type('{enter}');
    getCellAtPosition(0, 2).contains('Class');

    // check that by default navigation buttons are disabled
    getPreviousPageButton().should('be.disabled');
    getNextPageButton().should('be.disabled');

    // set only 5 rows per page
    setNumberRowPerPage(5);
    checkNumberOfRows(5);
    getPreviousPageButton().should('be.disabled');
    getNextPageButton().should('not.be.disabled');

    // go to next page
    getNextPageButton().click();
    getPreviousPageButton().should('not.be.disabled');
    getNextPageButton().should('be.disabled');
    checkNumberOfRows(2);
    getCellAtPosition(0, 1).contains('we are on second page');

    // go back to previous page
    getPreviousPageButton().click();
    getPreviousPageButton().should('be.disabled');
    getNextPageButton().should('not.be.disabled');
    checkNumberOfRows(5);
  });
});

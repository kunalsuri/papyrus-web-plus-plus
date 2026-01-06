/*******************************************************************************
 * Copyright (c) 2023, 2026 CEA LIST
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

const projectName = 'Cypress Project - stereotypes';

describe('/projects', () => {
  beforeEach(() => {
    cy.deleteProjectByName(projectName);
    cy.createProjectWithName(context, projectName, 'EmptyUMLTemplate');
  });

  afterEach(() => {
    cy.deleteProjectByName(projectName);
  });

  /**
   * Test validating the root UML model creation from a blank project
   */
  it('Test base UML stereotypes', () => {
    cy.getByTestId('new-model').click();
    cy.getByTestId('name-input').type('Base UML.uml');
    cy.getByTestId('stereotype').click();
    cy.get('li').contains('Model UML').click();
    cy.getByTestId('create-document').click();
    cy.getByTestId('explorer://').findByTestId('Base UML.uml').should('be.visible').dblclick();
  });
});

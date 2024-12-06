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

const projectName = 'Cypress Project - redefined-reference-types';
const context = {};

describe('Redefined reference types test', () => {
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

  const checkChildDescriptionContent = (content) => {
    cy.getByTestId('create-modal').should('be.visible').as('dialog');
    cy.getByTestId('create-modal')
      .findByTestId('childCreationDescription')
      .should('not.have.class', 'Mui-disabled')
      .click();
    cy.get('#menu-').find(`li`).should('have.length', content.length);
    content.forEach((element) => {
      cy.get('#menu-').find(`li`).contains(element).should('be.visible');
    });
  };

  it('check ConnectableElementTemplateParameter.parameteredElement: ConnectableElement instead of ParameterableElement', () => {
    cy.getByTestId('<Connectable Element Template Parameter>').scrollIntoView().should('be.visible').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'Parametered element');
    cy.openReferenceChildCreationDialog('Parametered element', 'Activity');
    checkChildDescriptionContent([
      'ExtensionEnd (in ownedAttribute)',
      'Parameter (in ownedParameter)',
      'Port (in ownedAttribute)',
      'Property (in ownedAttribute)',
      'Variable (in variable)',
    ]);
  });

  it('check DurationConstraint.specification: DurationInterval instead of ValueSpecification', () => {
    cy.getByTestId('DurationConstraint').should('be.visible').click();
    cy.activateDetailsTabAndWaitForElement('UML', 'containment-reference-Specification')
      .findByTestId('containment-reference-create-child').scrollIntoView()
      .should('be.visible')
      .click();
    cy.getByTestId('containment-reference-Specification').findByTestId('DurationInterval1').should('be.visible');
  });
});

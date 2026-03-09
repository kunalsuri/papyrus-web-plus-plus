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
/*
 * Test used for the profile page
 */
describe('Stereotype application page tests', () => {
  const instanceProjectName = 'Cypress Test - Profil page - Instance';
  const profileProjectName = 'Cypress Test - Profil page - Profile';
  const profileName = 'ProfilPageTest-Profile';

  const context = {};

  before(() => {
    // In case the profile is published by another test that do not clean its data
    cy.deletePublishedDynamicProfileByName(profileName);
    cy.deleteProjectByName(instanceProjectName);
    cy.deleteProjectByName(profileProjectName);

    cy.createTestProfileProject(context, profileProjectName, profileName);
  });

  beforeEach(() => {
    cy.createTestProject(context, instanceProjectName, 'model4test', 'Profile');
  });

  const checkReferenceTransferModalRightContent = (values) => {
    cy.getByTestId('transfer-modal')
      .should('be.visible')
      .findByTestId('selected-items-list')
      .find('li')
      .should(($lis) => {
        const optionTexts = $lis.toArray().map((el) => el.getAttribute('data-testid'));
        expect(optionTexts).to.deep.eq(values);
      });
  };

  afterEach(() => {
    cy.deleteProjectByName(instanceProjectName);
  });

  after(() => {
    cy.deletePublishedDynamicProfileByName(profileName);
    cy.deleteProjectByName(profileProjectName);
  });

  it('Check static profile application', () => {
    // Apply Java profile
    cy.addItemInPrimitiveListStrictMode('Applied profiles', 'Standard');

    // Check there is no refresh button
    cy.getByTestId('primitive-list-item-action-button-Standard').should('not.exist');

    // Apply one stereotype
    cy.addItemInPrimitiveListStrictMode('Applied stereotypes', 'ModelLibrary (from StandardProfile)');

    // Unapply stereotype
    cy.getByTestId('primitive-list-item-delete-button-ModelLibrary (from StandardProfile)').click();
    cy.getByTestId(`primitive-list-table-Applied stereotypes`)
      .findByTestId(`primitive-list-item-ModelLibrary (from StandardProfile)`)
      .should('not.exist');

    // Unapply profile
    cy.getByTestId('primitive-list-item-delete-button-Standard').click();
    cy.getByTestId(`primitive-list-table-Applied profiles`)
      .findByTestId(`primitive-list-item-Standard`)
      .should('not.exist');
  });

  it('Check dynamic profile application', () => {
    cy.visit(`/projects/${context.projectId}/edit`).then((res) => {
      cy.expandAll('model4test.uml');
      cy.getByTestId('model4test').should('exist').click();
      cy.activateDetailsTabAndWaitForElement('Profile', 'primitive-list-autocomplete-textfield-Applied profiles');

      // Apply Java profile
      cy.addItemInPrimitiveListStrictMode('Applied profiles', `${profileName} (0.0.1)`);

      // Check there is no refresh action
      cy.getByTestId(`primitive-list-item-action-button-${profileName} (0.0.1)`).should('not.exist');

      // Apply one stereotype
      cy.addItemInPrimitiveListStrictMode('Applied stereotypes', `Stereotype2 (from ${profileName})`);

      // Apply stereotype 1 on class
      cy.getByTestId('Class').first().should('exist').click();
      cy.getByTestId('page-tab-UML').click();
      cy.getByTestId('page-tab-UML').should('have.class', 'Mui-selected');
      cy.getByTestId('view-Details').findByTestId('Name').should('be.visible');
      cy.activateDetailsTabAndWaitForElement('Profile', 'primitive-list-autocomplete-textfield-Applied stereotypes');
      cy.addItemInPrimitiveListStrictMode('Applied stereotypes', `Stereotype1 (from ${profileName})`);
    });

    // Publish a new version the profile to a new version
    cy.visit(`/projects/${context.profileProjectId}/edit`).then((res) => {
      cy.expandAll(`${profileName}.profile.uml`);
      cy.getByTestId(`${profileName}-more`).first().click();
      cy.getByTestId('publish-profile').should('be.visible').click();

      cy.getByTestId('publish-profile-dialog').findByTestId('publish-profile-author').find('input').type('Jerome');
      cy.getByTestId('publish-profile-publish').should('not.have.class', 'Mui-disabled').should('be.visible').click();

      cy.get('img[alt="Back to the homepage"]').should('be.visible').click();
      // We need to wait 7 sec to be sure that the ResourceSet of the first project is unloaded
      cy.wait(7000);
    });

    // Go back to the project
    cy.visit(`/projects/${context.projectId}/edit`).then((res) => {
      cy.expandAll('model4test.uml');
      cy.getByTestId('«Stereotype2» model4test').click();

      // Check there is a refresh button
      cy.activateDetailsTabAndWaitForElement(
        'Profile',
        `primitive-list-item-action-button-${profileName} (0.0.1)`
      ).click();

      //The version should be updated to version 0.0.2
      cy.getByTestId(`primitive-list-table-Applied profiles`)
        .findByTestId(`primitive-list-item-${profileName} (0.0.2)`)
        .should('exist');

      // The stereotype should not be deleted due to reapplication of the profile
      cy.getByTestId(`primitive-list-table-Applied stereotypes`)
        .findByTestId(`primitive-list-item-Stereotype2 (from ProfilPageTest-Profile)`)
        .should('exist');

      // Check that the properties are still working
      cy.getByTestId('«Stereotype1» Class').first().should('exist').click();
      cy.getByTestId('page-tab-UML').click();
      cy.getByTestId('page-tab-UML').should('have.class', 'Mui-selected');
      cy.getByTestId('view-Details').findByTestId('Name').should('be.visible');
      // Check the dialog content to reference a stereotype application
      cy.activateDetailsTabAndWaitForElement('Stereotype1', 'testMultiReftoStereotype2-more').click();
      cy.getByTestId('transfer-modal').should('be.visible').as('dialog');
      checkReferenceTransferModalRightContent([]);
      cy.get('@dialog').findByTestId('tree-root-elements').findByTestId('model4test').should('be.visible').click();
      cy.get('@dialog').findByTestId('move-right').click();
      cy.get('@dialog').findByTestId('close-transfer-modal').click();
    });
  });
});

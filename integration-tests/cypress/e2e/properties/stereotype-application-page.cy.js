/*****************************************************************************
 * Copyright (c) 2023, 2025 CEA LIST, Obeo.
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

describe('Stereotype application page tests', () => {
  const instanceProjectName = 'Cypress Test - Stereotype application page - Instance';
  const profileProjectName = 'Cypress Test - Stereotype application page - Profile';
  const profileName = 'StereotypeApplicationPageTests-Profile';

  const context = {};

  before(() => {
    // In case the profile is published by another test that do not clean its data
    cy.deletePublishedDynamicProfileByName(profileName);
    cy.deleteProjectByName(instanceProjectName);
    cy.deleteProjectByName(profileProjectName);

    cy.createTestProfileProject(context, profileProjectName, profileName);
    cy.createTestProject(context, instanceProjectName, 'model4test', 'Profile').then(() => {
      applyProfileAndStereotypes();
    });
  });

  beforeEach(() => {
    cy.visit('/projects');
    cy.visit(`/projects/${context.projectId}/edit`);
    cy.expandAll('model4test.uml');
    cy.getByTestId('«Stereotype1» Class').click();
    cy.activateDetailsTabAndWaitForElement('Stereotype1', 'testInt');
  });

  after(() => {
    cy.visit('/projects');
    cy.deleteProjectByName(instanceProjectName);
    cy.deleteProjectByName(profileProjectName);
  });

  it('Check mono string', () => {
    cy.getByTestId('input-testString').clear().type('new string{enter}');

    // Check persisted value
    refreshView();
    cy.getByTestId('input-testString').contains('new string');
  });

  it('Check mono integer', () => {
    // mono Integer => testInt
    cy.getByTestId('input-testInt').clear().type('wrong value{enter}');
    // check that an alert pops up
    cy.get('[role="alert"]')
      .should('be.visible')
      .contains("'wrong value' is not a valid value for feature 'testInt' expecting an integer number");
    // close alert, clear and wait until the alert goes away
    cy.get('[role="alert"]').find('button').should('be.visible').click();
    cy.getByTestId('input-testInt').clear();
    cy.get('[role="alert"]').should('not.exist');
    // replace with a valid integer
    cy.getByTestId('input-testInt').type('1{enter}');
    cy.get('[role="alert"]').should('not.exist');

    // Check persisted value
    refreshView();
    cy.getByTestId('input-testInt').should('have.value', '1');
  });

  it('Check mono Real', () => {
    // mono Double => testReal
    cy.getByTestId('input-testReal').clear().type('wrong value{enter}');
    // check that an alert pops up
    cy.get('[role="alert"]')
      .should('be.visible')
      .contains("'wrong value' is not a valid value for feature 'testReal' expecting a double number");
    // close alert, clear and wait until the alert goes away
    cy.getByTestId('input-testReal').clear();
    cy.get('[role="alert"]').should('not.exist');
    // replace with a valid real number
    cy.getByTestId('input-testReal').type('-0.9999{enter}');
    cy.get('[role="alert"]').should('not.exist');

    // Check persisted value
    refreshView();
    cy.getByTestId('input-testReal').should('have.value', '-0.9999');
  });

  it('Check mono boolean', () => {
    // mono boolean => testBoolean
    cy.getByTestId('testBoolean').should('not.have.class', 'Mui-checked');
    cy.getByTestId('testBoolean').click();

    // mono boolean object => testBooleanObject
    cy.getByTestId('primitive-radio-widget').contains('testBooleanObject').click();
    checkPrimitiveRadioState('true', false);
    checkPrimitiveRadioState('false', false);
    checkPrimitiveRadioState('null', true);
    cy.getByTestId('primitive-radio-true').click();

    // Check persisted value
    refreshView();
    cy.getByTestId('testBoolean').should('have.class', 'Mui-checked');

    checkPrimitiveRadioState('true', true);
    checkPrimitiveRadioState('false', false);
    checkPrimitiveRadioState('null', false);
  });

  it('Check mono enumeration', () => {
    // mono enumeration => testRefToEnumeration
    cy.getByTestId('testRefToEnumeration').contains('EnumerationLiteral1');
    chooseSelectValue('testRefToEnumeration', 'EnumerationLiteral2');

    // Check persisted value
    refreshView();
    cy.getByTestId('testRefToEnumeration').contains('EnumerationLiteral2');
  });

  it('Check multi String', () => {
    // multi String => testMultiString
    checkPrimitiveListIsEmpty('testMultiString');
    addItemInPrimitiveListFreeMode('testMultiString', 'first value');
    addItemInPrimitiveListFreeMode('testMultiString', 'second value');
    addItemInPrimitiveListFreeMode('testMultiString', 'third value');

    // Check persisted value
    refreshView();
    checkPrimitiveListContent('testMultiString', ['first value', 'second value', 'third value']);
  });

  it('Check multi Integer', () => {
    // multi Integer => testMultiInt
    checkPrimitiveListIsEmpty('testMultiInt');
    addItemInPrimitiveListFreeMode('testMultiInt', '-1');
    addItemInPrimitiveListFreeMode('testMultiInt', '99');

    // Check persisted value
    refreshView();
    checkPrimitiveListContent('testMultiInt', ['-1', '99']);
  });

  it('Check multi Real', () => {
    // multi Double => testMultiReal
    checkPrimitiveListIsEmpty('testMultiReal');
    addItemInPrimitiveListFreeMode('testMultiReal', '-0.1');
    addItemInPrimitiveListFreeMode('testMultiReal', '3.14159265');
    addItemInPrimitiveListFreeMode('testMultiReal', '2.718281828459045');

    // Check persisted value
    refreshView();
    checkPrimitiveListContent('testMultiReal', ['-0.1', '3.14159265', '2.718281828459045']);
  });

  it('Check multi Boolean', () => {
    // multi boolean => testMultiBoolean
    checkPrimitiveListIsEmpty('testMultiBoolean');
    addItemInPrimitiveListStrictMode('testMultiBoolean', 'false');
    addItemInPrimitiveListStrictMode('testMultiBoolean', 'true');
    addItemInPrimitiveListStrictMode('testMultiBoolean', 'true');

    // Check persisted value
    refreshView();
    checkPrimitiveListContent('testMultiBoolean', ['false', 'true', 'true']);
  });

  it('Check multi Enumeration', () => {
    // multi enumeration => testMultiEnumeration
    checkPrimitiveListIsEmpty('testMultiEnumeration');
    addItemInPrimitiveListStrictMode('testMultiEnumeration', 'EnumerationLiteral2');
    addItemInPrimitiveListStrictMode('testMultiEnumeration', 'EnumerationLiteral3');
    addItemInPrimitiveListStrictMode('testMultiEnumeration', 'EnumerationLiteral1');

    // Check persisted value
    refreshView();
    checkPrimitiveListContent('testMultiEnumeration', [
      'EnumerationLiteral2',
      'EnumerationLiteral3',
      'EnumerationLiteral1',
    ]);
  });

  it('Check mono reference to metaclass', () => {
    // mono ref to Metaclass => testMonoRefToMetaclass
    checkReferenceValues('testMonoRefToMetaclass', []);
    cy.checkDropdownContent('testMonoRefToMetaclass', ['Activity', 'Class', 'FunctionBehavior']);
    // set reference value using dropdown
    cy.selectDropdownValue('testMonoRefToMetaclass', 'FunctionBehavior');
    checkReferenceValues('testMonoRefToMetaclass', ['FunctionBehavior']);
    cy.checkDropdownContent('testMonoRefToMetaclass', ['Activity', 'Class']);
    // remove FunctionBehavior
    cy.getByTestId('reference-value-FunctionBehavior').find('.MuiChip-deleteIcon').click();
    checkReferenceValues('testMonoRefToMetaclass', []);
    // set reference value using dialog
    cy.getByTestId('testMonoRefToMetaclass-more').click();
    cy.getByTestId('browse-modal').should('be.visible').as('dialog');
    cy.get('@dialog').findByTestId('tree-root-elements').findByTestId('model4test-toggle').should('be.visible').click();
    cy.get('@dialog').findByTestId('tree-root-elements').findByTestId('Class').should('be.visible').click();
    cy.get('@dialog')
      .findByTestId('tree-root-elements')
      .findByTestId('Class')
      .parent()
      .parent()
      .should('have.attr', 'data-testid', 'selected');
    cy.get('@dialog').findByTestId('select-value').click();
    checkReferenceValues('testMonoRefToMetaclass', ['Class']);
    // clear the reference list
    cy.getByTestId('testMonoRefToMetaclass-clear').click();
    checkReferenceValues('testMonoRefToMetaclass', []);
    // create new instance of Class under Package
    createReferenceNewObject('testMonoRefToMetaclass', 'Package', 'packagedElement::Class');
    cy.checkChildren('Package', ['Class1']);

    // Check persisted value
    refreshView();
    checkReferenceValues('testMonoRefToMetaclass', ['Class1']);
  });

  it('Check multi reference to metaclass', () => {
    // multi ref to Metaclass => testMultiRefToMetaclass
    checkReferenceValues('testMultiRefToMetaclass', []);
    cy.checkDropdownContent('testMultiRefToMetaclass', ['UseCase1', 'UseCase2']);
    cy.selectDropdownValue('testMultiRefToMetaclass', 'UseCase1');
    checkReferenceValues('testMultiRefToMetaclass', ['UseCase1']);
    // add another value using the transfer modal
    cy.getByTestId('testMultiRefToMetaclass-more').click();
    cy.getByTestId('transfer-modal').should('be.visible').as('dialog');
    checkReferenceTransferModalRightContent(['UseCase1']);
    cy.get('@dialog').findByTestId('tree-root-elements').findByTestId('model4test-toggle').should('be.visible').click();
    cy.get('@dialog')
      .findByTestId('tree-root-elements')
      .findByTestId('UseCase2')
      .scrollIntoView()
      .should('be.visible')
      .click();
    cy.get('@dialog')
      .findByTestId('tree-root-elements')
      .findByTestId('UseCase2')
      .parent()
      .parent()
      .should('have.attr', 'data-testid', 'selected');
    cy.get('@dialog').findByTestId('move-right').click();
    checkReferenceTransferModalRightContent(['UseCase1', 'UseCase2']);
    cy.get('@dialog').findByTestId('close-transfer-modal').click();
    checkReferenceValues('testMultiRefToMetaclass', ['UseCase1', 'UseCase2']);
    // create new UseCase under model
    createReferenceNewObject('testMultiRefToMetaclass', 'model4test', 'packagedElement::UseCase');

    // Check persisted value
    refreshView();

    checkReferenceValues('testMultiRefToMetaclass', ['UseCase1', 'UseCase2', 'UseCase3']);
  });

  it('Check mono reference to stereotype application', () => {
    // multi ref to stereotype application => testMonoReftoStereotype2
    checkReferenceValues('testMonoReftoStereotype2', []);
    cy.checkDropdownContent('testMonoReftoStereotype2', ['Activity']);
    cy.selectDropdownValue('testMonoReftoStereotype2', 'Activity');
    checkReferenceValues('testMonoReftoStereotype2', ['Activity']);
    // remove chip
    cy.getByTestId('reference-value-Activity').find('svg').click();
    checkReferenceValues('testMonoReftoStereotype2', []);

    // select reference value using dialog
    cy.getByTestId('testMonoReftoStereotype2-more').click();
    cy.getByTestId('browse-modal').should('be.visible').as('dialog');
    cy.get('@dialog').findByTestId('tree-root-elements').findByTestId('model4test-toggle').should('be.visible').click();
    cy.get('@dialog').findByTestId('tree-root-elements').findByTestId('Activity').should('be.visible').click();
    cy.get('@dialog')
      .findByTestId('tree-root-elements')
      .findByTestId('Activity')
      .parent()
      .parent()
      .should('have.attr', 'data-testid', 'selected');
    cy.get('@dialog').findByTestId('select-value').click();
  });

  it('Check multi reference to stereotype application', () => {
    // multi ref to stereotype application => testMultiReftoStereotype2
    checkReferenceValues('testMultiReftoStereotype2', []);
    cy.checkDropdownContent('testMultiReftoStereotype2', ['Activity']);
    cy.selectDropdownValue('testMultiReftoStereotype2', 'Activity');
    checkReferenceValues('testMultiReftoStereotype2', ['Activity']);
    // remove chip
    cy.getByTestId('testMultiReftoStereotype2').findByTestId('reference-value-Activity').find('svg').click();
    checkReferenceValues('testMultiReftoStereotype2', []);
    // select reference value using dialog
    cy.getByTestId('testMultiReftoStereotype2-more').click();
    cy.getByTestId('transfer-modal').should('be.visible').as('dialog');
    checkReferenceTransferModalRightContent([]);
    cy.get('@dialog').findByTestId('tree-root-elements').findByTestId('model4test-toggle').should('be.visible').click();
    cy.get('@dialog').findByTestId('tree-root-elements').findByTestId('Activity').should('be.visible').click();
    cy.get('@dialog')
      .findByTestId('tree-root-elements')
      .findByTestId('Activity')
      .parent()
      .parent()
      .should('have.attr', 'data-testid', 'selected');
    cy.get('@dialog').findByTestId('move-right').click();
    cy.get('@dialog').findByTestId('close-transfer-modal').click();
  });

  const applyProfileAndStereotypes = () => {
    cy.applyProfileByMenu('model4test', profileName);

    // Apply stereotype2 on Activity
    applyStereotypeOn(`${profileName}::Stereotype2`, 'Activity');
    // Apply stereotype1 on Class
    applyStereotypeOn(`${profileName}::Stereotype1`, 'Class');
    // check Stereotype1 page is present
    cy.activateDetailsTabAndWaitForElement('Stereotype1', 'testInt');
  };

  const refreshView = () => {
    cy.getByTestId('«Stereotype2» Activity').click();
    cy.getByTestId('«Stereotype1» Class').click();
    // wait until selection on Class is complete
    cy.getByTestId(`page-tab-UML`).should('have.class', 'Mui-selected');
    cy.activateDetailsTab('Stereotype1');
  };

  const applyStereotypeOn = (stereotype, element) => {
    cy.getByTestId(`${element}-more`).should('be.visible').click();
    cy.getByTestId('apply-stereotype').should('be.visible').click();
    cy.getByTestId('stereotype').should('not.have.class', 'Mui-disabled').click();
    cy.get('#menu-')
      .find(`ul[aria-labelledby="newDocumentModalStereotypeDescriptionLabel"]`)
      .findByTestId(stereotype)
      .click();
    cy.getByTestId('apply-stereotype-submit').click();
  };

  const checkPrimitiveRadioState = (value, isChecked) => {
    cy.getByTestId(`primitive-radio-${value}`)
      .children()
      .first()
      .should(`${isChecked ? '' : 'not.'}have.class`, 'Mui-checked');
  };

  const checkPrimitiveListIsEmpty = (primitiveListName) => {
    cy.getByTestId(`primitive-list-table-${primitiveListName}`).children().as('pl-items').should('have.length', 2);
    cy.get('@pl-items').first().contains('None');
  };

  const addItemInPrimitiveListFreeMode = (primitiveListName, itemValue) => {
    cy.getByTestId(`primitive-list-add-section-${primitiveListName}`).find('input').clear().type(itemValue);
    cy.getByTestId(`primitive-list-add-${primitiveListName}`).click();
    cy.getByTestId(`primitive-list-table-${primitiveListName}`)
      .findByTestId(`primitive-list-item-${itemValue}`)
      .should('exist');
  };

  const addItemInPrimitiveListStrictMode = (primitiveListName, itemValue) => {
    cy.getByTestId(`primitive-list-autocomplete-textfield-${primitiveListName}`).click();
    cy.get(`.MuiAutocomplete-popper`).should('exist').find(`ul > li`).contains(itemValue).should('exist').click();
    cy.getByTestId(`primitive-list-add-${primitiveListName}`).should('not.have.class', 'Mui-disabled').click();
    cy.getByTestId(`primitive-list-table-${primitiveListName}`)
      .findByTestId(`primitive-list-item-${itemValue}`)
      .should('exist');
  };

  const checkPrimitiveListContent = (primitiveListName, values) => {
    cy.getByTestId(`primitive-list-table-${primitiveListName}`)
      .children()
      .should('have.length', values.length + 1);
    values.forEach((val) => {
      cy.getByTestId(`primitive-list-item-${val}`).should('exist');
    });
  };

  const checkReferenceValues = (refName, values) => {
    cy.getByTestId(refName)
      .find('.MuiChip-root')
      .should(($chips) => {
        const optionTexts = $chips
          .toArray()
          .map((el) => el.getAttribute('data-testid').substring('reference-value-'.length));
        expect(optionTexts).to.deep.eq(values);
      });
  };

  const chooseSelectValue = (selectTestId, value) => {
    cy.getByTestId(selectTestId).should('not.have.class', 'Mui-disabled').click();
    cy.get('#menu-').find(`li[data-value="${value}"]`).should('be.visible').click();
  };

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

  const createReferenceNewObject = (refName, containerName, childDescription) => {
    cy.openReferenceChildCreationDialog(refName, containerName);
    chooseSelectValue('childCreationDescription', childDescription);
    cy.getByTestId('create-object').click();
    cy.getByTestId('create-modal').should('not.exist');
  };
});

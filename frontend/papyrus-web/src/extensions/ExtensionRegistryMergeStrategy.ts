/*******************************************************************************
 * Copyright (c) 2025 CEA LIST.
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
import { DataExtension } from '@eclipse-sirius/sirius-components-core';
import { DefaultExtensionRegistryMergeStrategy } from '@eclipse-sirius/sirius-web-application';

export class ExtensionRegistryMergeStrategy extends DefaultExtensionRegistryMergeStrategy {
  public override mergeDataExtensions(
    identifier: string,
    existingValues: DataExtension<any>,
    newValues: DataExtension<any>
  ): DataExtension<any> {
    if (identifier === 'apolloClient#apolloClientOptionsConfigurers') {
      return this.mergeApolloClientContributions(existingValues, newValues);
    }
    if (identifier === 'navigationBarMenu#helpURL') {
      return this.mergeNavigationBarMenuHelpURL(existingValues, newValues);
    }
    return newValues;
  }

  private mergeApolloClientContributions(
    existingApolloClientContributions: DataExtension<any>,
    newApolloClientContributions: DataExtension<any>
  ): DataExtension<any> {
    return {
      identifier: 'papyrusweb_apolloClient#apolloClientOptionsConfigurers',
      data: [...existingApolloClientContributions.data, ...newApolloClientContributions.data],
    };
  }

  private mergeNavigationBarMenuHelpURL(
    apolloClientOptionsConfigurers: DataExtension<any>,
    _otherApolloClientOptionsConfigurers: DataExtension<any>
  ): DataExtension<any> {
    return {
      identifier: '`papyrus_web_doc_navigationBarMenu#helpURL',
      data: apolloClientOptionsConfigurers.data,
    };
  }
}

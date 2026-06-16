/*******************************************************************************
 * Copyright (c) 2025, 2026 CEA LIST, Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *     Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Issue 283
 *******************************************************************************/
import { DataExtension } from '@eclipse-sirius/sirius-components-core';
import { omniboxCommandOverrideContributionExtensionPoint } from '@eclipse-sirius/sirius-components-omnibox';
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
    if (identifier === omniboxCommandOverrideContributionExtensionPoint.identifier) {
      return this.mergeOmniboxCommandOverrideContributions(existingValues, newValues);
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

  private mergeOmniboxCommandOverrideContributions(
    existingOmniboxCommandOverrideContributions: DataExtension<any>,
    newOmniboxCommandOverrideContributions: DataExtension<any>
  ): DataExtension<any> {
    const result = {
      identifier: `papyrusweb_omnibox${omniboxCommandOverrideContributionExtensionPoint.identifier}_merged`,
      data: [...existingOmniboxCommandOverrideContributions.data, ...newOmniboxCommandOverrideContributions.data],
    };

    return result;
  }
}

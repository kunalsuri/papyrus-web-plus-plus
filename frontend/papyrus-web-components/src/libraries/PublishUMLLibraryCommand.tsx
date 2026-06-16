/*****************************************************************************
 * Copyright (c) 2026 Obeo, CEA LIST.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *****************************************************************************/

import { OmniboxCommandComponentProps } from '@eclipse-sirius/sirius-components-omnibox';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import { useState } from 'react';
import { PublishLibraryDialog } from '@eclipse-sirius/sirius-web-application';
import { PublishLibraryCommandState } from './PublishUMLLibraryCommand.types';

export const PublishUMLLibraryCommand = ({ command, onKeyDown, onClose }: OmniboxCommandComponentProps) => {
  const [state, setState] = useState<PublishLibraryCommandState>({
    open: false,
  });

  const handleClick = () => setState((prevState) => ({ ...prevState, open: true }));

  return (
    <>
      <ListItemButton key={command.id} data-testid={command.label} onClick={handleClick} onKeyDown={onKeyDown}>
        <ListItemIcon>{command.icon}</ListItemIcon>
        <ListItemText sx={{ whiteSpace: 'nowrap', textOverflow: 'ellipsis' }}>{command.label}</ListItemText>
      </ListItemButton>
      {state.open ? (
        <PublishLibraryDialog
          open={state.open}
          title={'Publish UML Library'}
          message={'Register your UML Model as a library'}
          publicationKind={'publishUMLModel'}
          onClose={onClose}
        />
      ) : null}
    </>
  );
};

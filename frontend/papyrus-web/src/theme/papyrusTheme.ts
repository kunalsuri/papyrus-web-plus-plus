/*******************************************************************************
 * Copyright (c) 2026 Obeo, CEA LIST
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *******************************************************************************/

import { theme } from '@eclipse-sirius/sirius-components-core';
import { Theme, createTheme } from '@mui/material/styles';

//adapted from SysON Theme

export const baseTheme: Theme = createTheme({
  ...theme,
  palette: {
    mode: 'light',
    primary: {
      main: '#0F766E',
      dark: '#94ddc2',
      light: '#534E75',
    },
    secondary: {
      main: '#94ddc2',
      dark: '#0F766E',
      light: '#D2D3D9',
    },
    text: {
      primary: '#292253',
      disabled: '#29225354',
    },
    error: {
      main: '#DE1000',
      dark: '#9B0B00',
      light: '#E43F33',
    },
    success: {
      main: '#43A047',
      dark: '#327836',
      light: '#4EBA54',
    },
    warning: {
      main: '#FF9800',
      dark: '#D98200',
      light: '#FFB800',
    },
    info: {
      main: '#2196F3',
      dark: '#1D7DCC',
      light: '#24A7FF',
    },
    divider: '#BFE9DB',
    navigation: {
      leftBackground: '#BFE9DB',
      rightBackground: '#BFE9DB',
    },
    navigationBar: {
      border: '#0F766E',
      background: '#0F766E',
      //background: '#A2ABAD', //Pauline
    },
    selected: '#FF9800',
    action: {
      hover: '#A1A4C436',
      selected: '#A1A4C460',
      disabledBackground: '#E0E0E0',
    },
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        colorPrimary: 'secondary',
      },
    },
    MuiSnackbarContent: {
      styleOverrides: {
        root: {
          backgroundColor: '#64669B',
        },
      },
    },
  },
});

const container = () => {
  return document.fullscreenElement ?? document.body;
};

export const papyrusTheme = createTheme(
  {
    components: {
      MuiAvatar: {
        styleOverrides: {
          colorDefault: {
            backgroundColor: baseTheme.palette.primary.main,
          },
        },
      },
      MuiMenu: {
        defaultProps: {
          container,
        },
      },
      MuiTooltip: {
        defaultProps: {
          PopperProps: {
            container,
          },
        },
        styleOverrides: {
          tooltip: {
            backgroundColor: baseTheme.palette.common.black,
          },
        },
      },
    },
  },
  baseTheme
);

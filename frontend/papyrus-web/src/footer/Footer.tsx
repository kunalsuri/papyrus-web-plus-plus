/*******************************************************************************
 * Copyright (c) 2021, 2024 Obeo.
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
import Link from '@mui/material/Link';
import { makeStyles } from 'tss-react/mui';
import Typography from '@mui/material/Typography';

const useFooterStyles = makeStyles()((theme) => ({
  footer: {
    display: 'flex',
    justifyContent: 'center',
    margin: theme.spacing(2),
    '& > *': {
      marginLeft: theme.spacing(0.5),
      marginRight: theme.spacing(0.5),
    },
  },
}));

export const Footer = () => {
  const { classes } = useFooterStyles();
  return (
    <footer className={classes.footer}>
      <Typography variant="caption">
        &copy; {new Date().getFullYear()} CEA LIST, Obeo, Artal. Powered by&nbsp;
      </Typography>
      <Link
        variant="caption"
        href="https://gitlab.eclipse.org/eclipse/papyrus/org.eclipse.papyrus-web"
        rel="noopener noreferrer"
        target="_blank">
        Papyrus Web.
      </Link>
      &nbsp;
      <Typography variant="caption">Papyrus-Web is developed under the&nbsp;</Typography>
      <Link variant="caption" href="https://www.eclipse.org/legal/epl-2.0" rel="noopener noreferrer" target="_blank">
        EPL v2 License.
      </Link>
    </footer>
  );
};

/*****************************************************************************
 * Copyright (c) 2024 CEA LIST, Obeo, Artal Technologies.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - initial API and implementation
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issue 224
 *******************************************************************************/

// Adapted from https://github.com/eclipse-sirius/sirius-web/blob/master/packages/diagrams/frontend/sirius-components-diagrams/src/renderer/Label.tsx

import { getCSSColor, IconOverlay } from '@eclipse-sirius/sirius-components-core';
import { Theme, useTheme } from '@mui/material/styles';
import { memo, useContext } from 'react';
import { DiagramContext } from '@eclipse-sirius/sirius-components-diagrams';
import { DiagramContextValue } from '@eclipse-sirius/sirius-components-diagrams';
import { Label, NoteLabelProps } from './NoteNode.types';
import { useDiagramDirectEdit } from '@eclipse-sirius/sirius-components-diagrams';
import { DiagramDirectEditInput } from '@eclipse-sirius/sirius-components-diagrams';
const isDisplayTopHeaderSeparator = (label: Label): boolean => {
  if ('displayHeaderSeparator' in label) {
    return label.displayHeaderSeparator && label.headerPosition === 'BOTTOM';
  }
  return false;
};

const isDisplayBottomHeaderSeparator = (label: Label): boolean => {
  if ('displayHeaderSeparator' in label) {
    return label.displayHeaderSeparator && label.headerPosition === 'TOP';
  }
  return false;
};

const getHeaderSeparatorStyle = (label: Label): React.CSSProperties | undefined => {
  if ('headerSeparatorStyle' in label) {
    return label.headerSeparatorStyle;
  }
  return undefined;
};

const labelStyle = (theme: Theme, style: React.CSSProperties, faded: Boolean): React.CSSProperties => {
  return {
    maxHeight: '100%',
    opacity: faded ? '0.4' : '',
    pointerEvents: 'all',
    display: 'flex',
    whiteSpace: 'pre-line',
    ...style,
    color: style.color ? getCSSColor(String(style.color), theme) : undefined,
  };
};

const labelContentStyle = (theme: Theme, label: Label): React.CSSProperties => {
  const labelContentStyle: React.CSSProperties = {
    display: 'flex',
    overflow: 'normal',
    height: '100%',
  };

  return {
    ...labelContentStyle,
    ...label.contentStyle,
    background: label.contentStyle.background ? getCSSColor(String(label.contentStyle.background), theme) : undefined,
    borderColor: label.contentStyle.borderColor
      ? getCSSColor(String(label.contentStyle.borderColor), theme)
      : undefined,
  };
};

const labelOverflowStyle = (): React.CSSProperties => {
  const style: React.CSSProperties = {};
  style.overflow = 'auto';
  style.overflowWrap = 'anywhere';

  return style;
};

export const NoteLabel = memo(({ diagramElementId, label, faded }: NoteLabelProps) => {
  const theme: Theme = useTheme();
  const { currentlyEditedLabelId, editingKey, resetDirectEdit } = useDiagramDirectEdit();
  const { readOnly } = useContext<DiagramContextValue>(DiagramContext);

  const handleClose = () => {
    resetDirectEdit();
    const diagramElement = document.querySelector(`[data-id="${diagramElementId}"]`);
    if (diagramElement instanceof HTMLElement) {
      diagramElement.focus();
    }
  };

  const content: JSX.Element =
    label.id === currentlyEditedLabelId && !readOnly ? (
      <DiagramDirectEditInput editingKey={editingKey} onClose={handleClose} labelId={label.id} />
    ) : (
      <div
        data-id={`${label.id}-content`}
        data-testid={`NoteLabel content - ${label.text}`}
        style={labelContentStyle(theme, label)}>
        <IconOverlay iconURL={label.iconURL} alt={label.text} customIconStyle={{ marginRight: theme.spacing(1) }} />
        <div style={labelOverflowStyle()}>{label.text}</div>
      </div>
    );
  return (
    <>
      {isDisplayTopHeaderSeparator(label) && (
        <div data-testid={`NoteLabel top separator - ${label.text}`} style={getHeaderSeparatorStyle(label)} />
      )}
      <div
        data-id={label.id}
        data-testid={`NoteLabel - ${label.text}`}
        style={labelStyle(theme, label.style, faded)}
        className="nopan">
        {content}
      </div>
      {isDisplayBottomHeaderSeparator(label) && (
        <div data-testid={`NoteLabel bottom separator - ${label.text}`} style={getHeaderSeparatorStyle(label)} />
      )}
    </>
  );
});

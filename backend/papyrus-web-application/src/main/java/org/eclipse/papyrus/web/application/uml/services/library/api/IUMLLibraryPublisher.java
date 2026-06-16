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
 *  Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *****************************************************************************/
package org.eclipse.papyrus.web.application.uml.services.library.api;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.events.ICause;
import org.eclipse.sirius.web.domain.boundedcontexts.library.Library;

/**
 * Publishes the proper UML contents of an {@link IEditingContext} as a {@link Library}.
 *
 * Adapted from SysON : ISysMLLibraryPublisher
 *
 * @author Vincent LORENZO
 *
 */
public interface IUMLLibraryPublisher {

    IPayload publish(ICause cause, IEditingContext libraryAuthoringEditingContext, String libraryNamespace, String libraryName, String libraryVersion, String libraryDescription);

}

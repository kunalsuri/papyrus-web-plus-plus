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
 *  Vincent LORENZO (CEA LIST) vincent.lorenzo@cea.fr - Initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.web.application.uml.services.library.api;

import java.util.function.Predicate;

/**
 * Used to test if an editing context is capable of supporting a UML Model.
 *
 * Adapted from IStudioCapableEditingContextPredicate
 *
 * @author Vincent LORENZO
 */
public interface IPapyrusCapableEditingContextPredicate extends Predicate<String> {

}

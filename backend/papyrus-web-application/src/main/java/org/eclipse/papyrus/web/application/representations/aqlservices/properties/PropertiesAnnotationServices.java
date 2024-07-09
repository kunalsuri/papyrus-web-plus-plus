/*****************************************************************************
 * Copyright (c) 2023, 2024 CEA LIST, Obeo, Artal Technologies.
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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issues 210, 218
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.aqlservices.properties;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Element;

/**
 * Properties Annotation services.
 *
 * @author tiboue
 */
public class PropertiesAnnotationServices {

    private static final String SYMBOL = "Symbol";

    private static final String SRC_PAPYRUS = "org.eclipse.papyrus";

    public String getSymbolValue(Element element) {
        EAnnotation annotation = element.getEAnnotation(SRC_PAPYRUS);
        if (annotation != null) {
            String value = annotation.getDetails().get(SYMBOL);
            if (value != null) {
                return value;
            }
        }
        return "";
    }

    public EObject createOrUpdateAnnotation(Element element, String newUuid) {
        EAnnotation annotation = element.getEAnnotation(SRC_PAPYRUS);
        if (annotation == null) {
            annotation = EcoreFactory.eINSTANCE.createEAnnotation();
            annotation.setSource(SRC_PAPYRUS);
            element.getEAnnotations().add(annotation);
        }
        annotation.getDetails().put(SYMBOL, newUuid);
        return element;

    }

    public EObject removeSymbolFromAnnotation(Element element) {
        EAnnotation annotation = element.getEAnnotation(SRC_PAPYRUS);
        if (annotation != null) {
            annotation.getDetails().removeKey(SYMBOL);
            if (annotation.getDetails().size() == 0) {
                EcoreUtil.delete(annotation);
            }
        }

        return element;
    }
}

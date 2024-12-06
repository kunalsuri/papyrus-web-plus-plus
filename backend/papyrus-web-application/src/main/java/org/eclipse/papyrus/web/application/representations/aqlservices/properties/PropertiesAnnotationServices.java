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
 *  Titouan BOUETE-GIRAUD (Artal Technologies) - Issues 210, 218, 219, 227, 229
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.aqlservices.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.web.application.representations.aqlservices.utils.ViewHelper;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.services.ObjectService;
import org.eclipse.sirius.components.diagrams.ListLayoutStrategy;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.emf.diagram.api.IViewDiagramDescriptionSearchService;
import org.eclipse.uml2.uml.Element;
import org.springframework.stereotype.Service;

/**
 * Properties Annotation services.
 *
 * @author tiboue
 */
@Service
public class PropertiesAnnotationServices {

    private static final String SYMBOL = "Symbol";

    private static final String CONTENT = "Content";

    private static final String SRC_PAPYRUS = "org.eclipse.papyrus";

    private final IViewDiagramDescriptionSearchService viewDiagramDescriptionSearchService;

    private final ObjectService objectService;

    /**
     * Instantiates a new properties annotation services.
     *
     * @param viewDiagramDescriptionSearchService
     *            the view diagram description search service
     * @param objectService
     *            the object service
     */
    public PropertiesAnnotationServices(IViewDiagramDescriptionSearchService viewDiagramDescriptionSearchService, ObjectService objectService) {
        this.viewDiagramDescriptionSearchService = Objects.requireNonNull(viewDiagramDescriptionSearchService);
        this.objectService = Objects.requireNonNull(objectService);
    }

    /**
     * Gets the symbol value.
     *
     * @param element
     *            the element
     * @return the symbol value
     */
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

    /**
     * Creates or update the annotation.
     *
     * @param element
     *            the element
     * @param newUuid
     *            the new uuid
     * @return the object
     */
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

    /**
     * Allow to select on which element we add the Symbol page.
     *
     * @param element
     *            the element
     * @return true if we want to display the page
     */
    public boolean shouldDisplaySymbolPage(Element element) {
        return true;
    }

    /**
     * Removes the symbol from annotation.
     *
     * @param element
     *            the element
     * @return the object
     */
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

    /**
     * Gets the all the symbol nodes.
     *
     * @param diagramContext
     *            the diagram context
     * @param editionContext
     *            the edition context
     * @return the list of nodes that are symbol
     */
    public List<Node> getAllSymbol(IDiagramContext diagramContext, IEditingContext editionContext) {
        List<Node> nodes = ViewHelper.getAllNodes(diagramContext.getDiagram());
        List<Node> result = new ArrayList<>();

        for (Node node : nodes) {
            Optional<NodeDescription> optionalNodeDesc = this.viewDiagramDescriptionSearchService.findViewNodeDescriptionById(editionContext, node.getDescriptionId());
            if (optionalNodeDesc.isPresent()) {
                NodeDescription nodeDesc = optionalNodeDesc.get();
                if (nodeDesc.getName().contains(SYMBOL)) {
                    result.add(node);
                }
            }
        }

        return result;
    }

    /**
     * Gets all the nodes which are not symbol.
     *
     * @param diagramContext
     *            the diagram context
     * @param editionContext
     *            the edition context
     * @return the list of nodes that are not symbol
     */
    public List<Node> getAllNonSymbol(IDiagramContext diagramContext, IEditingContext editionContext) {
        List<Node> nodes = ViewHelper.getAllNodes(diagramContext.getDiagram());
        List<Node> result = new ArrayList<>();

        for (Node node : nodes) {
            if (node.getChildrenLayoutStrategy() instanceof ListLayoutStrategy) {
                for (Node child : node.getChildNodes()) {
                    Optional<NodeDescription> optionalChildNodeDesc = this.viewDiagramDescriptionSearchService.findViewNodeDescriptionById(editionContext, child.getDescriptionId());
                    if (optionalChildNodeDesc.isPresent()) {
                        NodeDescription childNodeDesc = optionalChildNodeDesc.get();
                        String semanticElementId = node.getTargetObjectId();

                        Optional<Object> optSemanticElement = this.objectService.getObject(editionContext, semanticElementId);
                        if (optSemanticElement.get() instanceof Element elem) {
                            if (!this.getSymbolValue(elem).equals("") && !childNodeDesc.getName().contains(SYMBOL) && !childNodeDesc.getName().contains(CONTENT)) {
                                result.add(child);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}

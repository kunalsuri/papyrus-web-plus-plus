/*****************************************************************************
 * Copyright (c) 2022, 2025 CEA LIST, Obeo, Artal Technologies.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Obeo - Initial API and implementation
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.view;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;

/**
 * Build ids using consistent rules.
 *
 * @author Arthur Daussy
 */
public class IdBuilder {
    public static final String SPACE = " ";

    public static final String NEW = "New ";

    public static final String COMPARTMENT_NODE_SUFFIX = "_CompartmentNode";

    public static final String CONTENT_NODE_SUFFIX = "_Content";

    public static final String HOLDER_NODE_SUFFIX = "_Holder";

    public static final String SHARED_SUFFIX = "_SHARED";

    private static final String GRAPHICAL_DROP_TOOL = "GraphicalDropTool_";

    private static final String FAKE_CHILD_LABEL_NODE = "_FakeChildLabelNode";

    private static final String UNDERSCORE = "_";

    private static final Pattern WORD_FINDER = Pattern.compile("(([A-Z]?[a-z]+)|([A-Z]))");

    private final String diagramPrefix;

    private IDomainHelper metamodelHelper;

    public IdBuilder(String diagramPrefix, IDomainHelper metamodelHelper) {
        super();
        this.diagramPrefix = diagramPrefix;
        this.metamodelHelper = metamodelHelper;
    }

    public List<String> findWordsInMixedCase(String text) {
        Matcher matcher = WORD_FINDER.matcher(text);
        List<String> words = new ArrayList<>();
        while (matcher.find()) {
            words.add(matcher.group(0));
        }
        return words;
    }

    public String getCreationToolId(EClass newElementType) {
        return NEW + this.findWordsInMixedCase(newElementType.getName()).stream().collect(joining(SPACE));
    }

    public String getSiblingCreationToolId(EClass newElementType) {
        return NEW + " Sibling " + this.findWordsInMixedCase(newElementType.getName()).stream().collect(joining(SPACE));
    }

    /**
     * Id to be used for fake child nodes only used to contains tools. See
     * https://github.com/PapyrusSirius/papyrus-web/issues/164
     *
     * @param parentNode
     *            the parent node
     * @return an id
     */
    // Workaround for https://github.com/PapyrusSirius/papyrus-web/issues/164
    public String getFakeChildNodeId(NodeDescription parentNode) {
        return parentNode.getName() + FAKE_CHILD_LABEL_NODE;
    }

    public static boolean isCompartmentNode(NodeDescription description) {
        String name = description.getName();
        return name != null && name.endsWith(COMPARTMENT_NODE_SUFFIX);
    }

    public static boolean isContentNode(NodeDescription description) {
        String name = description.getName();
        return name != null && name.endsWith(CONTENT_NODE_SUFFIX);
    }

    public static boolean isHolderNode(NodeDescription description) {
        String name = description.getName();
        return name != null && name.endsWith(HOLDER_NODE_SUFFIX);
    }

    // Workaround for https://github.com/PapyrusSirius/papyrus-web/issues/164
    public static boolean isFakeChildNode(DiagramElementDescription description) {
        String name = description.getName();
        return name != null && name.endsWith(FAKE_CHILD_LABEL_NODE);
    }

    public String getDomainBaseEdgeId(EClass domain) {
        return this.diagramPrefix + domain.getName() + "_DomainEdge";
    }

    public String getFeatureBaseEdgeId(EStructuralFeature feature) {
        return this.diagramPrefix + feature.getEContainingClass().getName() + UNDERSCORE + feature.getName() + "_FeatureEdge";
    }

    private String getBaseName(DiagramElementDescription description) {
        String base = this.findWordsInMixedCase(this.metamodelHelper.toEClass(description.getDomainType()).getName()).stream().collect(joining(SPACE));
        return base;
    }

    public String getDomainNodeName(EClass domain) {
        return this.diagramPrefix + domain.getName();
    }

    public String getSpecializedDomainNodeName(EClass domain, String specialization) {
        return this.diagramPrefix + domain.getName() + UNDERSCORE + specialization;
    }

    public String getCompartmentDomainNodeName(EClass domain, String compartmentName) {
        return this.diagramPrefix + domain.getName() + UNDERSCORE + compartmentName + UNDERSCORE + COMPARTMENT_NODE_SUFFIX;
    }

    public String getSpecializedCompartmentDomainNodeName(EClass domain, String compartmentName, String specialization) {
        return this.diagramPrefix + domain.getName() + UNDERSCORE + compartmentName + UNDERSCORE + specialization + COMPARTMENT_NODE_SUFFIX;
    }

    public String getListItemDomainNodeName(EClass domain, EClass parentContainer) {
        return this.diagramPrefix + domain.getName() + SHARED_SUFFIX;
    }

    public String getNodeGraphicalDropToolName(NodeDescription nodeToCreate) {
        return this.diagramPrefix + GRAPHICAL_DROP_TOOL + this.getBaseName(nodeToCreate);
    }

    public String getSpecializedNodeGraphicalDropToolName(NodeDescription nodeToCreate, String specialization) {
        return this.diagramPrefix + GRAPHICAL_DROP_TOOL + this.getBaseName(nodeToCreate) + UNDERSCORE + specialization;
    }

    public String getDiagramSemanticDropToolName() {
        return this.diagramPrefix + "SemanticDropTool";
    }

    public String getDiagramGraphicalDropToolName() {
        return this.diagramPrefix + "GraphicalDropTool";
    }

    public String getCreationToolId(EdgeDescription description) {
        if (description.isIsDomainBasedEdge()) {
            return NEW + this.getBaseName(description);
        }
        // Improve this
        return this.diagramPrefix + this.getBaseName(description) + "_EdgeTool";
    }

    public String getSourceReconnectionToolId(EdgeDescription padPackageMerge) {
        return padPackageMerge.getName() + "_SourceReconnectionTool";
    }

    public String getTargetReconnectionToolId(EdgeDescription padPackageMerge) {
        return padPackageMerge.getName() + "_TargetReconnectionTool";
    }

}
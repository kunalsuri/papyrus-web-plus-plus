/*****************************************************************************
 * Copyright (c) 2022, 2024 CEA LIST, Obeo, Artal Technolgies.
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
 *  Aurelien Didier (Artal Technologies) - Issue 229
 *****************************************************************************/
package org.eclipse.papyrus.web.application.representations.uml;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.papyrus.web.application.representations.utils.DiagramDescriptionDescriptionValidator;
import org.eclipse.papyrus.web.application.representations.view.IdBuilder;
import org.eclipse.papyrus.web.tests.utils.Status;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test that validates all diagram descriptions.
 *
 * @author Arthur Daussy
 */
public class RepresentationValidatorTests {

    private static final String EOL = "\n";

    private DiagramDescriptionDescriptionValidator buildeDefaultValidator() {
        return new DiagramDescriptionDescriptionValidator()//
                .excludeFromDeleteToolValidation(p -> !IdBuilder.isFakeChildNode(p) && this.isNotCompartment(p))//
                .excludeFromDirectEditValidation(p -> !IdBuilder.isFakeChildNode(p) && this.isNotCompartment(p));
    }

    @Test
    public void validateActivityDiagram() {
        DiagramDescription diagram = new ADDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = this.buildeDefaultValidator();

        Predicate<DiagramElementDescription> isNotActivityRoot = p -> !p.getName().equals("AD_Activity_Holder") || p.getName().equals("AD_Activity_Content");
        Predicate<DiagramElementDescription> isNotDecisionNodeNote = p -> !p.getName().equals("AD_DecisionNode_Note_SHARED");
        validator.excludeFromDeleteToolValidation(isNotActivityRoot.and(isNotDecisionNodeNote));
        validator.excludeFromDirectEditValidation(isNotDecisionNodeNote);
        List<Status> validations = validator.validate(diagram);

        List<Status> errors = validations.stream().filter(v -> !v.isValid()).toList();

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Activity Diagram description \n{0}", errors.stream().map(e -> e.getMessage()).collect(joining(EOL))));
        }
    }

    @Test
    public void validateClassDiagram() {
        DiagramDescription diagram = new CDDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = this.buildeDefaultValidator();
        validator.disableReusedNodeDescriptionsValidation();
        validator.disableSharedDescriptionsValidation();

        List<Status> validations = validator.validate(diagram);

        List<Status> errors = validations.stream().filter(v -> !v.isValid()).collect(toList());

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Class Diagram description : \n{0}", errors.stream().map(Status::getMessage).collect(joining(EOL))));
        }
    }

    @Test
    public void validateCommmunicationDiagram() {
        DiagramDescription diagram = new CODDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = this.buildeDefaultValidator();

        Predicate<DiagramElementDescription> isNotInteractionRoot = p -> !p.getName().equals("COD_Interaction_Holder");
        validator.excludeFromDeleteToolValidation(isNotInteractionRoot);
        validator.excludeFromDeleteToolValidation(isNotInteractionRoot);

        List<Status> validations = validator.validate(diagram);

        List<Status> errors = validations.stream().filter(v -> !v.isValid()).toList();

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Communication Diagram description \n{0}", errors.stream().map(e -> e.getMessage()).collect(joining(EOL))));
        }
    }

    @Test
    public void validateComponentDiagram() {
        DiagramDescription diagram = new CPDDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = this.buildeDefaultValidator();
        validator.excludeFromDirectEditValidation(p -> !this.isCpdDirectEditDisabled(p));
        List<Status> validations = validator.validate(diagram);
        List<Status> errors = validations.stream().filter(v -> !v.isValid()).toList();

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Component Diagram description \n{0}", errors.stream().map(Status::getMessage).collect(joining(EOL))));
        }
    }

    @Test
    public void validateCompositeStructure() {
        DiagramDescription diagram = new CSDDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = this.buildeDefaultValidator();
        validator.disableReusedNodeDescriptionsValidation();
        validator.disableSharedDescriptionsValidation();

        List<Status> validations = validator.validate(diagram);

        List<Status> errors = validations.stream().filter(v -> !v.isValid()).collect(toList());

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Composite Structure Diagram description : \n{0}", errors.stream().map(Status::getMessage).collect(joining(EOL))));
        }
    }

    @Test
    public void validateDeploymentDiagram() {
        DiagramDescription diagram = new DDDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = this.buildeDefaultValidator();
        validator.excludeFromDirectEditValidation(p -> !this.isDdDirectEditDisabled(p));
        List<Status> validations = validator.validate(diagram);
        List<Status> errors = validations.stream().filter(v -> !v.isValid()).toList();

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Deployment Diagram description \n{0}", errors.stream().map(Status::getMessage).collect(joining(EOL))));
        }
    }

    @Test
    public void validatePackageDiagram() {
        DiagramDescription diagram = new PADDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = this.buildeDefaultValidator();
        validator.disableReusedNodeDescriptionsValidation();
        validator.disableSharedDescriptionsValidation();

        List<Status> validations = validator.validate(diagram);

        List<Status> errors = validations.stream().filter(v -> !v.isValid()).collect(toList());

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Package Diagram description : \n{0}", errors.stream().map(Status::getMessage).collect(joining(EOL))));
        }
    }

    @Test
    public void validateProfileDiagram() {
        Predicate<DiagramElementDescription> isNotMetaclassAndNotCompartment = p -> !p.getName()
                .contains(PRDDiagramDescriptionBuilder.PRD_METACLASS)
                && !p.getName().contains(IdBuilder.COMPARTMENT_NODE_SUFFIX);
        DiagramDescription diagram = new PRDDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = new DiagramDescriptionDescriptionValidator();
        // Exclude the deletion tool check for PRD_METACLASS and PRD_SHARED_METACLASS: these elements have a particular
        // behavior because they represent a metaclass from the UML metamodel and thus cannot be deleted.
        validator.excludeFromDeleteToolValidation(isNotMetaclassAndNotCompartment);
        validator.excludeFromDirectEditValidation(isNotMetaclassAndNotCompartment);
        validator.excludeFromDirectEditValidation(p -> !this.isPrdDirectEditDisabled(p));

        List<Status> validations = validator.validate(diagram);
        List<Status> errors = validations.stream().filter(v -> !v.isValid()).toList();

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Profile Diagram description : \n{0}", errors.stream().map(Status::getMessage).collect(joining(EOL))));
        }
    }

    @Test
    public void validateStateMachineDiagram() {
        DiagramDescription diagram = new SMDDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = this.buildeDefaultValidator();
        // Exclude the direct edit tool check on transition since it is a complex semantic and we do not have yet a way
        // to implement it
        validator.excludeFromDirectEditValidation(p -> !this.isTransitionEdge(p));
        validator.disableReusedNodeDescriptionsValidation();
        validator.disableSharedDescriptionsValidation();

        List<Status> validations = validator.validate(diagram);

        List<Status> errors = validations.stream().filter(v -> !v.isValid()).collect(toList());

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Class Diagram description : \n{0}", errors.stream().map(e -> e.getMessage()).collect(joining(EOL))));
        }
    }

    @Test
    public void validateUseCaseDiagram() {
        DiagramDescription diagram = new UCDDiagramDescriptionBuilder().createDiagramDescription(ViewFactory.eINSTANCE.createView());

        DiagramDescriptionDescriptionValidator validator = this.buildeDefaultValidator();
        validator.excludeFromDirectEditValidation(p -> !this.isUcdDirectEditDisabled(p));
        List<Status> validations = validator.validate(diagram);
        List<Status> errors = validations.stream().filter(v -> !v.isValid()).toList();

        if (!errors.isEmpty()) {
            Assertions.fail(MessageFormat.format("Invalid Use Case Diagram description \n{0}", errors.stream().map(Status::getMessage).collect(joining(EOL))));
        }
    }

    private boolean isUcdDirectEditDisabled(DiagramElementDescription p) {
        return "UCD_PackageMerge_DomainEdge".equals(p.getName()) || "UCD_PackageImport_DomainEdge".equals(p.getName()) || "UCD_Generalization_DomainEdge".equals(p.getName());
    }

    private boolean isCpdDirectEditDisabled(DiagramElementDescription p) {
        return "CPD_Generalization_DomainEdge".equals(p.getName());
    }

    private boolean isDdDirectEditDisabled(DiagramElementDescription p) {
        return "DD_Generalization_DomainEdge".equals(p.getName());
    }

    private boolean isPrdDirectEditDisabled(DiagramElementDescription p) {
        return "PRD_Generalization_DomainEdge".equals(p.getName());
    }

    private boolean isTransitionEdge(DiagramElementDescription p) {
        return "SMD_Transition_DomainEdge".equals(p.getName());
    }

    private boolean isNotCompartment(DiagramElementDescription diagramelementdescription1) {
        return !(diagramelementdescription1 instanceof NodeDescription) || !IdBuilder.isCompartmentNode((NodeDescription) diagramelementdescription1);
    }

}

/*****************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST, Obeo, Artal Technologies.
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
package org.eclipse.papyrus.web.tools.activity.utils;

import static org.eclipse.uml2.uml.UMLPackage.ACCEPT_CALL_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.ACTIVITY;
import static org.eclipse.uml2.uml.UMLPackage.ACTIVITY_PARTITION;
import static org.eclipse.uml2.uml.UMLPackage.ADD_STRUCTURAL_FEATURE_VALUE_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.CALL_OPERATION_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.CLEAR_ASSOCIATION_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.CLEAR_STRUCTURAL_FEATURE_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.CONDITIONAL_NODE;
import static org.eclipse.uml2.uml.UMLPackage.CREATE_OBJECT_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.DESTROY_OBJECT_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.EXPANSION_REGION;
import static org.eclipse.uml2.uml.UMLPackage.INTERRUPTIBLE_ACTIVITY_REGION;
import static org.eclipse.uml2.uml.UMLPackage.LOOP_NODE;
import static org.eclipse.uml2.uml.UMLPackage.READ_EXTENT_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.READ_IS_CLASSIFIED_OBJECT_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.READ_SELF_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.READ_STRUCTURAL_FEATURE_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.RECLASSIFY_OBJECT_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.REDUCE_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.SEND_OBJECT_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.SEND_SIGNAL_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.SEQUENCE_NODE;
import static org.eclipse.uml2.uml.UMLPackage.START_CLASSIFIER_BEHAVIOR_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.START_OBJECT_BEHAVIOR_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.STRUCTURED_ACTIVITY_NODE;
import static org.eclipse.uml2.uml.UMLPackage.TEST_IDENTITY_ACTION;
import static org.eclipse.uml2.uml.UMLPackage.VALUE_SPECIFICATION_ACTION;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * The mapping types for the Activity Diagram.
 * <p>
 * This class can be used to retrieve both node mapping types and edge mapping types.
 * </p>
 *
 * @author <a href="mailto:gwendal.daniel@obeosoft.com">Gwendal Daniel</a>
 */
public final class ADMappingTypes {

    private static final String AD_PREFIX = "AD_";

    private ADMappingTypes() {
        // private to prevent instantiation.
    }

    public static String getMappingType(EClass eClass) {
        String result = AD_PREFIX + eClass.getName();
        if (UMLPackage.eINSTANCE.getActivityEdge().isSuperTypeOf(eClass)) {
            result = AD_PREFIX + eClass.getName() + "_DomainEdge";
        }
        return result;
    }

    public static String getMappingTypeAsSubNode(EClass eClass) {
        String suffix = switch (eClass.getName()) {
            case "Activity", "ActivityPartition", "InterruptibleActivityRegion", "ClearAssociationAction", "ConditionalNode", "ExpansionRegion", "LoopNode", "ReadExtentAction", "ReadStructuralFeatureAction", "ReduceAction", "SequenceNode", "StartClassifierBehaviorAction", "StructuredActivityNode", "ValueSpecificationAction" -> "_Holder";
            default -> "";
        };
        String result = switch (eClass.getClassifierID()) {
            default -> AD_PREFIX + eClass.getName() + "_SHARED" + suffix;
        };
        return result;
    }

    public static String getMappingTypeContentAsSubNode(EClass eClass) {
        String suffix = "_Content";
        return AD_PREFIX + eClass.getName() + "_SHARED" + suffix;
    }

    public static int getExpectedNumberOfCreatedElement(EClass expectedType) {
        int expectedNumberOfPins = getNumberOfPins(expectedType);
        // If the element is an Holder/Content kind of node
        int numberOfNode = 1;
        if (isHolderContent(expectedType)) {
            numberOfNode = 2;
        }
        return expectedNumberOfPins + numberOfNode;
    }

    public static int getNumberOfPins(EClass expectedType) {
        int expectedNumberOfPins = switch (expectedType.getClassifierID()) {
            case ACCEPT_CALL_ACTION, //
                    CALL_OPERATION_ACTION, //
                    CLEAR_ASSOCIATION_ACTION, //
                    CREATE_OBJECT_ACTION, //
                    DESTROY_OBJECT_ACTION, //
                    READ_EXTENT_ACTION, //
                    READ_SELF_ACTION, //
                    RECLASSIFY_OBJECT_ACTION, //
                    SEND_SIGNAL_ACTION, //
                    START_CLASSIFIER_BEHAVIOR_ACTION, //
                    START_OBJECT_BEHAVIOR_ACTION, //
                    VALUE_SPECIFICATION_ACTION -> 1; //
            case CLEAR_STRUCTURAL_FEATURE_ACTION, //
                    READ_IS_CLASSIFIED_OBJECT_ACTION, //
                    READ_STRUCTURAL_FEATURE_ACTION, //
                    REDUCE_ACTION, //
                    SEND_OBJECT_ACTION -> 2;
            case TEST_IDENTITY_ACTION -> 3;
            case ADD_STRUCTURAL_FEATURE_VALUE_ACTION -> 4;
            default -> 0;
        };
        return expectedNumberOfPins;
    }

    public static boolean isHolderContent(EClass expectedType) {
        boolean isHolder = switch (expectedType.getClassifierID()) {
            case //
                    ACTIVITY, //
                    ACTIVITY_PARTITION, //
                    CLEAR_ASSOCIATION_ACTION, //
                    CONDITIONAL_NODE, //
                    EXPANSION_REGION, //
                    INTERRUPTIBLE_ACTIVITY_REGION, //
                    LOOP_NODE, //
                    REDUCE_ACTION, //
                    READ_EXTENT_ACTION, //
                    READ_STRUCTURAL_FEATURE_ACTION, //
                    SEQUENCE_NODE, //
                    START_CLASSIFIER_BEHAVIOR_ACTION, //
                    STRUCTURED_ACTIVITY_NODE, //
                    VALUE_SPECIFICATION_ACTION -> true;
            default -> false;
        };
        return isHolder;
    }
}

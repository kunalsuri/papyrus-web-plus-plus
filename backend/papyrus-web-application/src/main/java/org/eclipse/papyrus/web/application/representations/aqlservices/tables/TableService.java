/*******************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST.
 *
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

package org.eclipse.papyrus.web.application.representations.aqlservices.tables;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.uml.domain.services.properties.PropertiesStereotypeApplicationServices;
import org.eclipse.papyrus.web.application.tables.comment.UMLCommentTableRepresentationDescriptionBuilder;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.emf.tables.CursorBasedNavigationServices;
import org.eclipse.sirius.components.tables.ColumnFilter;
import org.eclipse.sirius.components.tables.ColumnSort;
import org.eclipse.sirius.components.tables.descriptions.PaginatedData;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.springframework.stereotype.Service;

/**
 * AQL services used in UML tables.
 *
 * @author Jerome Gout
 */
@Service
public class TableService {

    public static final String VIRTUAL_ROW_PREFIX = "PackageTableTree";

    public static final String ID_SEPARATOR = "_";

    private static final String OWNED_ATTRIBUTES = VIRTUAL_ROW_PREFIX + "ownedAttributes";

    private static final String OWNED_OPERATIONS = VIRTUAL_ROW_PREFIX + "ownedOperations";

    private static final String OWNED_NESTED_CLASSES = VIRTUAL_ROW_PREFIX + "ownedNestedClasses";

    private static final String OWNED_PARAMETERS = VIRTUAL_ROW_PREFIX + "ownedParameters";

    private final ILogger logger;

    private final IObjectService objectService;

    private final CursorBasedNavigationServices cursorBasedNavigationServices;

    private final PropertiesStereotypeApplicationServices stereotypeApplicationServices;

    private final ObjectMapper objectMapper;

    public TableService(ILogger logger, IObjectService objectService, ObjectMapper objectMapper) {
        this.logger = Objects.requireNonNull(logger);
        this.objectService = Objects.requireNonNull(objectService);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.cursorBasedNavigationServices = new CursorBasedNavigationServices();
        this.stereotypeApplicationServices = new PropertiesStereotypeApplicationServices(logger);
    }

    /**
     * Returns the label of a given UML element.
     *
     * @param element
     *         an UML element
     * @return a String that is the label of the given element
     */
    public String getElementLabel(EObject element) {
        String label = this.objectService.getLabel(element);
        // Fallback for elements which are not NamedElement and not properly handled by the Edit framework
        if (label == null || label.isBlank()) {
            label = "<" + this.splitCamelCase(element.eClass().getName() + ">");
        }
        return label;
    }

    /**
     * 'MyClassName' => 'My Class Name'
     */
    private String splitCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        result.append(input.charAt(0));

        for (int i = 1; i < input.length(); i++) {
            if (Character.isUpperCase(input.charAt(i))) {
                result.append(' ');
            }
            result.append(input.charAt(i));
        }
        return result.toString();
    }

    /**
     * Returns a String that contains all annotated elements label of a given {@link Comment} separated by a given separators.
     *
     * @param comment
     *         an UML Comment
     * @param separator
     *         a separator used between labels of the comment annotated elements.
     * @return a String that contains all annotated elements label of a given {@link Comment} separated by a given separators.
     */
    public String getCommentAnnotatedElementLabels(Comment comment, String separator) {
        return comment.getAnnotatedElements().stream()
                .map(this::getElementLabel)
                .collect(Collectors.joining(separator));
    }

    /**
     * Returns the icon path of the given UML element.
     *
     * @param element
     *         an UML Element
     * @return the list of all paths associated to the given element
     */
    public List<String> getElementIconPath(Element element) {
        return this.objectService.getImagePath(element);
    }

    /**
     * Convert a numerical index to an alphabetic one.
     *
     * @param indexObject
     *         an Integer index
     * @return the alphabetic index starting to "A" equivalent to the given index.
     */
    public String alphabetic(Integer indexObject) {
        return this.alphabetic(indexObject, 0);
    }

    public String alphabetic(Integer indexObject, int offset) {
        var index = indexObject + offset;
        if (index >= 0) {
            StringBuilder result = new StringBuilder();
            while (index >= 0) {
                result.insert(0, (char) ('A' + (index % 26)));
                index = (index / 26) - 1;
            }
            return result.toString();
        }
        return "";
    }

    /**
     * Returns the list of structural features (attribute or reference) that are used as column in the Comment table.
     *
     * @param self
     *         a comment
     * @return the structural features that are presented as column in the Comment Table
     */
    public List<EStructuralFeature> getCommentColumns(EObject self) {
        return List.of(UMLPackage.eINSTANCE.getComment_Body(), UMLPackage.eINSTANCE.getComment_AnnotatedElement());
    }

    /**
     * Returns the label used in the column header of the table for the given structural feature.
     *
     * @param self
     *         a structural feature that designate a column of the table.
     * @return the label of the column header of the table for the given structural feature.
     */
    public String getCommentColumnLabel(EStructuralFeature self) {
        String result = self.getName();
        if (UMLPackage.eINSTANCE.getComment_Body().getName().equals(self.getName())) {
            result = "Body";
        } else if (UMLPackage.eINSTANCE.getComment_AnnotatedElement().getName().equals(self.getName())) {
            result = "Annotated Elements";
        }
        return result;
    }

    /**
     * Returns the text value for a given structural feature (column) and a Comment (row).
     *
     * @param self
     *         a UML element which is expected to be a Comment
     * @param columnFeature
     *         a structural feature of the given element
     * @return the textual value of the given structural feature of the given comment.
     */
    public String getCommentCellValue(EObject self, EStructuralFeature columnFeature) {
        String result = "";
        if (self instanceof Comment comment) {
            if (Objects.equals(columnFeature, UMLPackage.eINSTANCE.getComment_Body())) {
                result = comment.getBody();
            } else if (Objects.equals(columnFeature, UMLPackage.eINSTANCE.getComment_AnnotatedElement())) {
                result = this.getCommentAnnotatedElementLabels(comment, ", ");
            }
        }
        return result;
    }

    /**
     * Returns the list of semantic objects of the given self element through its feature given by name.
     *
     * @param self
     *         an UML element
     * @param featureName
     *         the structural feature that holds the semantic objects
     * @param globalFilter
     *         the global text that can filter semantic objects
     * @param columnFilters
     *         the column specific filters that can filter semantic objects
     * @param cursor
     *         last element rendered in the current page (last row if forward and first if backward)
     * @param direction
     *         the direction of the navigation
     * @param size
     *         the size of the table page (number of row chosen in the navigation control)
     * @return the list of semantic objects of the given self element through its feature given by name this list is wrapped inside a pagination mechanism.
     */
    public PaginatedData getSemanticObjectsFromFeatureName(EObject self, String featureName, String globalFilter, List<ColumnFilter> columnFilters, EObject cursor, String direction, int size) {
        var result = new ArrayList<Object>();

        Predicate<EObject> predicate = this.getValidCommentCandidatePredicate(self, globalFilter, columnFilters);
        return this.cursorBasedNavigationServices.collect(self, cursor, direction, size, predicate);
    }

    public List<Comment> getAllComments(EObject self, String globalFilter, List<ColumnFilter> columnFilters) {
        List<Comment> result = List.of();

        if (self instanceof Package pack) {
            result = pack.eContents().stream()
                    .filter(Comment.class::isInstance)
                    .map(Comment.class::cast)
                    .filter(this.getValidCommentCandidatePredicate(self, globalFilter, columnFilters))
                    .toList();
        }
        return result;
    }

    public List<Comment> sortComments(List<Object> objects, List<ColumnSort> columnSort) {
        var comments = new ArrayList<>(objects.stream().filter(Comment.class::isInstance).map(Comment.class::cast).toList());
        for (int i = columnSort.size() - 1; i >= 0; i--) {
            var sort = columnSort.get(i);
            if ("Body".equals(sort.id())) {
                if (sort.desc()) {
                    comments.sort(Comparator.comparing(Comment::getBody, String.CASE_INSENSITIVE_ORDER).reversed());
                } else {
                    comments.sort(Comparator.comparing(Comment::getBody, String.CASE_INSENSITIVE_ORDER));
                }
            }
        }
        return comments;
    }

    /**
     * Delete the given UML element.
     *
     * @param self
     *         an element to delete
     * @return the deleted element
     */
    public EObject deleteElement(EObject self) {
        EcoreUtil.delete(self);
        return self;
    }

    private Predicate<EObject> getValidCommentCandidatePredicate(EObject self, String globalFilter, List<ColumnFilter> columnFilters) {
        return eObject -> {
            boolean isValidCandidate = eObject instanceof Comment && eObject.eContainer() == self; // only comments owned by the given element
            if (isValidCandidate) {
                var comment = (Comment) eObject;
                if (globalFilter != null && !globalFilter.isBlank()) {
                    isValidCandidate = comment.getBody() != null && this.contains(comment.getBody(), globalFilter);
                    isValidCandidate = isValidCandidate || comment.getAnnotatedElements() != null && this.contains(this.getCommentAnnotatedElementLabels(comment, ""), globalFilter);
                }
                isValidCandidate = isValidCandidate && columnFilters.stream().allMatch(columnFilter -> {
                    boolean isCandidate = true;
                    String columnFilterValue = this.getColumnFilterValue(columnFilter);
                    if (columnFilter.id().equals(UMLCommentTableRepresentationDescriptionBuilder.COMMENT_COLUMN_BODY)) {
                        isCandidate = comment.getBody() != null && this.contains(comment.getBody(), columnFilterValue);
                    } else if (columnFilter.id().equals(UMLCommentTableRepresentationDescriptionBuilder.COMMENT_COLUMN_ANNOTATED_ELEMENTS)) {
                        String annotatedElementNames = this.getCommentAnnotatedElementLabels(comment, "");
                        isCandidate = !annotatedElementNames.isBlank() && this.contains(annotatedElementNames, columnFilterValue);
                    }
                    return isCandidate;
                });
            }
            return isValidCandidate;
        };
    }

    private String getColumnFilterValue(ColumnFilter columnFilter) {
        try {
            return this.objectMapper.readValue(columnFilter.value(), new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            this.logger.log(exception.getMessage(), ILogger.ILogLevel.WARNING);
        }
        return "";
    }

    /**
     * Returns all types found underneath the given UML Package.
     *
     * @param umlPackage
     *         an UML Package
     * @return the list of types owned by the given package
     */
    public List<Type> getOwnedTypes(Package umlPackage) {
        return umlPackage.getOwnedTypes();
    }

    /**
     * Return whether the given type contains the filters text in one of the properties of one stereotype applied to this type.
     *
     * @param type
     *         ab UML type
     * @param editingContext
     *         the current editing context used to retrieve semantic objects
     * @param globalFilter
     *         the global filter text
     * @param columnFilters
     *         the list of column filters
     * @return <code>true</code> if the given type contains the global filter text in one of the properties of one stereotype applied to this type and <code>false</code> otherwise.
     *         if no filters are given returns <code>true</code> as well.
     */
    public boolean containsFilterValue(Type type, IEditingContext editingContext, String globalFilter, List<ColumnFilter> columnFilters) {
        boolean result = true;
        if (globalFilter != null && !globalFilter.isBlank()) {
            result = this.getStereotypeApplicationLabels(type).stream().anyMatch(name -> this.contains(name, globalFilter));
            result = result || type.getStereotypeApplications().stream()
                    .anyMatch(stereotypeApplication ->
                            this.stereotypeApplicationServices.getAllFeatures(stereotypeApplication).stream()
                                    .anyMatch(eStructuralFeature -> this.contains(this.getStereotypeCellValue(type, eStructuralFeature).toString(), globalFilter)));
        }
        result = result && columnFilters.stream().allMatch(columnFilter -> {
            boolean isMatching = true;
            String columnFilterValue = this.getColumnFilterValue(columnFilter);
            if (Objects.equals("stereotype", columnFilter.id())) {
                isMatching = this.contains(String.join("", this.getStereotypeApplicationLabels(type)), columnFilterValue);
            } else {
                isMatching = type.getStereotypeApplications().stream()
                        .anyMatch(stereotypeApplication -> {
                            return this.objectService.getObject(editingContext, columnFilter.id()).stream()
                                    .filter(EStructuralFeature.class::isInstance)
                                    .map(EStructuralFeature.class::cast)
                                    .map(eStructuralFeature -> this.contains(this.getStereotypeCellValue(type, eStructuralFeature).toString(), columnFilterValue))
                                    .findFirst()
                                    .orElse(false);
                        });
            }
            return isMatching;
        });
        return result;
    }

    private boolean contains(String s, String text) {
        return s.toLowerCase().contains(text.toLowerCase());
    }

    /**
     * Return the list of label of stereotype applications applied on the given element.
     *
     * @param self
     *         an UML element
     * @return the list of label of stereotype applications applied on the given element separated by the given separator.
     */
    public List<String> getStereotypeApplicationLabels(Element self) {
        return self.getStereotypeApplications().stream()
                .map(EObject::eClass)
                .map(this.objectService::getLabel)
                .toList();
    }

    /**
     * Return a joined String of the given list of String and the given separator.
     *
     * @param labels
     *         a list of String to join
     * @param separator
     *         the separator placed in between two strings
     * @return a joined String of the given list of String and the given separator.
     */
    public String join(List<String> labels, String separator) {
        return String.join(separator, labels);
    }

    /**
     * Return the list of all stereotypes applied to the given type.
     *
     * @param type
     *         an UML type
     * @return the list of all stereotypes applied to the given type.
     */
    public List<Stereotype> getStereotypes(Type type) {
        return type.getApplicableStereotypes().stream()
                .toList();
    }

    /**
     * Return the list of all distinct stereotype classes applied to the given type.
     *
     * @param type
     *         an UML type
     * @return the list of all distinct stereotype classes applied to the given type.
     */
    public List<EClass> getStereotypeClasses(Type type) {
        return type.getApplicableStereotypes().stream()
                .map(Stereotype::getDefinition)
                .toList();
    }

    /**
     * Return the list of all valid structural features defined by the given EClass.
     *
     * @param eClass
     *         an EClass
     * @return the list of all valid structural features defined by the given EClass.
     */
    public List<EStructuralFeature> getAllFeatures(EClass eClass) {
        return eClass.getEAllStructuralFeatures().stream()
                .filter(this::isValidFeature)
                .toList();
    }

    private boolean isValidFeature(EStructuralFeature feature) {
        return !feature.getName().startsWith("base_") && !feature.isTransient() && !feature.isDerived()
                && !this.isContainmentReference(feature);
    }

    private boolean isContainmentReference(EStructuralFeature feature) {
        if (feature instanceof EReference reference) {
            return reference.isContainment();
        }
        return false;
    }

    /**
     * Returns the content of the cell between the given element (row) and the given feature (column).
     *
     * @param self
     *         a UML element
     * @param columnFeature
     *         a structural feature of a stereotype application of the given element
     * @return the textual value of the given structural feature or "N/A" if this feature is not available on the given element.
     */
    public Object getStereotypeCellValue(Element self, EStructuralFeature columnFeature) {
        String result = "N/A";
        EObject stereotypeApplication = this.getStereotypeApplicationAppliedOn(self, columnFeature);
        if (stereotypeApplication != null) {
            Object value = this.getStereotypeFeatureContent(stereotypeApplication, columnFeature);
            if (value instanceof List<?> list) {
                result = "[" + String.join(", ", list.stream()
                        .map(this::getLabel)
                        .toList()) + "]";
            } else {
                result = this.getLabel(value);
            }
        }
        return result;
    }

    private Object getStereotypeFeatureContent(EObject stereotypeApplication, EStructuralFeature columnFeature) {
        Object value = this.stereotypeApplicationServices.getStereotypeFeatureValue(stereotypeApplication, columnFeature);
        if (columnFeature.isMany() && value instanceof List<?> list) {
            if (list.stream().anyMatch(DynamicEObjectImpl.class::isInstance) && columnFeature instanceof EReference reference) {
                value = this.stereotypeApplicationServices.getStereotypeFeatureBaseElementValue(stereotypeApplication, reference);
            }
        } else {
            if (value instanceof DynamicEObjectImpl && columnFeature instanceof EReference reference) {
                value = this.stereotypeApplicationServices.getStereotypeFeatureBaseElementValue(stereotypeApplication, reference);
            }
        }
        return value;
    }

    private String getLabel(Object value) {
        String result = "";
        if (value instanceof EObject) {
            result = this.objectService.getLabel(value);
        } else if (value != null) {
            result = value.toString();
        }
        return result;
    }

    /**
     * Given an Element and a feature, return the stereotype application applied on the Element defining the feature.
     *
     * @param self
     *         an Element
     * @param columnFeature
     *         a feature
     * @return the stereotype application applied on the given Element defining the given feature or <code>null</code> if the feature is not belonging to any stereotype application of the element.
     */
    private EObject getStereotypeApplicationAppliedOn(Element self, EStructuralFeature columnFeature) {
        EClass stereotypeApplicationEClass = columnFeature.getEContainingClass();
        return self.getStereotypeApplications().stream()
                .filter(eObject -> Objects.equals(eObject.eClass(), stereotypeApplicationEClass) || eObject.eClass().getEAllSuperTypes().contains(stereotypeApplicationEClass))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the element to select when user selects a cell content in a Stereotype table.
     *
     * @param self
     *         the row element
     * @param columnFeature
     *         the structural feature associated to the cell (via its column)
     * @return if the cell contains referenced element return the first (or unique) element, otherwise self itself.
     */
    public EObject getStereotypeSelectedTargetObject(Element self, EStructuralFeature columnFeature) {
        EObject result = self;
        if (columnFeature instanceof EReference) {
            var cellObject = this.getStereotypeCellValue(self, columnFeature);
            if (cellObject instanceof List list && !list.isEmpty()) {
                result = (EObject) list.get(0);
            } else if (cellObject instanceof EObject eObject) {
                result = eObject;
            }
        }
        return result;
    }

    /**
     * Returns the first annotated element to be selected.
     *
     * @param self
     *         an UML comment element
     * @return the first element in the annotated element of the given comment or self argument if no annotated elements.
     */
    public EObject getFirstAnnotatedElement(Comment self) {
        return self.getAnnotatedElements().stream()
                .findFirst()
                .orElse(self);
    }

    public List<Object> getPackageTableTreeSemanticElements(Package self, IEditingContext editingContext, String globalFilter, List<ColumnFilter> columnFilters, List<String> expandedIds,
            List<String> activeRowFilterIds) {
        Predicate<Object> predicate = object -> {
            boolean isValidCandidate = true;
            if (object instanceof NamedElement element) {
                isValidCandidate = this.isValidElement(element, globalFilter, columnFilters);
            } else if (object instanceof String id) {
                // virtual rows are displayed only if the parent element is displayed
                isValidCandidate = this.getVirtualRowTargetObject(editingContext, id).stream()
                        .map(parent -> this.isValidElement(parent, globalFilter, columnFilters))
                        .findFirst()
                        .orElse(false);
            }
            return isValidCandidate;
        };
        return this.getSemanticElements(self, expandedIds, activeRowFilterIds).stream()
                .filter(predicate)
                .toList();
    }

    private boolean isValidElement(EObject object, String globalFilter, List<ColumnFilter> columnFilters) {
        if (object instanceof NamedElement element) {
            boolean isValidCandidate = true;
            if (globalFilter != null && !globalFilter.isBlank()) {
                isValidCandidate = element.getName() != null && element.getName().contains(globalFilter);
                isValidCandidate = isValidCandidate || element.getQualifiedName().contains(globalFilter);
            }
            return isValidCandidate && columnFilters.stream().allMatch(new UMLPackageTableTreeColumnFilterPredicate(this.objectMapper, element));
        }
        return false;
    }

    private List<Object> getSemanticElements(Package pack, List<String> expandedIds, List<String> activeRowFilterIds) {
        return new ArrayList<>(pack.getPackagedElements().stream()
                .flatMap(pe -> this.getSemanticElements(pe, expandedIds, activeRowFilterIds).stream())
                .toList());
    }

    private List<Object> getSemanticElements(PackageableElement element, List<String> expandedIds, List<String> activeRowFilterIds) {
        var classElements = new ArrayList<>();
        if (element instanceof Class clazz) {
            classElements.add(clazz);
            String classId = this.objectService.getId(clazz);
            if (expandedIds.contains(classId)) {
                if (!activeRowFilterIds.contains("hide-attributes")) {
                    String ownedAttributesId = this.getVirtualRowId(OWNED_ATTRIBUTES, clazz);
                    classElements.add(ownedAttributesId);
                    if (expandedIds.contains(ownedAttributesId)) {
                        classElements.addAll(clazz.getOwnedAttributes());
                    }
                }
                String ownedOperationsId = this.getVirtualRowId(OWNED_OPERATIONS, clazz);
                classElements.add(ownedOperationsId);
                if (expandedIds.contains(ownedOperationsId)) {
                    classElements.addAll(clazz.getOperations().stream().flatMap(operation -> this.getSemanticElements(operation, expandedIds, activeRowFilterIds).stream()).toList());
                }
                String ownedNestedClassesId = this.getVirtualRowId(OWNED_NESTED_CLASSES, clazz);
                classElements.add(ownedNestedClassesId);
                if (expandedIds.contains(ownedNestedClassesId)) {
                    classElements.addAll(clazz.getNestedClassifiers().stream().flatMap(nestedClass -> this.getSemanticElements(nestedClass, expandedIds, activeRowFilterIds).stream()).toList());
                }
            }
        }
        return classElements;
    }

    private List<Object> getSemanticElements(Operation operation, List<String> expandedIds, List<String> activeRowFilterIds) {
        var operationElements = new ArrayList<>();
        operationElements.add(operation);
        String operationId = this.objectService.getId(operation);
        if (expandedIds.contains(operationId)) {
            String ownedParametersId = this.getVirtualRowId(OWNED_PARAMETERS, operation);
            operationElements.add(ownedParametersId);
            if (expandedIds.contains(ownedParametersId)) {
                operationElements.addAll(operation.getOwnedParameters());
            }
        }
        return operationElements;
    }

    private String getVirtualRowId(String radical, EObject parentElement) {
        return radical + ID_SEPARATOR + this.objectService.getId(parentElement);
    }

    private Optional<EObject> getVirtualRowTargetObject(IEditingContext editingContext, String objectId) {
        String id = objectId.substring(TableService.VIRTUAL_ROW_PREFIX.length());
        return this.objectService.getObject(editingContext, id.substring(id.indexOf(ID_SEPARATOR) + ID_SEPARATOR.length())).stream()
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast)
                .findFirst();
    }

    public String getPackageTableTreeRowLabel(Object self) {
        String result = "N/A";
        if (self instanceof NamedElement namedElement) {
            result = this.getElementLabel(namedElement);
        } else if (self instanceof String id) {
            result = this.getVirtualRowLabel(id);
        }
        return result;
    }

    private String getVirtualRowLabel(String id) {
        String result = "";
        if (id.startsWith(OWNED_ATTRIBUTES)) {
            result = "Owned Attributes";
        } else if (id.startsWith(OWNED_OPERATIONS)) {
            result = "Owned Operations";
        } else if (id.startsWith(OWNED_PARAMETERS)) {
            result = "Owned Parameters";
        } else if (id.startsWith(OWNED_NESTED_CLASSES)) {
            result = "Owned Classes";
        }
        return result;
    }

    public String getPackageTableTreeCellValue(Object self, String columnId, IEditingContext editingContext) {
        String result = "";
        if (self instanceof NamedElement element && "name".equals(columnId)) {
            result = element.getName();
        } else if (self instanceof NamedElement element && "qualified-name".equals(columnId)) {
            result = element.getQualifiedName();
        }
        return result;
    }

    public Object getPackageTableTreeTargetObject(Object self, IEditingContext editingContext) {
        Object result = null;
        if (self instanceof NamedElement element) {
            result = element;
        } else if (self instanceof String id) {
            result = this.getVirtualRowTargetObject(editingContext, id).orElse(null);
        }
        return result;
    }

    public Integer getPackageTableTreeRowDepthLevel(Object self, IEditingContext editingContext) {
        Integer result = null;
        if (self instanceof NamedElement element) {
            result = this.getEObjectDepthLevel(element);
        } else if (self instanceof String id) {
            result = this.getVirtualRowTargetObject(editingContext, id)
                    .map(this::getEObjectDepthLevel)
                    .orElse(-1) + 1;
        }
        return result;
    }

    private Integer getEObjectDepthLevel(EObject eObject) {
        if (eObject instanceof Package) {
            return -2;
        } else {
            return 2 + this.getEObjectDepthLevel(eObject.eContainer());
        }
    }
}

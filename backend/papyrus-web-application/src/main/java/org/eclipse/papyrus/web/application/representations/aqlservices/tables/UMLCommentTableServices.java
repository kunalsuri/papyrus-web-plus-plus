/*******************************************************************************
 * Copyright (c) 2025 CEA LIST.
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.uml.domain.services.properties.ILogger;
import org.eclipse.papyrus.web.application.tables.comment.UMLCommentTableRepresentationDescriptionBuilder;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.tables.ColumnFilter;
import org.eclipse.sirius.components.tables.ColumnSort;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;
import org.springframework.stereotype.Service;

/**
 * AQL services used in the UML Comment table.
 *
 * @author Jerome Gout
 */
@Service
public class UMLCommentTableServices {

    private final ILogger logger;

    private final IObjectService objectService;

    private final ObjectMapper objectMapper;

    public UMLCommentTableServices(ILogger logger, IObjectService objectService, ObjectMapper objectMapper) {
        this.logger = Objects.requireNonNull(logger);
        this.objectService = Objects.requireNonNull(objectService);
        this.objectMapper = Objects.requireNonNull(objectMapper);
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

    private boolean contains(String s, String text) {
        return s.toLowerCase().contains(text.toLowerCase());
    }
}

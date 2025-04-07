/*******************************************************************************
 * Copyright (c) 2024, 2025 CEA LIST.
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

package org.eclipse.papyrus.web.application.tables.comment;

import java.util.List;
import java.util.UUID;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.IDAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.table.TableBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.table.CellDescription;
import org.eclipse.sirius.components.view.table.ColumnDescription;
import org.eclipse.sirius.components.view.table.RowDescription;
import org.eclipse.sirius.components.view.table.TableDescription;
import org.eclipse.sirius.emfjson.resource.JsonResource;

/**
 * Builder of the {@link org.eclipse.sirius.components.view.table.TableDescription} to be used in an UML element.
 *
 * @author Jerome Gout
 */
public class UMLCommentTableRepresentationDescriptionBuilder {

    public static final String AQL_TRUE = "aql:true";

    public static final String COMMENT_COLUMN_BODY = "Body";

    public static final String COMMENT_COLUMN_ANNOTATED_ELEMENTS = "Annotated Elements";

    /**
     * Name of the table.
     */
    public static final String UML_COMMENT_TABLE = "UML Comment Table";

    public View createView() {
        var umlCommentTableRepresentationDescription = this.build();

        var umlCommentTableView = new ViewBuilders()
                .newView()
                .descriptions(umlCommentTableRepresentationDescription)
                .build();

        umlCommentTableView.eAllContents().forEachRemaining(eObject -> {
            eObject.eAdapters().add(new IDAdapter(UUID.nameUUIDFromBytes(EcoreUtil.getURI(eObject).toString().getBytes())));
        });

        UUID resourceId = UUID.nameUUIDFromBytes(UML_COMMENT_TABLE.getBytes());
        String resourcePath = resourceId.toString();
        JsonResource resource = new JSONResourceFactory().createResourceFromPath(resourcePath);
        resource.eAdapters().add(new ResourceMetadataAdapter(UML_COMMENT_TABLE));
        resource.getContents().add(umlCommentTableView);

        return umlCommentTableView;
    }

    private TableDescription build() {

        var rowDescription = this.buildRowDescription();

        var columnDescriptions = this.buildColumnDescriptions();

        var cellDescriptions = this.buildCellDescriptions();

        return new TableBuilders().newTableDescription()
                .name(UML_COMMENT_TABLE)
                .titleExpression(UML_COMMENT_TABLE)
                .domainType("uml::Element")
                .useStripedRowsExpression(AQL_TRUE)
                .rowDescription(rowDescription)
                .columnDescriptions(columnDescriptions.toArray(new ColumnDescription[0]))
                .cellDescriptions(cellDescriptions.toArray(new CellDescription[0]))
                .build();

    }

    private RowDescription buildRowDescription() {

        var deleteAction = new TableBuilders().newRowContextMenuEntry()
                .name("comment-delete")
                .labelExpression("Delete this comment")
                .preconditionExpression("aql:true")
                .iconURLExpression("aql:Sequence{'/images/row-delete.svg'}")
                .body(new ViewBuilders().newChangeContext()
                        .expression("aql:self.deleteElement()").build())
                .build();

        return new TableBuilders().newRowDescription()
                .name("Comment")
                .semanticCandidatesExpression("aql:self.getAllComments(globalFilterData, columnFilters)->sortComments(columnSort)->toPaginatedData(cursor,direction,size)")
                .initialHeightExpression("aql:53")
                .isResizableExpression(AQL_TRUE)
                .headerIconExpression("aql:self.getElementIconPath()")
                .headerIndexLabelExpression("aql:rowIndex + 1")
                .contextMenuEntries(deleteAction)
                .depthLevelExpression("aql:0")
                .build();
    }

    private List<ColumnDescription> buildColumnDescriptions() {
        var comment = new TableBuilders().newColumnDescription()
                .name("comment features")
                .isResizableExpression(AQL_TRUE)
                .initialWidthExpression("aql:200")
                .headerLabelExpression("aql:self")
                .headerIndexLabelExpression("aql:columnIndex.alphabetic()")
                .semanticCandidatesExpression("aql:Sequence{'" + COMMENT_COLUMN_BODY + "', '" + COMMENT_COLUMN_ANNOTATED_ELEMENTS + "'}")
                .filterWidgetExpression("text")
                .isSortableExpression("aql:self.equals('" + COMMENT_COLUMN_BODY + '\'' + ')')
                .build();
        return List.of(comment);
    }

    private List<CellDescription> buildCellDescriptions() {
        var setBodyOperation = new ViewBuilders().newSetValue()
                .featureName("body")
                .valueExpression("aql:newValue");
        
        var bodyCellDescription = new TableBuilders().newCellDescription()
                .name("bodyCell")
                .preconditionExpression("aql:columnTargetObject.equals('" + COMMENT_COLUMN_BODY + "')")
                .valueExpression("aql:self.body")
                .cellWidgetDescription(new TableBuilders().newCellTextareaWidgetDescription()
                        .body(setBodyOperation.build())
                        .build())
                .build();

        var annotatedElementsCellDescription = new TableBuilders().newCellDescription()
                .name("annotatedElementsCell")
                .preconditionExpression("aql:columnTargetObject.equals('" + COMMENT_COLUMN_ANNOTATED_ELEMENTS + "')")
                .valueExpression("aql:self.getCommentAnnotatedElementLabels(', ')")
                .selectedTargetObjectExpression("aql:self.getFirstAnnotatedElement()")
                .cellWidgetDescription(new TableBuilders().newCellLabelWidgetDescription().build())
                .build();
        return List.of(bodyCellDescription, annotatedElementsCellDescription);
    }
}

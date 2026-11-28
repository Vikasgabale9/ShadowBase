
package com.shadowbase.migration.service;

import com.shadowbase.migration.dto.SchemaComparisonResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SchemaComparisonMapper {

    /**
     * Converts the internal comparison result
     * into a frontend-friendly API response.
     */
    public SchemaComparisonResponse toResponse(
            SchemaComparisonService.SchemaComparisonResult result) {

        List<SchemaComparisonResponse.ColumnDifferenceResponse>
                columnDifferences = result.columnDifferences()
                .stream()
                .map(this::toColumnDifferenceResponse)
                .toList();

        String summary = buildSummary(result);

        return new SchemaComparisonResponse(
                result.migrationId(),
                result.identical(),
                result.missingTables(),
                result.extraTables(),
                columnDifferences,
                summary
        );
    }

    /**
     * Maps one column difference.
     */
    private SchemaComparisonResponse.ColumnDifferenceResponse
    toColumnDifferenceResponse(
            SchemaComparisonService.ColumnDifference difference) {

        return new SchemaComparisonResponse.ColumnDifferenceResponse(
                difference.tableName(),
                difference.columnName(),
                difference.property(),
                difference.sourceValue(),
                difference.shadowValue()
        );
    }

    /**
     * Creates a readable comparison summary.
     */
    private String buildSummary(
            SchemaComparisonService.SchemaComparisonResult result) {

        if (result.identical()) {
            return "No differences detected in the compared schema properties.";
        }

        int missingTableCount = result.missingTables().size();
        int extraTableCount = result.extraTables().size();
        int columnDifferenceCount = result.columnDifferences().size();

        return "Schema differences detected: "
                + missingTableCount + " missing tables, "
                + extraTableCount + " extra tables, and "
                + columnDifferenceCount + " column or primary-key differences.";
    }
}

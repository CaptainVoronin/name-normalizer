package com.nextbi.name_normalizer;

import com.nextbi.nor.NameNormalizer;
import com.nextbi.nor.SourceTerm;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import datareader.IFieldDefinition;
import datareader.SQLProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NormilizationProcessor {
    SQLProvider dataProvider;
    String schemaName;
    String tableName;
    IFieldDefinition keyField;
    IFieldDefinition fieldToNormalize;
    IFieldDefinition fieldToUpdate;
    NameNormalizer nameNormalizer;
    String logFile;
    CsvWriter logWriter;

    public NormilizationProcessor(SQLProvider dataProvider, NameNormalizer nameNormalizer, CsvWriter logWriter, String schemaName, String tableName, IFieldDefinition keyField, IFieldDefinition fieldToNormalize, IFieldDefinition fieldToUpdate, String logFile) {
        this.dataProvider = dataProvider;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.keyField = keyField;
        this.fieldToNormalize = fieldToNormalize;
        this.nameNormalizer = nameNormalizer;
        this.fieldToUpdate = fieldToUpdate;
        this.logFile = logFile;
        this.logWriter = logWriter;
    }

    // Формирует текст запроса, который выбирает
    public String getGroupQuery(String keyValue) {
        StringBuilder stb = new StringBuilder();
        //stb.append("select distinct \"").append(fieldToNormalize.getName()).append("\"\n");
        stb.append("select \"").append(fieldToNormalize.getName()).append("\", count(*) as cnt\n");
        stb.append("from ").append(schemaName).append('.').append("\"").append(tableName).append("\"\n");
        stb.append("where \"").append(keyField.getName()).append("\"=").append(keyField.prepareQueryText(keyValue)).append("\n");
        stb.append( "group by \"" ).append( fieldToNormalize.getName() ).append( "\"");
        return stb.toString();
    }

    public long applyResult(String keyValue, String normalizedName) throws SQLException {
        StringBuilder stb = new StringBuilder();
        stb.append("update ").append(schemaName).append('.').append("\"").append(tableName).append("\"\n");
        stb.append("set \"").append(fieldToUpdate.getName()).append("\"=\'").append(normalizedName).append("'\n");
        stb.append("where \"").append(keyField.getName()).append("\"=").append(keyField.prepareQueryText(keyValue));
        Statement stmt = dataProvider.getConnection().createStatement();
        long res = stmt.executeUpdate(stb.toString());
        return res;
    }

    boolean go() throws SQLException, FileNotFoundException {
        boolean hasSomethingToProcess = false;

        String queryText = makeGetRowsQueryText();
        Statement stmt = dataProvider.getConnection().createStatement();
        ResultSet result = stmt.executeQuery(queryText);

        if (result.next()) {
            hasSomethingToProcess = true;
            String keyValue = result.getString(keyField.getName());
            normalizeGroup(keyValue);
        }
        result.close();
        return hasSomethingToProcess;
    }

    private String makeGetRowsQueryText() {
        // TODO: Убрать is null
        return "select \"" + keyField.getName() + "\", count(*) from \n" +
                "( \n" +
                "select distinct \"" + keyField.getName() + "\", \"" + fieldToNormalize.getName() + "\"\n" +
                "from " + schemaName + ".\"" + tableName + "\"\n" +
                "where name_plat_norm is null\n" + // это для отладки
                "limit 1000\n" +
                ") as dataset\n" +
                "group by \"" + keyField.getName() + "\"\n" +
                "having count( * ) > 1";
    }

    private void normalizeGroup(String keyValue) throws SQLException {

        List<SourceTerm> terms = getRowForKey(getGroupQuery(keyValue));
        String normalizedName = nameNormalizer.normalize(terms);
        long updated = applyResult(keyValue, normalizedName);
        String[] row = new String[3];
        row[0] = new Long(updated).toString();
        row[1] = keyValue;
        row[2] = normalizedName;
        logWriter.writeRow(row);
    }

    private List<SourceTerm> getRowForKey(String query) throws SQLException {
        Statement stmt = dataProvider.getConnection().createStatement();
        List<SourceTerm> rows = new ArrayList<>();

        try (ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next())
                rows.add(new SourceTerm( rs.getString(fieldToNormalize.getName() ), rs.getInt( "cnt" )) );
        }
        return rows;
    }
}

package com.nextbi.name_normalizer;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import com.nextbi.nor.NameNormalizer;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import datareader.SQLProvider;
import datareader.VarcharField;
import org.apache.commons.cli.*;

public class Main {

    public static void main(String[] args) {

/*        Options ops = new Options();

        ops.addRequiredOption("i", "input", true, "");
        ops.addOption("d", "distance", true, "");
        ops.addOption("r", "rate", true, "");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(ops, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", ops);
            System.exit(1);
            return;
        }

        List<String> rows = null;*/
        try {
/*
            if (cmd.hasOption('d'))
                distnceLimit = Float.parseFloat(cmd.getOptionValue('d'));
            if (cmd.hasOption('r'))
                rateLimit = Float.parseFloat(cmd.getOptionValue('r'));


            rows = read(cmd.getOptionValue("input"));
            NameNormalizer nn = new NameNormalizer();
            String name = nn.normalize( rows );
            System.out.println(name); */

            SQLProvider provider = new SQLProvider( "demo1.stand.nextbi.ru", "dev_import_data", "postgres", "123456" );

            NameNormalizer nn = new NameNormalizer();
            VarcharField keyField = new VarcharField( "INN_PLAT" );
            VarcharField normalizeField = new VarcharField( "NAME_PLAT" );
            VarcharField updateField = new VarcharField( "name_plat_norm" );
            CsvWriterSettings set = new CsvWriterSettings();
            CsvWriter logWriter = new CsvWriter(new FileOutputStream(new File( "d:\\temp\\names\\update.log" )), set);

            NormilizationProcessor np = new NormilizationProcessor( provider, nn, logWriter, "old_payments", "transaction", keyField, normalizeField, updateField,"d:\\temp\\names\\update.log" );
            provider.open();
            while ( np.go() );
            provider.close();
            logWriter.close();
            System.out.println( "Finished");

        }
        catch (SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static List<String> read(String name) throws IOException {
        CsvParser parser;
        CsvParserSettings settings;
        settings = new CsvParserSettings();
        settings.getFormat().setDelimiter(',');
        settings.getFormat().setQuoteEscape('\\');
        settings.getFormat().setLineSeparator("\n");
        settings.setMaxCharsPerColumn(20000);

        List<String> list = new ArrayList<>();

        try (FileInputStream ins = new FileInputStream(new File(name))) {
            parser = new CsvParser(settings);
            parser.beginParsing(ins);
            String[] row = null;

            while ((row = parser.parseNext()) != null)
                list.add(row[0]);
        }

        return list;
    }
}

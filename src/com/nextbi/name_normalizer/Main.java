package com.nextbi.name_normalizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.cli.*;
import org.simmetrics.StringDistance;
import org.simmetrics.metrics.Levenshtein;

public class Main {

    private static float distnceLimit = 2.5f;
    private static float rateLimit = .25f;

    static StringDistance metric;

    static {
        metric = new Levenshtein();
    }

    public static void main(String[] args) {

        Options ops = new Options();

        ops.addRequiredOption( "i", "input", true, "" );
        ops.addOption( "d", "distance", true, ""  );
        ops.addOption( "r", "rate", true, ""  );

        //String file = "d:\\temp\\names\\names-3.csv";
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(ops, args);
        } catch( ParseException e ) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", ops);
            System.exit(1);
            return;
        }

        List<String> rows = null;
        try {
            if( cmd.hasOption( 'd' ))
                distnceLimit = Float.parseFloat( cmd.getOptionValue( 'd' ) );
            if( cmd.hasOption( 'r' ) )
                rateLimit = Float.parseFloat( cmd.getOptionValue( 'r' ) );

            rows = read( cmd.getOptionValue( "input" ));
            clear(rows);
            Map<String, Usage> result = analyze(rows);
            output( result );
            List<Usage> order = OrderMaker.makeOrder( result.values() );
            String name = buildName( order );

            System.out.println( name );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String buildName(List<Usage> result)
    {
        List<Usage> list = new ArrayList<>();

        for( Usage usage : result )
        {
            if( usage.getRate() < rateLimit )
                continue;
            list.add( usage );
        }

        String name = "";
        for( Usage usage : list )
            name += usage.getOriginal() + " ";

        return name;
    }

    private static Map<String, Usage> analyze(List<String> rows) {
        Map<String, Usage> terms = new HashMap<>();
        for (String row : rows) {
            calcRate(rows.size(), terms, row);
        }
        return terms;
    }

    private static void output(Map<String, Usage> terms) {
        for (String term : terms.keySet()) {
            Usage usage = terms.get(term);
            System.out.println(String.format("%f %s", usage.getRate(), usage.getOriginal()));
        }
    }

    private static void calcRate(int size, Map<String, Usage> terms, String row) {

        String[] tokens = row.split(" ");
        int pos = 0;
        for (String token : tokens) {
            if (token.trim().length() == 0)
                continue;

            Position p = new Position( tokens.length, pos );

            Usage usage = null;
            if ((usage = terms.get(token)) != null) {
                usage.incUsage();
            } else {
                if ((usage = findSimilar(terms, token)) == null) {
                    usage = new Usage(size, token );
                    terms.put(token, usage);
                }
                usage.incUsage();
            }
            usage.addPos( p );

            pos++;
        }
    }

    private static Usage findSimilar(Map<String, Usage> terms, String token) {
        Usage usage = null;
        Set<String> keys = terms.keySet();
        for (String key : keys) {
            if (distance(key, token) < distnceLimit)
                usage = terms.get(key);
        }
        return usage;
    }

    private static float distance(String key, String token) {
        float dist = metric.distance(key.toLowerCase(), token.toLowerCase());
        if( dist < distnceLimit )
        {
            if( dist > ((float) key.length() ) / 2 )
                dist = 100;
        }
        return dist;
    }

    private static void clear(List<String> rows) {
        for (int i = 0; i < rows.size(); i++) {
            String row = rows.get(i);
            row = row.replaceAll("[^a-zA-Zа-яА-Я0-9]", " ").trim();
            rows.set(i, row);
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

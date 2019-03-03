package com.nextbi.nor;

import org.simmetrics.StringDistance;
import org.simmetrics.metrics.Levenshtein;

import java.util.*;

public class NameNormalizer {

    private float distnceLimit = 2.5f;
    private float weightThreshold = .3f;

    public float getDistnceLimit() {
        return distnceLimit;
    }

    public void setDistnceLimit(float distnceLimit) {
        this.distnceLimit = distnceLimit;
    }

    public float getRateLimit() {
        return weightThreshold;
    }

    public void setRateLimit(float rateLimit) {
        this.weightThreshold = rateLimit;
    }

    StringDistance metric;

    public NameNormalizer() {
        metric = new Levenshtein();
    }

    public String normalize(List<SourceTerm> items) {
        clear(items);

        Map<String, Usage> result = analyze(items);

//        output(result);
        List<Usage> selected = new ArrayList<>();

        for (Usage usage : result.values()) {
            if (usage.getRate() < weightThreshold)
                continue;
            selected.add(usage);
        }

        List<Usage> order = OrderMaker.makeOrder(selected);
        return buildName(order);
    }

    private String buildName(List<Usage> items) {
        StringBuilder stb = new StringBuilder();

        for (Usage usage : items)
            stb.append(usage.getOriginal()).append(" ");
        stb.deleteCharAt(stb.length() - 1);
        return stb.toString();
    }

    private Map<String, Usage> analyze(List<SourceTerm> terms) {
        Map<String, Usage> estimatedWords = new HashMap<>();

        // Посчитать общее количество элементов
        int quantity = 0;
        for( SourceTerm term : terms )
            quantity += term.getCount();

        for (SourceTerm term : terms)
            calcRate( quantity, estimatedWords, term);

        return estimatedWords;
    }

    private void output(Map<String, Usage> terms) {
        for (String term : terms.keySet()) {
            Usage usage = terms.get(term);
            System.out.println(String.format("%f %s", usage.getRate(), usage.getOriginal()));
        }
    }

    private void calcRate(int total, Map<String, Usage> estimatedWords, SourceTerm term) {

        // Пилим текст элемента на слова
        String[] tokens = term.getText().split(" ");
        int pos = 0;

        // вес слов в этом элементе
        float weight =  ((float) term.getCount()) / total ;

        // Проходим по всему массиву элементов
        for (String token : tokens) {
            // Пустые элементы пропускаем
            if (token.trim().length() == 0)
                continue;

            Position p = new Position(tokens.length, pos);
            Usage usage = null;

            if ((usage = estimatedWords.get(token)) != null) {
                usage.incWeight( weight );
            } else {
                if ((usage = findSimilar(estimatedWords, token)) == null) {
                    usage = new Usage( total, token);
                    estimatedWords.put(token, usage);
                }
                usage.incWeight( weight );
            }
            usage.addPos(p);

            pos++;
        }
    }

    private Usage findSimilar(Map<String, Usage> estimatedWords, String token) {
        Usage usage = null;
        Set<String> keys = estimatedWords.keySet();
        for (String key : keys) {
            if (distance(key, token) < distnceLimit)
                usage = estimatedWords.get(key);
        }
        return usage;
    }

    private float distance(String key, String token) {
        float dist = metric.distance(key.toLowerCase(), token.toLowerCase());
        if (dist < distnceLimit) {
            if (dist > ((float) key.length()) / 2)
                dist = 100;
        }
        return dist;
    }

    private void clear(List<SourceTerm> items) {
        for (int i = 0; i < items.size(); i++) {
            SourceTerm item = items.get(i);
            String res = item.getText().replaceAll("[^a-zA-Zа-яА-Я0-9]", " ").replaceAll("\\s\\s", " ").trim();
            item.setText(res);
        }
    }
}

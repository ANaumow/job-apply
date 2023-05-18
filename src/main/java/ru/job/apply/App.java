package ru.job.apply;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Test {

    public static void main(String[] args) throws IOException {
        new Test().run();
    }

    StringBuilder sb = new StringBuilder();

    private void run() throws IOException {
        long startTime = System.nanoTime();

        // Колонка, Id числа, Номера строк
        Map<Integer, Map<Integer, List<Integer>>> index = new HashMap<>();
        final String EMPTY = "\"\"";
//        final String filename = "test.txt";
        final String filename = "lng.csv";
//        final String filename = "tete";

        List<String> rawLines;
        try (Stream<String> fileLines = Files.lines(new File(filename).toPath())) {
            Predicate<String> linePredicate = Pattern.compile("^\"[^\"]*\"(;\"[^\"]*\")*$")
                    .asPredicate();

            rawLines = fileLines.distinct()
                    .filter(linePredicate)
                    .toList();
        }

        List<List<String>> lines = rawLines.stream()
                .map(line -> List.of(line.split(";")))
                .toList();

        List<String> uniqueWords = lines.stream()
                .flatMap(Collection::stream)
                .distinct()
                .toList();

        Map<String, Integer> wordIds = new HashMap<>();
        for (int wordId = 0; wordId < uniqueWords.size(); wordId++) {
            String word = uniqueWords.get(wordId);
            wordIds.put(word, wordId);
        }

        for (int row = 0; row < lines.size(); row++) {
            List<String> line = lines.get(row);

            for (int col = 0; col < line.size(); col++) {
                String word = line.get(col);

                if (word.equals(EMPTY))
                    continue;

                Integer wId = wordIds.get(word);

                index.computeIfAbsent(col, c -> new HashMap<>())
                        .computeIfAbsent(wId, i -> new ArrayList<>())
                        .add(row);
            }
        }

        int n = lines.size();
        int[] groups = new int[n];
        // пока что одна линия - одна группа
        for (int i = 0; i < groups.length; i++) {
            groups[i] = i;
        }
        int[] rank = new int[n];

        index.forEach((col, rowsByWord) -> {
            rowsByWord.forEach((word, rows) -> {
                int first = rows.get(0);
                int head1 = find(groups, first);
                for (int i = 1; i < rows.size(); i++) {
                    int second = rows.get(i);
                    int head2 = find(groups, second);
                    if (head1 != head2) {
                        union(rank, groups, head1, head2);
                    }
                }
            });
        });

        Map<Integer, List<Integer>> groupedLines = IntStream.range(0, lines.size())
                .boxed()
                .collect(Collectors.groupingBy(i -> groups[i]));

        List<List<Integer>> sortedGroups = groupedLines.values().stream()
                .filter(list -> list.size() > 1)
                .sorted(Comparator.<List<Integer>, Integer>comparing(List::size).reversed())
                .toList();

        println(sortedGroups.size());
        for (int i = 0; i < sortedGroups.size(); i++) {
            List<Integer> group = sortedGroups.get(i);
            println("Группа " + (i + 1));
            for (Integer lineId : group) {
                println(rawLines.get(lineId));
            }
        }

        flush();

        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
//        System.out.println(totalTime / 1e9);
    }


    int find(int[] p, int v) {
        if (p[v] == v) return v;
        int pv = find(p, p[v]);
        p[v] = pv;
        return pv;
    }

    void union(int[] r, int[] p, int v, int u) {
        v = find(p, v);
        u = find(p, u);
        if (r[v] == r[u]) {
            r[v]++;
        }
        if (r[v] > r[u]) p[u] = v;
        else p[v] = u;
    }

    void println(Object object) {
        sb.append(object.toString()).append("\n").append("\n");
    }

    void flush() {
        System.out.println(sb);
    }



}

import javafx.util.Pair;

import java.io.Serializable;
import java.util.*;

public class Model implements Serializable {
    public HashMap<ArrayList<String>, Double> analyzed;
    private HashSet<String> knowledgeBase;
    private final int n;

    public Model(int n) {
        this.n = n;
        knowledgeBase = new HashSet<>();
    }

    public HashSet<String> getKnowledgeBase() {
        return knowledgeBase;
    }


    public String[] normalizeToModel(ArrayList<String> input) {
        ArrayList<String> words = new ArrayList<>(input);
        for (int i = 0; i < n - 1; i++) {
            words.add(0, "<s>");
            words.add("</s>");
        }
        String[] output = new String[words.size()];
        words.toArray(output);
        return output;
    }

    public ArrayList<ArrayList<String>> partition(String[] sentence) {
        ArrayList<ArrayList<String>> output = new ArrayList<>();
        ArrayList<String> part;
        int sentenceLength;
        sentenceLength = sentence.length - n;
        for (int i = 0; i <= sentenceLength; i++) {
            part = new ArrayList<>(n);
            part.addAll(Arrays.asList(sentence).subList(i, n + i));
            output.add(part);
        }
        return output;
    }

    public HashMap<ArrayList<String>, Double> analyze(ArrayList<ArrayList<String>> parts) {
        HashMap<ArrayList<String>, Pair<Integer, Integer>> count = new HashMap<>();
        HashMap<ArrayList<String>, Double> output = new HashMap<>();
        boolean flag;
        int save;
        int i = 0;
        double div = (double) parts.size() / 50;
        double value = 0;
        for (ArrayList<String> part1 :
                parts) {
            value++;
            if (value > i * div) {
                System.out.print("|");
                i++;
            }
            flag = true;
            save = 1;
            for (ArrayList<String> part2 :
                    count.keySet()) {
                if (arrayEquals(part1, part2, 0, n - 1)) {
                    if (part1.get(n - 1).equals(part2.get(n - 1))) {
                        count.replace(part2, new Pair<>(count.get(part2).getKey() + 1, count.get(part2).getValue() + 1));
                        flag = false;
                    } else {
                        count.replace(part2, new Pair<>(count.get(part2).getKey(), count.get(part2).getValue() + 1));
                        save = count.get(part2).getValue();
                    }
                }
            }
            if (flag) {
                count.put(part1, new Pair<>(1, save));
            }
        }
        for (ArrayList<String> part :
                count.keySet()) {
            output.put(part, (double) count.get(part).getKey() / (double) count.get(part).getValue());
        }
        return output;
    }

    public boolean arrayEquals(ArrayList<String> s1, ArrayList<String> s2, int start, int end) {
        for (int i = start; i < end; i++) {
            if (!s1.get(i).equals(s2.get(i)))
                return false;
        }
        return true;
    }

    public double deepSearch(ArrayList<String> part) {
        Double output = analyzed.get(part);
        if (output != null)
            return output;
        return 0;
    }


    public void train(String input) {
        String[] sentences = input.split("\n");
        int size = sentences.length;
        String[][] words = new String[size][];
        ArrayList<ArrayList<String>> partitioned = new ArrayList<>();
        ArrayList<String> normalized;
        for (int i = 0; i < size; i++) {
            normalized = new ArrayList<>(Arrays.asList(sentences[i].split(" ")));
            knowledgeBase.addAll(normalized);
            words[i] = normalizeToModel(normalized);
            partitioned.addAll(partition(words[i]));
        }
        analyzed = analyze(partitioned);
    }

}

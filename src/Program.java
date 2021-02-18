import java.io.*;
import java.util.*;

public class Program {
    private final int learnMax;
    private final int testSize;
    private final String learnPath1;
    private final String learnPath2;
    private final String learnPath3;
    private final String testPath;
    private final String savePath;
    private String result;
    private Model unigram_ferdosi;
    private Model bigram_ferdosi;
    private Model unigram_molavi;
    private Model bigram_molavi;
    private Model unigram_hafez;
    private Model bigram_hafez;

    public Program(String savePath, String learnPath1, String learnPath2, String learnPath3, String testPath, int learnMax, int testSize) {
        this.learnPath1 = learnPath1;
        this.learnPath2 = learnPath2;
        this.learnPath3 = learnPath3;
        this.testPath = testPath;
        this.savePath = savePath;
        this.learnMax = learnMax;
        this.testSize = testSize;
        System.out.println("Loading file");
        if (!load(savePath)) {
            System.out.println("no file to load");
            unigram_ferdosi = new Model(1);
            bigram_ferdosi = new Model(2);
            unigram_molavi = new Model(1);
            bigram_molavi = new Model(2);
            unigram_hafez = new Model(1);
            bigram_hafez = new Model(2);
            String trainString1 = "";
            String trainString2 = "";
            String trainString3 = "";

            try {
                trainString1 = readFile(this.learnPath1, 0, learnMax);
                trainString2 = readFile(this.learnPath2, 0, learnMax);
                trainString3 = readFile(this.learnPath3, 0, learnMax);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("\ntraining on unigram");
            unigram_ferdosi.train(trainString1);
            unigram_molavi.train(trainString2);
            unigram_hafez.train(trainString3);
            System.out.println("\ntraining on bigram");
            bigram_ferdosi.train(trainString1);
            bigram_molavi.train(trainString2);
            bigram_hafez.train(trainString3);
            System.out.println("\ntrain finished");
            System.out.println("saving");
            if (store(savePath))
                System.out.println("trained files saved");
        } else
            System.out.println("file loaded");
        System.out.println("testing the test cases. please wait  ");
        result = test();
    }

    public String test() {
        String output = "";
        String testSet = "";
        int counter = 0;
        try {
            testSet = readFile(testPath, 0, testSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] temp = testSet.split("\n");
        int testLength = temp.length;
        String[][] tests = new String[testLength][];
        for (int i = 0; i < testLength; i++) {
            tests[i] = temp[i].split("\t");
        }
        ArrayList<String> sentence;
        double p1, p2, p3;
        for (int i = 0; i < testLength; i++) {
            sentence = new ArrayList<>(Arrays.asList(tests[i][1].split(" ")));
            p1 = probabilityOfSentence(sentence, unigram_ferdosi, bigram_ferdosi);
            p2 = probabilityOfSentence(sentence, unigram_hafez, bigram_hafez);
            p3 = probabilityOfSentence(sentence, unigram_molavi, bigram_molavi);
            if (p1 <= p2 && p1 <= p3) {
                output += "predicted for " + (i + 1) + "th sentence: ferdowsi\n";
                if (Integer.parseInt(tests[i][0]) == 1)
                    counter++;
            } else if (p2 <= p1 && p2 <= p3) {
                output += "predicted for " + (i + 1) + "th sentence: hafez\n";
                if (Integer.parseInt(tests[i][0]) == 2)
                    counter++;
            } else {
                output += "predicted for " + (i + 1) + "th sentence: molavi\n";
                if (Integer.parseInt(tests[i][0]) == 3)
                    counter++;
            }
        }
        output += "correctly predicted : " + String.format("%.2f" , (double) counter * 100 / testLength) + "\n";
        return output;
    }

    public String getResult() {
        return result;
    }

    private double probabilityOfSentence(ArrayList<String> analyze, Model unigram, Model bigram) {
        double output = 0;
        String[] normalized = bigram.normalizeToModel(analyze);
        int size = normalized.length;

        for (int i = 1; i < size; i++) {
            output += backOff3(normalized, unigram, bigram, i);
        }
        return output;
    }

    public double backOff3(String[] normalized, Model unigram, Model bigram, int place) {
        double weight1 = 0.34;
        double weight2 = 0.65;
        double weight3 = 0.01;
        double ep = 0.0001;
        double output;
        output = (unigram.deepSearch(new ArrayList<>(Collections.singletonList(normalized[place]))) * weight1) +
                (bigram.deepSearch(new ArrayList<>(Arrays.asList(normalized[place - 1], normalized[place]))) * weight2) +
                (weight3 * ep);
        return -Math.log(output);
    }


    private String readFile(String path, int first, int last) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String everything;
        int i = 0;
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null && i < last) {
                if (i >= first) {
                    sb.append(line);
                    sb.append("\n");
                }
                line = br.readLine();
                i++;
            }
            everything = sb.toString();
        } finally {
            br.close();
        }
        return everything;
    }


    public boolean store(String path) {
        File f = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(unigram_ferdosi);
            oos.writeObject(unigram_molavi);
            oos.writeObject(unigram_hafez);
            oos.writeObject(bigram_ferdosi);
            oos.writeObject(bigram_molavi);
            oos.writeObject(bigram_hafez);
            oos.close();
            fos.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public boolean load(String path) {
        File f = new File(path);
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.unigram_ferdosi = (Model) ois.readObject();
            this.unigram_molavi = (Model) ois.readObject();
            this.unigram_hafez = (Model) ois.readObject();
            this.bigram_ferdosi = (Model) ois.readObject();
            this.bigram_molavi = (Model) ois.readObject();
            this.bigram_hafez = (Model) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Program program = new Program("savedModel.learned", "train_set\\ferdowsi_train.txt", "train_set\\molavi_train.txt", "train_set\\hafez_train.txt", "test_set\\test_file.txt", 10000, 3000);
        System.out.println("\n\n" + program.getResult());
    }
}

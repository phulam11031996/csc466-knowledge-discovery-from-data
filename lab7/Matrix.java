import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Matrix {
    private ArrayList<ArrayList<Integer>> matrix = new ArrayList<ArrayList<Integer>>();

    public Matrix() {

    }

    // Examines only the specified rows of the array. It returns the number of rows
    // in which the element at position attribute (a number between 0 and 4) is
    // equal to value.
    private int findFrequency(int attribute, int value, ArrayList<Integer> rows) {
        int freq = 0;
        for (int i : rows) {
            if (this.matrix.get(i).get(attribute) == value) {
                freq++;
            }
        }
        return freq;
    }

    // Examines only the specified rows of the array. It returns a HashSet of the
    // different values for the specified attribute.
    private HashSet<Integer> findDifferentValues(int attribute, ArrayList<Integer> rows) {
        HashSet<Integer> res = new HashSet<>();
        for (int i : rows) {
            res.add(this.matrix.get(i).get(attribute));
        }
        return res;
    }

    // Examines only the specified rows of the array. Returns an ArrayList of the
    // rows where the value for the attribute is equal to value.
    private ArrayList<Integer> findRows(int attribute, int value, ArrayList<Integer> rows) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i : rows) {
            if (this.matrix.get(i).get(attribute) == value) {
                res.add(i);
            }
        }
        return res;
    }

    // returns log2 of the input
    private double log2(double number) {
        return Math.log(number) / Math.log(2);
    }

    // finds the entropy of the dataset that consists of the specified rows.
    private double findEntropy(ArrayList<Integer> rows) {
        double entropy = 0.0;
        HashMap<Integer, Integer> classCounts = new HashMap<>();
        for (int i : rows) {
            int classValue = this.matrix.get(i).get(this.matrix.get(i).size() - 1);
            classCounts.put(classValue, classCounts.getOrDefault(classValue, 0) + 1);
        }

        for (int count : classCounts.values()) {
            double p = (double) count / rows.size();
            entropy -= p * log2(p);
        }

        return entropy;
    }

    // finds the entropy of the dataset that consists of the specified rows after it
    // is partitioned on the attribute.
    private double findEntropy(int attribute, ArrayList<Integer> rows) {
        double entropy = 0.0;
        HashSet<Integer> differentValues = findDifferentValues(attribute, rows);
        for (int value : differentValues) {
            ArrayList<Integer> partitionedRows = findRows(attribute, value, rows);
            double p = (double) partitionedRows.size() / rows.size();
            entropy += p * findEntropy(partitionedRows);
        }

        return entropy;
    }

    // finds the information gain of partitioning on the attribute. Considers only
    // the specified rows.
    private double findGain(int attribute, ArrayList<Integer> rows) {
        return findEntropy(rows) - findEntropy(attribute, rows);
    }

    // returns the Information Gain Ratio, where we only look at the data defined by
    // the set of rows and we consider splitting on attribute.
    public double computeIGR(int attribute, ArrayList<Integer> rows) {
        double informationGain = findGain(attribute, rows);
        double splitInformation = 0;

        HashMap<Integer, ArrayList<Integer>> partitions = split(attribute, rows);
        for (int value : partitions.keySet()) {
            double ratio = (double) partitions.get(value).size() / rows.size();
            splitInformation -= ratio * log2(ratio);
        }

        // return the information gain ratio
        if (splitInformation == 0) {
            return 0;
        } else {
            return informationGain / splitInformation;
        }
    }

    // returns the most common category for the dataset that is the defined by the
    // specified rows.
    public int findMostCommonValue(ArrayList<Integer> rows) {
        HashMap<Integer, Integer> valueCounts = new HashMap<Integer, Integer>();
        for (int i : rows) {
            int classValue = this.matrix.get(i).get(this.matrix.get(i).size() - 1);
            if (valueCounts.containsKey(classValue)) {
                int count = valueCounts.get(classValue);
                valueCounts.put(classValue, count + 1);
            } else {
                valueCounts.put(classValue, 1);
            }
        }

        int mostCommonValue = 0;
        int maxCount = 0;
        for (int value : valueCounts.keySet()) {
            int count = valueCounts.get(value);
            if (count > maxCount) {
                mostCommonValue = value;
                maxCount = count;
            }
        }

        return mostCommonValue;
    }

    // Splits the dataset that is defined by rows on the attribute. Each element of
    // the HashMap that is returned contains the value for the attribute and an
    // ArrayList of rows that have this value.
    public HashMap<Integer, ArrayList<Integer>> split(int attribute, ArrayList<Integer> rows) {
        HashMap<Integer, ArrayList<Integer>> partitions = new HashMap<Integer, ArrayList<Integer>>();

        for (int i : rows) {
            int value = this.matrix.get(i).get(attribute);
            if (!partitions.containsKey(value)) {
                partitions.put(value, new ArrayList<Integer>());
            }
            partitions.get(value).add(i);
        }
        return partitions;
    }
}
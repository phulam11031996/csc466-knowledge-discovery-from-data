import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Matrix {
    private int[][] matrix;

    public Matrix(int[][] data) {
        this.matrix = data;
    }

    // Examines only the specified rows of the array. It returns a HashSet of the
    // different values for the specified attribute.
    private HashSet<Integer> findDifferentValues(int attribute, ArrayList<Integer> rows) {
        HashSet<Integer> res = new HashSet<>();
        for (int i : rows) {
            res.add(this.matrix[i][attribute]);
        }
        return res;
    }

    // Examines only the specified rows of the array. Returns an ArrayList of the
    // rows where the value for the attribute is equal to value.
    private ArrayList<Integer> findRows(int attribute, int value, ArrayList<Integer> rows) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i : rows) {
            if (this.matrix[i][attribute] == value) {
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
            int classValue = this.matrix[i][this.matrix[i].length - 1];
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

    // tested
    // returns the most common category for the dataset that is the defined by the
    // specified rows.
    public int findMostCommonValue(ArrayList<Integer> rows) {
        HashMap<Integer, Integer> categoryFreq = new HashMap<>();
        int maxFreqCategory = -1;
        int maxFreq = 0;
    
        // Loop through the specified rows
        for (int i = 0; i < rows.size(); i++) {
            int[] row = matrix[rows.get(i)];
            int category = row[row.length - 1];
    
            // Update the frequency of the category in the HashMap
            if (categoryFreq.containsKey(category)) {
                int freq = categoryFreq.get(category) + 1;
                categoryFreq.put(category, freq);
                if (freq > maxFreq) {
                    maxFreq = freq;
                    maxFreqCategory = category;
                }
            } else {
                categoryFreq.put(category, 1);
                if (1 > maxFreq) {
                    maxFreq = 1;
                    maxFreqCategory = category;
                }
            }
        }
    
        return maxFreqCategory;
    }

    // Splits the dataset that is defined by rows on the attribute. Each element of
    // the HashMap that is returned contains the value for the attribute and an
    // ArrayList of rows that have this value.
    public HashMap<Integer, ArrayList<Integer>> split(int attribute, ArrayList<Integer> rows) {
        HashMap<Integer, ArrayList<Integer>> partitions = new HashMap<Integer, ArrayList<Integer>>();
        for (int i : rows) {
            int value = this.matrix[i][attribute];
            if (!partitions.containsKey(value)) {
                partitions.put(value, new ArrayList<Integer>());
            }
            partitions.get(value).add(i);
        }
        return partitions;
    }
}
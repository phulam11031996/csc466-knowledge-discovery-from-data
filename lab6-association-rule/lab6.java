import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

class lab6 {

    public static List<List<Integer>> transactions = new ArrayList<>();
    public static HashSet<Integer> items = new HashSet<>();
    public static HashMap<Integer, List<List<Integer>>> frequentItemSet = new HashMap<>();
    public static ArrayList<Rule> rules;
    public static final double minSup = 0.01;
    public static final double minConfi = 0.99;

    // processes the input file
    public static void process(String fileName) {
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] arrData = data.split(", ");
                List<Integer> itemSet = new ArrayList<>();
                for (int i = 0; i < arrData.length; i++) {
                    if (i != 0) {
                        items.add(Integer.parseInt(arrData[i]));
                        itemSet.add(Integer.parseInt(arrData[i]));
                    }
                }
                transactions.add(itemSet);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // finds all k-itemsets, Returns false if no itemsets were found (precondition
    // k>=2)
    public static void findFrequentItemSets(int k) {
        List<List<Integer>> candItemSets = new ArrayList<>();
        List<List<Integer>> freqItemSets = new ArrayList<>();
        List<List<Integer>> preF = frequentItemSet.get(k - 1);
        for (int i = 0; i < preF.size(); i++) {
            for (int j = i + 1; j < preF.size(); j++) {
                List<Integer> itemSeti = preF.get(i);
                List<Integer> itemSetj = preF.get(j);
                if (canCombine(itemSeti, itemSetj)) {
                    List<Integer> tmp = new ArrayList<>();
                    tmp.addAll(itemSeti);
                    tmp.add(itemSetj.get(k - 2));
                    candItemSets.add(tmp);
                }
            }
        }
        for (List<Integer> itemList : candItemSets) {
            if (isFrequent(itemList)) {
                freqItemSets.add(itemList);
            }
        }
        frequentItemSet.put(k, freqItemSets);
    }

    // tells if the itemset is frequent, i.e., meets the minimum support
    public static boolean isFrequent(List<Integer> itemSet) {
        int supportCount = 0;
        for (List<Integer> transaction : transactions) {
            if (transaction.containsAll(itemSet)) {
                supportCount++;
            }
        }
        double support = (double) supportCount / transactions.size();
        return support >= minSup;
    }

    // finds all 1-itemsets
    public static void findFrequentSingleItemSets() {
        List<List<Integer>> result = new ArrayList<>();
        for (int i : items) {
            double support = transactions.stream()
                    .filter(itemSet -> itemSet.contains(i))
                    .count() / items.size();
            if (minSup <= support) {
                List<Integer> itemSet = new ArrayList<>();
                itemSet.add(i);
                result.add(itemSet);
            }
        }
        frequentItemSet.put(1, result);
    }

    public static void generateRules() {
        for (List<List<Integer>> listItemSets : frequentItemSet.values())
            for (List<Integer> itemSets : listItemSets)
                for (Rule rule : splitIntoTwo(itemSets))
                    if (isMinConfidenceMet(rule))
                        System.out.println(rule);
    }

    public static boolean isMinConfidenceMet(Rule r) {
        double leftCount = 0;
        double rightCount = 0;
        for (List<Integer> transaction : transactions) {
            if (transaction.containsAll(r.getLeft())) {
                leftCount += 1;
                if (transaction.containsAll(r.getRight())) {
                    rightCount += 1;
                }
            }
        }
        return (rightCount / leftCount) >= minConfi;
    }

    public static List<Rule> splitIntoTwo(List<Integer> inputList) {
        List<Rule> allCombinations = new ArrayList<>();
        int n = inputList.size();
        for (int i = 1; i < (1 << n); i++) {
            List<Integer> firstPartIndexes = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    firstPartIndexes.add(j);
                }
            }
            List<Integer> firstPart = new ArrayList<>();
            List<Integer> secondPart = new ArrayList<>();
            for (int k = 0; k < n; k++) {
                if (firstPartIndexes.contains(k)) {
                    firstPart.add(inputList.get(k));
                } else {
                    secondPart.add(inputList.get(k));
                }
            }
            Rule combination = new Rule(firstPart, secondPart);
            if (firstPart.size() != 0 && secondPart.size() != 0) {
                allCombinations.add(combination);
            }
        }
        return allCombinations;
    }

    public static boolean canCombine(List<Integer> itemSet1, List<Integer> itemSet2) {
        boolean flag = true;
        for (int i = 0; i < itemSet1.size() - 1; i++) {
            if (itemSet1.get(i) != itemSet2.get(i)) {
                flag = false;
            }
        }
        return flag;
    }

    public static void main(String args[]) {
        process("./files/shopping_data.txt");
        findFrequentSingleItemSets();
        findFrequentItemSets(2);
        findFrequentItemSets(3);
        findFrequentItemSets(4);
        findFrequentItemSets(5);

        generateRules();

    }
}
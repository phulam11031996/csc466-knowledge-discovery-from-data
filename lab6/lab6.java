import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

class lab6 {

    public static List<ItemSet> transactions = new ArrayList<>();
    public static HashSet<Integer> items = new HashSet<>();
    public static HashMap<Integer, ArrayList<ItemSet>> frequentItemSet = new HashMap<>();
    public static final double minSup = 0.01;

    // processes the input file
    public static void process(String fileName) {
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] arrData = data.split(", ");

                ItemSet itemSet = new ItemSet();
                for (int i = 0; i < arrData.length; i++) {
                    if (i != 0) {
                        int num = Integer.parseInt(arrData[i]);
                        items.add(num);
                        itemSet.addItem(num);
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

    public static boolean canCombine(ItemSet itemSet1, ItemSet itemSet2) {
        List<Integer> items1 = itemSet1.getItemSet();
        List<Integer> items2 = itemSet2.getItemSet();
        boolean flag = true;
        for (int i = 0; i < items1.size() - 1; i++) {
            if (items1.get(i) != items2.get(i)) {
                flag = false;
            }
        }
        return flag;
    }

    // finds all k-itemsets, Returns false if no itemsets were found (precondition
    // k>=2)
    public static void findFrequentItemSets(int k) {
        ArrayList<List<Integer>> candItemSets = new ArrayList<>();
        ArrayList<ItemSet> freqItemSets = new ArrayList<>();
        ArrayList<ItemSet> preF = frequentItemSet.get(k - 1);

        for (int i = 0; i < preF.size(); i++) {
            for (int j = i + 1; j < preF.size(); j++) {
                ItemSet itemSeti = preF.get(i);
                ItemSet itemSetj = preF.get(j);
                if (canCombine(itemSeti, itemSetj)) {
                    List<Integer> tmp = new ArrayList<>();
                    tmp.addAll(itemSeti.getItemSet());
                    tmp.add(itemSetj.getItemSet().get(k - 2));
                    candItemSets.add(tmp);
                }
            }
        }

        for (List<Integer> itemList : candItemSets) {
            if (isFrequent(new ItemSet(itemList))) {
                freqItemSets.add(new ItemSet(itemList));
            }
        }
        frequentItemSet.put(k, freqItemSets);
    }

    // tells if the itemset is frequent, i.e., meets the minimum support
    public static boolean isFrequent(ItemSet itemSet) {
        int supportCount = 0;
        for (ItemSet transaction : transactions) {
            if (transaction.getItemSet().containsAll(itemSet.getItemSet())) {
                supportCount++;
            }
        }
        double support = (double) supportCount / transactions.size();
        return support >= minSup;
    }

    // finds all 1-itemsets
    public static void findFrequentSingleItemSets() {
        ArrayList<ItemSet> result = new ArrayList<>();
        for (int i : items) {
            double support = transactions.stream()
                    .filter(itemSet -> itemSet.getItemSet().contains(i))
                    .count() / items.size();

            if (minSup <= support) {
                ItemSet itemSet = new ItemSet();
                itemSet.addItem(i);
                result.add(itemSet);
            }
        }
        frequentItemSet.put(1, result);
    }

    public static void main(String args[]) {
        process("./files/shopping_data.txt");
        findFrequentSingleItemSets();
        findFrequentItemSets(2);
        findFrequentItemSets(3);
        findFrequentItemSets(4);
        findFrequentItemSets(5);
        findFrequentItemSets(6);
        System.out.println(frequentItemSet);
    }
}
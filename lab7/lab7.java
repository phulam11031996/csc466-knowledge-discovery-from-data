import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class lab7 {

    // creates a two-dimensional array from the input file.
    public static int[][] process(String filename) {
        List<String> dataInput = new ArrayList<>();
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                dataInput.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        int[][] result = new int[dataInput.size()][5];
        for (int i = 0; i < dataInput.size(); i++) {
            String[] row = dataInput.get(i).split(",");
            for (int j = 0; j < 5; j++) {
                result[i][j] = (int) Double.parseDouble(row[j]);
            }
        }
        return result;
    }

    // recursive method that prints the decision tree. It takes as input the data,
    // the set of attributes that have not been used so far in this branch of the
    // tree, the set of rows to examine, the current level (initially 0, use to
    // determine how many tabs to print), and the information gain ratio from last
    // iteration (I set it initially equal to 100, used to create terminating
    // condition).
    public static void printDecisionTree(
            Matrix m,
            ArrayList<Integer> attributes,
            ArrayList<Integer> rows,
            int level,
            double currentIGR) {
        ArrayList<Integer> newAttribute = (ArrayList<Integer>) attributes.clone();

        if (attributes.isEmpty()) {
            int val = m.findMostCommonValue(rows);
            for (int j = 0; j < level; j++) {
                System.out.print('\t');
            }
            System.out.println("Has value " + val);
            return;
        }

        double maxE = -1.0;
        int atri = -1;
        for (int i : attributes) {
            double temp = m.computeIGR(i, rows);
            if (maxE < temp) {
                maxE = temp;
                atri = i;
            }
        }

        int index = newAttribute.indexOf(atri);
        int attributeSplit = newAttribute.remove(index);

        if (maxE <= 0.01) {
            int val = m.findMostCommonValue(rows);
            for (int j = 0; j < level; j++) {
                System.out.print('\t');
            }
            System.out.println("value = " + val);
            return;
        }

        HashMap<Integer, ArrayList<Integer>> splitHashMap = m.split(atri, rows);
        for (Map.Entry<Integer, ArrayList<Integer>> entry : splitHashMap.entrySet()) {
            for (int j = 0; j < level; j++) {
                System.out.print('\t');
            }
            System.out.println("When attribute " + (attributeSplit + 1) + " has value " + entry.getKey());
            printDecisionTree(m, newAttribute, entry.getValue(), level + 1, maxE);
        }
    }

    public static void main(String[] args) {
        int[][] array = process("files/data.txt");
        // first is row
        // second is col

        ArrayList<Integer> attribute = new ArrayList<>();
        attribute.add(0);
        attribute.add(1);
        attribute.add(2);
        attribute.add(3);

        ArrayList<Integer> rows = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            rows.add(i);
        }


        printDecisionTree(new Matrix(array), attribute, rows, 0, 100);


    }
}

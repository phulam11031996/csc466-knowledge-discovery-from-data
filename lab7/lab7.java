import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    public static void printDecisionTree(int[][] data, ArrayList<Integer> attributes, ArrayList<Integer> rows,
            int level, double currentIGR) {

    }

    public static void main(String[] args) {
        int[][] array = process("./files/data.txt");
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(array[i][j]);

            }
            System.out.println();
        }

    }
}
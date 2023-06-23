import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class lab8 {

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

    public static void main(String[] args) {
        int[][] array = process("./files/data.txt");
        Matrix matrix = new Matrix(array);

        int[] row = {5, 3, 1, 2};
        System.out.println(matrix.findProb(row, 1));
        System.out.println(matrix.findProb(row, 2));
        System.out.println(matrix.findProb(row, 3));
    }
}
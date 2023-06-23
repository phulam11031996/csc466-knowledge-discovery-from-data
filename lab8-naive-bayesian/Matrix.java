import java.util.ArrayList;
import java.util.Arrays;

public class Matrix {

    public int[][] matrix;
    public final double alpha = 1.0 / 150.0;
    public final double nj = 50.0;

    public double prc1;
    public double prc2;
    public double prc3;

    public Matrix(int[][] matrix) {
        this.matrix = matrix;
        calculatePrC();
    }

    public double getZ(int[] row) {
        double[] prRow = { 0.0, 0.0, 0.0, 0.0 };
        for (int i = 0; i < this.matrix.length; i++) {
            if (matrix[i][0] == row[0])
                prRow[0] += 1;
            if (matrix[i][1] == row[1])
                prRow[1] += 1;
            if (matrix[i][2] == row[2])
                prRow[2] += 1;
            if (matrix[i][3] == row[3])
                prRow[3] += 1;
        }
        prRow[0] = prRow[0] / 150.0;
        prRow[1] = prRow[1] / 150.0;
        prRow[2] = prRow[2] / 150.0;
        prRow[3] = prRow[3] / 150.0;
        return 1.0 / (prRow[0] * prRow[1] * prRow[2] * prRow[3]);
    }

    // tested
    public int getMi(int col) {
        return (int) Arrays.stream(this.matrix)
                .mapToInt(row -> row[col])
                .distinct()
                .count();
    }

    public double[] getNij(int[] row, int category) {
        double[] pr = { 0.0, 0.0, 0.0, 0.0 };
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < this.matrix.length; j++) {
                if (matrix[j][4] == category && matrix[j][i] == row[i]) {
                    pr[i] += 1;
                }
            }
        }
        return pr;
    }


    // takes as input the values for a single row, e.g., 5,3,1,2 and the category,
    // e.g. 2. Returns the probability that the row belongs to the category using
    // the Naïve Bayesian model.
    public double findProb(int[] row, int category) {
        double[] nij = getNij(row, category);
        double alpha = this.alpha;
        double nj = this.nj;
        double mi = getMi(category);
        double z = getZ(row);

        double[] prij = new double[4];
        for (int i = 0; i < nij.length; i++) {
            prij[i] = (nij[i] + alpha) / (nj + alpha * mi);
        }
        double pr = this.prc1 * prij[0] * prij[1] * prij[2] * prij[3];

        return pr;
    }






    public void calculatePrC() {
        for (int i = 0; i < this.matrix.length; i++) {
            if (matrix[i][4] == 1)
                this.prc1 += 1;
            if (matrix[i][4] == 2)
                this.prc2 += 1;
            if (matrix[i][4] == 3)
                this.prc3 += 1;
        }
        this.prc1 = this.prc1 / this.matrix.length;
        this.prc2 = this.prc2 / this.matrix.length;
        this.prc3 = this.prc3 / this.matrix.length;
    }

    //////////////////////////////////////////////////////////////////////////////
    // takes as input the values for a single row, e.g., 5,3,1,2. Returns the most
    // probable category of the row using the Naïve Bayesian Model.
    public int findCategory(int[] row) {
        return 0;
    }

    // returns the index of the category attribute
    public int getCategoryAttribute() {
        return 0;
    }

    // returns all the indices of all rows, e.g., 0,1,... up to the total number of
    // rows -1
    public ArrayList<Integer> findAllRows() {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < this.matrix.length; i++) {
            indices.add(i);
        }
        return indices;
    }
}

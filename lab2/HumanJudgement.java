import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class HumanJudgement {

    private ArrayList<String> lines = new ArrayList<>();
    private HashMap<Integer, ArrayList<Integer>> humanJudgement = new HashMap<Integer, ArrayList<Integer>>();

    public HumanJudgement(String fileName) {
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                this.lines.add(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        this.storeData();
    }

    public void storeData() {
        for (String s : this.lines) {
            String[] parts = s.split(" ");
            int key = Integer.parseInt(parts[0]);
            int value = Integer.parseInt(parts[1]);
            int third = Integer.parseInt(parts[2]);
            if (third == 1 || third == 2 || third == 3) {
                if (!this.humanJudgement.containsKey(key)) {
                    this.humanJudgement.put(key, new ArrayList<>());
                }
                this.humanJudgement.get(key).add(value);
            }
        }
    }
    
    public HashMap<Integer, ArrayList<Integer>> getHumanJudgement() {
        return humanJudgement;
    }
}

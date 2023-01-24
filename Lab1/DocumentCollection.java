import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class DocumentCollection {

    // data
    private LinkedList<String> lines = new LinkedList<>();
    private NoiseWord noiseWord = new NoiseWord();

    private HashMap<Integer, TextVector> docCollection = new HashMap<Integer, TextVector>();

    // constructor
    public DocumentCollection(String fileName) {
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
        Boolean flag = false;
        TextVector textVector = new TextVector();
        int iValue = -1;
        for (String line : this.lines) {
            if (line.contains(".I")) {
                if (iValue != -1)
                    this.docCollection.put(iValue, textVector);

                iValue = Integer.valueOf(line.split(" ")[1].strip());
                flag = false;
            }

            if (line.contains(".W")) {
                textVector = new TextVector();
                flag = true;
            }

            if (flag) {
                String[] words = line.split("[^a-zA-Z]+");
                for (String word : words) {
                    if (word.length() >= 2 && !this.isNoiseWord(word)) {
                        textVector.add(word);
                    }
                }
            }
        }

        this.docCollection.put(iValue, textVector);
    }

    // private
    private Boolean isNoiseWord(String word) {
        return this.noiseWord.getNoiseWords().contains(word);

    }

    // public
    public TextVector getDocumentBytId(int id) {
        return this.docCollection.get(id);
    }

    public int getAverageDocumentLength() {
        int sum = 0;
        for (TextVector textVector : this.docCollection.values()) {
            sum += textVector.getTotalWordCount();
        }
        return sum / this.docCollection.size();
    }

    public int getSize() {
        return this.docCollection.size();
    }

    public Collection<TextVector> getDocuments() {
        return this.docCollection.values();
    }

    public Set<Map.Entry<Integer, TextVector>> getEntrySet() {
        return this.docCollection.entrySet();
    }

}

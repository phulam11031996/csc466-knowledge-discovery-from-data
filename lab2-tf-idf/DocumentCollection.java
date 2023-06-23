import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

public class DocumentCollection implements Serializable {

    // private
    private String[] noiseWordArr = { "a", "about", "above", "all", "along",
            "also", "although", "am", "an", "and", "any", "are", "aren't", "as", "at",
            "be", "because", "been", "but", "by", "can", "cannot", "could", "couldn't",
            "did", "didn't", "do", "does", "doesn't", "e.g.", "either", "etc", "etc.",
            "even", "ever", "enough", "for", "from", "further", "get", "gets", "got", "had", "have",
            "hardly", "has", "hasn't", "having", "he", "hence", "her", "here",
            "hereby", "herein", "hereof", "hereon", "hereto", "herewith", "him",
            "his", "how", "however", "i", "i.e.", "if", "in", "into", "it", "it's", "its",
            "me", "more", "most", "mr", "my", "near", "nor", "now", "no", "not", "or", "on", "of", "onto",
            "other", "our", "out", "over", "really", "said", "same", "she",
            "should", "shouldn't", "since", "so", "some", "such",
            "than", "that", "the", "their", "them", "then", "there", "thereby",
            "therefore", "therefrom", "therein", "thereof", "thereon", "thereto",
            "therewith", "these", "they", "this", "those", "through", "thus", "to",
            "too", "under", "until", "unto", "upon", "us", "very", "was", "wasn't",
            "we", "were", "what", "when", "where", "whereby", "wherein", "whether",
            "which", "while", "who", "whom", "whose", "why", "with", "without",
            "would", "you", "your", "yours", "yes" };

    // data
    private HashSet<String> noiseWord = new HashSet<>();
    private LinkedList<String> lines = new LinkedList<>();
    private VectorType vectorType;
    private HashMap<Integer, TextVector> docCollection = new HashMap<>();

    // constructor
    public DocumentCollection(String fileName, VectorType vectorType) {
        this.vectorType = vectorType;
        Stream.of(noiseWordArr).forEach(word -> this.noiseWord.add(word));
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
        TextVector textVector = this.vectorType == VectorType.DOCUMENT
                ? new DocumentVector()
                : new QueryVector();
        int iValue = 0;
        for (String line : this.lines) {
            if (line.contains(".I")) {
                if (iValue != 0) {
                    this.docCollection.put(iValue, textVector);
                }
                iValue += 1;
                flag = false;
            }
            if (line.contains(".W")) {
                textVector = this.vectorType == VectorType.DOCUMENT
                        ? new DocumentVector()
                        : new QueryVector();
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

    public void normalize(DocumentCollection dc) {
        dc.getDocuments()
                .stream()
                .forEach(textVector -> textVector.normalize(dc));
    }

    private Boolean isNoiseWord(String word) {
        return this.noiseWord.contains(word);
    }

    // public
    public TextVector getDocumentBytId(int id) {
        return this.docCollection.get(id);
    }

    public double getAverageDocumentLength() {
        return this.docCollection.values()
                .stream()
                .mapToDouble(textVec -> textVec.getTotalWordCount())
                .average()
                .orElse(0);
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TextVector implements Serializable {
    // data
    protected HashMap<String, Integer> rawVector = new HashMap<String, Integer>();

    public abstract Set<Map.Entry<String, Double>> getNormalizedVectorEntrySet();

    public abstract void normalize(DocumentCollection dc);

    public abstract double getNormalizedFrequency(String word);

    // public
    public ArrayList<Integer> findClosestDocuments(DocumentCollection documents, DocumentDistance distanceAlg) {
        HashMap<Double, Integer> docSimilarity = new HashMap<>();
        ArrayList<Integer> result = new ArrayList<Integer>();

        documents.getEntrySet().stream().forEach(entry -> {
            Integer I = entry.getKey();
            TextVector textVectorDocument = entry.getValue();
            Double similarity = 0.0;

            if (textVectorDocument.getTotalWordCount() != 0) {
                similarity = distanceAlg.findDistance(this, textVectorDocument, documents);
            }
            docSimilarity.put(similarity, I);
        });

        List<Double> keys = new ArrayList<>(docSimilarity.keySet());
        Collections.sort(keys);
        Collections.reverse(keys);

        for (int i = 0; i < 20; i++) {
            result.add(docSimilarity.get(keys.get(i)));
        }

        return result;
    }

    public void add(String word) {
        word = word.toLowerCase();
        if (this.rawVector.containsKey(word)) {
            this.rawVector.put(word, this.rawVector.get(word) + 1);
        } else {
            this.rawVector.put(word, 1);
        }
    }

    public Boolean contains(String word) {
        return this.rawVector.containsKey(word);
    }

    // getter
    public Set<Map.Entry<String, Integer>> getRawVectorEntrySet() {
        return this.rawVector.entrySet();
    }

    public int getTotalWordCount() {
        return this.rawVector.values()
                .stream()
                .reduce(0, (a, b) -> a + b);
    }

    public int getRawFrequency(String word) {
        return this.rawVector.get(word);
    }

    public int getHighestRawFrequency() {
        int highestFrequency = 0;
        for (int fre : this.rawVector.values()) {
            highestFrequency = Math.max(fre, highestFrequency);
        }
        return highestFrequency;
    }

    public String getMostFrequentWord() {
        String mostFrequentWord = "";
        int highestFrequency = 0;
        for (String word : this.rawVector.keySet()) {
            if (this.rawVector.get(word) >= highestFrequency) {
                highestFrequency = this.rawVector.get(word);
                mostFrequentWord = word;
            }
        }
        return mostFrequentWord;
    }

    public int getDistinctWordCount() {
        return this.rawVector.size();
    }

    public double getL2Norm() {
        return Math.sqrt(this.rawVector.keySet().stream()
                .mapToDouble(word -> Math.pow(this.getNormalizedFrequency(word), 2))
                .sum());
    }

    @Override
    public String toString() {
        return this.rawVector.toString();
    }
}

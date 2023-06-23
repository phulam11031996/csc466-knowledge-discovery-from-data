import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Set;

public class DocumentVector extends TextVector {
    private HashMap<String, Double> normalizedVector = new HashMap<String, Double>();

    @Override
    public void normalize(DocumentCollection dc) {
        // tf-idf = fi / max * log(m / dfi)
        Integer max = this.getHighestRawFrequency();
        Integer m = dc.getSize();
        this.rawVector.entrySet().stream().forEach(textVector -> {
            String word = textVector.getKey();
            double fi = textVector.getValue();
            double dfi = dc.getDocumentFrequency(word);
            double tf_idf = (dfi != 0)
                    ? (fi / max) * (Math.log(m / dfi) / Math.log(2))
                    : 0;
            this.normalizedVector.put(word, tf_idf);
        });
    }

    @Override
    public double getNormalizedFrequency(String word) {
        if (!this.normalizedVector.containsKey(word)) {
            return 0;
        }
        return this.normalizedVector.get(word);
    }

    @Override
    public Set<Entry<String, Double>> getNormalizedVectorEntrySet() {
        return this.normalizedVector.entrySet();
    }

    public HashMap<String, Double> getNormalizedVector() {
        return normalizedVector;
    }
}
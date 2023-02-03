import java.util.ArrayList;
import java.util.stream.IntStream;

public class CosineDistance implements DocumentDistance {

    @Override
    public double findDistance(TextVector query, TextVector document, DocumentCollection documents) {
        ArrayList<Double> arrQ = new ArrayList<>();
        ArrayList<Double> arrD = new ArrayList<>();

        query.getNormalizedVectorEntrySet().stream().forEach(entry -> {
            String word = entry.getKey();
            if (document.rawVector.containsKey(word)) {
                arrD.add(document.getNormalizedFrequency(word));
                arrQ.add(query.getNormalizedFrequency(word));
            }
        });

        double queryMag = query.getL2Norm();
        double documentMag = document.getL2Norm();
        Double dotProduct = IntStream.range(0, arrD.size())
                .mapToDouble(i -> arrQ.get(i) * arrD.get(i))
                .sum();

        return dotProduct / (queryMag * documentMag);
    }
}

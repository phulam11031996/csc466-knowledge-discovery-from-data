import java.util.Map;

public class OkapiBM25 implements DocumentDistance {
    private double k1 = 1.2;
    private double k2 = 100;
    private double b = 0.75;

    @Override
    public double findDistance(TextVector query, TextVector document, DocumentCollection documents) {
        double N = documents.getSize();
        double dlj = document.getTotalWordCount();
        double avdl = documents.getAverageDocumentLength();

        double similarity = 0;
        for (Map.Entry<String, Integer> entry : query.getRawVectorEntrySet()) {
            double dfi = documents.getDocumentFrequency(entry.getKey());
            double fij = document.getRawFrequency(entry.getKey());
            double fiq = entry.getValue();

            double first = (N * dfi + 0.5) / (dfi + 0.5);
            double second = ((this.k1 + 1) * fij) / ((this.k1 * (1 - this.b + this.b * (dlj / avdl))) + fij);
            double third = ((this.k2 + 1) * fiq) * (this.k2 + fiq);
            if (first != 0 && second != 0 && third != 0) {
                similarity += Math.log(first * second * third);
            }
        }
        return similarity;
    }


}

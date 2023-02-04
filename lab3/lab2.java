import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class lab2 {
    public static DocumentCollection documents = new DocumentCollection("./files/documents.txt", VectorType.DOCUMENT);
    public static DocumentCollection queries = new DocumentCollection("./files/queries.txt", VectorType.QUERY);

    public static HashMap<Integer, ArrayList<Integer>> getQueriesResult(
            DocumentCollection qc,
            DocumentCollection dc,
            DocumentDistance dd) {
        HashMap<Integer, ArrayList<Integer>> queriesResult = new HashMap<>();
        int count = 0;
        for (Map.Entry<Integer, TextVector> entry : qc.getEntrySet()) {
            int queryNum = entry.getKey();
            ArrayList<Integer> top20Doc = entry.getValue().findClosestDocuments(dc, dd);
            queriesResult.put(queryNum, top20Doc);
            count += 1;
            if (count == 20) {
                break;
            }
            System.out.println(top20Doc);
        }
        return queriesResult;
    }

    public static HashMap<Integer, Double> computeMAP(
            HashMap<Integer, ArrayList<Integer>> humanJudgement,
            HashMap<Integer, ArrayList<Integer>> returnedDocuments) {

        HashMap<Integer, Double> mapScores = new HashMap<>();

        for (Map.Entry<Integer, ArrayList<Integer>> entry : humanJudgement.entrySet()) {
            Integer queryId = entry.getKey();
            ArrayList<Integer> relevantDocuments = entry.getValue();
            ArrayList<Integer> retrievedDocuments = returnedDocuments.get(queryId);
            if (retrievedDocuments == null) {
                continue;
            }
            double averagePrecision = 0.0;
            int relevantRetrieved = 0;
            for (int i = 0; i < retrievedDocuments.size(); i++) {
                if (relevantDocuments.contains(retrievedDocuments.get(i))) {
                    relevantRetrieved++;
                    averagePrecision += (double) relevantRetrieved / (i + 1);
                }
            }
            averagePrecision /= relevantDocuments.size();
            mapScores.put(queryId, averagePrecision);
        }
        return mapScores;
    }

    public static void main(String[] args) {
        CosineDistance cosineDistance = new CosineDistance();
        OkapiBM25 okapiBM25 = new OkapiBM25();
        HumanJudgement hj = new HumanJudgement("./files/human_judgement.txt");

        documents.normalize(documents);
        queries.normalize(documents);

        HashMap<Integer, ArrayList<Integer>> humanJudgement = hj.getHumanJudgement();
        HashMap<Integer, ArrayList<Integer>> queryResultCosineDis = getQueriesResult(
                queries,
                documents,
                cosineDistance);
        System.out.println();
        HashMap<Integer, ArrayList<Integer>> queryResultOkapi = getQueriesResult(
                queries,
                documents,
                okapiBM25);

        System.out.println(computeMAP(humanJudgement, queryResultCosineDis).entrySet()
                .stream()
                .mapToDouble(entry -> entry.getValue())
                .average()
                .orElseThrow());

        System.out.println(computeMAP(humanJudgement, queryResultOkapi).entrySet()
                .stream()
                .mapToDouble(entry -> entry.getValue())
                .average()
                .orElseThrow());
    }
}

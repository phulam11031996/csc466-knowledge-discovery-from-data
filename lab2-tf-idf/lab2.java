import java.util.ArrayList;

public class lab2 {
    public static DocumentCollection documents = new DocumentCollection("./files/documents.txt", VectorType.DOCUMENT);
    public static DocumentCollection queries = new DocumentCollection("./files/queries.txt", VectorType.QUERY);

    public static void main(String[] args) {
        CosineDistance cosineDistance = new CosineDistance();

        documents.normalize(documents);
        queries.normalize(queries);

        queries.getEntrySet().stream().forEach(entry -> {
            TextVector textVectorQuery = entry.getValue();
            textVectorQuery.findClosestDocuments(documents, cosineDistance);
            Integer queryNumber = entry.getKey();
            ArrayList<Integer> rankDoc = textVectorQuery.findClosestDocuments(documents, cosineDistance);
            System.out.print(queryNumber);
            System.out.print(" ");
            System.out.println(rankDoc);
        });
    }
}

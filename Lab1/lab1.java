
public class lab1 {

    public static void main(String[] args) {
        DocumentCollection docs = new DocumentCollection("documents.txt");

        String word = "";
        int fre = 0;
        int distictWord = 0;
        int totalWordCount = 0;
        for (TextVector tv : docs.getDocuments()) {
            if (fre <= tv.getHighestRawFrequency()) {
                fre = tv.getHighestRawFrequency();
                word = tv.getMostFrequentWord();
            }
            distictWord += tv.getDistinctWordCount();
            totalWordCount += tv.getTotalWordCount();

        }
        System.out.println(word);
        System.out.println(fre);
        System.out.println(distictWord);
        System.out.println(totalWordCount);

    }
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class lab4 {
    public static List<Integer> sortByValue(HashMap<Integer, Double> map) {
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        List<Integer> result = new ArrayList<>();
        int count = 0;
        for (Map.Entry<Integer, Double> entry : list) {
            result.add(entry.getKey());
            count += 1;
            if (count == 20) {
                break;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        PageRank pg = new PageRank("./files/graph.txt");

        List<Integer> sortedKeys = sortByValue(pg.getPageRankNew());
        System.out.println(sortedKeys);

        // System.out.println(pg.getAdjacencyList());
        // System.out.println(pg.getNodes());
        // System.out.println(pg.getOutGoingLinks());

    }
}
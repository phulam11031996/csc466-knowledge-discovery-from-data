import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class PageRank {
    private HashSet<Integer> nodes = new HashSet<Integer>();
    private HashMap<Integer, HashSet<Integer>> adjacencyList = new HashMap<Integer, HashSet<Integer>>();
    private HashMap<Integer, Integer> outGoingLinks = new HashMap<Integer, Integer>();
    private HashMap<Integer, Double> pageRankOld = new HashMap<Integer, Double>();
    private HashMap<Integer, Double> pageRankNew = new HashMap<Integer, Double>();
    private double epsilon = 0.001;

    public PageRank(String fileName) {
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] node = data.split(",");
                this.addEdge(Integer.parseInt(node[0]), Integer.parseInt(node[2]));
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        computePageRank();
    }

    public void computePageRank() {
        double V = this.nodes.size();
        for (int node : this.nodes) {
            this.pageRankOld.put(node, 1.0 / V);
        }
        double L1Norm = 1;
        while (L1Norm >= this.epsilon) {
            for (int node : this.nodes) {
                double sum = 0.0;
                HashSet<Integer> edges = this.adjacencyList.get(node);
                for (int i : edges) {
                    sum += this.pageRankOld.get(i) / (double) this.outGoingLinks.get(i);
                }
                double rank = (0.1 / V) + (0.9 * sum);

                this.pageRankNew.put(node, rank);
            }
            L1Norm = findDistance();
            for (int node : this.nodes) {
                this.pageRankOld.put(node, this.pageRankNew.get(node));
            }
        }
    }

    public double findDistance() {
        double distance = 0.0;
        for (int node : this.nodes) {
            distance += Math.abs(this.pageRankOld.get(node) - this.pageRankNew.get(node));
        }
        return distance;
    }

    public void addEdge(int node1, int node2) {
        this.nodes.add(node1);
        this.nodes.add(node2);
        if (!this.adjacencyList.containsKey(node1)) {
            this.adjacencyList.put(node1, new HashSet<Integer>());
            this.outGoingLinks.put(node1, 0);
        }
        if (!this.adjacencyList.containsKey(node2)) {
            this.adjacencyList.put(node2, new HashSet<Integer>());
            this.outGoingLinks.put(node2, 0);
        }
        this.adjacencyList.get(node2).add(node1);
        this.outGoingLinks.put(node1, this.outGoingLinks.get(node1) + 1);
    }

    public HashSet<Integer> getNodes() {
        return nodes;
    }

    public HashMap<Integer, HashSet<Integer>> getAdjacencyList() {
        return adjacencyList;
    }

    public HashMap<Integer, Integer> getOutGoingLinks() {
        return outGoingLinks;
    }

    public HashMap<Integer, Double> getPageRankNew() {
        return pageRankNew;
    }

    public HashMap<Integer, Double> getPageRankOld() {
        return pageRankOld;
    }

}

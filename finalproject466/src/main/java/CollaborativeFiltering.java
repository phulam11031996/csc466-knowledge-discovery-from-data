import java.util.HashMap;
import java.util.List;

public class CollaborativeFiltering {
    private HashMap<String, HashMap< String, Integer>> userMapping = new HashMap<>();
    private HashMap<String, HashMap< String, Integer>> itemMapping = new HashMap<>();
    public CollaborativeFiltering(List<Review> reviews) {
        for (Review review : reviews) {
            if (!this.userMapping.containsKey(review.getUser_id())) {
                this.userMapping.put(review.getUser_id(), new HashMap<>());
            }
            this.userMapping.get(review.getUser_id()).put(review.getBusiness_id(), review.getStars());

            if (!this.itemMapping.containsKey(review.getBusiness_id())) {
                this.itemMapping.put(review.getBusiness_id(), new HashMap<>());
            }
            this.itemMapping.get(review.getBusiness_id()).put(review.getUser_id(), review.getStars());
        }
    }

    public HashMap<String, HashMap<String, Integer>> getItemMapping() {
        return itemMapping;
    }

    public HashMap<String, HashMap<String, Integer>> getUserMapping() {
        return userMapping;
    }

    public void normalizeRating() {
        
    }
}

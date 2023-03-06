import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollaborativeFiltering {
    private HashMap<String, HashMap<String, Double>> userMapping = new HashMap<>();
    private HashMap<String, HashMap<String, Double>> itemMapping = new HashMap<>();

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

    public HashMap<String, HashMap<String, Double>> getItemMapping() {
        return itemMapping;
    }

    public HashMap<String, HashMap<String, Double>> getUserMapping() {
        return userMapping;
    }

    public void normalizeRatings() {
        // First, find the min and max ratings for each user and item
        HashMap<String, Double> userMinRating = new HashMap<>();
        HashMap<String, Double> userMaxRating = new HashMap<>();

        for (HashMap<String, Double> userRatings : itemMapping.values()) {
            for (String userId : userRatings.keySet()) {
                double rating = userRatings.get(userId);
                if (!userMinRating.containsKey(userId) || rating < userMinRating.get(userId)) {
                    userMinRating.put(userId, rating);
                }
                if (!userMaxRating.containsKey(userId) || rating > userMaxRating.get(userId)) {
                    userMaxRating.put(userId, rating);
                }
            }
        }

        for (HashMap<String, Double> userRatings : itemMapping.values()) {
            for (String userId : userRatings.keySet()) {
                double rating = userRatings.get(userId);
                double minRating = userMinRating.get(userId);
                double maxRating = userMaxRating.get(userId);
                double normalizedRating = (rating - minRating) / (maxRating - minRating);
                userRatings.put(userId, normalizedRating * 5); // Scale the normalized rating to a 5-star rating scale
            }
        }

        for (Map.Entry<String, HashMap<String, Double>> entry : itemMapping.entrySet()) {
            String itemId = entry.getKey();
            for (Map.Entry<String, Double> innerEntry : entry.getValue().entrySet()) {
                userMapping.get(innerEntry.getKey()).put(itemId, innerEntry.getValue());

            }
        }
    }


    public double calculateSimilary(String businessId1, String businessId2) {
        List<String> intersection = this.itemMapping.get(businessId1).keySet()
                .stream()
                .filter(key -> this.itemMapping.get(businessId2).containsKey(key))
                .collect(Collectors.toList());
        if (intersection.size() == 1)
            return 0.0;
        double dotProduct = intersection
                .stream()
                .mapToDouble(userId -> this.itemMapping.get(businessId1).get(userId)
                        * this.itemMapping.get(businessId2).get(userId))
                .sum();
        double magnitude1 = Math.sqrt(intersection
                .stream()
                .mapToDouble(userId -> Math.pow(this.itemMapping.get(businessId1).get(userId), 2))
                .sum());
        double magnitude2 = Math.sqrt(intersection
                .stream()
                .mapToDouble(userId -> Math.pow(this.itemMapping.get(businessId2).get(userId), 2))
                .sum());
        return dotProduct / (magnitude1 * magnitude2);
    }

    public double calculateRating(String userId, String businessId) {
        double numerator = 0.0;
        double denominator = 0.0;
        for (Map.Entry<String, Double> item : this.userMapping.get(userId).entrySet()) {
            String itemId = item.getKey();
            double rating = item.getValue();
            if (!itemId.equals(businessId)) {
                double similarity = this.calculateSimilary(itemId, businessId);
                numerator += rating * similarity;
                denominator += similarity;
            }
        }
        return numerator / denominator;
    }

}

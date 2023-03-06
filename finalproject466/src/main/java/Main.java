import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Main {

    public static List<Review> processFile(String fileName) throws IOException {
        Reviews reviews;
        ObjectMapper mapper = new ObjectMapper();
        reviews = mapper.readValue(new File(fileName), Reviews.class);
        return reviews.getReviews();
    }

    public static double randomPrediction(CollaborativeFiltering cf, List<Review> reviews) {
        Random rand = new Random();
        Review randReview = reviews.get(rand.nextInt(reviews.size()));
        double predictRating = cf.calculateRating(randReview.getUser_id(), randReview.getBusiness_id());
        double actualRating = randReview.getStars();
        System.out.print(randReview);
        System.out.println("predicted_rating: " + predictRating);
        System.out.println(cf.getUserMapping().get(randReview.getUser_id()));
        System.out.println(cf.getItemMapping().get(randReview.getBusiness_id()));
        return Math.abs(predictRating - actualRating);
    }

    public static void main(String[] args) throws IOException {
        List<Review> reviews = processFile("./input/test.json");
        CollaborativeFiltering cf = new CollaborativeFiltering(reviews);
//        cf.normalizeRatings();

        randomPrediction(cf, reviews);

    }
}

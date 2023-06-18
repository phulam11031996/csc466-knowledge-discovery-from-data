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
        double predictRating = cf.calculateRating2(randReview.getUser_id(), randReview.getBusiness_id());
        double actualRating = randReview.getStars();
        System.out.print(randReview);
        System.out.println("predicted_rating: " + predictRating);
        System.out.println(cf.getUserMapping().get(randReview.getUser_id()));
        return Math.abs(predictRating - actualRating);
    }

    public static double testSet(CollaborativeFiltering cf, List<Review> reviews){
        double size = Math.ceil(reviews.size()* 0.2);
        double avgFalse = 0;
        int num = 0;
        for(int i = 0; i <  size; i++){
            double star = randomPrediction(cf, reviews);
            if(star >0){
                avgFalse += star;
                num += 1;
            }
        }
        return avgFalse / num;
    }
    public static void main(String[] args) throws IOException {
        List<Review> reviews = processFile("./input/yelp_academic_dataset_review.json");
//        System.out.println(reviews);
        CollaborativeFiltering cf = new CollaborativeFiltering(reviews);
        System.out.println("Average false is plus or minus: " + testSet(cf, reviews));
    }
}

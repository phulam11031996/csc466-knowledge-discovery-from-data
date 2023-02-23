import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static List<Review> processFile(String fileName) throws IOException {
        Reviews reviews;
        ObjectMapper mapper = new ObjectMapper();
        reviews = mapper.readValue(new File(fileName), Reviews.class);
        return reviews.getReviews();
    }
    public static void main(String[] args) throws IOException {
        List<Review> reviews = processFile("./input/yelp_academic_dataset_review.json");
        CollaborativeFiltering cf = new CollaborativeFiltering(reviews);
        System.out.println(cf.getItemMapping());
        System.out.println(cf.getUserMapping());

    }
}

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Reviews {

    private final List<Review> reviews;

    @JsonCreator
    private Reviews(@JsonProperty("reviews") List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<Review> getReviews() {
        return this.reviews;
    }
}

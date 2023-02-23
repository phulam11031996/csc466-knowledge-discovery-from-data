import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Reviews {

    private List<Review> reviews = new ArrayList<>();

    @JsonCreator
    private Reviews(@JsonProperty("reviews") List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<Review> getReviews() {
        return this.reviews;
    }
}

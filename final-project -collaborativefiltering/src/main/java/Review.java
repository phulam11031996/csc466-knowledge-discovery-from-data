import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Review {
    private final String user_id;
    private final String business_id;
    private final int stars;

    @JsonCreator
    private Review(@JsonProperty("review_id") String review_id,
            @JsonProperty("user_id") String user_id,
            @JsonProperty("business_id") String business_id,
            @JsonProperty("stars") int stars,
            @JsonProperty("useful") int useful,
            @JsonProperty("funny") int funny,
            @JsonProperty("cool") int cool,
            @JsonProperty("text") String text,
            @JsonProperty("date") String date) {
        this.user_id = user_id;
        this.business_id = business_id;
        this.stars = stars;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getBusiness_id() {
        return business_id;
    }

    public int getStars() {
        return stars;
    }

    public String toString() {
        StringBuilder resultStr = new StringBuilder();
        resultStr.append("user_id: " + this.user_id + "\n");
        resultStr.append("business_id: " + this.business_id + "\n");
        resultStr.append("actual rating: " + this.stars + "\n");
        return resultStr.toString();
    }

}

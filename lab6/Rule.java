import java.util.List;

public class Rule {

    public List<Integer> left, right;

    public Rule(List<Integer> left, List<Integer> right) {
        this.left = left;
        this.right = right;
    }

    public List<Integer> getLeft() {
        return left;
    }

    public List<Integer> getRight() {
        return right;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(left.toString());
        result.append("->");
        result.append(right.toString());
        return result.toString();
    }


}

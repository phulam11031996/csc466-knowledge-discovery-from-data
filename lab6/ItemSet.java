import java.util.ArrayList;
import java.util.List;

public class ItemSet {
    List<Integer> itemSet = new ArrayList<>();

    public ItemSet() {
    }

    public ItemSet(List<Integer> items) {
        for (int i : items) {
            this.itemSet.add(i);
        }
    }

    public void addItem(int itemNum) {
        this.itemSet.add(itemNum);
    }

    public List<Integer> getItemSet() {
        return itemSet;
    }

    @Override
    public String toString() {
        return this.itemSet.toString();
    }

}

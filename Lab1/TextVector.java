import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextVector implements Serializable {
   
    // data
    private HashMap<String, Integer> rawVector = new HashMap<String, Integer>();

    // public
    public void add(String word) {
        word = word.toLowerCase();
        if (this.rawVector.containsKey(word)) {
            this.rawVector.put(word, this.rawVector.get(word) + 1);
        } else {
            this.rawVector.put(word, 1);
        }
    }

    public Boolean contains(String word) {
        return this.rawVector.get(word) != null;
    }

    // getter
    public Set<Map.Entry<String, Integer>> getRawVectorEntrySet() {
        return this.rawVector.entrySet();
    }

    public int getTotalWordCount() {
        int sum = 0;
        for (int count : this.rawVector.values()) {
            sum += count;
        }
        return sum;
    }

    public int getRawFrequency(String word) {
        return this.rawVector.get(word);
    }

    public int getHighestRawFrequency() {
        int highestFrequency = 0;
        for (int fre : this.rawVector.values()) {
            highestFrequency = Math.max(fre, highestFrequency);
        }
        return highestFrequency;
    }

    public String getMostFrequentWord() {
        String mostFrequentWord = "";
        int highestFrequency = 0;
        for (String word : this.rawVector.keySet()) {
            if (this.rawVector.get(word) >= highestFrequency) {
                highestFrequency = this.rawVector.get(word);
                mostFrequentWord = word;
            }
        }
        return mostFrequentWord;
    }

    public int getDistinctWordCount() {
        return this.rawVector.size();
    }

    // override
    @Override
    public String toString() {
        return this.rawVector.toString();
    }

}

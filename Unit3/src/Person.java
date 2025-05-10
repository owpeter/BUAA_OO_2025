import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Person implements PersonInterface {
    private int id;
    private String name;
    private int age;
    private final HashMap<Integer, Person> acquaintance = new HashMap<>();
    private final HashMap<Integer, Integer> value = new HashMap<>();
    private final HashMap<Integer, TagInterface> tags = new HashMap<>();
    private final ArrayList<Integer> receivedArticles = new ArrayList<>();

    private int bestAcquaintanceId;
    private int bestAcquaintanceValue; // Store the value too

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        bestAcquaintanceId = Integer.MIN_VALUE;
        bestAcquaintanceValue = Integer.MIN_VALUE;
    }

    public boolean strictEquals(PersonInterface person) { return this.equals(person); }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    @Override
    public TagInterface getTag(int id) {
        if (tags.containsKey(id)) {
            return tags.get(id);
        }
        return null;
    }

    @Override
    public void addTag(TagInterface tag) {
        if (!containsTag(tag.getId())) {
            tags.put(tag.getId(), tag);
        }
    }

    @Override
    public void delTag(int id) {
        if (containsTag(id)) {
            tags.remove(id);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PersonInterface)) {
            return false;
        }
        return ((PersonInterface) obj).getId() == this.id;
    }

    @Override
    public boolean isLinked(PersonInterface person) {
        if (person.getId() == this.id) {
            return true;
        }
        return acquaintance.containsKey(person.getId());
    }

    @Override
    public int queryValue(PersonInterface person) {
        if (acquaintance.containsKey(person.getId())) {
            return value.get(person.getId());
        }
        return 0;
    }

    @Override
    public List<Integer> getReceivedArticles() {
        return receivedArticles;
    }

    public List<Integer> queryReceivedArticles() {
        int limit = Math.min(receivedArticles.size(), 5);
        return receivedArticles.subList(0, limit);
    }

    public void addRelation(Person person, int value) {
        if (!acquaintance.containsKey(person.getId())) {
            acquaintance.put(person.getId(), person);
            this.value.put(person.getId(), value);
        }
        if (value > bestAcquaintanceValue) {
            bestAcquaintanceId = person.getId();
            bestAcquaintanceValue = value;
        } else if (value == bestAcquaintanceValue) {
            if (person.getId() < bestAcquaintanceId) {
                bestAcquaintanceId = person.getId();
            }
        }
    }

    public void delRelation(Person person) {
        if (acquaintance.containsKey(person.getId())) {
            acquaintance.remove(person.getId());
            this.value.remove(person.getId());
        }
        if (person.getId() == bestAcquaintanceId) {
            recalculateBestAcquaintance();
        }
    }

    public HashMap<Integer, TagInterface> getTags() {
        return tags;
    }

    public void setValue(Person person, int newValue) {
        int oldValue = value.get(person.getId());
        this.value.put(person.getId(), newValue);
        if (newValue > oldValue) {
            if (newValue > this.bestAcquaintanceValue) {
                this.bestAcquaintanceValue = newValue;
                this.bestAcquaintanceId = person.getId();
            } else if (newValue == this.bestAcquaintanceValue) {
                if (person.getId() < this.bestAcquaintanceId) {
                    this.bestAcquaintanceId = person.getId();
                }
            }
        } else if (newValue < oldValue) {
            if (person.getId() == this.bestAcquaintanceId) {
                recalculateBestAcquaintance();
            }
        }
    }

    public HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public int getAcquaintanceSize() {
        return acquaintance.size();
    }

    public int getBestAcquaintanceId() {
        return bestAcquaintanceId;
    }

    private void recalculateBestAcquaintance() {
        if (acquaintance.isEmpty()) {
            this.bestAcquaintanceId = -1;
            this.bestAcquaintanceValue = Integer.MIN_VALUE;
            return;
        }

        int currentBestId = -1;
        int currentMaxValue = Integer.MIN_VALUE;

        // Iterate through the current value map (source of truth)
        for (Map.Entry<Integer, Integer> entry : value.entrySet()) {
            int acqId = entry.getKey();
            int acqValue = entry.getValue();

            if (acqValue > currentMaxValue) {
                currentMaxValue = acqValue;
                currentBestId = acqId;
            } else if (acqValue == currentMaxValue) {
                if (currentBestId == -1 || acqId < currentBestId) {
                    currentBestId = acqId;
                }
            }
        }
        this.bestAcquaintanceId = currentBestId;
        this.bestAcquaintanceValue = currentMaxValue;
    }

    // Helper method for Network.queryReceivedArticles
    public void receiveArticle(int articleId) {
        receivedArticles.add(0, articleId);
    }

    public void removeReceivedArticle(int articleId) {
        receivedArticles.remove(Integer.valueOf(articleId));
    }
}

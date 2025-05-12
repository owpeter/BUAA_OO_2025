import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Tag implements TagInterface {

    private final int id;
    private final long time;
    private final Map<Integer, PersonInterface> persons;
    private int valueSum = 0;

    public Tag(int id) {
        this.id = id;
        this.persons = new HashMap<>();
        this.time = System.currentTimeMillis();
    }

    @Override
    public int getId() {
        return this.id;
    }

    public Map<Integer, PersonInterface> getPersons() {
        return persons;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof TagInterface)) {
            return false;
        }
        Tag tag = (Tag) obj;
        return id == tag.id &&
                time == tag.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time); // 确保包含所有参与equals的字段
    }

    public void addPerson(PersonInterface person) {
        if (person != null) {
            // int valueChange = 0;
            // for (PersonInterface personI : persons.values()) {
            //     if (personI.isLinked(person)) {
            //         valueChange += 2 * personI.queryValue(person);
            //     }
            // }
            this.persons.put(person.getId(), person);
            // valueChange += person.queryValue(person);
            // this.valueSum += valueChange;
        }
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        if (person == null) {
            return false;
        }
        return this.persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        return this.valueSum;

    }

    @Override
    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        }
        long ageSum = 0;
        for (PersonInterface person : persons.values()) {
            ageSum += person.getAge();
        }
        return (int) (ageSum / persons.size());
    }

    @Override
    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        }
        int mean = getAgeMean();
        long varianceSum = 0;
        for (PersonInterface person : persons.values()) {
            long diff = person.getAge() - mean;
            varianceSum += diff * diff;
        }
        return (int) (varianceSum / persons.size());
    }

    @Override
    public void delPerson(PersonInterface person) {
        for (PersonInterface personI : persons.values()) {
            if (personI.isLinked(person)) {
                this.valueSum -= 2 * personI.queryValue(person);
            }
        }
        if (person != null && this.persons.containsKey(person.getId())) {
            this.persons.remove(person.getId());
        }
    }

    @Override
    public int getSize() {
        return this.persons.size();
    }

    public void updateValueSumForRelationValueChange(PersonInterface person1,
        PersonInterface person2, int deltaValue) {
        if (hasPerson(person1) && hasPerson(person2) && !person1.equals(person2)) {
            this.valueSum += 2 * deltaValue;
        }
    }

    public void updateValueSumForRelationAddition(PersonInterface person1,
        PersonInterface person2, int relationValue) {
        if (hasPerson(person1) && hasPerson(person2) && !person1.equals(person2)) {
            this.valueSum += 2 * relationValue;
        }
    }

    public void updateValueSumForRelationRemoval(PersonInterface person1,
        PersonInterface person2, int oldRelationValue) {
        if (hasPerson(person1) && hasPerson(person2) && !person1.equals(person2)) {
            this.valueSum -= 2 * oldRelationValue;
        }
    }
}
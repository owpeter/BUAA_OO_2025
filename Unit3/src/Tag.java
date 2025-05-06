import java.util.HashMap;
import java.util.Map;

import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

public class Tag implements TagInterface {

    private final int id;
    private final Map<Integer, PersonInterface> persons;
    private int valueSum = 0;

    public Tag(int id) {
        this.id = id;
        this.persons = new HashMap<>();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof TagInterface)) {
            return false;
        }
        return ((TagInterface) obj).getId() == this.id;
    }

    public void addPerson(PersonInterface person) {
        if (person != null) {
            int valueChange = 0;
            for (PersonInterface personI : persons.values()) {
                if (personI.isLinked(person)) {
                    valueChange += 2 * personI.queryValue(person);
                }
            }
            this.persons.put(person.getId(), person);
            valueChange += person.queryValue(person);
            this.valueSum += valueChange;
        }
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        if (person == null) {
            return false;
        }
        return this.persons.containsKey(person.getId());
    }

    // @Override
    // public int getValueSum() {
    //     // return this.valueSum;
    //
    // }
    // Inside your Tag.java class (assuming 'persons' is a Map<Integer, PersonInterface>)
    @Override
    public int getValueSum() {
        long sum = 0; // Use long to prevent overflow during calculation
        PersonInterface[] personArray = persons.values().toArray(new PersonInterface[0]);
        int n = personArray.length;

        for (int i = 0; i < n; i++) {
            PersonInterface p1 = personArray[i];
            for (int j = 0; j < n; j++) { // Iterate including i == j
                PersonInterface p2 = personArray[j];
                if (p1.isLinked(p2)) { // isLinked is true for p1 == p2
                    sum += p1.queryValue(p2); // queryValue is 0 for p1 == p2
                }
            }
        }
        // The JML implies summing over all pairs (i, j) where they are linked.
        // The loop structure naturally counts edge (p1, p2) and (p2, p1) if p1!=p2.
        // And correctly handles (p1, p1) where value is 0.
        return (int) sum;
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
        if (person != null && this.persons.containsKey(person.getId())) {
            int valueChange = 0;
            PersonInterface personToRemove = this.persons.get(person.getId());
            for (PersonInterface personI : persons.values()) {
                if (personI.isLinked(personToRemove)) {
                    valueChange += 2 * personI.queryValue(personToRemove);
                }
            }
            this.persons.remove(person.getId());
            valueChange += person.queryValue(person);
            this.valueSum -= valueChange; // Update sum
        }
    }

    @Override
    public int getSize() {
        return this.persons.size();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.id);
    }
}
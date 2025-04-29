import java.util.HashMap;
import java.util.Map;

import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

public class Tag implements TagInterface {

    private final int id;
    private final Map<Integer, PersonInterface> persons;

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
        // JML spec requires comparison based on ID
        return ((TagInterface) obj).getId() == this.id;
    }

    public void addPerson(PersonInterface person) {
        if (person != null) {
            this.persons.put(person.getId(), person);
        }
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        if (person == null) {
            return false; // Cannot contain a null person
        }
        return this.persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        // The JML defines a sum over persons within the tag.
        // Iterate through all pairs of persons in the 'persons' map.
        long valueSum = 0; // Use long for summation to avoid potential overflow

        for (PersonInterface personI : persons.values()) {
            for (PersonInterface personJ : persons.values()) {
                // Check if personI is linked to personJ according to the PersonInterface
                // and the JML condition persons[i].isLinked(persons[j])
                if (personI.isLinked(personJ)) {
                    // Add the value of the link persons[i].queryValue(persons[j])
                    valueSum += personI.queryValue(personJ);
                }
            }
        }

        // Cast the final sum to int as per the JML return type
        return (int) valueSum;
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
        if (person != null) {
            this.persons.remove(person.getId());
        }
    }

    @Override
    public int getSize() {
        return this.persons.size();
    }

    // Optional: Override hashCode if equals is overridden.
    // Consistent with equals (based on ID).
    @Override
    public int hashCode() {
        return Integer.hashCode(this.id);
    }
}
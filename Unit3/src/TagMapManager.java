import java.util.HashMap;
import java.util.Map;

public class TagMapManager {

    private HashMap<Integer, HashMap<Long, Tag>> tagsMapById;

    public TagMapManager() {
        this.tagsMapById = new HashMap<>();
    }

    public void addPerson(Integer personId) {
        if (!this.tagsMapById.containsKey(personId)) {
            this.tagsMapById.put(personId, new HashMap<>());
        }
    }

    public void addPersonToTag(Integer personId, Tag tag) {
        tagsMapById.get(personId).put(tag.getTime(), tag);
    }

    public void removePersonFromTag(Integer personId, Tag tag) {
        tagsMapById.get(personId).remove(tag.getTime());
    }

    public void removeTag(Tag tag) {
        for (Map.Entry<Integer, HashMap<Long, Tag>> entry : tagsMapById.entrySet()) {
            entry.getValue().remove(tag.getTime());
        }
    }

    public void addValue(Person person1, Person person2, Integer value) {
        for (Map.Entry<Long, Tag> entry : this.tagsMapById.get(person1.getId()).entrySet()) {
            entry.getValue().updateValueSumForRelationAddition(person1, person2, value);
        }
    }
    //    public void removePerson(Person person1, Person person2) {
    //        for (Map.Entry<Long, Tag> entry: this.tagsMapById.get(person1.getId()).entrySet()) {
    //            // 所有包含person1的tag，如果person2有这个tag，就要把person1从里面删掉
    //            if (person2.hasTag(entry.getValue().getId())) {
    //                entry.getValue().delPerson(person1);
    //            }
    //        }
    //        for (Map.Entry<Long, Tag> entry: this.tagsMapById.get(person2.getId()).entrySet()) {
    //            if (person1.hasTag(entry.getValue().getId())) {
    //                entry.getValue().delPerson(person2);
    //            }
    //        }
    //    }
}

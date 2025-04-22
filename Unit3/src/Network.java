import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.exceptions.EqualTagIdException;
import com.oocourse.spec1.exceptions.TagIdNotFoundException;
import com.oocourse.spec1.exceptions.AcquaintanceNotFoundException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons = new HashMap<>();
    // private final DynamicUnionFind uf = new DynamicUnionFind();
    private int tripleSum;

    public Network() {
        tripleSum = 0;
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        return persons.getOrDefault(id, null);
    }

    @Override
    public void addPerson(PersonInterface person) throws EqualPersonIdException {
        if (containsPerson(person.getId())) {
            throw new EqualPersonIdException(person.getId());
        }
        persons.put(person.getId(), (Person) person);
        // uf.add(person.getId());
    }

    @Override
    public void addRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualRelationException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        Person person1 = getPerson(id1);
        Person person2 = getPerson(id2);
        if (person1.isLinked(person2)) {
            throw new EqualRelationException(id1, id2);
        }
        // 实现添加关系的逻辑
        person1.addRelation(person2, value);
        person2.addRelation(person1, value);
        // uf.union(id1, id2);
        tripleSum += getSameAcquaintance(id1, id2);
    }

    @Override
    public void modifyRelation(int id1, int id2, int value)
        throws PersonIdNotFoundException, EqualPersonIdException, RelationNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        if (id1 == id2) {
            throw new EqualPersonIdException(id1);
        }
        Person person1 = getPerson(id1);
        Person person2 = getPerson(id2);
        if (!person1.isLinked(person2)) {
            throw new RelationNotFoundException(id1, id2);
        }
        // 实现修改关系的逻辑
        int currentValue1 = person1.queryValue(person2);
        int newValue1 = currentValue1 + value;

        if (newValue1 <= 0) {
            // 断开关系
            person1.delRelation(person2);
            person2.delRelation(person1);

            // 清理标签中的关系
            for (TagInterface tag : person1.getTags().values()) {
                if (tag.hasPerson(person2)) {
                    tag.delPerson(person2);
                }
            }
            for (TagInterface tag : person2.getTags().values()) {
                if (tag.hasPerson(person1)) {
                    tag.delPerson(person1);
                }
            }

            tripleSum -= getSameAcquaintance(id1, id2);
        } else {
            // 更新关系值
            int currentValue2 = person2.queryValue(person1);
            int newValue2 = currentValue2 + value;
            person1.setValue(person2, newValue1);
            person2.setValue(person1, newValue2);
        }
    }

    @Override
    public int queryValue(int id1, int id2)
        throws PersonIdNotFoundException, RelationNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }
        Person person1 = getPerson(id1);
        Person person2 = getPerson(id2);
        if (!person1.isLinked(person2)) {
            throw new RelationNotFoundException(id1, id2);
        }
        return person1.queryValue(person2);
    }

    @Override
    public boolean isCircle(int id1, int id2)
        throws PersonIdNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }

        if (id1 == id2) {
            return true;
        }

        return bfs(id1, id2);
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    @Override
    public void addTag(int personId, TagInterface tag)
        throws PersonIdNotFoundException, EqualTagIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        Person person = getPerson(personId);
        if (person.containsTag(tag.getId())) {
            throw new EqualTagIdException(tag.getId());
        }
        person.addTag(tag);
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, RelationNotFoundException,
        TagIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        if (!containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        }
        if (personId1 == personId2) {
            throw new EqualPersonIdException(personId1);
        }
        Person person1 = getPerson(personId1);
        Person person2 = getPerson(personId2);
        if (!person2.isLinked(person1)) {
            throw new RelationNotFoundException(personId1, personId2);
        }
        if (!person2.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        TagInterface tag = person2.getTag(tagId);
        if (tag.hasPerson(person1)) {
            throw new EqualPersonIdException(personId1);
        }

        if (tag.getSize() > 999) {
            return;
        }
        tag.addPerson(person1);
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        Person person = getPerson(personId);
        if (!person.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        return person.getTag(tagId).getAgeVar();
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        if (!containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        }
        Person person2 = getPerson(personId2);
        if (!person2.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        if (!person2.getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new PersonIdNotFoundException(personId1);
        }
        // 实现从 person2 的 tag 中删除 person1 的逻辑
        TagInterface tag = person2.getTag(tagId);
        tag.delPerson(getPerson(personId1));
    }

    @Override
    public void delTag(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        Person person = getPerson(personId);
        if (!person.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        person.delTag(tagId);
    }

    @Override
    public int queryBestAcquaintance(int id)
        throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        Person person = getPerson(id);
        if (person.getAcquaintance().isEmpty()) {
            throw new AcquaintanceNotFoundException(id);
        }
        return person.getBestAcquaintanceId();
    }

    private boolean bfs(int startId, int targetId) {
        Person startPerson = getPerson(startId);
        Person targetPerson = getPerson(targetId);

        // 使用队列进行 BFS
        Queue<Person> queue = new LinkedList<>();
        Set<Person> visited = new HashSet<>();

        queue.add(startPerson);
        visited.add(startPerson);

        while (!queue.isEmpty()) {
            Person current = queue.poll();

            // 遍历当前节点的所有熟人
            for (Person acquaintance : current.getAcquaintance().values()) {
                if (acquaintance.equals(targetPerson)) {
                    return true; // 找到目标节点
                }
                if (!visited.contains(acquaintance)) {
                    visited.add(acquaintance);
                    queue.add(acquaintance);
                }
            }
        }

        return false; // 未找到目标节点
    }

    private int getSameAcquaintance(int id1, int id2) {
        int cnt = 0;
        Person person1 = getPerson(id1);
        Person person2 = getPerson(id2);
        if (person1.getAcquaintanceSize() > person2.getAcquaintanceSize()) {
            for (Person person : person2.getAcquaintance().values()) {
                if (person1.isLinked(person)) {
                    cnt++;
                }
            }
            if (person2.getAcquaintance().containsKey(id1)) {
                cnt--;
            }
        } else {
            for (Person person : person1.getAcquaintance().values()) {
                if (person2.isLinked(person)) {
                    cnt++;
                }
            }
            if (person1.getAcquaintance().containsKey(id2)) {
                cnt--;
            }
        }

        return cnt;
    }
}

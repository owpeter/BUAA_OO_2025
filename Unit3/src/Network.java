import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec2.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec2.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec2.exceptions.EqualArticleIdException;
import com.oocourse.spec2.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec2.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec2.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec2.exceptions.PathNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons = new HashMap<>();
    private final HashMap<Integer, OfficialAccount> accounts = new HashMap<>();
    private final HashMap<Integer, Integer> articlesMap = new HashMap<>();
    // articleId -> contributorId
    // private final DynamicUnionFind uf = new DynamicUnionFind();
    private int tripleSum;

    public Network() {
        tripleSum = 0;
    }

    public PersonInterface[] getPersons() { return persons.values().toArray(new Person[0]); }

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

        tripleSum += getCorrectSameAcquaintanceCount(person1, person2);
        person1.addRelation(person2, value);
        person2.addRelation(person1, value);
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

        int currentValue = person1.queryValue(person2);
        int newValue = currentValue + value;

        if (newValue <= 0) {
            tripleSum -= getCorrectSameAcquaintanceCount(person1, person2);

            person1.delRelation(person2);
            person2.delRelation(person1);
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
        } else {
            // 更新关系值
            person1.setValue(person2, newValue);
            person2.setValue(person1, newValue);
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
    public int queryTagValueSum(int personId, int tagId)
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        Person person = getPerson(personId);
        if (!person.containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        }
        return person.getTag(tagId).getValueSum();
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
        TagInterface tag = person2.getTag(tagId);
        Person person1 = getPerson(personId1);
        if (!tag.hasPerson(person1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        tag.delPerson(person1);
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
        if (person.getAcquaintanceSize() == 0) {
            throw new AcquaintanceNotFoundException(id);
        }
        return person.getBestAcquaintanceId();
    }

    private boolean bfs(int startId, int targetId) {
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        queue.add(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            int currentId = queue.poll();
            Person current = getPerson(currentId);

            if (currentId == targetId) {
                return true;
            }

            for (Integer acqId : current.getAcquaintance().keySet()) {
                if (!visited.contains(acqId)) {
                    visited.add(acqId);
                    queue.add(acqId);
                }
            }
        }
        return false;
    }
    // --- End BFS Helper ---

    private int getCorrectSameAcquaintanceCount(Person person1, Person person2) {
        int cnt = 0;
        for (Person neighborOf1 : person1.getAcquaintance().values()) {
            if (neighborOf1.getId() != person2.getId() && person2.isLinked(neighborOf1)) {
                cnt++;
            }
        }
        return cnt;
    }

    // --- Missing Methods Implementation ---

    @Override
    public int queryCoupleSum() {
        int count = 0;
        for (Person person1 : persons.values()) {
            int id1 = person1.getId();
            int bestId1 = person1.getBestAcquaintanceId();

            if (bestId1 != -1 && containsPerson(bestId1)) {
                Person person2 = getPerson(bestId1);
                if (id1 < bestId1) {
                    if (person2.getAcquaintanceSize() > 0
                        && person2.getBestAcquaintanceId() == id1) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public int queryShortestPath(int id1, int id2)
        throws PersonIdNotFoundException, PathNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        }
        if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        }

        if (id1 == id2) {
            return 0;
        }

        Queue<Integer> queue = new LinkedList<>();
        Map<Integer, Integer> distance = new HashMap<>();

        queue.add(id1);
        distance.put(id1, 0);

        while (!queue.isEmpty()) {
            int currentId = queue.poll();
            int currentDist = distance.get(currentId);
            Person current = getPerson(currentId);

            if (currentId == id2) {
                return currentDist;
            }

            for (Integer neighborId : current.getAcquaintance().keySet()) {
                if (!distance.containsKey(neighborId)) {
                    distance.put(neighborId, currentDist + 1);
                    queue.add(neighborId);
                }
            }
        }
        throw new PathNotFoundException(id1, id2);
    }

    @Override
    public boolean containsAccount(int id) {
        return accounts.containsKey(id);
    }

    @Override
    public void createOfficialAccount(int personId, int accountId, String name)
        throws PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (containsAccount(accountId)) {
            throw new EqualOfficialAccountIdException(accountId);
        }
        Person owner = getPerson(personId);
        OfficialAccount newAccount = new OfficialAccount(personId, accountId, name);
        accounts.put(accountId, newAccount);
        newAccount.addFollower(owner);
    }

    @Override
    public void deleteOfficialAccount(int personId, int accountId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        DeleteOfficialAccountPermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccount account = accounts.get(accountId);
        if (account.getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        }
        accounts.remove(accountId);
    }

    @Override
    public boolean containsArticle(int id) {
        return articlesMap.containsKey(id);
    }

    @Override
    public void contributeArticle(int personId, int accountId, int articleId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        EqualArticleIdException, ContributePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccount account = accounts.get(accountId);
        Person contributor = getPerson(personId);
        if (containsArticle(articleId)) {
            throw new EqualArticleIdException(articleId);
        }
        if (!account.containsFollower(contributor)) {
            throw new ContributePermissionDeniedException(personId, articleId);
        }

        account.addArticle(contributor, articleId);

        articlesMap.put(articleId, personId);

        account.incrementContributor(personId);

        for (PersonInterface follower : account.getFollowersMap().values()) {
            ((Person) follower).receiveArticle(articleId);
        }
    }

    @Override
    public void deleteArticle(int personId, int accountId, int articleId)
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException,
        ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccount account = accounts.get(accountId);
        if (!account.containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        }
        if (account.getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        }

        int contributorId = articlesMap.get(articleId);

        account.removeArticle(articleId);

        articlesMap.remove(articleId);

        account.decrementContributor(contributorId);

        for (PersonInterface follower : account.getFollowersMap().values()) {
            ((Person) follower).removeReceivedArticle(articleId);
        }
    }

    @Override
    public void followOfficialAccount(int personId, int accountId)
        throws PersonIdNotFoundException,
        OfficialAccountIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        }
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        }
        OfficialAccount account = accounts.get(accountId);
        Person follower = getPerson(personId);
        if (account.containsFollower(follower)) {
            throw new EqualPersonIdException(personId);
        }
        account.addFollower(follower);
    }

    @Override
    public int queryBestContributor(int id) throws OfficialAccountIdNotFoundException {
        if (!containsAccount(id)) {
            throw new OfficialAccountIdNotFoundException(id);
        }
        return accounts.get(id).getBestContributor();
    }

    @Override
    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return getPerson(id).queryReceivedArticles();
    }
}
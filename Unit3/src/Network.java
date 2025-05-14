import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec3.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec3.exceptions.EqualArticleIdException;
import com.oocourse.spec3.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec3.exceptions.PathNotFoundException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons = new HashMap<>();
    private final HashMap<Integer, OfficialAccount> accounts = new HashMap<>();
    private final HashMap<Integer, Integer> articlesMap = new HashMap<>();
    private int tripleSum;
    private TagMapManager globalTags = new TagMapManager(); // personId -> <tagTime -> Tag>
    // 包含personId这个人的所有Tag
    private final HashMap<Integer, Message> messages = new HashMap<>();
    private final HashMap<Integer, Integer> emojis = new HashMap<>(); // emojiId -> heat

    public Network() {
        tripleSum = 0;
    }

    public MessageInterface[] getMessages() {
        return messages.values().toArray(new Message[0]);
    }

    public int[] getEmojiIdList() {
        return emojis.keySet().stream().mapToInt(Integer::intValue).toArray();
    }

    public int[] getEmojiHeatList() {
        return emojis.values().stream().mapToInt(Integer::intValue).toArray();
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
        globalTags.addPerson(person.getId());
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
        globalTags.addValue(person1, person2, value);
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
            globalTags.addValue(person1, person2, -currentValue);
            person1.delRelation(person2);
            person2.delRelation(person1);
            for (TagInterface tag : person1.getTags().values()) {
                if (tag.hasPerson(person2)) {
                    tag.delPerson(person2);
                    globalTags.removePersonFromTag(id2, (Tag) tag);
                }
            }
            for (TagInterface tag : person2.getTags().values()) {
                if (tag.hasPerson(person1)) {
                    tag.delPerson(person1);
                    globalTags.removePersonFromTag(id1, (Tag) tag);
                }
            }
        } else {
            globalTags.addValue(person1, person2, value);
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
        Tag tag = (Tag) person2.getTag(tagId);
        if (tag.hasPerson(person1)) {
            throw new EqualPersonIdException(personId1);
        }

        if (tag.getSize() > 999) {
            return;
        }

        tag.addPerson(person1);
        globalTags.addPersonToTag(personId1, tag);
        for (PersonInterface person : tag.getPersons().values()) {
            tag.updateValueSumForRelationAddition(person1, person, person1.queryValue(person));
        }
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
        Tag tag = (Tag) person2.getTag(tagId);
        Person person1 = getPerson(personId1);
        if (!tag.hasPerson(person1)) {
            throw new PersonIdNotFoundException(personId1);
        }
        tag.delPerson(person1);
        globalTags.removePersonFromTag(personId1, tag);
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
        Tag tag = (Tag) person.getTag(tagId);
        globalTags.removeTag(tag);
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
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        HashSet<Integer> visited = new HashSet<>();

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

    @Override
    public int queryCoupleSum() {
        int count = 0;
        for (Person person1 : persons.values()) {
            int id1 = person1.getId();
            int bestId1 = person1.getBestAcquaintanceId();
            if (bestId1 != Integer.MIN_VALUE && containsPerson(bestId1)) {
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

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        HashMap<Integer, Integer> distance = new HashMap<>();
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

    @Override
    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    @Override
    public boolean containsEmojiId(int id) {
        return emojis.containsKey(id);
    }

    @Override
    public void addMessage(MessageInterface message) throws EqualMessageIdException,
        EmojiIdNotFoundException, ArticleIdNotFoundException, EqualPersonIdException {
        int messageId = message.getId();
        if (containsMessage(messageId)) {
            throw new EqualMessageIdException(messageId);
        }
        if (message instanceof EmojiMessage
            && !containsEmojiId(((EmojiMessage) message).getEmojiId())) {
            throw new EmojiIdNotFoundException(((EmojiMessage) message).getEmojiId());
        }
        if (message instanceof ForwardMessage
            && !containsArticle(((ForwardMessage) message).getArticleId())) {
            throw new ArticleIdNotFoundException(((ForwardMessage) message).getArticleId());
        }
        if (message instanceof ForwardMessage
            && containsArticle(((ForwardMessage) message).getArticleId())
            && !(message).getPerson1().getReceivedArticles()
            .contains(((ForwardMessage) message).getArticleId())) {
            throw new ArticleIdNotFoundException(((ForwardMessage) message).getArticleId());
        }
        if (message.getType() == 0
            && message.getPerson1().equals(message.getPerson2())) {
            throw new EqualPersonIdException(message.getPerson1().getId());
        }
        messages.put(messageId, (Message) message);
    }

    @Override
    public Message getMessage(int id) {
        if (messages.containsKey(id)) {
            return messages.get(id);
        }
        return null;
    }

    @Override
    public void sendMessage(int id) throws RelationNotFoundException,
        MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MessageIdNotFoundException(id);
        }
        Message message = messages.get(id);
        Person sender = (Person) message.getPerson1();
        int type = message.getType();
        if (type == 0) { // 处理普通消息
            Person receiver = (Person) message.getPerson2();
            if (!sender.isLinked(receiver)) {
                throw new RelationNotFoundException(sender.getId(), receiver.getId());
            }
            sender.addSocialValue(message.getSocialValue());
            receiver.addSocialValue(message.getSocialValue());
            if (message instanceof RedEnvelopeMessage) {
                sender.addMoney(-((RedEnvelopeMessage) message).getMoney());
                receiver.addMoney(((RedEnvelopeMessage) message).getMoney());
            }
            if (message instanceof ForwardMessage) {
                ((Person) message.getPerson2())
                .receiveArticle(((ForwardMessage) message).getArticleId());
            }
            if (message instanceof EmojiMessage) {
                emojis.put(((EmojiMessage) message).getEmojiId(),
                    emojis.getOrDefault(((EmojiMessage) message).getEmojiId(), 0) + 1);
            }
            receiver.addMessage(message);
        } else if (type == 1) { // 处理群发消息
            int tagId = message.getTag().getId();
            if (!sender.containsTag(tagId)) {
                throw new TagIdNotFoundException(tagId);
            }
            Tag tag = (Tag) message.getTag();
            sender.addSocialValue(message.getSocialValue());
            for (PersonInterface person : tag.getPersons().values()) {
                person.addSocialValue(message.getSocialValue());
            }
            int tagSize = tag.getSize();
            if (message instanceof RedEnvelopeMessage && tagSize > 0) {
                int money = ((RedEnvelopeMessage) message).getMoney();
                sender.addMoney(-((money / tagSize) * tagSize));
                for (PersonInterface person : tag.getPersons().values()) {
                    person.addMoney(money / tagSize);
                }
            }
            if (message instanceof ForwardMessage && tagSize > 0) {
                for (PersonInterface person : tag.getPersons().values()) {
                    ((Person) person).receiveArticle(((ForwardMessage) message).getArticleId());
                }
            }
            if (message instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) message).getEmojiId();
                emojis.put(emojiId, emojis.getOrDefault(emojiId, 0) + 1);
            }
            for (PersonInterface person : tag.getPersons().values()) {
                ((Person) person).addMessage(message);
            }
        }
        messages.remove(id);
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!persons.containsKey(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return persons.get(id).getSocialValue();
    }

    @Override
    public List<MessageInterface> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (!persons.containsKey(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return persons.get(id).getReceivedMessages();
    }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (emojis.containsKey(id)) {
            throw new EqualEmojiIdException(id);
        }
        emojis.put(id, 0);
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!persons.containsKey(id)) {
            throw new PersonIdNotFoundException(id);
        }
        return persons.get(id).getMoney();
    }

    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!emojis.containsKey(id)) {
            throw new EmojiIdNotFoundException(id);
        }
        return emojis.get(id);
    }

    @Override
    public int deleteColdEmoji(int limit) {
        Iterator<Map.Entry<Integer, Integer>> iterator = emojis.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            if (entry.getValue() < limit) {
                iterator.remove();
            }
        }
        Iterator<Map.Entry<Integer, Message>> iterator1 = messages.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry<Integer, Message> entry = iterator1.next();
            if (entry.getValue() instanceof EmojiMessage) {
                if (!emojis.containsKey(((EmojiMessage) entry.getValue()).getEmojiId())) {
                    iterator1.remove();
                }
            }
        }
        return emojis.size();
    }
}
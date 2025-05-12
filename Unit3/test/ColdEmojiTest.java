import com.oocourse.spec3.exceptions.*;
import com.oocourse.spec3.main.*;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.Collectors;

public class ColdEmojiTest {

    private Network network;
    private Person person1;
    private Person person2;
    private Person person3;

    private static int nextPersonIdSource = 1;
    private static int nextMessageIdSource = 1;
    private static int nextTagIdSource = 1;

    // Helper to create and add a person, ensuring unique IDs for tests
    private Person addTestPersonToNetwork() throws EqualPersonIdException {
        Person p = new Person(nextPersonIdSource, "Person" + nextPersonIdSource, 20 + nextPersonIdSource);
        network.addPerson(p);
        nextPersonIdSource++;
        return p;
    }

    private MessageInterface deepCopyMessage(MessageInterface msg) {
        if (msg == null) {
            return null;
        }
        if (msg.getType() == 0) {
            if (msg instanceof EmojiMessage) {
                return new EmojiMessage(msg.getId(), ((EmojiMessage) msg).getEmojiId(),
                        msg.getPerson1(), msg.getPerson2());
            } else if (msg instanceof RedEnvelopeMessage) {
                return new RedEnvelopeMessage(msg.getId(), ((RedEnvelopeMessage) msg).getMoney(),
                        msg.getPerson1(), msg.getPerson2());
            } else if (msg instanceof ForwardMessage) {
                return new ForwardMessage(msg.getId(), ((ForwardMessage) msg).getArticleId(),
                        msg.getPerson1(), msg.getPerson2());
            } else {
                return new Message(msg.getId(), msg.getSocialValue(),
                        msg.getPerson1(), msg.getPerson2());
            }
        } else if (msg.getType() == 1) {
            if (msg instanceof EmojiMessage) {
                return new EmojiMessage(msg.getId(), ((RedEnvelopeMessage) msg).getMoney(),
                        msg.getPerson1(), msg.getTag());
            } else if (msg instanceof RedEnvelopeMessage) {
                return new RedEnvelopeMessage(msg.getId(), ((RedEnvelopeMessage) msg).getMoney(),
                        msg.getPerson1(), msg.getTag());
            } else if (msg instanceof ForwardMessage) {
                return new ForwardMessage(msg.getId(), ((ForwardMessage) msg).getArticleId(),
                        msg.getPerson1(), msg.getTag());
            } else {
                return new Message(msg.getId(), msg.getSocialValue(),
                        msg.getPerson1(), msg.getTag());
            }
        }
        throw new IllegalArgumentException("Unknown message type for deep copy: " + msg.getClass());
    }

    // Helper method to compare two MessageInterface objects
    private boolean areMessagesEqualDetailed(MessageInterface msg1, MessageInterface msg2) {
        if (msg1 == null || msg2 == null) return false;
        if (msg1.getClass() != msg2.getClass()) return false;

        if (msg1.getId() != msg2.getId() ||
                msg1.getSocialValue() != msg2.getSocialValue() ||
                msg1.getType() != msg2.getType()) {
            return false;
        }

        if (!msg1.getPerson1().equals(msg2.getPerson1())) {
            return false;
        }
        if (msg1.getType() == 0) {
            if (!msg1.getPerson2().equals(msg2.getPerson2())) {
                return false;
            }
        } else if (msg1.getType() == 1) {
            if (!msg1.getTag().equals(msg2.getTag())) {
                return false;
            }
        }

        if (msg1 instanceof EmojiMessageInterface) {
            return ((EmojiMessageInterface) msg1).getEmojiId() == ((EmojiMessageInterface) msg2).getEmojiId();
        } else if (msg1 instanceof RedEnvelopeMessageInterface) {
            return ((RedEnvelopeMessageInterface) msg1).getMoney() == ((RedEnvelopeMessageInterface) msg2).getMoney();
        } else if (msg1 instanceof ForwardMessageInterface) {
            return ((ForwardMessageInterface) msg1).getArticleId() == ((ForwardMessageInterface) msg2).getArticleId();
        }
        return true;
    }

    private void createEmojiMessage(Network network, Person person1, Person person2, int emojiId, int heat) throws EqualEmojiIdException, EmojiIdNotFoundException, EqualPersonIdException, EqualMessageIdException, ArticleIdNotFoundException, RelationNotFoundException, TagIdNotFoundException, MessageIdNotFoundException {
        network.storeEmojiId(emojiId); // heat 0
        EmojiMessage emojimsg = new EmojiMessage(nextMessageIdSource++, emojiId, person1, person2);

        for(int i=0; i < heat; i++) {
            network.addMessage(emojimsg);
            network.sendMessage(emojimsg.getId());
        }
        network.addMessage(emojimsg);
    }

    private void createRedEnvelopeMessage(Network network, Person person1, Person person2, int money, int sendTimes) throws EqualEmojiIdException, EmojiIdNotFoundException, EqualPersonIdException, EqualMessageIdException, ArticleIdNotFoundException, RelationNotFoundException, TagIdNotFoundException, MessageIdNotFoundException {
        RedEnvelopeMessage redEnvelopeMessage = new RedEnvelopeMessage(nextMessageIdSource++, money, person1, person2);
        for(int i=0; i < sendTimes; i++) {
            network.addMessage(redEnvelopeMessage);
            network.sendMessage(redEnvelopeMessage.getId());
        }
        network.addMessage(redEnvelopeMessage);
    }

    private void createForwardMessage(Network network, Person person1, Person person2,int accountId, int articleId, int sendTimes) throws EqualEmojiIdException, EmojiIdNotFoundException, EqualPersonIdException, EqualMessageIdException, ArticleIdNotFoundException, RelationNotFoundException, TagIdNotFoundException, MessageIdNotFoundException, EqualArticleIdException, PersonIdNotFoundException, ContributePermissionDeniedException, OfficialAccountIdNotFoundException {
        network.contributeArticle(person1.getId(),accountId,articleId);
        ForwardMessage forwardMessage = new ForwardMessage(nextMessageIdSource++, articleId, person1, person2);
        for (int i=0; i < sendTimes; i++) {
            network.addMessage(forwardMessage);
            network.sendMessage(forwardMessage.getId());
        }
        network.addMessage(forwardMessage);
    }

    private void createMessageType0(Network network,int socialValue, Person person1, Person person2) throws EmojiIdNotFoundException, EqualPersonIdException, EqualMessageIdException, ArticleIdNotFoundException {
        network.addMessage(new Message(nextMessageIdSource++, socialValue, person1, person2));
    }

    private void createMessageType1(Network network,int socialValue, Person person1, Tag tag) throws EmojiIdNotFoundException, EqualPersonIdException, EqualMessageIdException, ArticleIdNotFoundException {
        network.addMessage(new Message(nextMessageIdSource++, socialValue, person1, tag));
    }


    // Helper to check if a list contains an equivalent message
    private boolean messageListContains(List<MessageInterface> list, MessageInterface message) {
        if (message == null || list == null) return false;
        for (MessageInterface m : list) {
            if (m.getId() == message.getId()) {
                return true;
            }
        }
        return false;
    }

    public List<MessageInterface> getDeepCopiedMessages(Network net) {
        // 1. 从network类获取原始消息数组
        MessageInterface[] originalMessages = net.getMessages();
        // 2. 创建ArrayList来存储深克隆后的消息
        List<MessageInterface> clonedMessages = new ArrayList<>(originalMessages.length);
        // 3. 对每条消息进行深克隆并添加到列表中
        for (MessageInterface msg : originalMessages) {
            clonedMessages.add(deepCopyMessage(msg));
        }
        return clonedMessages;
    }

    public void assertRightRemaining(List<Integer> remainingEmojiIds, int result, List<Integer> oldEmojiIdList, List<Integer> oldEmojiHeatList, List<Integer> currentEmojiIdList, List<Integer> currentEmojiHeatList) {
        assertEquals(remainingEmojiIds.size(), result);
        assertEquals(remainingEmojiIds.size(), currentEmojiIdList.size());
        assertEquals(remainingEmojiIds.size(), currentEmojiHeatList.size());
        assertTrue(currentEmojiIdList.containsAll(remainingEmojiIds));
        assertHeatNotChanged(currentEmojiIdList, currentEmojiHeatList, oldEmojiIdList, oldEmojiHeatList);
    }

    public void assertHeatNotChanged(List<Integer> currentEmojiIdList, List<Integer> currentEmojiHeatList, List<Integer> oldEmojiIdList, List<Integer> oldEmojiHeatList) {
        for(int i=0; i<currentEmojiIdList.size(); i++) {
            int id = currentEmojiIdList.get(i);
            int heat = currentEmojiHeatList.get(i);
            int oldIdx = oldEmojiIdList.indexOf(id);
            assertEquals((int)oldEmojiHeatList.get(oldIdx), heat);
        }
    }

    public boolean verifyMessageDeletion(
            List<MessageInterface> originalMessages,
            List<MessageInterface> filteredMessages,
            List<Integer> remainingEmojiIds
    ) {
        // 1. 检查所有非 EmojiMessageInterface 消息是否全部保留
        boolean nonEmojiMessagesPreserved = originalMessages.stream()
                .filter(msg -> !(msg instanceof EmojiMessageInterface))
                .allMatch(msg -> filteredMessages.contains(msg));

        if (!nonEmojiMessagesPreserved) {
            return false; // 非表情消息被错误删除
        }

        // 2. 检查所有 EmojiMessageInterface 消息：
        //    - 如果 emojiId 在 remainingEmojiIds 中，则必须保留
        //    - 否则必须删除
        boolean emojiMessagesCorrectlyFiltered = originalMessages.stream()
                .filter(msg -> msg instanceof EmojiMessageInterface)
                .allMatch(msg -> {
                    int emojiId = ((EmojiMessageInterface) msg).getEmojiId();
                    if (remainingEmojiIds.contains(emojiId)) {
                        return filteredMessages.contains(msg); // 应该保留
                    } else {
                        return !filteredMessages.contains(msg); // 应该删除
                    }
                });

        if (!emojiMessagesCorrectlyFiltered) {
            return false; // 表情消息删除/保留错误
        }

        // 3. 检查 filteredMessages 没有引入新消息
        boolean noNewMessages = filteredMessages.stream()
                .allMatch(msg -> originalMessages.contains(msg));

        if (!noNewMessages) {
            return false; // 过滤后的列表包含新消息（不应该发生）
        }

        return true; // 所有检查通过
    }

    public boolean verifyMessageUnchanged(List<MessageInterface> originalMessages, List<MessageInterface> filteredMessages) {
        for (MessageInterface msg : filteredMessages) {
            if (messageListContains(originalMessages, msg)) {
                if (!areMessagesEqualDetailed(msg, originalMessages.get(originalMessages.indexOf(msg)))) {
                    return false;
                }
            }
        }
        return true;
    }


    private Map<Integer, String> initialPersonNames;
    private Map<Integer, Integer> initialPersonAges;
    // Add other relevant, checkable, non-assignable states here

    @Before
    public void setUp() throws Exception {
        network = new Network();
        nextPersonIdSource = 1;
        nextMessageIdSource = 1;
        nextTagIdSource = 1;

        person1 = addTestPersonToNetwork();
        person2 = addTestPersonToNetwork();
        person3 = addTestPersonToNetwork();

        network.addRelation(person1.getId(), person2.getId(), 10);
        network.addRelation(person2.getId(), person3.getId(), 10);

        // Capture initial state of entities that should NOT change
        initialPersonNames = new HashMap<>();
        initialPersonAges = new HashMap<>();
        PersonInterface p;
        if (network.containsPerson(person1.getId())) {
            p = network.getPerson(person1.getId());
            initialPersonNames.put(p.getId(), p.getName());
            initialPersonAges.put(p.getId(), p.getAge());
        }
        if (network.containsPerson(person2.getId())) {
            p = network.getPerson(person2.getId());
            initialPersonNames.put(p.getId(), p.getName());
            initialPersonAges.put(p.getId(), p.getAge());
        }
        if (network.containsPerson(person3.getId())) {
            p = network.getPerson(person3.getId());
            initialPersonNames.put(p.getId(), p.getName());
            initialPersonAges.put(p.getId(), p.getAge());
        }
    }

    @Test
    public void noMessage() throws Exception {
        List<Integer> oldEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> oldEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        List<MessageInterface> oldMessages = getDeepCopiedMessages(network);

        int limit = 5;
        int result = network.deleteColdEmoji(limit);

        List<Integer> currentEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> currentEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        List<MessageInterface> currentMessages = Arrays.asList(network.getMessages());

        List<Integer> remainingEmojiIds = Collections.emptyList();

        // 验证 emojiIdList 和 emojiHeatList 是否仅保留热度≥limit 的条目
        assertRightRemaining(remainingEmojiIds, result, oldEmojiIdList, oldEmojiHeatList, currentEmojiIdList, currentEmojiHeatList);
        //验证消息是否被正确保留或删除。
        assertTrue(verifyMessageDeletion(oldMessages, currentMessages, currentEmojiIdList));
        // 确保 messages 中的消息对象未被篡改
        assertTrue(verifyMessageUnchanged(oldMessages, currentMessages));
    }

    @Test
    public void allReserved() throws Exception {
        createEmojiMessage(network, person1, person2, 101, 10);
        createEmojiMessage(network, person1, person2, 102, 13);
        createEmojiMessage(network, person1, person2, 103, 5);
        createEmojiMessage(network, person2, person1, 104, 6);
        createEmojiMessage(network, person1, person2, 105, 5);

        List<Integer> oldEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> oldEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        // Messages used for setup are part of \old(messages)
        List<MessageInterface> oldMessages = getDeepCopiedMessages(network);

        int limit = 5;
        int result = network.deleteColdEmoji(limit);
        List<Integer> remainingEmojiIds = Arrays.asList(101, 102, 103, 104, 105);

        List<Integer> currentEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> currentEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        List<MessageInterface> currentMessages = Arrays.asList(network.getMessages());

        // 验证 emojiIdList 和 emojiHeatList 是否仅保留热度≥limit 的条目
        assertRightRemaining(remainingEmojiIds, result, oldEmojiIdList, oldEmojiHeatList, currentEmojiIdList, currentEmojiHeatList);
        //验证消息是否被正确保留或删除。
        assertTrue(verifyMessageDeletion(oldMessages, currentMessages, currentEmojiIdList));
        // 确保 messages 中的消息对象未被篡改
        assertTrue(verifyMessageUnchanged(oldMessages, currentMessages));
    }

    @Test
    public void allRemoved() throws Exception {
        createEmojiMessage(network, person1, person2, 101, 10);
        createEmojiMessage(network, person1, person2, 102, 13);
        createEmojiMessage(network, person1, person2, 103, 5);
        createEmojiMessage(network, person2, person1, 104, 6);
        createEmojiMessage(network, person1, person2, 105, 13);

        List<Integer> oldEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> oldEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        // Messages used for setup are part of \old(messages)
        List<MessageInterface> oldMessages = getDeepCopiedMessages(network);

        int limit = 14;
        int result = network.deleteColdEmoji(limit);
        List<Integer> remainingEmojiIds = Collections.emptyList();

        List<Integer> currentEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> currentEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        List<MessageInterface> currentMessages = Arrays.asList(network.getMessages());

        // 验证 emojiIdList 和 emojiHeatList 是否仅保留热度≥limit 的条目
        assertRightRemaining(remainingEmojiIds, result, oldEmojiIdList, oldEmojiHeatList, currentEmojiIdList, currentEmojiHeatList);
        //验证消息是否被正确保留或删除。
        assertTrue(verifyMessageDeletion(oldMessages, currentMessages, currentEmojiIdList));
        // 确保 messages 中的消息对象未被篡改
        assertTrue(verifyMessageUnchanged(oldMessages, currentMessages));
    }

    @Test
    public void reservedSome() throws Exception {
        createEmojiMessage(network, person2, person1, 100, 4);
        createEmojiMessage(network, person1, person2, 101, 13);
        createEmojiMessage(network, person1, person2, 102, 13);
        createEmojiMessage(network, person1, person2, 103, 5);
        createEmojiMessage(network, person2, person1, 104, 6);
        createEmojiMessage(network, person2, person1, 105, 1);

        List<Integer> oldEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> oldEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        // Messages used for setup are part of \old(messages)
        List<MessageInterface> oldMessages = getDeepCopiedMessages(network);

        int limit = 5;
        int result = network.deleteColdEmoji(limit);
        List<Integer> remainingEmojiIds = Arrays.asList(101, 102, 103, 104);

        List<Integer> currentEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> currentEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        List<MessageInterface> currentMessages = Arrays.asList(network.getMessages());

        // 验证 emojiIdList 和 emojiHeatList 是否仅保留热度≥limit 的条目
        assertRightRemaining(remainingEmojiIds, result, oldEmojiIdList, oldEmojiHeatList, currentEmojiIdList, currentEmojiHeatList);
        //验证消息是否被正确保留或删除。
        assertTrue(verifyMessageDeletion(oldMessages, currentMessages, currentEmojiIdList));
        // 确保 messages 中的消息对象未被篡改
        assertTrue(verifyMessageUnchanged(oldMessages, currentMessages));
    }

    @Test
    public void reservedNoneEmoji() throws Exception {
        createEmojiMessage(network, person2, person1, 100, 4);
        createEmojiMessage(network, person1, person2, 101, 13);
        createEmojiMessage(network, person1, person2, 102, 13);
        createEmojiMessage(network, person1, person2, 103, 5);

        network.createOfficialAccount(person1.getId(), 1, "official");
        createForwardMessage(network, person1, person2, 1, 1, 3);
        createForwardMessage(network, person1, person2, 1, 2, 2);
        createRedEnvelopeMessage(network, person1, person2, 3, 1);
        createMessageType0(network, 4, person1, person2);
        Tag tag = new Tag(1);
        tag.addPerson(person1);
        network.addTag(person1.getId(), tag);
        createMessageType1(network, 4, person1, tag);

        List<Integer> oldEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> oldEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        // Messages used for setup are part of \old(messages)
        List<MessageInterface> oldMessages = getDeepCopiedMessages(network);

        int limit = 10;
        int result = network.deleteColdEmoji(limit);
        List<Integer> remainingEmojiIds = Arrays.asList(101, 102);

        List<Integer> currentEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> currentEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        List<MessageInterface> currentMessages = Arrays.asList(network.getMessages());

        // 验证 emojiIdList 和 emojiHeatList 是否仅保留热度≥limit 的条目
        assertRightRemaining(remainingEmojiIds, result, oldEmojiIdList, oldEmojiHeatList, currentEmojiIdList, currentEmojiHeatList);
        //验证消息是否被正确保留或删除。
        assertTrue(verifyMessageDeletion(oldMessages, currentMessages, currentEmojiIdList));
        // 确保 messages 中的消息对象未被篡改
        assertTrue(verifyMessageUnchanged(oldMessages, currentMessages));
    }

    @Test
    public void onlyNoneEmoji() throws Exception {

        network.createOfficialAccount(person1.getId(), 1, "official");
        createForwardMessage(network, person1, person2, 1, 1, 3);
        createForwardMessage(network, person1, person2, 1, 2, 2);
        createRedEnvelopeMessage(network, person1, person2, 3, 1);
        createMessageType0(network, 4, person1, person2);
        Tag tag1 = new Tag(nextTagIdSource++);
        tag1.addPerson(person1);
        network.addTag(person1.getId(), tag1);
        createMessageType1(network, 4, person1, tag1);
        createMessageType1(network, 10, person2, tag1);
        Tag tag2 = new Tag(nextTagIdSource++);
        createMessageType1(network, 1, person2, tag2);

        List<Integer> oldEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> oldEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        // Messages used for setup are part of \old(messages)
        List<MessageInterface> oldMessages = getDeepCopiedMessages(network);

        int limit = 10;
        int result = network.deleteColdEmoji(limit);
        List<Integer> remainingEmojiIds = Collections.emptyList();

        List<Integer> currentEmojiIdList = Arrays.stream(network.getEmojiIdList()).boxed().collect(Collectors.toList());
        List<Integer> currentEmojiHeatList = Arrays.stream(network.getEmojiHeatList()).boxed().collect(Collectors.toList());
        List<MessageInterface> currentMessages = Arrays.asList(network.getMessages());

        // 验证 emojiIdList 和 emojiHeatList 是否仅保留热度≥limit 的条目
        assertRightRemaining(remainingEmojiIds, result, oldEmojiIdList, oldEmojiHeatList, currentEmojiIdList, currentEmojiHeatList);
        //验证消息是否被正确保留或删除。
        assertTrue(verifyMessageDeletion(oldMessages, currentMessages, currentEmojiIdList));
        // 确保 messages 中的消息对象未被篡改
        assertTrue(verifyMessageUnchanged(oldMessages, currentMessages));
    }
}
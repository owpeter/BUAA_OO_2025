import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Message implements MessageInterface {
    private final int id;
    private final int socialValue;
    private final int type;
    private final PersonInterface person1;
    private final PersonInterface person2;
    private final TagInterface tag;

    public Message(int messageId, int messageSocialValue, PersonInterface messagePerson1,
                     PersonInterface messagePerson2) {
        this.type = 0;
        this.tag = null;
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.person1 = messagePerson1;
        this.person2 = messagePerson2;
    }

    public Message(int messageId, int messageSocialValue, PersonInterface messagePerson1, TagInterface messageTag) {
        this.type = 1;
        this.tag = messageTag;
        this.id = messageId;
        this.socialValue = messageSocialValue;
        this.person1 = messagePerson1;
        this.person2 = null;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public PersonInterface getPerson1() {
        return person1;
    }

    @Override
    public PersonInterface getPerson2() {
        return person2;
    }

    @Override
    public TagInterface getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Message)) {
            return false;
        }
        return this.id == ((Message) obj).getId();
    }
}
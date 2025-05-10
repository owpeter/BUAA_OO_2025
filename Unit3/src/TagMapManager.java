import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class TagMapManager {

    private Map<Integer, Set<Tag>> tagsMapById;

    public TagMapManager() {
        this.tagsMapById = new HashMap<>();
    }

    public void addTag(Tag tag) {
        if (tag == null) {
            return;
        }
        this.tagsMapById.computeIfAbsent(tag.getId(), k -> new HashSet<>()).add(tag);
    }

    /**
     * 从数据结构中删除一个特定的 Tag 对象。
     *
     * @param tagToDelete 要删除的 Tag 对象。
     * @return 如果成功删除则返回 true，否则返回 false (例如，Tag 不存在)。
     */
    public boolean deleteTag(Tag tagToDelete) {
        if (tagToDelete == null) {
            return false;
        }
        int tagId = tagToDelete.getId();
        Set<Tag> tagsWithSameId = tagsMapById.get(tagId);
        if (tagsWithSameId == null) {
            return false; // 没有这个id对应的集合，自然也无法删除其中的tag
        }

        boolean removedFromSet = tagsWithSameId.remove(tagToDelete);

        if (removedFromSet) {
            if (tagsWithSameId.isEmpty()) {
                tagsMapById.remove(tagId);
            }
            return true;
        } else {
            return false; // Tag 不在对应的 Set 中
        }
    }

    /**
     * 遍历该类管理的所有 Tag 对象，并对每个 Tag 对象执行指定的操作。
     *
     * @param action 要对每个 Tag 对象执行的操作。如果 action 为 null，则不执行任何操作。
     */
    public void forEachTag(Consumer<Tag> action) {
        if (action == null) {
            return;
        }
        // 遍历 Map 中的所有 Set<Tag>
        for (Set<Tag> tagSet : this.tagsMapById.values()) {
            // 遍历 Set 中的每个 Tag
            for (Tag tag : tagSet) {
                action.accept(tag); // 对每个 Tag 执行指定的操作
            }
        }
    }
}

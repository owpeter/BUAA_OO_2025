import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 一个自定义的 LinkedHashMap 实现，它使用 HashMap 来存储键值对，
 * 并通过双向链表来维护元素的插入顺序或访问顺序。
 *
 * @param <K> 键的类型
 * @param <V> 值的类型
 */
public class CustomLinkedHashMap<K, V> {

    /**
     * 内部节点类，用于存储键值对以及在双向链表中的前后指针。
     */
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * 用于封装 recentNodes 方法返回的节点信息。
     */
    public static class NodeInfo<K, V> {
        public final K key;
        public final V value;

        public NodeInfo(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "NodeInfo{key=" + key + ", value=" + value + "}";
        }
    }

    private final Map<K, Node<K, V>> map;
    private final Node<K, V> head; // 哨兵头节点
    private final Node<K, V> tail; // 哨兵尾节点

    public CustomLinkedHashMap() {
        this.map = new HashMap<>();
        // 初始化哨兵头尾节点，简化边界条件处理
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        this.head.next = this.tail;
        this.tail.prev = this.head;
    }

    /**
     * 将节点添加到双向链表的头部（最近使用的位置）。
     * @param node 要添加的节点
     */
    private void addNodeToFront(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * 从双向链表中移除指定的节点。
     * @param node 要移除的节点
     */
    private void removeNode(Node<K, V> node) {
        Node<K, V> prevNode = node.prev;
        Node<K, V> nextNode = node.next;
        prevNode.next = nextNode;
        nextNode.prev = prevNode;
    }

    /**
     * 将指定的节点移动到双向链表的头部。
     * @param node 要移动的节点
     */
    private void moveToFront(Node<K, V> node) {
        removeNode(node);
        addNodeToFront(node);
    }

    /**
     * 向映射中添加一个键值对。
     * 如果键已存在，则更新其值，并将该节点移动到链表头部（标记为最近使用）。
     * 如果键不存在，则创建一个新节点，添加到映射和链表头部。
     *
     * @param key 键
     * @param value 值
     */
    public void put(K key, V value) {
        if (map.containsKey(key)) {
            Node<K, V> node = map.get(key);
            node.value = value; // 更新值
            moveToFront(node);  // 标记为最近使用/访问
        } else {
            Node<K, V> newNode = new Node<>(key, value);
            map.put(key, newNode);
            addNodeToFront(newNode);
        }
    }

    /**
     * 获取与指定键关联的值。
     * 如果找到键，则将对应节点移动到链表头部（标记为最近使用），并返回值。
     * 如果未找到键，则返回 null。
     *
     * @param key 键
     * @return 与键关联的值，如果键不存在则返回 null
     */
    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }
        moveToFront(node); // 标记为最近使用/访问
        return node.value;
    }

    /**
     * 从映射中移除指定的键及其关联的值。
     * 如果找到键，则从映射和链表中移除对应节点，并返回其值。
     * 如果未找到键，则返回 null。
     *
     * @param key 要移除的键
     * @return 被移除的值，如果键不存在则返回 null
     */
    public V remove(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }
        removeNode(node);
        map.remove(key);
        return node.value;
    }

    /**
     * 返回链表中的前5个节点的信息（键和值）。
     * 如果链表中的节点总数少于5个，则返回所有节点的信息。
     * 节点按最近使用/插入的顺序排列（最新的在前）。
     *
     * @return 包含最近节点信息的列表 (NodeInfo 对象)
     */
    public List<NodeInfo<K, V>> recentNodes() {
        List<NodeInfo<K, V>> recent = new ArrayList<>();
        Node<K, V> current = head.next; // 从实际的第一个数据节点开始
        int count = 0;
        // 遍历直到到达哨兵尾节点或已收集到5个节点
        while (current != tail && count < 5) {
            recent.add(new NodeInfo<>(current.key, current.value));
            current = current.next;
            count++;
        }
        return recent;
    }

    /**
     * 返回映射中的键值对数量。
     * @return 映射的大小
     */
    public int size() {
        return map.size();
    }

    /**
     * (辅助方法，用于调试) 打印当前链表中节点的顺序。
     */
    public void displayOrder() {
        Node<K,V> current = head.next;
        System.out.print("Current Order: ");
        List<String> elements = new ArrayList<>();
        while(current != tail) {
            elements.add("(" + current.key + ":" + current.value + ")");
            current = current.next;
        }
        System.out.println(String.join(" -> ", elements));
    }
}
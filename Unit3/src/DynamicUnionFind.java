import java.util.HashMap;
import java.util.Map;

public class DynamicUnionFind {
    private Map<Integer, Integer> parent;  // 存储每个元素的父节点
    private Map<Integer, Integer> rank;    // 存储每个元素的秩
    private int count;                    // 连通分量数量

    public DynamicUnionFind() {
        parent = new HashMap<>();
        rank = new HashMap<>();
        count = 0;
    }

    public void add(int x) {
        if (!parent.containsKey(x)) {
            parent.put(x, x);  // 初始时父节点是自己
            rank.put(x, 0);    // 初始秩为0
            count++;           // 新增一个连通分量
        }
    }

    public int find(int x) {
        if (parent.get(x) != x) {
            parent.put(x, find(parent.get(x)));
        }
        return parent.get(x);
    }

    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);

        if (rootX == rootY) {
            return;  // 已经在同一集合中
        }

        // 按秩合并
        if (rank.get(rootX) < rank.get(rootY)) {
            parent.put(rootX, rootY);
        } else if (rank.get(rootX) > rank.get(rootY)) {
            parent.put(rootY, rootX);
        } else {
            parent.put(rootY, rootX);
            rank.put(rootX, rank.get(rootX) + 1);  // 秩相同时，合并后秩增加
        }

        count--;  // 连通分量减少
    }

    public boolean isConnected(int x, int y) {
        return find(x) == find(y);
    }

    public int count() {
        return count;
    }

    public boolean contains(int x) {
        return parent.containsKey(x);
    }

    public int size() {
        return parent.size();
    }
}
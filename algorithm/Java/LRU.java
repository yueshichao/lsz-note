import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 参考：https://github.com/CyC2018/CS-Notes/blob/master/notes/%E7%BC%93%E5%AD%98.md#%E4%B8%83lru
 * 双向链表 + Map 实现LRU
 * 访问某结点时，就将其置于链表头部
 * 这样尾部元素就是最近最久未使用的
 * 每次要淘汰缓存时，直接删除尾元素即可
 * 借助Map，可以直接拿到Node，get()和put()时间复杂度都是O(1)
 */
public class LRU<K, V> implements Iterable<String> {

    Node head;
    Node tail;
    Map<K, Node> map;
    int maxSize;

    class Node {
        Node pre;
        Node next;
        K k;
        V v;

        public Node(K k, V v) {
            this.k = k;
            this.v = v;
        }
    }

    public LRU(int maxSize) {
        this.maxSize = maxSize;
        map = new HashMap<>();
        head = new Node(null, null);
        tail = new Node(null, null);
        head.next = tail;
        tail.pre = head;
    }

    public V get(K k) {
        if (!map.containsKey(k)) return null;

        // 缓存命中，将该Node从原先位置删除，移至表头
        Node node = map.get(k);
        unlink(node);
        appendToHead(node);

        return node.v;
    }

    public void put(K k, V v) {
        if (map.containsKey(k)) {
            unlink(map.get(k));
        }
        // 移至表头
        Node node = new Node(k, v);
        map.put(k, node);
        appendToHead(node);
        // 超过容量，删除末尾元素
        if (map.size() > maxSize) {
            Node pre = tail.pre;
            tail.pre = pre.pre;
            pre.pre.next = tail;
            map.remove(pre.k);
        }
    }

    // 移至表头
    private void appendToHead(Node node) {
        Node preHead = head.next;
        head.next = node;
        node.pre = head;
        node.next = preHead;
        preHead.pre = node;
    }

    // 从原先位置删除
    private void unlink(Node node) {
        Node pre = node.pre;
        Node next = node.next;
        pre.next = next;
        next.pre = pre;
        node.pre = node.next = null;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            Node cur = head.next;
            @Override
            public boolean hasNext() {
                return cur != tail;
            }

            @Override
            public String next() {
                Node node = cur;
                cur = cur.next;
                return "(" + node.k.toString() + ", " + node.v.toString() + ")";
            }
        };
    }

}

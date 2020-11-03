package com.lsz;

import java.util.HashMap;
import java.util.Map;

public class UnionFindSetTest {

    interface IUnionFindSet<V> {
        // 定义连接
        void makeSet(V v, V father);

        // 合并集合
        void union(V v1, V v2);

        // 判断是否在同一集合上
        boolean find(V v1, V v2);
    }

    public static void main(String[] args) {
        IUnionFindSet<Integer> uf = new UnionFindSetImpl<>();
        uf.makeSet(2, 1);
        uf.makeSet(3, 1);
        uf.makeSet(1, 1);

        uf.makeSet(5, 4);
        uf.makeSet(6, 4);

        System.out.println("uf.find(1, 2) = " + uf.find(1, 2));
        System.out.println("uf.find(1, 6) = " + uf.find(2, 6));

        uf.union(2, 6);
        System.out.println("uf.find(1, 6) = " + uf.find(2, 6));
    }

    static class UnionFindSetImpl<V> implements IUnionFindSet<V> {

        private Map<V, V> map = new HashMap<>();

        @Override
        public void makeSet(V v, V father) {
            if (map.containsKey(v)) return;
            map.put(v, father);
        }

        @Override
        public void union(V v1, V v2) {
            if (find(v1, v2)) return;
            V father1 = findFather(v1);
            V father2 = findFather(v2);
            map.put(father1, father2);
        }

        @Override
        public boolean find(V v1, V v2) {
            return findFather(v1) == findFather(v2);
        }

        private V findFather(V v) {
            V cur = v;
            while (cur != map.get(cur)) {
                cur = map.get(cur);
            }
            return cur;
        }
    }

}

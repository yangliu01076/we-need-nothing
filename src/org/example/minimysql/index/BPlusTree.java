package org.example.minimysql.index;

import java.util.*;

/**
 * @author duoyian
 * @date 2026/4/8
 */
public class BPlusTree {
    private Node root;
    private final int order;

    public BPlusTree(int order) {
        this.order = order;
        this.root = new LeafNode();
    }

    public void insert(long key, int rowIndex) {
        root.insert(key, rowIndex);
        if (root.isOverflow()) {
            InternalNode newRoot = new InternalNode();
            newRoot.children.add(root);
            root.split(newRoot, 0);
            root = newRoot;
        }
    }

    public Integer search(long key) {
        return (Integer) root.search(key);
    }

    // ================= 节点定义 =================
    abstract static class Node {
        List<Long> keys = new ArrayList<>();
        abstract Object search(long key);
        abstract void insert(long key, int rowIndex);
        abstract void split(Node parent, int index);
        abstract boolean isOverflow();
    }

    class InternalNode extends Node {
        List<Node> children = new ArrayList<>();

        @Override
        Object search(long key) {
            int idx = 0;
            while (idx < keys.size() && key >= keys.get(idx)) idx++;
            return children.get(idx).search(key);
        }

        @Override
        void insert(long key, int rowIndex) {
            int idx = 0;
            while (idx < keys.size() && key >= keys.get(idx)) idx++;
            children.get(idx).insert(key, rowIndex);
            if (children.get(idx).isOverflow()) {
                children.get(idx).split(this, idx);
            }
        }

        @Override
        void split(Node parent, int index) {
            InternalNode newNode = new InternalNode();
            int mid = keys.size() / 2;
            long upKey = keys.get(mid);

            for (int i = mid + 1; i < keys.size(); i++) newNode.keys.add(keys.get(i));
            for (int i = mid + 1; i < children.size(); i++) newNode.children.add(children.get(i));

            keys.subList(mid, keys.size()).clear();
            children.subList(mid + 1, children.size()).clear();

            parent.keys.add(index, upKey);
            ((InternalNode)parent).children.add(index + 1, newNode);
        }

        @Override
        boolean isOverflow() {
            return children.size() > order;
        }
    }

    class LeafNode extends Node {
        List<Integer> values = new ArrayList<>();

        @Override
        Object search(long key) {
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i).equals(key)) return values.get(i);
            }
            return null;
        }

        @Override
        void insert(long key, int rowIndex) {
            int i = 0;
            while (i < keys.size() && keys.get(i) < key) i++;
            keys.add(i, key);
            values.add(i, rowIndex);
        }

        @Override
        void split(Node parent, int index) {
            LeafNode newNode = new LeafNode();
            int mid = keys.size() / 2;

            for (int i = mid; i < keys.size(); i++) {
                newNode.keys.add(keys.get(i));
                newNode.values.add(values.get(i));
            }
            keys.subList(mid, keys.size()).clear();
            values.subList(mid, values.size()).clear();

            long upKey = newNode.keys.get(0);
            parent.keys.add(index, upKey);
            ((InternalNode)parent).children.add(index + 1, newNode);
        }

        @Override
        boolean isOverflow() {
            return values.size() > order;
        }
    }
}

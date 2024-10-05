package bstmap;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class BSTMap<K extends Comparable<K>,V> implements Map61B<K, V>{

    int size=0;

    private class Node{
        K key;
        V value;
        Node left;
        Node right;

        Node(){
            left = null;
            right = null;
        }
        Node(K key, V value){
            this();
            this.key = key;
            this.value = value;
        }
    }

    private Node root;

    @Override
    public void clear() {
        removeall(root);
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return get(root, key) != null;
    }

    @Override
    public V get(K key) {
        Node t = get(root, key);
        return t==null?null:t.value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
        size++;
    }

    @Override
    public Set<K> keySet() {
        TreeSet<K> keyset = new TreeSet<>();
        _keysets(root, keyset);
        return keyset;
    }

    @Override
    public V remove(K key) {
        return _remove(key, null, false);
    }

    @Override
    public V remove(K key, V value) {
        return _remove(key, value, true);
    }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<>() {
            final Iterator<K> it = keySet().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public K next() {
                return it.next();
            }
        };
    }

    private V _remove(K key, V value, boolean flag){
        Node t = get(root, key);
        if(t==null) return null;
        if(flag && !t.value.equals(value)){
            return null;
        }

        root = _remove(root, key);
        size--;
        return t.value;
    }
    private Node _remove(Node root, K key){
        if(root==null) return null;
        int cmp = key.compareTo(root.key);
        if(cmp<0){
            root.left = _remove(root.left, key);
        }else if(cmp>0){
            root.right = _remove(root.right, key);
        }else{
            if(root.right == null){
                return root.left;
            }
            Node t = _min(root.right);

            t.right = _deleteMin(root.right);
            t.left = root.left;

            return t;
        }
        return root;
    }

    private Node _min(Node root){
        if(root == null) return null;
        if(root.left !=  null) return _min(root.left);
        else return root;
    }
    private Node _deleteMin(Node root){
         if(root==null) return null;
         if(root.left==null){
             return root.right;
         }
         root.left = _deleteMin(root.left);
         return root;
    }


    private void _keysets(Node root, TreeSet<K> set){
        if(root == null) return ;
        _keysets(root.left, set);
        set.add(root.key);
        _keysets(root.right, set);
    }

    private Node put(Node root, K key, V value){
        if(root == null){
            return new Node(key, value);
        }
        int cmp = key.compareTo(root.key);
        if (cmp < 0) {
            root.left = put(root.left, key, value);
        } else if (cmp > 0) {
            root.right = put(root.right, key, value);
        } else {
            root.value = value;
        }
        return root;
    }

    private Node get(Node root, K key){
        if(root == null) return null;
        int cmp = key.compareTo(root.key);
        if(cmp<0){
            return get(root.left, key);
        }else if(cmp>0){
            return get(root.right, key);
        }else{
            return root;
        }
    }
    private void removeall(Node root){
        if(root == null) return;
        removeall(root.left);
        removeall(root.right);
        root.left = null;
        root.right = null;
    }
    protected void printInOrder(){
       this.forEach(System.out::println);
    }
}

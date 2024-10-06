package hashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private double load_factor;
    private int size=0;
    private HashSet<K> keySet = new HashSet<>();
    private static final int MOD = 65537;

    /** Constructors */
    public MyHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = new Collection[initialSize];
        load_factor = maxLoad;

        for (int i = 0; i < initialSize; i++) {
            buckets[i] = createBucket();
        }
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new HashSet<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return null;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    @Override
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i].clear();
        }
        keySet.clear();
        size=0;
    }

    @Override
    public boolean containsKey(K key) {
        return null != _get(key);
    }

    @Override
    public V get(K key) {
        Node t = _get(key);
        if(t == null) return null;
        return t.value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        Node t = _get(key);
        if(null != t) {
            t.value = value;
            return ;
        }

        if((size+1)/(double) buckets.length > load_factor){
            _resize();
        }

        int ind = _computeIndex(key);
        Node node = createNode(key, value);
        buckets[ind].add(node);
        keySet.add(key);
        size++;
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public V remove(K key) {
        int ind = _computeIndex(key);
        Node t=null;
        for(Node n:buckets[ind]){
            if(n.key.equals(key)){
                t = n;
                break;
            }
        }
        if(t == null) return null;

        buckets[ind].remove(t);
        keySet.remove(key);
        size--;
        return t.value;
    }

    @Override
    public V remove(K key, V value) {
        Node t = _get(key);
        if(t == null) return null;
        return remove(key);
    }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            Iterator<K> it = keySet.iterator();
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

    private void _resize(){
        HashSet<Node> s = new HashSet<>();
        for (Collection<Node> bucket:buckets){
            s.addAll(bucket);
        }

        buckets = new Collection[buckets.length*2];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = createBucket();
        }
        for(Node node:s){
            int ind = _computeIndex(node.key);
            buckets[ind].add(node);
        }
    }

    private Node _get(K key){
        int ind = _computeIndex(key);
        for (Node n : buckets[ind]) {
            if (n.key.equals(key))
                return n;
        }
        return null;
    }

    private int _computeIndex(K key){
        int hash = key.hashCode();
        return ((hash%MOD + MOD) % MOD)%(buckets.length);
    }

    private static final int DEFAULT_INITIAL_SIZE = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;

}

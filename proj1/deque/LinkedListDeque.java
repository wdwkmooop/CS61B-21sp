package deque;


import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>{
    private ListNode<T> head, tail;
    private int size=0;

    public LinkedListDeque() {
        head = null;
        tail = null;
    }

    @Override
    public void addFirst(T item) {
        ListNode<T> n = new ListNode<>(item);
        if(head != null){
            n.next = head;
            head.prev = n;
            head = n;
        }else{
            head = n;
            tail = n;
        }
        size++;
    }

    @Override
    public void addLast(T item) {
        ListNode<T> n = new ListNode<>(item);
        if(tail != null){
            tail.next = n;
            n.prev = tail;
            tail = n;
        }else{
            head = n;
            tail = n;
        }
        size++;
    }

    @Override
    public boolean isEmpty() {
        return size==0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        ListNode<T> it = head;
        StringBuilder sb = new StringBuilder();
        while(it!=null){
            sb.append(it.val);
            sb.append(' ');
            it = it.next;
        }
        System.out.println(sb);
    }

    @Override
    public T removeFirst() {
        if(head == null)
            return null;
        ListNode<T> ret = head;
        if(head == tail){
            head = null;
            tail = null;
        }else{
            head = head.next;
            head.prev = null;
        }
        size--;
        return ret.val;
    }

    @Override
    public T removeLast() {
        if(tail == null){
            return null;
        }
        ListNode<T> ret =tail;
        if(head == tail){
            tail = null;
            head = null;
        }else{
            tail = tail.prev;
            tail.next = null;
        }
        size--;
        return ret.val;
    }

    @Override
    public T get(int index) {
        if(index >= size){
            return null;
        }
        ListNode<T> it=head;
        for(int i=0;i<index;i++){
            it = it.next;
        }
        return it.val;
    }

    @Override
    public Iterator<T> iterator() {
        return new NodeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o==null || getClass()!=o.getClass()) return false;
        LinkedListDeque<T> that = (LinkedListDeque<T>) o;
        if(size != that.size()) return false;

        Iterator<T> it1 = iterator();
        Iterator<T> it2 = that.iterator();

        while (it1.hasNext()) {
            if(!it1.next().equals(it2.next())){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = head!=null ? head.hashCode():0;
        result = 31 * result + (tail!=null ? tail.hashCode():0);
        result = 31 * result + size;
        return result;
    }

    private class NodeIterator implements Iterator<T> {
        ListNode<T> cur = LinkedListDeque.this.head;
        @Override
        public boolean hasNext() {
            return cur != null;
        }

        @Override
        public T next() {
            T ret = cur.val;
            cur = cur.next;
            return ret;
        }
    }


    private static class ListNode<R>{
        R val;
        ListNode<R> next;
        ListNode<R> prev;
        public ListNode(R val){
            this.val = val;
            this.next=null;
            this.prev=null;
        }
        public ListNode(R val, ListNode<R> next, ListNode<R> prev) {
            this.val = val;
            this.next = next;
            this.prev = prev;
        }
    }
}

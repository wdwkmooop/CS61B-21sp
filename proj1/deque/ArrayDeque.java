package deque;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>{
    private T[] data;
    private int size;
    private int head;
    private int tail;
    private int capability;

    private static final int initSize = 8;


    public ArrayDeque(){
        size = 0;
        data = (T[]) new Object[initSize];
        capability = initSize;
        head = 0;
        tail = 0;
    }

    @Override
    public void addFirst(T item) {
        if(needExtend()){
            resize(capability*2);
        }
        head = (head-1+capability)% capability;
        data[head] = item;
        size++;
    }

    @Override
    public void addLast(T item) {
        if(needExtend()){
            resize(capability*2);
        }
        data[tail] = item;
        tail = (tail+1) % capability;
        size++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Iterator<T> it = iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()){
            sb.append(it.next());
            sb.append(" ");
        }
        System.out.println(sb);
    }

    @Override
    public T removeFirst() {
        if(size == 0) return null;
        if(needSubstract()){
            resize(capability/2);
        }
        size--;
        T ret = data[head];
        data[head] = null;
        head = (1+head) % capability;
        return ret;
    }

    @Override
    public T removeLast() {
        if(size == 0) return null;
        if(needSubstract()){
            resize(capability/2);
        }
        size--;
        tail = (tail-1+capability) % capability;
        T ret = data[tail];
        data[tail] = null;

        return ret;
    }

    @Override
    public T get(int index) {
        if(index >= size) return null;
        return data[(head + index) % capability];
    }

    @Override
    public Iterator<T> iterator() {
        return new mIterator();
    }


    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o==null || getClass()!=o.getClass()) return false;

        ArrayDeque<T> that = (ArrayDeque<T>) o;

        if (size!=that.size) return false;
        if (head!=that.head) return false;
        if (tail!=that.tail) return false;
        if (capability!=that.capability) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        Iterator<T> it1 = iterator();
        Iterator<T> it2 = that.iterator();
        while(it1.hasNext()){
            if(!it1.next().equals(it2.next())) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(data);
        result = 31 * result + size;
        result = 31 * result + head;
        result = 31 * result + tail;
        result = 31 * result + capability;
        return result;
    }

    private boolean needExtend(){
        // 满了
        return (tail+1) % capability == head ;
    }

    private boolean needSubstract(){
        return size < capability/4 && capability > 16;
    }

    private void resize(int newCapability){
        T[] newData = (T[]) new Object[newCapability];
        for(int i=0;i<size;i++){
            newData[i] = data[(head+i) % capability];
        }
        data = newData;
        capability = newCapability;
        head=0;
        tail=size;
    }


    private class mIterator implements Iterator<T>{
        int index = head;

        @Override
        public boolean hasNext() {
            return index%capability != tail;
        }

        @Override
        public T next() {
            return data[index++ % capability];
        }
    }
}

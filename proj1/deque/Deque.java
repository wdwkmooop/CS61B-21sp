package deque;

import java.util.Iterator;

public interface Deque <T> {
    public void addFirst(T item);
    public void addLast(T item);
    public default boolean isEmpty(){
        return size() == 0;
    }
    public int size();
    public void printDeque();
    public T removeFirst();
    public T removeLast();
    public T get(int index);//：获取给定索引处的项目，其中 0 是第一个，1 是下一个项目，依此类推。如果不存在这样的项目，则返回null。不得更改双端队列！
    public Iterator<T> iterator();
}

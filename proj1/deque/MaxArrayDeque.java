package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{

    Comparator<T> cmp;

    public MaxArrayDeque(Comparator<T> c){
        cmp = c;
    }
    public T max(){
        T ret=null;
        Iterator<T> it = iterator();
        while(it.hasNext()){
            T b = it.next();
            if(ret==null || cmp.compare(ret, b)<0){
                ret = b;
            }
        }
        return ret;
    }
    public T max(Comparator<T> c){
        T ret=null;
        Iterator<T> it = iterator();
        while(it.hasNext()){
            T b = it.next();
            if(ret==null || c.compare(ret, b)<0){
                ret = b;
            }
        }
        return ret;
    }

}

package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Comparator;

public class MaxArrayDequeTest {

    @Test
    public void maxTest(){
        int M = -1;
        MaxArrayDeque<Integer> l = new MaxArrayDeque<>(Comparator.comparingInt(integer -> integer));
        for(int i=0;i<1000;i++){
            int x = StdRandom.uniform(0, 500);
            M = Math.max(M, x);
            l.addFirst(x);
            int m = l.max();
            assertEquals(M, m);
        }
    }



}

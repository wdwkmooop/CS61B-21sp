package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    public void randomTest(){
        int N = 100000;

        ArrayDeque<Integer> myQue = new ArrayDeque<>();
        java.util.ArrayDeque<Integer> stdQue = new java.util.ArrayDeque<>();

        for(int i=0;i<N;i++) {
            int op = StdRandom.uniform(0, 4);
            switch (op) {
                case 0:
                    assertEquals(stdQue.pollFirst(), myQue.removeFirst());
                    System.out.println("del first");
                    break;
                case 1:
                    assertEquals(stdQue.pollLast(), myQue.removeLast());
                    System.out.println("del last");
                    break;
                case 2:
                    int x = StdRandom.uniform(100);
                    System.out.println("add first " + x);
                    stdQue.addFirst(x);
                    myQue.addFirst(x);
                    assertEquals(stdQue.size(), myQue.size());
                    break;
                case 3:
                    x = StdRandom.uniform(100);
                    System.out.println("add last " + x);
                    stdQue.addLast(x);
                    myQue.addLast(x);
                    assertEquals(stdQue.size(), myQue.size());
                    break;
            }

        }
    }

    @Test
    public void equalTest(){
        ArrayDeque<String> l1 = new ArrayDeque<>();
        ArrayDeque<String> l2 = new ArrayDeque<>();
            l1.addFirst("abc");
            l2.addFirst("abc");
        l1.addFirst("hha");
        l2.addFirst("hha");
        assertEquals(l1, l2);

        l1.addLast("cnm");
        l2.addLast("fuck");
        assertNotEquals(l1, l2);

    }
}

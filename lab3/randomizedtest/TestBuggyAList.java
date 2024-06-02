package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove(){
        AListNoResizing<Integer> goodList = new AListNoResizing<>();
        BuggyAList<Integer> badList = new BuggyAList<>();
        for(int i=0;i<3;i++){
                goodList.addLast(i);
                badList.addLast(i);
        }
        for(int i=0;i<3;i++){
            int a =goodList.removeLast();
            int b = badList.removeLast();
            assertEquals(a, b);
        }

    }

    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> buggyAList = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                buggyAList.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                assertEquals(size, buggyAList.size());
                System.out.println("size: " + size);
            } else if (L.size()!=0 && operationNumber == 2){
                int lastVal = L.removeLast();
                assertEquals("last val not correct!", lastVal, (int)buggyAList.removeLast());
                assertEquals("size not correct!", L.size(), buggyAList.size());
                System.out.printf("removed(%d), size %d -> %d \n", lastVal, L.size()+1, L.size());
            } else if (L.size()!=0 && operationNumber == 3 ){
                int lastVal = L.getLast();
                assertEquals("last val not correct!", lastVal, (int)buggyAList.getLast());
                assertEquals("unexpectedly modified the size!", buggyAList.size(), L.size());
                System.out.printf("get last is %d \n", lastVal);
            }
        }
    }
}

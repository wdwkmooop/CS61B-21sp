package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        int[] a = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000};
        int M = 10000;
        int[] b = {10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000};
        AList<Integer> Ns = new AList<>();
        for(int N:a){
            Ns.addLast(N);
        }
        AList<Double> times = new AList<>();
        AList<Integer> counts = new AList<>();
        for(int cnt:b){
            counts.addLast(cnt);
        }

        for(int N:a){
            SLList<Integer> t = new SLList<>();
            for(int i=0;i<N;i++){
                t.addLast(i);
            }
            Stopwatch sw = new Stopwatch();
            for(int j=0;j<M;j++){
                t.getLast();
            }
            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);
        }

        printTimingTable(Ns, times, counts);
    }

}

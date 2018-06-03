// RV Predict benchmark

public class RVBrokenSpinningLoop {
 
    static int sharedVar;
 
    static boolean condition = false;
 
    public static void main(String[] args) {
        new ThreadRunner(2) {
            @Override
            public void thread1() {
                condition = true;
                sharedVar = 1;
            }
 
            @Override
            public void thread2() {
                while (!condition) {
                    Thread.yield();
                }
                assert(sharedVar == 1);
                System.out.println("Success");
            }
        };
    }
}

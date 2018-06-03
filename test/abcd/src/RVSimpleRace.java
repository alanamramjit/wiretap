public class RVSimpleRace {
 
    static int sharedVar = 0;
 
    public static void main(String[] args) {
        new ThreadRunner(2) {
            @Override
            public void thread1() {
                sharedVar++;
            }
 
            @Override
            public void thread2() {
                sharedVar++;
            }
        };
        assert(sharedVar == 2); 
    }
}
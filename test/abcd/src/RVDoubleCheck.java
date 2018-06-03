public class RVDoubleCheck{
 
    static class Helper {
        Object data;
 
        Helper() {
            data = new Object();
        }
    }
 
    public static void main(String[] args) {
        new ThreadRunner(2) {
 
            private Helper helper;
 
            private Helper getHelper() {
                if (helper == null) {
                    synchronized (this) {
                        assert(helper == null);
                        helper = new Helper();
                    }
                }
                return helper;
            }
 
            @Override
            public void thread1() {
                getHelper();
            }
 
            @Override
            public void thread2() {
                getHelper();
            }
        };
    }
}
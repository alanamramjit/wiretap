import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
 
public class RV_RWLock {
 
    static int sharedVar = 0;
 
    static ReadWriteLock lock = new ReentrantReadWriteLock();
 
    public static void main(String[] args) {
        new ThreadRunner(2) {
            @Override
            public void thread1() {
                lock.readLock().lock();
                sharedVar++;
                lock.readLock().unlock();
            }
 
            @Override
            public void thread2() {
                lock.readLock().lock();
                sharedVar++;
                lock.readLock().unlock();
            }
        };
        assert(sharedVar == 2);
    }
}
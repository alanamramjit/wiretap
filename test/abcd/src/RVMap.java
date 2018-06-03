import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
 
public class RVMap {
 
    static Map<Integer, Integer> map = Collections.synchronizedMap(new HashMap<>());
 
    public static void main(String[] args) {
        new ThreadRunner(2) {
            @Override
            public void thread1() {
                map.remove(0);
                map.put(1, 1);
            }
 
            @Override
            public void thread2() {
                map.put(0, 0);
                Set<Integer> keySet = map.keySet();
                synchronized (keySet) {
                    assert(keySet.contains(0));
                }
            }
        };
    }
}
import java.util.ArrayList;
import java.util.List;
 
public class RVArrayList {
 
    static List<Integer> list = new ArrayList<>();
 
    public static void main(String[] args) {
        new ThreadRunner(2) {
            @Override
            public void thread1() {
                if (list.isEmpty())
                    list.add(0);
            }
 
            @Override
            public void thread2() {
                if (list.isEmpty())
                    list.add(1);
            }
        };
    assert(list.size() == 1);
    }
}

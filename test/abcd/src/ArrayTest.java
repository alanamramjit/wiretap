public class ArrayTest extends Thread{
	static int[] nums = new int[100];

	public void run(){
		for (int i = 0; i < nums.length; i++){
			if(nums[i] == i){
				try{
					Thread.sleep(1);
				}
				catch(InterruptedException e){}
				nums[i] += 1;
			}
		}
	}

	public static void main(String[] args){
		for(int i = 0; i < nums.length; i++){
			nums[i] = i;
		}
		Thread [] stupid = new Thread[10];
		for(int i = 0; i < stupid.length; i++){
			stupid[i] = new ArrayTest();
			stupid[i].start();
		}
		for(Thread t: stupid){
			try{
					t.join();
				}
				catch(InterruptedException e){}
		}

		int sum = 0;
		for (int n: nums) sum +=  n;
		assert(sum == 5050);
	}
}

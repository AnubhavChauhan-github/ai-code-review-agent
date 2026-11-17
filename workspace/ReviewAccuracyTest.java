public class ReviewAccuracyTest{
	
	public int findMax(int [] numbers) {
		
		int max = 0;
		
		for(int number : numbers) {
			if(number > max) {
				max = number;
			}
		}
		
		return max;
	}
	
	public int divide(int a, int b) {
		return a/b;
	}
}
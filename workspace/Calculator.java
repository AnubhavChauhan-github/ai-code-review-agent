public class Calculator{
	
	
	public int divide(int a, int b) {
		return a/b;
	}
	
	public int findMax(int [] numbers) {
		int max = 0;
		
		for(int n : numbers) {
			if(n>max) {
				max = n;
			}
		}
		
		return max;
	}
}
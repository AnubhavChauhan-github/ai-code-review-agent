public class ReviewAccuracyTest{
	
	public int findMax(int [] numbers) {
		
		if(numbers == null || numbers.length == 0){
			
			throw new IllegalArgumentException(
					"Array cannot be null or empty"
					);
		}
		
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
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReviewAccuracyTestTest{
	
	@Test
	void findMaxWithNegativeNumber() {
		ReviewAccuracyTest calculator = new ReviewAccuracyTest();
		
		int[] numbers = {-8,-3,-5};
		
		assertEquals(-3, calculator.findMax(numbers));
	}
	
	@Test
	void findMaxWithPositiveNumbers() {
		ReviewAccuracyTest calculator = new ReviewAccuracyTest();
		
		int[] numbers = {1,2,3,4,5};
		
		assertEquals(5, calculator.findMax(numbers));
	}
	
	
	@Test
	void divideByZeroThrowsException() {
		
		ReviewAccuracyTest calculator = new ReviewAccuracyTest();
		
		assertThrows(ArithmeticException.class, 
				() -> calculator.divide(10,0)
				);
		
	}
}
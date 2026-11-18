package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class JavaCodeAnalyzerTest {
	
	private final JavaCodeAnalyzer analyzer = 
			new JavaCodeAnalyzer();
	
	@Test
	void detectsSuspiciousMaxInitialization() {
		
		
		String source = """
				public class Calculator{
					public int findMax(int [] numbers){
						int max = 0;
						
						for(int number : numbers){
						
							if(number > max){
								max = number;
							}
						}
						
						return max;
					}
				}
				""";
		
		String result = analyzer.analyze(source);
		
		assertTrue(result.contains("Potential correctness bug"));
	}
	
	@Test
	void doesNotFlagFirstElementInitialization() {
		
		String source = """
				public class Calculator{
					public int findMax(int [] numbers){
						
						int max = numbers[0];
						
						for(int number : numbers){
						
							if(number > max){
								max = number;
							}
						}
					}
				}
				
				""";
		
		String result = analyzer.analyze(source);
		
		assertEquals(
				"No issues detected by the current Java rules.", result);
		
	}

}

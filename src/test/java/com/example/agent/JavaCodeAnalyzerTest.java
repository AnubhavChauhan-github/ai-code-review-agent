package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.List;

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
	
	@Test
	void returnStructuredFinding() {
		
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
		
		List<CodeReviewFinding> findings = 
				analyzer.analyzeFindings(source);
		
		assertEquals(1, findings.size());
		
		CodeReviewFinding finding = findings.get(0);
		
		assertEquals("MEDIUM", finding.severity());
		assertEquals("findMax", finding.method());
		
		assertTrue(
				finding.problem().contains("initialized to zero")
				);
		
		
	}
	
	@Test
	void detectsMaxBugAfterInputValidation() {
		
		String source = """
				public class Calculator{
				
					public int findMax(int [] numbers){
					
						if(numbers == null || numbers.length == 0){
							throw new IllegalArgumentException(
								"Array cannot be null or empty"
							);
						}
						
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
		
		List<CodeReviewFinding> findings = 
				analyzer.analyzeFindings(source);
		
		assertEquals(1, findings.size());
		assertEquals("findMax", findings.get(0).method());
	}
	

		@Test
		void ignoresCorrectMaxInitialization() {
		
		    String source = """
		        class Calculator {
		            int findMax(int[] numbers) {
		                int max = numbers[0];
		
		                for (int n : numbers) {
		                    if (n > max) {
		                        max = n;
		                    }
		                }
		                return max;
		            }
		        }
		        """;
		
		    assertTrue(analyzer.analyzeFindings(source).isEmpty());
		}
		
		@Test
		void ignoresZeroInitializationInUnrelatedMethod() {
		
		    String source = """
		        class Calculator {
		            int sum(int[] numbers) {
		                int max = 0;
		
		                for (int n : numbers) {
		                    max += n;
		                }
		                return max;
		            }
		        }
		        """;
		
		    assertTrue(analyzer.analyzeFindings(source).isEmpty());
		}
		
		@Test
		void ignoresMaxWhenLoopUsesDifferentComparison() {
		
		    String source = """
		        class Calculator {
		            int findMax(int[] numbers) {
		                int max = 0;
		
		                for (int n : numbers) {
		                    if (n < max) {
		                        max = n;
		                    }
		                }
		                return max;
		            }
		        }
		        """;
		
		    assertTrue(analyzer.analyzeFindings(source).isEmpty());
		}
		
		@Test
		void handlesBlankAndInvalidJavaSafely() {
		
		    assertTrue(analyzer.analyzeFindings(null).isEmpty());
		
		    assertTrue(analyzer.analyzeFindings("").isEmpty());
		
		    assertTrue(analyzer.analyzeFindings(
		            "class Calculator { int findMax(").isEmpty());
		}

		
		@Test
		void reportsFaultyInitializationLine() {
			
			String source ="""
					class Calculator{
					
						int findMax(int [] numbers){
							int max = 0;
							for(int n : numbers){
								if(n > max){
									max = n;
								}
							}
							return max;
						}
					}
					""";
			
			List<CodeReviewFinding> findings =
					analyzer.analyzeFindings(source);
			
			assertEquals(1, findings.size());
			assertEquals(4, findings.get(0).lineNumber());
		}

}

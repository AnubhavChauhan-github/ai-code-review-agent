package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

public class JUnitTestSuggestionGeneratorTest {
	
	private final JUnitTestSuggestionGenerator generator = 
			new JUnitTestSuggestionGenerator();
	
	
	@Test
	void generatesTestForFindMax() {
		
		CodeReviewFinding finding = new CodeReviewFinding(
				
				"MEDIUM",
				"findMax",
				"Maximum initialized to zero",
				"{-8,-3,-5}",
				"Initialize max using the first element"
				);
		
		String result = generator.generate(
				"ReviewAccuracyTest", 
				List.of(finding)
				);
		
		assertTrue(
				result.contains("class ReviewAccuracyTestTest")
				);
		
		assertTrue(
				result.contains("calculator.findMax(numbers)")
				);
		assertTrue(
				result.contains("assertEquals")
				);
		
	}
	
	@Test
	void rejectsInvalidClassName() {
		
		assertThrows(
				IllegalArgumentException.class,
				() -> generator.generate(
						"../OtherClass", List.of()
						)
			);
	}
	
	@Test
	void handlesNoKnownFindings() {
		
		String result = generator.generate(
				"Calculator", List.of()
				);
		
		assertEquals(
				"No test template available for these findings.", 
				result
				);
		}
	
	

}

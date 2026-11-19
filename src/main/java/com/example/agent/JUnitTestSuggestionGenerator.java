package com.example.agent;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class JUnitTestSuggestionGenerator {
	
	public String generate(
			String className, 
			List<CodeReviewFinding> findings) {
		
		
		if(className == null || 
				!className.matches(
						"[A-Za-z_$][A-Za-z0-9_$]*")) {
			
			throw new IllegalArgumentException(
					"Invalid Java class name"
					);
		}
			
			boolean hasMaxFinding = findings.stream()
					.anyMatch(f ->
							"findMax".equals(f.method())
					);
			
			if(!hasMaxFinding) {
				return "No test template available for these findings.";
			}
			
			return """
					import static org.junit.jupiter.api.Assertions.assertEquals;
					import org.junit.jupiter.api.Test;
					
					class %sTest{
					
						@Test
						void findMaxWithNegativeNumbers(){
						
							%s calculator = new %s();
							
							int [] numbers = {-8,-3,-5};
							
							assertEquals(-3, calculator.findMax(numbers));
						}
					
					}
					
					
					""".formatted(className, className, className);
			
		}
}
	
	


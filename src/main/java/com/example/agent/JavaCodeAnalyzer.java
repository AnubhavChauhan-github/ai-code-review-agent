package com.example.agent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class JavaCodeAnalyzer {
	
	
	private static final Pattern MAX_PATTERN = 
			Pattern.compile(
					"\\bint\\s+findMax\\s*\\(" +
					"\\s*int\\s*\\[\\s*\\]\\s+\\w+\\s*\\)" +
					"\\s*\\{\\s*int\\s+max\\s*=\\s*0\\s*;"
			);
	
	public String analyze(String sourceCode) {
		
		if(sourceCode == null || sourceCode.isBlank()) {
			return "No Java source code provided";
		}
		
		StringBuilder findings = new StringBuilder();
		
		Matcher matcher = MAX_PATTERN.matcher(sourceCode);
		
		if(matcher.find()) {
			findings.append("""
					
					Potential correctness bug detected.
					
					Method: findMax
					Severity: MEDIUM
					
					Problem:
					The maximum value is initialized to zero.
					
					Example:
					Input: {-8,-3,-5}
					
					If the method only updates max when a 
					number is greater than max, it will
					incorrectly return 0 instead of -3.
					
					Suggested fix:
					For non-empty arrays, initialize max
					using numbers[0], or use a suitable
					alternative initialization.
					
					Also define how null and empty arrays
					should be handled.
					
					This is a heuristic finding.
					Verify against the complete method.
					""");
		}
		
		if(findings.isEmpty()) {
			return "No issues detected by the current Java rules.";
		}
		
		return findings.toString();
	}
	

}

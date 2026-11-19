package com.example.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class JavaCodeAnalyzer {
	
	
	private static final Pattern MAX_PATTERN = 
			Pattern.compile(
					"\\bint\\s+findMax\\s*\\(" +
					  "\\s*int\\s*\\[\\s*\\]\\s+\\w+\\s*\\)" +
					    "\\s*\\{\\s*" +
					      "(?:if\\s*\\([^{}]*\\)\\s*\\{[^{}]*\\}\\s*)?" +
					       "int\\s+max\\s*=\\s*0\\s*;"
			);
	
	public List<CodeReviewFinding> analyzeFindings(String sourceCode){
		
		List<CodeReviewFinding> findings = 
				new ArrayList<>();
		
		if(sourceCode == null || sourceCode.isBlank()) {
			return findings;
		}
		
		Matcher matcher = MAX_PATTERN.matcher(sourceCode);
		
		if(matcher.find()) {
			
			CodeReviewFinding finding = new CodeReviewFinding(
					
					"MEDIUM",
					"findMax",
					"Maximun value is initialized to zero. "
					+ "This can return an incorrect result "
					+ "for arrays containing only negative "
					+ "numbers when max is updated only "
					+ "for greater elements.",
					"{-8,-3,-5}: expected -3, "
					+ "but the common faulty loop returns 0.",
					"For non-emoty arrays, initialize max "
					+ "with numbers[0]. Define behavior "
					+ "for null and empty arrays. "
					);
			findings.add(finding);
		}
		
		return findings;
	}
	
	public String analyze(String sourceCode) {
		
		List<CodeReviewFinding> findings = analyzeFindings(sourceCode);
		
		if(findings.isEmpty()) {
			return "No issues detected by the current Java rules.";
		}
		
		StringBuilder result = new StringBuilder();
		
		for(CodeReviewFinding finding : findings) {
			
			result.append("Potential correctness bug detected. \n");
			result.append("Severity: ")
					.append(finding.severity()).append("\n");
			result.append("Method: ")
					.append(finding.method()).append("\n");
			result.append("Problem: ")
					.append(finding.problem()).append("\n");
			result.append("Reproducing input: ")
					.append(finding.reproducingInput()).append("\n");
			result.append("Suggested fix: ")
					.append(finding.suggestedFix()).append("\n");
			result.append("This is a heuristic finding, ")
					.append("not a verified bug. \n");
		}
		
		return result.toString();
	}
	

}

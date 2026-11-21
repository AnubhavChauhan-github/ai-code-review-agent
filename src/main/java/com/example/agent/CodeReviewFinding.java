package com.example.agent;

public record CodeReviewFinding(
		
		String severity,
		String method,
		String problem,
		String reproducingInput,
		String suggestedFix,
		Integer lineNumber
		) {
	
	public CodeReviewFinding(
            String severity,
            String method,
            String problem,
            String reproducingInput,
            String suggestedFix) {

        this(severity, method, problem,
             reproducingInput, suggestedFix, null);
    }

}



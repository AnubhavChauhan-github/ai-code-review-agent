package com.example.agent;

public record CodeReviewFinding(
		
		String severity,
		String method,
		String problem,
		String reproducingInput,
		String suggestedFix
		) {

}

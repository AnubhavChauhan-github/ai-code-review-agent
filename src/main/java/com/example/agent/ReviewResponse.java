
package com.example.agent;

import java.util.List;

public record ReviewResponse(
        String fileName,
        List<CodeReviewFinding> findings,
        List<CodeReviewFinding> riskFindings,
        String aiExplanation,
        String suggestedJUnitTest,
        String verificationStatus,
        List<String> claimWarnings,
        List<CodeReviewFinding> reliabilityFindings
) {
}

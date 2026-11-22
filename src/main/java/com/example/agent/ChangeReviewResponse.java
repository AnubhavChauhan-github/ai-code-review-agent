package com.example.agent;

import java.util.List;

public record ChangeReviewResponse(
        String originalFileName,
        String proposedFileName,
        List<MethodChange> methodChanges,
        List<String> warnings,
        List<CodeReviewFinding> introducedFindings,
        String verificationStatus
) {
}

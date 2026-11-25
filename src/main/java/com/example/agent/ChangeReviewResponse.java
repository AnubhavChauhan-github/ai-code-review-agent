package com.example.agent;

import java.util.List;

public record ChangeReviewResponse(
        String originalFileName,
        String proposedFileName,
        List<MethodChange> methodChanges,
        List<String> warnings,
        List<CodeReviewFinding> introducedFindings,
        String verificationStatus,
        SafetyGateDecision safetyGate
) {

    // Retain the old six-argument constructor.
    // Existing JavaChangeAnalyzer code can remain unchanged.
    public ChangeReviewResponse(
            String originalFileName,
            String proposedFileName,
            List<MethodChange> methodChanges,
            List<String> warnings,
            List<CodeReviewFinding> introducedFindings,
            String verificationStatus) {

        this(
            originalFileName,
            proposedFileName,
            methodChanges,
            warnings,
            introducedFindings,
            verificationStatus,
            new ChangeSafetyGate().evaluate(
                methodChanges,
                warnings,
                introducedFindings
            )
        );
    }
}

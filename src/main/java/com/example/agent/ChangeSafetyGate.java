
package com.example.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChangeSafetyGate {

    public SafetyGateDecision evaluate(
            List<MethodChange> methodChanges,
            List<String> warnings,
            List<CodeReviewFinding> introducedFindings) {

        List<String> reasons = new ArrayList<>();

        boolean blocked = false;
        boolean reviewRequired = false;

        // 1. Unauthorized method changes are blocked.
        for (MethodChange change : methodChanges) {

            if (!change.allowed()) {
                blocked = true;

                reasons.add(
                    "Unauthorized " + change.changeType()
                    + " method: " + change.method()
                );
            }

            if ("REMOVED".equals(change.changeType())) {
                reviewRequired = true;

                reasons.add(
                    "Method removal needs review: "
                    + change.method()
                );
            }
        }

        // 2. Newly introduced security/correctness risks.
        for (CodeReviewFinding finding : introducedFindings) {

            String severity = finding.severity() == null
                    ? "UNKNOWN"
                    : finding.severity()
                        .toUpperCase(Locale.ROOT);

            if (severity.equals("HIGH")
                    || severity.equals("CRITICAL")) {

                blocked = true;

                reasons.add(
                    "Introduced " + severity
                    + " risk in "
                    + finding.method()
                    + ": " + finding.problem()
                );

            } else {
                reviewRequired = true;

                reasons.add(
                    "Introduced " + severity
                    + " finding in "
                    + finding.method()
                    + ": " + finding.problem()
                );
            }
        }

        // 3. Other structural changes require manual review.
        if (!warnings.isEmpty()) {
            reviewRequired = true;

            for (String warning : warnings) {
                reasons.add(
                    "Change warning: " + warning
                );
            }
        }

        // 4. BLOCK takes priority over REVIEW_REQUIRED.
        if (blocked) {
            return new SafetyGateDecision(
                "BLOCK",
                List.copyOf(reasons)
            );
        }

        if (reviewRequired) {
            return new SafetyGateDecision(
                "REVIEW_REQUIRED",
                List.copyOf(reasons)
            );
        }

        return new SafetyGateDecision(
            "PASS",
            List.of(
                "No unauthorized method changes or "
                + "introduced findings detected by "
                + "the configured static analyzers."
            )
        );
    }
}

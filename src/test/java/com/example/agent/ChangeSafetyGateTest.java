package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class ChangeSafetyGateTest {

    private final ChangeSafetyGate gate =
            new ChangeSafetyGate();

    @Test
    void blocksUnauthorizedMethodModification() {

        var changes = List.of(
            new MethodChange(
                "Calculator#divide(int, int)",
                "MODIFIED",
                false
            )
        );

        var decision = gate.evaluate(
            changes,
            List.of(),
            List.of()
        );

        assertEquals("BLOCK", decision.status());

        assertTrue(decision.reasons().stream()
            .anyMatch(r ->
                r.contains("Unauthorized")));
    }

    @Test
    void blocksNewHighSeverityFinding() {

        var finding = new CodeReviewFinding(
            "HIGH",
            "divide",
            "Process termination detected",
            "System.exit(1)",
            "Remove unauthorized termination",
            17
        );

        var decision = gate.evaluate(
            List.of(),
            List.of(),
            List.of(finding)
        );

        assertEquals("BLOCK", decision.status());
    }

    @Test
    void requiresReviewForMediumSeverityFinding() {

        var finding = new CodeReviewFinding(
            "MEDIUM",
            "findMax",
            "Suspicious initialization",
            "int max = 0",
            "Initialize from first element",
            5
        );

        var decision = gate.evaluate(
            List.of(),
            List.of(),
            List.of(finding)
        );

        assertEquals(
            "REVIEW_REQUIRED",
            decision.status()
        );
    }

    @Test
    void requiresReviewForImportChanges() {

        var decision = gate.evaluate(
            List.of(),
            List.of("Imports changed."),
            List.of()
        );

        assertEquals(
            "REVIEW_REQUIRED",
            decision.status()
        );
    }

    @Test
    void passesAuthorizedChangeWithoutFindings() {

        var changes = List.of(
            new MethodChange(
                "Calculator#findMax(int[])",
                "MODIFIED",
                true
            )
        );

        var decision = gate.evaluate(
            changes,
            List.of(),
            List.of()
        );

        assertEquals("PASS", decision.status());
    }

    @Test
    void blockTakesPriorityOverManualReview() {

        var changes = List.of(
            new MethodChange(
                "Calculator#divide(int, int)",
                "MODIFIED",
                false
            )
        );

        var decision = gate.evaluate(
            changes,
            List.of("Imports changed."),
            List.of()
        );

        assertEquals("BLOCK", decision.status());

        assertTrue(decision.reasons().size() >= 2);
    }
}

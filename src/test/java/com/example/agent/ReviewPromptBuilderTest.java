
package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class ReviewPromptBuilderTest {

    private final ReviewPromptBuilder builder =
            new ReviewPromptBuilder();

    @Test
    void includesEveryAnalyzerFinding() {

        CodeReviewFinding correctness =
                new CodeReviewFinding(
                    "MEDIUM",
                    "findMax",
                    "Incorrect maximum initialization",
                    "{-8, -3, -5}",
                    "Initialize from the first element",
                    12
                );

        CodeReviewFinding security =
                new CodeReviewFinding(
                    "HIGH",
                    "shutdown",
                    "Unexpected process termination",
                    "System.exit(1)",
                    "Review termination behavior",
                    20
                );

        CodeReviewFinding reliability =
                new CodeReviewFinding(
                    "MEDIUM",
                    "startWorkers",
                    "Unbounded thread creation",
                    "Executors.newCachedThreadPool()",
                    "Use a bounded executor",
                    9
                );

        String prompt = builder.build(
            "Example.java",
            "class Example {}",
            List.of(correctness),
            List.of(security),
            List.of(reliability)
        );

        assertAll(
            () -> assertTrue(prompt.contains(
                "Incorrect maximum initialization")),
            () -> assertTrue(prompt.contains(
                "Unexpected process termination")),
            () -> assertTrue(prompt.contains(
                "Unbounded thread creation")),
            () -> assertTrue(prompt.contains(
                "Line: 12")),
            () -> assertTrue(prompt.contains(
                "Line: 20")),
            () -> assertTrue(prompt.contains(
                "Line: 9")),
            () -> assertTrue(prompt.contains(
                "NOT_EXECUTED"))
        );
    }

    @Test
    void handlesEmptyFindings() {

        String prompt = builder.build(
            "Calculator.java",
            "class Calculator {}",
            List.of(),
            List.of(),
            List.of()
        );

        assertTrue(prompt.contains(
            "No findings from this analyzer."));

        assertTrue(prompt.contains(
            "class Calculator {}"));

        assertTrue(prompt.contains(
            "JAVA CODE REVIEW"));
    }
}


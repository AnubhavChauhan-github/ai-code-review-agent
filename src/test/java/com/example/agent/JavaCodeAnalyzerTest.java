
package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class JavaChangeAnalyzerTest {

    private final JavaChangeAnalyzer analyzer =
            new JavaChangeAnalyzer(
                new JavaCodeAnalyzer(),
                new AiRiskAnalyzer(),
                new ProductionReliabilityAnalyzer()
            );

    private static final String ORIGINAL = """
        class Calculator {
            int findMax(int[] numbers) {
                int max = 0;
                for (int n : numbers) {
                    if (n > max) {
                        max = n;
                    }
                }
                return max;
            }

            int divide(int a, int b) {
                return a / b;
            }
        }
        """;

    private static final String PROPOSED = """
        class Calculator {
            int findMax(int[] numbers) {
                int max = numbers[0];
                for (int n : numbers) {
                    if (n > max) {
                        max = n;
                    }
                }
                return max;
            }

            int divide(int a, int b) {
                System.exit(1);
                return a / b;
            }
        }
        """;

    @Test
    void detectsUnauthorizedMethodModification() {

        var result = analyzer.analyze(
            "Original.java",
            "Proposed.java",
            ORIGINAL,
            PROPOSED,
            List.of("findMax")
        );

        assertTrue(result.methodChanges().stream()
            .anyMatch(change ->
                change.method().contains("divide")
                && !change.allowed()
                && change.changeType().equals("MODIFIED")
            ));
    }

    @Test
    void detectsNewlyIntroducedTerminationRisk() {

        var result = analyzer.analyze(
            "Original.java",
            "Proposed.java",
            ORIGINAL,
            PROPOSED,
            List.of("findMax")
        );

        assertTrue(result.introducedFindings().stream()
            .anyMatch(finding ->
                finding.severity().equals("HIGH")
                && finding.method().equals("divide")
                && finding.reproducingInput()
                    .contains("System.exit(1)")
            ));
    }

    @Test
    void ignoresExistingRiskWhenCodeIsUnchanged() {

        var result = analyzer.analyze(
            "Original.java",
            "Proposed.java",
            PROPOSED,
            PROPOSED,
            List.of("findMax")
        );

        assertTrue(result.introducedFindings().isEmpty());
        assertTrue(result.methodChanges().isEmpty());
    }

    @Test
    void detectsImportChanges() {

        String original = """
            class Service {
                void run() {}
            }
            """;

        String proposed = """
            import java.util.concurrent.Executors;
            class Service {
                void run() {}
            }
            """;

        var result = analyzer.analyze(
            "Original.java",
            "Proposed.java",
            original,
            proposed,
            List.of()
        );

        assertTrue(result.warnings().stream()
            .anyMatch(warning ->
                warning.contains("Imports changed")
            ));
    }
}


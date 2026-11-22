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
            List.of("Calculator#findMax(int[])")
        );

        assertTrue(result.methodChanges().stream()
            .anyMatch(change ->
                change.method().contains("divide")
                && !change.allowed()
                && change.changeType().equals("MODIFIED")
            ));

		assertTrue(result.methodChanges().stream()
		        .anyMatch(change ->
		            change.method().equals("Calculator#findMax(int[])")
		            && change.allowed()
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
    

		@Test
		void rejectsSameMethodNameInAnotherClass() {
		
		    String before = """
		        class Calculator {
		            int findMax(int[] values) {
		                return 1;
		            }
		        }
		
		        class OtherCalculator {
		            int findMax(int[] values) {
		                return 2;
		            }
		        }
		        """;
		
		    String after = """
		        class Calculator {
		            int findMax(int[] values) {
		                return 3;
		            }
		        }
		
		        class OtherCalculator {
		            int findMax(int[] values) {
		                return 4;
		            }
		        }
		        """;
		
		    var result = analyzer.analyze(
		        "Before.java",
		        "After.java",
		        before,
		        after,
		        List.of("Calculator#findMax(int[])")
		    );
		
		    assertTrue(result.methodChanges().stream()
		        .anyMatch(c ->
		            c.method().equals("Calculator#findMax(int[])")
		            && c.allowed()));
		
		    assertTrue(result.methodChanges().stream()
		        .anyMatch(c ->
		            c.method().equals("OtherCalculator#findMax(int[])")
		            && !c.allowed()));
		}
		
		@Test
		void rejectsUnauthorizedOverloadChange() {
		
		    String before = """
		        class Calculator {
		            int findMax(int[] values) {
		                return 1;
		            }
		
		            int findMax(int a, int b) {
		                return a;
		            }
		        }
		        """;
		
		    String after = """
		        class Calculator {
		            int findMax(int[] values) {
		                return 2;
		            }
		
		            int findMax(int a, int b) {
		                return b;
		            }
		        }
		        """;
		
		    var result = analyzer.analyze(
		        "Before.java",
		        "After.java",
		        before,
		        after,
		        List.of("Calculator#findMax(int[])")
		    );
		
		    assertTrue(result.methodChanges().stream()
		        .anyMatch(c ->
		            c.method().equals("Calculator#findMax(int, int)")
		            && !c.allowed()));
		}
		
		@Test
		void detectsClassDeclarationChange() {
		
		    String before = """
		        class Service {
		            void run() {}
		        }
		        """;
		
		    String after = """
		        public final class Service {
		            void run() {}
		        }
		        """;
		
		    var result = analyzer.analyze(
		        "Before.java",
		        "After.java",
		        before,
		        after,
		        List.of()
		    );
		
		    assertTrue(result.warnings().stream()
		        .anyMatch(w ->
		            w.contains("Type declaration changed")));
		}
		
		@Test
		void detectsDangerousStaticInitializer() {
		
		    String before = """
		        class Service {
		            void run() {}
		        }
		        """;
		
		    String after = """
		        class Service {
		            static {
		                System.exit(1);
		            }
		
		            void run() {}
		        }
		        """;
		
		    var result = analyzer.analyze(
		        "Before.java",
		        "After.java",
		        before,
		        after,
		        List.of()
		    );
		
		    assertTrue(result.warnings().stream()
		        .anyMatch(w ->
		            w.contains("initializers changed")));
		
		    assertTrue(result.introducedFindings().stream()
		        .anyMatch(f ->
		            f.severity().equals("HIGH")
		            && f.method().equals("Service.<clinit>")
		            && f.reproducingInput().contains("System.exit(1)")));
		}
		
		@Test
		void doesNotReportUnchangedStaticRiskAsIntroduced() {
		
		    String source = """
		        class Service {
		            static {
		                System.exit(1);
		            }
		
		            void run() {}
		        }
		        """;
		
		    var result = analyzer.analyze(
		        "Before.java",
		        "After.java",
		        source,
		        source,
		        List.of()
		    );
		
		    assertTrue(result.introducedFindings().isEmpty());
		    assertTrue(result.warnings().isEmpty());
		}

}


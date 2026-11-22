
package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class AiRiskAnalyzerTest {

    private final AiRiskAnalyzer analyzer =
            new AiRiskAnalyzer();

    @Test
    void detectsApplicationTermination() {

        String source = """
            class Service {
                void stopApplication() {
                    System.exit(1);
                }
            }
            """;

        List<CodeReviewFinding> findings =
                analyzer.analyze(source);

        assertEquals(1, findings.size());
        assertEquals("HIGH", findings.get(0).severity());
        assertEquals(3, findings.get(0).lineNumber());
    }

    @Test
    void detectsOperatingSystemExecution() {

        String source = """
            class Service {
                void runCommand() throws Exception {
                    Runtime.getRuntime().exec("echo test");
                }
            }
            """;

        List<CodeReviewFinding> findings =
                analyzer.analyze(source);

        assertEquals(1, findings.size());
        assertEquals("HIGH", findings.get(0).severity());
    }

    @Test
    void detectsPotentiallyUnboundedLoop() {

        String source = """
            class Service {
                void run() {
                    while (true) {
                        System.out.println("running");
                    }
                }
            }
            """;

        List<CodeReviewFinding> findings =
                analyzer.analyze(source);

        assertEquals(1, findings.size());
        assertEquals("MEDIUM", findings.get(0).severity());
    }

    @Test
    void doesNotFlagOrdinaryBusinessLogic() {

        String source = """
            class Calculator {
                int add(int a, int b) {
                    return a + b;
                }
            }
            """;

        assertTrue(analyzer.analyze(source).isEmpty());
    }

    @Test
    void doesNotFlagLoopWithExplicitBreak() {

        String source = """
            class Service {
                void run() {
                    while (true) {
                        break;
                    }
                }
            }
            """;

        assertTrue(analyzer.analyze(source).isEmpty());
    }
}

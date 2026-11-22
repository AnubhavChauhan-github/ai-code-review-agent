
package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProductionReliabilityAnalyzerTest {

    private final ProductionReliabilityAnalyzer analyzer =
            new ProductionReliabilityAnalyzer();

    @Test
    void detectsCachedThreadPool() {

        String source = """
            import java.util.concurrent.Executors;

            class Worker {
                void start() {
                    var pool = Executors.newCachedThreadPool();
                }
            }
            """;

        var findings = analyzer.analyze(source);

        assertEquals(1, findings.size());
        assertEquals("MEDIUM", findings.get(0).severity());
    }

    @Test
    void detectsQueueWithoutCapacity() {

        String source = """
            import java.util.concurrent.LinkedBlockingQueue;

            class Worker {
                void start() {
                    var queue = new LinkedBlockingQueue<String>();
                }
            }
            """;

        var findings = analyzer.analyze(source);

        assertEquals(1, findings.size());
        assertTrue(findings.get(0).problem()
                .contains("capacity"));
    }

    @Test
    void ignoresExplicitlyBoundedQueue() {

        String source = """
            import java.util.concurrent.LinkedBlockingQueue;

            class Worker {
                void start() {
                    var queue =
                        new LinkedBlockingQueue<String>(100);
                }
            }
            """;

        assertTrue(analyzer.analyze(source).isEmpty());
    }
}


package com.example.agent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

@Component
public class ProductionReliabilityAnalyzer {

    public List<CodeReviewFinding> analyze(
            String sourceCode) {

        List<CodeReviewFinding> findings =
                new ArrayList<>();

        if (sourceCode == null || sourceCode.isBlank()) {
            return findings;
        }

        CompilationUnit unit;

        try {
            unit = StaticJavaParser.parse(sourceCode);
        } catch (ParseProblemException e) {
            return findings;
        }

        for (MethodDeclaration method :
                unit.findAll(MethodDeclaration.class)) {

            String methodName = method.getNameAsString();

            // Detect potentially unbounded thread pools.
            for (MethodCallExpr call :
                    method.findAll(MethodCallExpr.class)) {

                String scope = call.getScope()
                        .map(Object::toString)
                        .orElse("");

                if (call.getNameAsString()
                            .equals("newCachedThreadPool")
                        && scope.equals("Executors")) {

                    findings.add(new CodeReviewFinding(
                        "MEDIUM",
                        methodName,
                        "Cached thread pool may grow to "
                            + "a large number of threads "
                            + "under sustained load.",
                        call.toString(),
                        "Consider a bounded executor "
                            + "with an appropriate queue "
                            + "and rejection policy.",
                        call.getBegin()
                            .map(p -> p.line)
                            .orElse(null)
                    ));
                }
            }

            // Detect queues with no explicit capacity.
            for (ObjectCreationExpr creation :
                    method.findAll(ObjectCreationExpr.class)) {

                boolean unboundedQueue =
                        creation.getType()
                            .getNameAsString()
                            .equals("LinkedBlockingQueue")
                        && creation.getArguments().isEmpty();

                if (unboundedQueue) {

                    findings.add(new CodeReviewFinding(
                        "MEDIUM",
                        methodName,
                        "LinkedBlockingQueue created without "
                            + "an explicit capacity. Queue "
                            + "growth may exhaust memory.",
                        creation.toString(),
                        "Consider setting a capacity "
                            + "and applying backpressure.",
                        creation.getBegin()
                            .map(p -> p.line)
                            .orElse(null)
                    ));
                }
            }
        }

        return findings;
    }
}

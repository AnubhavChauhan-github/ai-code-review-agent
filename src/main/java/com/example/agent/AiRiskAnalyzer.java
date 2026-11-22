
package com.example.agent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

@Component
public class AiRiskAnalyzer {

    public List<CodeReviewFinding> analyze(String source) {

        List<CodeReviewFinding> findings = new ArrayList<>();

        if (source == null || source.isBlank()) {
            return findings;
        }

        CompilationUnit unit;

        try {
            unit = StaticJavaParser.parse(source);
        } catch (ParseProblemException e) {
            return findings;
        }

        for (MethodDeclaration method :
                unit.findAll(MethodDeclaration.class)) {

            String methodName = method.getNameAsString();

            for (MethodCallExpr call :
                    method.findAll(MethodCallExpr.class)) {

                String name = call.getNameAsString();

                String scope = call.getScope()
                        .map(Object::toString)
                        .orElse("");

                boolean shutdown =
                        name.equals("exit")
                                && scope.equals("System")
                        || name.equals("halt")
                                && scope.equals("Runtime.getRuntime()");

                boolean command =
                        name.equals("exec")
                                && scope.equals("Runtime.getRuntime()")
                        || name.equals("start")
                                && scope.startsWith("new ProcessBuilder(");

                if (shutdown) {
                    findings.add(new CodeReviewFinding(
                        "HIGH",
                        methodName,
                        "Process termination detected. "
                            + "This could shut down the application.",
                        call.toString(),
                        "Check whether termination is authorized "
                            + "and appropriate for this application.",
                        call.getBegin()
                            .map(p -> p.line)
                            .orElse(null)
                    ));
                }

                if (command) {
                    findings.add(new CodeReviewFinding(
                        "HIGH",
                        methodName,
                        "Operating-system command execution detected. "
                            + "Review command sources and permissions.",
                        call.toString(),
                        "Avoid unnecessary shell execution. "
                            + "Validate inputs and restrict privileges.",
                        call.getBegin()
                            .map(p -> p.line)
                            .orElse(null)
                    ));
                }
            }

            for (WhileStmt loop :
                    method.findAll(WhileStmt.class)) {

                if (!loop.getCondition().isBooleanLiteralExpr()
                        || !loop.getCondition()
                            .asBooleanLiteralExpr()
                            .getValue()) {
                    continue;
                }

                boolean hasExit =
                        !loop.findAll(BreakStmt.class).isEmpty()
                        || !loop.findAll(ReturnStmt.class).isEmpty()
                        || !loop.findAll(ThrowStmt.class).isEmpty();

                if (!hasExit) {
                    findings.add(new CodeReviewFinding(
                        "MEDIUM",
                        methodName,
                        "Potentially unbounded while(true) loop "
                            + "with no obvious exit statement.",
                        "while (true)",
                        "Review loop termination, cancellation "
                            + "and resource limits.",
                        loop.getBegin()
                            .map(p -> p.line)
                            .orElse(null)
                    ));
                }
            }
        }

        return findings;
    }
}

package com.example.agent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

@Component
public class JavaCodeAnalyzer {

    public List<CodeReviewFinding> analyzeFindings(
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
            // This rule does not analyze invalid syntax.
            return findings;
        }

        for (MethodDeclaration method :
                unit.findAll(MethodDeclaration.class)) {

            if (hasFaultyMaxPattern(method)) {

                findings.add(new CodeReviewFinding(
                    "MEDIUM",
                    method.getNameAsString(),
                    "Maximum initialized to zero. "
                        + "Negative-only arrays can return zero "
                        + "instead of their largest element.",
                    "{-8, -3, -5}: expected -3, "
                        + "faulty implementation returns 0.",
                    "After validating the input, initialize "
                        + "max using numbers[0]."
                ));
            }
        }

        return findings;
    }

    private boolean hasFaultyMaxPattern(
            MethodDeclaration method) {

        // This first rule covers findMax(int[]).
        if (!method.getNameAsString().equals("findMax")
                || !method.getType().asString().equals("int")
                || method.getParameters().size() != 1
                || !method.getParameter(0).getType()
                        .isArrayType()
                || !method.getParameter(0).getType()
                        .asArrayType()
                        .getComponentType()
                        .asString().equals("int")
                || method.getBody().isEmpty()) {

            return false;
        }

        String arrayName =
                method.getParameter(0).getNameAsString();

        BlockStmt body = method.getBody().get();
        List<Statement> statements = body.getStatements();

        // Search top-level statements in the method.
        // Validation statements may precede max = 0.
        for (int i = 0; i < statements.size() - 2; i++) {

            Statement declaration = statements.get(i);

            if (!declaration.isExpressionStmt()
                    || !declaration.asExpressionStmt()
                        .getExpression()
                        .isVariableDeclarationExpr()) {
                continue;
            }

            var variables = declaration
                    .asExpressionStmt()
                    .getExpression()
                    .asVariableDeclarationExpr()
                    .getVariables();

            if (variables.size() != 1) {
                continue;
            }

            VariableDeclarator variable = variables.get(0);

            if (!variable.getNameAsString().equals("max")
                    || !variable.getType()
                        .asString().equals("int")
                    || variable.getInitializer().isEmpty()) {
                continue;
            }

            Expression initialValue =
                    variable.getInitializer().get();

            if (!initialValue.isIntegerLiteralExpr()
                    || !initialValue.asIntegerLiteralExpr()
                        .getValue().equals("0")) {
                continue;
            }

            // Require the expected loop directly after
            // the initialization, followed by return max.
            Statement loop = statements.get(i + 1);
            Statement result = statements.get(i + 2);

            if (!loop.isForEachStmt()
                    || !result.isReturnStmt()
                    || result.asReturnStmt()
                        .getExpression().isEmpty()) {
                continue;
            }

            Expression returnValue = result
                    .asReturnStmt()
                    .getExpression()
                    .get();

            if (!returnValue.isNameExpr()
                    || !returnValue.asNameExpr()
                        .getNameAsString().equals("max")) {
                continue;
            }

            if (hasExpectedMaxLoop(
                    loop.asForEachStmt(), arrayName)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasExpectedMaxLoop(
            ForEachStmt loop, String arrayName) {

        // Example: for (int number : numbers)
        if (!loop.getIterable().isNameExpr()
                || !loop.getIterable()
                    .asNameExpr()
                    .getNameAsString().equals(arrayName)) {
            return false;
        }

        String itemName = loop.getVariableDeclarator()
                .getNameAsString();

        if (!loop.getBody().isBlockStmt()) {
            return false;
        }

        List<Statement> loopStatements =
                loop.getBody().asBlockStmt().getStatements();

        if (loopStatements.size() != 1
                || !loopStatements.get(0).isIfStmt()) {
            return false;
        }

        IfStmt condition = loopStatements.get(0).asIfStmt();

        if (condition.getElseStmt().isPresent()
                || !condition.getCondition().isBinaryExpr()) {
            return false;
        }

        BinaryExpr comparison =
                condition.getCondition().asBinaryExpr();

        if (comparison.getOperator()
                    != BinaryExpr.Operator.GREATER
                || !isName(comparison.getLeft(), itemName)
                || !isName(comparison.getRight(), "max")) {
            return false;
        }

        Statement thenBranch = condition.getThenStmt();

        // Accept if (...) { max = number; }
        if (!thenBranch.isBlockStmt()) {
            return false;
        }

        List<Statement> updates =
                thenBranch.asBlockStmt().getStatements();

        if (updates.size() != 1
                || !updates.get(0).isExpressionStmt()
                || !updates.get(0)
                    .asExpressionStmt()
                    .getExpression()
                    .isAssignExpr()) {
            return false;
        }

        AssignExpr assignment = updates.get(0)
                .asExpressionStmt()
                .getExpression()
                .asAssignExpr();

        return assignment.getOperator()
                    == AssignExpr.Operator.ASSIGN
                && isName(assignment.getTarget(), "max")
                && isName(assignment.getValue(), itemName);
    }

    private boolean isName(Expression expression,
            String expectedName) {

        return expression.isNameExpr()
                && expression.asNameExpr()
                    .getNameAsString()
                    .equals(expectedName);
    }

    public String analyze(String sourceCode) {

        List<CodeReviewFinding> findings =
                analyzeFindings(sourceCode);

        if (findings.isEmpty()) {
            return "No issues detected by the current Java rules.";
        }

        StringBuilder result = new StringBuilder();

        for (CodeReviewFinding finding : findings) {
            result.append("Potential correctness bug detected.\n");
            result.append("Severity: ")
                    .append(finding.severity()).append("\n");
            result.append("Method: ")
                    .append(finding.method()).append("\n");
            result.append("Problem: ")
                    .append(finding.problem()).append("\n");
            result.append("Reproducing input: ")
                    .append(finding.reproducingInput()).append("\n");
            result.append("Suggested fix: ")
                    .append(finding.suggestedFix()).append("\n");
            result.append("This is a heuristic finding, ")
                    .append("not a verified bug.\n");
        }

        return result.toString();
    }
}

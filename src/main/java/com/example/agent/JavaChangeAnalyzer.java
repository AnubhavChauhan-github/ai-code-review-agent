
package com.example.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import org.springframework.stereotype.Component;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

@Component
public class JavaChangeAnalyzer {

    private final JavaCodeAnalyzer correctnessAnalyzer;
    private final AiRiskAnalyzer riskAnalyzer;
    private final ProductionReliabilityAnalyzer reliabilityAnalyzer;

    public JavaChangeAnalyzer(
            JavaCodeAnalyzer correctnessAnalyzer,
            AiRiskAnalyzer riskAnalyzer,
            ProductionReliabilityAnalyzer reliabilityAnalyzer) {

        this.correctnessAnalyzer = correctnessAnalyzer;
        this.riskAnalyzer = riskAnalyzer;
        this.reliabilityAnalyzer = reliabilityAnalyzer;
    }

    public ChangeReviewResponse analyze(
            String originalFileName,
            String proposedFileName,
            String originalSource,
            String proposedSource,
            List<String> allowedMethods) {

        if (originalSource == null || proposedSource == null) {
            throw new IllegalArgumentException(
                    "Both source files are required");
        }

        // Parsing errors are intentionally propagated.
        CompilationUnit original =
                StaticJavaParser.parse(originalSource);

        CompilationUnit proposed =
                StaticJavaParser.parse(proposedSource);

        Set<String> allowed = Set.copyOf(
                allowedMethods == null
                    ? List.<String>of()
                    : allowedMethods
        );

        Map<String, MethodDeclaration> before =
                collectMethods(original);

        Map<String, MethodDeclaration> after =
                collectMethods(proposed);

        List<MethodChange> changes = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (String key : before.keySet()) {

            MethodDeclaration oldMethod = before.get(key);
            MethodDeclaration newMethod = after.get(key);

            boolean permitted = allowed.contains(key);

            if (newMethod == null) {
                changes.add(new MethodChange(
                        key, "REMOVED", permitted));

                if (!permitted) {
                    warnings.add(
                        "Method removed outside allowed scope: "
                        + key);
                }

            } else if (!oldMethod.toString()
                    .equals(newMethod.toString())) {

                changes.add(new MethodChange(
                        key, "MODIFIED", permitted));

                if (!permitted) {
                    warnings.add(
                        "Method modified outside allowed scope: "
                        + key);
                }
            }
        }

        for (String key : after.keySet()) {

            if (before.containsKey(key)) {
                continue;
            }

            MethodDeclaration newMethod = after.get(key);

            boolean permitted = allowed.contains(key);

            changes.add(new MethodChange(
                    key, "ADDED", permitted));

            if (!permitted) {
                warnings.add(
                    "Method added outside allowed scope: "
                    + key);
            }
        }

        // Detect selected changes not covered by method comparison.
        if (!original.getImports().toString()
                .equals(proposed.getImports().toString())) {

            warnings.add(
                "Imports changed. Review dependency and "
                + "capability changes manually.");
        }
        

		Map<String, String> originalTypes =
		        collectTypeHeaders(original);
		
		Map<String, String> proposedTypes =
		        collectTypeHeaders(proposed);
		
		for (String typeName : originalTypes.keySet()) {
		
		    if (!proposedTypes.containsKey(typeName)) {
		        warnings.add(
		            "Type removed: " + typeName);
		    } else if (!originalTypes.get(typeName)
		            .equals(proposedTypes.get(typeName))) {
		
		        warnings.add(
		            "Type declaration changed: " + typeName
		            + ". Review modifiers, annotations, "
		            + "inheritance and interfaces.");
		    }
		}
		
		for (String typeName : proposedTypes.keySet()) {
		
		    if (!originalTypes.containsKey(typeName)) {
		        warnings.add(
		            "Type added: " + typeName);
		    }
		}
		
		if (!original.getPackageDeclaration().toString()
		        .equals(proposed.getPackageDeclaration().toString())) {
		
		    warnings.add(
		        "Package declaration changed.");
		}

        
        if (!nonMethodMembers(original)
                .equals(nonMethodMembers(proposed))) {

            warnings.add(
                "Fields, constructors, or initializers changed. "
                + "Manual scope review required.");
        }
        

        List<CodeReviewFinding> originalFindings =
                analyzeAll(originalSource);

        List<CodeReviewFinding> proposedFindings =
                analyzeAll(proposedSource);

        List<CodeReviewFinding> introducedFindings =
                findIntroduced(
                    originalFindings, proposedFindings);

        return new ChangeReviewResponse(
                originalFileName,
                proposedFileName,
                changes,
                warnings,
                introducedFindings,
                "NOT_EXECUTED"
        );
    }


		private Map<String, MethodDeclaration> collectMethods(
		        CompilationUnit unit) {
		
		    Map<String, MethodDeclaration> methods =
		            new LinkedHashMap<>();
		
		    for (MethodDeclaration method :
		            unit.findAll(MethodDeclaration.class)) {
		
		        String typeName = method
		                .findAncestor(TypeDeclaration.class)
		                .map(this::typeKey)
		                .orElse("UnknownType");
		
		        String key = typeName + "#"
		                + method.getSignature().asString();
		
		        methods.put(key, method);
		    }
		
		    return methods;
		}
		
		private String typeKey(TypeDeclaration<?> type) {
		
		    List<String> names = new ArrayList<>();
		
		    Node current = type;
		
		    while (current != null) {
		
		        if (current instanceof TypeDeclaration<?> declaration) {
		            names.add(declaration.getNameAsString());
		        }
		
		        current = current.getParentNode().orElse(null);
		    }
		
		    Collections.reverse(names);
		
		    return String.join(".", names);
		}


		private Map<String, String> collectTypeHeaders(
		        CompilationUnit unit) {
		
		    Map<String, String> headers = new LinkedHashMap<>();
		
		    for (TypeDeclaration<?> type :
		            unit.findAll(TypeDeclaration.class)) {
		
		        StringBuilder header = new StringBuilder();
		
		        header.append(type.getClass().getSimpleName())
		              .append("|")
		              .append(type.getModifiers())
		              .append("|")
		              .append(type.getAnnotations());
		
		        if (type.isClassOrInterfaceDeclaration()) {
		
		            var declaration =
		                    type.asClassOrInterfaceDeclaration();
		
		            header.append("|interface=")
		                  .append(declaration.isInterface());
		
		            header.append("|extends=")
		                  .append(declaration.getExtendedTypes());
		
		            header.append("|implements=")
		                  .append(declaration.getImplementedTypes());
		
		            header.append("|generics=")
		                  .append(declaration.getTypeParameters());
		        }
		
		        headers.put(typeKey(type), header.toString());
		    }
		
		    return headers;
		}


    private List<String> nonMethodMembers(
            CompilationUnit unit) {

        List<String> members = new ArrayList<>();

        unit.findAll(
            com.github.javaparser.ast.body.FieldDeclaration.class
        ).forEach(f -> members.add("FIELD:" + f.toString()));

        unit.findAll(
            com.github.javaparser.ast.body.ConstructorDeclaration.class
        ).forEach(c -> members.add("CTOR:" + c.toString()));

        unit.findAll(
            com.github.javaparser.ast.body.InitializerDeclaration.class
        ).forEach(i -> members.add("INIT:" + i.toString()));

        return members;
    }

    private List<CodeReviewFinding> analyzeAll(String source) {

        List<CodeReviewFinding> findings =
                new ArrayList<>();

        findings.addAll(
                correctnessAnalyzer.analyzeFindings(source));

        findings.addAll(riskAnalyzer.analyze(source));

        findings.addAll(
                reliabilityAnalyzer.analyze(source));

		findings.addAll(
		    analyzeStaticInitializers(source)
		);


        return findings;
    }
    

	private List<CodeReviewFinding> analyzeStaticInitializers(
	        String source) {
	
	    List<CodeReviewFinding> findings =
	            new ArrayList<>();
	
	    CompilationUnit unit =
	            StaticJavaParser.parse(source);
	
	    for (InitializerDeclaration initializer :
	            unit.findAll(InitializerDeclaration.class)) {
	
	        if (!initializer.isStatic()) {
	            continue;
	        }
	
	        for (MethodCallExpr call :
	                initializer.findAll(MethodCallExpr.class)) {
	
	            boolean systemExit =
	                    call.getNameAsString().equals("exit")
	                    && call.getScope()
	                        .map(Object::toString)
	                        .orElse("")
	                        .equals("System");
	
	            if (systemExit) {
	
	                String typeName = initializer
	                        .findAncestor(TypeDeclaration.class)
	                        .map(this::typeKey)
	                        .orElse("UnknownType");
	
	                findings.add(new CodeReviewFinding(
	                    "HIGH",
	                    typeName + ".<clinit>",
	                    "System.exit() detected in a "
	                        + "static initializer. Class "
	                        + "initialization may terminate "
	                        + "the application.",
	                    call.toString(),
	                    "Remove unexpected process "
	                        + "termination from class "
	                        + "initialization.",
	                    call.getBegin()
	                        .map(p -> p.line)
	                        .orElse(null)
	                ));
	            }
	        }
	    }
	
	    return findings;
	}


    private List<CodeReviewFinding> findIntroduced(
            List<CodeReviewFinding> before,
            List<CodeReviewFinding> after) {

        Map<String, Integer> previousCounts =
                new HashMap<>();

        for (CodeReviewFinding finding : before) {
            previousCounts.merge(
                    findingKey(finding), 1, Integer::sum);
        }

        List<CodeReviewFinding> introduced =
                new ArrayList<>();

        for (CodeReviewFinding finding : after) {

            String key = findingKey(finding);
            int previous = previousCounts.getOrDefault(key, 0);

            if (previous > 0) {
                previousCounts.put(key, previous - 1);
            } else {
                introduced.add(finding);
            }
        }

        return introduced;
    }

    private String findingKey(CodeReviewFinding finding) {

        return finding.severity() + "|"
                + finding.method() + "|"
                + finding.problem() + "|"
                + finding.reproducingInput();
    }
}

package com.example.agent;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/changes")
public class ChangeReviewController {

    private final JavaFileTool fileTool;
    private final JavaChangeAnalyzer changeAnalyzer;

    public ChangeReviewController(
            JavaFileTool fileTool,
            JavaChangeAnalyzer changeAnalyzer) {

        this.fileTool = fileTool;
        this.changeAnalyzer = changeAnalyzer;
    }

    @PostMapping("/review")
    public ChangeReviewResponse review(
            @RequestBody ChangeReviewRequest request) {

        validateName(request.originalFileName());
        validateName(request.proposedFileName());

        String original = readSource(request.originalFileName());
        String proposed = readSource(request.proposedFileName());

        return changeAnalyzer.analyze(
                request.originalFileName(),
                request.proposedFileName(),
                original,
                proposed,
                request.allowedMethods()
        );
    }

    private void validateName(String fileName) {

        if (fileName == null ||
                !fileName.matches("[A-Za-z0-9_-]+\\.java")) {

            throw new IllegalArgumentException(
                    "Invalid Java filename");
        }
    }

    private String readSource(String fileName) {

        String source = fileTool.readFile(fileName);

        if (source == null ||
                source.startsWith("Invalid Java filename") ||
                source.startsWith("File access denied") ||
                source.startsWith("File exceeds") ||
                source.startsWith("Unable to read")) {

            throw new IllegalArgumentException(
                    "Unable to read requested Java file");
        }

        return source;
    }
}

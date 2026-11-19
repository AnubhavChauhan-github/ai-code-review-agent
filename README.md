# Java AI Code Review Agent

A locally hosted, AI-powered Java code review agent built with **Java 21, Spring Boot, Maven, and Ollama**.

The application uses a local large language model (LLM) to analyze Java source files, identify potential bugs, suggest improvements, and recommend JUnit tests.

Unlike a basic chatbot, the agent can request a Java file-reading tool, allowing the application to retrieve source code from a controlled workspace before asking the model to review it.

## Features

- **AI-powered code reviews** — Analyze Java code for potential correctness bugs, edge cases, performance issues, and security concerns.
- **Tool calling** — Support model-requested execution of the `read_java_file` function.
- **Local LLM integration** — Run Ollama without sending source code to a hosted AI API.
- **RESTful APIs** — Interact with the agent using Spring Boot endpoints and Postman.
- **File access validation** — Restrict file-reading operations to authorized Java filenames within a configured workspace.
- **JUnit recommendations** — Generate suggestions for JUnit 5 tests.
- **Lightweight development setup** — Designed to work with compact local language models.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application development |
| Spring Boot | REST APIs and dependency injection |
| Maven | Project build and dependency management |
| Ollama | Local LLM runtime |
| Qwen2.5-Coder 1.5B | Code analysis model |
| Java HttpClient | Communication with Ollama |
| Jackson | JSON serialization and parsing |
| JUnit | Application testing |

## Architecture

```text
             Client / Postman
                    |
                    v
           AgentController
                    |
                    v
        ToolCallingAgentService
                    |
                    v
             Ollama LLM
                    |
           Tool request generated
                    |
                    v
              JavaFileTool
                    |
                    v
           Java source workspace
                    |
            File contents returned
                    |
                    v
              Ollama LLM
                    |
                    v
            AI Code Review
                    |
                    v
              API Response
```

The application supports native Ollama tool calls and an application-level fallback for models that return tool requests as JSON text.

## Project Structure

```text
java-ai-code-review-agent/
├── src/
│   ├── main/
│   │   ├── java/com/example/agent/
│   │   │   ├── AgentController.java
│   │   │   ├── ToolCallingAgentService.java
│   │   │   ├── JavaFileTool.java
│   │   │   ├── OllamaService.java
│   │   │   ├── CodeReviewAgentService.java
│   │   │   ├── ReviewController.java
│   │   │   └── CodeReveiwAgentApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── workspace/
│   ├── Calculator.java
│   └── ReviewAccuracyTest.java
├── pom.xml
├── mvnw
└── README.md
```

## Getting Started

### Prerequisites

Install:

- Java 21
- Maven (or use the included Maven Wrapper)
- Ollama
- Git
- Postman (optional)

### 1. Clone the repository

```bash
git clone https://github.com/AnubhavChauhan-github/java-ai-code-review-agent.git
cd java-ai-code-review-agent
```

### 2. Download the Ollama model

```bash
ollama pull qwen2.5-coder:1.5b
```

Start the Ollama service if it is not already running:

```bash
ollama serve
```

Ollama must be available at:

```text
http://localhost:11434
```

### 3. Start Spring Boot

Using the included Maven Wrapper:

```bash
./mvnw spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

### 4. Add a Java file to the workspace

Create a file named `Calculator.java` inside the `workspace` directory.

Example:

```java
public class Calculator {

    public int divide(int a, int b) {
        return a / b;
    }

    public int findMax(int[] numbers) {
        int max = 0;

        for (int number : numbers) {
            if (number > max) {
                max = number;
            }
        }

        return max;
    }
}
```

This example intentionally contains a correctness bug for arrays containing only negative numbers.

## API Usage

### AI Agent Code Review

**Endpoint**

```http
POST /api/agent/review
```

**URL**

```text
http://localhost:8080/api/agent/review
```

**Request body**

```json
{
  "fileName": "Calculator.java"
}
```

**Response**

The endpoint returns an AI-generated code review as text.

Depending on the model output, the review may include:

- Potential bugs and their severity
- Reproducing inputs
- Suggested fixes
- Edge-case analysis
- JUnit 5 test suggestions

### Example using curl

```bash
curl -X POST http://localhost:8080/api/agent/review \
  -H "Content-Type: application/json" \
  -d '{"fileName":"Calculator.java"}'
```

## How Tool Calling Works

1. The user submits a Java filename.
2. The Spring Boot controller forwards the request to the agent service.
3. The agent sends a tool definition and conversation to Ollama.
4. Ollama requests `read_java_file`, either through native tool calling or a JSON-formatted text response.
5. The Java application validates the requested filename and executes `JavaFileTool`.
6. The source code is sent back to Ollama.
7. Ollama generates the final review.
8. The review is returned to the client.

**Security consideration:** The application validates requested filenames and restricts file access. It is currently intended for local development, not deployment as a publicly accessible service.

## Current Limitations

This project is under active development.

- Small local language models can miss real correctness bugs.
- AI-generated JUnit tests may contain compilation or logic errors.
- Some models generate tool calls as JSON text instead of native tool-call structures.
- Suggested findings are not automatically verified through code execution.
- Long source files may exceed the model's effective context window.
- The API does not yet provide production-grade authentication or authorization.

AI-generated reviews should be treated as suggestions requiring developer verification.

## Planned Improvements

- [ ] Improve code review accuracy with structured evaluation cases
- [ ] Validate AI findings against executable JUnit tests
- [ ] Add a `list_java_files` tool
- [ ] Generate structured JSON review reports
- [ ] Support multi-file Java project reviews
- [ ] Add consistent exception handling and API validation
- [ ] Integrate GitHub Actions for continuous integration
- [ ] Expand evaluation across different local models

## Running Tests

Use Maven:

```bash
./mvnw test
```

Note: Sample Java files in `workspace/` are used as review inputs and are not automatically compiled as Maven test sources.

## Contributing

Contributions, suggestions, and bug reports are welcome. Feel free to open an issue or submit a pull request.

## Project Status

**Work in progress — functional prototype**

The REST API, Ollama integration, and file-reading workflow are implemented. The current development focus is improving the accuracy and reliability of generated code reviews.

## AI Code Review Agent — Latest Features

This project is a Java 21 and Spring Boot application that uses a locally running Ollama model (`qwen2.5-coder:1.5b`) to review Java source code.

### Features

- REST API endpoint for requesting code reviews.
- Ollama-powered tool calling to request Java source files.
- Secure workspace-based file reading with filename and size validation.
- Rule-based Java code analysis with structured findings.
- Detection of a known maximum-value initialization bug.
- Automated JUnit 5 regression-test suggestions for supported findings.
- Unit tests using JUnit 5.

### Example API Request

**POST** `http://localhost:8080/api/agent/review`

```json
{
  "fileName": "Calculator.java"
}
```

### Example Bug Detection

Consider this implementation:

```java
public int findMax(int[] numbers) {
    int max = 0;

    for (int number : numbers) {
        if (number > max) {
            max = number;
        }
    }

    return max;
}
```

For input `{-8, -3, -5}`, the method incorrectly returns `0` instead of `-3`.

The analyzer identifies this as a potential correctness issue and the AI explains the problem.

### Suggested Fix

```java
public int findMax(int[] numbers) {
    if (numbers == null || numbers.length == 0) {
        throw new IllegalArgumentException("Array cannot be null or empty");
    }

    int max = numbers[0];

    for (int number : numbers) {
        if (number > max) {
            max = number;
        }
    }

    return max;
}
```

The null/empty-array exception is an example of an explicitly defined input contract.

### Generated JUnit 5 Test

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CalculatorTest {
    @Test
    void findMaxWithNegativeNumbers() {
        Calculator calculator = new Calculator();
        int[] numbers = {-8, -3, -5};

        assertEquals(-3, calculator.findMax(numbers));
    }
}
```

The suggested regression test demonstrates the expected behaviour. The agent does not automatically compile or execute its generated test templates.

### Current Limitations

The static analyzer uses heuristic rules rather than full Java syntax analysis. AI-generated explanations should be reviewed for accuracy. Syntax-aware parsing, broader rule coverage, and additional integration tests are planned improvements.
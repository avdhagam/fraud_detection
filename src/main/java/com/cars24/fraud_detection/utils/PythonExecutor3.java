package com.cars24.fraud_detection.utils;

import com.cars24.fraud_detection.exception.PythonExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class PythonExecutor3 {
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON parser

    public Map<String, Object> runPythonScript(String scriptName, Object... args) {
        try {
            // Determine Python command based on OS
            String pythonCommand = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";

            // Prepare command: python3 scriptName arg1 arg2 ...
            List<String> command = new ArrayList<>();
            command.add(pythonCommand);
            command.add(scriptName);

            for (Object arg : args) {
                if (arg == null) {
                    command.add("null"); // Handle null values explicitly
                } else if (arg instanceof Map) {
                    command.add(objectMapper.writeValueAsString(arg)); // Convert Map to JSON
                } else {
                    command.add(arg.toString());
                }
            }

            log.info("Executing Python script: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Merge stderr with stdout
            Process process = processBuilder.start();

            // Capture output efficiently
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line).append("\n");
            }
            String scriptOutput = outputBuilder.toString().trim();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Python script failed with exit code {}: {}", exitCode, scriptOutput);
                throw new PythonExecutionException("Python script execution failed with exit code " + exitCode);
            }

            log.info("Python script executed successfully: {}", scriptOutput);

            // Try parsing JSON output safely
            try {
                if (scriptOutput.startsWith("{") && scriptOutput.endsWith("}")) {
                    return objectMapper.readValue(scriptOutput, Map.class);
                }
            } catch (Exception e) {
                log.warn("Output is not a valid JSON, returning as plain text.");
            }

            // If not JSON, return plain text inside a Map
            return Collections.singletonMap("output", scriptOutput.isEmpty() ? "No output from script" : scriptOutput);

        } catch (Exception e) {
            log.error("Error executing Python script: {}", e.getMessage(), e);
            throw new PythonExecutionException("Error executing Python script", e);
        }
    }
}

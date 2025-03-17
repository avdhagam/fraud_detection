package com.cars24.fraud_detection.utils;

import com.cars24.fraud_detection.exception.PythonExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;


@Service
public class PythonExecutor {
    private static final Logger log = LoggerFactory.getLogger(PythonExecutor.class);
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON parser

    public Map<String, Object> runPythonScript(String scriptName, Object... args) {
        try {
            // Use platform-independent Python command
            String pythonCommand = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";

            // Prepare command: python3 scriptName arg1 arg2 ...
            List<String> command = new ArrayList<>();
            command.add(pythonCommand);
            command.add(scriptName);

            for (Object arg : args) {
                if (arg instanceof Map) {
                    command.add(objectMapper.writeValueAsString(arg).replace("\n", "").trim()); // Convert Map to JSON
                } else {
                    command.add(arg.toString());
                }
            }

            log.info("Executing Python script: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Merge stderr with stdout

            Process process = processBuilder.start();

            // Capture output efficiently (line-by-line)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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

            try {
                // Parse the JSON output into a Map
                Map<String, Object> result = objectMapper.readValue(scriptOutput, Map.class);
                log.info("Python script executed successfully with parsed JSON result: {}", result);
                return result;
            } catch (Exception jsonEx) {
                log.warn("Failed to parse Python output as JSON. Raw output:\n{}", scriptOutput);
                Map<String, Object> result = new HashMap<>();
                result.put("output", scriptOutput);
                result.put("parse_error", "Could not parse output as JSON");
                return result;
            }
        } catch (Exception e) {
            log.error("Error executing Python script: {}", e.getMessage(), e);
            throw new PythonExecutionException("Error executing Python script", e);
        }
    }
}